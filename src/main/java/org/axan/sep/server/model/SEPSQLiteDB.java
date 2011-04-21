package org.axan.sep.server.model;

import java.io.File;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;
import java.util.Vector;
import java.util.logging.Logger;

import org.axan.eplib.orm.sqlite.SQLiteDB;
import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteStatementJob;
import org.axan.eplib.utils.Basic;
import org.axan.eplib.utils.Reflect;
import org.axan.sep.common.Protocol.eBuildingType;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.SEPUtils.RealLocation;
import org.axan.sep.common.GameConfigCopier;
import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.PlayerGameBoard;
import org.axan.sep.common.SEPUtils;
import org.axan.sep.common.GameConfigCopier.GameConfigCopierException;
import org.axan.sep.server.SEPServer;
import org.axan.sep.server.model.GameBoard.ATurnResolvingEvent;
import org.axan.sep.server.model.ISEPServerDataBase;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteJob;
import com.almworks.sqlite4java.SQLiteStatement;

public class SEPSQLiteDB implements ISEPServerDataBase, Serializable
{

	/** Serialization version */
	private static final long serialVersionUID = 1L;
	
	private static final Logger log = Logger.getLogger(SEPSQLiteDB.class.getName());
	
	private static class SQLiteDBGameConfigInvocationHandler implements InvocationHandler
	{
		private final SEPSQLiteDB sepDB;
		private final SQLiteDB db;
		
		public SQLiteDBGameConfigInvocationHandler(SEPSQLiteDB sepDB)
		{
			this.sepDB = sepDB;
			this.db = sepDB.getDB();
		}
		
		@Override
		public Object invoke(Object proxy, final Method method, Object[] args) throws Throwable
		{
			if (method.getDeclaringClass().equals(Object.class))
			{
				return method.invoke(this, args);
			}
			else if (!method.getDeclaringClass().equals(IGameConfig.class))
			{
				throw new SEPServer.SEPImplementationException("SQLiteDBGameConfigInvocationHandler must be used with IGameConfig.class proxy.");
			}
			else
			{
				/*
				 * Setters must start with "set", have arguments and return void.
				 */
				if (method.getName().startsWith("set") && method.getReturnType().equals(void.class) && args != null && args.length > 0)
				{				
					// Setter
					String key = method.getName().substring(3);
					
					int i;
					for(i=0; i < args.length-1; ++i)
					{
						if (Enum.class.isInstance(args[i]))
						{
							key += '-'+args[i].toString();
						}
						else
						{
							break;
						}
					}
					
					if (i+1 == args.length)
					{						
						db.exec("INSERT OR REPLACE INTO GameConfig (key, value) VALUES ('%s', '%s');", key, args[0].toString());
					}
					else
					{
						for(int j=0; i+j < args.length; ++j)
						{
							db.exec("INSERT OR REPLACE INTO GameConfig (key, value) VALUES ('%s-%d', '%s');", key, j, args[i+j].toString());
						}
					}
					
					return null;
				}
				/*
				 * Getters must do not return void and have only Enum<?> arguments.
				 */
				else if (!void.class.equals(method.getReturnType()))
				{
					// Getter
					String key = method.getName();
					if (method.getName().startsWith("get") || method.getName().startsWith("has"))
					{
						key = method.getName().substring(3);
					}
					else if (method.getName().startsWith("is"))
					{
						key = method.getName().substring(2);
					}
					
					if (args != null) for(int i=0; i<args.length; ++i)
					{
						if (!Enum.class.isInstance(args[i]))
						{
							throw new SEPServer.SEPImplementationException("Invalid IGameConfig: Bad argument type in method (all arguments must be Enum<?>): "+method.toGenericString());
						}
						
						key += '-'+args[i].toString();
					}
					
					Object result = db.prepare("SELECT value FROM GameConfig WHERE key GLOB '%s*' ORDER BY key;", new SQLiteStatementJob<Object>()
					{
						public Object job(SQLiteStatement stmnt) throws SQLiteException
						{
							Stack<Object> results = new Stack<Object>();
							while(stmnt.step())
							{
								results.add(stmnt.columnValue(0));
							}
							
							if (results.size() == 0) return null;
							
							if (!method.getReturnType().isArray())
							{
								if (results.size() > 1) throw new RuntimeException("Return type is not an array, but several results found in DB ("+method.toGenericString()+").");
								return valueOf(Class.class.cast(method.getReturnType()), results.firstElement().toString());
							}
							else
							{
								// TODO: Support multi-dimenstional array (get/set).
								// int nrDims = 1 + method.getReturnType().getName().lastIndexOf('[');
								Object arr = Array.newInstance(method.getReturnType().getComponentType(), results.size());
								for(int i=0; i<results.size(); ++i)
								{
									Array.set(arr, i, valueOf(Class.class.cast(method.getReturnType().getComponentType()), results.get(i).toString()));
								}
								return arr;
							}
						};
					}, key);
					
					
					return result;
					//return method.getReturnType().cast(result);
				}
				else
				{
					throw new SEPServer.SEPImplementationException("Invalid IGameConfig: Cannot recognize getter nor setter in method: "+method.toGenericString());
				}
			}
		}
	}
	
