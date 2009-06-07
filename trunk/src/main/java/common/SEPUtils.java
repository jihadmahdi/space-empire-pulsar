/**
 * @author Escallier Pierre
 * @file SEPUtils.java
 * @date 6 juin 2009
 */
package common;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
}
