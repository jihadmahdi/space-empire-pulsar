/**
 * @author Escallier Pierre
 * @file FilteredCelestialBody.java
 * @date 3 juin 2009
 */
package org.axan.sep.common;

import java.io.Serializable;
import java.util.Set;


/**
 * Represent a productive celestial body. That is to say a celestial body that can produce carbon resource and has slots free to build.
 */
public abstract class ProductiveCelestialBody implements ICelestialBody, Serializable
{
	private static final long		serialVersionUID	= 1L;
	
	public static class CelestialBodyBuildException extends Exception
	{
		private static final long	serialVersionUID	= 1L;

		public CelestialBodyBuildException(String msg)
		{
			super(msg);
		}
	}
	
	public static final int NATURAL_CARBON_PER_TURN = 2000; // TODO : A revoir
	
	public static final int MAX_NATURAL_CARBON = 2000;

	private final boolean			isVisible;

	private final int				lastObservation;

	// Constant
	private final String			name;

	private final int				startingCarbonStock;

	private final int				slots;

	// Only if visible
	private final int				carbonStock;
	private int				carbon;

	private final Set<ABuilding>	buildings;

	private final String			ownerName;
	
	/*
	private final Map<StarshipTemplate, Integer>				unasignedFleetStarships = new HashMap<StarshipTemplate, Integer>(); // For viewer point of view, not owner.
	private final Set<ISpecialUnit>								unasignedFleetSpecialUnits = new HashSet<ISpecialUnit>();
	*/
	
	//private final Map<String, Fleet> unasignedFleets = new HashMap<String, Fleet>();
	
	// Local
	private boolean attackEnemiesFleet = false;
	private boolean hasAlreadyBuiltThisTurn = false;

