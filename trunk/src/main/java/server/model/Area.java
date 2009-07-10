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

import server.SEPServer;

import common.IMarker;
import common.SEPUtils;
import common.UnitMarker;

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
	
	public <U extends Unit> U getUnit(Class<U> unitType, String ownerName, String unitName)
	{
		for(Unit u : units)
		{
			if (!unitType.isInstance(u)) continue;
			
			// Owner filter
			if (!((ownerName == null && u.getOwner() == null) || (ownerName != null && u.getOwner() != null && u.getOwner().isNamed(ownerName)))) continue;
			
			// Name filter
			if (u.getName().compareTo(unitName) == 0) return unitType.cast(u);
		}
		
		return null;
	}
	
	public void updateUnit(Unit unit)
	{
		Unit oldUnit = getUnit(unit.getClass(), unit.getOwnerName(), unit.getName());
		if (oldUnit != null) units.remove(oldUnit);
		
		units.add(unit);
	}
	
	public <U extends Unit> void removeUnit(Class<U> unitType, String ownerName, String unitName)
	{
		Unit unit = getUnit(unitType, ownerName, unitName);
		if (unit != null) units.remove(unit);
	}
	
	public UnitMarker getUnitMarker(String playerLogin, String ownerName, String unitName)
	{
		if (playersMarkers.containsKey(playerLogin)) for(IMarker m : playersMarkers.get(playerLogin))
		{
			if (m != null && UnitMarker.class.isInstance(m))
			{
				UnitMarker um = UnitMarker.class.cast(m);
				
				common.Unit u = um.getUnit();
				
				// Owner filter
				if (!((ownerName == null && u.getOwner() == null) || (ownerName != null && u.getOwner() != null && u.getOwner().isNamed(ownerName)))) continue;
				
				// Name filter
				if (u.getName().compareTo(unitName) == 0) return um;
			}
		}
		
		return null;
	}
	
	public void removeUnitMarker(String playerLogin, String markedUnitOwnerName, String markedUnitName)
	{
		UnitMarker unitMarker = getUnitMarker(playerLogin, markedUnitOwnerName, markedUnitName);
		if (unitMarker != null) playersMarkers.get(playerLogin).remove(unitMarker);
	}
	
	public void updateUnitMarker(String playerLogin, UnitMarker unitMarker)
	{
		if (!playersMarkers.containsKey(playerLogin) || playersMarkers.get(playerLogin) == null)
		{
			playersMarkers.put(playerLogin, new HashSet<IMarker>());
		}
		
		UnitMarker oldUnitMarker = getUnitMarker(playerLogin, unitMarker.getUnit().getOwnerName(), unitMarker.getUnit().getName());
		if (oldUnitMarker != null) playersMarkers.get(playerLogin).remove(oldUnitMarker);
		
		playersMarkers.get(playerLogin).add(unitMarker);
	}

	/**
	 * @param playerLogin
	 * @param isVisible
	 * @return
	 */
	public common.Area getPlayerView(int date, String playerLogin, boolean isVisible)
	{				
		HashSet<common.Unit> unitsView;
		Set<common.IMarker> markersView;
		
		if (isVisible)
		{
			// Updates
			playersLastObservation.updateView(playerLogin, date, date);
			
			// Updates
			playersLastObservation.updateView(playerLogin, date, date);
			
			unitsView = new HashSet<common.Unit>();
			for(Unit u : units)
			{
				unitsView.add(u.getPlayerView(date, playerLogin, isVisible));
			}
			playersUnitsView.updateView(playerLogin, unitsView, date);
			
			// Refresh markers
			markersView = new HashSet<IMarker>();
			if (playersMarkers.containsKey(playerLogin)) for(common.IMarker m : playersMarkers.get(playerLogin))
			{
				// Unit markers
				if (UnitMarker.class.isInstance(m))
				{
					UnitMarker um = UnitMarker.class.cast(m);
					Unit unit = getUnit(getServerUnitType(um.getUnit().getClass()), um.getUnit().getOwnerName(), um.getUnit().getName());
					
					if (unit == null) markersView.add(um);
					
					continue;
				}
				
				// TODO: Other markers
			}
		}
		else
		{		
			// Refresh player units only
			boolean modified = false;
			unitsView = new HashSet<common.Unit>();
			if (playersUnitsView.hasView(playerLogin)) for(common.Unit u : playersUnitsView.getView(playerLogin).getValue())
			{
				if (u.getOwner() != null && u.getOwner().isNamed(playerLogin))
				{
					modified = true;
					
					Unit unit = getUnit(getServerUnitType(u.getClass()), u.getOwnerName(), u.getName());						
					if (unit == null) continue;
					
					unitsView.add(unit.getPlayerView(date, playerLogin, true));					
				}
				else
				{
					unitsView.add(u);
				}
			}
			
			if (modified)
			{
				playersUnitsView.updateView(playerLogin, unitsView, date);
			}
			
			markersView = playersMarkers.get(playerLogin);
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
		
		return new common.Area(isVisible, lastObservation, isSun, celestialBodyView, unitsView, markersView);
	}
	
	private static <U extends common.Unit> Class<? extends Unit> getServerUnitType(Class<U> clientUnitType)
	{
		try
		{
			return Class.forName(Unit.class.getPackage().getName()+"."+clientUnitType.getSimpleName()).asSubclass(Unit.class);
		}
		catch(ClassNotFoundException e)
		{
			throw new SEPServer.SEPImplementationException("Cannot find server unit type for '" + clientUnitType.getSimpleName() + "'", e);
		}
	}
	
}
