package org.axan.sep.common;

@Deprecated // All references in server package are commented code.
public enum eStarshipSpecializationClass
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
