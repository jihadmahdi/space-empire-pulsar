package org.axan.sep.common.db;

import java.io.Serializable;

public class FleetMove implements Serializable
{
	private final String destinationName;
	private final int delay;
	private final boolean isAnAttack;
	
	public FleetMove(String destinationName, int delay, boolean isAnAttack)
	{
		this.destinationName = destinationName;
		this.delay = delay;
		this.isAnAttack = isAnAttack;
	}
	
	public String getDestinationName()
	{
		return destinationName;
	}
	
	public int getDelay()
	{
		return delay;
	}
	
	public boolean isAnAttack()
	{
		return isAnAttack;
	}
	
	@Override
	public String toString()
	{
		return String.format("%s %s %s", isAnAttack ? "Attack" : "Go to", destinationName, delay == 0 ? "" : "after "+delay+" turns");
	}
}