	private static Class<?> getWrapper(Class<?> primitive)
	{
		if (!primitive.isPrimitive()) return primitive;
		
		if (primitive == byte.class) return Byte.class;
		if (primitive == short.class) return Short.class;
		if (primitive == int.class) return Integer.class;
		if (primitive == long.class) return Long.class;
		if (primitive == float.class) return Float.class;
		if (primitive == double.class) return Double.class;
		if (primitive == boolean.class) return Boolean.class;
		if (primitive == char.class) return Character.class;
		
		return primitive;
	}
	
	private static Object valueOf(Class clazz, String s)
	{
		try
		{
			Class<?> obClazz = clazz;
			if (clazz.isPrimitive())
			{
				obClazz = getWrapper(clazz);
			}
			
			Method valueOf = obClazz.getMethod("valueOf", String.class);
			
			return obClazz.cast(valueOf.invoke(null, s));
		}
		catch(Throwable t)
		{
			// 
		}
		
		return clazz.cast(s);
	}
	
	private static final Random rnd = new Random();

	private SQLiteDB db;
	private transient IGameConfig config;

	// Game
	private String	nextCelestialBodyName	= "A";

	private String generateCelestialBodyName()
	{
		String result = nextCelestialBodyName;
		if (nextCelestialBodyName.toLowerCase().charAt(nextCelestialBodyName.length() - 1) == 'z')
		{
			nextCelestialBodyName += "a";
		}
		else
		{
			nextCelestialBodyName = nextCelestialBodyName.substring(0, nextCelestialBodyName.length() - 1) + (char) (nextCelestialBodyName.charAt(nextCelestialBodyName.length() - 1) + 1);
		}
		return result;
	}
	
	// Game DataBase
	
