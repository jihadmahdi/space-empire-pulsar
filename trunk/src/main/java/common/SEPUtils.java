/**
 * @author Escallier Pierre
 * @file SEPUtils.java
 * @date 6 juin 2009
 */
package common;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;

import org.axan.eplib.utils.Basic;

/**
 * 
 */
public abstract class SEPUtils
{
	public static class Location implements Serializable
	{
		private static final long	serialVersionUID	= 1L;
		
		public final int x;
		public final int y;
		public final int z;
		
		public Location(String s)
		{
			String[] parts = Basic.split(s, ";");
			if (parts.length != 3) throw new IllegalArgumentException("Bad string format '"+s+"', '[x;y;z]' expected");
			this.x = Basic.intValueOf(parts[0].substring(1), -1);
			this.y = Basic.intValueOf(parts[1], -1);
			this.z = Basic.intValueOf(parts[2].substring(0, parts[2].length()-1), -1);
			
			if (x < 0 || y < 0 || z < 0) throw new IllegalArgumentException("Bad string format '"+s+"', '[x;y;z]' expected, with x, y, z positives or null.");
		}
		
		public Location(int x, int y, int z)
		{
			//if (x < 0 || y < 0 || z < 0) throw new IllegalArgumentException("Location coordinates cannot be negatives.");
			
			this.x = x;
			this.y = y;
			this.z = z;
		}
		
		public boolean equals(Location loc)
		{
			return x == loc.x && y == loc.y && z == loc.z; 
		}
		
		@Override
		public int hashCode()
		{
			return toString().hashCode();
		}
		
		@Override
		public String toString()
		{
			return "["+x+";"+y+";"+z+"]";
		}
	}
	
	public static double getDistance(Location a, Location b)
	{
		return Math.sqrt(Math.pow(a.x-b.x, 2) + Math.pow(a.y-b.y, 2) + Math.pow(a.z-b.z, 2));
	}
	
	public static Location getMobileEstimatedLocation(Location a, Location b, double progress, boolean stopOnB)
	{
		double[] loc = getMobileLocation(a, b, progress, stopOnB);
		return new Location((int) loc[0], (int) loc[1], (int) loc[2]);
	}
	public static double[] getMobileLocation(Location a, Location b, double progress, boolean stopOnB)
	{
		double x = a.x + (b.x - a.x)*progress;
		double y = a.y + (b.y - a.y)*progress;
		double z = a.z + (b.z - a.z)*progress;
		
		if (!stopOnB)
		{
			return new double[]{x, y, z};
		}
		else
		{
			return new double[]{(a.x<b.x?Math.min(x, b.x):Math.max(x, b.x)), (a.y<b.y?Math.min(y, b.y):Math.max(y, b.y)), (a.z<b.z?Math.min(z, b.z):Math.max(z, b.z))};
		}
	}
	
	public static Stack<Location> getAllPathLoc(Location a, Location b)
	{
		Stack<Location> result = new Stack<Location>();
		double d = getDistance(a, b);
		float delta = ((float) 1) / ((float) (2*d));
		Location lastLoc = null;
		Location loc;
		for(float t = 0; t < 1; t += delta)
		{
			loc = getMobileEstimatedLocation(a, b, t, true);
			if (lastLoc == null || !loc.equals(lastLoc))
			{
				result.add(loc);
				lastLoc = loc;
			}			
		}
		
		lastLoc = result.lastElement();
		if (lastLoc == null || !b.equals(lastLoc))
		{
			result.add(b);
		}
		
		return result;
	}
	
	public static final Set<Class<? extends IBuilding>> buildingTypes;
	
	static
	{
		Set<Class<? extends IBuilding>> buildingsTypesSet = new HashSet<Class<? extends IBuilding>>();
		buildingsTypesSet.add(DefenseModule.class);
		buildingsTypesSet.add(ExtractionModule.class);
		buildingsTypesSet.add(GovernmentModule.class);
		buildingsTypesSet.add(PulsarLauchingPad.class);
		buildingsTypesSet.add(SpaceCounter.class);
		buildingsTypesSet.add(StarshipPlant.class);
		buildingTypes = Collections.unmodifiableSet(buildingsTypesSet);
	}
	
	public static final Set<Class<? extends IStarship>> starshipTypes;
	
	static
	{
		Set<Class<? extends IStarship>> starshipTypesSet = new TreeSet<Class<? extends IStarship>>(new Comparator<Class<? extends IStarship>>()
		{
			public int compare(Class<? extends IStarship> o1, Class<? extends IStarship> o2)
			{
				return o1.toString().compareTo(o2.toString());
			}
		});
		
		starshipTypesSet.add(LightFighter.class);
		starshipTypesSet.add(LightDestroyer.class);
		starshipTypesSet.add(LightArtillery.class);
		starshipTypesSet.add(MediumFighter.class);
		starshipTypesSet.add(MediumDestroyer.class);
		starshipTypesSet.add(MediumArtillery.class);
		starshipTypesSet.add(HeavyFighter.class);
		starshipTypesSet.add(HeavyDestroyer.class);
		starshipTypesSet.add(HeavyArtillery.class);
		starshipTypes = Collections.unmodifiableSet(starshipTypesSet);
	}
	
	public static void main(String[] args)
	{
		Location A = new Location(2, 2, 0);
		Location B = new Location(9, 4, 0);
		System.out.println("getAllPathLoc("+A+", "+B+")");
		Stack<Location> path = getAllPathLoc(A, B);
		for(Location loc : path)
		{
			System.out.println(loc);
		}
	}
}
