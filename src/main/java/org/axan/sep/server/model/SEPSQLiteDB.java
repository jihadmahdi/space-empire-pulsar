package org.axan.sep.server.model;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.NotSerializableException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.Stack;

import org.axan.eplib.utils.Basic;
import org.axan.sep.common.GameConfig;
import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.SEPUtils;
import org.axan.sep.common.Protocol.eBuildingType;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.SEPUtils.RealLocation;
import org.axan.sep.server.SEPServer;
import org.axan.sep.server.TestSQLite;
import org.axan.sep.server.model.ProductiveCelestialBody.CelestialBodyBuildException;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteJob;
import com.almworks.sqlite4java.SQLiteQueue;
import com.almworks.sqlite4java.SQLiteStatement;

public class SEPSQLiteDB implements ISEPServerDataBase, Serializable
{

	/** Serialization version */
	private static final long serialVersionUID = 1L;
	
	private static class SQLiteDBGameConfigInvocationHandler implements InvocationHandler
	{
		private final SEPSQLiteDB db;
		
		public SQLiteDBGameConfigInvocationHandler(SEPSQLiteDB db)
		{
			this.db = db;
		}
		
		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
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
				if (method.getName().startsWith("set") && method.getReturnType().equals(Void.class) && args != null && args.length > 0)
				{
					// TODO: To test
					
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
					
					if (i+2 == args.length)
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
				else if (!Void.class.equals(method.getReturnType()))
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
					
					for(int i=0; i<args.length; ++i)
					{
						if (!Enum.class.isInstance(args[i]))
						{
							throw new SEPServer.SEPImplementationException("Invalid IGameConfig: Bad argument type in method (all arguments must be Enum<?>): "+method.toGenericString());
						}
						
						key += '-'+args[i].toString();
					}
					
					return db.prepare("SELECT value FROM GameConfig WHERE key GLOB '%s*' ORDER BY key;", new SQLiteStatementJob<Object>()
					{
						public Object job(SQLiteStatement stmnt) throws SQLiteException
						{
							Stack<Object> results = new Stack<Object>();
							while(stmnt.step())
							{
								results.add(stmnt.columnValue(0));
							}
							
							if (results.size() == 0) return null;
							else if (results.size() == 1) return results.firstElement();
							else return results.toArray();
						};
					}, key);
				}
				else
				{
					throw new SEPServer.SEPImplementationException("Invalid IGameConfig: Cannot recognize getter nor setter in method: "+method.toGenericString());
				}
			}
		}
	}
	
	private static final Random rnd = new Random();

	private final File dbFile;
	private SQLiteQueue sqliteQueue;
	private final IGameConfig config;

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
	
	public SEPSQLiteDB(Set<org.axan.sep.common.Player> players, org.axan.sep.common.GameConfig config) throws IOException, SQLiteException
	{
		this.config = (IGameConfig) Proxy.newProxyInstance(IGameConfig.class.getClassLoader(), new Class<?>[] {IGameConfig.class}, new SQLiteDBGameConfigInvocationHandler(this));
		// TODO: Write default config to DB
		
		//dbFile = File.createTempFile("SEP-", ".sep");
		dbFile = new File("/tmp/SEP-test.sep");
		if (dbFile.exists())
			dbFile.delete();

		sqliteQueue = new SQLiteQueue(dbFile);

		sqliteQueue.start();

		exec("PRAGMA foreign_keys=1;");
		if (debug("PRAGMA foreign_keys;").compareToIgnoreCase("|foreign_keys|\n|------------|\n|1           |") != 0)
		{
			throw new SQLiteException(-1, "Foreign keys setting error");
		}

		// Create Tables
		importResourceFile("SEPSQLiteDB.server.sql");

		// Write GameConfig
		/*
		 * TODO: Write config into DB. Reflect getters names ? ex:
		 * exec("INSERT INTO GameConfig VALUES ('carbonMinimalFreight','"+config.getCarbonMinimalFreight()+"');");
		 */

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
			
			insertCelestialBody(celestialBodyType, nextName, celestialBodyLocation);
		}
	}
	
	private void insertCelestialBody(eCelestialBodyType celestialBodyType, String name, Location location) throws SQLiteException
	{
		boolean productiveCelestialBody = (celestialBodyType != eCelestialBodyType.Vortex);
		exec("INSERT INTO CelestialBody (name, location_x, location_y, location_z, Vortex, ProductiveCelestialBody) VALUES (%s, %d, %d, %d, %d, %d);", name, location.x, location.y, location.z, (celestialBodyType == eCelestialBodyType.Vortex)?1:0, (productiveCelestialBody)?1:0);
		
		if (productiveCelestialBody)
		{
			// Fix carbon amount to the mean value.
			int[] carbonAmount = config.getCelestialBodiesStartingCarbonAmount(celestialBodyType);
			int initialCarbon = rnd.nextInt(carbonAmount[1] - carbonAmount[0]) + carbonAmount[0];
			
			// Fix slots amount to the mean value.
			int[] slotsAmount = config.getCelestialBodiesSlotsAmount(celestialBodyType);
			int maxSlots = rnd.nextInt(slotsAmount[1] - slotsAmount[0]) + slotsAmount[0];
			if (maxSlots <= 0) maxSlots = 1;			
			
			exec("INSERT INTO ProductiveCelestialBody (name, initialCarbon, maxSlots, %s) VALUES (%s, %d, %d, %d, %d, %d)", celestialBodyType, name, initialCarbon, maxSlots, 1);
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
				exec("INSERT INTO %s (name) VALUES (%s);", celestialBodyType, name);
				break;
			}
			
			case Planet:
			{
				int[] populationPerTurnRange = config.getPopulationPerTurn();
				int populationPerTurn = rnd.nextInt(populationPerTurnRange[1] - populationPerTurnRange[0]) + populationPerTurnRange[0];
				
				int[] populationLimitRange = config.getPopulationLimit();
				int maxPopulation = rnd.nextInt(populationLimitRange[1] - populationLimitRange[0]) + populationLimitRange[0];
				
				exec("INSERT INTO Planet (name, populationPerTurn, maxPopulation) VALUES (%s, %d, %d);", name, populationPerTurn, maxPopulation);
				break;
			}
			
			default:
			{
				throw new SEPServer.SEPImplementationException("'"+celestialBodyType+"' not implemented.");
			}
		}
	}

	private void insertPlayer(org.axan.sep.common.Player player) throws SQLiteException
	{
		exec("INSERT INTO Player (name) VALUES (%s);", player.getName());
		exec("INSERT INTO PlayerConfig (name, color, symbol, portrait) VALUES (%s, %s, NULL, NULL);", player.getName(), player.getConfig().getColor().getRGB());		
	}
	
	private void insertArea(Location location, boolean isSun) throws SQLiteException
	{
		exec("INSERT INTO Area (location_x, location_y, location_z, isSun) VALUES (%d, %d, %d, %d);", location.x, location.y, location.z, isSun ? 1 : 0);
	}

	private boolean areaExists(final Location location) throws SQLiteException
	{
		return exec(new SQLiteJob<Boolean>()
		{
			@Override
			protected Boolean job(SQLiteConnection connection) throws Throwable
			{
				SQLiteStatement stmnt = connection.prepare(String.format("EXISTS ( SELECT location_x FROM Area WHERE location_x = %d AND location_y = %d AND location_z = %d );", location.x, location.y, location.z));
				stmnt.step();
				return stmnt.columnInt(0) == 1;
			}
		});
	}
	
	private boolean areaHasCelestialBody(Location location) throws SQLiteException
	{
		return prepare("EXISTS ( SELECT name FROM CelestialBody WHERE location_x = %d AND location_y = %d AND location_z = %d", new SQLiteStatementJob<Boolean>()
		{
			@Override
			public Boolean job(SQLiteStatement stmnt) throws SQLiteException
			{
				return stmnt.columnInt(0) != 0;
			}			
		},location.x, location.y, location.z,location.x, location.y, location.z);
	}
	
	private boolean areaIsSun(Location location) throws SQLiteException
	{
		return prepare("SELECT isSun FROM Area WHERE location_x = %d AND location_y = %d AND location_z = %d;", new SQLiteStatementJob<Boolean>()
		{
			@Override
			public Boolean job(SQLiteStatement stmnt) throws SQLiteException
			{
				return (stmnt.columnInt(0) != 0);
			}
		}, location.x, location.y, location.z, location.x, location.y, location.z);
	}
	
	private boolean isTravellingTheSun(RealLocation a, RealLocation b) throws SQLiteException
	{
		// TODO: Optimize with a SQL request using "... IN ( ... )" as where clause.
		for(RealLocation pathStep : SEPUtils.getAllPathLoc(a, b))
		{
			if (areaExists(pathStep.asLocation()) && areaIsSun(pathStep.asLocation())) return true;
		}
		
		return false;
	}
	
	private void insertStartingPlanet(String planetName, Location planetLocation, String ownerName) throws SQLiteException
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

		exec("INSERT INTO CelestialBody (name, location_x, location_y, location_z, Vortex, ProductiveCelestialBody) VALUES (%s, %d, %d, %d, %d, %d);", planetName, planetLocation.x, planetLocation.y, planetLocation.z, 0, 1);
	    exec("INSERT INTO ProductiveCelestialBody (name, initialCarbonStock, maxSlots, Planet, AsteroidField, Nebula) VALUES (%s, %d, %d, %d, %d, %d);", planetName, carbonStock, slots, 1, 0, 0);
	    exec("INSERT INTO Planet (name, populationPerTurn, maxPopulation) VALUES (%s, %d, %d);", planetName, populationPerTurn, populationLimit);
	    exec("INSERT INTO VersionedProductiveCelestialBody (name, turn, carbonStock, currentCarbon, owner, VersionedPlanet, VersionedAsteroidField, VersionedNebula) VALUES (%s, %d, %d, %d, %s, %d, %d, %d);", planetName, 0, carbonStock, config.getPlayersPlanetsStartingCarbonResources(), ownerName, 1, 0, 0);
	    exec("INSERT INTO VersionedPlanet (name, turn, currentPopulation) VALUES (%s, %d, %d);", planetName, 0, config.getPlayersPlanetsStartingPopulation());
		
		// If victory rule "Regimicide" is on, starting planet has a pre-built government module.	    
	    if (config.isRegimicide())
		{
	    	// Buildin, GovernmentModule, Government
	    	exec("INSERT INTO Building (type, nbSlots, celestialBodyName, turn) VALUES (%s, %d, %s, %d);", eBuildingType.GovernmentModule, 1, planetName, 0);
	    	exec("INSERT INTO GovernmentModule (type, celestialBodyName, turn) VALUES (%s, %s, %d);", eBuildingType.GovernmentModule, planetName, 0);
	    	exec("INSERT INTO Government (owner, turn, planetName, planetTurn) VALUES (%s, %d, %s, %d);", ownerName, 0, planetName, 0);
		}
	}
	
	////////// DB primitives
	
	private void importResourceFile(String sqlResourceFile) throws SQLiteException
	{
		String resourcesBasePath = "resources/" + SEPSQLiteDB.class.getPackage().getName().replace('.', File.separatorChar) + File.separatorChar;
		final URL sqlURL = ClassLoader.getSystemResource(resourcesBasePath + sqlResourceFile);
		if (sqlURL == null)
			throw new SQLiteException(-1, "Import resource '" + sqlResourceFile + "' not found");

		sqliteQueue.execute(new SQLiteJob<Void>()
		{

			public URL url = sqlURL;

			@Override
			protected Void job(SQLiteConnection conn) throws Throwable
			{
				boolean cancelled = false;
				try
				{
					conn.exec("BEGIN TRANSACTION;");

					InputStreamReader isr = new InputStreamReader(url.openStream());
					StringBuffer sb = new StringBuffer("");
					char lastChar = '\0';
					StringBuffer lastWord = new StringBuffer(10);
					int inBegin = 0;
					boolean inComment = false;
					boolean isBlank = true;

					while (isr.ready())
					{
						lastChar = (char) isr.read();

						sb.append(lastChar);
						lastWord.append(lastChar);

						if (lastWord.toString().endsWith("--"))
						{
							inComment = true;
						}

						if (inComment)
						{
							if (lastChar == '\n')
							{
								inComment = false;
								isBlank = true;
							}

							continue;
						}

						if (isBlank && !Character.isWhitespace(lastChar))
						{
							isBlank = false;
						}

						if (lastWord.toString().endsWith("BEGIN"))
						{
							++inBegin;
							lastWord.setLength(0);
						}

						if (lastWord.toString().endsWith("END"))
						{
							--inBegin;
							lastWord.setLength(0);
						}

						if (inBegin <= 0 && lastChar == ';')
						{
							conn.exec(sb.toString());

							sb.setLength(0);
							lastWord.setLength(0);
							isBlank = true;
						}
					}

					if (!isBlank && !inComment && sb.length() != 0)
						throw new SQLiteException(-1, "Remaining not executed lines in file '" + url + "'");
				}
				catch(Throwable t)
				{
					conn.exec("ROLLBACK;");
					cancelled = true;
				}

				if (!cancelled)
				{
					conn.exec("COMMIT;");
				}

				return null;
			}
		}).complete();
	}

	private String debug(final String sql) throws SQLiteException
	{
		SQLiteJob<String> j;
		String result = sqliteQueue.execute(j = new SQLiteJob<String>()
		{
			@Override
			protected String job(SQLiteConnection connection) throws Throwable
			{
				return connection.debug(sql);
			}
		}).complete();

		Throwable t = j.getError();
		if (t != null)
		{
			if (SQLiteException.class.isInstance(t))
			{
				throw SQLiteException.class.cast(t);
			}
			else
			{
				throw new SQLiteException(-1, "SQLiteJob error", t);
			}
		}

		return result;
	}
	
	private static interface SQLiteStatementJob<T>
	{
		<T> T job(SQLiteStatement stmnt) throws SQLiteException;
	}

	private <T> T prepare(final String sql, final SQLiteStatementJob<T> job, final Object ... params) throws SQLiteException
	{
		SQLiteJob<T> sqliteJob = new SQLiteJob<T>()
		{
			@Override
			protected T job(SQLiteConnection connection) throws Throwable
			{
				SQLiteStatement stmnt = connection.prepare(String.format(sql, params));
				return job.job(stmnt);
			}
		};
		
		T result = sqliteQueue.execute(sqliteJob).complete();
		
		Throwable t = sqliteJob.getError();
		if (t != null)
		{
			if (SQLiteException.class.isInstance(t))
			{
				throw SQLiteException.class.cast(t);
			}
			else
			{
				throw new SQLiteException(-1, "SQLiteJob error");
			}
		}

		return result;
	}
	
	private void exec(String sql, Object ... params) throws SQLiteException
	{
		exec(String.format(sql, params));
	}

	private <T> T exec(SQLiteJob<T> job) throws SQLiteException
	{
		T result = sqliteQueue.execute(job).complete();

		Throwable t = job.getError();
		if (t != null)
		{
			if (SQLiteException.class.isInstance(t))
			{
				throw SQLiteException.class.cast(t);
			}
			else
			{
				throw new SQLiteException(-1, "SQLiteJob error");
			}
		}

		return result;
	}

	private void exec(final String sql) throws SQLiteException
	{
		SQLiteJob<Void> j;
		sqliteQueue.execute(j = new SQLiteJob<Void>()
		{
			@Override
			protected Void job(SQLiteConnection connection) throws SQLiteException
			{
				connection.exec(sql);
				return null;
			}
		}).complete();

		Throwable t = j.getError();
		if (t != null)
		{
			if (SQLiteException.class.isInstance(t))
			{
				throw SQLiteException.class.cast(t);
			}
			else
			{
				throw new SQLiteException(-1, "SQLiteJob error");
			}
		}
	}

	private void writeObject(java.io.ObjectOutputStream out) throws IOException
	{
		// TODO: Not implemented yet, copy DB file
		throw new NotSerializableException();
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		// TODO: Not implemented yet, copy DB file
		throw new NotSerializableException();
	}
}
