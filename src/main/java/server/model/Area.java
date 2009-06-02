/**
 * @author Escallier Pierre
 * @file Area.java
 * @date 1 juin 2009
 */
package server.model;

import common.CelestialBody;

/**
 * This class represent an area located in the universe.
 */
public class Area
{
	public static class AreaIllegalDefinitionException extends Exception
	{

		private static final long	serialVersionUID	= 1L;
		
		/**
		 * Full constructor.
		 */
		public AreaIllegalDefinitionException(String msg)
		{
			super(msg);
		}
		
	}
	
	private boolean isSun;
	private CelestialBody celestialBody;
	
	/**
	 * Sun flag is set to true if this area is filled with the sun.
	 * @param isSun value to set.
	 */
	public void setSunFlag(boolean isSun)
	{
		this.isSun = isSun;
	}
	
	/**
	 * Return true if this area is empty, false otherwise.
	 * @return true if this area is empty, false otherwise.
	 */
	public boolean isEmpty()
	{
		if (isSun) return false;
		if (celestialBody != null) return false;
		
		// TODO: Maintain this method up to date with new celestial bodies implementation.
		
		return true;
	}

	/**
	 * Set area filled with the given celestial body.
	 * @param celestialBody Celestial body to put in this area.
	 * @throws AreaIllegalDefinitionException On illegal setCelestialBody attempt (ie: if current area is in the sun).
	 */
	public void setCelestialBody(CelestialBody celestialBody) throws AreaIllegalDefinitionException
	{
		if (isSun) throw new AreaIllegalDefinitionException("Cannot set a celestialBody in area filled with sun.");
		this.celestialBody = celestialBody;
	}
}
