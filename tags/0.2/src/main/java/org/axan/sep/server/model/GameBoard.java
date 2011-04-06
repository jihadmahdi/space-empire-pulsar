package org.axan.sep.server.model;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.axan.eplib.utils.Basic;
import org.axan.sep.common.ALogEntry;
import org.axan.sep.common.CarbonOrder;
import org.axan.sep.common.CommandCheckResult;
import org.axan.sep.common.ConflictLogEntry;
import org.axan.sep.common.GameConfig;
import org.axan.sep.common.SEPUtils;
import org.axan.sep.common.StarshipTemplate;
import org.axan.sep.common.UnitSeenLogEntry;
import org.axan.sep.common.eStarshipSpecializationClass;
import org.axan.sep.common.Diplomacy.PlayerPolicies;
import org.axan.sep.common.Diplomacy.PlayerPolicies.eForeignPolicy;
import org.axan.sep.common.IGame.AttackEnemiesFleetParams;
import org.axan.sep.common.IGame.BuildParams;
import org.axan.sep.common.IGame.BuildSpaceRoadParams;
import org.axan.sep.common.IGame.ChangeDiplomacyParams;
import org.axan.sep.common.IGame.DemolishParams;
import org.axan.sep.common.IGame.DemolishSpaceRoadParams;
import org.axan.sep.common.IGame.DismantleFleetParams;
import org.axan.sep.common.IGame.EmbarkGovernmentParams;
import org.axan.sep.common.IGame.FireAntiProbeMissileParams;
import org.axan.sep.common.IGame.FormFleetParams;
import org.axan.sep.common.IGame.LaunchProbeParams;
import org.axan.sep.common.IGame.MakeAntiProbeMissilesParams;
import org.axan.sep.common.IGame.MakeProbesParams;
import org.axan.sep.common.IGame.MakeStarshipsParams;
import org.axan.sep.common.IGame.ModifyCarbonOrderParams;
import org.axan.sep.common.IGame.MoveFleetParams;
import org.axan.sep.common.IGame.SettleGovernmentParams;
import org.axan.sep.common.Protocol.ServerRunningGame.RunningGameCommandException;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.SEPUtils.RealLocation;
import org.axan.sep.server.SEPServer;
import org.axan.sep.server.SEPServer.SEPImplementationException;
import org.axan.sep.server.model.Fleet.SpecializedEquivalentFleet;
import org.axan.sep.server.model.ProductiveCelestialBody.CelestialBodyBuildException;
import org.axan.sep.server.model.SpaceCounter.SpaceRoad;


public class GameBoard implements Serializable
{
	private static final long			serialVersionUID	= 1L;

	private static final Random			rnd					= new Random();
	
	private final DataBase				db;

	private GameBoard(Hashtable<String, org.axan.sep.common.Player> players, org.axan.sep.common.GameConfig config, int date, Hashtable<Location, Area> areas, Hashtable<ICelestialBody.Key, ICelestialBody> celestialBodies, Hashtable<String, Hashtable<IMarker.Key, IMarker>> playersMarkers, RealLocation sunLocation, Hashtable<Unit.Key, Unit> units, Map<String, Diplomacy> playersPolicies, Map<String, SortedSet<ALogEntry>> playersLogs)
	{
		this.db = new DataBase(players, config, date, areas, celestialBodies, playersMarkers, sunLocation, units, playersPolicies, playersLogs);
	}

