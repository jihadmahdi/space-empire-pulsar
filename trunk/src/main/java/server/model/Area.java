/**
 * @author Escallier Pierre
 * @file Area.java
 * @date 1 juin 2009
 */
package server.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

class Area implements Serializable
{
	private static final long	serialVersionUID	= 1L;

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
	
	// Constants
	private boolean isSun;	
	private ICelestialBody celestialBody;
	
	// Variables
	private final Set<Unit> units = new HashSet<Unit>();
	
	// Views
	private final PlayerDatedView<Integer> playersLastObservation = new PlayerDatedView<Integer>();
	private final PlayerDatedView<HashSet<common.Unit>> playersUnitsView = new PlayerDatedView<HashSet<common.Unit>>();
	
	// Markers
	private final Map<String, Set<common.IMarker>> playersMarkers = new HashMap<String, Set<common.IMarker>>();
	
	/**
	 * Sun flag is set to true if this area is filled with the sun.
	 * @param isSun value to set.
	 */
	public void setSunFlag(boolean isSun)
	{
		this.isSun = isSun;
	}
	
	public boolean isSun()
	{
		return isSun;
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
	public void setCelestialBody(ICelestialBody celestialBody) throws AreaIllegalDefinitionException
	{
		if (isSun) throw new AreaIllegalDefinitionException("Cannot set a celestialBody in area filled with sun.");
		this.celestialBody = celestialBody;		
	}

	/**
	 * @return ICelestialBody celestialBody
	 */
	public ICelestialBody getCelestialBody()
	{
		return celestialBody;
	}
	
	/**
	 * Return true if at least one of the given units is located in this area.
	 * @param testedUnits Set of units to test.
	 * @return true if at least one of the given units is located in this area, false if none.
	 */
	public boolean containsOneOf(Set<Unit> testedUnits)
	{
		Set<Unit> clone = new HashSet<Unit>(units);
		clone.retainAll(testedUnits);
		return !clone.isEmpty();		
	}

	/**
	 * @return unmodifiable set of units located in this area.
	 */
	public Set<Unit> getUnits()
	{
		return Collections.unmodifiableSet(units);
	}
	
	public void addUnit(Unit unit)
	{
		units.add(unit);
	}
	
	public void removeUnit(Unit unit)
	{
		units.remove(unit);
	}

	/**
	 * @param playerLogin
	 * @param isVisible
	 * @return
	 */
	public common.Area getPlayerView(int date, String playerLogin, boolean isVisible)
	{		
		HashSet<common.Unit> unitsView;
		if (isVisible)
		{
			// Updates
			playersLastObservation.updateView(playerLogin, date, date);
			
			unitsView = new HashSet<common.Unit>();
			for(Unit u : units)
			{
				unitsView.add(u.getPlayerView(date, playerLogin, isVisible));
			}
			playersUnitsView.updateView(playerLogin, unitsView, date);
		}
		else
		{
			unitsView = playersUnitsView.getLastValue(playerLogin, null);
		}
		
		if (celestialBody != null && ProductiveCelestialBody.class.isInstance(celestialBody))
		{
			ProductiveCelestialBody productiveCelestialBody = ProductiveCelestialBody.class.cast(celestialBody);
			
			Map<String, common.Fleet> unasignedFleetsView = productiveCelestialBody.getUnasignedFleetView(date, playerLogin, isVisible);
			
			if (unasignedFleetsView != null) for(common.Fleet unasignedFleetView : unasignedFleetsView.values())
			{
				unitsView.add(unasignedFleetView);
			}
		}
	
		int lastObservation = playersLastObservation.getLastValue(playerLogin, -1);
		common.ICelestialBody celestialBodyView = (celestialBody == null)?null:celestialBody.getPlayerView(date, playerLogin, isVisible);		
		
		return new common.Area(isVisible, lastObservation, isSun, celestialBodyView, unitsView, playersMarkers.get(playerLogin));
	}
	
}
