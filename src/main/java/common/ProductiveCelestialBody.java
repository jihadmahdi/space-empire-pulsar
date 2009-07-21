/**
 * @author Escallier Pierre
 * @file FilteredCelestialBody.java
 * @date 3 juin 2009
 */
package common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represent a productive celestial body. That is to say a celestial body that can produce carbon resource and has slots free to build.
 */
public abstract class ProductiveCelestialBody implements ICelestialBody, Serializable
{
	private static final long		serialVersionUID	= 1L;
	
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
	private final int				carbon;

	private final Set<IBuilding>	buildings;

	private final Player			owner;
	
	private final Map<StarshipTemplate, Integer>				unasignedFleetStarships; // For viewer point of view, not owner.
	private final Set<ISpecialUnit>								unasignedFleetSpecialUnits;

	/**
	 * Full constructor.
	 */
	public ProductiveCelestialBody(boolean isVisible, int lastObservation, String name, int startingCarbonStock, int carbonStock, int carbon, int slots, Set<IBuilding> buildings, Player owner, Map<StarshipTemplate, Integer> unasignedFleetStarships, Set<ISpecialUnit> unasignedFleetSpecialUnits)
	{
		this.isVisible = isVisible;
		this.lastObservation = lastObservation;
		this.name = name;
		this.startingCarbonStock = startingCarbonStock;
		this.carbonStock = carbonStock;
		this.carbon = carbon;
		this.slots = slots;
		this.buildings = buildings;
		this.owner = owner;
		if (unasignedFleetStarships == null)
		{
			this.unasignedFleetStarships = new HashMap<StarshipTemplate, Integer>();
		}
		else
		{
			this.unasignedFleetStarships = unasignedFleetStarships;
		}
		
		if (unasignedFleetSpecialUnits == null)
		{
			this.unasignedFleetSpecialUnits = new HashSet<ISpecialUnit>();
		}
		else
		{
			this.unasignedFleetSpecialUnits = unasignedFleetSpecialUnits;
		}
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

	public Set<IBuilding> getBuildings()
	{
		return buildings;
	}
	
	public <T extends IBuilding> T getBuilding(Class<T> buildingType)
	{
		if (buildings != null) for(IBuilding b : buildings)
		{
			if (buildingType.isInstance(b)) return buildingType.cast(b);
		}
		return null;
	}

	public Player getOwner()
	{
		return owner;
	}
	
	public String getOwnerName()
	{
		return Player.getName(owner);
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

		sb.append((owner == null) ? "" : "[" + owner.getName() + "] ");
		sb.append(name + " (" + getClass().getSimpleName() + ")\n");
		sb.append("  Carbon : " + carbon + " / " + carbonStock + " ("+startingCarbonStock+")\n");
		sb.append("  Slots : " + getBuildSlotsCount() + " / " + slots + "\n");
		if (buildings != null)
		{
			sb.append("  Buildings :\n");

			for (IBuilding b : buildings)
			{
				sb.append("    " + b.getClass().getSimpleName() + " : " + b.getBuildSlotsCount() + "\n");
			}
		}

		return sb.toString();
	}

	public int getBuildSlotsCount()
	{
		int i = 0;
		if (buildings != null) for (IBuilding b : buildings)
		{
			i += b.getBuildSlotsCount();
		}
		return i;
	}
	
	public int getFreeSlotsCount()
	{
		return slots - getBuildSlotsCount();
	}
	
	public Map<StarshipTemplate, Integer> getUnasignedStarships()
	{
		return unasignedFleetStarships;
	}
	
	public Set<ISpecialUnit> getUnasignedSpecialUnits()
	{
		return unasignedFleetSpecialUnits;
	}
	
	public abstract boolean canBuildType(Class<? extends IBuilding> buildingType);
}
