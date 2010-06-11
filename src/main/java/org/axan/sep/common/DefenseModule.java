/**
 * @author Escallier Pierre
 * @file DefeneModule.java
 * @date 3 juin 2009
 */
package org.axan.sep.common;

import java.io.Serializable;


/**
 * Represent all defense modules build on a celestial body.
 */
public class DefenseModule extends ABuilding implements Serializable
{
	public static class DefenseModuleSpecialUnit extends FleetBattleSkillsModifierAdaptor implements ISpecialUnit, Serializable
	{
		private final int fixedAttackBonus;
		private final String name;
		
		public DefenseModuleSpecialUnit(String name, int fixedAttackBonus)
		{
			this.name = name;
			this.fixedAttackBonus = fixedAttackBonus;
		}

		@Override
		public int getFixedAttackBonus()
		{
			return fixedAttackBonus;
		}

		@Override
		public boolean canJoinFleet()
		{
			return false;
		}

		@Override
		public String getName()
		{
			return name;
		}

		@Override
		public boolean isVisibleToClients()
		{
			return false;
		}
		
	}
	
	private static final long	serialVersionUID	= 1L;
	
	public static final int FIRST_CARBON_COST = 4000;
	
	// Constants
	private final int lastBuildDate;
	
	// Only if visible
	private final int nbBuild;
	
	/**
	 * Full constructor. 
	 */
	public DefenseModule(int lastBuildDate, int nbBuild)
	{
		this.nbBuild = nbBuild;
		this.lastBuildDate = lastBuildDate;
	}
	
	/**
	 * First build constructor.
	 */
	public DefenseModule(int lastBuildDate)
	{
		this(lastBuildDate, 1);
	}

	private int getTotalBonus()
	{
		// TODO : Redefine the formula
		return (int) (0.25 * nbBuild * 100);
	}

	@Override
	public int getBuildSlotsCount()
	{
		return nbBuild;
	}
	
	@Override
	public int getLastBuildDate()
	{
		return lastBuildDate;
	}
	
	@Override
	public
	int getUpgradeCarbonCost()
	{
		return (int) ((Float.valueOf(1+nbBuild) * 0.25) * 1000);
	}
	
	@Override
	int getUpgradePopulationCost()
	{
		return 0;
	}

	@Override
	DefenseModule getUpgraded(int date)
	{
		return new DefenseModule(date, nbBuild+1);
	}

	@Override
	DefenseModule getDowngraded()
	{
		return new DefenseModule(lastBuildDate, Math.max(0, nbBuild-1));
	}

	@Override
	boolean canDowngrade()
	{
		return nbBuild > 0;
	}
	
	DefenseModuleSpecialUnit getSpecialUnit()
	{
		return new DefenseModuleSpecialUnit("DefenseModule", getTotalBonus());
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return nbBuild+" Defense modules build, give a defense bonus of "+getTotalBonus()+".\n"+((getUpgradeCarbonCost()<0 && getUpgradePopulationCost()<0)?"Can't build more":"Next build cost "+getUpgradeCarbonCost())+"C, "+getUpgradePopulationCost()+"P.";
	}

	@Override
	boolean canUpgrade()
	{
		return true;
	}
}
