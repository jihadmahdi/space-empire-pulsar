package common;

import java.io.Serializable;

public class CarbonOrder implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	/** Source productive celestial body name. */
	private final String sourceName;
	
	/** Destination productive celestial body name. */
	private final String destinationName;
	
	/** Carbon amount. */
	private final int amount;
	
	/** Automatically repeated order. */
	private final boolean automated;
	
	public CarbonOrder(String sourceName, String destinationName, int amount, boolean automated)
	{
		this.sourceName = sourceName;
		this.destinationName = destinationName;
		this.amount = amount;
		this.automated = automated;
	}
	
	@Override
	public String toString()
	{
		return String.format("%s to %s (%dc)", sourceName, destinationName, amount);
	}
	
	public int getAmount()
	{
		return amount;
	}

	public String getSourceName()
	{
		return sourceName;
	}
	
	public String getDestinationName()
	{
		return destinationName;
	}
	
	public boolean isAutomated()
	{
		return automated;
	}
}