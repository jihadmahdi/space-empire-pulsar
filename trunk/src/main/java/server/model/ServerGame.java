/**
 * @author Escallier Pierre
 * @file ServerGame.java
 * @date 1 juin 2009
 */
package server.model;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.axan.eplib.utils.Basic;

import server.SEPServer;
import server.model.Area.AreaIllegalDefinitionException;

import common.GameConfig;
import common.PlayerGameBoard;
import common.Player;
import common.SEPUtils;

/**
 * Represent a running game at a specific turn. It also provide previous turns archives.
 */
public class ServerGame
{
	//SUIS LA : getGameBoard(playerLogin) BCP TROP LONG ! Repenser la structure (passer par une SGBD intégré ?).
	
	private static final Logger			log	= SEPServer.log;

	private static final Random			rnd	= new Random();

	private final Area[][][]			universe;

	private final int[]					sunLocation;			// Sun center location : [0] x; [1] y; [2] z. Sun is always fill 9 area.

	private final Set<Player>			players;

	private final Stack<ServerGameTurn>	turns;

	private final GameConfig			config;

	/**
	 * Full new game constructor.
	 * 
	 * @param playerList
	 * @param gameConfig
	 */
	public ServerGame(Set<Player> players, GameConfig config)
	{
		this.config = config;
		this.players = players;
		this.turns = new Stack<ServerGameTurn>();

		// Create a blank universe.
		universe = new Area[config.getDimX()][config.getDimY()][config.getDimZ()];

		// Make the sun
		sunLocation = new int[] {(int) config.getDimX() / 2, (int) config.getDimY() / 2, (int) config.getDimZ() / 2};
			
		for(int x = -Math.min(config.getSunRadius(), config.getDimX()-1); x <= Math.min(config.getSunRadius(), config.getDimX()-1); ++x)
		for(int y = -Math.min(config.getSunRadius(), config.getDimY()-1); y <= Math.min(config.getSunRadius(), config.getDimY()-1); ++y)
		for(int z = -Math.min(config.getSunRadius(), config.getDimZ()-1); z <= Math.min(config.getSunRadius(), config.getDimZ()-1); ++z)
		{			
			if (SEPUtils.getDistance(new int[]{sunLocation[0]+x, sunLocation[1]+y, sunLocation[2]+z}, sunLocation) <= config.getSunRadius())
			{
				getArea(sunLocation[0]+x, sunLocation[1]+y, sunLocation[2]+z).setSunFlag(true);
			}
		}						

		// Add the players starting planets.
		for (Player player : players)
		{
			// Found a location to pop the planet.
			int[] planetLocation;
			do
			{
				planetLocation = new int[] {rnd.nextInt(config.getDimX()), rnd.nextInt(config.getDimY()), rnd.nextInt(config.getDimZ())};
			} while ( universe[planetLocation[0]][planetLocation[1]][planetLocation[2]] != null && !universe[planetLocation[0]][planetLocation[1]][planetLocation[2]].isEmpty());

			Planet planet = Planet.newStartingPlanet(generateCelestialBodyName(), player, config);
			try
			{
				getArea(planetLocation).setCelestialBody(planet);
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
				Constructor<? extends ICelestialBody> ctor = serverCelestialBodyType.getConstructor(String.class, GameConfig.class);
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
		return turns.size();
	}

	/**
	 * @param playerLogin
	 */
	public PlayerGameBoard getGameBoard(String playerLogin)
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
						if (SEPUtils.getDistance(location, probesLocation) > config.getProbeScope()) continue;
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

		return new PlayerGameBoard(playerUniverseView, sunLocation);
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
}