	public SEPSQLiteDB(Set<org.axan.sep.common.Player> players, org.axan.sep.common.GameConfig config) throws IOException, SEPServerDataBaseException, GameConfigCopierException
	{	
		//dbFile = File.createTempFile("SEP-", ".sep");		
		File dbFile = new File("/tmp/SEP-test.sep");
		if (dbFile.exists())
			dbFile.delete();
		
		try
		{	
			this.db = new SQLiteDB(dbFile);
			
			// Create Tables
			/*
			String resourcesBasePath = "resources/" + SEPSQLiteDB.class.getPackage().getName().replace('.', File.separatorChar) + File.separatorChar;
			final URL sqlURL = ClassLoader.getSystemResource(resourcesBasePath + sqlResourceFile);
			if (sqlURL == null)
			{
				throw new SQLiteDBException("Import resource '" + sqlResourceFile + "' not found");
			}
			*/
			URL sqlURL = Reflect.getResource(SEPSQLiteDB.class.getPackage().getName(), "SEPSQLiteDB.server.sql");
			if (sqlURL == null)
			{
				throw new SEPServerDataBaseException("Import resource '" + sqlURL + "' not found");
			}
			
			db.importSQLFile(new File(sqlURL.getFile()));
	
			// Write GameConfig
			this.config = (IGameConfig) Proxy.newProxyInstance(IGameConfig.class.getClassLoader(), new Class<?>[] {IGameConfig.class}, new SQLiteDBGameConfigInvocationHandler(this));
			
			GameConfigCopier.copy(IGameConfig.class, config, this.config);
	
			// Make the sun
			Location sunLocation = new Location(config.getDimX() / 2, config.getDimY() / 2, config.getDimZ() / 2);
	
			for(int x = -Math.min(config.getSunRadius(), sunLocation.x); x <= Math.min(config.getSunRadius(), sunLocation.x); ++x)
				for(int y = -Math.min(config.getSunRadius(), sunLocation.y); y <= Math.min(config.getSunRadius(), sunLocation.y); ++y)
					for(int z = -Math.min(config.getSunRadius(), sunLocation.z); z <= Math.min(config.getSunRadius(), sunLocation.z); ++z)
					{
						Location parsedLoc = new Location(sunLocation.x + x, sunLocation.y + y, sunLocation.z + z);
						if (SEPUtils.getDistance(parsedLoc, sunLocation) <= config.getSunRadius())
						{
							insertArea(parsedLoc, true);
						}
					}
	
			// Add the players starting planets.
			Set<Location> playersPlanetLocations = new HashSet<Location>();
			
			for(org.axan.sep.common.Player player : players)
			{
				insertPlayer(player);								
				
				// Found a location to pop the planet.
				Location planetLocation;
				boolean locationOk;
				do
				{
					locationOk = false;
					planetLocation = new Location(rnd.nextInt(config.getDimX()), rnd.nextInt(config.getDimY()), rnd.nextInt(config.getDimZ()));
	
					if (areaExists(planetLocation)) continue;
	
					locationOk = true;
					for(Location l : playersPlanetLocations)
					{
						if (isTravellingTheSun(planetLocation.asRealLocation(), l.asRealLocation()))
						{
							locationOk = false;
							break;
						}										
	
						if (!locationOk) break;
					}
				} while(!locationOk);
	
				insertArea(planetLocation, false);
				insertStartingPlanet(generateCelestialBodyName(), planetLocation, player.getName());
				playersPlanetLocations.add(planetLocation);									
			}
	
			// Add neutral celestial bodies
			for(int i = 0; i < config.getNeutralCelestialBodiesCount(); ++i)
			{
				// Found a location to pop the celestial body
				Location celestialBodyLocation;
				do
				{
					celestialBodyLocation = new Location(rnd.nextInt(config.getDimX()), rnd.nextInt(config.getDimY()), rnd.nextInt(config.getDimZ()));
				} while(areaExists(celestialBodyLocation) && areaHasCelestialBody(celestialBodyLocation));
	
				eCelestialBodyType celestialBodyType = Basic.getKeyFromRandomTable(config.getNeutralCelestialBodiesGenerationTable());
	
				String nextName = generateCelestialBodyName();
				
				if (!areaExists(celestialBodyLocation)) insertArea(celestialBodyLocation, false);
				insertCelestialBody(celestialBodyType, nextName, celestialBodyLocation);
			}
		}
		catch(SQLiteException e)
		{
			throw new SEPServerDataBaseException(e);
		}
	}
	
	public IGameConfig getConfig()
	{
		return config;
	}
	
