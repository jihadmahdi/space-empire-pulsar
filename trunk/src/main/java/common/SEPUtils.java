/**
 * @author Escallier Pierre
 * @file SEPUtils.java
 * @date 6 juin 2009
 */
package common;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/**
 * 
 */
public abstract class SEPUtils
{
	public static int getDistance(int[] a, int[] b)
	{
		return getDistance(a[0], a[1], a[2], b[0], b[1], b[2]);
	}
	public static int getDistance(int xA, int yA, int zA, int xB, int yB, int zB)
	{
		return (int) Math.sqrt(Math.pow(xA-xB, 2) + Math.pow(yA-yB, 2) + Math.pow(zA-zB, 2));
	}
	
	public static int[] getMobileEstimatedLocation(int xA, int yA, int zA, int xB, int yB, int zB, float progress)
	{
		float[] loc = getMobileLocation(xA, yA, zA, xB, yB, zB, progress);
		return new int[]{(int) loc[0], (int) loc[1], (int) loc[2]};
	}
	public static float[] getMobileLocation(int xA, int yA, int zA, int xB, int yB, int zB, float progress)
	{
		float x = xA + (xB - xA)*progress;
		float y = yA + (yB - yA)*progress;
		float z = zA + (zB - zA)*progress;
		
		return new float[]{x, y, z};
	}
	
	public static Stack<int[]> getAllPathLoc(int xA, int yA, int zA, int xB, int yB, int zB)
	{
		Stack<int[]> result = new Stack<int[]>();
		int d = getDistance(xA, yA, zA, xB, yB, zB);
		float delta = ((float) 1) / ((float) (2*d));
		int[] lastLoc = null;
		int[] loc;
		for(float t = 0; t < 1; t += delta)
		{
			loc = getMobileEstimatedLocation(xA, yA, zA, xB, yB, zB, t);
			if (lastLoc == null || loc[0] != lastLoc[0] || loc[1] != lastLoc[1] || loc[2] != lastLoc[2])
			{
				result.add(loc);
				lastLoc = loc;
			}			
		}
		
		lastLoc = result.lastElement();
		if (lastLoc == null || xB != lastLoc[0] || yB != lastLoc[1] || zB != lastLoc[2])
		{
			result.add(new int[]{xB, yB, zB});
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
	
	public static void main(String[] args)
	{
		int[] A = new int[]{2, 2, 0};
		int[] B = new int[]{9, 4, 0};
		System.out.println("getAllPathLoc("+Arrays.toString(A)+", "+Arrays.toString(B)+")");
		Stack<int[]> path = getAllPathLoc(A[0], A[1], A[2], B[0], B[1], B[2]);
		for(int[] loc : path)
		{
			System.out.println(Arrays.toString(loc));
		}
	}
}
