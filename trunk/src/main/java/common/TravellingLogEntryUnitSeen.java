package common;

import java.util.HashSet;
import java.util.Set;

import common.SEPUtils.Location;

public class TravellingLogEntryUnitSeen extends ATravellingLogEntry
{
	private final Set<Unit> units;
	
	public TravellingLogEntryUnitSeen(String title, int date, String instantTime, Location location, Set<Unit> units)
	{
		super(title, date, instantTime, location);
		this.units = units;
	}

}
