/**
 * @author Escallier Pierre
 * @file GameTurnInfos.java
 * @date 2 juin 2009
 */
package common;

import java.io.Serializable;

/**
 * Represent the game board at a specific turn for a specific player.
 * It provide informations about the universe and the last turn resolution.
 */
public class PlayerGameBoard implements Serializable
{
	private static final long	serialVersionUID	= 1L;
	
	/** 3 dimensional array of universe area. */
	private final Area[][][] universe;
	
	/** Sun location. Sun always fills 9 area. */
	private final int[] sunLocation;
	
	private final int date;
	
	// TODO : add last turn resolution informations.

	/**
	 * Full constructor.
	 */
	public PlayerGameBoard(Area[][][] universe, int[] sunLocation, int date)
	{
		this.universe = universe;
		this.sunLocation = sunLocation;
		this.date = date;
	}
	
	public int getDimX()
	{
		return universe.length;
	}
	
	public int getDimY()
	{
		return universe[0].length;
	}
	
	public int getDimZ()
	{
		return universe[0][0].length;
	}
	
	public Area getArea(int x, int y, int z)
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
}
