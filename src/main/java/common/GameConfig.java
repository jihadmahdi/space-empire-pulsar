/**
 * @author Escallier Pierre
 * @file GameConfig.java
 * @date 29 mai 2009
 */
package common;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Map;

/**
 * Represent a game creation configuration.
 */
public class GameConfig implements Serializable
{
	private static final long	serialVersionUID	= 1L;

	/** 
	 * X dimension of the universe.
	 */
	private int	dimX;

	/** 
	 * Y dimension of the universe.
	 */
	private int	dimY;

	/**
	 * Z dimension of the universe.
	 */
	private int	dimZ;

	/**
	 * Number of neutral celestial bodies to create.
	 */
	private int	neutralCelestialBodiesCount;

	/**
	 * Starting carbon resource amount on players planets.
	 */
	private Map<Class<? extends ICelestialBody>, Integer[]>	celestialBodiesStartingCarbonAmount	= new Hashtable<Class<? extends ICelestialBody>, Integer[]>();

	/**
	 * Number of slots on celestial bodies.
	 */
	private Map<Class<? extends ICelestialBody>, Integer[]>	celestialBodiesSlotsAmount	= new Hashtable<Class<? extends ICelestialBody>, Integer[]>();
	
	/**
	 * Neutral celestial bodies generation, type table.
	 * This define the chance for a neutral celestial body to be one of the different celestial body type. 
	 */
	private Map<Class<? extends ICelestialBody>, Float> neutralCelestialBodiesGenerationTable = new Hashtable<Class<? extends ICelestialBody>, Float>();
	
	/**
	 * Victory rule : Alliance victory.
	 * Whatever the victory condition, every allied winner are winners.
	 */
	private boolean allianceVictory;
	
	/**
	 * Victory rule : Regimicide.
	 * Enable government modules and government starship to represent the player in its empire.
	 * If a player lose its governement he lose the game and its people fall into enemy people or neutral according to the "assimilateNeutralisedPeoples".
	 */
	private boolean regimicide;

	/**
	 * Option for the victory rule : Regimicide.
	 * If option is on, peoples whose government is destroyed are assimilated to the empire of the player who destroyed the government.
	 * If option if off, peoples whose government is destroyed are broke up in wild planets.
	 */
	private boolean assimilateNeutralisedPeoples;
	
	/**
	 * Victory rule : Total conquest.
	 * The first player who conquer all celestial bodies and destroy all enemies fleet win the game.
	 * Note : You have to disable every other victory rules to make the most of this mode. 
	 */
	private boolean totalConquest;
	
	/**
	 * Victory rule : Economic victory.
	 * The first player who reach the carbon resource and population goals win the game.
	 * [0] population goal; [1] carbon resource goal;
	 * Null values to disable.
	 */
	private int[] economicVictory;
	
	/**
	 * Victory rule : Time limit.
	 * The game ends after a fixed number of turns, the player with the best score is the winner.
	 * Null value to disable.
	 */
	private int timeLimitVictory;
	
	/**
	 * Population generated on a planet per turn.
	 * [0] min; [1] max. 
	 */
	private int[] populationPerTurn;
	
	/**
	 * Population limit on a planet.
	 * [0] min; [1] max.
	 */
	private int[] populationLimit;
	
	/**
	 * Empty constructor.
	 * Default config.
	 */
	public GameConfig()
	{
		setDimX(20);
		setDimY(20);
		setDimZ(20);
		
		setNeutralCelestialBodiesCount(5);
		
		setPopulationPerTurn(Planet.POPULATION_PER_TURN_MIN, Planet.POPULATION_PER_TURN_MAX);
		setPopulationLimit(Planet.POPULATION_LIMIT_MIN, Planet.POPULATION_LIMIT_MAX);
		
		setCelestialBodiesStartingCarbonAmount(Planet.class, Planet.CARBON_MIN, Planet.CARBON_MAX);
		setCelestialBodiesStartingCarbonAmount(AsteroidField.class, AsteroidField.CARBON_MIN, AsteroidField.CARBON_MAX);
		setCelestialBodiesStartingCarbonAmount(Nebula.class, Nebula.CARBON_MIN, Nebula.CARBON_MAX);
		
		setCelestialBodiesSlotsAmount(Planet.class, Planet.SLOTS_MIN, Planet.SLOTS_MAX);
		setCelestialBodiesSlotsAmount(AsteroidField.class, AsteroidField.SLOTS_MIN, AsteroidField.SLOTS_MAX);
		setCelestialBodiesSlotsAmount(Nebula.class, Nebula.SLOTS_MIN, Nebula.SLOTS_MAX);
		
		setNeutralCelestialBodiesGenerationTable(Planet.class, Planet.GENERATION_RATE);
		setNeutralCelestialBodiesGenerationTable(AsteroidField.class, AsteroidField.GENERATION_RATE);
		setNeutralCelestialBodiesGenerationTable(Nebula.class, Nebula.GENERATION_RATE);
		
		setAllianceVictory(false);
		setRegimicide(true);
		setAssimilateNeutralisedPeoples(false);
		setTotalConquest(true);
		setEconomicVictory(0, 0);		
		setTimeLimitVictory(0);
	}
	
