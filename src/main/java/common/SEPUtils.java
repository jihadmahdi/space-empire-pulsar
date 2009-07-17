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
	public static class RealLocation implements Serializable
	{
		private static final long	serialVersionUID	= 1L;
		
		public final double x;
		public final double y;
		public final double z;
		
		public RealLocation(double x, double y, double z)
		{			
			this.x = x;
			this.y = y;
			this.z = z;
		}

		public boolean equals(RealLocation loc)
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
			return String.format("[%.2f;%.2f;%.2f]", x, y, z);
		}

		public RealLocation asLocation()
		{
			return new RealLocation((int) x, (int) y, (int) z);
		}
	}
	
	/*
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
	*/		
	
	public static double getDistance(RealLocation a, RealLocation b)
	{
		return Math.sqrt(Math.pow(a.x-b.x, 2) + Math.pow(a.y-b.y, 2) + Math.pow(a.z-b.z, 2));
	}
	
	public static RealLocation getMobileEstimatedLocation(RealLocation a, RealLocation b, double progress, boolean stopOnB)
	{
		return getMobileLocation(a, b, progress, stopOnB);
	}
	public static RealLocation getMobileLocation(RealLocation a, RealLocation b, double progress, boolean stopOnB)
	{
		double x = a.x + (b.x - a.x)*progress;
		double y = a.y + (b.y - a.y)*progress;
		double z = a.z + (b.z - a.z)*progress;
		
		if (!stopOnB)
		{
			return new RealLocation(x, y, z);
		}
		else
		{
			return new RealLocation((a.x<b.x?Math.min(x, b.x):Math.max(x, b.x)), (a.y<b.y?Math.min(y, b.y):Math.max(y, b.y)), (a.z<b.z?Math.min(z, b.z):Math.max(z, b.z)));
		}
	}
	
	public static Stack<RealLocation> getAllPathLoc(RealLocation a, RealLocation b)
	{
		Stack<RealLocation> result = new Stack<RealLocation>();
		double d = getDistance(a, b);
		float delta = ((float) 1) / ((float) (2*d));
		RealLocation lastLoc = null;
		RealLocation loc;
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
		RealLocation A = new RealLocation(19, 4, 0);
		RealLocation B = new RealLocation(17, 12, 0);
		
		System.out.println("getAllPathLoc("+A+", "+B+")");
		Stack<RealLocation> path = getAllPathLoc(A, B);
		for(RealLocation loc : path)
		{
			System.out.println(loc);
		}
		
		System.out.println("\nRefresh path after each step");
		RealLocation loc;
		do
		{
			loc = path.get(1);
			System.out.println(loc);
			path = getAllPathLoc(loc, B);
		}while(!loc.equals(B));
	}
}