	@Override
	public PlayerGameBoard getPlayerGameBoard(String playerLogin)
	{
		// TODO: Create Client SQLiteDB for given player.
		return null;
		
		/*
		org.axan.sep.common.Area[][][] playerUniverseView = new org.axan.sep.common.Area[config.getDimX()][config.getDimY()][config.getDimZ()];

		Set<Probe> playerProbes = db.getUnits(Probe.class, playerLogin);

		Map<String, org.axan.sep.common.Diplomacy> playersPoliciesView = new Hashtable<String, org.axan.sep.common.Diplomacy>();
		
		boolean isVisible = false;

		for(int x = 0; x < config.getDimX(); ++x)
			for(int y = 0; y < config.getDimY(); ++y)
				for(int z = 0; z < config.getDimZ(); ++z)
				{
					Location location = new Location(x, y, z);
					Area area = db.getArea(location);

					// Check for Area visibility (default to false)
					isVisible = false;

					//NOTE: location -> productiveCelestialBody
					
					ICelestialBody celestialBody = (area != null ? area.getCelestialBody() : null);
					ProductiveCelestialBody productiveCelestialBody = (celestialBody != null && ProductiveCelestialBody.class.isInstance(celestialBody) ? ProductiveCelestialBody.class.cast(celestialBody) : null);
					String celestialBodyOwnerName = (celestialBody != null ? celestialBody.getOwnerName() : null);
					Fleet unassignedFleet = (productiveCelestialBody != null ? productiveCelestialBody.getUnasignedFleet(playerLogin) : null);

					// Visible if area celestial body is owned by the player.
					if (!isVisible && playerLogin.equals(celestialBodyOwnerName))
					{
						isVisible = true;
					}

					// Visible if area contains a celestial body and player has a unit on it.
					if (!isVisible && ((unassignedFleet != null && !unassignedFleet.hasNoMoreStarships()) || (productiveCelestialBody != null && !db.getUnits(location, playerLogin).isEmpty())))
					{
						isVisible = true;
					}

					// Area is under a player probe scope.
					if (!isVisible) for(Probe p : playerProbes)
					{
						if (org.axan.sep.common.SEPUtils.getDistance(location.asRealLocation(), p.getRealLocation()) > config.getProbeScope()) continue;

						if (p.isDeployed())
						{
							isVisible = true;
							break;
						}

						if (isVisible) break;
					}

					if (isVisible || area != null)
					{
						// If celestial body is a planet with government settled.
						Planet planet = (productiveCelestialBody == null ? null : Planet.class.isInstance(productiveCelestialBody) ? Planet.class.cast(productiveCelestialBody) : null);
						if (planet != null && planet.isGovernmentSettled())
						{
							playersPoliciesView.put(planet.getOwnerName(), db.getPlayerPolicies(planet.getOwnerName()).getPlayerView(db.getDate(), playerLogin, isVisible));							
						}
						
						// If governmental fleets are located in this area
						for(Fleet fleet : db.getUnits(location, Fleet.class))
						{
							if (fleet.isGovernmentFleet())
							{
								playersPoliciesView.put(fleet.getOwnerName(), db.getPlayerPolicies(fleet.getOwnerName()).getPlayerView(db.getDate(), playerLogin, isVisible));
							}
						}												
					}
					
					playerUniverseView[x][y][z] = db.getCreateArea(location).getPlayerView(db.getDate(), playerLogin, isVisible);
				}
		
		for(String playerName : db.getPlayersKeySet())
		{
			if (playersPoliciesView.containsKey(playerName)) continue;
			playersPoliciesView.put(playerName, db.getPlayerPolicies(playerName).getPlayerView(db.getDate(), playerLogin, false));
		}
		
		return new org.axan.sep.common.PlayerGameBoard(config, playerLogin, playerUniverseView, db.getSunLocation(), db.getDate(), playersPoliciesView, db.getPlayerLogs(playerLogin));
		*/
	}
	
	private static final SortedSet<ATurnResolvingEvent> resolvingEvents = new TreeSet<ATurnResolvingEvent>(); 
	