	public GameConfig(int dimX, int dimY, int dimZ, int neutralCelesialBodiesCount, int populationPerTurnMin, int populationPerTurnMax, int populationLimitMin, int populationLimitMax, Map<Class<? extends ICelestialBody>, Integer[]> celestialBodiesStartingCarbonAmount, Map<Class<? extends ICelestialBody>, Integer[]> celestialBodiesSlotsAmount, Map<Class<? extends ICelestialBody>, Float> neutralCelestialBodiesGenerationTable, boolean allianceVictory, boolean regimicide, boolean assimilateNeutralisedPeoples, boolean totalConquest, int economicVictoryCarbon, int economicVictoryPopulation, int timeLimitVictory)
	{
		// TODO: call with ritgh parameters
		
		setDimX(dimX);
		setDimY(dimY);
		setDimZ(dimZ);
		
		setNeutralCelestialBodiesCount(neutralCelesialBodiesCount);
		
		setPopulationPerTurn(populationPerTurnMin, populationPerTurnMax);
		setPopulationLimit(populationLimitMin, populationLimitMax);
		
		if (celestialBodiesStartingCarbonAmount != null) for(Map.Entry<Class<? extends ICelestialBody>, Integer[]> e : celestialBodiesStartingCarbonAmount.entrySet())
		{
			if (e.getValue() == null) continue;
			if (e.getValue().length < 2) continue;
			if (e.getValue()[0] == null || e.getValue()[1] == null) continue;
			
			setCelestialBodiesStartingCarbonAmount(e.getKey(), e.getValue()[0], e.getValue()[1]);
		}
		
		if (celestialBodiesSlotsAmount != null) for(Map.Entry<Class<? extends ICelestialBody>, Integer[]> e : celestialBodiesSlotsAmount.entrySet())
		{
			if (e.getValue() == null) continue;
			if (e.getValue().length < 2) continue;
			if (e.getValue()[0] == null || e.getValue()[1] == null) continue;
			
			setCelestialBodiesSlotsAmount(e.getKey(), e.getValue()[0], e.getValue()[1]);
		}
		
		if (neutralCelestialBodiesGenerationTable != null) for(Map.Entry<Class<? extends ICelestialBody>, Float> e : neutralCelestialBodiesGenerationTable.entrySet())
		{
			if (e.getValue() == null) continue;
			
			setNeutralCelestialBodiesGenerationTable(e.getKey(), e.getValue());
		}
		
		setAllianceVictory(allianceVictory);
		setRegimicide(regimicide);
		setAssimilateNeutralisedPeoples(assimilateNeutralisedPeoples);
		setTotalConquest(totalConquest);
		setEconomicVictory(economicVictoryPopulation, economicVictoryCarbon);		
		setTimeLimitVictory(timeLimitVictory);		
	}

	/**
	 * @return the dimX
	 */
	public int getDimX()
	{
		return dimX;
	}

	/**
	 * @param dimX the dimX to set
	 */
	public void setDimX(int dimX)
	{
		if (dimX < 5) throw new IllegalArgumentException("dimX cannot must be greater than 5.");
		this.dimX = dimX;
	}

	/**
	 * @return the dimY
	 */
	public int getDimY()
	{
		return dimY;
	}

	/**
	 * @param dimY the dimY to set
	 */
	public void setDimY(int dimY)
	{
		if (dimY < 5) throw new IllegalArgumentException("dimY cannot must be greater than 5.");
		this.dimY = dimY;
	}

