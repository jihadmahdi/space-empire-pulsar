/**
 * @author Escallier Pierre
 * @file Starship.java
 * @date 3 juin 2009
 */
package org.axan.sep.common;

import java.io.Serializable;

/**
 * Represent a Starship model.
 */
@Deprecated // Use common.db.IStarshipTemplate instead.
public class StarshipTemplate implements Serializable, Comparable<StarshipTemplate>
{
	public final int	defense;

	public final int	attack;

	public final double		attackSpecializationBonus;

	public final double		defenseSpecializationBonus;

	public final double		speed;

	public final eStarshipSpecializationClass	specializationClass;

	public final String		name;
	
	public final int carbonPrice;
	public final int populationPrice;
	
	public StarshipTemplate(String name, int defense, int attack, eStarshipSpecializationClass specializationClass, double attackSpecializationBonus, double defenseSpecializationBonus, double speed, int carbonPrice, int populationPrice)
	{
		if (defenseSpecializationBonus > 1.0) throw new NumberFormatException("Error: defenseSpecializationBonus cannot be greter than 100%");
		this.name = name;
		this.defense = defense;
		this.attack = attack;
			
		this.specializationClass = specializationClass;

		this.attackSpecializationBonus = attackSpecializationBonus;
		this.defenseSpecializationBonus = defenseSpecializationBonus;
		
		this.speed = speed;
		
		this.carbonPrice = carbonPrice;
		this.populationPrice = populationPrice;
	}

	public String getName()
	{
		return name;
	}
	
	public eStarshipSpecializationClass getSpecializationClass()
	{
		return specializationClass;
	}
	
	public int getDefense()
	{
		return defense;
	}

	public int getAttack()
	{
		return attack;
	}

	public double getAttackSpecializationBonus()
	{
		return attackSpecializationBonus;
	}

	public double getDefenseSpecializationBonus()
	{
		return defenseSpecializationBonus;
	}

	public double getSpeed()
	{
		return speed;
	}

	@Override
	public String toString()
	{
		return name + " Def:" + defense + " / Att:" + attack + " / AttSpe:" + attackSpecializationBonus + " / DefSpe:" + defenseSpecializationBonus;
	}

	public int getCarbonPrice()
	{
		return carbonPrice;
	}
	
	public int getPopulationPrice()
	{
		return populationPrice;
	}

	@Override
	public int compareTo(StarshipTemplate o)
	{
		int result = eStarshipSpecializationClass.compare(specializationClass, o.specializationClass);
		if (result != 0) return result;
				
		if (defense == o.defense) return 0;
		if (defense > o.defense) return 1;
		return -1;		
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (!getClass().isInstance(obj)) return false;
		StarshipTemplate o = StarshipTemplate.class.cast(obj);
		
		return (super.equals(o) || (this.name.equals(o.name) && this.specializationClass == o.specializationClass));
	}
	
	@Override
	public int hashCode()
	{
		return (getClass().getName()+this.name+this.specializationClass).hashCode();
	}
}
