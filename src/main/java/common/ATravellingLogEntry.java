package common;

import common.SEPUtils.Location;

public abstract class ATravellingLogEntry
{
	private final String title;
	private final int date;
	private final String instantTime;
	private final Location location;
	
	public ATravellingLogEntry(String title, int date, String instantTime, Location location)
	{
		this.title = title;
		this.date = date;
		this.instantTime = instantTime;
		this.location = location;
	}
}
