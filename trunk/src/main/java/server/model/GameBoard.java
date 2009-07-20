package server.model;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.axan.eplib.utils.Basic;

import server.SEPServer;
import server.model.Area.AreaIllegalDefinitionException;
import server.model.ProductiveCelestialBody.CelestialBodyBuildException;

import common.GovernmentStarship;
import common.IStarship;
import common.Player;
import common.SEPUtils;
import common.TravellingLogEntryUnitSeen;
import common.UnitMarker;
import common.Protocol.ServerRunningGame.RunningGameCommandException;

import common.SEPUtils.RealLocation;

public class GameBoard implements Serializable
{
	private static final long			serialVersionUID	= 1L;

	private static final Logger			log					= SEPServer.log;

	private static final Random			rnd					= new Random();

	private final Set<common.Player>	players;
	
	private final Map<String, Diplomacy>	playersPolicies;

	private final Area[][][]			universe;

	private final RealLocation			sunLocation;							// Sun center location : [0] x; [1] y; [2] z. Sun is always fill 9 area.

	private final common.GameConfig		config;

	private int							date;

	private GameBoard(Set<common.Player> players, common.GameConfig config, int date, Area[][][] universe, RealLocation sunLocation, Map<String, Diplomacy> playersPolicies)
	{
		this.players = players;
		this.config = config;
		this.date = date;
		this.universe = universe;
		this.sunLocation = sunLocation;
		this.playersPolicies = playersPolicies;
	}

	/**
	 * Full new game constructor.
	 * 
	 * @param playerList
	 * @param gameConfig
	 */
	public GameBoard(Set<common.Player> players, common.GameConfig config, int date)
	{
		this.config = config;
		this.players = players;
		this.date = date;

		// Create a blank universe.
		universe = new Area[config.getDimX()][config.getDimY()][config.getDimZ()];

		// Make the sun
		sunLocation = new RealLocation(Double.valueOf(config.getDimX()) / 2.0, Double.valueOf(config.getDimY()) / 2.0, Double.valueOf(config.getDimZ()) / 2.0);

		for(int x = -Math.min(config.getSunRadius(), config.getDimX() / 2); x <= Math.min(config.getSunRadius(), config.getDimX() / 2); ++x)
			for(int y = -Math.min(config.getSunRadius(), config.getDimY() / 2); y <= Math.min(config.getSunRadius(), config.getDimY() / 2); ++y)
				for(int z = -Math.min(config.getSunRadius(), config.getDimZ() / 2); z <= Math.min(config.getSunRadius(), config.getDimZ() / 2); ++z)
				{
					RealLocation parsedLoc = new RealLocation(sunLocation.x + x, sunLocation.y + y, sunLocation.z + z);
					if (common.SEPUtils.getDistance(parsedLoc, sunLocation) <= config.getSunRadius())
					{
						getArea(parsedLoc).setSunFlag(true);
					}
				}

		// Add the players starting planets.
		Set<RealLocation> playersPlanetLocations = new HashSet<RealLocation>();
		
		this.playersPolicies = new Hashtable<String, Diplomacy>();

		for(common.Player player : players)
		{
			this.playersPolicies.put(player.getName(), new Diplomacy(player, players));
			
			// Found a location to pop the planet.
			RealLocation planetLocation;
			boolean locationOk;
			do
			{
				locationOk = false;
				planetLocation = new RealLocation(rnd.nextInt(config.getDimX()) + 0.5, rnd.nextInt(config.getDimY()) + 0.5, rnd.nextInt(config.getDimZ()) + 0.5);

				if (getNullArea(planetLocation) != null && !getNullArea(planetLocation).isEmpty()) continue;

				locationOk = true;
				for(RealLocation l : playersPlanetLocations)
				{
					Stack<RealLocation> path = common.SEPUtils.getAllPathLoc(planetLocation, l);
					for(RealLocation pl : path)
					{
						Area a = getNullArea(pl);
						if (a != null && a.isSun())
						{
							locationOk = false;
							break;
						}
					}

					if (!locationOk) break;
				}
			} while(!locationOk);

			Planet planet = Planet.newStartingPlanet(generateCelestialBodyName(), player, config);
			try
			{
				getArea(planetLocation).setCelestialBody(planet);
				playersPlanetLocations.add(planetLocation);
			}
			catch(AreaIllegalDefinitionException e)
			{
				throw new Error(e);
			}						
		}

		// Add neutral celestial bodies
		for(int i = 0; i < config.getNeutralCelestialBodiesCount(); ++i)
		{
			// Found a location to pop the celestial body
			RealLocation celestialBodyLocation;
			do
			{
				celestialBodyLocation = new RealLocation(rnd.nextInt(config.getDimX()) + 0.5, rnd.nextInt(config.getDimY()) + 0.5, rnd.nextInt(config.getDimZ()) + 0.5);
			} while(getNullArea(celestialBodyLocation) != null && !getNullArea(celestialBodyLocation).isEmpty());

			Class<? extends common.ICelestialBody> celestialBodyType = Basic.getKeyFromRandomTable(config.getNeutralCelestialBodiesGenerationTable());

			Class<? extends ICelestialBody> serverCelestialBodyType;
			String nextName = generateCelestialBodyName();
			try
			{
				serverCelestialBodyType = Class.forName("server.model." + celestialBodyType.getSimpleName()).asSubclass(ICelestialBody.class);
				Constructor<? extends ICelestialBody> ctor = serverCelestialBodyType.getConstructor(String.class, common.GameConfig.class);
				ICelestialBody celestialBody = ctor.newInstance(nextName, config);
				getArea(celestialBodyLocation).setCelestialBody(celestialBody);
			}
			catch(Exception e)
			{
				throw new Error("Cannot create celestial body type " + celestialBodyType.getSimpleName() + " (not implemented server side ?)", e);
			}
		}
	}

	private Player getPlayer(String playerLogin)
	{
		for(Player p : players)
		{
			if (p.isNamed(playerLogin)) return p;
		}
		return null;
	}

	private Area getNullArea(RealLocation location)
	{
		return universe[(int) location.x][(int) location.y][(int) location.z];
	}

	private Area getArea(RealLocation location)
	{
		if (universe[(int) location.x][(int) location.y][(int) location.z] == null)
		{
			universe[(int) location.x][(int) location.y][(int) location.z] = new Area();
		}
		return universe[(int) location.x][(int) location.y][(int) location.z];
	}

	public int getDate()
	{
		return date;
	}

