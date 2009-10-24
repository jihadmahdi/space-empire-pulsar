package org.axan.sep.common;

import java.io.Serializable;

import org.axan.sep.common.SEPUtils.RealLocation;



public class TravellingLogEntryUnitSeen extends ATravellingLogEntry implements Serializable
{
	private static final long	serialVersionUID	= 1L;
	
	private final Unit unit;
	
	public TravellingLogEntryUnitSeen(String title, int date, float instantTime, RealLocation location, Unit unit)
	{
		super(title, date, instantTime, location);
		this.unit = unit;
	}	
}