	@Override
	public SortedSet<ATurnResolvingEvent> getResolvingEvents()
	{
		synchronized(resolvingEvents)
		{
			if (resolvingEvents.isEmpty())
			{
				// TODO: Implement resolving events.
				resolvingEvents.add(new ATurnResolvingEvent(0, "OnTimeTick")
				{					
					@Override
					public void run(SortedSet<ATurnResolvingEvent> eventsQueue, ISEPServerDataBase sepDB) throws SEPServerDataBaseException
					{
						/* TODO:
						OnTimeTick			Le temps s'écoule.
							Déplacer les unités mobiles, écrire le journal de bord (rencontres, vortex, ...) (calculer à l'avance les rencontres avec une vrai distance au lieu de faire step/step et/ou zone/zone).
						*/
						try
						{
							SQLiteDB db = ((SEPSQLiteDB) sepDB).getDB();							
							
							db.exec(new SQLiteJob<Void>()
							{
								@Override
								protected Void job(SQLiteConnection conn) throws Throwable
								{
									SQLiteStatement stmnt = conn.prepare(String.format("SELECT U.type, * FROM Unit U LEFT JOIN	VersionedUnit VU USING (name, owner, type) LEFT JOIN	PulsarMissile PM USING (name, owner, type) LEFT JOIN	Probe P USING (name, owner, type) LEFT JOIN	AntiProbeMissile APM USING (name, owner, type) LEFT JOIN	CarbonCarrier CC USING (name, owner, type) LEFT JOIN	SpaceRoadDeliverer SRD USING (name, owner, type) LEFT JOIN	Fleet F USING (name, owner, type) LEFT JOIN	VersionedPulsarMissile VPM USING (name, owner, turn) LEFT JOIN	VersionedProbe VP USING (name, owner, turn) LEFT JOIN	VersionedAntiProbeMissile VAPM USING (name, owner, turn) LEFT JOIN	VersionedCarbonCarrier VCC USING (name, owner, turn) LEFT JOIN	VersionedFleet VF USING (name, owner, turn) WHERE VU.turn = %d AND VU.destination_x IS NOT NULL;", getConfig().getTurn()));
									Vector<VersionedUnit> movingUnits = new Vector<VersionedUnit>();
									while(stmnt.step())
									{
										Class<? extends org.axan.sep.server.model.orm.IVersionedUnit> vuClazz = SEPSQLiteDB.getVersionedUnitClass(stmnt.columnString(0));
										movingUnits.add(new VersionedUnit(SQLiteORMGenerator.mapTo(vuClazz, stmnt), getConfig()));
									}
									// SUIS LA
									double step = 0;
									VersionedUnit fasterUnit = null;
									double minDistance = Double.POSITIVE_INFINITY;
									VersionedUnit u, v;
									while(step < 1)
									{
										for(int i=0; i < movingUnits.size(); ++i)
										{
											u = movingUnits.elementAt(i);
											if (fasterUnit == null || u.getSpeed() > fasterUnit.getSpeed())
											{
												fasterUnit = u;
											}
											
											double uStep = u.getProgress() + u.getSpeed()*step;
											RealLocation uLocation = SEPUtils.getMobileLocation(u.getDeparture(), u.getDestination(), uStep, true);
											
											for(int j=i+1; j < movingUnits.size(); ++j)
											{
												v = movingUnits.elementAt(j);
												
												double vStep = v.getProgress() + v.getSpeed()*step;
												RealLocation vLocation = SEPUtils.getMobileLocation(v.getDeparture(), v.getDestination(), vStep, true);
												
												double distanceUV = SEPUtils.getDistance(uLocation, vLocation);
												boolean spotted = false;
												
												if (u.getSight() >= distanceUV)
												{
													// TODO: u.loggerRencontre(v, step);
													spotted = true;
												}
												if (v.getSight() >= distanceUV)
												{
													// TODO: v.loggerRencontre(u, step);
													spotted = true;
												}
												
												if (!spotted && minDistance < distanceUV) minDistance = distanceUV;
											}
										}
										
										step += minDistance / fasterUnit.getSpeed();
									}							
									/*
									distance = f(unit1, unit2, t) // avec t écoulement du temps pour 1 tour.
									
									
									Déplacer les unités mobiles
									Ecrire leur journal de bord (rencontres, vortex, ...)
									
									---
									
									movingUnits <- getMovingUnits(turn);									
									step <- 0
									TANTQUE(step < 1)		
										// A chaque step garder trace de la collision survenant le plus tôt, et faire le step suivant directement à cette date (si elle survient dans le tour courant)
										fasterUnit <- null
										minDistance <- INFINI
										
										POUR CHAQUE movingUnits u FAIRE
											SI u.speed > fasterUnit.speed ALORS fasterUnit <- u;
											POUR CHAQUE movingUnits v FAIRE
												SI u = v ALORS continue;
												d <- Distance(u, v, step);
												
												SI u.sight >= d ALORS u.loggerRencontre(v, step);
												SI v.sight >= d ALORS v.loggerRencontre(u, step);
												
												SI (d > u.sight OU d > v.sight) ALORS minDistance <- Min(minDistance, d);												
											FPOUR
										FPOUR
										
										// Le plus petit incrément susceptible d'etre utile (mais peut etre pas vu que minDistance et fasterUnit ne sont pas vraiment liés).
										step += minDistance / fasterUnit.speed;
										 
									FPOUR
									
									
									*/
									return null;
								}
							});
						}
						catch(SQLiteException e)
						{
							throw new SEPServerDataBaseException(e);
						}
					}
				});
				
				/*
				
	 *
	 * OnUnitArrival		Une unité spéciale arrive à destination.
	 * 	Les missiles pulsar engendrent un pulsar,
	 * 	les probes se déploient,
	 * 	les missiles anti-probes explosent en détruisant éventuellement une probe,
	 * 	les flottes déclenchent un conflit, se posent, repartent, et peuvent communiquer leur journal de bord.
	 * 	les spaceRoadDeliverer spawnent une spaceRoad, et peuvent communiquer leur journal de bord.
	 * 	les carbonCarrier spawn du carbone, éventuellement repartent, et peuvent communiquer leur journal de bord.
	 * 
	 * OnConflict			Un conflit est déclaré sur un cors céleste.
	 * 	On résoud le conflit concerné, en mettant à jour les journals de bords des flottes concernées (+ log du corps céleste champs de bataille communiqué en direct au joueur).
	 * 
	 * OnTimeTickEnd		Le temps à fini de s'écouler.
	 * 	On génère le carbone et la population pour le tour écoulé, on incrémente la date.
				 */
			}
		}
		
		return resolvingEvents;
	}
	
