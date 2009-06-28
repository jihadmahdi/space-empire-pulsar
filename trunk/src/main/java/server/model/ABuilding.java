/**
 * @author Escallier Pierre
 * @file Building.java
 * @date 1 juin 2009
 */
package server.model;

import java.lang.reflect.Constructor;

import server.SEPServer;
import server.SEPServer.SEPImplementationException;

/**
 * This abstract class represent a building on a celestial body.
 */
abstract class ABuilding
{

	/**
	 * @param playerLogin
	 * @return
	 */
	abstract common.IBuilding getPlayerView(int date, String playerLogin);

	/**
	 * Return number of slots used by current building.
	 * 
	 * @return
	 */
	abstract int getBuildSlotsCount();

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
	abstract int getUpgradeCarbonCost();

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
	 * Get the carbon cost for the first build of the given building type.
	 * @param buildingType
	 * @return
	 */
	static int getFirstCarbonCost(Class<? extends common.IBuilding> buildingType)
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
	static int getFirstPopulationCost(Class<? extends common.IBuilding> buildingType)
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
	static ABuilding getFirstBuild(Class<? extends common.IBuilding> buildingType, int date)
	{
		try
		{
			Class<? extends ABuilding> serverBuildingType = getServerBuildingClass(buildingType);
			
			Constructor<? extends ABuilding> constructor = serverBuildingType.getConstructor(int.class);
			
			return constructor.newInstance(date);
		}
		catch(Exception e)
		{
			throw new SEPServer.SEPImplementationException("Cannot get first build for type '"+buildingType.getSimpleName()+"'", e);
		}
	}

	private static Class<? extends ABuilding> getServerBuildingClass(Class<? extends common.IBuilding> clientBuildingType) throws SEPImplementationException
	{
		Class<?> serverClass;
		try
		{
			serverClass = Class.forName(ABuilding.class.getPackage().getName() + "." + clientBuildingType.getSimpleName());
		}
		catch(ClassNotFoundException e)
		{
			throw new SEPServer.SEPImplementationException("Cannot find server building type for '" + clientBuildingType.getSimpleName() + "'", e);
		}

		if (!ABuilding.class.isAssignableFrom(serverClass))
			throw new SEPServer.SEPImplementationException("Cannot find server building type for '" + clientBuildingType.getSimpleName() + "', '"
					+ ABuilding.class.getName() + "' is not assignable from '" + serverClass.getName() + "'");
		Class<? extends ABuilding> serverBuildingType = serverClass.asSubclass(ABuilding.class);

		return serverBuildingType;
	}

	/**
	 * Return true if the building can currently be downgraded (and/or demolished)
	 * @return
	 */
	abstract boolean canDowngrade();
}
