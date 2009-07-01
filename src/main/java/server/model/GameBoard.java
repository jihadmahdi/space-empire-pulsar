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
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.axan.eplib.utils.Basic;

import client.gui.RunningGamePanel;

import common.GovernmentStarship;
import common.IStarship;
import common.Player;
import common.Protocol;
import common.Protocol.ServerRunningGame.RunningGameCommandException;

import server.SEPServer;
import server.model.Area.AreaIllegalDefinitionException;
import server.model.ProductiveCelestialBody.CelestialBodyBuildException;

public class GameBoard implements Serializable
{
	private static final long	serialVersionUID	= 1L;

	private static final Logger			log	= SEPServer.log;

	private static final Random			rnd	= new Random();

	private final Set<common.Player>			players;

	private final Area[][][]			universe;

	private final int[]					sunLocation;			// Sun center location : [0] x; [1] y; [2] z. Sun is always fill 9 area.

	private final common.GameConfig			config;
	
	private int date;

	
	private GameBoard(Set<common.Player> players, common.GameConfig config, int date, Area[][][] universe, int[] sunLocation)
	{
		this.players = players;
		this.config = config;
		this.date = date;
		this.universe = universe;
		this.sunLocation = sunLocation;
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
		sunLocation = new int[] {(int) config.getDimX() / 2, (int) config.getDimY() / 2, (int) config.getDimZ() / 2};
			
		for(int x = -Math.min(config.getSunRadius(), config.getDimX()/2); x <= Math.min(config.getSunRadius(), config.getDimX()/2); ++x)
		for(int y = -Math.min(config.getSunRadius(), config.getDimY()/2); y <= Math.min(config.getSunRadius(), config.getDimY()/2); ++y)
		for(int z = -Math.min(config.getSunRadius(), config.getDimZ()/2); z <= Math.min(config.getSunRadius(), config.getDimZ()/2); ++z)
		{
			if (common.SEPUtils.getDistance(new int[]{sunLocation[0]+x, sunLocation[1]+y, sunLocation[2]+z}, sunLocation) <= config.getSunRadius())
			{
				getArea(sunLocation[0]+x, sunLocation[1]+y, sunLocation[2]+z).setSunFlag(true);
			}
		}						

		// Add the players starting planets.
		Set<int[]> playersPlanetLocations = new HashSet<int[]>();
		
		for (common.Player player : players)
		{
			// Found a location to pop the planet.
			int[] planetLocation;
			boolean locationOk;
			do
			{
				locationOk = false;
				planetLocation = new int[] {rnd.nextInt(config.getDimX()), rnd.nextInt(config.getDimY()), rnd.nextInt(config.getDimZ())};
				
				if (universe[planetLocation[0]][planetLocation[1]][planetLocation[2]] != null && !universe[planetLocation[0]][planetLocation[1]][planetLocation[2]].isEmpty()) continue;
				
				locationOk = true;
				for(int[] l : playersPlanetLocations)
				{
					Stack<int[]> path = common.SEPUtils.getAllPathLoc(planetLocation[0], planetLocation[1], planetLocation[2], l[0], l[1], l[2]);
					for(int[] pl : path)
					{
						Area a = universe[pl[0]][pl[1]][pl[2]];
						if (a != null && a.isSun())
						{
							locationOk = false;
							break;
						}
					}
					
					if (!locationOk) break;
				}
			} while (!locationOk);

			Planet planet = Planet.newStartingPlanet(generateCelestialBodyName(), player, config);
			try
			{
				getArea(planetLocation).setCelestialBody(planet);
				playersPlanetLocations.add(planetLocation);
			}
			catch (AreaIllegalDefinitionException e)
			{
				throw new Error(e);
			}
		}

		// Add neutral celestial bodies
		for (int i = 0; i < config.getNeutralCelestialBodiesCount(); ++i)
		{
			// Found a location to pop the celestial body
			int[] celestialBodyLocation;
			do
			{
				celestialBodyLocation = new int[] {rnd.nextInt(config.getDimX()), rnd.nextInt(config.getDimY()), rnd.nextInt(config.getDimZ())};
			} while ( universe[celestialBodyLocation[0]][celestialBodyLocation[1]][celestialBodyLocation[2]] != null && !universe[celestialBodyLocation[0]][celestialBodyLocation[1]][celestialBodyLocation[2]].isEmpty());

			Class<? extends common.ICelestialBody> celestialBodyType = Basic.getKeyFromRandomTable(config.getNeutralCelestialBodiesGenerationTable());

			try
			{
				Class<? extends ICelestialBody> serverCelestialBodyType = Class.forName("server.model." + celestialBodyType.getSimpleName()).asSubclass(ICelestialBody.class);
				Constructor<? extends ICelestialBody> ctor = serverCelestialBodyType.getConstructor(String.class, common.GameConfig.class);
				ICelestialBody celestialBody = ctor.newInstance(generateCelestialBodyName(), config);
				getArea(celestialBodyLocation).setCelestialBody(celestialBody);
			}
			catch (Exception e)
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
	
	private Area getArea(int[] location)
	{
		return getArea(location[0], location[1], location[2]);
	}
	
	private Area getArea(int x, int y, int z)
	{
		if (universe[x][y][z] == null)
		{
			universe[x][y][z] = new Area();
		}
		return universe[x][y][z];
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

		Map<int[], Set<Probe>> playerProbes = getUnits(Probe.class, playerLogin);
		
		boolean isVisible = false;

		for (int x = 0; x < config.getDimX(); ++x)
			for (int y = 0; y < config.getDimY(); ++y)
				for (int z = 0; z < config.getDimZ(); ++z)
				{					
					Area area = universe[x][y][z];
					int[] location = new int[]{x, y, z};

					// Check for Area visibility (default to false)
					isVisible = false;					
					
					// Visible if area celestial body is owned by the player.
					if (!isVisible && area != null && area.getCelestialBody() != null && area.getCelestialBody().getOwner() != null && area.getCelestialBody().getOwner().isNamed(playerLogin)) isVisible = true;

					// Visible if area contains a celestial body and player has a unit on it.
					if (!isVisible && area != null && area.getCelestialBody() != null && !getUnits(location, playerLogin).isEmpty()) isVisible = true;

					// Area is under a player probe scope.
					if (!isVisible) for(Map.Entry<int[], Set<Probe>> e : playerProbes.entrySet())
					{
						int[] probesLocation = e.getKey();
						if (common.SEPUtils.getDistance(location, probesLocation) > config.getProbeScope()) continue;
						for(Probe p : e.getValue())
						{
							if (p.isDeployed())
							{
								isVisible = true;
								break;
							}
						}
						
						if (isVisible) break;
					}
					
					if (isVisible || area != null)
					{						
						playerUniverseView[x][y][z] = getArea(x, y, z).getPlayerView(getDate(), playerLogin, isVisible);
					}
				}

		return new common.PlayerGameBoard(playerUniverseView, sunLocation, date);
	}		
	
	/**
	 * Return the unit location by its name and owner.
	 * @param playerLogin
	 * @param unitName
	 * @return
	 */
	private <U extends Unit> int[] getUnitLocation(String playerLoginFilter, Class<U> unitTypeFilter, String unitName)
	{
		for (int x = 0; x < config.getDimX(); ++x)
			for (int y = 0; y < config.getDimY(); ++y)
				for (int z = 0; z < config.getDimZ(); ++z)
				{
					if (universe[x][y][z] == null) continue;
					
					int[] location = new int[]{x, y, z};
										
					Set<U> currentLocationUnits = getUnits(location, unitTypeFilter, playerLoginFilter);
					
					for(Unit unit : currentLocationUnits)
					{
						if (unit.getName().compareTo(unitName) == 0) return location;
					}
				}
		
		return null;
	}
	
	/**
	 * Return the unit by its name, type, and owner.
	 * @param <U>
	 * @param unitType
	 * @param playerLogin
	 * @param unitName
	 * @return
	 */
	private <U extends Unit> U getUnit(Class<U> unitType, String playerLogin, String unitName)
	{
		for(Set<U> set : getUnits(unitType, playerLogin).values())
		{
			for(U unit : set)
			{
				if (unit.getName().compareTo(unitName) == 0) return unit;
			}
		}
		
		return null;
	}
	
	/**
	 * Return all units that implement a given unit sub class.
	 * @param unitTypeFilter if not null, return only units that implement this unit sub class.
	 * @param playerLoginFilter if not null, return only units owned by this player.
	 * @return Map<int[], Set<U extends Unit>> filtered unit from the entire universe.
	 * @see #getUnits(int[], String, Class)
	 */
	private <U extends Unit> Map<int[], Set<U>> getUnits(Class<U> unitType, String playerLogin)
	{
		Map<int[], Set<U>> filteredUnits = new Hashtable<int[], Set<U>>();
		Set<U> currentLocationUnits;
		
		for (int x = 0; x < config.getDimX(); ++x)
			for (int y = 0; y < config.getDimY(); ++y)
				for (int z = 0; z < config.getDimZ(); ++z)
				{
					if (universe[x][y][z] == null) continue;
					
					int[] location = new int[]{x, y, z};
					currentLocationUnits = getUnits(location, unitType, playerLogin);
					
					if (currentLocationUnits.isEmpty()) continue;
					
					filteredUnits.put(location, currentLocationUnits);
				}
		
		return filteredUnits;
	}
	
	/**
	 * Return units that implement a given unit sub class and are located in a specific location .
	 * @param location Location to look units for.
	 * @param unitTypeFilter (cannot not be null) return only units that implement this unit sub class.
	 * @param playerLoginFilter if not null, return only units owned by this player.
	 * @return Set<Unit> filtered unit for the given location.
	 * @see #getUnits(String, Class)
	 */
	private <U extends Unit> Set<U> getUnits(int[] location, Class<U> unitTypeFilter, String playerLoginFilter)
	{
		Set<U> filteredUnits = new HashSet<U>();
		for(Unit u : getUnits(location, playerLoginFilter))
		{
			if (unitTypeFilter.isInstance(u))
			{
				filteredUnits.add(unitTypeFilter.cast(u));
			}
		}
		return filteredUnits;
	}
	
	/**
	 * Return all units. Optional filters can apply.
	 * @param playerLoginFilter if not null, return only units owned by this player.
	 * @return Map<int[], Set<Unit>> filtered unit from the entire universe.
	 * @see #getUnits(int[], String, Class)
	 */
	private Map<int[], Set<Unit>> getUnits(String playerLogin)
	{
		Map<int[], Set<Unit>> filteredUnits = new Hashtable<int[], Set<Unit>>();
		Set<Unit> currentLocationUnits;
		
		for (int x = 0; x < config.getDimX(); ++x)
			for (int y = 0; y < config.getDimY(); ++y)
				for (int z = 0; z < config.getDimZ(); ++z)
				{
					if (universe[x][y][z] == null) continue;
					
					int[] location = new int[]{x, y, z};
					currentLocationUnits = getUnits(location, playerLogin);
					
					if (currentLocationUnits.isEmpty()) continue;
					
					filteredUnits.put(location, currentLocationUnits);
				}
		
		return filteredUnits;
	}
	
	/**
	 * Return units from a specific location. Optional filters can apply.
	 * @param location Location to look units for.
	 * @param playerLoginFilter if not null, return only units owned by this player.
	 * @return
	 */
	private Set<Unit> getUnits(int[] location, String playerLoginFilter)
	{
		Set<Unit> filteredUnits = new HashSet<Unit>();
		
		Area area = universe[location[0]][location[1]][location[2]];
		if (area == null) return filteredUnits;
		
		Set<Unit> units = area.getUnits();
		
		for(Unit u : units)
		{
			if (playerLoginFilter != null && ((u.getOwner() == null) || (!u.getOwner().isNamed(playerLoginFilter)))) continue;
			
			filteredUnits.add(u);
		}
		
		return filteredUnits;
	}
	
	/**
	 * Merge all set into one (location info are lost).
	 * @param Map<int[], Set<T>> locatedObjects
	 * @return Set<T> single set with all objects.
	 */
	private <T> Set<T> asSingleSet(Map<int[], Set<T>> locatedObjects)
	{
		Set<T> allObjects = new HashSet<T>();
		for(Set<T> s : locatedObjects.values())
		{
			if (s == null || !s.isEmpty()) continue;
			
			allObjects.addAll(s);
		}
		return allObjects;
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
	 * @param celestialBodyName
	 * @return
	 */
	private int[] getCelestialBodyLocation(String celestialBodyName)
	{
		for (int x = 0; x < config.getDimX(); ++x)
			for (int y = 0; y < config.getDimY(); ++y)
				for (int z = 0; z < config.getDimZ(); ++z)
				{
					Area area = universe[x][y][z];
					if (area == null) continue;
					ICelestialBody celestialBody = area.getCelestialBody();
					if (celestialBody == null) continue;
					
					if (celestialBody.getName().compareTo(celestialBodyName) == 0) return new int[]{x, y, z};
				}
		return null;
	}
	
	private ICelestialBody getCelestialBody(String celestialBodyName)
	{
		int[] location = getCelestialBodyLocation(celestialBodyName);
		if (location == null) return null;
		return universe[location[0]][location[1]][location[2]].getCelestialBody();
	}
	
	/**
	 * Return the area where the given player government module is located.
	 * @param playerLogin
	 * @return
	 */
	private Area locateGovernmentModule(String playerLogin)
	{
		for (int x = 0; x < config.getDimX(); ++x)
			for (int y = 0; y < config.getDimY(); ++y)
				for (int z = 0; z < config.getDimZ(); ++z)
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
		final ProductiveCelestialBody productiveCelestialBody;
		final ABuilding existingBuilding;

		public DemolishCheckResult(ProductiveCelestialBody productiveCelestialBody, ABuilding building)
		{
			this.productiveCelestialBody = productiveCelestialBody;
			this.existingBuilding = building;
		}
	}

	public DemolishCheckResult checkDemolish(String playerLogin, String celestialBodyName, Class<? extends common.IBuilding> buildingType) throws RunningGameCommandException
	{
        ICelestialBody celestialBody = getCelestialBody(celestialBodyName);
		if (celestialBody == null) throw new RunningGameCommandException("Celestial body '"+celestialBodyName+"' does not exist.");

		// If celestial body is not a productive one.
		if (!ProductiveCelestialBody.class.isInstance(celestialBody)) throw new RunningGameCommandException("Celestial body '"+celestialBodyName+"' is not a productive one.");
		ProductiveCelestialBody productiveCelestialBody = ProductiveCelestialBody.class.cast(celestialBody);

		// If player is not the celestial body owner.
		if (productiveCelestialBody.getOwner() == null || !productiveCelestialBody.getOwner().isNamed(playerLogin)) throw new RunningGameCommandException("Player '"+playerLogin+"' is not the '"+celestialBodyName+"' celestial body owner.");

        ABuilding building = getBuildingFromClientType(productiveCelestialBody, buildingType);

		// If no building of this type exist.
		if (building == null || building.getBuildSlotsCount() == 0) throw new RunningGameCommandException("No building type '"+buildingType.getSimpleName()+"' built yet.");

		if (!building.canDowngrade()) throw new RunningGameCommandException("Cannot demolish building type '"+buildingType.getSimpleName()+"'");				

		return new DemolishCheckResult(productiveCelestialBody, building);
	}

	public void embarkGovernment(String playerLogin) throws RunningGameCommandException
	{
		EmbarkGovernmentCheckResult embarkGovernmentCheckResult = checkEmbarkGovernment(playerLogin);		
		embarkGovernmentCheckResult.planet.removeBuilding(GovernmentModule.class);
		Map<Class<? extends IStarship>, Integer> starshipToMake = new HashMap<Class<? extends IStarship>, Integer>();
		starshipToMake.put(GovernmentStarship.class, 1);
		embarkGovernmentCheckResult.planet.mergeToUnasignedFleet(getPlayer(playerLogin), starshipToMake);
		embarkGovernmentCheckResult.planet.setCarbon(embarkGovernmentCheckResult.planet.getCarbon()-embarkGovernmentCheckResult.carbonCost);
		embarkGovernmentCheckResult.planet.setPopulation(embarkGovernmentCheckResult.planet.getPopulation()-embarkGovernmentCheckResult.populationCost);
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
		final Planet planet;
		final int carbonCost;
		final int populationCost;
		final GovernmentModule governmentModule;
		final StarshipPlant starshipPlant;

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
		if (area == null) throw new RunningGameCommandException("Cannot locate '"+playerLogin+"' government module.");
		
		ICelestialBody celestialBody = area.getCelestialBody();
		if (celestialBody == null) throw new RunningGameCommandException("Cannot locate '"+playerLogin+"' government module celestial body (unexpected error).");
		if (!Planet.class.isInstance(celestialBody)) throw new RunningGameCommandException("Cannot locate '"+playerLogin+"' government module planet (unexpected error).");
		Planet planet = Planet.class.cast(celestialBody);

		// If player is not the celestial body owner.
		if (planet.getOwner() == null || !planet.getOwner().isNamed(playerLogin)) throw new RunningGameCommandException("Player '"+playerLogin+"' is not the planet owner (unexpected error).");
		
		GovernmentModule governmentModule = getBuilding(planet, GovernmentModule.class);
		
		if (governmentModule == null) throw new RunningGameCommandException("No government module on the planet '"+planet.getName()+"' (unexpected error)");
		
		StarshipPlant starshipPlant = getBuilding(planet, StarshipPlant.class);
		
		// If planet has no starshipPlant.
		if (starshipPlant == null) throw new RunningGameCommandException("No starship plant on planet '"+planet.getName()+"'");
		
		// If starship has just been build this turn.
		if (starshipPlant.getLastBuildDate() >= date) throw new RunningGameCommandException("Starship plant is still in construction.");				
		
		int carbonCost = common.GovernmentStarship.PRICE_CARBON;
		int populationCost = common.GovernmentStarship.PRICE_POPULATION;
		
		if (carbonCost > planet.getCarbon()) throw new RunningGameCommandException("Not enough carbon.");;
		if (populationCost > planet.getPopulation()) throw new RunningGameCommandException("Not enough population.");;;
		
		return new EmbarkGovernmentCheckResult(planet, governmentModule, starshipPlant, carbonCost, populationCost);
	}

	public boolean canFirePulsarMissile(common.Player player, String celestialBodyName)
	{
		ICelestialBody celestialBody = getCelestialBody(celestialBodyName);
		if (celestialBody == null) throw new IllegalArgumentException("Celestial body '"+celestialBodyName+"' does not exist.");
		
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
		int[] celestialBodyLocation = getCelestialBodyLocation(planetName);
		if (celestialBodyLocation == null) throw new IllegalArgumentException("Celestial body '"+planetName+"' does not exist.");
		
		ICelestialBody celestialBody = universe[celestialBodyLocation[0]][celestialBodyLocation[1]][celestialBodyLocation[2]].getCelestialBody();
		
		// If celestial body is not a productive one.
		if (!Planet.class.isInstance(celestialBody)) return false;
		Planet planet = Planet.class.cast(celestialBody);
		
		// If player is not the celestial body owner.
		if (planet.getOwner() == null || !planet.getOwner().isNamed(player.getName())) return false;
		
		// Check if government fleet is on the planet.
		for(Fleet f : getUnits(celestialBodyLocation, Fleet.class, player.getName()))
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
		final ProductiveCelestialBody productiveCelestialBody;
		final ABuilding existingBuilding;
		final ABuilding newBuilding;
		final int carbonCost;
		final int populationCost;
		
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
		if (celestialBody == null) throw new CelestialBodyBuildException("Celestial body '"+celestialBodyName+"' does not exist.");
		
		// If celestial body is not a productive one.
		if (!ProductiveCelestialBody.class.isInstance(celestialBody)) throw new CelestialBodyBuildException("Celestial body '"+celestialBodyName+"' is not a productive one.");
		ProductiveCelestialBody productiveCelestialBody = ProductiveCelestialBody.class.cast(celestialBody);
		
		// If player is not the celestial body owner.
		if (productiveCelestialBody.getOwner() == null || !productiveCelestialBody.getOwner().isNamed(playerLogin)) throw new CelestialBodyBuildException("Player '"+playerLogin+"' is not the '"+celestialBodyName+"' celestial body owner.");
		
		// If this productive celestial body build was already used this turn.
		if (productiveCelestialBody.getLastBuildDate() >= date) throw new CelestialBodyBuildException("Celestial body '"+celestialBodyName+"' already in work for this turn.");
		
		// If there is no more free slots.
		if (productiveCelestialBody.getFreeSlotsCount() < 1) throw new CelestialBodyBuildException("No more free slots on celestial body '"+celestialBodyName+"'");
		
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
			if (!Planet.class.isInstance(productiveCelestialBody)) throw new CelestialBodyBuildException("Only planet can afford population costs, '"+celestialBodyName+"' is not a planet.");;
			Planet planet = Planet.class.cast(productiveCelestialBody);
			if (populationCost > planet.getPopulation()) throw new CelestialBodyBuildException("Not enough population.");
		}
		
		return new BuildCheckResult(productiveCelestialBody, building, carbonCost, populationCost, newBuilding);		
	}


	public void resolveCurrentTurn()
	{
		// TODO : Résolve mobile units movement, attacks, etc... On Current instance.
		++date;
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
		formFleetCheckResult.area.addUnit(formFleetCheckResult.newFleet);
		formFleetCheckResult.productiveCelestialBody.removeFromUnasignedFleet(getPlayer(playerLogin), fleetToForm);
	}
	
	private static class FormFleetCheckResult
	{
		final Area area;
		final ProductiveCelestialBody productiveCelestialBody;
		final Fleet newFleet;

		public FormFleetCheckResult(Area area, ProductiveCelestialBody productiveCelestialBody, Fleet newFleet)
		{
			this.area = area;
			this.productiveCelestialBody = productiveCelestialBody;
			this.newFleet = newFleet;
		}		
	}
	
	private FormFleetCheckResult checkFormFleet(String playerLogin, String planetName, String fleetName, Map<Class<? extends common.IStarship>, Integer> fleetToForm) throws RunningGameCommandException
	{
		int[] location = getCelestialBodyLocation(planetName);
		Area area = universe[location[0]][location[1]][location[2]];
		
		if (area == null) throw new RunningGameCommandException("Celestial body '"+planetName+"' does not exist.");
		ICelestialBody celestialBody = area.getCelestialBody();
		
		if (celestialBody == null) throw new RunningGameCommandException("Celestial body '"+planetName+"' does not exist.");
		
		// If celestial body is not a productive one.
		if (!ProductiveCelestialBody.class.isInstance(celestialBody)) throw new RunningGameCommandException("Celestial body '"+planetName+"' is not a productive celestial body.");
		ProductiveCelestialBody productiveCelestialBody = ProductiveCelestialBody.class.cast(celestialBody);
		
		Fleet unasignedFleet = productiveCelestialBody.getUnasignedFleet(playerLogin);
		if (unasignedFleet == null) throw new RunningGameCommandException("No available unasigned fleet on celestial body '"+celestialBody.getName()+"'");
		
		Fleet fleet = getUnit(Fleet.class, playerLogin, fleetName);
		if (fleet != null) throw new RunningGameCommandException("Fleet named '"+fleetName+"' already exist.");
		
		// Starship availability check		
		for(Entry<Class<? extends IStarship>, Integer> e : fleetToForm.entrySet())
		{
			if (e.getValue() <= 0) continue;
			
			int qt = e.getValue();
			if (!unasignedFleet.getComposition().containsKey(e.getKey())) throw new RunningGameCommandException("Unasigned fleet does not have required starship type '"+e.getKey().getSimpleName()+"'");
			if (unasignedFleet.getComposition().get(e.getKey()) < qt) throw new RunningGameCommandException("Unasigned flee does not have enough starship type '"+e.getKey()+"'");
		}
				
		Player owner = getPlayer(playerLogin);
		if (owner == null) throw new RunningGameCommandException("Unknown player name '"+playerLogin+"'");
		
		Fleet newFleet = new Fleet(fleetName, owner, fleetToForm, false);
		
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
		dismantleFleetCheckResult.area.removeUnit(dismantleFleetCheckResult.fleet);
		dismantleFleetCheckResult.productiveCelestialBody.mergeToUnasignedFleet(getPlayer(playerLogin), dismantleFleetCheckResult.fleet.getComposition());
	}
	
	private static class DismantleFleetCheckResult
	{
		final Area area;
		final ProductiveCelestialBody productiveCelestialBody;
		final Fleet fleet;

		public DismantleFleetCheckResult(Area area, ProductiveCelestialBody productiveCelestialBody, Fleet fleet)
		{
			this.area = area;
			this.productiveCelestialBody = productiveCelestialBody;
			this.fleet = fleet;
		}		
	}
	
	private DismantleFleetCheckResult checkDismantleFleet(String playerLogin, String fleetName) throws RunningGameCommandException
	{
		int[] location = getUnitLocation(playerLogin, Fleet.class, fleetName);
		Area area = universe[location[0]][location[1]][location[2]];
		
		if (area == null) throw new RunningGameCommandException("Fleet '"+fleetName+"' does not exist.");
		ICelestialBody celestialBody = area.getCelestialBody();
		
		if (celestialBody == null) throw new RunningGameCommandException("Fleet is in travel.");
		
		// If celestial body is not a ProductiveCelestialBody.
		if (!ProductiveCelestialBody.class.isInstance(celestialBody)) throw new RunningGameCommandException("Fleet is not on a ProductiveCelestialBody.");
		ProductiveCelestialBody productiveCelestialBody = ProductiveCelestialBody.class.cast(celestialBody);
		
		Fleet fleet = getUnit(Fleet.class, playerLogin, fleetName);
		if (fleet == null) throw new RunningGameCommandException("Fleet '"+fleetName+"' does not exist.");
		
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
			makeProbesCheckResult.area.addUnit(p);
		}
		makeProbesCheckResult.planet.setCarbon(makeProbesCheckResult.planet.getCarbon()-makeProbesCheckResult.carbonCost);
		makeProbesCheckResult.planet.setPopulation(makeProbesCheckResult.planet.getPopulation()-makeProbesCheckResult.populationCost);
	}
	
	private static class MakeProbesCheckResult
	{
		final Area area;
		final Planet planet;
		final int carbonCost;
		final int populationCost;
		final StarshipPlant starshipPlant;
		final Set<Probe> newProbes;

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
		int[] location = getCelestialBodyLocation(planetName);
		Area area = universe[location[0]][location[1]][location[2]];
		
		if (area == null) throw new RunningGameCommandException("Celestial body '"+planetName+"' does not exist.");
		ICelestialBody celestialBody = area.getCelestialBody();
		
		if (celestialBody == null) throw new RunningGameCommandException("Celestial body '"+planetName+"' does not exist.");
		
		// If celestial body is not a planet.
		if (!Planet.class.isInstance(celestialBody)) throw new RunningGameCommandException("Celestial body '"+planetName+"' is not a planet.");
		Planet planet = Planet.class.cast(celestialBody);
		
		// If player is not the celestial body owner.
		if (planet.getOwner() == null || !planet.getOwner().isNamed(playerLogin)) throw new RunningGameCommandException("Player '"+playerLogin+"' is not the '"+planetName+"' planet owner.");
		
		StarshipPlant starshipPlant = getBuilding(planet, StarshipPlant.class);
		
		// If planet has no starshipPlant.
		if (starshipPlant == null) throw new RunningGameCommandException("No starship plant on planet '"+planetName+"'");
		
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
		if (owner == null) throw new RunningGameCommandException("Unknown player name '"+playerLogin+"'");
		
		if (getUnit(Probe.class, playerLogin, probeName) != null || getUnit(Probe.class, playerLogin, probeName+"1") != null)
		{
			throw new RunningGameCommandException("Probe serial '"+probeName+"' already exist.");
		}
		
		Set<Probe> newProbes = new HashSet<Probe>();
		for(int i=0; i<quantity; ++i)
		{
			newProbes.add(new Probe(probeName+i, owner, false));
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
			makeAntiProbeMissilesCheckResult.area.addUnit(p);
		}
		makeAntiProbeMissilesCheckResult.planet.setCarbon(makeAntiProbeMissilesCheckResult.planet.getCarbon()-makeAntiProbeMissilesCheckResult.carbonCost);
		makeAntiProbeMissilesCheckResult.planet.setPopulation(makeAntiProbeMissilesCheckResult.planet.getPopulation()-makeAntiProbeMissilesCheckResult.populationCost);
	}
	
	private static class MakeAntiProbeMissilesCheckResult
	{
		final Area area;
		final Planet planet;
		final int carbonCost;
		final int populationCost;
		final StarshipPlant starshipPlant;
		final Set<AntiProbeMissile> newAntiProbeMissiles;

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
		int[] location = getCelestialBodyLocation(planetName);
		Area area = universe[location[0]][location[1]][location[2]];
		
		if (area == null) throw new RunningGameCommandException("Celestial body '"+planetName+"' does not exist.");
		ICelestialBody celestialBody = area.getCelestialBody();
		
		if (celestialBody == null) throw new RunningGameCommandException("Celestial body '"+planetName+"' does not exist.");
		
		// If celestial body is not a planet.
		if (!Planet.class.isInstance(celestialBody)) throw new RunningGameCommandException("Celestial body '"+planetName+"' is not a planet.");
		Planet planet = Planet.class.cast(celestialBody);
		
		// If player is not the celestial body owner.
		if (planet.getOwner() == null || !planet.getOwner().isNamed(playerLogin)) throw new RunningGameCommandException("Player '"+playerLogin+"' is not the '"+planetName+"' planet owner.");
		
		StarshipPlant starshipPlant = getBuilding(planet, StarshipPlant.class);
		
		// If planet has no starshipPlant.
		if (starshipPlant == null) throw new RunningGameCommandException("No starship plant on planet '"+planetName+"'");
		
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
		if (owner == null) throw new RunningGameCommandException("Unknown player name '"+playerLogin+"'");
		
		if (getUnit(Probe.class, playerLogin, antiProbeMissileName) != null || getUnit(Probe.class, playerLogin, antiProbeMissileName+"1") != null)
		{
			throw new RunningGameCommandException("AntiProbeMissile serial '"+antiProbeMissileName+"' already exist.");
		}
		
		Set<AntiProbeMissile> newAntiProbeMissiles = new HashSet<AntiProbeMissile>();
		for(int i=0; i<quantity; ++i)
		{
			newAntiProbeMissiles.add(new AntiProbeMissile(antiProbeMissileName+i, owner, false));
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
		final Planet planet;
		final int carbonCost;
		final int populationCost;
		final StarshipPlant starshipPlant;

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
	
		if (celestialBody == null) throw new RunningGameCommandException("Celestial body '"+planetName+"' does not exist.");
		
		// If celestial body is not a planet.
		if (!Planet.class.isInstance(celestialBody)) throw new RunningGameCommandException("Celestial body '"+planetName+"' is not a planet.");
		Planet planet = Planet.class.cast(celestialBody);
		
		// If player is not the celestial body owner.
		if (planet.getOwner() == null || !planet.getOwner().isNamed(playerLogin)) throw new RunningGameCommandException("Player '"+playerLogin+"' is not the '"+planetName+"' planet owner.");
		
		StarshipPlant starshipPlant = getBuilding(planet, StarshipPlant.class);
		
		// If planet has no starshipPlant.
		if (starshipPlant == null) throw new RunningGameCommandException("No starship plant on planet '"+planetName+"'");
		
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
				throw new RunningGameCommandException("Implementation error : Price are not defined for Starship class '"+e.getKey().getSimpleName()+"'");
			}
			
			carbonCost += carbonPrice * e.getValue();
			populationCost += populationPrice * e.getValue();			
		}
		
		if (carbonCost == 0 && populationCost == 0) throw new RunningGameCommandException("Seems like no starships are selected (cost is null).");
		
		if (planet.getCarbon() < carbonCost) throw new RunningGameCommandException("Not enough carbon.");
		if (planet.getPopulation() < populationCost) throw new RunningGameCommandException("Not enough population.");				
		
		Player owner = getPlayer(playerLogin);
		if (owner == null) throw new RunningGameCommandException("Unknown player name '"+playerLogin+"'");
		
		return new MakeStarshipsCheckResult(planet, carbonCost, populationCost, starshipPlant);		
	}
	
}
