/**
 * @author Escallier Pierre
 * @file CarbonCarrier.java
 * @date 3 juin 2009
 */
package org.axan.sep.common;

import java.io.Serializable;

import org.axan.sep.common.SEPUtils.RealLocation;


/**
 * Represent a carbon carrier
 */
public class CarbonCarrier extends Unit implements Serializable
{
	private static final long	serialVersionUID	= 1L;
	
	private final CarbonOrder order;
	
	/**
	 * 
	 */
	public CarbonCarrier(boolean isVisible, int lastObervation, String name, String ownerName, RealLocation sourceLocation, RealLocation destinationLocation, RealLocation currentLocation, double travellingProgress, double speed, CarbonOrder order)
	{
		super(isVisible, lastObervation, name, ownerName, sourceLocation, destinationLocation, currentLocation, travellingProgress, speed);
		this.order = order;
	}
	
	public int getOrderAmount()
	{
		return order.getAmount();
	}
	
	@Override
	public String toString()
	{
		return "Order: "+(order == null ? "null" : order.toString())+", Carrier: "+super.toString();
	}
}
