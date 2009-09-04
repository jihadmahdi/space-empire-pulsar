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
import sun.security.action.GetLongAction;

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
	
	// DB context
	private final GameBoard gameBoard;
	
	// Primary Key
	private final common.SEPUtils.Location location;
	
	// Constants
	private boolean isSun = false;	
	private String celestialBodyName = null;
	
	// Variables
	
	// Views
	private final PlayerDatedView<Integer> playersLastObservation = new PlayerDatedView<Integer>();
	private final PlayerDatedView<HashSet<common.Unit>> playersUnitsView = new PlayerDatedView<HashSet<common.Unit>>();

	// Markers
	private final Map<String, Set<common.IMarker>> playersMarkers = new HashMap<String, Set<common.IMarker>>();
	
	public Area(GameBoard gameBoard, common.SEPUtils.Location location)
	{
		this.gameBoard = gameBoard;
		this.location = location;
	}
	
	protected ICelestialBody getCelestialBody()
	{		
		return celestialBodyName == null ? null : gameBoard.getCelestialBody(celestialBodyName);
	}
	
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
		if (getCelestialBody() != null) return false;
		
		return true;
	}

	/**
	 * Set area filled with the given celestial body.
	 * @param celestialBody Celestial body to put in this area.
	 * @throws AreaIllegalDefinitionException On illegal setCelestialBody attempt (ie: if current area is in the sun).
	 */
	public void setCelestialBody(String celestialBodyName) throws AreaIllegalDefinitionException
	{
		if (isSun) throw new AreaIllegalDefinitionException("Cannot set a celestialBody in area filled with sun.");
		if (this.celestialBodyName != null) throw new AreaIllegalDefinitionException("CelestialBody is already set.");		
		this.celestialBodyName = celestialBodyName;
		if (getCelestialBody() == null) throw new AreaIllegalDefinitionException("CelestialBody '"+celestialBodyName+"' not found.");		
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
			
			unitsView = new HashSet<common.Unit>();
			
			for(Unit u : gameBoard.getUnits(location))
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
					
					Unit unit = gameBoard.getUnit(location, getServerUnitType(um.getUnit().getClass()), um.getUnit().getOwnerName(), um.getUnit().getName());
					
					if (unit == null) markersView.add(um);
					
					continue;
				}
				
				// TODO: Other markers
			}
			
			playersMarkers.put(playerLogin, markersView);
		}
		else
		{		
			// Refresh player units only
			boolean modified = false;
			unitsView = new HashSet<common.Unit>();
			if (playersUnitsView.hasView(playerLogin)) for(common.Unit u : playersUnitsView.getView(playerLogin).getValue())
			{
				if (u.getOwnerName() != null && u.getOwnerName().compareTo(playerLogin) == 0)
				{
					modified = true;
					
					Unit unit = gameBoard.getUnit(location, getServerUnitType(u.getClass()), u.getOwnerName(), u.getName());						
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
		
		ICelestialBody celestialBody = getCelestialBody();
		
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
