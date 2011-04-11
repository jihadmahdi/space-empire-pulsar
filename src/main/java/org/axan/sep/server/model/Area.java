/**
 * @author Escallier Pierre
 * @file Area.java
 * @date 1 juin 2009
 */
package org.axan.sep.server.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.axan.sep.server.SEPServer;


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
	private final ISEPServerDataBase db;
	
	// Primary Key
	private final org.axan.sep.common.SEPUtils.Location location;
	
	// Constants
	private boolean isSun = false;	
	private String celestialBodyName = null;
	
	// Variables
	
	// Views
	private final PlayerDatedView<Integer> playersLastObservation = new PlayerDatedView<Integer>();
	private final PlayerDatedView<HashSet<org.axan.sep.common.Unit>> playersUnitsView = new PlayerDatedView<HashSet<org.axan.sep.common.Unit>>();
	
	public Area(ISEPServerDataBase db, org.axan.sep.common.SEPUtils.Location location)
	{
		this.db = db;
		this.location = location;
	}
	
	public String getCelestialBodyName()
	{
		return celestialBodyName;
	}
	
	public ICelestialBody getCelestialBody()
	{		
		return celestialBodyName == null ? null : db.getCelestialBody(celestialBodyName);
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
	public org.axan.sep.common.Area getPlayerView(int date, String playerLogin, boolean isVisible)
	{
		HashSet<org.axan.sep.common.Unit> unitsView;
		Set<org.axan.sep.common.IMarker> markersView;
		
		if (isVisible)
		{
			// Updates
			playersLastObservation.updateView(playerLogin, date, date);
			
			unitsView = new HashSet<org.axan.sep.common.Unit>();
			
			for(Unit u : db.getUnits(location))
			{
				if (Fleet.class.isInstance(u))
				{
					Fleet f = Fleet.class.cast(u);
					if (f.isUnassignedFleet()) continue;
					if (f.hasNoMoreStarships())
					{
						if (!f.isUnassignedFleet()) throw new Error("Assigned fleet cannot be empty.");
						continue;
					}
				}
				
				unitsView.add(u.getPlayerView(date, playerLogin, isVisible));
			}
			playersUnitsView.updateView(playerLogin, unitsView, date);
		}
		else
		{		
			// Refresh player units only
			boolean modified = false;
			unitsView = new HashSet<org.axan.sep.common.Unit>();
			if (playersUnitsView.hasView(playerLogin)) for(org.axan.sep.common.Unit u : playersUnitsView.getView(playerLogin).getValue())
			{
				if (playerLogin.equals(u.getOwnerName()))
				{
					modified = true;
					
					Unit unit = db.getUnit(location, getServerUnitType(u.getClass()), u.getOwnerName(), u.getName());						
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
		}
		
		// Refresh markers	
		markersView = new HashSet<org.axan.sep.common.IMarker>();
		for(UnitMarker um : db.getUnitMarkers(playerLogin, location))
		{	
			Unit unit = db.getUnit(location, getServerUnitType(um.getUnit().getClass()), um.getUnit().getOwnerName(), um.getUnit().getName());
			
			if (!isVisible || unit == null) markersView.add(um.getView(isVisible));
			
			// TODO: remove marker for existing units if (unit != null) db.removeMarker(...) ?
		}
				
		ICelestialBody celestialBody = getCelestialBody();
		
		if (celestialBody != null && ProductiveCelestialBody.class.isInstance(celestialBody))
		{
			ProductiveCelestialBody productiveCelestialBody = ProductiveCelestialBody.class.cast(celestialBody);
			
			Map<String, org.axan.sep.common.Fleet> unasignedFleetsView = productiveCelestialBody.getUnasignedFleetsView(date, playerLogin, isVisible);
			
			if (unasignedFleetsView != null) for(org.axan.sep.common.Fleet unasignedFleetView : unasignedFleetsView.values())
			{
				unitsView.add(unasignedFleetView);
			}
		}
	
		int lastObservation = playersLastObservation.getLastValue(playerLogin, -1);
		org.axan.sep.common.ICelestialBody celestialBodyView = (celestialBody == null)?null:celestialBody.getPlayerView(date, playerLogin, isVisible);		
		
		return new org.axan.sep.common.Area(isVisible, lastObservation, isSun, celestialBodyView, unitsView, markersView);
	}
	
	private static <U extends org.axan.sep.common.Unit> Class<? extends Unit> getServerUnitType(Class<U> clientUnitType)
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
