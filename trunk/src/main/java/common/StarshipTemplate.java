/**
 * @author Escallier Pierre
 * @file Starship.java
 * @date 3 juin 2009
 */
package common;

import java.io.Serializable;

/**
 * Represent a Starship model.
 */
public class StarshipTemplate implements Serializable, Comparable<StarshipTemplate>
{
	public enum eClass
	{
		ARTILLERY, DESTROYER, FIGHTER;
		
		static public eClass getBN(eClass starshipClass)
		{
			switch(starshipClass)
			{
				case ARTILLERY:
					return FIGHTER;
				case DESTROYER:
					return ARTILLERY;
				case FIGHTER:
					return DESTROYER;
				default:
					throw new RuntimeException("BN inconnue pour la classe \"" + starshipClass + "\"");
			}
		}

		static public eClass getTdT(eClass starshipClass)
		{
			switch(starshipClass)
			{
				case ARTILLERY:
					return DESTROYER;
				case DESTROYER:
					return FIGHTER;
				case FIGHTER:
					return ARTILLERY;
				default:
					throw new RuntimeException("TdT inconnue pour la classe \"" + starshipClass + "\"");
			}
		}

		public int comparer(eClass starshipClass)
		{			
			if (this == starshipClass) return 0;
			if (getTdT(this) == starshipClass) return 1;
			if (getBN(this) == starshipClass) return -1;

			throw new RuntimeException("Comparaison de classes impossible: \"" + this + "\" et \"" + starshipClass + "\"");
		}
		
		@Override
		public String toString()
		{
			String upper = super.toString();
			return upper.substring(0, 1)+upper.substring(1).toLowerCase();
		}
	};

	public final int	defense;

	public final int	attack;

	public final double		attackSpecializationBonus;

	public final double		defenseSpecializationBonus;

	public final double		speed;

	public final eClass		specializationClass;

	public final String		name;
	
	public final int carbonPrice;
	public final int populationPrice;
	
	public StarshipTemplate(String name, int defense, int attack, eClass specializationClass, double attackSpecializationBonus, double defenseSpecializationBonus, double speed, int carbonPrice, int populationPrice)
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
	
	public eClass getSpecializationClass()
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
		int result = specializationClass.comparer(o.specializationClass);
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
		
		return (super.equals(o) || (this.name.compareTo(o.name) == 0 && this.specializationClass == o.specializationClass));
	}
	
	@Override
	public int hashCode()
	{
		return (getClass().getName()+this.name+this.specializationClass).hashCode();
	}
}
