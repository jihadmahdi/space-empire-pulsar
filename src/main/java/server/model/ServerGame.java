/**
 * @author Escallier Pierre
 * @file ServerGame.java
 * @date 1 juin 2009
 */
package server.model;

import java.lang.reflect.Constructor;
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

/**
 * Represent a running game at a specific turn.
 * It also provide previous turns archives.
 */
public class ServerGame
{
	private static final Logger log = SEPServer.log;
	
	private static final Random rnd = new Random();
	
	private final Area[][][] universe;
	private final int[] sunLocation; // Sun center location : [0] x; [1] y; [2] z. Sun is always fill 9 area.
	
	private final Set<Player> players;
	private final Stack<ServerGameTurn> turns;
	private final GameConfig config;
	
	/**
	 * Full new game constructor.
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
		for(int x = 0; x < universe.length; ++x)
		{
			for(int y = 0; y < universe[x].length; ++y)
			{
				for(int z=0; z < universe[x][y].length; ++z)
				{
					universe[x][y][z] = new Area();
				}
			}
		}
		
		// Make the sun
		sunLocation = new int[] {(int) config.getDimX()/2, (int) config.getDimY()/2, (int) config.getDimZ()/2};
		for(int x = 0; x < 3; ++x)
		for(int y = 0; y < 3; ++y)
		for(int z = 0; z < 3; ++z)
		{
			universe[sunLocation[0]-1+x][sunLocation[1]-1+y][sunLocation[2]-1+z].setSunFlag(true);
		}
		
		// Add the players starting planets.
		for(Player player : players)
		{			
			// Found a location to pop the planet.
			int[] planetLocation;
			do
			{
				planetLocation = new int[] {rnd.nextInt(config.getDimX()), rnd.nextInt(config.getDimY()), rnd.nextInt(config.getDimZ())};				
			}while(!universe[planetLocation[0]][planetLocation[1]][planetLocation[2]].isEmpty());
			
			Planet planet = Planet.newStartingPlanet(generateCelestialBodyName(), player, config);
			try
			{
				universe[planetLocation[0]][planetLocation[1]][planetLocation[2]].setCelestialBody(planet);
			}
			catch (AreaIllegalDefinitionException e)
			{
				throw new Error(e);
			}
		}
		
		// Add neutral celestial bodies
		for(int i = 0; i < config.getNeutralCelestialBodiesCount(); ++i)
		{
			// Found a location to pop the celestial body
			int[] celestialBodyLocation;
			do
			{
				celestialBodyLocation = new int[] {rnd.nextInt(config.getDimX()), rnd.nextInt(config.getDimY()), rnd.nextInt(config.getDimZ())};				
			}while(!universe[celestialBodyLocation[0]][celestialBodyLocation[1]][celestialBodyLocation[2]].isEmpty());
			
			Class<? extends common.ICelestialBody> celestialBodyType = Basic.getKeyFromRandomTable(config.getNeutralCelestialBodiesGenerationTable());
			
			try
			{
				Class<? extends ICelestialBody> serverCelestialBodyType = Class.forName("server.model."+celestialBodyType.getSimpleName()).asSubclass(ICelestialBody.class);				
				Constructor<? extends ICelestialBody> ctor = serverCelestialBodyType.getConstructor(String.class, GameConfig.class);
				ICelestialBody celestialBody = ctor.newInstance(generateCelestialBodyName(), config);
				universe[celestialBodyLocation[0]][celestialBodyLocation[1]][celestialBodyLocation[2]].setCelestialBody(celestialBody);
			}
			catch (Exception e)
			{
				throw new Error("Cannot create celestial body type "+celestialBodyType.getSimpleName()+" (not implemented server side ?)", e);
			}						
		}				
	}

	/**
	 * @param playerLogin
	 */
	public PlayerGameBoard getGameBoard(String playerLogin)
	{
		log.log(Level.INFO, "getGameBoard("+playerLogin+")");
		common.Area[][][] playerUniverseView = new common.Area[config.getDimX()][config.getDimY()][config.getDimZ()];
		// SUIS LA, Area intelligentes (avec réference vers l'univers, et conscience de leur position) ?
		return null;
	}
	
	private String nextCelestialBodyName = "A";
	private String generateCelestialBodyName()
	{
		String result = nextCelestialBodyName;
		if (nextCelestialBodyName.toLowerCase().charAt(nextCelestialBodyName.length()-1) == 'z')
		{
			nextCelestialBodyName += "a";
		}
		else
		{
			nextCelestialBodyName = nextCelestialBodyName.substring(0, nextCelestialBodyName.length()-1) + (char) (nextCelestialBodyName.charAt(nextCelestialBodyName.length()-1)+1);
		}
		return result;
	}
}
