/**
 * @author Escallier Pierre
 * @file Vortex.java
 * @date 3 juin 2009
 */
package common;

/**
 * Represet a vortex.
 */
public class Vortex implements ICelestialBody
{
	private final boolean isVisible;	
	private final int lastObservation; 

	/**
	 * Full constructor.
	 */
	public Vortex(boolean isVisible, int lastObservation)
	{
		this.isVisible = isVisible;
		this.lastObservation = lastObservation;
	}
	
	/* (non-Javadoc)
	 * @see common.Observable#getLastObservation()
	 */
	@Override
	public int getLastObservation()
	{
		return lastObservation;
	}

	/* (non-Javadoc)
	 * @see common.Observable#isVisible()
	 */
	@Override
	public boolean isVisible()
	{
		return isVisible;
	}

}
