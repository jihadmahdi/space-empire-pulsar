package org.axan.sep.server.model;

import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.RealLocation;
import org.axan.sep.server.model.orm.IVersionedUnit;

public class VersionedUnit
{
	private final IVersionedUnit unit;

	private final float speed;
	private final float sight;
	
	public VersionedUnit(IVersionedUnit unit, IGameConfig config)
	{
		this.unit = unit;
		
		// TODO: implement fleet speed, computed from fleet composition.
		// TODO: implement space road
		this.speed = config.getUnitTypeSpeed(getType());
		this.sight = config.getUnitTypeSight(getType());
	}

	private RealLocation departure; 
	public RealLocation getDeparture()
	{
		if (departure == null)
		{
			if (unit.getDeparture_x() == null || unit.getDeparture_y() == null || unit.getDeparture_z() == null) return null;
			departure = new RealLocation(unit.getDeparture_x(), unit.getDeparture_y(), unit.getDeparture_z());			
		}
		
		return departure;
	}
	
	private RealLocation destination; 
	public RealLocation getDestination()
	{
		if (departure == null)
		{
			if (unit.getDestination_x() == null || unit.getDestination_y() == null || unit.getDestination_z() == null) return null;
			destination = new RealLocation(unit.getDestination_x(), unit.getDestination_y(), unit.getDestination_z());			
		}
		
		return destination;
	}
	
	public eUnitType getType()
	{
		return eUnitType.valueOf(unit.getType());
	}
	
	public float getSpeed()
	{
		return speed;
	}
	
	public float getSight()
	{
		return sight;
	}
	
	/// Delegates
	
	public String getName()
	{
		return unit.getName();
	}
	
	public String getOwner()
	{
		return unit.getOwner();
	}
	
	public Double getProgress()
	{
		return unit.getProgress();
	}
	
	public Integer getTurn()
	{
		return unit.getTurn();
	}
	
}
