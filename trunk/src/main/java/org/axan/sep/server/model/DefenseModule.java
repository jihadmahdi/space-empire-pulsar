/**
 * @author Escallier Pierre
 * @file DefenseModule.java
 * @date 1 juin 2009
 */
package org.axan.sep.server.model;

import java.io.Serializable;

/**
 * 
 */
class DefenseModule extends ABuilding implements Serializable
{
	public static class DefenseModuleSpecialUnit extends FleetBattleSkillsModifierAdaptor implements ISpecialUnit
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
		public org.axan.sep.common.ISpecialUnit getPlayerView(int date, String playerLogin, boolean isVisible)
		{
			throw new Error("DefenseModule SpecialUnit are not expected to be shown to players.");
			/*
			return new ISpecialUnit()
			{
				
				@Override
				public String getName()
				{
					return "DefenseModule (+"+fixedAttackBonus+"Att)";
				}
			};
			*/
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
	
	// Constants
	private final int lastBuildDate;
	
	// Variables
	private int nbBuild;	
	
	/**
	 * First build constructor
	 */
	public DefenseModule(int lastBuildDate)
	{
		this(lastBuildDate, 1); 
	}
	
	/**
	 * Full constructor. 
	 */
	public DefenseModule(int lastBuildDate, int nbBuild)
	{
		this.nbBuild = nbBuild;
		this.lastBuildDate = lastBuildDate;
	}

	/* (non-Javadoc)
	 * @see org.axan.sep.server.model.IBuilding#getPlayerView(java.lang.String)
	 */
	@Override
	public org.axan.sep.common.DefenseModule getPlayerView(int date, String playerLogin)
	{
		return new org.axan.sep.common.DefenseModule(nbBuild, getTotalBonus(), getUpgradeCarbonCost());
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
	
	public DefenseModule getUpgradedBuilding(int lastBuildDate)
	{
		return new DefenseModule(lastBuildDate, nbBuild+1);
	}

	@Override
	public int getLastBuildDate()
	{
		return lastBuildDate;
	}

	@Override
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
}
