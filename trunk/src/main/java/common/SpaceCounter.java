/**
 * @author Escallier Pierre
 * @file SpaceCounter.java
 * @date 3 juin 2009
 */
package common;

import java.util.Set;

/**
 * Represent all space counter build on a celestial body.
 */
public class SpaceCounter implements IBuilding
{
	// Only if visible
	private final int nbBuild;
	
	private final Set<SpaceRoad> spaceRoads;
	
	private final Set<CarbonOrder> carbonToReceive;
	
	private final Set<CarbonOrder> nextCarbonOrder;
	
	private final Set<CarbonOrder> currentCarbonOrder;
	
	/**
	 * Full constructor.
	 */
	public SpaceCounter(int nbBuild, Set<SpaceRoad> spaceRoads, Set<CarbonOrder> carbonToReceive, Set<CarbonOrder> currentCarbonOrder, Set<CarbonOrder> nextCarbonOrder)
	{
		this.nbBuild = nbBuild;
		this.spaceRoads = spaceRoads;
		this.carbonToReceive = carbonToReceive;
		this.currentCarbonOrder = currentCarbonOrder;
		this.nextCarbonOrder = nextCarbonOrder;
	}
}