	// Private
	
	void insertCelestialBody(eCelestialBodyType celestialBodyType, String name, Location location) throws SQLiteException
	{
		boolean productiveCelestialBody = (celestialBodyType != eCelestialBodyType.Vortex);
		db.exec("INSERT INTO CelestialBody (name, location_x, location_y, location_z, type) VALUES ('%s', %d, %d, %d, '%s');", name, location.x, location.y, location.z, celestialBodyType);
		
		if (productiveCelestialBody)
		{
			// Fix carbon amount to the mean value.
			int[] carbonAmount = config.getCelestialBodiesStartingCarbonAmount(celestialBodyType);
			int initialCarbon = rnd.nextInt(carbonAmount[1] - carbonAmount[0]) + carbonAmount[0];
			
			// Fix slots amount to the mean value.
			int[] slotsAmount = config.getCelestialBodiesSlotsAmount(celestialBodyType);
			int maxSlots = rnd.nextInt(slotsAmount[1] - slotsAmount[0]) + slotsAmount[0];
			if (maxSlots <= 0) maxSlots = 1;			
			
			db.exec("INSERT INTO ProductiveCelestialBody (name, initialCarbonStock, maxSlots, type) VALUES ('%s', %d, %d, '%s')", name, initialCarbon, maxSlots, celestialBodyType);
		}
		
		switch(celestialBodyType)
		{
			case Vortex:
			{
				// TODO:
				throw new SEPServer.SEPImplementationException("insertCelestialBody(Vortex, ...) not Implemented");
			}
			case AsteroidField:
			case Nebula:
			{
				db.exec("INSERT INTO %s (name, type) VALUES ('%s', '%s');", celestialBodyType, name, celestialBodyType);
				break;
			}
			
			case Planet:
			{
				int[] populationPerTurnRange = config.getPopulationPerTurn();
				int populationPerTurn = rnd.nextInt(populationPerTurnRange[1] - populationPerTurnRange[0]) + populationPerTurnRange[0];
				
				int[] populationLimitRange = config.getPopulationLimit();
				int maxPopulation = rnd.nextInt(populationLimitRange[1] - populationLimitRange[0]) + populationLimitRange[0];
				
				db.exec("INSERT INTO Planet (name, populationPerTurn, maxPopulation, type) VALUES ('%s', %d, %d, '%s');", name, populationPerTurn, maxPopulation, celestialBodyType);
				break;
			}
			
			default:
			{
				throw new SEPServer.SEPImplementationException("'"+celestialBodyType+"' not implemented.");
			}
		}
	}

