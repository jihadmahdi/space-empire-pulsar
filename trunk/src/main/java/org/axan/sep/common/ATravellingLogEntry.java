package org.axan.sep.common;

import java.io.Serializable;

import org.axan.sep.common.SEPUtils.RealLocation;


public abstract class ATravellingLogEntry implements Serializable
{
	private static final long	serialVersionUID	= 1L;
	
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