	/**
	 * @return the dimZ
	 */
	public int getDimZ()
	{
		return dimZ;
	}

	/**
	 * @param dimZ the dimZ to set
	 */
	public void setDimZ(int dimZ)
	{
		if (dimZ < 5) throw new IllegalArgumentException("dimZ cannot must be greater than 5.");
		this.dimZ = dimZ;
	}

	/**
	 * @return the neutralCelestialBodiesCount
	 */
	public int getNeutralCelestialBodiesCount()
	{
		return neutralCelestialBodiesCount;
	}

	/**
	 * @param neutralCelestialBodiesCount the neutralCelestialBodiesCount to set
	 */
	public void setNeutralCelestialBodiesCount(int neutralCelestialBodiesCount)
	{
		if (neutralCelestialBodiesCount < 0) throw new IllegalArgumentException("neutralCelestialBodiesCount cannot must be greater or equal to 0.");
		this.neutralCelestialBodiesCount = neutralCelestialBodiesCount;
	}
	
	/**
	 * @param populationPerTurnMin Minimum value for population generation on a planet.
	 * @param populationPerTurnMax Maximum value for population generation on a planet.
	 */
	public void setPopulationPerTurn(int populationPerTurnMin, int populationPerTurnMax)
	{
		if (populationPerTurnMin < 0) throw new IllegalArgumentException("populationPerTurnMin　must be positive or null.");
		if (populationPerTurnMax < populationPerTurnMin) throw new IllegalArgumentException("populationPerTurnMax　must be greater or equal to populationPerTurnMin.");
		this.populationPerTurn = new int[] {populationPerTurnMin, populationPerTurnMax};
	}

	/**
	 * @return the populationPerTurn range ([0] min; [1] max).
	 */
	public int[] getPopulationPerTurn()
	{
		return populationPerTurn;
	}

	/**
	 * @param populationLimitMin Minimum value for population limit on a planet.
	 * @param populationLimitMax Maximum value for population limit on a planet.
	 */
	public void setPopulationLimit(int populationLimitMin, int populationLimitMax)
	{
		if (populationLimitMin < 0) throw new IllegalArgumentException("populationLimitMin　must be positive or null.");
		if (populationLimitMax < populationLimitMin) throw new IllegalArgumentException("populationLimitMax　must be greater or equal to populationLimitMin.");
		this.populationLimit = new int[] {populationLimitMin, populationLimitMax};
	}
	
	/**
	 * @return the populationLimit range ([0] min; [1] max).
	 */
	public int[] getPopulationLimit()
	{
		return populationLimit;
	}
	
	/**
	 * @return the celestialBodiesStartingCarbonAmount
	 */
	public Map<Class<? extends ICelestialBody>, Integer[]> getCelestialBodiesStartingCarbonAmount()
	{
		return celestialBodiesStartingCarbonAmount;
	}

	/**
	 * @param celestialBodiesStartingCarbonAmount the celestialBodiesStartingCarbonAmount to set
	 */
	public void setCelestialBodiesStartingCarbonAmount(Class<? extends ICelestialBody> celestialBodyType, int min, int max)
	{
		if (min <= 0) throw new IllegalArgumentException("minimum carbon amount must be positive or null.");
		if (max <= 0) throw new IllegalArgumentException("minimum carbon amount must be positive or null.");
		if (max < min) throw new IllegalArgumentException("minimum carbon amount must be lesser than maximum.");
		this.celestialBodiesStartingCarbonAmount.put(celestialBodyType, new Integer[] {min, max});
	}

	/**
	 * @return the celestialBodiesSlotsAmount
	 */
	public Map<Class<? extends ICelestialBody>, Integer[]> getCelestialBodiesSlotsAmount()
	{
		return celestialBodiesSlotsAmount;
	}

	/**
	 * @param celestialBodiesSlotsAmount the qtSlotsCorpsCelestes to set
	 */
	public void setCelestialBodiesSlotsAmount(Class<? extends ICelestialBody> celestialBodyType, int min, int max)
	{
		if (min <= 0) throw new IllegalArgumentException("minimum slots amount must be positive or null.");
		if (max <= 0) throw new IllegalArgumentException("minimum slots amount must be positive or null.");
		if (max < min) throw new IllegalArgumentException("minimum slots amount must be lesser than maximum.");
		this.celestialBodiesSlotsAmount.put(celestialBodyType, new Integer[] {min, max});
	}