	void insertPlayer(org.axan.sep.common.Player player) throws SQLiteException
	{
		db.exec("INSERT INTO Player (name) VALUES ('%s');", player.getName());
		db.exec("INSERT INTO PlayerConfig (name, color, symbol, portrait) VALUES ('%s', '%s', NULL, NULL);", player.getName(), player.getConfig().getColor().getRGB());		
	}
	
	void insertArea(Location location, boolean isSun) throws SQLiteException
	{
		db.exec("INSERT INTO Area (location_x, location_y, location_z, isSun) VALUES (%d, %d, %d, %d);", location.x, location.y, location.z, isSun ? 1 : 0);
	}

	boolean areaExists(Location location) throws SQLiteException
	{
		return db.prepare("SELECT EXISTS ( SELECT location_x FROM Area WHERE location_x = %d AND location_y = %d AND location_z = %d );", new SQLiteStatementJob<Boolean>()
		{
			@Override
			public Boolean job(SQLiteStatement stmnt) throws SQLiteException
			{
				try
				{
					stmnt.step();
					return (stmnt.columnInt(0) != 0);
				}
				catch(SQLiteException e)
				{
					e.printStackTrace();
					throw e;
				}
			}
		}, location.x, location.y, location.z, location.x, location.y, location.z);
	}
	
	boolean areaHasCelestialBody(Location location) throws SQLiteException
	{
		return db.prepare("EXISTS ( SELECT name FROM CelestialBody WHERE location_x = %d AND location_y = %d AND location_z = %d", new SQLiteStatementJob<Boolean>()
		{
			@Override
			public Boolean job(SQLiteStatement stmnt) throws SQLiteException
			{
				return (stmnt.columnInt(0) != 0);
			}			
		},location.x, location.y, location.z,location.x, location.y, location.z);
	}
	
	boolean areaIsSun(Location location) throws SQLiteException
	{
		return db.prepare("SELECT isSun FROM Area WHERE location_x = %d AND location_y = %d AND location_z = %d;", new SQLiteStatementJob<Boolean>()
		{
			@Override
			public Boolean job(SQLiteStatement stmnt) throws SQLiteException
			{
				stmnt.step();
				return (stmnt.columnInt(0) != 0);
			}
		}, location.x, location.y, location.z, location.x, location.y, location.z);
	}
	
	boolean isTravellingTheSun(RealLocation a, RealLocation b) throws SQLiteException
	{
		// TODO: Optimize with a SQL request using "... IN ( ... )" as where clause.
		for(RealLocation pathStep : SEPUtils.getAllPathLoc(a, b))
		{
			if (areaExists(pathStep.asLocation()) && areaIsSun(pathStep.asLocation())) return true;
		}
		
		return false;
	}
	
