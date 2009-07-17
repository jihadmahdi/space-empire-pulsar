package common;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import common.SEPUtils.RealLocation;

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
