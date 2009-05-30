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
	private int	dimX	= 20;

	/** 
	 * Y dimension of the universe.
	 */
	private int	dimY	= 20;

	/**
	 * Z dimension of the universe.
	 */
	private int	dimZ	= 20;

	/**
	 * Number of neutral celestial bodies to create.
	 */
	private int	neutralCelestialBodiesCount	= 0;

	/**
	 * Starting carbon resource amount on players planets.
	 */
	private Map<Class<? extends CelestialBody>, Integer[]>	celestialBodiesStartingCarbonAmount	= new Hashtable<Class<? extends CelestialBody>, Integer[]>();

	/**
	 * Number of slots on celestial bodies.
	 */
	private Map<Class<? extends CelestialBody>, Integer[]>	celestialBodiesSlotsAmount	= new Hashtable<Class<? extends CelestialBody>, Integer[]>();
	
	/**
	 * Victory rule : Alliance victory.
	 * Whatever the victory condition, every allied winner are winners.
	 */
	private boolean allianceVictory = false;
	
	/**
	 * Victory rule : Regimicide.
	 * Enable government modules and government starship to represent the player in its empire.
	 * If a player lose its governement he lose the game and its people fall into enemy people or neutral according to the "assimilateNeutralisedPeoples".
	 */
	private boolean regimicide = true;

	/**
	 * Option for the victory rule : Regimicide.
	 * If option is on, peoples whose government is destroyed are assimilated to the empire of the player who destroyed the government.
	 * If option if off, peoples whose government is destroyed are broke up in wild planets.
	 */
	private boolean assimilateNeutralisedPeoples = false;
	
	/**
	 * Victory rule : Total conquest.
	 * The first player who conquer all celestial bodies and destroy all enemies fleet win the game.
	 * Note : You have to disable every other victory rules to make the most of this mode. 
	 */
	private boolean totalConquest = true;
	
	/**
	 * Victory rule : Economic victory.
	 * The first player who reach the carbon resource and population goals win the game.
	 * [0] population goal; [1] carbon resource goal;
	 * Null values to disable.
	 */
	private Integer[] economicVictory = {null, null};
	
	/**
	 * Victory rule : Time limit.
	 * The game ends after a fixed number of turns, the player with the best score is the winner.
	 * Null value to disable.
	 */
	private Integer timeLimitVictory = null;
	
	/**
	 * Empty constructor.
	 */
	public GameConfig()
	{
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
		this.neutralCelestialBodiesCount = neutralCelestialBodiesCount;
	}

	/**
	 * @return the celestialBodiesStartingCarbonAmount
	 */
	public Map<Class<? extends CelestialBody>, Integer[]> getCelestialBodiesStartingCarbonAmount()
	{
		return celestialBodiesStartingCarbonAmount;
	}

	/**
	 * @param celestialBodiesStartingCarbonAmount the celestialBodiesStartingCarbonAmount to set
	 */
	public void setCelestialBodiesStartingCarbonAmount(Class<? extends CelestialBody> celestialBodyType, int min, int max)
	{
		this.celestialBodiesStartingCarbonAmount.put(celestialBodyType, new Integer[] {min, max});
	}

	/**
	 * @return the celestialBodiesSlotsAmount
	 */
	public Map<Class<? extends CelestialBody>, Integer[]> getCelestialBodiesSlotsAmount()
	{
		return celestialBodiesSlotsAmount;
	}

	/**
	 * @param celestialBodiesSlotsAmount the qtSlotsCorpsCelestes to set
	 */
	public void setCelestialBodiesSlotsAmount(Class<? extends CelestialBody> celestialBodyType, int min, int max)
	{
		this.celestialBodiesSlotsAmount.put(celestialBodyType, new Integer[] {min, max});
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
	public Integer[] getEconomicVictory()
	{
		return economicVictory;
	}

	/**
	 * @param economicVictory the economicVictory to set
	 */
	public void setEconomicVictory(int populationGoal, int carbonResourceGoal)
	{
		this.economicVictory = new Integer[] {populationGoal, carbonResourceGoal};
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
		this.timeLimitVictory = timeLimitVictory;
	}
}