	/**
	 * Full constructor.
	 */
	public ProductiveCelestialBody(boolean isVisible, int lastObservation, String name, int startingCarbonStock, int carbonStock, int carbon, int slots, Set<ABuilding> buildings, String ownerName/*, Map<String, Fleet> unasignedFleets*/)
	{
		this.isVisible = isVisible;
		this.lastObservation = lastObservation;
		this.name = name;
		this.startingCarbonStock = startingCarbonStock;
		this.carbonStock = carbonStock;
		this.carbon = carbon;
		this.slots = slots;
		this.buildings = buildings;
		this.ownerName = ownerName;
		//if (unasignedFleets != null) this.unasignedFleets.putAll(unasignedFleets);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see common.Observable#getLastObservation()
	 */
	@Override
	public int getLastObservation()
	{
		return lastObservation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see common.Observable#isVisible()
	 */
	@Override
	public boolean isVisible()
	{
		return isVisible;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see common.ICelestialBody#getName()
	 */
	@Override
	public String getName()
	{
		return name;
	}

	public int getStartingCarbonStock()
	{
		return startingCarbonStock;
	}
	
	public int getCarbonStock()
	{
		return carbonStock;
	}

	public int getSlots()
	{
		return slots;
	}

	public int getCarbon()
	{
		return carbon;
	}

	public Set<ABuilding> getBuildings()
	{
		return buildings;
	}
	
	public <T extends ABuilding> T getBuilding(Class<T> buildingType)
	{
		if (buildings != null) for(ABuilding b : buildings)
		{
			if (buildingType.isInstance(b)) return buildingType.cast(b);
		}
		return null;
	}

	public String getOwnerName()
	{
		return ownerName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer();

		sb.append((ownerName == null) ? "" : "[" + ownerName + "] ");
		sb.append(name + " (" + getClass().getSimpleName() + ")\n");
		if (attackEnemiesFleet) {sb.append("Enemies fleet will be attacked next turn\n");}
		sb.append("  Carbon : " + carbon + " / " + carbonStock + " ("+startingCarbonStock+")\n");
		sb.append("  Slots : " + getBuildSlotsCount() + " / " + slots + "\n");
		if (buildings != null)
		{
			sb.append("  Buildings :\n");

			for (ABuilding b : buildings)
			{
				sb.append("    " + b.getClass().getSimpleName() + " : " + b.getBuildSlotsCount() + "\n");
			}
		}

		return sb.toString();
	}

	public int getBuildSlotsCount()
	{
		int i = 0;
		if (buildings != null) for (ABuilding b : buildings)
		{
			i += b.getBuildSlotsCount();
		}
		return i;
	}
	
	public int getFreeSlotsCount()
	{
		return slots - getBuildSlotsCount();
	}
	
	/*	
	public Fleet getUnasignedFleet(String ownerName)
	{
		return unasignedFleets.containsKey(ownerName) ? unasignedFleets.get(ownerName) : null;
	}
	
	public Map<String, Fleet> getUnasignedFleets()
	{
		return Collections.unmodifiableMap(unasignedFleets);
	}
	*/
	
	/*
	public Map<StarshipTemplate, Integer> getUnasignedStarships()
	{
		return unasignedFleetStarships;
	}
	
	public Set<ISpecialUnit> getUnasignedSpecialUnits()
	{
		return unasignedFleetSpecialUnits;
	}
	*/
	
	public abstract boolean canBuildType(Class<? extends ABuilding> buildingType);

	public void setAttackEnemiesFleetFlag(boolean attackEnemiesFleet)
	{
		this.attackEnemiesFleet = attackEnemiesFleet;
	}
	
	public boolean getAttackEnemiesFleetFlag()
	{
		return attackEnemiesFleet;
	}
	
	public boolean hasAlreadyBuiltThisTurn()
	{
		return hasAlreadyBuiltThisTurn;
	}
	
	public void setAlreadyBuiltThisTurn(boolean hasAlreadyBuiltThisTurn)
	{
		this.hasAlreadyBuiltThisTurn = hasAlreadyBuiltThisTurn;
	}
	
	public void updateBuilding(ABuilding building) throws CelestialBodyBuildException
	{
		ABuilding oldBuilding = null;
		
		int buildSlotsCount = 0;
		
		if (buildings != null) for(ABuilding b : buildings)
		{
			if (b.getClass().equals(building.getClass()))
			{
				buildSlotsCount += building.getBuildSlotsCount();
				oldBuilding = b;
			}
			else if (b != null)
			{
				buildSlotsCount += b.getBuildSlotsCount();
			}
		}
		
		if (oldBuilding == null)
		{
			buildSlotsCount += building.getBuildSlotsCount();
		}
		
		if (buildSlotsCount > slots) throw new CelestialBodyBuildException("Not enough free slots");
		
		if (oldBuilding != null) buildings.remove(oldBuilding);
		buildings.add(building);
	}
	
	void setCarbon(int carbon)
	{
		this.carbon = carbon;
	}
	
	public void downgradeBuilding(ABuilding existingBuilding)
	{
		ABuilding downgradedBuilding = existingBuilding.getDowngraded();
		if (downgradedBuilding == null)
		{
			buildings.remove(existingBuilding.getClass());
		}
		else
		{
			buildings.add(downgradedBuilding);
		}
	}
	
	public void removeBuilding(Class<? extends ABuilding> buildingType)
	{
		buildings.remove(getBuilding(buildingType));
	}
	
	/*
	public void removeFromUnasignedFleet(Map<StarshipTemplate, Integer> starshipsToRemove, Set<ISpecialUnit> specialUnitsToRemove)
	{
		Fleet unasignedFleet = getUnasignedFleet(getOwnerName());
		
		if (unasignedFleet == null) throw new SEPCommonImplementationException("Try to remove units from unexisting unasigned fleet");
		unasignedFleet.remove(starshipsToRemove, specialUnitsToRemove);								
	}
	
	public void mergeToUnasignedFleet(Map<StarshipTemplate, Integer> starshipsToMerge, Set<ISpecialUnit> specialUnitsToMerge)
	{
		Fleet unasignedFleet = getUnasignedFleet(getOwnerName());
		
		if (unasignedFleet == null)
		{
			unasignedFleet = new Fleet(true, lastObservation, todo, getOwnerName(), null, null, null, 0.0, 0.0, starshipsToMerge, specialUnitsToMerge, null, null, true);
			unasignedFleets.put(getOwnerName(), unasignedFleet);
		}
		else
		{
			unasignedFleet.merge(starshipsToMerge, specialUnitsToMerge);
		}
	}
	*/
}