	/**
	 * @param playerLogin
	 */
	public common.PlayerGameBoard getPlayerGameBoard(String playerLogin)
	{
		log.log(Level.INFO, "getGameBoard(" + playerLogin + ")");
		common.Area[][][] playerUniverseView = new common.Area[config.getDimX()][config.getDimY()][config.getDimZ()];

		Set<Probe> playerProbes = getUnits(Probe.class, playerLogin);

		Map<String, common.Diplomacy> playersPoliciesView = new Hashtable<String, common.Diplomacy>();
		
		boolean isVisible = false;

		for(int x = 0; x < config.getDimX(); ++x)
			for(int y = 0; y < config.getDimY(); ++y)
				for(int z = 0; z < config.getDimZ(); ++z)
				{
					Area area = universe[x][y][z];
					RealLocation location = new RealLocation(x + 0.5, y + 0.5, z + 0.5);

					// Check for Area visibility (default to false)
					isVisible = false;

					ICelestialBody celestialBody = (area != null ? area.getCelestialBody() : null);
					ProductiveCelestialBody productiveCelestialBody = (celestialBody != null && ProductiveCelestialBody.class.isInstance(celestialBody) ? ProductiveCelestialBody.class.cast(celestialBody) : null);
					Player celestialBodyOwner = (celestialBody != null ? celestialBody.getOwner() : null);
					Fleet unassignedFleet = (productiveCelestialBody != null ? productiveCelestialBody.getUnasignedFleet(playerLogin) : null);

					// Visible if area celestial body is owned by the player.
					if (!isVisible && celestialBodyOwner != null && celestialBodyOwner.isNamed(playerLogin))
					{
						isVisible = true;
					}

					// Visible if area contains a celestial body and player has a unit on it.
					if (!isVisible && ((unassignedFleet != null && !unassignedFleet.isEmpty()) || (productiveCelestialBody != null && !getUnits(area, playerLogin).isEmpty())))
					{
						isVisible = true;
					}

					// Area is under a player probe scope.
					if (!isVisible) for(Probe p : playerProbes)
					{
						if (common.SEPUtils.getDistance(location, p.getCurrentLocation()) > config.getProbeScope()) continue;

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
							playersPoliciesView.put(planet.getOwnerName(), playersPolicies.get(planet.getOwnerName()).getPlayerView(date, playerLogin, isVisible));							
						}
						
						// If governmental fleets are located in this area
						for(Fleet fleet : getUnits(area, Fleet.class, null))
						{
							if (fleet.isGovernmentFleet())
							{
								playersPoliciesView.put(fleet.getOwnerName(), playersPolicies.get(fleet.getOwnerName()).getPlayerView(date, playerLogin, isVisible));
							}
						}
						
						playerUniverseView[x][y][z] = getArea(location).getPlayerView(getDate(), playerLogin, isVisible);
					}
				}
		
		for(Player player : players)
		{
			if (playersPoliciesView.containsKey(player.getName())) continue;
			playersPoliciesView.put(player.getName(), playersPolicies.get(player.getName()).getPlayerView(date, playerLogin, false));
		}
		
