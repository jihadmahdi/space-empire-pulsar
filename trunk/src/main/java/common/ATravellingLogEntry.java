package common;

import java.io.Serializable;

import common.SEPUtils.RealLocation;

public abstract class ATravellingLogEntry implements Serializable
{
	private final String title;
	private final int date;
	private final float instantTime;
	private final RealLocation location;
	
	public ATravellingLogEntry(String title, int date, float instantTime, RealLocation location)
	{
		this.title = title;
		this.date = date;
		this.instantTime = instantTime;
		this.location = location;
	}
}
