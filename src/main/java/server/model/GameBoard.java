package server.model;

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
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.axan.eplib.utils.Basic;

import server.SEPServer;
import server.model.Area.AreaIllegalDefinitionException;
import server.model.DataBase.DataBaseError;
import server.model.DataBase.genericResultSet;
import server.model.ProductiveCelestialBody.CelestialBodyBuildException;
import server.model.SpaceCounter.SpaceRoad;
import server.model.SpaceCounter.SpaceRoadDeliverer;

import common.GameConfig;
import common.GovernmentStarship;
import common.ISpecialUnit;
import common.Player;
import common.SEPUtils;
import common.TravellingLogEntryUnitSeen;
import common.Protocol.ServerRunningGame.RunningGameCommandException;

import common.SEPUtils.RealLocation;
import common.SEPUtils.Location;

public class GameBoard implements Serializable
{
	private static final long			serialVersionUID	= 1L;

	private static final Logger			log					= SEPServer.log;

	private static final Random			rnd					= new Random();
	
	private final DataBase				db;

	private GameBoard(Hashtable<String, common.Player> players, common.GameConfig config, int date, Hashtable<Location, Area> areas, Hashtable<ICelestialBody.Key, ICelestialBody> celestialBodies, Hashtable<String, Hashtable<IMarker.Key, IMarker>> playersMarkers, RealLocation sunLocation, Hashtable<Unit.Key, Unit> units, Map<String, Diplomacy> playersPolicies)
	{
		this.db = new DataBase(players, config, date, areas, celestialBodies, playersMarkers, sunLocation, units, playersPolicies);
	}

