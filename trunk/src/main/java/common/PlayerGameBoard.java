/**
 * @author Escallier Pierre
 * @file GameTurnInfos.java
 * @date 2 juin 2009
 */
package common;

/**
 * Represent the game board at a specific turn for a specific player.
 * It provide informations about the universe and the last turn resolution.
 */
public class PlayerGameBoard
{
	/** 3 dimensional array of universe area. */
	private final Area[][][] universe;
	
	/** Sun location. Sun always fills 9 area. */
	private final int[] sunLocation;
	
	// TODO : add last turn resolution informations.

	/**
	 * Full constructor.
	 */
	public PlayerGameBoard(Area[][][] universe, int[] sunLocation)
	{
		this.universe = universe;
		this.sunLocation = sunLocation;
	}
}
