package org.axan.sep.common;

import org.axan.sep.common.Protocol.eBuildingType;

public class Rules
{
	private Rules() {}
	
	public static boolean getBuildingCanBeUpgraded(eBuildingType buildingType)
	{
		switch (buildingType)
		{
			case DefenseModule:
			case ExtractionModule:
			case PulsarLaunchingPad:
			case SpaceCounter:
				return true;
			
			case GovernmentModule:
			case StarshipPlant:
				return false;
				
			default:
				throw new SEPCommonImplementationException(buildingType+".canBeUpgraded() not implemented.");					
		}
	}
	
	public static int getBuildingUpgradeCarbonCost(eBuildingType buildingType, int nbBuilt)
	{
		switch (buildingType)
		{
			// TODO: implement real values.

			default:
				return (int) ((Float.valueOf(1+nbBuilt) * 0.25) * 1000);
		}
	}
	
	public static int getBuildingUpgradePopulationCost(eBuildingType buildingType, int nbBuilt)
	{
		switch (buildingType)
		{
			// TODO: implement real values.

			default:
				return 0;
		}
	}
}