	/**
	 * @return the neutralCelestialBodiesGenerationTable
	 */
	public Map<Class<? extends ICelestialBody>, Float> getNeutralCelestialBodiesGenerationTable()
	{
		return neutralCelestialBodiesGenerationTable;
	}
	
	/**
	 * @param celestialBodyType Celestial body type.
	 * @param rate percentage to be generated (not a true percentage but a weight).
	 */
	public void setNeutralCelestialBodiesGenerationTable(Class<? extends ICelestialBody> celestialBodyType, float rate)
	{
		if (rate < 0) throw new IllegalArgumentException("rate cannot be negative.");
		this.neutralCelestialBodiesGenerationTable.put(celestialBodyType, rate);
	}
	
	/**
	 * @return the allianceVictory
	 */
	public boolean isAllianceVictory()
	{
		return allianceVictory;
	}

	/**
	 * @param allianceVictory the allianceVictory to set
	 */
	public void setAllianceVictory(boolean allianceVictory)
	{
		this.allianceVictory = allianceVictory;
	}

	/**
	 * @return the regimicide
	 */
	public boolean isRegimicide()
	{
		return regimicide;
	}

	/**
	 * @param regimicide the regimicide to set
	 */
	public void setRegimicide(boolean regimicide)
	{
		this.regimicide = regimicide;
	}

	/**
	 * @return the assimilerPeuplesNeutralises
	 */
	public boolean isAssimilateNeutralisedPeoples()
	{
		return assimilateNeutralisedPeoples;
	}

	/**
	 * @param assimilateNeutralisedPeoples the assimilateNeutralisedPeoples to set
	 */
	public void setAssimilateNeutralisedPeoples(boolean assimilateNeutralisedPeoples)
	{
		this.assimilateNeutralisedPeoples = assimilateNeutralisedPeoples;
	}

	/**
	 * @return the conqueteTotale
	 */
	public boolean isTotalConquest()
	{
		return totalConquest;
	}

	/**
	 * @param totalConquest the totalConquest to set
	 */
	public void setTotalConquest(boolean totalConquest)
	{
		this.totalConquest = totalConquest;
	}

	/**
	 * @return the economicVictory
	 */
	public int[] getEconomicVictory()
	{
		return economicVictory;
	}

	/**
	 * @param economicVictory the economicVictory to set
	 */
	public void setEconomicVictory(int populationGoal, int carbonResourceGoal)
	{
		if (populationGoal < 0) throw new IllegalArgumentException("populationGoal　must be positive or null.");
		if (carbonResourceGoal < 0) throw new IllegalArgumentException("carbonResourceGoal　must be positive or null.");
		this.economicVictory = new int[] {populationGoal, carbonResourceGoal};
	}

	/**
	 * @return the timeLimitVictory
	 */
	public Integer getTimeLimitVictory()
	{
		return timeLimitVictory;
	}

	/**
	 * @param timeLimitVictory the timeLimitVictory to set
	 */
	public void setTimeLimitVictory(Integer timeLimitVictory)
	{
		if (timeLimitVictory < 0) throw new IllegalArgumentException("timeLimitVictory　must be positive or null.");
		this.timeLimitVictory = timeLimitVictory;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("Universe ("+dimX+";"+dimY+";"+dimZ+"); ");
		sb.append("Neutrals "+neutralCelestialBodiesCount+"; ");
		// TODO: Celestials carbon & slots amount
		if (allianceVictory) sb.append("AllianceVictory; ");
		if (regimicide)
		{
			sb.append("Regimicide");
			if (assimilateNeutralisedPeoples) sb.append(" (assimilatPeoples)");
			sb.append("; ");
		}
		if (totalConquest) sb.append("TotalConquest; ");
		if (economicVictory != null && economicVictory.length >= 2 && (economicVictory[0] > 0 || economicVictory[1] > 0))
		{
			sb.append("EconomicVictory("+economicVictory[0]+";"+economicVictory[1]+"); ");
		}
		if (timeLimitVictory > 0)
		{
			sb.append("TimeLimitVictory("+timeLimitVictory+"); ");
		}
		
		return sb.toString();
	}
}