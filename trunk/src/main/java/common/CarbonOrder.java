/**
 * @author Escallier Pierre
 * @file CarbonOrder.java
 * @date 3 juin 2009
 */
package common;

/**
 * Represent a carbon trade order.
 */
public class CarbonOrder
{
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
}
