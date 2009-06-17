package server.model;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.axan.eplib.utils.Basic;

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
	
	
	private IBuilding getBuildingFromClientType(ProductiveCelestialBody productiveCelestialBody, Class<? extends common.IBuilding> clientBuildingType)
	{
		for(IBuilding b : productiveCelestialBody.getBuildings())
		{
			if (b.getClass().getSimpleName().compareTo(clientBuildingType.getSimpleName()) == 0)
			{
				return b;
			}
		}
		
		return null;
	}	
	
	public boolean canDemolish(common.Player player, String celestialBodyName, Class<? extends common.IBuilding> buildingType)
	{
		ICelestialBody celestialBody = getCelestialBody(celestialBodyName);
		if (celestialBody == null) throw new IllegalArgumentException("Celestial body '"+celestialBodyName+"' does not exist.");
		
		// If celestial body is not a productive one.
		if (!ProductiveCelestialBody.class.isInstance(celestialBody)) return false;
		ProductiveCelestialBody productiveCelestialBody = ProductiveCelestialBody.class.cast(celestialBody);
		
		// If player is not the celestial body owner.
		if (productiveCelestialBody.getOwner() == null || !productiveCelestialBody.getOwner().isNamed(player.getName())) return false;
		
		IBuilding building = getBuildingFromClientType(productiveCelestialBody, buildingType);
		
		// If no building of this type exist.
		if (building == null) return false;
		
		// Building type check		
		if (GovernmentModule.class.isInstance(building) || PulsarLauchingPad.class.isInstance(building))
		{
			return false;
		}
		else
		{
			return true;
		}
	}

	public boolean canEmbarkGovernment(common.Player player)
	{
		Area area = locateGovernmentModule(player.getName());
		
		// If player has no government module.
		if (area == null) return false;
		
		ICelestialBody celestialBody = area.getCelestialBody();
		if (celestialBody == null) return false; // Unexpected
		if (!Planet.class.isInstance(celestialBody)) return false; // Unexpected
		Planet planet = Planet.class.cast(celestialBody);
		
		int carbonCost = common.GovernmentStarship.PRICE_CARBON;
		int populationCost = common.GovernmentStarship.PRICE_POPULATION;
		
		if (carbonCost > planet.getCarbon()) return false;
		if (populationCost > planet.getPopulation()) return false;
		
		return true;
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
		
		IBuilding building = getBuildingFromClientType(productiveCelestialBody, common.PulsarLauchingPad.class);
		
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
		final IBuilding existingBuilding;
		final IBuilding newBuilding;
		final int carbonCost;
		final int populationCost;
		
		public BuildCheckResult(ProductiveCelestialBody productiveCelestialBody, IBuilding building, int carbonCost, int populationCost, IBuilding newBuilding)
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

		IBuilding building = getBuildingFromClientType(productiveCelestialBody, buildingType);
		IBuilding newBuilding;
		
		if (common.DefenseModule.class.equals(buildingType))
		{
			if (building != null)
			{
				DefenseModule defenseModule = DefenseModule.class.cast(building);
				carbonCost = defenseModule.getNextBuildCost();
				newBuilding = defenseModule.getUpgradedBuilding();
			}
			else
			{
				carbonCost = common.DefenseModule.FIRST_BUILD_COST;
				newBuilding = new DefenseModule(1);
			}
			populationCost = 0;
		}
		else if (common.ExtractionModule.class.equals(buildingType))
		{
			if (building != null)
			{
				ExtractionModule extractionModule = ExtractionModule.class.cast(building);
				carbonCost = extractionModule.getNextBuildCost();
				newBuilding = extractionModule.getUpgradedBuilding();
			}
			else
			{
				carbonCost = common.ExtractionModule.FIRST_BUILD_COST;
				newBuilding = new ExtractionModule(1);
			}
			populationCost = 0;
		}
		else if (common.GovernmentModule.class.equals(buildingType))
		{
			if (building != null)
			{
				throw new CelestialBodyBuildException("Government module already built on '"+celestialBodyName+"'");
			}
			else
			{
				if (!Planet.class.isInstance(productiveCelestialBody)) throw new CelestialBodyBuildException("Government can only be build on planet, '"+celestialBodyName+"' is not a planet.");
				
				carbonCost = 0;
				populationCost = 0;
				
				newBuilding = new GovernmentModule();
			}
		}
		else if (common.PulsarLauchingPad.class.equals(buildingType))
		{
			if (!Planet.class.isInstance(productiveCelestialBody)) throw new CelestialBodyBuildException("PulsarLaunchingPad can only be build on planet, '"+celestialBodyName+"' is not a planet.");
			
			carbonCost = common.PulsarLauchingPad.PRICE_CARBON;
			populationCost = common.PulsarLauchingPad.PRICE_POPULATION;
			
			if (building != null)
			{
				PulsarLauchingPad pulsarLaunchingPad = PulsarLauchingPad.class.cast(building);
				newBuilding = pulsarLaunchingPad.getUpgradedBuilding();
			}
			else
			{
				newBuilding = new PulsarLauchingPad(1, 0);
			}
		}
		else if (common.SpaceCounter.class.equals(buildingType))
		{
			carbonCost = common.SpaceCounter.PRICE;
			populationCost = 0;
			
			if (building != null)
			{
				SpaceCounter spaceCounter = SpaceCounter.class.cast(building);
				newBuilding = spaceCounter.getUpgradedBuilding();
			}
			else
			{
				newBuilding = new SpaceCounter(1);
			}
			
		}
		else if (common.StarshipPlant.class.equals(buildingType))
		{
			if (building != null)
			{
				throw new CelestialBodyBuildException("StarshipPlant module already built on '"+celestialBodyName+"'");
			}
			else
			{
				if (!Planet.class.isInstance(productiveCelestialBody)) throw new CelestialBodyBuildException("StarshipPlant can only be build on planet, '"+celestialBodyName+"' is not a planet.");
			
				carbonCost = common.StarshipPlant.PRICE_CARBON;
				populationCost = common.StarshipPlant.PRICE_POPULATION;
				
				newBuilding = new StarshipPlant();
			}
		}
		else
		{
			throw new CelestialBodyBuildException("Unknown building type : "+buildingType);
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
}
