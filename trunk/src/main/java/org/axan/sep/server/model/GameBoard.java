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
import java.util.Stack;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.axan.eplib.utils.Basic;
import org.axan.sep.common.CarbonOrder;
import org.axan.sep.common.CommandCheckResult;
import org.axan.sep.common.GameConfig;
import org.axan.sep.common.ISpecialUnit;
import org.axan.sep.common.SEPUtils;
import org.axan.sep.common.StarshipTemplate;
import org.axan.sep.common.TravellingLogEntryUnitSeen;
import org.axan.sep.common.Diplomacy.PlayerPolicies;
import org.axan.sep.common.Diplomacy.PlayerPolicies.eForeignPolicy;
import org.axan.sep.common.Protocol.ServerRunningGame.RunningGameCommandException;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.SEPUtils.RealLocation;
import org.axan.sep.server.SEPServer;
import org.axan.sep.server.model.IMarker.Key;
import org.axan.sep.server.model.ProductiveCelestialBody.CelestialBodyBuildException;
import org.axan.sep.server.model.SpaceCounter.SpaceRoad;

import sun.security.action.GetBooleanAction;


public class GameBoard implements Serializable
{
	private static final long			serialVersionUID	= 1L;

	private static final Logger			log					= SEPServer.log;

	private static final Random			rnd					= new Random();
	
	private final DataBase				db;

	private GameBoard(Hashtable<String, org.axan.sep.common.Player> players, org.axan.sep.common.GameConfig config, int date, Hashtable<Location, Area> areas, Hashtable<ICelestialBody.Key, ICelestialBody> celestialBodies, Hashtable<String, Hashtable<IMarker.Key, IMarker>> playersMarkers, RealLocation sunLocation, Hashtable<Unit.Key, Unit> units, Map<String, Diplomacy> playersPolicies)
	{
		this.db = new DataBase(players, config, date, areas, celestialBodies, playersMarkers, sunLocation, units, playersPolicies);
	}

