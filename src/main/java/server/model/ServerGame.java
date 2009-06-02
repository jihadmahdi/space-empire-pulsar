/**
 * @author Escallier Pierre
 * @file ServerGame.java
 * @date 1 juin 2009
 */
package server.model;

import java.lang.reflect.Constructor;
import java.util.Random;
import java.util.Set;

import org.axan.eplib.utils.Basic;

import server.model.Area.AreaIllegalDefinitionException;

import common.CelestialBody;
import common.GameConfig;
import common.Player;

/**
 * 
 */
public class ServerGame
{
	private static final Random rnd = new Random();
	
	private final Area[][][] universe;
	private final int[] sunLocation; // Sun center location : [0] x; [1] y; [2] z. Sun is always fill 9 area.
	
	private final Set<Player> players;
	
	/**
	 * Full new game constructor.
	 * @param playerList
	 * @param gameConfig
	 */
	public ServerGame(Set<Player> players, GameConfig gameConfig)
	{
		this.players = players;
		
		// Create a blank universe.
		universe = new Area[gameConfig.getDimX()][gameConfig.getDimY()][gameConfig.getDimZ()];
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
		sunLocation = new int[] {(int) gameConfig.getDimX()/2, (int) gameConfig.getDimY()/2, (int) gameConfig.getDimZ()/2};
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
				planetLocation = new int[] {rnd.nextInt(gameConfig.getDimX()), rnd.nextInt(gameConfig.getDimY()), rnd.nextInt(gameConfig.getDimZ())};				
			}while(!universe[planetLocation[0]][planetLocation[1]][planetLocation[2]].isEmpty());
			
			Planet planet = Planet.newStartingPlanet(player, gameConfig);
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
		for(int i = 0; i < gameConfig.getNeutralCelestialBodiesCount(); ++i)
		{
			// Found a location to pop the celestial body
			int[] celestialBodyLocation;
			do
			{
				celestialBodyLocation = new int[] {rnd.nextInt(gameConfig.getDimX()), rnd.nextInt(gameConfig.getDimY()), rnd.nextInt(gameConfig.getDimZ())};				
			}while(!universe[celestialBodyLocation[0]][celestialBodyLocation[1]][celestialBodyLocation[2]].isEmpty());
			
			Class<? extends CelestialBody> celestialBodyType = Basic.getKeyFromRandomTable(gameConfig.getNeutralCelestialBodiesGenerationTable());
			
			try
			{
				Constructor<? extends CelestialBody> ctor = celestialBodyType.getConstructor(GameConfig.class);
				CelestialBody celestialBody = ctor.newInstance(gameConfig);
				universe[celestialBodyLocation[0]][celestialBodyLocation[1]][celestialBodyLocation[2]].setCelestialBody(celestialBody);
			}
			catch (Exception e)
			{
				throw new Error(e);
			}						
		}				
	}

}
