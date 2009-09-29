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
	
	public CarbonOrder(String sourceName, String destinationName, int amount)
	{
		this.sourceName = sourceName;
		this.destinationName = destinationName;
		this.amount = amount;
	}
	
	public int getAmount()
	{
		return amount;
	}

	public String getDestinationName()
	{
		return destinationName;
	}
}