	/**
	 * Full new game constructor.
	 * 
	 * @param playerList
	 * @param gameConfig
	 */
	public GameBoard(Set<org.axan.sep.common.Player> players, org.axan.sep.common.GameConfig config, int date)
	{
		this(new Hashtable<String, org.axan.sep.common.Player>(),  config, date, new Hashtable<Location, Area>(players.size()*2), new Hashtable<ICelestialBody.Key, ICelestialBody>(players.size()*2), new Hashtable<String, Hashtable<IMarker.Key, IMarker>>(players.size()*2), new RealLocation(Double.valueOf(config.getDimX()) / 2.0, Double.valueOf(config.getDimY()) / 2.0, Double.valueOf(config.getDimZ()) / 2.0), new Hashtable<Unit.Key, Unit>(), new Hashtable<String, Diplomacy>());

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
					Stack<RealLocation> path = org.axan.sep.common.SEPUtils.getAllPathLoc(planetLocation.asRealLocation(), l.asRealLocation());
					for(RealLocation pl : path)
					{
						a = db.getArea(pl.asLocation());
						if (a != null && a.isSun())
						{
							locationOk = false;
							break;
						}
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

			Class<? extends ICelestialBody> serverCelestialBodyType;
			String nextName = generateCelestialBodyName();
			try
			{
				serverCelestialBodyType = Class.forName("server.model." + celestialBodyType.getSimpleName()).asSubclass(ICelestialBody.class);
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

	/**
	 * @param playerLogin
	 */
	public org.axan.sep.common.PlayerGameBoard getPlayerGameBoard(String playerLogin)
	{
		log.log(Level.INFO, "getGameBoard(" + playerLogin + ")");
		
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
					if (!isVisible && ((unassignedFleet != null && !unassignedFleet.isEmpty()) || (productiveCelestialBody != null && !db.getUnits(location, playerLogin).isEmpty())))
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
		
		return new org.axan.sep.common.PlayerGameBoard(playerUniverseView, db.getSunLocation(), db.getDate(), playersPoliciesView);
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
		for(float currentStep = 0; currentStep <= 1; currentStep += step)
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
					if (SEPUtils.getDistance(currentStepLocation, movedUnitCurrentLocation) <= 1)
					{
						u.addTravelligLogEntry(new TravellingLogEntryUnitSeen("Unit seen", db.getDate(), currentStep, movedUnitCurrentLocation, movedUnit.getPlayerView(db.getDate(), u.getOwnerName(), true)));
						movedUnit.addTravelligLogEntry(new TravellingLogEntryUnitSeen("Unit seen", db.getDate(), currentStep, currentStepLocation, u.getPlayerView(db.getDate(), movedUnit.getOwnerName(), true)));
					}
				}
				
				for(Probe probe : deployedProbes)
				{
					RealLocation probeLocation = probe.getRealLocation();
					distance = SEPUtils.getDistance(probeLocation, currentStepLocation);

					if (distance <= db.getGameConfig().getProbeScope())
					{
						probe.addTravelligLogEntry(new TravellingLogEntryUnitSeen("Unit seen", db.getDate(), currentStep, currentStepLocation, u.getPlayerView(db.getDate(), probe.getOwnerName(), true)));
					}

					if (distance <= 1)
					{
						u.addTravelligLogEntry(new TravellingLogEntryUnitSeen("Probe seen", db.getDate(), currentStep, probeLocation, probe.getPlayerView(db.getDate(), u.getOwnerName(), true)));
					}
				}

				currentStepMovedUnits.add(u);
			}
		}
		
		Set<AntiProbeMissile> explodingAntiProbeMissiles = new HashSet<AntiProbeMissile>();
		
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
		
		// Conflicts
		for(ProductiveCelestialBody productiveCelestialBody : productiveCelestialBodies)
		{
			if (!productiveCelestialBody.getConflictInitiators().isEmpty())
			{
				resolveConflict(productiveCelestialBody);
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

	public void resolveConflict(ProductiveCelestialBody productiveCelestialBody)
	{
		// Initiate conflicts table : PlayerName/PlayerName: boolean
		Map<String, Map<String, Boolean>> conflictDiplomacy = resolveConflictDiplomacy(productiveCelestialBody);
						
		// List merged (unassigned fleets + fleets) forces for each players
		// Backup and remove original fleets (including unassigned ones). 
		Map<String, Fleet> mergedFleets = new Hashtable<String, Fleet>();
		Map<String, Set<Fleet>> originalFleets = new Hashtable<String, Set<Fleet>>();
		
		Location location = productiveCelestialBody.getLocation();
		
		for(String p : db.getPlayersKeySet())
		{
			if (productiveCelestialBody.getUnasignedFleet(p) != null)
			{												
				mergedFleets.put(p, new Fleet(db, p+" forces in conflict on "+productiveCelestialBody.getName()+" at turn "+db.getDate(), p, location.asRealLocation(), productiveCelestialBody.getUnasignedFleetStarships(p), productiveCelestialBody.getUnasignedFleetSpecialUnits(p), false));
				productiveCelestialBody.removeFromUnasignedFleet(p, productiveCelestialBody.getUnasignedFleetStarships(p), productiveCelestialBody.getUnasignedFleetSpecialUnits(p));
			}
		}
		
		Set<Fleet> fleetsToRemove = new HashSet<Fleet>();
		for(Fleet f : db.getUnits(location, Fleet.class))
		{						
			if (!mergedFleets.containsKey(f.getOwnerName()))
			{
				mergedFleets.put(f.getOwnerName(), new Fleet(db, f.getOwnerName()+" forces in conflict on "+productiveCelestialBody.getName()+" at turn "+db.getDate(), f.getOwnerName(), location.asRealLocation(), f.getStarships(), f.getSpecialUnits(), false));
			}
			else
			{
				mergedFleets.get(f.getOwnerName()).merge(f.getStarships(), f.getSpecialUnits());
			}
			
			fleetsToRemove.add(f);			
		}
		
		for(Fleet f : fleetsToRemove)
		{
			if (!originalFleets.containsKey(f.getOwnerName())) originalFleets.put(f.getOwnerName(), new HashSet<Fleet>());
			originalFleets.get(f.getOwnerName()).add(f);
			db.removeUnit(f.getKey());
		}
		
		// Run battle
		Map<String, Fleet> survivors = resolveBattle(conflictDiplomacy, mergedFleets);
		
		// Restore original fleets from survived ones.
		for(Map.Entry<String, Fleet> e : survivors.entrySet())
		{
			productiveCelestialBody.mergeToUnasignedFleet(e.getKey(), e.getValue().getStarships(), e.getValue().getSpecialUnits());
		}
		
		// End conflict, enventually change the celestial body owner, and check for new conflict.
		productiveCelestialBody.endConflict();
		
		if (productiveCelestialBody.getOwnerName() == null || !survivors.keySet().contains(productiveCelestialBody.getOwnerName()))
		{
			Random rnd = new Random();
			String newOwner = survivors.keySet().toArray(new String[survivors.keySet().size()])[rnd.nextInt(survivors.keySet().size())];
			productiveCelestialBody.changeOwner(newOwner);
			
			productiveCelestialBody.addConflictInititor(newOwner);
			resolveConflict(productiveCelestialBody);
		}
	}
	
	private static Map<String, Fleet> resolveBattle(Map<String, Map<String, Boolean>> conflictDiplomacy, Map<String, Fleet> forces)
	{
		Map<String, Fleet> survivors = new Hashtable<String, Fleet>(forces);
		Set<String> killed = new HashSet<String>();
		
		Set<String> fighting = new HashSet<String>();
		for(String p : survivors.keySet())
		{
			for(String t : survivors.keySet())
			{
				if (conflictDiplomacy.containsKey(p) && conflictDiplomacy.get(p).containsKey(t) && conflictDiplomacy.get(p).get(t))
				{
					fighting.add(p); fighting.add(t);
				}
			}
		}
		
		// Fake resolution
		boolean isFinished = true;
		do
		{			
			isFinished = true;
			for(String p : fighting)
			{
				for(String t : fighting)
				{
					if (killed.contains(t)) continue;
					
					if (conflictDiplomacy.containsKey(p) && conflictDiplomacy.get(p).containsKey(t) && conflictDiplomacy.get(p).get(t)) isFinished = false;
					
					if (!isFinished) break;
				}
				
				if (!isFinished) break;
			}
			
			if (!isFinished)
			{
				String victim = fighting.toArray(new String[fighting.size()])[rnd.nextInt(fighting.size())];
				survivors.remove(victim);
				fighting.remove(victim);
				killed.add(victim);				
			}
		}while(!isFinished);

		for(String s : fighting)
		{
			Fleet f = survivors.get(s);
			Map<StarshipTemplate, Integer> starships = new Hashtable<StarshipTemplate, Integer>();
			Set<ISpecialUnit> specialUnits = new HashSet<ISpecialUnit>();
			
			for(StarshipTemplate t : f.getStarships().keySet())
			{
				starships.put(t, rnd.nextInt(f.getStarships().get(t)));
			}
			
			Iterator<ISpecialUnit> it = f.getSpecialUnits().iterator();
			if (it.hasNext()) for(int i = rnd.nextInt(f.getSpecialUnits().size()); i > 0; --i)
			{
				specialUnits.add(it.next());
			}
			
			f.remove(starships, specialUnits);
		}
		
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
	
	
	public void demolish(String playerLogin, String celestialBodyName, Class<? extends org.axan.sep.common.IBuilding> buildingType) throws RunningGameCommandException
	{
		DemolishCheckResult demolishCheckResult = checkDemolish(playerLogin, celestialBodyName, buildingType);
		demolishCheckResult.productiveCelestialBody.demolishBuilding(demolishCheckResult.existingBuilding);
	}

	public CommandCheckResult canDemolish(String playerLogin, String celestialBodyName, Class<? extends org.axan.sep.common.IBuilding> buildingType)
	{
		try
		{
			checkDemolish(playerLogin, celestialBodyName, buildingType);
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

	public DemolishCheckResult checkDemolish(String playerName, String celestialBodyName, Class<? extends org.axan.sep.common.IBuilding> buildingType) throws RunningGameCommandException
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
		Set<org.axan.sep.common.ISpecialUnit> specialUnitsToMake = new HashSet<org.axan.sep.common.ISpecialUnit>();
		specialUnitsToMake.add(new org.axan.sep.common.GovernmentStarship(playerName+" government starship"));		
		embarkGovernmentCheckResult.planet.mergeToUnasignedFleet(playerName, null, specialUnitsToMake);
		embarkGovernmentCheckResult.planet.setCarbon(embarkGovernmentCheckResult.planet.getCarbon() - embarkGovernmentCheckResult.carbonCost);
		embarkGovernmentCheckResult.planet.setPopulation(embarkGovernmentCheckResult.planet.getPopulation() - embarkGovernmentCheckResult.populationCost);
	}

	public CommandCheckResult canEmbarkGovernment(String playerLogin)
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
	
	public CommandCheckResult canSettleGovernment(String playerLogin, String planetName)
	{
		try
		{
			checkSettleGovernment(playerLogin, planetName);
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

	public void build(String playerLogin, String celestialBodyName, Class<? extends org.axan.sep.common.IBuilding> buildingType) throws CelestialBodyBuildException
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

	public CommandCheckResult canBuild(String playerLogin, String celestialBodyName, Class<? extends org.axan.sep.common.IBuilding> buildingType)
	{
		BuildCheckResult check;
		try
		{
			check = checkBuild(playerLogin, celestialBodyName, buildingType);
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

	private BuildCheckResult checkBuild(String playerName, String celestialBodyName, Class<? extends org.axan.sep.common.IBuilding> buildingType) throws CelestialBodyBuildException
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
			carbonCost = building.getUpgradeCarbonCost();
			populationCost = building.getUpgradePopulationCost();
			newBuilding = building.getUpgraded(db.getDate());
		}
		else
		{
			carbonCost = ABuilding.getFirstCarbonCost(buildingType);
			populationCost = ABuilding.getFirstPopulationCost(buildingType);
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

	public CommandCheckResult canFireAntiProbeMissile(String playerLogin, String antiProbeMissileName, String targetOwnerName, String targetProbeName)
	{
		try
		{
			checkFireAntiProbeMissile(playerLogin, antiProbeMissileName, targetOwnerName, targetProbeName);
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

	public CommandCheckResult canLaunchProbe(String playerLogin, String probeName, RealLocation destination)
	{
		try
		{
			checkLaunchProbe(playerLogin, probeName, destination);
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

		for(RealLocation pathStep : SEPUtils.getAllPathLoc(probe.getRealLocation(), destination))
		{
			if (db.getArea(pathStep.asLocation()) != null && db.getArea(pathStep.asLocation()).isSun()) throw new RunningGameCommandException("Impossible path : " + probe.getRealLocation() + " to " + destination + ", cannot travel the sun.");
		}

		return new LaunchProbeCheckResult(probe, destination);
	}

	public CommandCheckResult canMoveFleet(String playerLogin, String fleetName, Stack<org.axan.sep.common.Fleet.Move> checkpoints)
	{
		try
		{
			checkMoveFleet(playerLogin, fleetName, checkpoints);
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

			for(RealLocation pathStep : SEPUtils.getAllPathLoc(currentStart, destinationLocation.asRealLocation()))
			{
				if (db.getArea(pathStep.asLocation()) != null && db.getArea(pathStep.asLocation()).isSun()) throw new RunningGameCommandException("Impossible path : " + currentStart + " to " + destinationLocation + ", cannot travel the sun.");
			}

			currentStart = destinationLocation.asRealLocation();

			locatedCheckpoints.add(new org.axan.sep.common.Fleet.Move(move, destinationLocation.asRealLocation()));
		}

		return new MoveFleetCheckResult(fleet, locatedCheckpoints);
	}

	public CommandCheckResult canFormFleet(String playerLogin, String planetName, String fleetName, Map<org.axan.sep.common.StarshipTemplate, Integer> fleetToFormStarships, Set<org.axan.sep.common.ISpecialUnit> fleetToFormSpecialUnits)
	{
		try
		{
			checkFormFleet(playerLogin, planetName, fleetName, fleetToFormStarships, fleetToFormSpecialUnits);
		}
		catch(Throwable t)
		{
			return new CommandCheckResult(t);
		}
		return new CommandCheckResult();
	}

	public void formFleet(String playerLogin, String planetName, String fleetName, Map<org.axan.sep.common.StarshipTemplate, Integer> fleetToFormStarships, Set<org.axan.sep.common.ISpecialUnit> fleetToFormSpecialUnits) throws RunningGameCommandException
	{
		FormFleetCheckResult formFleetCheckResult = checkFormFleet(playerLogin, planetName, fleetName, fleetToFormStarships, fleetToFormSpecialUnits);
		db.insertUnit(formFleetCheckResult.newFleet);
		formFleetCheckResult.productiveCelestialBody.removeFromUnasignedFleet(playerLogin, fleetToFormStarships, fleetToFormSpecialUnits);
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

	private FormFleetCheckResult checkFormFleet(String playerLogin, String productiveCelestialBodyName, String fleetName, Map<org.axan.sep.common.StarshipTemplate, Integer> fleetToFormStarships, Set<org.axan.sep.common.ISpecialUnit> fleetToFormSpecialUnits) throws RunningGameCommandException
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
		for(org.axan.sep.common.ISpecialUnit u : fleetToFormSpecialUnits)
		{
			if (u == null) continue;
			
			if (!unasignedFleet.getSpecialUnits().contains(u)) throw new RunningGameCommandException("Unasigned fleet does not have require special unit '"+u.toString()+"'");
		}
				
		Fleet newFleet = new Fleet(db, fleetName, playerLogin, productiveCelestialBody.getLocation().asRealLocation(), fleetToFormStarships, fleetToFormSpecialUnits, false);

		return new FormFleetCheckResult(productiveCelestialBody, newFleet);
	}

	public CommandCheckResult canDismantleFleet(String playerLogin, String fleetName)
	{
		try
		{
			checkDismantleFleet(playerLogin, fleetName);
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

	public CommandCheckResult canMakeProbes(String playerLogin, String planetName, String probeName, int quantity)
	{
		MakeProbesCheckResult check;
		try
		{
			check = checkMakeProbes(playerLogin, planetName, probeName, quantity);
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
			newProbes.add(new Probe(db, probeName + i, playerName, planet.getLocation().asRealLocation(), false));
		}

		return new MakeProbesCheckResult(planet, carbonCost, populationCost, newProbes);
	}

	public CommandCheckResult canMakeAntiProbeMissiles(String playerLogin, String planetName, String antiProbeMissileName, int quantity)
	{
		MakeAntiProbeMissilesCheckResult check;
		try
		{
			check = checkMakeAntiProbeMissiles(playerLogin, planetName, antiProbeMissileName, quantity);
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
			newAntiProbeMissiles.add(new AntiProbeMissile(db, antiProbeMissileName + i, playerName, planet.getLocation().asRealLocation(), false));
		}

		return new MakeAntiProbeMissilesCheckResult(planet, carbonCost, populationCost, newAntiProbeMissiles);
	}

	public CommandCheckResult canMakeStarships(String playerLogin, String planetName, Map<org.axan.sep.common.StarshipTemplate, Integer> starshipsToMake)
	{
		MakeStarshipsCheckResult check;
		try
		{
			check = checkMakeStarships(playerLogin, planetName, starshipsToMake);
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

	public CommandCheckResult canChangeDiplomacy(String playerLogin, Map<String,PlayerPolicies> newPolicies)
	{
		try
		{
			checkChangeDiplomacy(playerLogin, newPolicies);
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
	
	public CommandCheckResult canAttackEnemiesFleet(String playerLogin, String celestialBodyName)
	{
		try
		{
			checkAttackEnemiesFleet(playerLogin, celestialBodyName);
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
		ProductiveCelestialBody productiveCelestialBody = db.getCelestialBody(celestialBodyName, ProductiveCelestialBody.class, playerName);
		if (productiveCelestialBody == null) throw new RunningGameCommandException("Celestial body '"+celestialBodyName+"' is not a productive celestial body.");
		
		return new AttackEnemiesFleetCheckResult(productiveCelestialBody);			
	}
	
	/////
	
	public CommandCheckResult canBuildSpaceRoad(String playerLogin, String productiveCelestialBodyNameA, String productiveCelestialBodyNameB)
	{
		BuildSpaceRoadCheckResult check;
		try
		{
			check = checkBuildSpaceRoad(playerLogin, productiveCelestialBodyNameA, productiveCelestialBodyNameB);
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
		
		for(RealLocation pathStep : SEPUtils.getAllPathLoc(source.getLocation().asRealLocation(), destination.getLocation().asRealLocation()))
		{
			if (db.getArea(pathStep.asLocation()) != null && db.getArea(pathStep.asLocation()).isSun()) throw new RunningGameCommandException("Impossible path : " + source.getLocation() + " to " + destination.getLocation() + ", cannot travel the sun.");
		}
		
		double distance = SEPUtils.getDistance(source.getLocation().asRealLocation(), destination.getLocation().asRealLocation());
		int price = (int) (db.getGameConfig().getSpaceRoadPricePerArea() * distance);
		
		if (!playerLogin.equals(source.getOwnerName()) || sourceSpaceCounter == null || sourceSpaceCounter.getAvailableRoadsBuilder() <= 0 || source.getCarbon() < price)
		{
			throw new RunningGameCommandException("None of the space road end can pay nor have free builder.");
		}
		
		SpaceRoadDeliverer deliverer = new SpaceRoadDeliverer(db, sourceName+" to "+destinationName+" space road deliverer", playerLogin, source.getLocation().asRealLocation(), sourceName, destinationName);
		
		return new BuildSpaceRoadCheckResult(source, price, deliverer, destination.getLocation());			
	}
	
	public CommandCheckResult canDemolishSpaceRoad(String playerLogin, String sourceName, String destinationName)
	{
		try
		{
			checkDemolishSpaceRoad(playerLogin, sourceName, destinationName);
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
	
	public CommandCheckResult canModifyCarbonOrder(String playerLogin, String originCelestialBodyName, Stack<CarbonOrder> nextCarbonOrders)
	{
		try
		{
			checkModifyCarbonOrder(playerLogin, originCelestialBodyName, nextCarbonOrders);
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
			
			for(RealLocation pathStep : SEPUtils.getAllPathLoc(source.getLocation().asRealLocation(), destination.getLocation().asRealLocation()))
			{
				if (db.getArea(pathStep.asLocation()) != null && db.getArea(pathStep.asLocation()).isSun()) throw new RunningGameCommandException("Impossible path : " + source.getLocation() + " to " + destination.getLocation() + ", cannot travel the sun.");
			}
		}
		
		return new ModifyCarbonOrderCheckResult(sourceSpaceCounter);
	}
	
	////

	
	
	
}