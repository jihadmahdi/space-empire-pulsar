package org.axan.sep.common;

import java.util.TreeMap;

import org.axan.sep.common.SEPUtils.RealLocation;

public class UnitSeenLogEntry extends ALogEntry.AUpdatableLogEntry<UnitSeenLogEntry>
{
	// Constant
	private final String unitUID;
	
	// Variables
	private double lastUpdate;	
	private final TreeMap<Double, RealLocation> locations;
	private final TreeMap<Double, Unit> unitViews;
	
	public UnitSeenLogEntry(int creationDate, double instantTime, RealLocation location, Unit unit)
	{
		super(creationDate, instantTime);
		this.locations = new TreeMap<Double, RealLocation>();
		this.locations.put(getCreationInstantDate(), location);		
		this.unitViews = new TreeMap<Double, Unit>();
		this.unitViews.put(getCreationInstantDate(), unit);
		this.lastUpdate = getCreationInstantDate();
		this.unitUID = unit.getOwnerName()+"@"+unit.getName();
	}
	
	/**
	 * @return The game date of the last update of this log.
	 */
	final public int getLastUpdateDate()
	{
		return (int) Math.floor(lastUpdate);
	}
	
	/**
	 * @return The game instant date of the last update of this log. 
	 */
	final public double getLastUpdateInstantDate()
	{
		return lastUpdate;
	}
	
	@Override
	protected String getALogEntryUID()
	{
		return String.format("%s", unitUID);
	}

	@Override
	public String toString()
	{
		if (getCreationInstantDate() == getLastUpdateInstantDate())
		{
			return String.format("Unit %s seen at %s on T%2f :\n%s",unitUID, locations.lastEntry().getValue().toString(), getLastUpdateInstantDate(), unitViews.lastEntry().getValue().toString());
		}
		else
		{
			return String.format("Unit %s seen moving from %s on T%2f to %s on T%2f; Last status :\n%s", unitUID, locations.firstEntry().getValue().toString(), locations.firstEntry().getKey(), locations.lastEntry().getValue().toString(), locations.lastEntry().getKey(), unitViews.lastEntry().getValue().toString());
		}		
	}
	
	@Override
	protected double getOrder()
	{
		return lastUpdate;
	}

	@Override
	public UnitSeenLogEntry update(AUpdatableLogEntry<?> o)
	{		
		if (!getUID().equals(o.getUID())) throw new IllegalArgumentException("Cannot update log entry with different uids.");
		if (!getType().equals(o.getType())) throw new IllegalArgumentException("Cannot update log entry with different final class.");
		
		UnitSeenLogEntry up = UnitSeenLogEntry.class.cast(o);
		
		locations.putAll(up.locations);
		unitViews.putAll(up.unitViews);
		lastUpdate = up.lastUpdate;
		
		return this;
	}
	
	@Override
	public Class<UnitSeenLogEntry> getType()
	{
		return UnitSeenLogEntry.class;
	}
}
