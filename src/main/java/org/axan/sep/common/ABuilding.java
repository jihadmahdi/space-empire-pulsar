/**
 * @author Escallier Pierre
 * @file Building.java
 * @date 3 juin 2009
 */
package org.axan.sep.common;

import java.lang.reflect.Constructor;

/**
 * Represent a building on a productive celestial body, from a specific player point of view.
 */
public abstract class ABuilding
{
	/**
	 * Return number of slots used by current building.
	 * 
	 * @return
	 */
	abstract public int getBuildSlotsCount();
	
	/**
	 * Return last build/upgrade date.
	 * 
	 * @return
	 */
	abstract int getLastBuildDate();
	
	/**
	 * Return the carbon cost for next upgrade.
	 * @return
	 */
	abstract public int getUpgradeCarbonCost();
	
	/**
	 * Return the population cost for next upgrage.
	 * @return
	 */
	abstract int getUpgradePopulationCost();
	
	/**
	 * Return a new building instance which represent the current one next upgrade (at the given date).
	 * @param date Last build (upgrade) date.
	 * @return
	 */
	abstract ABuilding getUpgraded(int date);
	
	/**
	 * Return a new building instance which represent the current one previous upgrade (keep current last build date).
	 * @return
	 */
	abstract ABuilding getDowngraded();
	
	/**
	 * Return true if the building can currently be downgraded (and/or demolished)
	 * @return
	 */
	abstract boolean canDowngrade();
	
	/**
	 * Return true if the building can currently be upgraded
	 * @return
	 */
	abstract boolean canUpgrade();
	
	/////
	
	/**
	 * Get the carbon cost for the first build of the given building type.
	 * @param buildingType
	 * @return
	 */
	static public int getFirstCarbonCost(Class<? extends ABuilding> buildingType)
	{
		int result = 0;
		try
		{
			result = buildingType.getField("FIRST_CARBON_COST").getInt(null);
		}
		catch(Exception e)
		{
			try
			{
				result = buildingType.getField("CARBON_COST").getInt(null);
			}
			catch(Exception e1)
			{
				result = 0;
			}
		}

		return result;
	}

	/**
	 * Get the population cost for the first build of the given building type.
	 * @param buildingType
	 * @return
	 */
	static public int getFirstPopulationCost(Class<? extends ABuilding> buildingType)
	{
		int result = 0;
		try
		{
			result = buildingType.getField("FIRST_POPULATION_COST").getInt(null);
		}
		catch(Exception e)
		{
			try
			{
				result = buildingType.getField("POPULATION_COST").getInt(null);
			}
			catch(Exception e1)
			{
				result = 0;
			}
		}

		return result;
	}
	
	/**
	 * Get the first build of the given building type dated with the given date.
	 * @param buildingType
	 * @param date
	 * @return
	 */
	static public ABuilding getFirstBuild(Class<? extends ABuilding> buildingType, int date)
	{
		try
		{			
			Constructor<? extends ABuilding> constructor = buildingType.getConstructor(int.class);
			
			return constructor.newInstance(date);
		}
		catch(Exception e)
		{
			throw new SEPCommonImplementationException("Cannot get first build for type '"+buildingType.getSimpleName()+"'", e);
		}
	}

}