	/**
	 * Full new game constructor.
	 * 
	 * @param playerList
	 * @param gameConfig
	 */
	public GameBoard(Set<org.axan.sep.common.Player> players, org.axan.sep.common.GameConfig config, int date)
	{
		this(new Hashtable<String, org.axan.sep.common.Player>(),  config, date, new Hashtable<Location, Area>(players.size()*2), new Hashtable<ICelestialBody.Key, ICelestialBody>(players.size()*2), new Hashtable<String, Hashtable<IMarker.Key, IMarker>>(players.size()*2), new RealLocation(Double.valueOf(config.getDimX()) / 2.0, Double.valueOf(config.getDimY()) / 2.0, Double.valueOf(config.getDimZ()) / 2.0), new Hashtable<Unit.Key, Unit>(), new Hashtable<String, Diplomacy>(), new Hashtable<String, SortedSet<ALogEntry>>());

		// Make the sun
		RealLocation sunLocation = db.getSunLocation();
		
		for(int x = -Math.min(config.getSunRadius(), config.getDimX() / 2); x <= Math.min(config.getSunRadius(), config.getDimX() / 2); ++x)
			for(int y = -Math.min(config.getSunRadius(), config.getDimY() / 2); y <= Math.min(config.getSunRadius(), config.getDimY() / 2); ++y)
				for(int z = -Math.min(config.getSunRadius(), config.getDimZ() / 2); z <= Math.min(config.getSunRadius(), config.getDimZ() / 2); ++z)
				{
					RealLocation parsedLoc = new RealLocation(sunLocation.x + x, sunLocation.y + y, sunLocation.z + z);
					if (org.axan.sep.common.SEPUtils.getDistance(parsedLoc, sunLocation) <= config.getSunRadius())
					{
						db.getCreateArea(parsedLoc.asLocation()).setSunFlag(true);
					}
				}				
		
		// Add the players starting planets.
		Set<Location> playersPlanetLocations = new HashSet<Location>();
		
		for(org.axan.sep.common.Player player : players)
		{
			db.insertPlayer(player);								
			
			// Found a location to pop the planet.
			Location planetLocation;
			boolean locationOk;
			do
			{
				locationOk = false;
				planetLocation = new Location(rnd.nextInt(config.getDimX()), rnd.nextInt(config.getDimY()), rnd.nextInt(config.getDimZ()));

				Area a = db.getArea(planetLocation);
				if (a != null && !a.isEmpty()) continue;

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

			Planet planet = Planet.newStartingPlanet(db, generateCelestialBodyName(), planetLocation, player.getName(), config);
			db.insertCelestialBody(planet);
			playersPlanetLocations.add(planetLocation);									
		}

		// Add neutral celestial bodies
		for(int i = 0; i < config.getNeutralCelestialBodiesCount(); ++i)
		{
			// Found a location to pop the celestial body
			Location celestialBodyLocation;
			Area a;
			do
			{
				celestialBodyLocation = new Location(rnd.nextInt(config.getDimX()), rnd.nextInt(config.getDimY()), rnd.nextInt(config.getDimZ()));
				a = db.getArea(celestialBodyLocation);
			} while(a != null && !a.isEmpty());

			Class<? extends org.axan.sep.common.ICelestialBody> celestialBodyType = Basic.getKeyFromRandomTable(config.getNeutralCelestialBodiesGenerationTable());

			String nextName = generateCelestialBodyName();
			Class<? extends ICelestialBody> serverCelestialBodyType = getServerCelestialBodyClass(celestialBodyType);			
			try
			{
				Constructor<? extends ICelestialBody> ctor = serverCelestialBodyType.getConstructor(DataBase.class, String.class, Location.class, org.axan.sep.common.GameConfig.class);
				ICelestialBody celestialBody = ctor.newInstance(db, nextName, celestialBodyLocation, config);
				
				db.insertCelestialBody(celestialBody);
			}
			catch(Exception e)
			{
				throw new Error("Cannot create celestial body type " + celestialBodyType.getSimpleName() + " (not implemented server side ?)", e);
			}
		}
	}

	public static Class<? extends ICelestialBody> getServerCelestialBodyClass(Class<? extends org.axan.sep.common.ICelestialBody> clientCelestialBodyType) throws SEPImplementationException
	{
		Class<?> serverClass;
		try
		{
			serverClass = Class.forName(ICelestialBody.class.getPackage().getName() + "." + clientCelestialBodyType.getSimpleName());
		}
		catch(ClassNotFoundException e)
		{
			throw new SEPServer.SEPImplementationException("Cannot find server celestial body type for '" + clientCelestialBodyType.getSimpleName() + "'", e);
		}

		if (!ICelestialBody.class.isAssignableFrom(serverClass))
			throw new SEPServer.SEPImplementationException("Cannot find server celestial body type for '" + clientCelestialBodyType.getSimpleName() + "', '"
					+ ICelestialBody.class.getName() + "' is not assignable from '" + serverClass.getName() + "'");
		Class<? extends ICelestialBody> serverCelestialBodyType = serverClass.asSubclass(ICelestialBody.class);

		return serverCelestialBodyType;
	}
	
	/**
	 * @param playerLogin
	 */
	public org.axan.sep.common.PlayerGameBoard getPlayerGameBoard(String playerLogin)
	{		
		SEPServer.log.log(Level.INFO, "getGameBoard(" + playerLogin + ")");
		
		GameConfig config = db.getGameConfig();
		
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
	}
	
	
	
	/// UPDATE
	
	///

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

	public void resolveCurrentTurn()
	{
		/*
		 * TODO:
		 * Repenser la résolution des tours en terme d'évènements.
		 * La résolution d'un tour génère l'évènement "le temps s'écoule d'un tour".
		 * C'est évènement engendre plusieurs effets, et l'on teste pour chaque nouvel état de l'univers si de nouveaux évènements se sont produits (avec des requetes tests).
		 * Celà permet de décomposer la résolution d'un tour en résolution plus "modulaire" par évènement/traitement que l'on organise ensuite par ordre de priorité.
		 */
		
		// TODO : Résolve mobile units movement, attacks, etc... On Current instance.

		Map<Unit, Double> movingUnitsSpeeds = new HashMap<Unit, Double>();
		double maxSpeed = Double.MIN_VALUE;

		for(Unit unit : db.getUnits())
		{
			if (unit.isMoving() || unit.startMove())
			{
				SpaceRoad spaceRoad = db.getSpaceRoad(unit.getSourceLocation(), unit.getDestinationLocation());
				double unitSpeed = (spaceRoad == null ? unit.getSpeed() : Math.max(spaceRoad.getSpeed(), unit.getSpeed())); 
				movingUnitsSpeeds.put(unit, unitSpeed);
				maxSpeed = Math.max(maxSpeed, unitSpeed);
			}
		}

		// Unit moves

		Set<Unit> currentStepMovedUnits = new HashSet<Unit>();
		Set<Probe> deployedProbes = db.getDeployedProbes();
		
		double step = 1 / maxSpeed;
		for(float currentStep = 0; currentStep < 1; currentStep += step)
		{
			currentStepMovedUnits.clear();
			
			for(Entry<Unit, Double> e : movingUnitsSpeeds.entrySet())
			{
				Unit u = e.getKey();
				double speed = e.getValue();
				
				double distance = SEPUtils.getDistance(u.getSourceLocation(), u.getDestinationLocation());
				double progressInOneTurn = (distance != 0 ? (speed / distance) : 100);
				u.setTravellingProgress(Math.min(1, u.getTravellingProgress() + progressInOneTurn * step));
				RealLocation currentStepLocation = u.getRealLocation();
				
				for(Unit movedUnit : currentStepMovedUnits)
				{
					RealLocation movedUnitCurrentLocation = movedUnit.getRealLocation();
					
					if (movedUnit.getOwnerName().equals(u.getOwnerName())) continue;
					if (SEPUtils.getDistance(currentStepLocation, movedUnitCurrentLocation) <= 1)
					{
						UnitSeenLogEntry log = new UnitSeenLogEntry(db.getDate(), currentStep, movedUnitCurrentLocation, movedUnit.getPlayerView(db.getDate(), u.getOwnerName(), true));
						u.addTravellingLogEntry(log);
						log = new UnitSeenLogEntry(db.getDate(), currentStep, currentStepLocation, u.getPlayerView(db.getDate(), movedUnit.getOwnerName(), true));
						movedUnit.addTravellingLogEntry(log);
					}
				}
				
				for(Probe probe : deployedProbes)
				{
					if (probe.getOwnerName().equals(u.getOwnerName())) continue;
					RealLocation probeLocation = probe.getRealLocation();
					distance = SEPUtils.getDistance(probeLocation, currentStepLocation);

					if (distance <= db.getGameConfig().getProbeScope())
					{
						UnitSeenLogEntry log = new UnitSeenLogEntry(db.getDate(), currentStep, currentStepLocation, u.getPlayerView(db.getDate(), probe.getOwnerName(), true));
						db.writeLog(probe.getOwnerName(), log);
					}

					if (distance <= 1)
					{
						UnitSeenLogEntry log = new UnitSeenLogEntry(db.getDate(), currentStep, probeLocation, probe.getPlayerView(db.getDate(), u.getOwnerName(), true));
						u.addTravellingLogEntry(log);
					}
				}

				currentStepMovedUnits.add(u);
			}
		}
		
		Set<AntiProbeMissile> explodingAntiProbeMissiles = new HashSet<AntiProbeMissile>();
		
		Map<Unit, ProductiveCelestialBody> justFinishedToMove = new HashMap<Unit, ProductiveCelestialBody>();
		
		for(Entry<Unit, Double> e : movingUnitsSpeeds.entrySet())
		{
			Unit u = e.getKey();
			
			RealLocation endTurnLocation = u.getRealLocation();
			
			IMarker.Key key = new IMarker.Key("own unit("+u.getName()+") travelling marker", UnitMarker.class, u.getOwnerName());
			
			db.removeMarker(u.getOwnerName(), key);
			
			if (endTurnLocation.asLocation().equals(u.getDestinationLocation().asLocation()))
			{
				u.endMove();
				
				ProductiveCelestialBody productiveCelestialBody = db.getCelestialBody(endTurnLocation.asLocation(), ProductiveCelestialBody.class);
				
				if (productiveCelestialBody != null)
				{
					productiveCelestialBody.controlNewcomer(u);
				}								
				
				if (AntiProbeMissile.class.isInstance(u))
				{
					explodingAntiProbeMissiles.add(AntiProbeMissile.class.cast(u));
				}
				
				justFinishedToMove.put(u, productiveCelestialBody);
			}
			else
			{
				db.insertMarker(u.getOwnerName(), new UnitMarker(db.getDate(), key, u.getPlayerView(db.getDate(), u.getOwnerName(), true)));
			}			
		}
		
		// Explode anti-probe missiles.
		for(AntiProbeMissile apm : explodingAntiProbeMissiles)
		{
			Probe targetProbe = db.getUnit(Probe.class, apm.getTargetOwnerName(), apm.getTargetName());
			if (targetProbe != null && SEPUtils.getDistance(apm.getRealLocation(), targetProbe.getRealLocation()) <= 1)
			{
				// TODO: New event for targetProbe owner (probe destroyed)				
				db.removeUnit(targetProbe.getKey());
			}
			
			// TODO: New event for apm owner, destroy probe marker.
			db.removeUnit(apm.getKey());
		}
		
		//////////////////////////////////					

		// Carbon & Population generation
		for(ProductiveCelestialBody productiveCelestialBody : db.getCelestialBodies(ProductiveCelestialBody.class))
		{
			GovernmentModule governmentModule = productiveCelestialBody.getBuilding(GovernmentModule.class);
			
			int generatedCarbon = 0;

			if (productiveCelestialBody.getCarbonStock() > 0)
			{
				ExtractionModule extractionModule = productiveCelestialBody.getBuilding(ExtractionModule.class);
				if (extractionModule != null && extractionModule.getCarbonProductionPerTurn() > 0)
				{
					generatedCarbon = extractionModule.getCarbonProductionPerTurn();
				}
				else
				{
					generatedCarbon = db.getGameConfig().getNaturalCarbonPerTurn();							
				}
									
				if (governmentModule != null)
				{
					generatedCarbon = (int) (generatedCarbon * 1.5);
				}
				
				if (extractionModule == null || extractionModule.getCarbonProductionPerTurn() <= 0)
				{
					generatedCarbon = Math.min(Math.max(0, db.getGameConfig().getMaxNaturalCarbon() - productiveCelestialBody.getCarbon()), generatedCarbon);
				}					
			}																				
							
			productiveCelestialBody.setCarbon(productiveCelestialBody.getCarbon() + generatedCarbon);
			productiveCelestialBody.decreaseCarbonStock(generatedCarbon);
			
			if (Planet.class.isInstance(productiveCelestialBody))
			{
				Planet planet = Planet.class.cast(productiveCelestialBody);
				
				int generatedPopulation = planet.getPopulationPerTurn();
				
				if (governmentModule != null)
				{
					generatedPopulation = (int) (generatedPopulation * 1.5);
				}
				
				generatedPopulation = Math.min(planet.getPopulationLimit() - planet.getPopulation(), generatedPopulation);
				
				planet.setPopulation(planet.getPopulation() + generatedPopulation);
			}
		}
		
		Set<ProductiveCelestialBody> productiveCelestialBodies = db.getCelestialBodies(ProductiveCelestialBody.class);
		
		// Travelling Logs for pacifist landing.
		for(Unit u : justFinishedToMove.keySet())
		{
			if (justFinishedToMove.get(u) == null || justFinishedToMove.get(u).getConflictInitiators().isEmpty())
			{
				for(ALogEntry log : u.getTravellingLogs())
				{
					db.writeLog(u.getOwnerName(), log);
				}
			}
		}
		
		// Conflicts
		for(ProductiveCelestialBody productiveCelestialBody : productiveCelestialBodies)
		{
			if (!productiveCelestialBody.getConflictInitiators().isEmpty())
			{
				resolveConflict(productiveCelestialBody, 1);
			}
		}
		
		// Carbon freight
		for(ProductiveCelestialBody productiveCelstialBody : productiveCelestialBodies)
		{
			SpaceCounter spaceCounter = productiveCelstialBody.getBuilding(SpaceCounter.class);
			if (spaceCounter == null) continue;
			
			spaceCounter.prepareCarbonDelivery(db, productiveCelstialBody);
		}

		db.incDate();
	}

	public void resolveConflict(ProductiveCelestialBody productiveCelestialBody, int round)
	{
		// Initiate conflicts table : PlayerName/PlayerName: boolean
		Map<String, Map<String, Boolean>> conflictDiplomacy = resolveConflictDiplomacy(productiveCelestialBody);
						
		// List merged (unassigned fleets + fleets) forces for each players
		// Backup and remove original fleets (including unassigned ones). 
		Map<String, Fleet> mergedFleets = new Hashtable<String, Fleet>();
		Map<String, Set<Fleet>> originalPlayersFleets = new Hashtable<String, Set<Fleet>>();
		
		Location location = productiveCelestialBody.getLocation();
		
		for(String p : db.getPlayersKeySet())
		{
			if (productiveCelestialBody.getUnasignedFleet(p) != null)
			{												
				Fleet unasignedFleet = productiveCelestialBody.getUnasignedFleet(p);
				mergedFleets.put(p, new Fleet(db, p+" forces in conflict on "+productiveCelestialBody.getName()+" at turn "+db.getDate(), p, location.asRealLocation(), unasignedFleet.getStarships(), unasignedFleet.getSpecialUnits(), false, null, null));				
				productiveCelestialBody.removeFromUnasignedFleet(p, unasignedFleet.getStarships(), unasignedFleet.getSpecialUnits());
			}
		}
		
		Set<Fleet> fleetsToRemove = new HashSet<Fleet>();
		for(Fleet f : db.getUnits(location, Fleet.class))
		{						
			if (!mergedFleets.containsKey(f.getOwnerName()))
			{
				mergedFleets.put(f.getOwnerName(), new Fleet(db, f.getOwnerName()+" forces in conflict on "+productiveCelestialBody.getName()+" at turn "+db.getDate(), f.getOwnerName(), location.asRealLocation(), f.getStarships(), f.getSpecialUnits(), false, null, null));
			}
			else
			{
				mergedFleets.get(f.getOwnerName()).merge(f.getStarships(), f.getSpecialUnits());
			}
			
			fleetsToRemove.add(f);			
		}
		
		for(Fleet f : fleetsToRemove)
		{
			if (!originalPlayersFleets.containsKey(f.getOwnerName())) originalPlayersFleets.put(f.getOwnerName(), new HashSet<Fleet>());
			originalPlayersFleets.get(f.getOwnerName()).add(f);
			db.removeUnit(f.getKey());
		}
		
		DefenseModule defenseModule = productiveCelestialBody.getBuilding(DefenseModule.class);
		if (defenseModule != null)
		{
			if (!mergedFleets.containsKey(productiveCelestialBody.getOwnerName()))
			{	
				mergedFleets.put(productiveCelestialBody.getOwnerName(), new Fleet(db, productiveCelestialBody.getOwnerName()+" forces in conflict on "+productiveCelestialBody.getName()+" at turn "+db.getDate(), productiveCelestialBody.getOwnerName(), location.asRealLocation(), null, null, false, null, null));				
			}
			
			Set<ISpecialUnit> specialUnits = new HashSet<ISpecialUnit>();
			specialUnits.add(defenseModule.getSpecialUnit());
			mergedFleets.get(productiveCelestialBody.getOwnerName()).merge(null, specialUnits);
		}
		
		// Run battle				
		Map<String, Fleet> survivalFleets = resolveBattle(conflictDiplomacy, mergedFleets);
		
		for(String survivor : survivalFleets.keySet())
		{
			Map<String, org.axan.sep.common.Fleet> startingForces = new HashMap<String, org.axan.sep.common.Fleet>();
			for(String player : mergedFleets.keySet())
			{
				startingForces.put(player, mergedFleets.get(player).getPlayerView(db.getDate(), survivor, true));
			}
						
			Map<String, org.axan.sep.common.Fleet> survivingForces = new HashMap<String, org.axan.sep.common.Fleet>();
			for(String player : survivalFleets.keySet())
			{
				survivalFleets.get(player).rest();
				survivingForces.put(player, survivalFleets.get(player).getPlayerView(db.getDate(), survivor, true));
			}
			
			ConflictLogEntry conflictLog = new ConflictLogEntry(db.getDate(), (float) .999, round, productiveCelestialBody.getName(), startingForces, conflictDiplomacy, survivingForces);			
			
			db.writeLog(survivor, conflictLog);
		}				
		
		// Restore original fleets from survived ones.
		for(Map.Entry<String, Fleet> e : survivalFleets.entrySet())
		{
			String playerName = e.getKey();
			Fleet survivalFleet = e.getValue();			
			
			Set<Fleet> originalFleets = originalPlayersFleets.get(playerName);
			Vector<Fleet> originalFleetsCopy = new Vector<Fleet>();
			Map<String, Map<StarshipTemplate, Integer>> resultantFleetsStarships = new HashMap<String, Map<StarshipTemplate,Integer>>();
			Map<String, Set<ISpecialUnit>> resultantFleetsSpecialUnits = new HashMap<String, Set<ISpecialUnit>>();						
			Map<StarshipTemplate, Integer> resultantUnasignedStarships = new HashMap<StarshipTemplate, Integer>();
			Set<ISpecialUnit> resultantUnasignedSpecialUnits = new HashSet<ISpecialUnit>();
			
			for(ISpecialUnit specialUnit : survivalFleet.getSpecialUnits())
			{
				boolean found = false;
				for(Fleet originalFleet : originalFleets)
				{					
					if (originalFleet.getSpecialUnits().contains(specialUnit))
					{
						if (!resultantFleetsSpecialUnits.containsKey(originalFleet.getName())) resultantFleetsSpecialUnits.put(originalFleet.getName(), new HashSet<ISpecialUnit>());						
						resultantFleetsSpecialUnits.get(originalFleet.getName()).add(specialUnit);
						found = true;
						break;
					}
				}
				
				if (!found)
				{
					resultantUnasignedSpecialUnits.add(specialUnit);
				}
			}
						
			for(StarshipTemplate starshipTemplate : survivalFleet.getStarships().keySet())
			{
				int totalNb = survivalFleet.getStarships().get(starshipTemplate);				
				
				while(totalNb > 0)
				{
					originalFleetsCopy.clear();
					originalFleetsCopy.addAll(originalFleets);
					
					int alreadyRecovered = 0;
					int maxToRecover = 0;
					
					Fleet originalFleet = null;
					do
					{
						if (originalFleetsCopy.isEmpty())
						{
							originalFleet = null;
							break;
						}
						
						int i = rnd.nextInt(originalFleetsCopy.size());
						originalFleet = originalFleetsCopy.remove(i);
						
						if (!resultantFleetsStarships.containsKey(originalFleet.getName())) resultantFleetsStarships.put(originalFleet.getName(), new HashMap<StarshipTemplate, Integer>());
						if (!resultantFleetsStarships.get(originalFleet.getName()).containsKey(starshipTemplate)) resultantFleetsStarships.get(originalFleet.getName()).put(starshipTemplate, 0);
						
						alreadyRecovered = resultantFleetsStarships.get(originalFleet.getName()).get(starshipTemplate);
						maxToRecover = originalFleet.getStarships().containsKey(starshipTemplate)?originalFleet.getStarships().get(starshipTemplate):0;
							
					}while(maxToRecover <= alreadyRecovered);
					
					if (originalFleet == null)
					{						
						if (!resultantUnasignedStarships.containsKey(starshipTemplate)) resultantUnasignedStarships.put(starshipTemplate, 0);						
						resultantUnasignedStarships.put(starshipTemplate, resultantUnasignedStarships.get(starshipTemplate) + totalNb);
						totalNb = 0;
					}
					else
					{															
						int maxNb = Math.min(maxToRecover - alreadyRecovered, totalNb);
						int recoveredNb = rnd.nextInt(maxNb)+1;
						totalNb -= recoveredNb;
											
						resultantFleetsStarships.get(originalFleet.getName()).put(starshipTemplate, resultantFleetsStarships.get(originalFleet.getName()).get(starshipTemplate) + recoveredNb);
					}
				}
			}
			
			for(Fleet originalFleet : originalFleets)
			{
				Fleet recoveredFleet = null;
				if (resultantFleetsStarships.get(originalFleet.getName()) != null)
				{
					recoveredFleet = new Fleet(db, originalFleet.getKey(), originalFleet.getSourceLocation(), resultantFleetsStarships.get(originalFleet.getName()), resultantFleetsSpecialUnits.get(originalFleet.getName()), false, originalFleet.getCheckpoints(), originalFleet.getTravellingLogs());
					if (!recoveredFleet.hasNoMoreStarships()) db.insertUnit(recoveredFleet);
				}
				
				if (resultantFleetsSpecialUnits.get(originalFleet.getName()) != null && (recoveredFleet == null || recoveredFleet.hasNoMoreStarships()))
				{
					resultantUnasignedSpecialUnits.addAll(resultantFleetsSpecialUnits.get(originalFleet.getName()));
					productiveCelestialBody.mergeToUnasignedFleet(playerName, null, resultantFleetsSpecialUnits.get(originalFleet.getName()));
				}
			}
			
			productiveCelestialBody.mergeToUnasignedFleet(playerName, resultantUnasignedStarships, resultantUnasignedSpecialUnits);
		}
		
		// End conflict, enventually change the celestial body owner, and check for new conflict.
		productiveCelestialBody.endConflict();
		
		if (productiveCelestialBody.getOwnerName() == null || !survivalFleets.keySet().contains(productiveCelestialBody.getOwnerName()))
		{
			productiveCelestialBody.demolishBuilding(DefenseModule.class);
			
			Random rnd = new Random();
			String newOwner = survivalFleets.keySet().toArray(new String[survivalFleets.keySet().size()])[rnd.nextInt(survivalFleets.keySet().size())];
			productiveCelestialBody.changeOwner(newOwner);
			
			productiveCelestialBody.addConflictInititor(newOwner);
			resolveConflict(productiveCelestialBody, ++round);
		}
	}
	
	public static Map<String, Fleet> resolveBattle(Map<String, Map<String, Boolean>> conflictDiplomacy, Map<String, Fleet> forces)
	{
		Map<String, Fleet> survivors = new Hashtable<String, Fleet>(forces);
		
		boolean isFinished;
		do
		{
			// Look if there's still some alive player wanting to fight with another alive one.
			isFinished = true;
			for(String p : survivors.keySet())
			{
				for(String t : survivors.keySet())
				{		
					if (conflictDiplomacy.containsKey(p) && conflictDiplomacy.get(p).containsKey(t) && conflictDiplomacy.get(p).get(t)) isFinished = false;
					
					if (!isFinished) break;
				}
				
				if (!isFinished) break;
			}
			
			// If yes, play the battle round to next victim.
			if (!isFinished)
			{				
				boolean thereIsVictims = false;
				
				do
				{										
					Iterator<Entry<String, Fleet>> it = survivors.entrySet().iterator();
					while(it.hasNext())
					{
						Entry<String, Fleet> e = it.next();
						if (e.getValue().isDestroyed())
						{
							thereIsVictims = true;
							it.remove();
						}							
					}
					
					if (thereIsVictims) continue;
					
					Map<String, Map<eStarshipSpecializationClass, Double>> attackPromises = new HashMap<String, Map<eStarshipSpecializationClass,Double>>();
					Map<String, Fleet> mergedEnnemyFleets = new HashMap<String, Fleet>();
					
					for(String attacker : survivors.keySet())
					{
						Fleet attackerFleet = survivors.get(attacker);
																		
						Fleet mergedEnnemyFleet = Fleet.computeMergedAttackers(attacker, conflictDiplomacy, survivors);
						mergedEnnemyFleets.put(attacker, mergedEnnemyFleet);
						
						for(eStarshipSpecializationClass specialization : eStarshipSpecializationClass.values())
						{
							Map<String, Boolean> ennemies = new HashMap<String, Boolean>(conflictDiplomacy.get(attacker));							
							
							SpecializedEquivalentFleet attackerSubFleet = attackerFleet.getSpecializedFleet(specialization);																						
							if (attackerSubFleet == null) continue;
							
							SpecializedEquivalentFleet targetSubFleet = mergedEnnemyFleet.getNextTarget(specialization);
							double attack = attackerFleet.getBattleSkillsModifier().getFixedAttackBonus() + (attackerSubFleet.getAttack() * (1 + (specialization.getTdT() == targetSubFleet.getSpecialization() ? attackerSubFleet.getAttackSpecializationBonus() : (specialization.getBN() == targetSubFleet.getSpecialization() ? -1 * targetSubFleet.getDefenseSpecializationBonus() : 0))));
							
							while((attack > 0) && (!ennemies.isEmpty())) // We would not lose remaining attack, because in the worst case, the current round stops with the last specialized sub fleet death. 
							{
								int i = rnd.nextInt(ennemies.size());
								String ennemy = ennemies.keySet().toArray(new String[ennemies.size()])[i];
								
								if (!ennemies.get(ennemy) || !survivors.containsKey(ennemy))
								{
									ennemies.remove(ennemy);									
									continue;
								}
								else
								{
									SpecializedEquivalentFleet ennemySubFleet = survivors.get(ennemy).getSpecializedFleet(targetSubFleet.getSpecialization());
									if (ennemySubFleet == null || ennemySubFleet.getDefense() == 0)
									{
										ennemies.remove(ennemy);
										continue;
									}
									
									double r = (ennemies.size() > 1) ? rnd.nextDouble() : 1.0;
									double hit = r * attack;
									
									if (!attackPromises.containsKey(ennemy)) attackPromises.put(ennemy, new HashMap<eStarshipSpecializationClass, Double>());
									if (!attackPromises.get(ennemy).containsKey(targetSubFleet.getSpecialization())) attackPromises.get(ennemy).put(targetSubFleet.getSpecialization(), 0.0);									
									attackPromises.get(ennemy).put(targetSubFleet.getSpecialization(), attackPromises.get(ennemy).get(targetSubFleet.getSpecialization()) + hit);
									attack -= hit;
								}
							}															
						}							
					}
					
					double bestTime = Double.POSITIVE_INFINITY;
					
					for(Map.Entry<String, Map<eStarshipSpecializationClass, Double>> e : attackPromises.entrySet())
					{
						String target = e.getKey();
						for(Map.Entry<eStarshipSpecializationClass, Double> f : e.getValue().entrySet())
						{
							eStarshipSpecializationClass specialization = f.getKey();
							double attack = f.getValue();
							
							bestTime = Math.min(bestTime, ((double) survivors.get(target).getSpecializedFleet(specialization).getDefense()) / attack);
						}
					}
					
					for(Map.Entry<String, Map<eStarshipSpecializationClass, Double>> e : attackPromises.entrySet())
					{
						String target = e.getKey();						
						
						for(Map.Entry<eStarshipSpecializationClass, Double> f : e.getValue().entrySet())
						{
							eStarshipSpecializationClass specialization = f.getKey();
							double attack = f.getValue();
							double dmg = bestTime * attack;
											
							survivors.get(target).getSpecializedFleet(specialization).takeDamage(dmg);																												
						}
					}																				
					
				}while(!thereIsVictims);
				
				/*
				DO
				
					Map[Target, eStarshipSpecializationClass, Attack] attackPromises;
					Map[Target, Fleet] mergedAttackers; 										
					
					FOREACH belligerent
						Fleet defender <- forces(belligerent)
						mergedAttackers[belligerent] <- computeMergeAttackers(belligerent, conflictDiplomacy, forces)						
						FOREACH starshipClass							
							eStarshipSpecializationClass targetClass <- defenser.getNextTargetClass(starshipClass);
							attackPromises[defender, targetClass, mergedAttackers[belligerent].Attack * (1 + starshipClass.isTdT(targetClass) ? mergedAttackers[belligerent].Weapon : starshipClass.isBN(targetClass) ? -defenser.getNextTargetArmor(starshipClass) : 0)];							
						NEXT
					NEXT
					
					double bestTime <- +INF;
					
					FOREACH attackPromise IN attackPromises
						bestTime <- Min(bestTime, attackPromise.target.Defense / attackPromise.Attack);
					NEXT
					
					FOREACH attackPromise IN attackPromises
						attackPromise.target.takeDamage(bestTime * attackPromise.Attack);
					NEXT
				
				WHILE(!attackPromise.target.isEmpty())					
				 */									
			}
		}while(!isFinished);
		
		return survivors;
	}
	
	private Map<String, Map<String, Boolean>> resolveConflictDiplomacy(ProductiveCelestialBody productiveCelestialBody)
	{
		Stack<String> initiators = productiveCelestialBody.getConflictInitiators();
		Set<String> playersKeySet = db.getPlayersKeySet();
		String celestialBodyOwnerName = productiveCelestialBody.getOwnerName();
		
		Map<String, Map<String, Boolean>> conflicts = new Hashtable<String, Map<String,Boolean>>();
		Stack<String> seenInitiators = new Stack<String>();
		
		while(!initiators.isEmpty())
		{
			boolean fought = false;
			String initiator = initiators.pop();
			if (seenInitiators.contains(initiator)) continue;
			
			for(String target : playersKeySet)
			{
				if (target.equals(initiator)) continue;
				
				boolean initiatorPolicy = false;
				
				if (initiator.equals(celestialBodyOwnerName))
				{
					initiatorPolicy = !db.getPlayerPolicies(initiator).getPolicies(target).isAllowedToLandFleetInHomeTerritory();
				}
				else
				{
					eForeignPolicy fp = db.getPlayerPolicies(initiator).getPolicies(target).getForeignPolicy();
					initiatorPolicy = (fp == eForeignPolicy.HOSTILE || fp == eForeignPolicy.HOSTILE_IF_OWNER && target.equals(celestialBodyOwnerName));
				}
				
				boolean resultPolicy = conflicts.containsKey(target) && conflicts.get(target).containsKey(initiator) ? conflicts.get(target).get(initiator) || initiatorPolicy : initiatorPolicy;
				
				if (!conflicts.containsKey(initiator)) conflicts.put(initiator, new Hashtable<String, Boolean>());
				conflicts.get(initiator).put(target, resultPolicy);
				
				if (resultPolicy)
				{
					fought = true;
					initiators.push(target);
				}			
			}
			
			if (fought)
			{
				for(String target : playersKeySet)
				{
					if (target.equals(initiator)) continue;
					
					boolean targetPolicy = false;
					
					if (target.equals(celestialBodyOwnerName))
					{
						targetPolicy = !db.getPlayerPolicies(target).getPolicies(initiator).isAllowedToLandFleetInHomeTerritory();
					}
					else
					{
						eForeignPolicy fp = db.getPlayerPolicies(target).getPolicies(initiator).getForeignPolicy();
						targetPolicy = (fp == eForeignPolicy.HOSTILE || fp == eForeignPolicy.HOSTILE_IF_OWNER && initiator.equals(celestialBodyOwnerName));
					}
					
					boolean resultPolicy = conflicts.get(initiator).get(target) || targetPolicy;
					if (!conflicts.containsKey(target)) conflicts.put(target, new Hashtable<String, Boolean>());
					conflicts.get(target).put(initiator, resultPolicy);
					conflicts.get(initiator).put(target, resultPolicy);
					
					if (resultPolicy)
					{
						initiators.push(target);
					}
				}
			}
			
			seenInitiators.push(initiator);
		}
		
		return conflicts;
	}
	
	////////////
	
	
	public void demolish(String playerLogin, String celestialBodyName, Class<? extends org.axan.sep.common.ABuilding> buildingType) throws RunningGameCommandException
	{
		DemolishCheckResult demolishCheckResult = checkDemolish(playerLogin, celestialBodyName, buildingType);
		demolishCheckResult.productiveCelestialBody.downgradeBuilding(demolishCheckResult.existingBuilding);
	}

	public CommandCheckResult canDemolish(String playerLogin, DemolishParams p)
	{
		try
		{
			checkDemolish(playerLogin, p.celestialBodyName, p.buildingType);
		}
		catch(Throwable t)
		{
			return new CommandCheckResult(t);
		}
		return new CommandCheckResult();
	}

	private static class DemolishCheckResult
	{
		final ProductiveCelestialBody	productiveCelestialBody;
		final ABuilding					existingBuilding;

		public DemolishCheckResult(ProductiveCelestialBody productiveCelestialBody, ABuilding building)
		{
			this.productiveCelestialBody = productiveCelestialBody;
			this.existingBuilding = building;
		}
	}

	public DemolishCheckResult checkDemolish(String playerName, String celestialBodyName, Class<? extends org.axan.sep.common.ABuilding> buildingType) throws RunningGameCommandException
	{
		ProductiveCelestialBody productiveCelestialBody = db.getCelestialBody(celestialBodyName, ProductiveCelestialBody.class, playerName);		
		if (productiveCelestialBody == null) throw new RunningGameCommandException("Celestial body '" + celestialBodyName + "' does not exist, is not a productive one, or is not owned by '"+playerName+"'.");
		
		ABuilding building = productiveCelestialBody.getBuildingFromClientType(buildingType);
		
		// If no building of this type exist.
		if (building == null || building.getBuildSlotsCount() == 0) throw new RunningGameCommandException("No building type '" + buildingType.getSimpleName() + "' built yet.");
		
		if (!building.canDowngrade()) throw new RunningGameCommandException("Cannot demolish building type '" + buildingType.getSimpleName() + "'");

		return new DemolishCheckResult(productiveCelestialBody, building);
	}

	public void embarkGovernment(String playerName) throws RunningGameCommandException
	{
		EmbarkGovernmentCheckResult embarkGovernmentCheckResult = checkEmbarkGovernment(playerName);
		embarkGovernmentCheckResult.planet.removeBuilding(GovernmentModule.class);
		Set<ISpecialUnit> specialUnitsToMake = new HashSet<ISpecialUnit>();
		specialUnitsToMake.add(new GovernmentStarship(playerName+" government starship"));		
		embarkGovernmentCheckResult.planet.mergeToUnasignedFleet(playerName, null, specialUnitsToMake);
		embarkGovernmentCheckResult.planet.setCarbon(embarkGovernmentCheckResult.planet.getCarbon() - embarkGovernmentCheckResult.carbonCost);
		embarkGovernmentCheckResult.planet.setPopulation(embarkGovernmentCheckResult.planet.getPopulation() - embarkGovernmentCheckResult.populationCost);
	}

	public CommandCheckResult canEmbarkGovernment(String playerLogin, EmbarkGovernmentParams p)
	{
		EmbarkGovernmentCheckResult check;
		try
		{
			check = checkEmbarkGovernment(playerLogin);
		}
		catch(Throwable t)
		{
			return new CommandCheckResult(t);
		}
		return new CommandCheckResult(check.carbonCost, check.populationCost);
	}

	private static class EmbarkGovernmentCheckResult
	{
		final Planet			planet;
		final int				carbonCost;
		final int				populationCost;

		public EmbarkGovernmentCheckResult(Planet planet, int carbonCost, int populationCost)
		{
			this.planet = planet;
			this.carbonCost = carbonCost;
			this.populationCost = populationCost;
		}
	}

	public EmbarkGovernmentCheckResult checkEmbarkGovernment(String playerLogin) throws RunningGameCommandException
	{
		Planet planet = db.locateGovernmentModule(playerLogin);

		// If player has no government module.
		if (planet == null) throw new RunningGameCommandException("Cannot locate '" + playerLogin + "' government module.");		
		
		GovernmentModule governmentModule = planet.getBuilding(GovernmentModule.class);

		if (governmentModule == null) throw new RunningGameCommandException("No government module on the planet '" + planet.getName() + "' (unexpected error)");

		StarshipPlant starshipPlant = planet.getBuilding(StarshipPlant.class);

		// If planet has no starshipPlant.
		if (starshipPlant == null) throw new RunningGameCommandException("No starship plant on planet '" + planet.getName() + "'");

		// If starship has just been build this turn.
		if (starshipPlant.getLastBuildDate() >= db.getDate()) throw new RunningGameCommandException("Starship plant is still in construction.");

		int carbonCost = db.getGameConfig().getGovernmentStarshipCarbonPrice();
		int populationCost = db.getGameConfig().getGovernmentStarshipPopulationPrice();

		if (carbonCost > planet.getCarbon()) throw new RunningGameCommandException("Not enough carbon.");
		
		if (populationCost > planet.getPopulation()) throw new RunningGameCommandException("Not enough population.");
		
		return new EmbarkGovernmentCheckResult(planet, carbonCost, populationCost);
	}

	public CommandCheckResult canFirePulsarMissile(String playerName, String celestialBodyName)
	{
		ProductiveCelestialBody productiveCelestialBody = db.getCelestialBody(celestialBodyName, ProductiveCelestialBody.class, playerName);
		if (productiveCelestialBody == null) return new CommandCheckResult(new IllegalArgumentException("Celestial body '" + celestialBodyName + "' does not exist, or is not a productive one."));
		
		ABuilding building = productiveCelestialBody.getBuildingFromClientType(org.axan.sep.common.PulsarLauchingPad.class);

		// If no building of this type exist.
		if (building == null) return new CommandCheckResult("No pulsar launcher found.");

		// Building type check		
		if (!PulsarLauchingPad.class.isInstance(building)) return new CommandCheckResult("No pulsar launcher found.");

		PulsarLauchingPad pulsarLaunchingPad = PulsarLauchingPad.class.cast(building);
		if (pulsarLaunchingPad.getUnusedCount() <= 0) return new CommandCheckResult("No available pulsar launcher found.");

		// TODO
		return new CommandCheckResult();
	}

	public void settleGovernment(String playerLogin, String planetName) throws RunningGameCommandException
	{
		SettleGovernmentCheckResult settleGovernmentCheckResult = checkSettleGovernment(playerLogin, planetName);
		settleGovernmentCheckResult.governmentalFleet.removeGovernment();
		try
		{
			settleGovernmentCheckResult.planet.updateBuilding(settleGovernmentCheckResult.governmentModule);
		}
		catch(CelestialBodyBuildException e)
		{
			throw new SEPServer.SEPImplementationException("Unexpected exception", e);
		}
	}
	
	public CommandCheckResult canSettleGovernment(String playerLogin, SettleGovernmentParams p)
	{
		try
		{
			checkSettleGovernment(playerLogin, p.planetName);
		}
		catch(Throwable t)
		{
			return new CommandCheckResult(t);
		}
		return new CommandCheckResult();
	}
	
	private static class SettleGovernmentCheckResult
	{
		final Planet	planet;
		final Fleet		governmentalFleet;
		final GovernmentModule governmentModule;

		public SettleGovernmentCheckResult(Planet planet, Fleet governmentalFleet, GovernmentModule governmentModule)
		{
			this.planet = planet;
			this.governmentalFleet = governmentalFleet;
			this.governmentModule = governmentModule;
		}
	}
	
	public SettleGovernmentCheckResult checkSettleGovernment(String playerName, String planetName) throws RunningGameCommandException
	{
		Planet planet = db.getCelestialBody(planetName, Planet.class, playerName);
		
		// Check if government fleet is on the planet.
		Fleet governmentalFleet = null;
		
		for(Fleet f : db.getUnits(planet.getLocation(), Fleet.class, playerName))
		{
			if (f.isGovernmentFleet())
			{
				governmentalFleet = f;
				break;
			}
		}
		
		if (governmentalFleet == null) throw new RunningGameCommandException("'"+playerName+"' government cannot be found on planet '"+planetName+"'");
		
		if (planet.getFreeSlotsCount() <= 0) throw new RunningGameCommandException("No free slot available on '"+planet.getName()+"'");
		
		GovernmentModule governmentModule = new GovernmentModule(db.getDate());
		return new SettleGovernmentCheckResult(planet, governmentalFleet, governmentModule);
	}

	public void build(String playerLogin, String celestialBodyName, Class<? extends org.axan.sep.common.ABuilding> buildingType) throws CelestialBodyBuildException
	{
		BuildCheckResult buildCheckResult = checkBuild(playerLogin, celestialBodyName, buildingType);
		buildCheckResult.productiveCelestialBody.updateBuilding(buildCheckResult.newBuilding);
		buildCheckResult.productiveCelestialBody.setCarbon(buildCheckResult.productiveCelestialBody.getCarbon() - buildCheckResult.carbonCost);

		if (buildCheckResult.populationCost > 0)
		{
			Planet planet = Planet.class.cast(buildCheckResult.productiveCelestialBody);
			planet.setPopulation(planet.getPopulation() - buildCheckResult.populationCost);
		}

		buildCheckResult.productiveCelestialBody.setLastBuildDate(db.getDate());
	}

	public CommandCheckResult canBuild(String playerLogin, BuildParams p)
	{
		BuildCheckResult check;
		try
		{
			check = checkBuild(playerLogin, p.celestialBodyName, p.buildingType);
		}
		catch(Throwable t)
		{
			return new CommandCheckResult(t);
		}
		return new CommandCheckResult(check.carbonCost, check.populationCost);
	}

	private static class BuildCheckResult
	{
		final ProductiveCelestialBody	productiveCelestialBody;
		final ABuilding					newBuilding;
		final int						carbonCost;
		final int						populationCost;

		public BuildCheckResult(ProductiveCelestialBody productiveCelestialBody, int carbonCost, int populationCost, ABuilding newBuilding)
		{
			this.productiveCelestialBody = productiveCelestialBody;
			this.carbonCost = carbonCost;
			this.populationCost = populationCost;
			this.newBuilding = newBuilding;
		}
	}

	private BuildCheckResult checkBuild(String playerName, String celestialBodyName, Class<? extends org.axan.sep.common.ABuilding> buildingType) throws CelestialBodyBuildException
	{
		ProductiveCelestialBody productiveCelestialBody = db.getCelestialBody(celestialBodyName, ProductiveCelestialBody.class, playerName);
		if (productiveCelestialBody == null) throw new CelestialBodyBuildException("Celestial body '" + celestialBodyName + "' does not exist, is not a productive one, or is not owned by '"+playerName+"'.");
		
		// If this productive celestial body build was already used this turn.
		if (productiveCelestialBody.getLastBuildDate() >= db.getDate()) throw new CelestialBodyBuildException("Celestial body '" + celestialBodyName + "' already in work for this turn.");

		// If there is no more free slots.
		if (productiveCelestialBody.getFreeSlotsCount() < 1) throw new CelestialBodyBuildException("No more free slots on celestial body '" + celestialBodyName + "'");

		// Price check & Celestial body type / building type check
		int carbonCost = 0;
		int populationCost = 0;

		ABuilding building = productiveCelestialBody.getBuildingFromClientType(buildingType);
		ABuilding newBuilding;

		if (building != null)
		{
			if (!building.canUpgrade())
			{
				throw new CelestialBodyBuildException(building.getClass().getSimpleName()+" cannot be upgraded.");
			}
			
			newBuilding = building.getUpgraded(db.getDate());
			carbonCost = building.getUpgradeCarbonCost();
			populationCost = building.getUpgradePopulationCost();			
		}
		else
		{
			carbonCost = org.axan.sep.common.ABuilding.getFirstCarbonCost(buildingType);
			populationCost = org.axan.sep.common.ABuilding.getFirstPopulationCost(buildingType);
			newBuilding = ABuilding.getFirstBuild(buildingType, db.getDate());
		}

		if (carbonCost > productiveCelestialBody.getCarbon()) throw new CelestialBodyBuildException("Not enough carbon.");
		if (populationCost > 0)
		{
			if (!Planet.class.isInstance(productiveCelestialBody)) throw new CelestialBodyBuildException("Only planet can afford population costs, '" + celestialBodyName + "' is not a planet.");
			;
			Planet planet = Planet.class.cast(productiveCelestialBody);
			if (populationCost > planet.getPopulation()) throw new CelestialBodyBuildException("Not enough population.");
		}

		return new BuildCheckResult(productiveCelestialBody, carbonCost, populationCost, newBuilding);
	}

	public CommandCheckResult canFireAntiProbeMissile(String playerLogin, FireAntiProbeMissileParams p)
	{
		try
		{
			checkFireAntiProbeMissile(playerLogin, p.antiProbeMissileName, p.targetOwnerName, p.targetProbeName);
		}
		catch(Throwable t)
		{
			return new CommandCheckResult(t);
		}
		return new CommandCheckResult();
	}

	public void fireAntiProbeMissile(String playerLogin, String antiProbeMissileName, String targetOwnerName, String targetProbeName) throws RunningGameCommandException
	{
		FireAntiProbeMissileCheckResult fireAntiProbeMissileCheckResult = checkFireAntiProbeMissile(playerLogin, antiProbeMissileName, targetOwnerName, targetProbeName);
		fireAntiProbeMissileCheckResult.antiProbeMissile.fire(targetOwnerName, targetProbeName, fireAntiProbeMissileCheckResult.source, fireAntiProbeMissileCheckResult.destination);
	}

	private static class FireAntiProbeMissileCheckResult
	{
		final AntiProbeMissile	antiProbeMissile;
		final RealLocation		source;
		final RealLocation		destination;

		public FireAntiProbeMissileCheckResult(AntiProbeMissile antiProbeMissile, RealLocation source, RealLocation destination)
		{
			this.antiProbeMissile = antiProbeMissile;
			this.source = source;
			this.destination = destination;
		}
	}

	public FireAntiProbeMissileCheckResult checkFireAntiProbeMissile(String playerLogin, String antiProbeMissileName, String targetOwnerName, String targetProbeName) throws RunningGameCommandException
	{
		AntiProbeMissile antiProbeMissile = db.getUnit(AntiProbeMissile.class, playerLogin, antiProbeMissileName);
		if (antiProbeMissile == null) throw new RunningGameCommandException("AntiProbeMissile '" + antiProbeMissileName + "' does not exist.");

		if (antiProbeMissile.isMoving()) throw new RunningGameCommandException("AntiProbeMissile '" + antiProbeMissileName + "' has already been fired.");

		if (antiProbeMissile.isFired()) throw new RunningGameCommandException("AntiProbeMissile '" + antiProbeMissileName + "' is already fired.");

		RealLocation destination;
		Probe targetProbe = db.getUnit(Probe.class, targetOwnerName, targetProbeName);
		if (targetProbe != null)
		{
			destination = targetProbe.getCurrentLocationView(db.getDate(), playerLogin, false);
		}
		else
		{
			UnitMarker um = db.getUnitMarker(playerLogin, targetOwnerName, targetProbeName);
			if (um == null || um.getUnit() == null || !org.axan.sep.common.Probe.class.isInstance(um.getUnit()))
			{
				throw new RunningGameCommandException("AntiProbeMissile '" + antiProbeMissileName + "' target '" + targetProbeName + "' does not exist.");
			}

			org.axan.sep.common.Probe probe = org.axan.sep.common.Probe.class.cast(um.getUnit());

			if (probe.isMoving()) throw new RunningGameCommandException("AntiProbeMissile '" + antiProbeMissileName + "' cannot be fired on moving target '" + targetProbeName + "'");

			destination = probe.getCurrentLocation();
		}

		if (destination == null) throw new RunningGameCommandException("AntiProbeMissile '" + antiProbeMissileName + "' target '" + targetProbeName + "' does not exist.");

		return new FireAntiProbeMissileCheckResult(antiProbeMissile, antiProbeMissile.getRealLocation(), destination);
	}

	public CommandCheckResult canLaunchProbe(String playerLogin, LaunchProbeParams p)
	{
		try
		{
			checkLaunchProbe(playerLogin, p.probeName, p.destination);
		}
		catch(Throwable t)
		{
			return new CommandCheckResult(t);
		}
		return new CommandCheckResult();
	}

	public void launchProbe(String playerLogin, String probeName, RealLocation destination) throws RunningGameCommandException
	{
		LaunchProbeCheckResult launchProbeCheckResult = checkLaunchProbe(playerLogin, probeName, destination);
		launchProbeCheckResult.probe.launch(launchProbeCheckResult.destination);
	}

	private static class LaunchProbeCheckResult
	{
		final Probe			probe;
		final RealLocation	destination;

		public LaunchProbeCheckResult(Probe probe, RealLocation destination)
		{
			this.probe = probe;
			this.destination = destination;
		}
	}

	public LaunchProbeCheckResult checkLaunchProbe(String playerLogin, String probeName, RealLocation destination) throws RunningGameCommandException
	{
		Probe probe = db.getUnit(Probe.class, playerLogin, probeName);
		if (probe == null) throw new RunningGameCommandException("Probe '" + probeName + "' does not exist.");

		if (probe.isMoving()) throw new RunningGameCommandException("Probe '" + probeName + "' has already been launched.");

		if (probe.isDeployed()) throw new RunningGameCommandException("Probe '" + probeName + "' is already deployed.");

		if (destination.x < 0 || destination.x >= db.getGameConfig().getDimX()) throw new RunningGameCommandException("Probe '" + probeName + "' destination is incorrect (x).");
		if (destination.y < 0 || destination.y >= db.getGameConfig().getDimY()) throw new RunningGameCommandException("Probe '" + probeName + "' destination is incorrect (y).");
		if (destination.z < 0 || destination.z >= db.getGameConfig().getDimZ()) throw new RunningGameCommandException("Probe '" + probeName + "' destination is incorrect (z).");

		if (isTravellingTheSun(probe.getRealLocation(), destination))
		{
			throw new RunningGameCommandException("Impossible path : " + probe.getRealLocation() + " to " + destination + ", cannot travel the sun.");
		}

		return new LaunchProbeCheckResult(probe, destination);
	}

	public CommandCheckResult canMoveFleet(String playerLogin, MoveFleetParams p)
	{
		try
		{
			checkMoveFleet(playerLogin, p.fleetName, p.checkpoints);
		}
		catch(Throwable t)
		{
			return new CommandCheckResult(t);
		}
		return new CommandCheckResult();
	}

	public void moveFleet(String playerLogin, String fleetName, Stack<org.axan.sep.common.Fleet.Move> checkpoints) throws RunningGameCommandException
	{
		MoveFleetCheckResult moveFleetCheckResult = checkMoveFleet(playerLogin, fleetName, checkpoints);
		moveFleetCheckResult.fleet.updateMoveOrder(moveFleetCheckResult.locatedCheckpoints);
	}

	private static class MoveFleetCheckResult
	{
		final Fleet						fleet;
		final Stack<org.axan.sep.common.Fleet.Move>	locatedCheckpoints;

		public MoveFleetCheckResult(Fleet fleet, Stack<org.axan.sep.common.Fleet.Move> locatedCheckpoints)
		{
			this.fleet = fleet;
			this.locatedCheckpoints = locatedCheckpoints;
		}
	}

	private boolean isTravellingTheSun(RealLocation a, RealLocation b)
	{
		for(RealLocation pathStep : SEPUtils.getAllPathLoc(a, b))
		{
			Area area = db.getArea(pathStep.asLocation());
			if (area != null && area.isSun()) return true;
		}
		
		return false;
	}
	
	public MoveFleetCheckResult checkMoveFleet(String playerLogin, String fleetName, Stack<org.axan.sep.common.Fleet.Move> checkpoints) throws RunningGameCommandException
	{
		Fleet fleet = db.getUnit(Fleet.class, playerLogin, fleetName);
		if (fleet == null) throw new RunningGameCommandException("Fleet '" + fleetName + "' does not exist.");

		// Check paths
		Stack<org.axan.sep.common.Fleet.Move> locatedCheckpoints = new Stack<org.axan.sep.common.Fleet.Move>();

		RealLocation currentStart = (fleet.isMoving() ? fleet.getDestinationLocation() : fleet.getRealLocation());
		for(org.axan.sep.common.Fleet.Move move : checkpoints)
		{
			Location destinationLocation = db.getCelestialBody(move.getDestinationName()).getLocation();
			if (destinationLocation == null) throw new RunningGameCommandException("Unexpected error : checkpoint destination '" + move.getDestinationName() + "' not found.");

			if (isTravellingTheSun(currentStart, destinationLocation.asRealLocation()))
			{
				throw new RunningGameCommandException("Impossible path : " + currentStart + " to " + destinationLocation.asRealLocation() + ", cannot travel the sun.");
			}

			currentStart = destinationLocation.asRealLocation();

			locatedCheckpoints.add(new org.axan.sep.common.Fleet.Move(move, destinationLocation.asRealLocation()));
		}

		return new MoveFleetCheckResult(fleet, locatedCheckpoints);
	}

	public CommandCheckResult canFormFleet(String playerLogin, FormFleetParams p)
	{
		try
		{
			checkFormFleet(playerLogin, p.productiveCelestialBodyName, p.fleetName, p.fleetToFormStarships, p.fleetToFormSpecialUnits);
		}
		catch(Throwable t)
		{
			return new CommandCheckResult(t);
		}
		return new CommandCheckResult();
	}

	public void formFleet(String playerLogin, String planetName, String fleetName, Map<org.axan.sep.common.StarshipTemplate, Integer> fleetToFormStarships, Set<String> fleetToFormSpecialUnits) throws RunningGameCommandException
	{
		FormFleetCheckResult formFleetCheckResult = checkFormFleet(playerLogin, planetName, fleetName, fleetToFormStarships, fleetToFormSpecialUnits);
		db.insertUnit(formFleetCheckResult.newFleet);
		formFleetCheckResult.productiveCelestialBody.removeFromUnasignedFleet(playerLogin, fleetToFormStarships, formFleetCheckResult.newFleet.getSpecialUnits());
	}

	private static class FormFleetCheckResult
	{
		final ProductiveCelestialBody	productiveCelestialBody;
		final Fleet						newFleet;

		public FormFleetCheckResult(ProductiveCelestialBody productiveCelestialBody, Fleet newFleet)
		{
			this.productiveCelestialBody = productiveCelestialBody;
			this.newFleet = newFleet;
		}
	}

	private FormFleetCheckResult checkFormFleet(String playerLogin, String productiveCelestialBodyName, String fleetName, Map<org.axan.sep.common.StarshipTemplate, Integer> fleetToFormStarships, Set<String> fleetToFormSpecialUnits) throws RunningGameCommandException
	{
		ProductiveCelestialBody productiveCelestialBody = db.getCelestialBody(productiveCelestialBodyName, ProductiveCelestialBody.class);
		if (productiveCelestialBody == null) throw new RunningGameCommandException("Celestial body '" + productiveCelestialBodyName + "' does not exist.");

		Fleet unasignedFleet = productiveCelestialBody.getUnasignedFleet(playerLogin);
		if (unasignedFleet == null) throw new RunningGameCommandException("No available unasigned fleet on celestial body '" + productiveCelestialBodyName + "'");

		Fleet fleet = db.getUnit(Fleet.class, playerLogin, fleetName);
		if (fleet != null) throw new RunningGameCommandException("Fleet named '" + fleetName + "' already exist.");

		// Starship availability check		
		for(Entry<org.axan.sep.common.StarshipTemplate, Integer> e : fleetToFormStarships.entrySet())
		{
			if (e.getValue() <= 0) continue;

			int qt = e.getValue();
			if (!unasignedFleet.getStarships().containsKey(e.getKey())) throw new RunningGameCommandException("Unasigned fleet does not have required starship type '" + e.getKey().getName() + "'");
			if (unasignedFleet.getStarships().get(e.getKey()) < qt) throw new RunningGameCommandException("Unasigned flee does not have enough starship type '" + e.getKey() + "'");
		}

		// Special units availability check
		Set<ISpecialUnit> specialUnits = new HashSet<ISpecialUnit>();
		
		if (fleetToFormSpecialUnits != null) for(String specialUnitName : fleetToFormSpecialUnits)
		{
			ISpecialUnit specialUnit = unasignedFleet.getSpecialUnit(specialUnitName);
			
			if (specialUnit == null) throw new RunningGameCommandException("Cannot find special unit '"+specialUnitName+"' on '"+productiveCelestialBodyName+"'");			
			if (!specialUnit.canJoinFleet()) throw new RunningGameCommandException("Special unit '"+specialUnitName+"' cannot join fleet.");
			
			specialUnits.add(specialUnit);
		}
				
		Fleet newFleet = new Fleet(db, fleetName, playerLogin, productiveCelestialBody.getLocation().asRealLocation(), fleetToFormStarships, specialUnits, false, null, null);

		return new FormFleetCheckResult(productiveCelestialBody, newFleet);
	}

	public CommandCheckResult canDismantleFleet(String playerLogin, DismantleFleetParams p)
	{
		try
		{
			checkDismantleFleet(playerLogin, p.fleetName);
		}
		catch(Throwable t)
		{
			return new CommandCheckResult(t);
		}
		return new CommandCheckResult();
	}

	public void dismantleFleet(String playerName, String fleetName) throws RunningGameCommandException
	{
		DismantleFleetCheckResult dismantleFleetCheckResult = checkDismantleFleet(playerName, fleetName);
		
		db.removeUnit(dismantleFleetCheckResult.fleet.getKey());
		dismantleFleetCheckResult.productiveCelestialBody.mergeToUnasignedFleet(playerName, dismantleFleetCheckResult.fleet.getStarships(), dismantleFleetCheckResult.fleet.getSpecialUnits());
	}

	private static class DismantleFleetCheckResult
	{
		final ProductiveCelestialBody	productiveCelestialBody;
		final Fleet						fleet;

		public DismantleFleetCheckResult(ProductiveCelestialBody productiveCelestialBody, Fleet fleet)
		{
			this.productiveCelestialBody = productiveCelestialBody;
			this.fleet = fleet;
		}
	}

	private DismantleFleetCheckResult checkDismantleFleet(String playerName, String fleetName) throws RunningGameCommandException
	{
		Fleet fleet = db.getUnit(Fleet.class, playerName, fleetName);
		if (fleet == null) throw new RunningGameCommandException("Fleet '" + fleetName + "' does not exist.");

		ProductiveCelestialBody productiveCelestialBody = db.getCelestialBody(fleet.getRealLocation().asLocation(), ProductiveCelestialBody.class);		
		if (productiveCelestialBody == null) throw new RunningGameCommandException("Fleet is in travel.");

		return new DismantleFleetCheckResult(productiveCelestialBody, fleet);
	}

	public CommandCheckResult canMakeProbes(String playerLogin, MakeProbesParams p)
	{
		MakeProbesCheckResult check;
		try
		{
			check = checkMakeProbes(playerLogin, p.planetName, p.probeName, p.quantity);
		}
		catch(Throwable t)
		{
			return new CommandCheckResult(t);
		}
		return new CommandCheckResult(check.carbonCost, check.populationCost);
	}

	public void makeProbes(String playerLogin, String planetName, String probeName, int quantity) throws RunningGameCommandException
	{
		MakeProbesCheckResult makeProbesCheckResult = checkMakeProbes(playerLogin, planetName, probeName, quantity);
		for(Probe p : makeProbesCheckResult.newProbes)
		{
			db.insertUnit(p);
		}
		makeProbesCheckResult.planet.setCarbon(makeProbesCheckResult.planet.getCarbon() - makeProbesCheckResult.carbonCost);
		makeProbesCheckResult.planet.setPopulation(makeProbesCheckResult.planet.getPopulation() - makeProbesCheckResult.populationCost);
	}

	private static class MakeProbesCheckResult
	{
		final Planet		planet;
		final int			carbonCost;
		final int			populationCost;
		final Set<Probe>	newProbes;

		public MakeProbesCheckResult(Planet planet, int carbonCost, int populationCost, Set<Probe> newProbes)
		{
			this.planet = planet;
			this.carbonCost = carbonCost;
			this.populationCost = populationCost;
			this.newProbes = newProbes;
		}
	}

	private MakeProbesCheckResult checkMakeProbes(String playerName, String planetName, String probeName, int quantity) throws RunningGameCommandException
	{
		Planet planet = db.getCelestialBody(planetName, Planet.class, playerName);
		if (planet == null) throw new RunningGameCommandException("Celestial body '" + planetName + "' does not exist.");

		StarshipPlant starshipPlant = planet.getBuilding(StarshipPlant.class);

		// If planet has no starshipPlant.
		if (starshipPlant == null) throw new RunningGameCommandException("No starship plant on planet '" + planetName + "'");

		// If starship has just been build this turn.
		if (starshipPlant.getLastBuildDate() >= db.getDate()) throw new RunningGameCommandException("Starship plant is still in construction.");

		// Price check
		int carbonCost = org.axan.sep.common.Probe.PRICE_CARBON * quantity;
		int populationCost = org.axan.sep.common.Probe.PRICE_POPULATION * quantity;

		if (carbonCost == 0 && populationCost == 0) throw new RunningGameCommandException("Seems like quantity is null.");
		if (quantity < 0) throw new RunningGameCommandException("Quantity cannot be lesser than 0.");

		if (planet.getCarbon() < carbonCost) throw new RunningGameCommandException("Not enough carbon.");
		if (planet.getPopulation() < populationCost) throw new RunningGameCommandException("Not enough population.");
		
		if (db.getUnit(Probe.class, playerName, probeName) != null || db.getUnit(Probe.class, playerName, probeName + "1") != null)
		{
			throw new RunningGameCommandException("Probe serial '" + probeName + "' already exist.");
		}

		Set<Probe> newProbes = new HashSet<Probe>();
		for(int i = 0; i < quantity; ++i)
		{
			newProbes.add(new Probe(db, probeName + i, playerName, planet.getLocation().asRealLocation(), false, null));
		}

		return new MakeProbesCheckResult(planet, carbonCost, populationCost, newProbes);
	}

	public CommandCheckResult canMakeAntiProbeMissiles(String playerLogin, MakeAntiProbeMissilesParams p)
	{
		MakeAntiProbeMissilesCheckResult check;
		try
		{
			check = checkMakeAntiProbeMissiles(playerLogin, p.planetName, p.antiProbeMissileName, p.quantity);
		}
		catch(Throwable t)
		{
			return new CommandCheckResult(t);
		}
		return new CommandCheckResult(check.carbonCost, check.populationCost);
	}

	public void makeAntiProbeMissiles(String playerLogin, String planetName, String antiProbeMissileName, int quantity) throws RunningGameCommandException
	{
		MakeAntiProbeMissilesCheckResult makeAntiProbeMissilesCheckResult = checkMakeAntiProbeMissiles(playerLogin, planetName, antiProbeMissileName, quantity);
		for(AntiProbeMissile p : makeAntiProbeMissilesCheckResult.newAntiProbeMissiles)
		{
			db.insertUnit(p);
		}
		makeAntiProbeMissilesCheckResult.planet.setCarbon(makeAntiProbeMissilesCheckResult.planet.getCarbon() - makeAntiProbeMissilesCheckResult.carbonCost);
		makeAntiProbeMissilesCheckResult.planet.setPopulation(makeAntiProbeMissilesCheckResult.planet.getPopulation() - makeAntiProbeMissilesCheckResult.populationCost);
	}

	private static class MakeAntiProbeMissilesCheckResult
	{
		final Planet				planet;
		final int					carbonCost;
		final int					populationCost;
		final Set<AntiProbeMissile>	newAntiProbeMissiles;

		public MakeAntiProbeMissilesCheckResult(Planet planet, int carbonCost, int populationCost, Set<AntiProbeMissile> newAntiProbeMissiles)
		{
			this.planet = planet;
			this.carbonCost = carbonCost;
			this.populationCost = populationCost;
			this.newAntiProbeMissiles = newAntiProbeMissiles;
		}
	}

	private MakeAntiProbeMissilesCheckResult checkMakeAntiProbeMissiles(String playerName, String planetName, String antiProbeMissileName, int quantity) throws RunningGameCommandException
	{
		Planet planet = db.getCelestialBody(planetName, Planet.class, playerName);
		if (planet == null) throw new RunningGameCommandException("Celestial body '" + planetName + "' does not exist.");

		StarshipPlant starshipPlant = planet.getBuilding(StarshipPlant.class);

		// If planet has no starshipPlant.
		if (starshipPlant == null) throw new RunningGameCommandException("No starship plant on planet '" + planetName + "'");

		// If starship has just been build this turn.
		if (starshipPlant.getLastBuildDate() >= db.getDate()) throw new RunningGameCommandException("Starship plant is still in construction.");

		// Price check
		int carbonCost = org.axan.sep.common.AntiProbeMissile.PRICE_CARBON * quantity;
		int populationCost = org.axan.sep.common.AntiProbeMissile.PRICE_POPULATION * quantity;

		if (carbonCost == 0 && populationCost == 0) throw new RunningGameCommandException("Seems like quantity is null.");
		if (quantity < 0) throw new RunningGameCommandException("Quantity cannot be lesser than 0.");

		if (planet.getCarbon() < carbonCost) throw new RunningGameCommandException("Not enough carbon.");
		if (planet.getPopulation() < populationCost) throw new RunningGameCommandException("Not enough population.");
		
		if (db.getUnit(Probe.class, playerName, antiProbeMissileName) != null || db.getUnit(Probe.class, playerName, antiProbeMissileName + "1") != null)
		{
			throw new RunningGameCommandException("AntiProbeMissile serial '" + antiProbeMissileName + "' already exist.");
		}

		Set<AntiProbeMissile> newAntiProbeMissiles = new HashSet<AntiProbeMissile>();
		for(int i = 0; i < quantity; ++i)
		{
			newAntiProbeMissiles.add(new AntiProbeMissile(db, antiProbeMissileName + i, playerName, planet.getLocation().asRealLocation(), false, null));
		}

		return new MakeAntiProbeMissilesCheckResult(planet, carbonCost, populationCost, newAntiProbeMissiles);
	}

	public CommandCheckResult canMakeStarships(String playerLogin, MakeStarshipsParams p)
	{
		MakeStarshipsCheckResult check;
		try
		{
			check = checkMakeStarships(playerLogin, p.planetName, p.starshipsToMake);
		}
		catch(Throwable t)
		{
			return new CommandCheckResult(t);
		}
		return new CommandCheckResult(check.carbonCost, check.populationCost);
	}

	public void makeStarships(String playerLogin, String planetName, Map<org.axan.sep.common.StarshipTemplate, Integer> starshipsToMake) throws RunningGameCommandException
	{
		MakeStarshipsCheckResult makeStarshipsCheckResult = checkMakeStarships(playerLogin, planetName, starshipsToMake);

		Fleet unassignedFleet = makeStarshipsCheckResult.planet.getUnasignedFleet(playerLogin);
		makeStarshipsCheckResult.planet.mergeToUnasignedFleet(playerLogin, starshipsToMake, null);

		makeStarshipsCheckResult.planet.setCarbon(makeStarshipsCheckResult.planet.getCarbon() - makeStarshipsCheckResult.carbonCost);
		makeStarshipsCheckResult.planet.setPopulation(makeStarshipsCheckResult.planet.getPopulation() - makeStarshipsCheckResult.populationCost);
	}

	private static class MakeStarshipsCheckResult
	{
		final Planet		planet;
		final int			carbonCost;
		final int			populationCost;

		public MakeStarshipsCheckResult(Planet planet, int carbonCost, int populationCost)
		{
			this.planet = planet;
			this.carbonCost = carbonCost;
			this.populationCost = populationCost;
		}
	}

	private MakeStarshipsCheckResult checkMakeStarships(String playerName, String planetName, Map<org.axan.sep.common.StarshipTemplate, Integer> starshipsToMake) throws RunningGameCommandException
	{
		Planet planet = db.getCelestialBody(planetName, Planet.class, playerName);
		if (planet == null) throw new RunningGameCommandException("Celestial body '" + planetName + "' does not exist.");

		StarshipPlant starshipPlant = planet.getBuilding(StarshipPlant.class);

		// If planet has no starshipPlant.
		if (starshipPlant == null) throw new RunningGameCommandException("No starship plant on planet '" + planetName + "'");

		// If starship has just been build this turn.
		if (starshipPlant.getLastBuildDate() >= db.getDate()) throw new RunningGameCommandException("Starship plant is still in construction.");

		// Price check
		int carbonCost = 0;
		int populationCost = 0;

		for(Entry<org.axan.sep.common.StarshipTemplate, Integer> e : starshipsToMake.entrySet())
		{
			if (e.getValue() <= 0) continue;

			int carbonPrice = 0;
			int populationPrice = 0;

			carbonPrice = e.getKey().getCarbonPrice();			
			populationPrice = e.getKey().getPopulationPrice();
			
			if (carbonPrice == 0 && populationPrice == 0)
			{
				throw new RunningGameCommandException("Implementation error : Price are not defined for Starship template '" + e.getKey().getName() + "'");
			}

			carbonCost += carbonPrice * e.getValue();
			populationCost += populationPrice * e.getValue();
		}

		if (carbonCost == 0 && populationCost == 0) throw new RunningGameCommandException("Seems like no starships are selected (cost is null).");

		if (planet.getCarbon() < carbonCost) throw new RunningGameCommandException("Not enough carbon.");
		if (planet.getPopulation() < populationCost) throw new RunningGameCommandException("Not enough population.");

		return new MakeStarshipsCheckResult(planet, carbonCost, populationCost);
	}

	public CommandCheckResult canChangeDiplomacy(String playerLogin, ChangeDiplomacyParams p)
	{
		try
		{
			checkChangeDiplomacy(playerLogin, p.newPolicies);
		}
		catch(Throwable t)
		{
			return new CommandCheckResult(t);
		}
		return new CommandCheckResult();
	}

	public void changeDiplomacy(String playerLogin, Map<String,PlayerPolicies> newPolicies) throws RunningGameCommandException
	{
		ChangeDiplomacyCheckResult changeDiplomacyCheckResult = checkChangeDiplomacy(playerLogin, newPolicies);
		
		db.getPlayerPolicies(playerLogin).update(newPolicies);
	}

	private static class ChangeDiplomacyCheckResult
	{		
		public ChangeDiplomacyCheckResult()
		{
			
		}
	}

	private ChangeDiplomacyCheckResult checkChangeDiplomacy(String playerLogin, Map<String,PlayerPolicies> newPolicies) throws RunningGameCommandException
	{
		if (newPolicies.get(playerLogin) != null) throw new RunningGameCommandException("Cannot have a diplomacy toward ourselves.");		
		return new ChangeDiplomacyCheckResult();
	}
	
	public CommandCheckResult canAttackEnemiesFleet(String playerLogin, AttackEnemiesFleetParams p)
	{
		try
		{
			checkAttackEnemiesFleet(playerLogin, p.celestialBodyName);
		}
		catch(Throwable t)
		{
			return new CommandCheckResult(t);
		}
		
		return new CommandCheckResult();
	}

	public void attackEnemiesFleet(String playerLogin, String celestialBodyName) throws RunningGameCommandException
	{
		AttackEnemiesFleetCheckResult attackEnemiesFleetCheckResult = checkAttackEnemiesFleet(playerLogin, celestialBodyName);
		attackEnemiesFleetCheckResult.productiveCelestialBody.addConflictInititor(playerLogin);
	}
	
	private static class AttackEnemiesFleetCheckResult
	{
		final ProductiveCelestialBody productiveCelestialBody;
		public AttackEnemiesFleetCheckResult(ProductiveCelestialBody productiveCelestialBody)
		{
			this.productiveCelestialBody = productiveCelestialBody;
		}
	}

	private AttackEnemiesFleetCheckResult checkAttackEnemiesFleet(String playerName, String celestialBodyName) throws RunningGameCommandException
	{
		ProductiveCelestialBody productiveCelestialBody = db.getCelestialBody(celestialBodyName, ProductiveCelestialBody.class);
		if (productiveCelestialBody == null) throw new RunningGameCommandException("Celestial body '"+celestialBodyName+"' is not a productive celestial body.");
		
		return new AttackEnemiesFleetCheckResult(productiveCelestialBody);			
	}
	
	/////
	
	public CommandCheckResult canBuildSpaceRoad(String playerLogin, BuildSpaceRoadParams p)
	{
		BuildSpaceRoadCheckResult check;
		try
		{
			check = checkBuildSpaceRoad(playerLogin, p.productiveCelestialBodyNameA, p.productiveCelestialBodyNameB);
		}
		catch(Throwable t)
		{
			return new CommandCheckResult(t);
		}
		return new CommandCheckResult(check.price, 0);
	}

	public void buildSpaceRoad(String playerLogin, String productiveCelestialBodyNameA, String productiveCelestialBodyNameB) throws RunningGameCommandException
	{
		BuildSpaceRoadCheckResult buildSpaceRoadCheckResult = checkBuildSpaceRoad(playerLogin, productiveCelestialBodyNameA, productiveCelestialBodyNameB);
			
		db.insertUnit(buildSpaceRoadCheckResult.deliverer);
		buildSpaceRoadCheckResult.deliverer.launch(buildSpaceRoadCheckResult.destinationLocation.asRealLocation());		
		buildSpaceRoadCheckResult.payer.setCarbon(buildSpaceRoadCheckResult.payer.getCarbon() - buildSpaceRoadCheckResult.price);
	}

	private static class BuildSpaceRoadCheckResult
	{
		final SpaceRoadDeliverer deliverer;
		final Location destinationLocation;
		final ProductiveCelestialBody payer;
		final int price;
		
		public BuildSpaceRoadCheckResult(ProductiveCelestialBody payer, int price, SpaceRoadDeliverer deliverer, Location destinationLocation)
		{
			this.payer = payer;
			this.price = price;
			this.deliverer = deliverer;
			this.destinationLocation = destinationLocation;
		}
	}

	private BuildSpaceRoadCheckResult checkBuildSpaceRoad(String playerLogin, String sourceName, String destinationName) throws RunningGameCommandException
	{
		ProductiveCelestialBody source = db.getCelestialBody(sourceName, ProductiveCelestialBody.class);
		if (source == null) throw new RunningGameCommandException("Celestial body '"+sourceName+"' is not a productive celestial body.");
		
		SpaceCounter sourceSpaceCounter = source.getBuilding(SpaceCounter.class);
		if (sourceSpaceCounter == null) throw new RunningGameCommandException("'"+sourceName+"' has no space counter build.");
		
		ProductiveCelestialBody destination = db.getCelestialBody(destinationName, ProductiveCelestialBody.class);
		if (destination == null) throw new RunningGameCommandException("Celestial body '"+sourceName+"' is not a productive celestial body.");
		
		SpaceCounter destinationSpaceCounter = destination.getBuilding(SpaceCounter.class);
		if (destinationSpaceCounter == null) throw new RunningGameCommandException("'"+destinationName+"' has no space counter build.");		
	
		if (sourceSpaceCounter.hasSpaceRoadTo(destinationName))
		{
			throw new RunningGameCommandException("'"+sourceName+"' already has a space road to '"+destinationName+"'");
		}
		
		if (sourceSpaceCounter.hasSpaceRoadLinkedFrom(sourceName))
		{
			throw new RunningGameCommandException("'"+sourceName+"' already has a space road linked from '"+sourceName+"'");
		}
		
		if (isTravellingTheSun(source.getLocation().asRealLocation(), destination.getLocation().asRealLocation()))
		{
			throw new RunningGameCommandException("Impossible path : " + source.getLocation() + " to " + destination.getLocation() + ", cannot travel the sun.");
		}
		
		double distance = SEPUtils.getDistance(source.getLocation().asRealLocation(), destination.getLocation().asRealLocation());
		int price = (int) (db.getGameConfig().getSpaceRoadPricePerArea() * distance);
		
		if (!playerLogin.equals(source.getOwnerName()) || sourceSpaceCounter == null || sourceSpaceCounter.getAvailableRoadsBuilder() <= 0 || source.getCarbon() < price)
		{
			throw new RunningGameCommandException("None of the space road end can pay nor have free builder.");
		}
		
		String delivererId = sourceName+" to "+destinationName+" space road deliverer";
		if (db.getUnit(SpaceRoadDeliverer.class, playerLogin, delivererId) != null) throw new RunningGameCommandException("Space road from '"+sourceName+"' to '"+destinationName+"' is already in delivery.");
		SpaceRoadDeliverer deliverer = new SpaceRoadDeliverer(db, sourceName+" to "+destinationName+" space road deliverer", playerLogin, source.getLocation().asRealLocation(), sourceName, destinationName, null);
		
		return new BuildSpaceRoadCheckResult(source, price, deliverer, destination.getLocation());			
	}
	
	public CommandCheckResult canDemolishSpaceRoad(String playerLogin, DemolishSpaceRoadParams p)
	{
		try
		{
			checkDemolishSpaceRoad(playerLogin, p.sourceName, p.destinationName);
		}
		catch(Throwable t)
		{
			return new CommandCheckResult(t);
		}
		return new CommandCheckResult();
	}

	public void demolishSpaceRoad(String playerLogin, String sourceName, String destinationName) throws RunningGameCommandException
	{
		DemolishSpaceRoadCheckResult demolishSpaceRoadCheckResult = checkDemolishSpaceRoad(playerLogin, sourceName, destinationName);
		demolishSpaceRoadCheckResult.source.cutSpaceRoadLinkWith(destinationName);
	}

	private static class DemolishSpaceRoadCheckResult
	{
		final SpaceCounter source;
		
		public DemolishSpaceRoadCheckResult(SpaceCounter source)
		{
			this.source = source;			
		}
	}

	private DemolishSpaceRoadCheckResult checkDemolishSpaceRoad(String playerLogin, String sourceName, String destinationName) throws RunningGameCommandException
	{
		ProductiveCelestialBody source = db.getCelestialBody(sourceName, ProductiveCelestialBody.class);
		if (source == null) throw new RunningGameCommandException("Celestial body '"+sourceName+"' is not a productive celestial body.");
				
		SpaceCounter sourceSpaceCounter = source.getBuilding(SpaceCounter.class);
		if (sourceSpaceCounter == null) throw new RunningGameCommandException("'"+sourceName+"' has no space counter build.");
						
		if (!sourceSpaceCounter.hasSpaceRoadTo(destinationName) && !sourceSpaceCounter.hasSpaceRoadLinkedFrom(destinationName))
		{
			throw new RunningGameCommandException("'"+sourceName+"' has no space road link with '"+destinationName+"'");
		}
		
		return new DemolishSpaceRoadCheckResult(sourceSpaceCounter);			
	}
	
	////
	
	public CommandCheckResult canModifyCarbonOrder(String playerLogin, ModifyCarbonOrderParams p)
	{
		try
		{
			checkModifyCarbonOrder(playerLogin, p.originCelestialBodyName, p.nextCarbonOrders);
		}
		catch(Throwable t)
		{
			return new CommandCheckResult(t);
		}
		return new CommandCheckResult();
	}

	public void modifyCarbonOrder(String playerLogin, String originCelestialBodyName, Stack<CarbonOrder> nextCarbonOrders) throws RunningGameCommandException
	{
		ModifyCarbonOrderCheckResult modifyCarbonOrderCheckResult = checkModifyCarbonOrder(playerLogin, originCelestialBodyName, nextCarbonOrders);
		modifyCarbonOrderCheckResult.spaceCounter.modifyCarbonOrder(nextCarbonOrders);
	}

	private static class ModifyCarbonOrderCheckResult
	{
		final SpaceCounter spaceCounter;
		
		public ModifyCarbonOrderCheckResult(SpaceCounter spaceCounter)
		{
			this.spaceCounter = spaceCounter;
		}
	}

	private ModifyCarbonOrderCheckResult checkModifyCarbonOrder(String playerLogin, String originCelestialBodyName, Stack<CarbonOrder> nextCarbonOrders) throws RunningGameCommandException
	{
		ProductiveCelestialBody source = db.getCelestialBody(originCelestialBodyName, ProductiveCelestialBody.class);
		if (source == null) throw new RunningGameCommandException("Celestial body '"+originCelestialBodyName+"' is not a productive celestial body.");
		
		if (!playerLogin.equals(source.getOwnerName()))
		{
			throw new RunningGameCommandException("'"+playerLogin+"' is not the celestial body '"+originCelestialBodyName+"' owner.");
		}
		
		SpaceCounter sourceSpaceCounter = source.getBuilding(SpaceCounter.class);
		if (sourceSpaceCounter == null) throw new RunningGameCommandException("'"+originCelestialBodyName+"' has no space counter build.");
		 
		for(CarbonOrder order : nextCarbonOrders)
		{
			String destinationCelestialBodyName = order.getDestinationName();
			int amount = order.getAmount();
			
			ProductiveCelestialBody destination = db.getCelestialBody(destinationCelestialBodyName, ProductiveCelestialBody.class);
			if (destination == null) throw new RunningGameCommandException("Celestial body '"+destinationCelestialBodyName+"' is not a productive celestial body.");
			
			SpaceCounter destinationSpaceCounter = destination.getBuilding(SpaceCounter.class);
			if (destinationSpaceCounter == null) throw new RunningGameCommandException("'"+destinationCelestialBodyName+"' has no space counter build.");		
		
			if (amount < db.getGameConfig().getCarbonMinimalFreight()) throw new RunningGameCommandException("Carbon amount must be greater than 0.");
			
			if (isTravellingTheSun(source.getLocation().asRealLocation(), destination.getLocation().asRealLocation()))
			{
				throw new RunningGameCommandException("Impossible path : " + source.getLocation() + " to " + destination.getLocation() + ", cannot travel the sun.");
			}
		}
		
		return new ModifyCarbonOrderCheckResult(sourceSpaceCounter);
	}
	
	////

	
	
	
}
