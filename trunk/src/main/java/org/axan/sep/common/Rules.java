package org.axan.sep.common;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.axan.sep.common.Protocol.eBuildingType;
import org.axan.sep.common.Protocol.eSpecialUnitType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.IGameConfig;

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
	
	public static Location getSunLocation(IGameConfig config)
	{
		return new Location(config.getDimX()/2, config.getDimY()/2, config.getDimZ()/2);
	}
	
	public static Set<Location> getSunAreasByZ(IGameConfig config, int z)
	{
		Location sunLocation = getSunLocation(config);
		
		if (Math.abs(z - sunLocation.z) > config.getSunRadius())
		{
			return Collections.EMPTY_SET;
		}		
		
		Set<Location> result = new HashSet<Location>();
		for(int x=0; x <= config.getSunRadius(); ++x)
		{
			if (x > sunLocation.x) break;
			
			for(int y=0; y <= config.getSunRadius(); ++y)
			{
				if (y > sunLocation.y) break;
				
				Location l = new Location(sunLocation.x + x, sunLocation.y + y, z);
				if (SEPUtils.getDistance(l, sunLocation) <= config.getSunRadius())
				{
					result.add(l); // ++
					result.add(new Location(sunLocation.x + x, sunLocation.y - y, z)); // +-
					result.add(new Location(sunLocation.x - x, sunLocation.y + y, z)); // -+
					result.add(new Location(sunLocation.x - x, sunLocation.y - y, z)); // --
				}
			}
		}
		
		return result;
	}
	
	public static enum eStarshipSpecializationClass
	{
		ARTILLERY, DESTROYER, FIGHTER;
		
		public eStarshipSpecializationClass getBN()
		{
			return getBN(this);
		}
		
		static public eStarshipSpecializationClass getBN(eStarshipSpecializationClass starshipClass)
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

		public eStarshipSpecializationClass getTdT()
		{
			return getTdT(this);
		}
		
		static public eStarshipSpecializationClass getTdT(eStarshipSpecializationClass starshipClass)
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

		public static int compare(eStarshipSpecializationClass inst, eStarshipSpecializationClass to)
		{
			if (inst == to) return 0;
			if (inst == null) return Integer.MAX_VALUE;
			if (inst.getTdT() == to) return 1;
			if (inst.getBN() == to) return -1;

			throw new RuntimeException("Comparaison de classes impossible: \"" + inst + "\" et \"" + to + "\"");
		}
		
		public final int compareThisTo(eStarshipSpecializationClass to)
		{
			return compare(this, to);
		}
			
		@Override
		public String toString()
		{
			String upper = super.toString();
			return upper.substring(0, 1)+upper.substring(1).toLowerCase();
		}
	}
	
	/**
	 * Represent a Starship model.
	 */
	public static class StarshipTemplate implements Serializable, Comparable<StarshipTemplate>
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
	
	private static Set<StarshipTemplate> unspecializedTemplates;
	private static Map<String, StarshipTemplate> starshipTemplates;
	
	public static Set<StarshipTemplate> getStarshipTemplates()
	{
		if (starshipTemplates == null)
		{
			unspecializedTemplates = new LinkedHashSet<StarshipTemplate>();
						
			unspecializedTemplates.add(new StarshipTemplate("Light", 10, 10, null, 0.1, 0.1, 3, 100, 100));
			unspecializedTemplates.add(new StarshipTemplate("Medium", 50, 50, null, 0.4, 0.4, 2, 300, 300));
			unspecializedTemplates.add(new StarshipTemplate("Heavy", 100, 100, null, 1.0, 1.0, 1, 600, 600));
			
			starshipTemplates = new LinkedHashMap<String, StarshipTemplate>();
			
			for(eStarshipSpecializationClass specialization : eStarshipSpecializationClass.values())
			{
				for(StarshipTemplate unspecialized : unspecializedTemplates)
				{
					StarshipTemplate template = new StarshipTemplate(unspecialized.getName()+specialization.toString(), unspecialized.getDefense(), unspecialized.getAttack(), specialization, unspecialized.getAttackSpecializationBonus(), unspecialized.getDefenseSpecializationBonus(), unspecialized.getSpeed(), unspecialized.getCarbonPrice(), unspecialized.getPopulationPrice());
					starshipTemplates.put(template.getName(), template);
				}
			}			
		}
		
		return new LinkedHashSet<StarshipTemplate>(starshipTemplates.values());
	}
	
	public static StarshipTemplate getStarshipTemplate(String templateName)
	{
		if (starshipTemplates == null) getStarshipTemplates();
		return starshipTemplates.get(templateName);
	}
}
