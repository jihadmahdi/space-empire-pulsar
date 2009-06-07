/**
 * @author Escallier Pierre
 * @file CarbonOrder.java
 * @date 3 juin 2009
 */
package common;

import java.io.Serializable;

/**
 * Represent a carbon trade order.
 */
public class CarbonOrder implements Serializable
{
	private static final long	serialVersionUID	= 1L;

	private final CarbonCarrier carbonCarrier;
	private final int carbonAmount;
	
	/**
	 * Full constructor.
	 */
	public CarbonOrder(CarbonCarrier carbonCarrier, int carbonAmount)
	{
		this.carbonCarrier = carbonCarrier;
		this.carbonAmount = carbonAmount;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return carbonAmount+" carbon; "+carbonCarrier;
	}
}