	/**
	 * Full new game constructor.
	 * 
	 * @param playerList
	 * @param gameConfig
	 */
	public GameBoard(Set<common.Player> players, common.GameConfig config, int date)
	{
		this(new Hashtable<String, common.Player>(),  config, date, new Hashtable<Location, Area>(players.size()*2), new Hashtable<ICelestialBody.Key, ICelestialBody>(players.size()*2), new Hashtable<String, Hashtable<IMarker.Key, IMarker>>(players.size()*2), new RealLocation(Double.valueOf(config.getDimX()) / 2.0, Double.valueOf(config.getDimY()) / 2.0, Double.valueOf(config.getDimZ()) / 2.0), new Hashtable<Unit.Key, Unit>(), new Hashtable<String, Diplomacy>());

		// Make the sun
		RealLocation sunLocation = db.getSunLocation();
		
		for(int x = -Math.min(config.getSunRadius(), config.getDimX() / 2); x <= Math.min(config.getSunRadius(), config.getDimX() / 2); ++x)
			for(int y = -Math.min(config.getSunRadius(), config.getDimY() / 2); y <= Math.min(config.getSunRadius(), config.getDimY() / 2); ++y)
				for(int z = -Math.min(config.getSunRadius(), config.getDimZ() / 2); z <= Math.min(config.getSunRadius(), config.getDimZ() / 2); ++z)
				{
					RealLocation parsedLoc = new RealLocation(sunLocation.x + x, sunLocation.y + y, sunLocation.z + z);
					if (common.SEPUtils.getDistance(parsedLoc, sunLocation) <= config.getSunRadius())
					{
						db.getCreateArea(parsedLoc.asLocation()).setSunFlag(true);
					}
				}				
		
		// Add the players starting planets.
		Set<Location> playersPlanetLocations = new HashSet<Location>();
		
		for(common.Player player : players)
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
					Stack<RealLocation> path = common.SEPUtils.getAllPathLoc(planetLocation.asRealLocation(), l.asRealLocation());
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

			Class<? extends common.ICelestialBody> celestialBodyType = Basic.getKeyFromRandomTable(config.getNeutralCelestialBodiesGenerationTable());

			Class<? extends ICelestialBody> serverCelestialBodyType;
			String nextName = generateCelestialBodyName();
			try
			{
				serverCelestialBodyType = Class.forName("server.model." + celestialBodyType.getSimpleName()).asSubclass(ICelestialBody.class);
				Constructor<? extends ICelestialBody> ctor = serverCelestialBodyType.getConstructor(DataBase.class, String.class, Location.class, common.GameConfig.class);
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
	public common.PlayerGameBoard getPlayerGameBoard(String playerLogin)
	{
		log.log(Level.INFO, "getGameBoard(" + playerLogin + ")");
		
		GameConfig config = db.getGameConfig();
		
		common.Area[][][] playerUniverseView = new common.Area[config.getDimX()][config.getDimY()][config.getDimZ()];

		Set<Probe> playerProbes = db.getUnits(Probe.class, playerLogin);

		Map<String, common.Diplomacy> playersPoliciesView = new Hashtable<String, common.Diplomacy>();
		
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
					if (!isVisible && celestialBodyOwnerName != null && celestialBodyOwnerName.compareTo(playerLogin) == 0)
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
						if (common.SEPUtils.getDistance(location.asRealLocation(), p.getRealLocation()) > config.getProbeScope()) continue;

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
						for(Fleet fleet : db.getUnits(location, Fleet.class, null))
						{
							if (fleet.isGovernmentFleet())
							{
								playersPoliciesView.put(fleet.getOwnerName(), db.getPlayerPolicies(fleet.getOwnerName()).getPlayerView(db.getDate(), playerLogin, isVisible));
							}
						}
						
						playerUniverseView[x][y][z] = db.getCreateArea(location).getPlayerView(db.getDate(), playerLogin, isVisible);
					}
				}
		
		for(String playerName : db.getPlayersKeySet())
		{
			if (playersPoliciesView.containsKey(playerName)) continue;
			playersPoliciesView.put(playerName, db.getPlayerPolicies(playerName).getPlayerView(db.getDate(), playerLogin, false));
		}
		
		return new common.PlayerGameBoard(playerUniverseView, db.getSunLocation(), db.getDate(), playersPoliciesView);
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

		Set<Unit> movingUnits = new HashSet<Unit>();
		double maxSpeed = Double.MIN_VALUE;

		for(Unit unit : db.getUnits())
		{
			if (unit.isMoving() || unit.startMove())
			{
				movingUnits.add(unit);
				maxSpeed = Math.max(maxSpeed, unit.getSpeed());
			}
		}

		// Unit moves

		Set<Unit> currentStepMovedUnits = new TreeSet<Unit>();
		Set<Probe> deployedProbes = db.getDeployedProbes();
		
		double step = 1 / maxSpeed;
		for(float currentStep = 0; currentStep <= 1; currentStep += step)
		{
			currentStepMovedUnits.clear();
			
			for(Unit u : movingUnits)
			{
				double distance = SEPUtils.getDistance(u.getSourceLocation(), u.getDestinationLocation());
				double progressInOneStep = (distance != 0 ? (u.getSpeed() / distance) : 1);
				u.setTravellingProgress(Math.min(1, u.getTravellingProgress() + progressInOneStep));
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
		
		Set<AntiProbeMissile> explodingAntiProbeMissiles = new TreeSet<AntiProbeMissile>();
		
		for(Unit u : movingUnits)
		{
			RealLocation endTurnLocation = u.getRealLocation();
			
			IMarker.Key key = new IMarker.Key("own unit("+u.getName()+") travelling marker", UnitMarker.class, u.getOwnerName());
			
			db.removeMarker(key);
			
			if (endTurnLocation.asLocation().equals(u.getDestinationLocation().asLocation()))
			{
				u.endMove();
				
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
					generatedCarbon = Math.min(db.getGameConfig().getMaxNaturalCarbon() - productiveCelestialBody.getCarbon(), generatedCarbon);
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
		
		// Conflicts
		for(ProductiveCelestialBody productiveCelestialBody : db.getCelestialBodies(ProductiveCelestialBody.class))
		{
			if (!productiveCelestialBody.getConflictInitiators().isEmpty())
			{
				resolveConflict(productiveCelestialBody);
			}
		}

		db.incDate();
	}

	public void resolveConflict(ProductiveCelestialBody productiveCelestialBody)
	{
		// Initiate conflicts table
		Map<String, Map<String, Boolean>> conflicts = new Hashtable<String, Map<String,Boolean>>();
		for(String p1 : db.getPlayersKeySet())
		{
			Map<String, Boolean> playerConflicts = new Hashtable<String, Boolean>();
			for(String p2 : db.getPlayersKeySet())
			{
				if (p1.compareTo(p2) == 0) continue;
				
				boolean p1Policy = false;
				
				if (p1.compareTo(productiveCelestialBody.getOwnerName()) == 0)
				{
					p1Policy = !db.getPlayerPolicies(p1).getPolicies(p2).isAllowedToLandFleetInHomeTerritory();					
				}
				else
				{
					p1Policy = productiveCelestialBody.getConflictInitiators().contains(p1) ? true : db.getPlayerPolicies(p1).getPolicies(p2).isAlwaysEngagedInConflictOnStrangerTerritory();
				}
				
				boolean p2Policy = conflicts.containsKey(p2) ? conflicts.get(p2).get(p1) : false;
				
				boolean finalPolicy = p1Policy || p2Policy;
				playerConflicts.put(p2, finalPolicy);
				if (conflicts.containsKey(p2)) conflicts.get(p2).put(p1, finalPolicy);
			}
			
			conflicts.put(p1, playerConflicts);
		}
		
		// List merged (unassigned fleets + fleets) forces for each players
		Map<String, Fleet> fleets = new Hashtable<String, Fleet>();		
		
		Location location = productiveCelestialBody.getLocation();
		
		for(String p : db.getPlayersKeySet())
		{
			if (productiveCelestialBody.getUnasignedFleet(p) != null)
			{
				productiveCelestialBody.removeFromUnasignedFleet(p, productiveCelestialBody.getUnasignedFleetStarships(p), productiveCelestialBody.getUnasignedFleetSpecialUnits(p));				
				
				fleets.put(p, new Fleet(db, p+" forces in conflict on "+productiveCelestialBody.getName()+" at turn "+db.getDate(), p, location.asRealLocation(), productiveCelestialBody.getUnasignedFleetStarships(p), productiveCelestialBody.getUnasignedFleetSpecialUnits(p), false));
			}
		}
		
		Set<Unit> unitsToRemove = new HashSet<Unit>();
		for(Unit u : db.getUnits(location))
		{
			if (!Fleet.class.isInstance(u)) continue;
			Fleet f = Fleet.class.cast(u);
			
			if (!fleets.containsKey(f.getOwnerName()))
			{
				fleets.put(f.getOwnerName(), new Fleet(db, f.getOwnerName()+" forces in conflict on "+productiveCelestialBody.getName()+" at turn "+db.getDate(), f.getOwnerName(), location.asRealLocation(), f.getStarships(), f.getSpecialUnits(), false));
			}
			else
			{
				fleets.get(f.getOwnerName()).merge(f.getStarships(), f.getSpecialUnits());
			}
			
			unitsToRemove.add(f);			
		}
		
		for(Unit u : unitsToRemove)
		{
			db.removeUnit(u.getKey());
		}
		
		// TODO : fake resolution, implement a true one
		
		Stack<String> winners = new Stack<String>();
		Random rnd = new Random();
		Map<String, Fleet> tempFleets = new Hashtable<String, Fleet>(fleets);
		
		do
		{			
			int winnerIndex = rnd.nextInt(tempFleets.size());
			Iterator<String> winnerIt = tempFleets.keySet().iterator();
			String winnerKey = winnerIt.next();
			for(int i=0; i < winnerIndex && winnerIt.hasNext(); ++i)
			{
				winnerKey = winnerIt.next();
			}
			
			Fleet winner = tempFleets.get(winnerKey);
			tempFleets.remove(winnerKey);
			winners.push(winnerKey);
			
			for(String p : db.getPlayersKeySet())
			{
				if (p.compareTo(winnerKey) == 0) continue; 
				if (conflicts.get(winnerKey).get(p))
				{
					tempFleets.remove(p);
					fleets.remove(p);
				}
			}
		}while(tempFleets.size() > 0);
		
		for(Map.Entry<String, Fleet> e : fleets.entrySet())
		{
			productiveCelestialBody.mergeToUnasignedFleet(e.getKey(), e.getValue().getStarships(), e.getValue().getSpecialUnits());
		}
		
		if (!winners.contains(productiveCelestialBody.getOwnerName()))
		{
			productiveCelestialBody.changeOwner(winners.firstElement());
		}
		
		productiveCelestialBody.endConflict();
	}
	
	////////////
	
	
	public void demolish(String playerLogin, String celestialBodyName, Class<? extends common.IBuilding> buildingType) throws RunningGameCommandException
	{
		DemolishCheckResult demolishCheckResult = checkDemolish(playerLogin, celestialBodyName, buildingType);
		demolishCheckResult.productiveCelestialBody.demolishBuilding(demolishCheckResult.existingBuilding);
	}

	public boolean canDemolish(String playerLogin, String celestialBodyName, Class<? extends common.IBuilding> buildingType)
	{
		try
		{
			checkDemolish(playerLogin, celestialBodyName, buildingType);
		}
		catch(Throwable t)
		{
			return false;
		}
		return true;
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

	public DemolishCheckResult checkDemolish(String playerName, String celestialBodyName, Class<? extends common.IBuilding> buildingType) throws RunningGameCommandException
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
		Set<common.ISpecialUnit> specialUnitsToMake = new HashSet<common.ISpecialUnit>();
		specialUnitsToMake.add(new common.GovernmentStarship(playerName+" government starship"));		
		embarkGovernmentCheckResult.planet.mergeToUnasignedFleet(playerName, null, specialUnitsToMake);
		embarkGovernmentCheckResult.planet.setCarbon(embarkGovernmentCheckResult.planet.getCarbon() - embarkGovernmentCheckResult.carbonCost);
		embarkGovernmentCheckResult.planet.setPopulation(embarkGovernmentCheckResult.planet.getPopulation() - embarkGovernmentCheckResult.populationCost);
	}

	public boolean canEmbarkGovernment(String playerLogin)
	{
		try
		{
			checkEmbarkGovernment(playerLogin);
		}
		catch(Throwable t)
		{
			return false;
		}
		return true;
	}

	private static class EmbarkGovernmentCheckResult
	{
		final Planet			planet;
		final int				carbonCost;
		final int				populationCost;
		final GovernmentModule	governmentModule;
		final StarshipPlant		starshipPlant;

		public EmbarkGovernmentCheckResult(Planet planet, GovernmentModule governmentModule, StarshipPlant starshipPlant, int carbonCost, int populationCost)
		{
			this.planet = planet;
			this.carbonCost = carbonCost;
			this.populationCost = populationCost;
			this.governmentModule = governmentModule;
			this.starshipPlant = starshipPlant;
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
		
		return new EmbarkGovernmentCheckResult(planet, governmentModule, starshipPlant, carbonCost, populationCost);
	}

	public boolean canFirePulsarMissile(String playerName, String celestialBodyName)
	{
		ProductiveCelestialBody productiveCelestialBody = db.getCelestialBody(celestialBodyName, ProductiveCelestialBody.class, playerName);
		if (productiveCelestialBody == null) throw new IllegalArgumentException("Celestial body '" + celestialBodyName + "' does not exist, or is not a productive one.");
		
		ABuilding building = productiveCelestialBody.getBuildingFromClientType(common.PulsarLauchingPad.class);

		// If no building of this type exist.
		if (building == null) return false;

		// Building type check		
		if (!PulsarLauchingPad.class.isInstance(building)) return false;

		PulsarLauchingPad pulsarLaunchingPad = PulsarLauchingPad.class.cast(building);
		if (pulsarLaunchingPad.getUnusedCount() <= 0) return false;

		return true;
	}

	public void settleGovernment(String playerLogin, String planetName) throws RunningGameCommandException
	{
		SettleGovernmentCheckResult settleGovernmentCheckResult = checkSettleGovernment(playerLogin, planetName);
		// TODO
	}
	
	public boolean canSettleGovernment(String playerLogin, String planetName)
	{
		try
		{
			checkSettleGovernment(playerLogin, planetName);
		}
		catch(Throwable t)
		{
			return false;
		}
		return true;
	}
	
	private static class SettleGovernmentCheckResult
	{
		final Planet	planet;
		final Fleet		governmentalFleet;

		public SettleGovernmentCheckResult(Planet planet, Fleet governmentalFleet)
		{
			this.planet = planet;
			this.governmentalFleet = governmentalFleet;			
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
		
		return new SettleGovernmentCheckResult(planet, governmentalFleet);
	}

	public void build(String playerLogin, String celestialBodyName, Class<? extends common.IBuilding> buildingType) throws CelestialBodyBuildException
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

	public boolean canBuild(String playerLogin, String celestialBodyName, Class<? extends common.IBuilding> buildingType)
	{
		try
		{
			checkBuild(playerLogin, celestialBodyName, buildingType);
		}
		catch(Throwable t)
		{
			return false;
		}
		return true;
	}

	private static class BuildCheckResult
	{
		final ProductiveCelestialBody	productiveCelestialBody;
		final ABuilding					existingBuilding;
		final ABuilding					newBuilding;
		final int						carbonCost;
		final int						populationCost;

		public BuildCheckResult(ProductiveCelestialBody productiveCelestialBody, ABuilding building, int carbonCost, int populationCost, ABuilding newBuilding)
		{
			this.productiveCelestialBody = productiveCelestialBody;
			this.existingBuilding = building;
			this.carbonCost = carbonCost;
			this.populationCost = populationCost;
			this.newBuilding = newBuilding;
		}
	}

	private BuildCheckResult checkBuild(String playerName, String celestialBodyName, Class<? extends common.IBuilding> buildingType) throws CelestialBodyBuildException
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

		return new BuildCheckResult(productiveCelestialBody, building, carbonCost, populationCost, newBuilding);
	}

	public boolean canFireAntiProbeMissile(String playerLogin, String antiProbeMissileName, String targetOwnerName, String targetProbeName)
	{
		try
		{
			checkFireAntiProbeMissile(playerLogin, antiProbeMissileName, targetOwnerName, targetProbeName);
		}
		catch(Throwable t)
		{
			return false;
		}
		return true;
	}

	public void fireAntiProbeMissile(String playerLogin, String antiProbeMissileName, String targetOwnerName, String targetProbeName) throws RunningGameCommandException
	{
		FireAntiProbeMissileCheckResult fireAntiProbeMissileCheckResult = checkFireAntiProbeMissile(playerLogin, antiProbeMissileName, targetOwnerName, targetProbeName);
		fireAntiProbeMissileCheckResult.antiProbeMissile.fire(targetOwnerName, targetProbeName, fireAntiProbeMissileCheckResult.source, fireAntiProbeMissileCheckResult.destination);
	}

	private static class FireAntiProbeMissileCheckResult
	{
		final AntiProbeMissile	antiProbeMissile;
		final String			targetProbeName;
		final RealLocation		source;
		final RealLocation		destination;

		public FireAntiProbeMissileCheckResult(AntiProbeMissile antiProbeMissile, String targetProbeName, RealLocation source, RealLocation destination)
		{
			this.antiProbeMissile = antiProbeMissile;
			this.targetProbeName = targetProbeName;
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
			if (um == null || um.getUnit() == null || !common.Probe.class.isInstance(um.getUnit()))
			{
				throw new RunningGameCommandException("AntiProbeMissile '" + antiProbeMissileName + "' target '" + targetProbeName + "' does not exist.");
			}

			common.Probe probe = common.Probe.class.cast(um.getUnit());

			if (probe.isMoving()) throw new RunningGameCommandException("AntiProbeMissile '" + antiProbeMissileName + "' cannot be fired on moving target '" + targetProbeName + "'");

			destination = probe.getCurrentLocation();
		}

		if (destination == null) throw new RunningGameCommandException("AntiProbeMissile '" + antiProbeMissileName + "' target '" + targetProbeName + "' does not exist.");

		return new FireAntiProbeMissileCheckResult(antiProbeMissile, targetProbeName, antiProbeMissile.getRealLocation(), destination);
	}

	public boolean canLaunchProbe(String playerLogin, String probeName, RealLocation destination)
	{
		try
		{
			checkLaunchProbe(playerLogin, probeName, destination);
		}
		catch(Throwable t)
		{
			return false;
		}
		return true;
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

	public boolean canMoveFleet(String playerLogin, String fleetName, Stack<common.Fleet.Move> checkpoints)
	{
		try
		{
			checkMoveFleet(playerLogin, fleetName, checkpoints);
		}
		catch(Throwable t)
		{
			return false;
		}
		return true;
	}

	public void moveFleet(String playerLogin, String fleetName, Stack<common.Fleet.Move> checkpoints) throws RunningGameCommandException
	{
		MoveFleetCheckResult moveFleetCheckResult = checkMoveFleet(playerLogin, fleetName, checkpoints);
		moveFleetCheckResult.fleet.updateMoveOrder(moveFleetCheckResult.locatedCheckpoints);
	}

	private static class MoveFleetCheckResult
	{
		final Fleet						fleet;
		final Stack<common.Fleet.Move>	locatedCheckpoints;

		public MoveFleetCheckResult(Fleet fleet, Stack<common.Fleet.Move> locatedCheckpoints)
		{
			this.fleet = fleet;
			this.locatedCheckpoints = locatedCheckpoints;
		}
	}

	public MoveFleetCheckResult checkMoveFleet(String playerLogin, String fleetName, Stack<common.Fleet.Move> checkpoints) throws RunningGameCommandException
	{
		Fleet fleet = db.getUnit(Fleet.class, playerLogin, fleetName);
		if (fleet == null) throw new RunningGameCommandException("Fleet '" + fleetName + "' does not exist.");

		// Check paths
		Stack<common.Fleet.Move> locatedCheckpoints = new Stack<common.Fleet.Move>();

		RealLocation currentStart = (fleet.isMoving() ? fleet.getDestinationLocation() : fleet.getRealLocation());
		for(common.Fleet.Move move : checkpoints)
		{
			Location destinationLocation = db.getCelestialBody(move.getDestinationName()).getLocation();
			if (destinationLocation == null) throw new RunningGameCommandException("Unexpected error : checkpoint destination '" + move.getDestinationName() + "' not found.");

			for(RealLocation pathStep : SEPUtils.getAllPathLoc(currentStart, destinationLocation.asRealLocation()))
			{
				if (db.getArea(pathStep.asLocation()) != null && db.getArea(pathStep.asLocation()).isSun()) throw new RunningGameCommandException("Impossible path : " + currentStart + " to " + destinationLocation + ", cannot travel the sun.");
			}

			currentStart = destinationLocation.asRealLocation();

			locatedCheckpoints.add(new common.Fleet.Move(move, destinationLocation.asRealLocation()));
		}

		return new MoveFleetCheckResult(fleet, locatedCheckpoints);
	}

	public boolean canFormFleet(String playerLogin, String planetName, String fleetName, Map<common.StarshipTemplate, Integer> fleetToFormStarships, Set<common.ISpecialUnit> fleetToFormSpecialUnits)
	{
		try
		{
			checkFormFleet(playerLogin, planetName, fleetName, fleetToFormStarships, fleetToFormSpecialUnits);
		}
		catch(Throwable t)
		{
			return false;
		}
		return true;
	}

	public void formFleet(String playerLogin, String planetName, String fleetName, Map<common.StarshipTemplate, Integer> fleetToFormStarships, Set<common.ISpecialUnit> fleetToFormSpecialUnits) throws RunningGameCommandException
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

	private FormFleetCheckResult checkFormFleet(String playerLogin, String productiveCelestialBodyName, String fleetName, Map<common.StarshipTemplate, Integer> fleetToFormStarships, Set<common.ISpecialUnit> fleetToFormSpecialUnits) throws RunningGameCommandException
	{
		ProductiveCelestialBody productiveCelestialBody = db.getCelestialBody(productiveCelestialBodyName, ProductiveCelestialBody.class);
		if (productiveCelestialBody == null) throw new RunningGameCommandException("Celestial body '" + productiveCelestialBodyName + "' does not exist.");

		Fleet unasignedFleet = productiveCelestialBody.getUnasignedFleet(playerLogin);
		if (unasignedFleet == null) throw new RunningGameCommandException("No available unasigned fleet on celestial body '" + productiveCelestialBodyName + "'");

		Fleet fleet = db.getUnit(Fleet.class, playerLogin, fleetName);
		if (fleet != null) throw new RunningGameCommandException("Fleet named '" + fleetName + "' already exist.");

		// Starship availability check		
		for(Entry<common.StarshipTemplate, Integer> e : fleetToFormStarships.entrySet())
		{
			if (e.getValue() <= 0) continue;

			int qt = e.getValue();
			if (!unasignedFleet.getStarships().containsKey(e.getKey())) throw new RunningGameCommandException("Unasigned fleet does not have required starship type '" + e.getKey().getName() + "'");
			if (unasignedFleet.getStarships().get(e.getKey()) < qt) throw new RunningGameCommandException("Unasigned flee does not have enough starship type '" + e.getKey() + "'");
		}

		// Special units availability check
		for(common.ISpecialUnit u : fleetToFormSpecialUnits)
		{
			if (u == null) continue;
			
			if (!unasignedFleet.getSpecialUnits().contains(u)) throw new RunningGameCommandException("Unasigned fleet does not have require special unit '"+u.toString()+"'");
		}
				
		Fleet newFleet = new Fleet(db, fleetName, playerLogin, productiveCelestialBody.getLocation().asRealLocation(), fleetToFormStarships, fleetToFormSpecialUnits, false);

		return new FormFleetCheckResult(productiveCelestialBody, newFleet);
	}

	public boolean canDismantleFleet(String playerLogin, String fleetName)
	{
		try
		{
			checkDismantleFleet(playerLogin, fleetName);
		}
		catch(Throwable t)
		{
			return false;
		}
		return true;
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

	public boolean canMakeProbes(String playerLogin, String planetName, String probeName, int quantity)
	{
		try
		{
			checkMakeProbes(playerLogin, planetName, probeName, quantity);
		}
		catch(Throwable t)
		{
			return false;
		}
		return true;
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
		final StarshipPlant	starshipPlant;
		final Set<Probe>	newProbes;

		public MakeProbesCheckResult(Planet planet, int carbonCost, int populationCost, StarshipPlant starshipPlant, Set<Probe> newProbes)
		{
			this.planet = planet;
			this.carbonCost = carbonCost;
			this.populationCost = populationCost;
			this.starshipPlant = starshipPlant;
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
		int carbonCost = common.Probe.PRICE_CARBON * quantity;
		int populationCost = common.Probe.PRICE_POPULATION * quantity;

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

		return new MakeProbesCheckResult(planet, carbonCost, populationCost, starshipPlant, newProbes);
	}

	public boolean canMakeAntiProbeMissiles(String playerLogin, String planetName, String antiProbeMissileName, int quantity)
	{
		try
		{
			checkMakeAntiProbeMissiles(playerLogin, planetName, antiProbeMissileName, quantity);
		}
		catch(Throwable t)
		{
			return false;
		}
		return true;
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
		final StarshipPlant			starshipPlant;
		final Set<AntiProbeMissile>	newAntiProbeMissiles;

		public MakeAntiProbeMissilesCheckResult(Planet planet, int carbonCost, int populationCost, StarshipPlant starshipPlant, Set<AntiProbeMissile> newAntiProbeMissiles)
		{
			this.planet = planet;
			this.carbonCost = carbonCost;
			this.populationCost = populationCost;
			this.starshipPlant = starshipPlant;
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
		int carbonCost = common.AntiProbeMissile.PRICE_CARBON * quantity;
		int populationCost = common.AntiProbeMissile.PRICE_POPULATION * quantity;

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

		return new MakeAntiProbeMissilesCheckResult(planet, carbonCost, populationCost, starshipPlant, newAntiProbeMissiles);
	}

	public boolean canMakeStarships(String playerLogin, String planetName, Map<common.StarshipTemplate, Integer> starshipsToMake)
	{
		try
		{
			checkMakeStarships(playerLogin, planetName, starshipsToMake);
		}
		catch(Throwable t)
		{
			return false;
		}
		return true;
	}

	public void makeStarships(String playerLogin, String planetName, Map<common.StarshipTemplate, Integer> starshipsToMake) throws RunningGameCommandException
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
		final StarshipPlant	starshipPlant;

		public MakeStarshipsCheckResult(Planet planet, int carbonCost, int populationCost, StarshipPlant starshipPlant)
		{
			this.planet = planet;
			this.carbonCost = carbonCost;
			this.populationCost = populationCost;
			this.starshipPlant = starshipPlant;
		}
	}

	private MakeStarshipsCheckResult checkMakeStarships(String playerName, String planetName, Map<common.StarshipTemplate, Integer> starshipsToMake) throws RunningGameCommandException
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

		for(Entry<common.StarshipTemplate, Integer> e : starshipsToMake.entrySet())
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

		return new MakeStarshipsCheckResult(planet, carbonCost, populationCost, starshipPlant);
	}

	public boolean canChangeDiplomacy(String playerLogin, common.Diplomacy newDiplomacy)
	{
		try
		{
			checkChangeDiplomacy(playerLogin, newDiplomacy);
		}
		catch(Throwable t)
		{
			return false;
		}
		return true;
	}

	public void changeDiplomacy(String playerLogin, common.Diplomacy newDiplomacy) throws RunningGameCommandException
	{
		ChangeDiplomacyCheckResult changeDiplomacyCheckResult = checkChangeDiplomacy(playerLogin, newDiplomacy);
		
		db.getPlayerPolicies(playerLogin).update(newDiplomacy);
	}

	private static class ChangeDiplomacyCheckResult
	{
		public ChangeDiplomacyCheckResult()
		{
		}
	}

	private ChangeDiplomacyCheckResult checkChangeDiplomacy(String playerLogin, common.Diplomacy newDiplomacy) throws RunningGameCommandException
	{
		if (newDiplomacy.getPolicies(playerLogin) != null) throw new RunningGameCommandException("Cannot have a diplomacy toward ourselves.");
		return new ChangeDiplomacyCheckResult();
	}
	
	public boolean canAttackEnemiesFleet(String playerLogin, String celestialBodyName)
	{
		try
		{
			checkAttackEnemiesFleet(playerLogin, celestialBodyName);
		}
		catch(Throwable t)
		{
			return false;
		}
		return true;
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
	
	public boolean canBuildSpaceRoad(String playerLogin, String productiveCelestialBodyNameA, String productiveCelestialBodyNameB)
	{
		try
		{
			checkBuildSpaceRoad(playerLogin, productiveCelestialBodyNameA, productiveCelestialBodyNameB);
		}
		catch(Throwable t)
		{
			return false;
		}
		return true;
	}

	public void buildSpaceRoad(String playerLogin, String productiveCelestialBodyNameA, String productiveCelestialBodyNameB) throws RunningGameCommandException
	{
		BuildSpaceRoadCheckResult buildSpaceRoadCheckResult = checkBuildSpaceRoad(playerLogin, productiveCelestialBodyNameA, productiveCelestialBodyNameB);
			
		db.insertUnit(buildSpaceRoadCheckResult.deliverer);
		buildSpaceRoadCheckResult.deliverer.launch(buildSpaceRoadCheckResult.sourceLocation.asRealLocation(), buildSpaceRoadCheckResult.destinationLocation.asRealLocation());		
		buildSpaceRoadCheckResult.payer.setCarbon(buildSpaceRoadCheckResult.payer.getCarbon() - buildSpaceRoadCheckResult.price);
	}

	private static class BuildSpaceRoadCheckResult
	{
		final SpaceRoadDeliverer deliverer;
		final Location sourceLocation;
		final Location destinationLocation;
		final ProductiveCelestialBody payer;
		final int price;
		
		public BuildSpaceRoadCheckResult(ProductiveCelestialBody payer, int price, SpaceRoadDeliverer deliverer, Location sourceLocation, Location destinationLocation)
		{
			this.payer = payer;
			this.price = price;
			this.deliverer = deliverer;
			this.sourceLocation = sourceLocation;
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
		
		double distance = SEPUtils.getDistance(source.getLocation().asRealLocation(), destination.getLocation().asRealLocation());
		int price = (int) (db.getGameConfig().getSpaceRoadPricePerArea() * distance);
		
		if (source.getOwnerName() == null || source.getOwnerName().compareTo(playerLogin) != 0 || sourceSpaceCounter == null || sourceSpaceCounter.getAvailableRoadsBuilder() <= 0 || source.getCarbon() < price)
		{
			throw new RunningGameCommandException("None of the space road end can pay nor have free builder.");
		}
		
		SpaceRoadDeliverer deliverer = new SpaceRoadDeliverer(db, sourceName+" to "+destinationName+" space road deliverer", playerLogin, source.getLocation().asRealLocation(), sourceName, destinationName);
		
		return new BuildSpaceRoadCheckResult(source, price, deliverer, source.getLocation(), destination.getLocation());			
	}
	
	public boolean canDemolishSpaceRoad(String playerLogin, String sourceName, String destinationName)
	{
		try
		{
			checkDemolishSpaceRoad(playerLogin, sourceName, destinationName);
		}
		catch(Throwable t)
		{
			return false;
		}
		return true;
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
						
		if (!sourceSpaceCounter.hasSpaceRoadTo(destinationName) && !sourceSpaceCounter.hasSpaceRoadLinkedFrom(sourceName))
		{
			throw new RunningGameCommandException("'"+sourceName+"' has no space road link with '"+destinationName+"'");
		}
		
		return new DemolishSpaceRoadCheckResult(sourceSpaceCounter);			
	}
	
	////

	
	
	
}