	void insertStartingPlanet(String planetName, Location planetLocation, String ownerName) throws SQLiteException
	{			
		// Fix carbon amount to the mean value.
		int[] carbonAmount = config.getCelestialBodiesStartingCarbonAmount(eCelestialBodyType.Planet);
		int carbonStock = (carbonAmount[1] - carbonAmount[0]) / 2 + carbonAmount[0];

		// Fix slots amount to the mean value.
		int[] slotsAmount = config.getCelestialBodiesSlotsAmount(eCelestialBodyType.Planet);
		int slots = (slotsAmount[1] - slotsAmount[0]) / 2 + slotsAmount[0];
		if (slots <= 0) slots = 1;

		int[] populationPerTurnRange = config.getPopulationPerTurn();
		int populationPerTurn = (populationPerTurnRange[1] - populationPerTurnRange[0])/2 + populationPerTurnRange[0];
		
		int[] populationLimitRange = config.getPopulationLimit();
		int populationLimit = (populationLimitRange[1] - populationLimitRange[0])/2 + populationLimitRange[0];

		db.exec("INSERT INTO CelestialBody (name, location_x, location_y, location_z, type) VALUES ('%s', %d, %d, %d, '%s');", planetName, planetLocation.x, planetLocation.y, planetLocation.z, eCelestialBodyType.Planet);
		db.exec("INSERT INTO ProductiveCelestialBody (name, initialCarbonStock, maxSlots, type) VALUES ('%s', %d, %d, '%s');", planetName, carbonStock, slots, eCelestialBodyType.Planet);
		db.exec("INSERT INTO Planet (name, populationPerTurn, maxPopulation, type) VALUES ('%s', %d, %d, '%s');", planetName, populationPerTurn, populationLimit, eCelestialBodyType.Planet);
		db.exec("INSERT INTO VersionedProductiveCelestialBody (name, turn, carbonStock, currentCarbon, owner, type) VALUES ('%s', %d, %d, %d, '%s', '%s');", planetName, 0, carbonStock, config.getPlayersPlanetsStartingCarbonResources(), ownerName, eCelestialBodyType.Planet);
		db.exec("INSERT INTO VersionedPlanet (name, turn, currentPopulation, type) VALUES ('%s', %d, %d, '%s');", planetName, 0, config.getPlayersPlanetsStartingPopulation(), eCelestialBodyType.Planet);
		
		// If victory rule "Regimicide" is on, starting planet has a pre-built government module.	    
	    if (config.isRegimicide())
		{
	    	// Buildin, GovernmentModule, Government
	    	db.exec("INSERT INTO Building (type, nbSlots, celestialBodyName, turn) VALUES ('%s', %d, '%s', %d);", eBuildingType.GovernmentModule, 1, planetName, 0);
	    	db.exec("INSERT INTO GovernmentModule (type, celestialBodyName, turn) VALUES ('%s', '%s', %d);", eBuildingType.GovernmentModule, planetName, 0);
	    	db.exec("INSERT INTO Government (owner, turn, planetName, planetTurn) VALUES ('%s', %d, '%s', %d);", ownerName, 0, planetName, 0);
		}
	}
	
	static <T extends org.axan.sep.server.model.orm.VersionedUnit> Class<T> getVersionedUnitClass(String type) throws ClassNotFoundException
	{
		if (type.startsWith("Versioned")) return (Class<T>) Class.forName(VersionedUnit.class.getPackage().getName()+"."+type);
		else return (Class<T>) Class.forName(VersionedUnit.class.getPackage().getName()+".Versioned"+type);
	}
	
	/// Tests
	
	SQLiteDB getDB()
	{
		return db;
	}
	
	/// Serialization
	
	private void writeObject(final java.io.ObjectOutputStream out) throws IOException
	{
		out.defaultWriteObject();
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
		this.config = (IGameConfig) Proxy.newProxyInstance(IGameConfig.class.getClassLoader(), new Class<?>[] {IGameConfig.class}, new SQLiteDBGameConfigInvocationHandler(this));
	}
	
	private void readObjectNoData() throws ObjectStreamException
	{

	}
}
