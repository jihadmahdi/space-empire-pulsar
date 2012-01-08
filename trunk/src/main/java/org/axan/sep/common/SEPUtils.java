/**
 * @author Escallier Pierre
 * @file SEPUtils.java
 * @date 6 juin 2009
 */
package org.axan.sep.common;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.axan.eplib.utils.Basic;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.db.IGameConfig;


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

		@Override
		public boolean equals(Object obj)
		{
			if (!RealLocation.class.isInstance(obj)) return false;
			
			RealLocation loc = RealLocation.class.cast(obj);
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
		
		public Location asLocation()
		{
			return new Location((int) x, (int) y, (int) z);
		}
	}
	
	public static class Location implements Serializable
	{
		private static final long	serialVersionUID	= 1L;
		
		public final int x;
		public final int y;
		public final int z;
				
		public Location(int x, int y, int z)
		{			
			this.x = x;
			this.y = y;
			this.z = z;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (!Location.class.isInstance(obj)) return false;
			
			Location loc = Location.class.cast(obj);
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
			return String.format("[%d;%d;%d]", x, y, z);
		}
		
		public static Location valueOf(String s)
		{
			Pattern p = Pattern.compile("\\[(\\d++);(\\d++);(\\d++)\\]", Pattern.DOTALL);
			Matcher m = p.matcher(s);
			if (!m.matches()) throw new NumberFormatException("Location string '"+s+"' does not matches \"[\\d++;\\d++;\\d++]\" format.");
			int x = Integer.valueOf(m.group(1));
			int y = Integer.valueOf(m.group(2));
			int z = Integer.valueOf(m.group(3));
			return new Location(x, y, z);
		}
		
		public RealLocation asRealLocation()
		{
			return asRealLocation(0.5);
		}
		public RealLocation asRealLocation(double offset)
		{
			return asRealLocation(offset, offset, offset);
		}
		public RealLocation asRealLocation(double xOffset, double yOffset, double zOffset)
		{
			return new RealLocation(x+xOffset, y+yOffset, z+zOffset);
		}
	}		
	
	public static double getDistance(Location a, Location b)
	{
		return Math.sqrt(Math.pow(a.x-b.x, 2) + Math.pow(a.y-b.y, 2) + Math.pow(a.z-b.z, 2));
	}
	
	public static double getDistance(RealLocation a, RealLocation b)
	{
		return Math.sqrt(Math.pow(a.x-b.x, 2) + Math.pow(a.y-b.y, 2) + Math.pow(a.z-b.z, 2));
	}
	
	public static RealLocation getMobileLocation(RealLocation departure, RealLocation destination, double progress, boolean stopOnB)
	{
		double x = departure.x + (destination.x - departure.x)*progress;
		double y = departure.y + (destination.y - departure.y)*progress;
		double z = departure.z + (destination.z - departure.z)*progress;
		
		if (!stopOnB)
		{
			return new RealLocation(x, y, z);
		}
		else
		{
			return new RealLocation((departure.x<destination.x?Math.min(x, destination.x):Math.max(x, destination.x)), (departure.y<destination.y?Math.min(y, destination.y):Math.max(y, destination.y)), (departure.z<destination.z?Math.min(z, destination.z):Math.max(z, destination.z)));
		}
	}
	
	public static Stack<RealLocation> getAllPathLoc(RealLocation a, RealLocation b)
	{
		Stack<RealLocation> result = new Stack<RealLocation>();
		double d = getDistance(a, b);
		float delta = (1) / ((float) (2*d));
		RealLocation lastLoc = null;
		RealLocation loc;
		for(float t = 0; t < 1; t += delta)
		{
			loc = getMobileLocation(a, b, t, true);
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
	
	/*
	public static final Set<StarshipTemplate> starshipSizeTemplates;
	
	static
	{
		Set<StarshipTemplate> starshipSizeTemplatesSet = new TreeSet<StarshipTemplate>();
		
		starshipSizeTemplatesSet.add(new StarshipTemplate("Light", 10, 10, null, 0.1, 0.1, 3, 100, 100));
		starshipSizeTemplatesSet.add(new StarshipTemplate("Medium", 50, 50, null, 0.4, 0.4, 2, 300, 300));
		starshipSizeTemplatesSet.add(new StarshipTemplate("Heavy", 100, 100, null, 1.0, 1.0, 1, 600, 600));
		
		//starshipSizeTemplates = Collections.unmodifiableSet(starshipSizeTemplatesSet);
		starshipSizeTemplates = starshipSizeTemplatesSet;
	}
	
	public static final Set<StarshipTemplate> starshipTypes;
	
	static
	{		
		Set<StarshipTemplate> starshipTypesSet = new TreeSet<StarshipTemplate>();
				
		for(StarshipTemplate gabarit : starshipSizeTemplates)
		{
			for(eStarshipSpecializationClass specializationClass : eStarshipSpecializationClass.values())
			{
				starshipTypesSet.add(new StarshipTemplate(gabarit.getName()+specializationClass.toString(), gabarit.getDefense(), gabarit.getAttack(), specializationClass, gabarit.getAttackSpecializationBonus(), gabarit.getDefenseSpecializationBonus(), gabarit.getSpeed(), gabarit.getCarbonPrice(), gabarit.getPopulationPrice()));
			}
		}		
		
		starshipTypes = Collections.unmodifiableSet(starshipTypesSet);		
	}
	*/
	
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
	
	public static String getSaveFileName(String saveId)
	{		
		return "SEPGameSave_" + saveId + ".sav";
	}
	
	public static String getSaveID(String saveFileName)
	{
		if (!saveFileName.startsWith("SEPGameSave_")) throw new IllegalArgumentException("Saves filenames expected to start with 'SEPGameSave_'");
		if (!saveFileName.endsWith(".sav")) throw new IllegalArgumentException("Saves filenames expected to end with '.sav'");
		
		return saveFileName.substring("SEPGameSave_".length(), saveFileName.length() - ".sav".length());
	}
	
	public static String SAVE_SUBDIR = "saves"+File.separatorChar;
	static
	{
		File dir = new File(SAVE_SUBDIR);
		if (!dir.exists())
		{
			try
			{
				dir.mkdir();
			}
			catch(Throwable t)
			{
				System.err.println("Cannot make saves games directory.");
			}
		}
	}
	
	public static Map<eCelestialBodyType, Float> getNeutralCelestialBodiesGenerationTable(IGameConfig config)
	{
		Map<eCelestialBodyType, Float> result = new HashMap<Protocol.eCelestialBodyType, Float>();
		for(eCelestialBodyType type : eCelestialBodyType.values())
		{
			Float rate = config.getNeutralCelestialBodiesGenerationRate(type);
			result.put(type, rate == null ? 0F : rate);
		}
		
		return result;
	}
}