		return new common.PlayerGameBoard(playerUniverseView, sunLocation, date, playersPoliciesView);
	}

	private UnitMarker getUnitMarker(String playerLogin, String ownerName, String unitName)
	{
		for(int x = 0; x < config.getDimX(); ++x)
			for(int y = 0; y < config.getDimY(); ++y)
				for(int z = 0; z < config.getDimZ(); ++z)
				{
					Area area = universe[x][y][z];
					if (area == null) continue;

					UnitMarker um = area.getUnitMarker(playerLogin, ownerName, unitName);
					if (um != null) return um;
				}

		return null;
	}

	/**
	 * Return the unit by its name, type, and owner.
	 * 
	 * @param <U>
	 * @param unitType
	 * @param playerLogin
	 * @param unitName
	 * @return
	 */
	private <U extends Unit> U getUnit(Class<U> unitType, String ownerName, String unitName)
	{
		for(U unit : getUnits(unitType, ownerName))
		{
			if (unit.getName().compareTo(unitName) == 0) return unit;
		}

		return null;
	}

	/**
	 * Return all units that implement a given unit sub class.
	 * 
	 * @param unitTypeFilter
	 *            if not null, return only units that implement this unit sub
	 *            class.
	 * @param playerLoginFilter
	 *            if not null, return only units owned by this player.
	 * @return Map<int[], Set<U extends Unit>> filtered unit from the entire
	 *         universe.
	 * @see #getUnits(int[], String, Class)
	 */
	private <U extends Unit> Set<U> getUnits(Class<U> unitType, String playerLogin)
	{
		Set<U> filteredUnits = new HashSet<U>();
		Set<U> currentLocationUnits;

		for(int x = 0; x < config.getDimX(); ++x)
			for(int y = 0; y < config.getDimY(); ++y)
				for(int z = 0; z < config.getDimZ(); ++z)
				{
					Area area = universe[x][y][z];
					if (area == null) continue;

					currentLocationUnits = getUnits(area, unitType, playerLogin);

					if (currentLocationUnits.isEmpty()) continue;

					filteredUnits.addAll(currentLocationUnits);
				}

		return filteredUnits;
	}

	/**
	 * Return units that implement a given unit sub class and are located in a
	 * specific location .
	 * 
	 * @param location
	 *            Location to look units for.
	 * @param unitTypeFilter
	 *            (cannot not be null) return only units that implement this
	 *            unit sub class.
	 * @param playerLoginFilter
	 *            if not null, return only units owned by this player.
	 * @return Set<Unit> filtered unit for the given location.
	 * @see #getUnits(String, Class)
	 */
	private <U extends Unit> Set<U> getUnits(Area area, Class<U> unitTypeFilter, String playerLoginFilter)
	{
		Set<U> filteredUnits = new HashSet<U>();
		for(Unit u : getUnits(area, playerLoginFilter))
		{
			if (unitTypeFilter.isInstance(u))
			{
				filteredUnits.add(unitTypeFilter.cast(u));
			}
		}
		return filteredUnits;
	}

	/**
	 * Return units from a specific location. Optional filters can apply.
	 * 
	 * @param location
	 *            Location to look units for.
	 * @param playerLoginFilter
	 *            if not null, return only units owned by this player.
	 * @return
	 */
	private Set<Unit> getUnits(Area area, String ownerName)
	{
		Set<Unit> filteredUnits = new HashSet<Unit>();

		if (area == null) return filteredUnits;

		Set<Unit> units = area.getUnits();

		for(Unit u : units)
		{
			// Owner filter (if ownerName == null then no filter)
			if (ownerName != null && (u.getOwner() == null || !u.getOwner().isNamed(ownerName))) continue;

			filteredUnits.add(u);
		}

		return filteredUnits;
	}

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

	/**
	 * Return the celestial body with the given name.
	 * 
	 * @param celestialBodyName
	 * @return
	 */
	public RealLocation getCelestialBodyLocation(String celestialBodyName)
	{
		for(int x = 0; x < config.getDimX(); ++x)
			for(int y = 0; y < config.getDimY(); ++y)
				for(int z = 0; z < config.getDimZ(); ++z)
				{
					Area area = universe[x][y][z];
					if (area == null) continue;
					ICelestialBody celestialBody = area.getCelestialBody();
					if (celestialBody == null) continue;

					if (celestialBody.getName().compareTo(celestialBodyName) == 0) return new RealLocation(x + 0.5, y + 0.5, z + 0.5);
				}
		return null;
	}

	private ICelestialBody getCelestialBody(String celestialBodyName)
	{
		RealLocation location = getCelestialBodyLocation(celestialBodyName);
		if (location == null) return null;
		return getNullArea(location).getCelestialBody();
	}

	/**
	 * Return the area where the given player government module is located.
	 * 
	 * @param playerLogin
	 * @return
	 */
	private Area locateGovernmentModule(String playerLogin)
	{
		for(int x = 0; x < config.getDimX(); ++x)
			for(int y = 0; y < config.getDimY(); ++y)
				for(int z = 0; z < config.getDimZ(); ++z)
				{
					if (universe[x][y][z] == null) continue;

					Area area = universe[x][y][z];

					ICelestialBody celestialBody = area.getCelestialBody();
					if (celestialBody == null) continue;

					if (!Planet.class.isInstance(celestialBody)) continue;
					Planet planet = Planet.class.cast(celestialBody);

					if (planet.getOwner() == null || !planet.getOwner().isNamed(playerLogin)) continue;

					if (planet.isGovernmentSettled()) return area;
				}

		return null;
	}

	private <T extends ABuilding> T getBuilding(ProductiveCelestialBody productiveCelestialBody, Class<T> buildingType)
	{
		for(ABuilding b : productiveCelestialBody.getBuildings())
		{
			if (buildingType.isInstance(b))
			{
				return buildingType.cast(b);
			}
		}

		return null;
	}

	private ABuilding getBuildingFromClientType(ProductiveCelestialBody productiveCelestialBody, Class<? extends common.IBuilding> clientBuildingType)
	{
		for(ABuilding b : productiveCelestialBody.getBuildings())
		{
			if (b.getClass().getSimpleName().compareTo(clientBuildingType.getSimpleName()) == 0)
			{
				return b;
			}
		}

		return null;
	}

	private Map<Probe, RealLocation> getDeployedProbesLocations()
	{
		Map<Probe, RealLocation> result = new HashMap<Probe, RealLocation>();

		for(int x = 0; x < config.getDimX(); ++x)
			for(int y = 0; y < config.getDimY(); ++y)
				for(int z = 0; z < config.getDimZ(); ++z)
				{
					Area area = universe[x][y][z];
					if (area == null) continue;

					Set<Unit> units = area.getUnits();
					if (units != null) for(Unit u : units)
					{
						if (Probe.class.isInstance(u))
						{
							Probe p = Probe.class.cast(u);
							if (p.isDeployed())
							{
								result.put(p, p.getCurrentLocation());
							}
						}
					}
				}

		return result;
	}

	public void resolveCurrentTurn()
	{
		// TODO : Résolve mobile units movement, attacks, etc... On Current instance.

		Set<ICelestialBody> celestialBodies = new HashSet<ICelestialBody>();
		Set<Unit> movingUnits = new HashSet<Unit>();
		double maxSpeed = Double.MIN_VALUE;

		for(int x = 0; x < config.getDimX(); ++x)
			for(int y = 0; y < config.getDimY(); ++y)
				for(int z = 0; z < config.getDimZ(); ++z)
				{
					Area area = universe[x][y][z];
					if (area == null) continue;

					if (area.getCelestialBody() != null)
					{
						celestialBodies.add(area.getCelestialBody());
					}

					RealLocation location = new RealLocation(x + 0.5, y + 0.5, z + 0.5);

					for(Unit unit : area.getUnits())
					{
						if (unit.isMoving() || unit.startMove(location, this))
						{
							movingUnits.add(unit);
							maxSpeed = Math.max(maxSpeed, unit.getSpeed());
						}

					}

				}

		// Unit moves

		// Detect units met.

		Map<Unit, RealLocation> currentStepRealLocations = new HashMap<Unit, RealLocation>();
		Map<Probe, RealLocation> deployedProbes = getDeployedProbesLocations();

		double step = 1 / maxSpeed;
		for(float currentStep = 0; currentStep <= 1; currentStep += step)
		{
			currentStepRealLocations.clear();

			for(Unit u : movingUnits)
			{
				double distance = SEPUtils.getDistance(u.getSourceLocation(), u.getDestinationLocation());
				double progressInOneTurn = (distance != 0 ? (Double.valueOf(1) / distance) * u.getSpeed() : 1);
				RealLocation currentStepLocation = SEPUtils.getMobileLocation(u.getSourceLocation(), u.getDestinationLocation(), u.getTravellingProgress() + progressInOneTurn * currentStep, true);

				for(Unit seenUnit : currentStepRealLocations.keySet())
				{
					RealLocation loc = currentStepRealLocations.get(seenUnit);

					if (SEPUtils.getDistance(loc, currentStepLocation) <= 1)
					{
						seenUnit.addTravelligLogEntry(new TravellingLogEntryUnitSeen("Unit seen", date, currentStep, currentStepLocation, u.getPlayerView(date, seenUnit.getOwnerName(), true)));
						u.addTravelligLogEntry(new TravellingLogEntryUnitSeen("Unit seen", date, currentStep, loc, seenUnit.getPlayerView(date, u.getOwnerName(), true)));
					}
				}

				for(Probe probe : deployedProbes.keySet())
				{
					RealLocation loc = deployedProbes.get(probe);
					distance = SEPUtils.getDistance(loc, currentStepLocation);

					if (distance <= config.getProbeScope())
					{
						probe.addTravelligLogEntry(new TravellingLogEntryUnitSeen("Unit seen", date, currentStep, currentStepLocation, u.getPlayerView(date, probe.getOwnerName(), true)));
					}

					if (distance <= 1)
					{
						u.addTravelligLogEntry(new TravellingLogEntryUnitSeen("Probe seen", date, currentStep, loc, probe.getPlayerView(date, u.getOwnerName(), true)));
					}
				}

				currentStepRealLocations.put(u, currentStepLocation);
			}
		}

		Map<Area, Set<AntiProbeMissile>> explodingAntiProbeMissiles = new HashMap<Area, Set<AntiProbeMissile>>();

		// Move units.
		for(Unit movingUnit : movingUnits)
		{
			double distance = SEPUtils.getDistance(movingUnit.getSourceLocation(), movingUnit.getDestinationLocation());
			double progressInOneTurn = (distance != 0 ? (Double.valueOf(1) / distance) * movingUnit.getSpeed() : 1);			
			double newTravellingProgress = movingUnit.getTravellingProgress() + progressInOneTurn;
			RealLocation endTurnLocation = SEPUtils.getMobileLocation(movingUnit.getSourceLocation(), movingUnit.getDestinationLocation(), newTravellingProgress, true).asLocation();

			String playerLogin = movingUnit.getOwner().getName();
			Area startingArea = getNullArea(movingUnit.getCurrentLocation().asLocation());
			startingArea.removeUnit(movingUnit.getClass(), movingUnit.getOwnerName(), movingUnit.getName());
			UnitMarker unitMarker = startingArea.getUnitMarker(playerLogin, movingUnit.getOwnerName(), movingUnit.getName());
			if (unitMarker != null)
			{
				startingArea.removeUnitMarker(playerLogin, movingUnit.getOwnerName(), movingUnit.getName());
			}
			else
			{
				unitMarker = new UnitMarker(date, true, movingUnit.getPlayerView(date, playerLogin, true));
			}

			Area endingArea = getArea(endTurnLocation);

			endingArea.updateUnit(movingUnit);
			movingUnit.setTravellingProgress(newTravellingProgress);

			if (endTurnLocation.equals(movingUnit.getDestinationLocation().asLocation()))
			{
				movingUnit.endMove(new RealLocation(endTurnLocation.x + 0.5, endTurnLocation.y + 0.5, endTurnLocation.z + 0.5), this);
				if (AntiProbeMissile.class.isInstance(movingUnit))
				{
					if (!explodingAntiProbeMissiles.containsKey(endingArea))
					{
						explodingAntiProbeMissiles.put(endingArea, new HashSet<AntiProbeMissile>());
					}

					explodingAntiProbeMissiles.get(endingArea).add(AntiProbeMissile.class.cast(movingUnit));
				}
			}
			else
			{
				endingArea.updateUnitMarker(playerLogin, unitMarker);
			}
		}

		// Explode anti-probe missiles.
		for(Area area : explodingAntiProbeMissiles.keySet())
		{
			if (explodingAntiProbeMissiles.get(area) == null) continue;
			for(AntiProbeMissile apm : explodingAntiProbeMissiles.get(area))
			{
				Probe targetProbe = area.getUnit(Probe.class, apm.getTargetOwnerName(), apm.getTargetName());
				if (targetProbe != null)
				{
					// TODO : New event for targetProbe owner (probe destroyed)
					area.removeUnit(Probe.class, targetProbe.getOwnerName(), targetProbe.getName());
				}

				area.removeUnit(AntiProbeMissile.class, apm.getOwnerName(), apm.getName());
			}
		}
		
		// Conflicts
		

		// Carbon & Population generation
		for(ICelestialBody celestialBody : celestialBodies)
		{
			if (ProductiveCelestialBody.class.isInstance(celestialBody))
			{
				ProductiveCelestialBody productiveCelestialBody = ProductiveCelestialBody.class.cast(celestialBody);

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
						generatedCarbon = config.getNaturalCarbonPerTurn();							
					}
				}																
				
				GovernmentModule governmentModule = productiveCelestialBody.getBuilding(GovernmentModule.class);
				if (governmentModule != null)
				{
					generatedCarbon = (int) (generatedCarbon * 1.5);
				}
				
				int currentCarbon = productiveCelestialBody.getCarbon();
				generatedCarbon = Math.min(config.getMaxNaturalCarbon() - currentCarbon, generatedCarbon);
				
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
		}

		++date;
	}

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

	public DemolishCheckResult checkDemolish(String playerLogin, String celestialBodyName, Class<? extends common.IBuilding> buildingType) throws RunningGameCommandException
	{
		ICelestialBody celestialBody = getCelestialBody(celestialBodyName);
		if (celestialBody == null) throw new RunningGameCommandException("Celestial body '" + celestialBodyName + "' does not exist.");

		// If celestial body is not a productive one.
		if (!ProductiveCelestialBody.class.isInstance(celestialBody)) throw new RunningGameCommandException("Celestial body '" + celestialBodyName + "' is not a productive one.");
		ProductiveCelestialBody productiveCelestialBody = ProductiveCelestialBody.class.cast(celestialBody);

		// If player is not the celestial body owner.
		if (productiveCelestialBody.getOwner() == null || !productiveCelestialBody.getOwner().isNamed(playerLogin)) throw new RunningGameCommandException("Player '" + playerLogin + "' is not the '" + celestialBodyName + "' celestial body owner.");

		ABuilding building = getBuildingFromClientType(productiveCelestialBody, buildingType);

		// If no building of this type exist.
		if (building == null || building.getBuildSlotsCount() == 0) throw new RunningGameCommandException("No building type '" + buildingType.getSimpleName() + "' built yet.");

		if (!building.canDowngrade()) throw new RunningGameCommandException("Cannot demolish building type '" + buildingType.getSimpleName() + "'");

		return new DemolishCheckResult(productiveCelestialBody, building);
	}

	public void embarkGovernment(String playerLogin) throws RunningGameCommandException
	{
		EmbarkGovernmentCheckResult embarkGovernmentCheckResult = checkEmbarkGovernment(playerLogin);
		embarkGovernmentCheckResult.planet.removeBuilding(GovernmentModule.class);
		Map<Class<? extends IStarship>, Integer> starshipToMake = new HashMap<Class<? extends IStarship>, Integer>();
		starshipToMake.put(GovernmentStarship.class, 1);
		embarkGovernmentCheckResult.planet.mergeToUnasignedFleet(getPlayer(playerLogin), starshipToMake);
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
		Area area = locateGovernmentModule(playerLogin);

		// If player has no government module.
		if (area == null) throw new RunningGameCommandException("Cannot locate '" + playerLogin + "' government module.");

		ICelestialBody celestialBody = area.getCelestialBody();
		if (celestialBody == null) throw new RunningGameCommandException("Cannot locate '" + playerLogin + "' government module celestial body (unexpected error).");
		if (!Planet.class.isInstance(celestialBody)) throw new RunningGameCommandException("Cannot locate '" + playerLogin + "' government module planet (unexpected error).");
		Planet planet = Planet.class.cast(celestialBody);

		// If player is not the celestial body owner.
		if (planet.getOwner() == null || !planet.getOwner().isNamed(playerLogin)) throw new RunningGameCommandException("Player '" + playerLogin + "' is not the planet owner (unexpected error).");

		GovernmentModule governmentModule = getBuilding(planet, GovernmentModule.class);

		if (governmentModule == null) throw new RunningGameCommandException("No government module on the planet '" + planet.getName() + "' (unexpected error)");

		StarshipPlant starshipPlant = getBuilding(planet, StarshipPlant.class);

		// If planet has no starshipPlant.
		if (starshipPlant == null) throw new RunningGameCommandException("No starship plant on planet '" + planet.getName() + "'");

		// If starship has just been build this turn.
		if (starshipPlant.getLastBuildDate() >= date) throw new RunningGameCommandException("Starship plant is still in construction.");

		int carbonCost = common.GovernmentStarship.PRICE_CARBON;
		int populationCost = common.GovernmentStarship.PRICE_POPULATION;

		if (carbonCost > planet.getCarbon()) throw new RunningGameCommandException("Not enough carbon.");
		;
		if (populationCost > planet.getPopulation()) throw new RunningGameCommandException("Not enough population.");
		;
		;

		return new EmbarkGovernmentCheckResult(planet, governmentModule, starshipPlant, carbonCost, populationCost);
	}

	public boolean canFirePulsarMissile(common.Player player, String celestialBodyName)
	{
		ICelestialBody celestialBody = getCelestialBody(celestialBodyName);
		if (celestialBody == null) throw new IllegalArgumentException("Celestial body '" + celestialBodyName + "' does not exist.");

		// If celestial body is not a productive one.
		if (!ProductiveCelestialBody.class.isInstance(celestialBody)) return false;
		ProductiveCelestialBody productiveCelestialBody = ProductiveCelestialBody.class.cast(celestialBody);

		// If player is not the celestial body owner.
		if (productiveCelestialBody.getOwner() == null || !productiveCelestialBody.getOwner().isNamed(player.getName())) return false;

		ABuilding building = getBuildingFromClientType(productiveCelestialBody, common.PulsarLauchingPad.class);

		// If no building of this type exist.
		if (building == null) return false;

		// Building type check		
		if (!PulsarLauchingPad.class.isInstance(building)) return false;

		PulsarLauchingPad pulsarLaunchingPad = PulsarLauchingPad.class.cast(building);
		if (pulsarLaunchingPad.getUnusedCount() <= 0) return false;

		return true;
	}

	public boolean canSettleGovernment(common.Player player, String planetName)
	{
		RealLocation celestialBodyLocation = getCelestialBodyLocation(planetName);
		if (celestialBodyLocation == null) throw new IllegalArgumentException("Celestial body '" + planetName + "' does not exist.");

		Area area = getNullArea(celestialBodyLocation);
		ICelestialBody celestialBody = area.getCelestialBody();

		// If celestial body is not a productive one.
		if (!Planet.class.isInstance(celestialBody)) return false;
		Planet planet = Planet.class.cast(celestialBody);

		// If player is not the celestial body owner.
		if (planet.getOwner() == null || !planet.getOwner().isNamed(player.getName())) return false;

		// Check if government fleet is on the planet.
		for(Fleet f : getUnits(area, Fleet.class, player.getName()))
		{
			if (f.isGovernmentFleet()) return true;
		}

		return false;
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

		buildCheckResult.productiveCelestialBody.setLastBuildDate(date);
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

	private BuildCheckResult checkBuild(String playerLogin, String celestialBodyName, Class<? extends common.IBuilding> buildingType) throws CelestialBodyBuildException
	{
		ICelestialBody celestialBody = getCelestialBody(celestialBodyName);
		if (celestialBody == null) throw new CelestialBodyBuildException("Celestial body '" + celestialBodyName + "' does not exist.");

		// If celestial body is not a productive one.
		if (!ProductiveCelestialBody.class.isInstance(celestialBody)) throw new CelestialBodyBuildException("Celestial body '" + celestialBodyName + "' is not a productive one.");
		ProductiveCelestialBody productiveCelestialBody = ProductiveCelestialBody.class.cast(celestialBody);

		// If player is not the celestial body owner.
		if (productiveCelestialBody.getOwner() == null || !productiveCelestialBody.getOwner().isNamed(playerLogin)) throw new CelestialBodyBuildException("Player '" + playerLogin + "' is not the '" + celestialBodyName + "' celestial body owner.");

		// If this productive celestial body build was already used this turn.
		if (productiveCelestialBody.getLastBuildDate() >= date) throw new CelestialBodyBuildException("Celestial body '" + celestialBodyName + "' already in work for this turn.");

		// If there is no more free slots.
		if (productiveCelestialBody.getFreeSlotsCount() < 1) throw new CelestialBodyBuildException("No more free slots on celestial body '" + celestialBodyName + "'");

		// Price check & Celestial body type / building type check
		int carbonCost = 0;
		int populationCost = 0;

		ABuilding building = getBuildingFromClientType(productiveCelestialBody, buildingType);
		ABuilding newBuilding;

		if (building != null)
		{
			carbonCost = building.getUpgradeCarbonCost();
			populationCost = building.getUpgradePopulationCost();
			newBuilding = building.getUpgraded(date);
		}
		else
		{
			carbonCost = ABuilding.getFirstCarbonCost(buildingType);
			populationCost = ABuilding.getFirstPopulationCost(buildingType);
			newBuilding = ABuilding.getFirstBuild(buildingType, date);
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
		final Area				area;
		final AntiProbeMissile	antiProbeMissile;
		final String			targetProbeName;
		final RealLocation		source;
		final RealLocation		destination;

		public FireAntiProbeMissileCheckResult(Area area, AntiProbeMissile antiProbeMissile, String targetProbeName, RealLocation source, RealLocation destination)
		{
			this.area = area;
			this.antiProbeMissile = antiProbeMissile;
			this.targetProbeName = targetProbeName;
			this.source = source;
			this.destination = destination;
		}
	}

	public FireAntiProbeMissileCheckResult checkFireAntiProbeMissile(String playerLogin, String antiProbeMissileName, String targetOwnerName, String targetProbeName) throws RunningGameCommandException
	{
		AntiProbeMissile antiProbeMissile = getUnit(AntiProbeMissile.class, playerLogin, antiProbeMissileName);
		if (antiProbeMissile == null) throw new RunningGameCommandException("AntiProbeMissile '" + antiProbeMissileName + "' does not exist.");

		Area area = getNullArea(antiProbeMissile.getCurrentLocation());
		if (area == null) throw new RunningGameCommandException("AntiProbeMissile '" + antiProbeMissileName + "' does not exist.");

		if (!antiProbeMissile.getOwner().isNamed(playerLogin)) throw new RunningGameCommandException("Unexpected error : " + playerLogin + " is not '" + antiProbeMissileName + "' anti-probe missile owner");

		if (antiProbeMissile.isMoving()) throw new RunningGameCommandException("AntiProbeMissile '" + antiProbeMissileName + "' has already been fired.");

		if (antiProbeMissile.isFired()) throw new RunningGameCommandException("AntiProbeMissile '" + antiProbeMissileName + "' is already fired.");

		RealLocation destination;
		Probe targetProbe = getUnit(Probe.class, targetOwnerName, targetProbeName);
		if (targetProbe != null)
		{
			destination = targetProbe.getCurrentLocationView(date, playerLogin, false);
		}
		else
		{
			UnitMarker um = getUnitMarker(playerLogin, targetOwnerName, targetProbeName);
			if (um == null || um.getUnit() == null || !common.Probe.class.isInstance(um.getUnit()))
			{
				throw new RunningGameCommandException("AntiProbeMissile '" + antiProbeMissileName + "' target '" + targetProbeName + "' does not exist.");
			}

			common.Probe probe = common.Probe.class.cast(um.getUnit());

			if (probe.isMoving()) throw new RunningGameCommandException("AntiProbeMissile '" + antiProbeMissileName + "' cannot be fired on moving target '" + targetProbeName + "'");

			destination = probe.getCurrentLocation();
		}

		if (destination == null) throw new RunningGameCommandException("AntiProbeMissile '" + antiProbeMissileName + "' target '" + targetProbeName + "' does not exist.");

		Area destinationArea = getNullArea(destination);
		if (destinationArea == null) throw new RunningGameCommandException("AntiProbeMissile '" + antiProbeMissileName + "' target '" + targetProbeName + "' does not exist.");

		return new FireAntiProbeMissileCheckResult(area, antiProbeMissile, targetProbeName, antiProbeMissile.getCurrentLocation(), destination);
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
		launchProbeCheckResult.probe.launch(launchProbeCheckResult.source, launchProbeCheckResult.destination);
	}

	private static class LaunchProbeCheckResult
	{
		final Area			area;
		final Probe			probe;
		final RealLocation	source;
		final RealLocation	destination;

		public LaunchProbeCheckResult(Area area, Probe probe, RealLocation source, RealLocation destination)
		{
			this.area = area;
			this.probe = probe;
			this.source = source;
			this.destination = destination;
		}
	}

	public LaunchProbeCheckResult checkLaunchProbe(String playerLogin, String probeName, RealLocation destination) throws RunningGameCommandException
	{
		Probe probe = getUnit(Probe.class, playerLogin, probeName);
		if (probe == null) throw new RunningGameCommandException("Probe '" + probeName + "' does not exist.");

		RealLocation location = probe.getCurrentLocation();
		if (location == null) throw new RunningGameCommandException("Probe '" + probeName + "' does not exist.");

		Area area = getNullArea(location);
		if (area == null) throw new RunningGameCommandException("Probe '" + probeName + "' does not exist.");

		if (!probe.getOwner().isNamed(playerLogin)) throw new RunningGameCommandException("Unexpected error : " + playerLogin + " is not '" + probeName + "' probe owner");

		if (probe.isMoving()) throw new RunningGameCommandException("Probe '" + probeName + "' has already been launched.");

		if (probe.isDeployed()) throw new RunningGameCommandException("Probe '" + probeName + "' is already deployed.");

		if (destination.x < 0 || destination.x >= config.getDimX()) throw new RunningGameCommandException("Probe '" + probeName + "' destination is incorrect (x).");
		if (destination.y < 0 || destination.y >= config.getDimY()) throw new RunningGameCommandException("Probe '" + probeName + "' destination is incorrect (y).");
		if (destination.z < 0 || destination.z >= config.getDimZ()) throw new RunningGameCommandException("Probe '" + probeName + "' destination is incorrect (z).");

		for(RealLocation pathStep : SEPUtils.getAllPathLoc(location, destination))
		{
			if (getNullArea(pathStep) != null && getNullArea(pathStep).isSun()) throw new RunningGameCommandException("Impossible path : " + location + " to " + destination + ", cannot travel the sun.");
		}

		Area destinationArea = getNullArea(destination);
		if (destinationArea != null && destinationArea.isSun()) throw new RunningGameCommandException("Cannot send Probe on sun.");

		return new LaunchProbeCheckResult(area, probe, location, destination);
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
		final Area						area;
		final Fleet						fleet;
		final Stack<common.Fleet.Move>	locatedCheckpoints;

		public MoveFleetCheckResult(Area area, Fleet fleet, Stack<common.Fleet.Move> locatedCheckpoints)
		{
			this.area = area;
			this.fleet = fleet;
			this.locatedCheckpoints = locatedCheckpoints;
		}
	}

	public MoveFleetCheckResult checkMoveFleet(String playerLogin, String fleetName, Stack<common.Fleet.Move> checkpoints) throws RunningGameCommandException
	{
		Fleet fleet = getUnit(Fleet.class, playerLogin, fleetName);
		if (fleet == null) throw new RunningGameCommandException("Fleet '" + fleetName + "' does not exist.");

		RealLocation location = fleet.getCurrentLocation();
		if (location == null) throw new RunningGameCommandException("Fleet '" + fleetName + "' does not exist.");

		Area area = getNullArea(location);
		if (area == null) throw new RunningGameCommandException("Fleet '" + fleetName + "' does not exist.");

		if (!fleet.getOwner().isNamed(playerLogin)) throw new RunningGameCommandException("Unexpected error : " + playerLogin + " is not '" + fleetName + "' fleet owner");

		// Check paths
		Stack<common.Fleet.Move> locatedCheckpoints = new Stack<common.Fleet.Move>();

		RealLocation currentStart = (fleet.isMoving() ? fleet.getDestinationLocation() : location);
		for(common.Fleet.Move move : checkpoints)
		{
			RealLocation destinationLocation = getCelestialBodyLocation(move.getDestinationName());
			if (destinationLocation == null) throw new RunningGameCommandException("Unexpected error : checkpoint destination '" + move.getDestinationName() + "' not found.");

			for(RealLocation pathStep : SEPUtils.getAllPathLoc(currentStart, destinationLocation))
			{
				if (getNullArea(pathStep) != null && getNullArea(pathStep).isSun()) throw new RunningGameCommandException("Impossible path : " + currentStart + " to " + destinationLocation + ", cannot travel the sun.");
			}

			currentStart = destinationLocation;

			locatedCheckpoints.add(new common.Fleet.Move(move, destinationLocation));
		}

		return new MoveFleetCheckResult(area, fleet, locatedCheckpoints);
	}

	public boolean canFormFleet(String playerLogin, String planetName, String fleetName, Map<Class<? extends IStarship>, Integer> fleetToForm)
	{
		try
		{
			checkFormFleet(playerLogin, planetName, fleetName, fleetToForm);
		}
		catch(Throwable t)
		{
			return false;
		}
		return true;
	}

	public void formFleet(String playerLogin, String planetName, String fleetName, Map<Class<? extends IStarship>, Integer> fleetToForm) throws RunningGameCommandException
	{
		FormFleetCheckResult formFleetCheckResult = checkFormFleet(playerLogin, planetName, fleetName, fleetToForm);
		formFleetCheckResult.area.updateUnit(formFleetCheckResult.newFleet);
		formFleetCheckResult.productiveCelestialBody.removeFromUnasignedFleet(getPlayer(playerLogin), fleetToForm);
	}

	private static class FormFleetCheckResult
	{
		final Area						area;
		final ProductiveCelestialBody	productiveCelestialBody;
		final Fleet						newFleet;

		public FormFleetCheckResult(Area area, ProductiveCelestialBody productiveCelestialBody, Fleet newFleet)
		{
			this.area = area;
			this.productiveCelestialBody = productiveCelestialBody;
			this.newFleet = newFleet;
		}
	}

	private FormFleetCheckResult checkFormFleet(String playerLogin, String planetName, String fleetName, Map<Class<? extends common.IStarship>, Integer> fleetToForm) throws RunningGameCommandException
	{
		RealLocation location = getCelestialBodyLocation(planetName);
		Area area = getNullArea(location);

		if (area == null) throw new RunningGameCommandException("Celestial body '" + planetName + "' does not exist.");
		ICelestialBody celestialBody = area.getCelestialBody();

		if (celestialBody == null) throw new RunningGameCommandException("Celestial body '" + planetName + "' does not exist.");

		// If celestial body is not a productive one.
		if (!ProductiveCelestialBody.class.isInstance(celestialBody)) throw new RunningGameCommandException("Celestial body '" + planetName + "' is not a productive celestial body.");
		ProductiveCelestialBody productiveCelestialBody = ProductiveCelestialBody.class.cast(celestialBody);

		Fleet unasignedFleet = productiveCelestialBody.getUnasignedFleet(playerLogin);
		if (unasignedFleet == null) throw new RunningGameCommandException("No available unasigned fleet on celestial body '" + celestialBody.getName() + "'");

		Fleet fleet = getUnit(Fleet.class, playerLogin, fleetName);
		if (fleet != null) throw new RunningGameCommandException("Fleet named '" + fleetName + "' already exist.");

		// Starship availability check		
		for(Entry<Class<? extends IStarship>, Integer> e : fleetToForm.entrySet())
		{
			if (e.getValue() <= 0) continue;

			int qt = e.getValue();
			if (!unasignedFleet.getComposition().containsKey(e.getKey())) throw new RunningGameCommandException("Unasigned fleet does not have required starship type '" + e.getKey().getSimpleName() + "'");
			if (unasignedFleet.getComposition().get(e.getKey()) < qt) throw new RunningGameCommandException("Unasigned flee does not have enough starship type '" + e.getKey() + "'");
		}

		Player owner = getPlayer(playerLogin);
		if (owner == null) throw new RunningGameCommandException("Unknown player name '" + playerLogin + "'");

		Fleet newFleet = new Fleet(fleetName, owner, location, fleetToForm, false);

		return new FormFleetCheckResult(area, productiveCelestialBody, newFleet);
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

	public void dismantleFleet(String playerLogin, String fleetName) throws RunningGameCommandException
	{
		DismantleFleetCheckResult dismantleFleetCheckResult = checkDismantleFleet(playerLogin, fleetName);
		dismantleFleetCheckResult.area.removeUnit(dismantleFleetCheckResult.fleet.getClass(), dismantleFleetCheckResult.fleet.getOwnerName(), dismantleFleetCheckResult.fleet.getName());
		dismantleFleetCheckResult.productiveCelestialBody.mergeToUnasignedFleet(getPlayer(playerLogin), dismantleFleetCheckResult.fleet.getComposition());
	}

	private static class DismantleFleetCheckResult
	{
		final Area						area;
		final ProductiveCelestialBody	productiveCelestialBody;
		final Fleet						fleet;

		public DismantleFleetCheckResult(Area area, ProductiveCelestialBody productiveCelestialBody, Fleet fleet)
		{
			this.area = area;
			this.productiveCelestialBody = productiveCelestialBody;
			this.fleet = fleet;
		}
	}

	private DismantleFleetCheckResult checkDismantleFleet(String playerLogin, String fleetName) throws RunningGameCommandException
	{
		Fleet fleet = getUnit(Fleet.class, playerLogin, fleetName);
		if (fleet == null) throw new RunningGameCommandException("Fleet '" + fleetName + "' does not exist.");

		RealLocation location = fleet.getCurrentLocation();
		Area area = getNullArea(location);

		if (area == null) throw new RunningGameCommandException("Fleet '" + fleetName + "' does not exist.");
		ICelestialBody celestialBody = area.getCelestialBody();

		if (celestialBody == null) throw new RunningGameCommandException("Fleet is in travel.");

		// If celestial body is not a ProductiveCelestialBody.
		if (!ProductiveCelestialBody.class.isInstance(celestialBody)) throw new RunningGameCommandException("Fleet is not on a ProductiveCelestialBody.");
		ProductiveCelestialBody productiveCelestialBody = ProductiveCelestialBody.class.cast(celestialBody);

		return new DismantleFleetCheckResult(area, productiveCelestialBody, fleet);
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
			makeProbesCheckResult.area.updateUnit(p);
		}
		makeProbesCheckResult.planet.setCarbon(makeProbesCheckResult.planet.getCarbon() - makeProbesCheckResult.carbonCost);
		makeProbesCheckResult.planet.setPopulation(makeProbesCheckResult.planet.getPopulation() - makeProbesCheckResult.populationCost);
	}

	private static class MakeProbesCheckResult
	{
		final Area			area;
		final Planet		planet;
		final int			carbonCost;
		final int			populationCost;
		final StarshipPlant	starshipPlant;
		final Set<Probe>	newProbes;

		public MakeProbesCheckResult(Area area, Planet planet, int carbonCost, int populationCost, StarshipPlant starshipPlant, Set<Probe> newProbes)
		{
			this.area = area;
			this.planet = planet;
			this.carbonCost = carbonCost;
			this.populationCost = populationCost;
			this.starshipPlant = starshipPlant;
			this.newProbes = newProbes;
		}
	}

	private MakeProbesCheckResult checkMakeProbes(String playerLogin, String planetName, String probeName, int quantity) throws RunningGameCommandException
	{
		RealLocation location = getCelestialBodyLocation(planetName);
		Area area = getNullArea(location);

		if (area == null) throw new RunningGameCommandException("Celestial body '" + planetName + "' does not exist.");
		ICelestialBody celestialBody = area.getCelestialBody();

		if (celestialBody == null) throw new RunningGameCommandException("Celestial body '" + planetName + "' does not exist.");

		// If celestial body is not a planet.
		if (!Planet.class.isInstance(celestialBody)) throw new RunningGameCommandException("Celestial body '" + planetName + "' is not a planet.");
		Planet planet = Planet.class.cast(celestialBody);

		// If player is not the celestial body owner.
		if (planet.getOwner() == null || !planet.getOwner().isNamed(playerLogin)) throw new RunningGameCommandException("Player '" + playerLogin + "' is not the '" + planetName + "' planet owner.");

		StarshipPlant starshipPlant = getBuilding(planet, StarshipPlant.class);

		// If planet has no starshipPlant.
		if (starshipPlant == null) throw new RunningGameCommandException("No starship plant on planet '" + planetName + "'");

		// If starship has just been build this turn.
		if (starshipPlant.getLastBuildDate() >= date) throw new RunningGameCommandException("Starship plant is still in construction.");

		// Price check
		int carbonCost = common.Probe.PRICE_CARBON * quantity;
		int populationCost = common.Probe.PRICE_POPULATION * quantity;

		if (carbonCost == 0 && populationCost == 0) throw new RunningGameCommandException("Seems like quantity is null.");
		if (quantity < 0) throw new RunningGameCommandException("Quantity cannot be lesser than 0.");

		if (planet.getCarbon() < carbonCost) throw new RunningGameCommandException("Not enough carbon.");
		if (planet.getPopulation() < populationCost) throw new RunningGameCommandException("Not enough population.");

		Player owner = getPlayer(playerLogin);
		if (owner == null) throw new RunningGameCommandException("Unknown player name '" + playerLogin + "'");

		if (getUnit(Probe.class, playerLogin, probeName) != null || getUnit(Probe.class, playerLogin, probeName + "1") != null)
		{
			throw new RunningGameCommandException("Probe serial '" + probeName + "' already exist.");
		}

		Set<Probe> newProbes = new HashSet<Probe>();
		for(int i = 0; i < quantity; ++i)
		{
			newProbes.add(new Probe(probeName + i, owner, location, false));
		}

		return new MakeProbesCheckResult(area, planet, carbonCost, populationCost, starshipPlant, newProbes);
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
			makeAntiProbeMissilesCheckResult.area.updateUnit(p);
		}
		makeAntiProbeMissilesCheckResult.planet.setCarbon(makeAntiProbeMissilesCheckResult.planet.getCarbon() - makeAntiProbeMissilesCheckResult.carbonCost);
		makeAntiProbeMissilesCheckResult.planet.setPopulation(makeAntiProbeMissilesCheckResult.planet.getPopulation() - makeAntiProbeMissilesCheckResult.populationCost);
	}

	private static class MakeAntiProbeMissilesCheckResult
	{
		final Area					area;
		final Planet				planet;
		final int					carbonCost;
		final int					populationCost;
		final StarshipPlant			starshipPlant;
		final Set<AntiProbeMissile>	newAntiProbeMissiles;

		public MakeAntiProbeMissilesCheckResult(Area area, Planet planet, int carbonCost, int populationCost, StarshipPlant starshipPlant, Set<AntiProbeMissile> newAntiProbeMissiles)
		{
			this.area = area;
			this.planet = planet;
			this.carbonCost = carbonCost;
			this.populationCost = populationCost;
			this.starshipPlant = starshipPlant;
			this.newAntiProbeMissiles = newAntiProbeMissiles;
		}
	}

	private MakeAntiProbeMissilesCheckResult checkMakeAntiProbeMissiles(String playerLogin, String planetName, String antiProbeMissileName, int quantity) throws RunningGameCommandException
	{
		RealLocation location = getCelestialBodyLocation(planetName);
		Area area = getNullArea(location);

		if (area == null) throw new RunningGameCommandException("Celestial body '" + planetName + "' does not exist.");
		ICelestialBody celestialBody = area.getCelestialBody();

		if (celestialBody == null) throw new RunningGameCommandException("Celestial body '" + planetName + "' does not exist.");

		// If celestial body is not a planet.
		if (!Planet.class.isInstance(celestialBody)) throw new RunningGameCommandException("Celestial body '" + planetName + "' is not a planet.");
		Planet planet = Planet.class.cast(celestialBody);

		// If player is not the celestial body owner.
		if (planet.getOwner() == null || !planet.getOwner().isNamed(playerLogin)) throw new RunningGameCommandException("Player '" + playerLogin + "' is not the '" + planetName + "' planet owner.");

		StarshipPlant starshipPlant = getBuilding(planet, StarshipPlant.class);

		// If planet has no starshipPlant.
		if (starshipPlant == null) throw new RunningGameCommandException("No starship plant on planet '" + planetName + "'");

		// If starship has just been build this turn.
		if (starshipPlant.getLastBuildDate() >= date) throw new RunningGameCommandException("Starship plant is still in construction.");

		// Price check
		int carbonCost = common.AntiProbeMissile.PRICE_CARBON * quantity;
		int populationCost = common.AntiProbeMissile.PRICE_POPULATION * quantity;

		if (carbonCost == 0 && populationCost == 0) throw new RunningGameCommandException("Seems like quantity is null.");
		if (quantity < 0) throw new RunningGameCommandException("Quantity cannot be lesser than 0.");

		if (planet.getCarbon() < carbonCost) throw new RunningGameCommandException("Not enough carbon.");
		if (planet.getPopulation() < populationCost) throw new RunningGameCommandException("Not enough population.");

		Player owner = getPlayer(playerLogin);
		if (owner == null) throw new RunningGameCommandException("Unknown player name '" + playerLogin + "'");

		if (getUnit(Probe.class, playerLogin, antiProbeMissileName) != null || getUnit(Probe.class, playerLogin, antiProbeMissileName + "1") != null)
		{
			throw new RunningGameCommandException("AntiProbeMissile serial '" + antiProbeMissileName + "' already exist.");
		}

		Set<AntiProbeMissile> newAntiProbeMissiles = new HashSet<AntiProbeMissile>();
		for(int i = 0; i < quantity; ++i)
		{
			newAntiProbeMissiles.add(new AntiProbeMissile(antiProbeMissileName + i, owner, location, false));
		}

		return new MakeAntiProbeMissilesCheckResult(area, planet, carbonCost, populationCost, starshipPlant, newAntiProbeMissiles);
	}

	public boolean canMakeStarships(String playerLogin, String planetName, Map<Class<? extends IStarship>, Integer> starshipsToMake)
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

	public void makeStarships(String playerLogin, String planetName, Map<Class<? extends IStarship>, Integer> starshipsToMake) throws RunningGameCommandException
	{
		MakeStarshipsCheckResult makeStarshipsCheckResult = checkMakeStarships(playerLogin, planetName, starshipsToMake);

		Fleet unassignedFleet = makeStarshipsCheckResult.planet.getUnasignedFleet(playerLogin);
		makeStarshipsCheckResult.planet.mergeToUnasignedFleet(getPlayer(playerLogin), starshipsToMake);

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

	private MakeStarshipsCheckResult checkMakeStarships(String playerLogin, String planetName, Map<Class<? extends common.IStarship>, Integer> starshipsToMake) throws RunningGameCommandException
	{
		ICelestialBody celestialBody = getCelestialBody(planetName);

		if (celestialBody == null) throw new RunningGameCommandException("Celestial body '" + planetName + "' does not exist.");

		// If celestial body is not a planet.
		if (!Planet.class.isInstance(celestialBody)) throw new RunningGameCommandException("Celestial body '" + planetName + "' is not a planet.");
		Planet planet = Planet.class.cast(celestialBody);

		// If player is not the celestial body owner.
		if (planet.getOwner() == null || !planet.getOwner().isNamed(playerLogin)) throw new RunningGameCommandException("Player '" + playerLogin + "' is not the '" + planetName + "' planet owner.");

		StarshipPlant starshipPlant = getBuilding(planet, StarshipPlant.class);

		// If planet has no starshipPlant.
		if (starshipPlant == null) throw new RunningGameCommandException("No starship plant on planet '" + planetName + "'");

		// If starship has just been build this turn.
		if (starshipPlant.getLastBuildDate() >= date) throw new RunningGameCommandException("Starship plant is still in construction.");

		// Price check
		int carbonCost = 0;
		int populationCost = 0;

		for(Entry<Class<? extends IStarship>, Integer> e : starshipsToMake.entrySet())
		{
			if (e.getValue() <= 0) continue;

			int carbonPrice = 0;
			int populationPrice = 0;

			try
			{
				carbonPrice = e.getKey().getField("PRICE_CARBON").getInt(null);
			}
			catch(Throwable t)
			{
				carbonPrice = 0;
			}

			try
			{
				populationPrice = e.getKey().getField("PRICE_POPULATION").getInt(null);
			}
			catch(Throwable t)
			{
				populationPrice = 0;
			}

			if (carbonPrice == 0 && populationPrice == 0)
			{
				throw new RunningGameCommandException("Implementation error : Price are not defined for Starship class '" + e.getKey().getSimpleName() + "'");
			}

			carbonCost += carbonPrice * e.getValue();
			populationCost += populationPrice * e.getValue();
		}

		if (carbonCost == 0 && populationCost == 0) throw new RunningGameCommandException("Seems like no starships are selected (cost is null).");

		if (planet.getCarbon() < carbonCost) throw new RunningGameCommandException("Not enough carbon.");
		if (planet.getPopulation() < populationCost) throw new RunningGameCommandException("Not enough population.");

		Player owner = getPlayer(playerLogin);
		if (owner == null) throw new RunningGameCommandException("Unknown player name '" + playerLogin + "'");

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
		
		playersPolicies.get(playerLogin).update(newDiplomacy);
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
}
