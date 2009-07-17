/**
 * @author Escallier Pierre
 * @file GameConfig.java
 * @date 29 mai 2009
 */
package common;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Map;

import common.SEPUtils.RealLocation;


/**
 * Represent a game creation configuration.
 */
public class GameConfig implements Serializable
{
	private static final long								serialVersionUID						= 1L;

	public static final int									UNIVERSE_DIM							= 20;
	/**
	 * X dimension of the universe.
	 */
	private int												dimX									= UNIVERSE_DIM;

	/**
	 * Y dimension of the universe.
	 */
	private int												dimY									= UNIVERSE_DIM;

	/**
	 * Z dimension of the universe.
	 */
	private int												dimZ									= UNIVERSE_DIM;

	public static final int									NEUTRAL_CELESTIAL_BODIES				= 5;
	/**
	 * Number of neutral celestial bodies to create.
	 */
	private int												neutralCelestialBodiesCount				= NEUTRAL_CELESTIAL_BODIES;

	/**
	 * Players starting planet carbon.
	 */
	private int												playersPlanetsStartingCarbonResources	= Planet.PLAYERS_STARTING_CARBON;

	/**
	 * Players starting planet population.
	 */
	private int												playersPlanetsStartingPopulation		= Planet.PLAYERS_STARTING_POPULATION;

	/**
	 * Starting carbon resource amount on players planets.
	 */
	private Map<Class<? extends ICelestialBody>, Integer[]>	celestialBodiesStartingCarbonAmount		= new Hashtable<Class<? extends ICelestialBody>, Integer[]>();

	/**
	 * Number of slots on celestial bodies.
	 */
	private Map<Class<? extends ICelestialBody>, Integer[]>	celestialBodiesSlotsAmount				= new Hashtable<Class<? extends ICelestialBody>, Integer[]>();

	/**
	 * Neutral celestial bodies generation, type table. This define the chance
	 * for a neutral celestial body to be one of the different celestial body
	 * type.
	 */
	private Map<Class<? extends ICelestialBody>, Float>		neutralCelestialBodiesGenerationTable	= new Hashtable<Class<? extends ICelestialBody>, Float>();

	public static final boolean								ALLIANCE_VICTORY						= false;
	/**
	 * Victory rule : Alliance victory. Whatever the victory condition, every
	 * allied winner are winners.
	 */
	private boolean											allianceVictory							= ALLIANCE_VICTORY;

	public static final boolean								REGIMICIDE								= true;
	/**
	 * Victory rule : Regimicide. Enable government modules and government
	 * starship to represent the player in its empire. If a player lose its
	 * governement he lose the game and its people fall into enemy people or
	 * neutral according to the "assimilateNeutralisedPeoples".
	 */
	private boolean											regimicide								= REGIMICIDE;

	public static final boolean								ASSIMILATE_NEUTRALISED_PEOPLES			= false;
	/**
	 * Option for the victory rule : Regimicide. If option is on, peoples whose
	 * government is destroyed are assimilated to the empire of the player who
	 * destroyed the government. If option if off, peoples whose government is
	 * destroyed are broke up in wild planets.
	 */
	private boolean											assimilateNeutralisedPeoples			= ASSIMILATE_NEUTRALISED_PEOPLES;

	public static final boolean								TOTAL_CONQUEST							= true;
	/**
	 * Victory rule : Total conquest. The first player who conquer all celestial
	 * bodies and destroy all enemies fleet win the game. Note : You have to
	 * disable every other victory rules to make the most of this mode.
	 */
	private boolean											totalConquest							= TOTAL_CONQUEST;

	public static final int									ECONOMIC_VICTORY_POPULATION				= 0;
	public static final int									ECONOMIC_VICTORY_CARBON					= 0;
	/**
	 * Victory rule : Economic victory. The first player who reach the carbon
	 * resource and population goals win the game. [0] population goal; [1]
	 * carbon resource goal; Null values to disable.
	 */
	private int[]											economicVictory							= new int[] { ECONOMIC_VICTORY_POPULATION,
			ECONOMIC_VICTORY_CARBON																};

	public static final int									TIME_LIMIT_VICTORY						= 0;
	/**
	 * Victory rule : Time limit. The game ends after a fixed number of turns,
	 * the player with the best score is the winner. Null value to disable.
	 */
	private int												timeLimitVictory						= TIME_LIMIT_VICTORY;

	/**
	 * Population generated on a planet per turn. [0] min; [1] max.
	 */
	private int[]											populationPerTurn						= new int[] { common.Planet.POPULATION_PER_TURN_MIN,
			common.Planet.POPULATION_PER_TURN_MAX													};

	/**
	 * Population limit on a planet. [0] min; [1] max.
	 */
	private int[]											populationLimit							= new int[] { common.Planet.POPULATION_LIMIT_MIN,
			common.Planet.POPULATION_LIMIT_MAX														};

	/**
	 * Deployed probe detection scope.
	 */
	private int												probeScope								= common.Probe.PROBE_SCORE;

	public static final int									SUN_RADIUS								= 3;
	/**
	 * Sun radius (0: sun volume is 1; 1: sun volume is 9; 2: sun volume is 125;
	 * n: sun volume is (2n+1)^3 );
	 */
	private int												sunRadius								= SUN_RADIUS;

	/**
	 * Empty constructor. Default config.
	 */
	public GameConfig()
	{
		setDimX(UNIVERSE_DIM);
		setDimY(UNIVERSE_DIM);
		setDimZ(UNIVERSE_DIM);

		setNeutralCelestialBodiesCount(NEUTRAL_CELESTIAL_BODIES);

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

		setPlayersPlanetsStartingPopulation(Planet.PLAYERS_STARTING_POPULATION);
		setPlayersPlanetsStartingCarbonResources(Planet.PLAYERS_STARTING_CARBON);

		setAllianceVictory(ALLIANCE_VICTORY);
		setRegimicide(REGIMICIDE);
		setAssimilateNeutralisedPeoples(ASSIMILATE_NEUTRALISED_PEOPLES);
		setTotalConquest(TOTAL_CONQUEST);
		setEconomicVictory(ECONOMIC_VICTORY_POPULATION, ECONOMIC_VICTORY_CARBON);
		setTimeLimitVictory(TIME_LIMIT_VICTORY);

		setProbeScope(Probe.PROBE_SCORE);
		setSunRadius(SUN_RADIUS);
	}

	public GameConfig(int dimX, int dimY, int dimZ, int neutralCelesialBodiesCount, int populationPerTurnMin, int populationPerTurnMax, int populationLimitMin, int populationLimitMax, Map<Class<? extends ICelestialBody>, Integer[]> celestialBodiesStartingCarbonAmount, Map<Class<? extends ICelestialBody>, Integer[]> celestialBodiesSlotsAmount, Map<Class<? extends ICelestialBody>, Float> neutralCelestialBodiesGenerationTable, boolean allianceVictory, boolean regimicide, boolean assimilateNeutralisedPeoples, boolean totalConquest, int economicVictoryCarbon, int economicVictoryPopulation, int timeLimitVictory, int probeScope, int sunRadius, int playersPlanetsStartingCarbonResources, int playersPlanetsStartingPopulation)
	{
		// TODO: call with right parameters

		setDimX(dimX);
		setDimY(dimY);
		setDimZ(dimZ);

		setNeutralCelestialBodiesCount(neutralCelesialBodiesCount);

		setPopulationPerTurn(populationPerTurnMin, populationPerTurnMax);
		setPopulationLimit(populationLimitMin, populationLimitMax);

		if (celestialBodiesStartingCarbonAmount != null)
			for(Map.Entry<Class<? extends ICelestialBody>, Integer[]> e : celestialBodiesStartingCarbonAmount.entrySet())
			{
				if (e.getValue() == null) continue;
				if (e.getValue().length < 2) continue;
				if (e.getValue()[0] == null || e.getValue()[1] == null) continue;

				setCelestialBodiesStartingCarbonAmount(e.getKey(), e.getValue()[0], e.getValue()[1]);
			}

		setPlayersPlanetsStartingCarbonResources(playersPlanetsStartingCarbonResources);
		setPlayersPlanetsStartingPopulation(playersPlanetsStartingPopulation);
		
		if (celestialBodiesSlotsAmount != null) for(Map.Entry<Class<? extends ICelestialBody>, Integer[]> e : celestialBodiesSlotsAmount.entrySet())
		{
			if (e.getValue() == null) continue;
			if (e.getValue().length < 2) continue;
			if (e.getValue()[0] == null || e.getValue()[1] == null) continue;

			setCelestialBodiesSlotsAmount(e.getKey(), e.getValue()[0], e.getValue()[1]);
		}

		if (neutralCelestialBodiesGenerationTable != null)
			for(Map.Entry<Class<? extends ICelestialBody>, Float> e : neutralCelestialBodiesGenerationTable.entrySet())
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

		setProbeScope(probeScope);
		setSunRadius(sunRadius);
	}

	/**
	 * @return the dimX
	 */
	public int getDimX()
	{
		return dimX;
	}

	/**
	 * @param dimX
	 *            the dimX to set
	 */
	public void setDimX(int dimX)
	{
		if (dimX <= 0) throw new IllegalArgumentException("dimX must be greater than 0.");
		universeVolumeCheck(dimX, getDimY(), getDimZ(), getSunRadius(), getNeutralCelestialBodiesCount());
		this.dimX = dimX;
	}

	private void universeVolumeCheck(int dimX, int dimY, int dimZ, int sunRadius, int neutralCelestialBodiesCount)
	{
		if (getSunVolume(dimX, dimY, dimZ, sunRadius) >= (getUniverseVolume(dimX, dimY, dimZ) - neutralCelestialBodiesCount))
		{
			throw new IllegalArgumentException("Universe must be strictly bigger than sun and have enough free space for neutral celestial bodies.");
		}
	}

	public int getFreeAreaCount()
	{
		return getUniverseVolume(getDimX(), getDimY(), getDimZ()) - getNeutralCelestialBodiesCount()
				- getSunVolume(getDimX(), getDimY(), getDimZ(), getSunRadius());
	}

	static private int getUniverseVolume(int dimX, int dimY, int dimZ)
	{
		return dimX * dimY * dimZ;
	}

	static private int getSunVolume(int dimX, int dimY, int dimZ, int sunRadius)
	{
		RealLocation origin = new RealLocation( 0, 0, 0);
		int volume = 0;

		for(int x = -Math.min(dimX - 1, sunRadius); x <= Math.min(dimX - 1, sunRadius); ++x)
			for(int y = -Math.min(dimY - 1, sunRadius); y <= Math.min(dimY - 1, sunRadius); ++y)
				for(int z = -Math.min(dimZ - 1, sunRadius); z <= Math.min(dimZ - 1, sunRadius); ++z)
				{
					if (SEPUtils.getDistance(new RealLocation(x, y, z), origin) <= sunRadius)
					{
						++volume;
					}
				}

		return volume;
	}

	/**
	 * @return the dimY
	 */
	public int getDimY()
	{
		return dimY;
	}

	/**
	 * @param dimY
	 *            the dimY to set
	 */
	public void setDimY(int dimY)
	{
		if (dimY <= 0) throw new IllegalArgumentException("dimY must be greater than 0.");
		universeVolumeCheck(getDimX(), dimY, getDimZ(), getSunRadius(), getNeutralCelestialBodiesCount());
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
	 * @param dimZ
	 *            the dimZ to set
	 */
	public void setDimZ(int dimZ)
	{
		if (dimZ <= 0) throw new IllegalArgumentException("dimZ must be greater than 0.");
		universeVolumeCheck(getDimX(), getDimY(), dimZ, getSunRadius(), getNeutralCelestialBodiesCount());
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
	 * @param neutralCelestialBodiesCount
	 *            the neutralCelestialBodiesCount to set
	 */
	public void setNeutralCelestialBodiesCount(int neutralCelestialBodiesCount)
	{
		if (neutralCelestialBodiesCount < 0) throw new IllegalArgumentException("neutralCelestialBodiesCount cannot must be greater or equal to 0.");
		universeVolumeCheck(getDimX(), getDimY(), getDimZ(), getSunRadius(), neutralCelestialBodiesCount);
		this.neutralCelestialBodiesCount = neutralCelestialBodiesCount;
	}

	public int getPlayersPlanetsStartingCarbonResources()
	{
		return playersPlanetsStartingCarbonResources;
	}

	public void setPlayersPlanetsStartingCarbonResources(int playersPlanetsStartingCarbonResources)
	{
		if (playersPlanetsStartingCarbonResources < 0) throw new IllegalArgumentException("Players starting carbon amount cannot be negative.");
		if (playersPlanetsStartingCarbonResources > celestialBodiesStartingCarbonAmount.get(Planet.class)[0])
			throw new IllegalArgumentException("Players starting carbon amount must be lesser than planets minimum starting carbon value.");
		this.playersPlanetsStartingCarbonResources = playersPlanetsStartingCarbonResources;
	}

	public int getPlayersPlanetsStartingPopulation()
	{
		return playersPlanetsStartingPopulation;
	}

	public void setPlayersPlanetsStartingPopulation(int playersPlanetsStartingPopulation)
	{
		if (playersPlanetsStartingPopulation < 0) throw new IllegalArgumentException("Players starting population cannot be negative.");
		if (playersPlanetsStartingPopulation > populationLimit[0])
			throw new IllegalArgumentException("Players starting population must be lesser then population minimum limit.");
		this.playersPlanetsStartingPopulation = playersPlanetsStartingPopulation;
	}

	/**
	 * @param populationPerTurnMin
	 *            Minimum value for population generation on a planet.
	 * @param populationPerTurnMax
	 *            Maximum value for population generation on a planet.
	 */
	public void setPopulationPerTurn(int populationPerTurnMin, int populationPerTurnMax)
	{
		if (populationPerTurnMin < 0) throw new IllegalArgumentException("populationPerTurnMin　must be positive or null.");
		if (populationPerTurnMax < populationPerTurnMin)
			throw new IllegalArgumentException("populationPerTurnMax　must be greater or equal to populationPerTurnMin.");
		this.populationPerTurn = new int[] { populationPerTurnMin, populationPerTurnMax };
	}

	/**
	 * @return the populationPerTurn range ([0] min; [1] max).
	 */
	public int[] getPopulationPerTurn()
	{
		return populationPerTurn;
	}

	/**
	 * @param populationLimitMin
	 *            Minimum value for population limit on a planet.
	 * @param populationLimitMax
	 *            Maximum value for population limit on a planet.
	 */
	public void setPopulationLimit(int populationLimitMin, int populationLimitMax)
	{
		if (populationLimitMin < 0) throw new IllegalArgumentException("populationLimitMin　must be positive or null.");
		if (populationLimitMax < populationLimitMin) throw new IllegalArgumentException("populationLimitMax　must be greater or equal to populationLimitMin.");
		if (populationLimitMin < playersPlanetsStartingPopulation)
			throw new IllegalArgumentException("populationLimitMin must be greater or equal to players starting population.");
		this.populationLimit = new int[] { populationLimitMin, populationLimitMax };
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
	 * @param celestialBodiesStartingCarbonAmount
	 *            the celestialBodiesStartingCarbonAmount to set
	 */
	public void setCelestialBodiesStartingCarbonAmount(Class<? extends ICelestialBody> celestialBodyType, int min, int max)
	{
		if (min <= 0) throw new IllegalArgumentException("minimum carbon amount must be positive or null.");
		if (max <= 0) throw new IllegalArgumentException("minimum carbon amount must be positive or null.");
		if (max < min) throw new IllegalArgumentException("minimum carbon amount must be lesser than maximum.");

		if (Planet.class.equals(celestialBodyType) && min < playersPlanetsStartingCarbonResources)
			throw new IllegalArgumentException("Planets minimum carbon amount must be greater or equal to players starting carbon amount.");

		this.celestialBodiesStartingCarbonAmount.put(celestialBodyType, new Integer[] { min, max });
	}

	/**
	 * @return the celestialBodiesSlotsAmount
	 */
	public Map<Class<? extends ICelestialBody>, Integer[]> getCelestialBodiesSlotsAmount()
	{
		return celestialBodiesSlotsAmount;
	}

	/**
	 * @param celestialBodiesSlotsAmount
	 *            the qtSlotsCorpsCelestes to set
	 */
	public void setCelestialBodiesSlotsAmount(Class<? extends ICelestialBody> celestialBodyType, int min, int max)
	{
		if (min <= 0) throw new IllegalArgumentException("minimum slots amount must be positive or null.");
		if (max <= 0) throw new IllegalArgumentException("minimum slots amount must be positive or null.");
		if (max < min) throw new IllegalArgumentException("minimum slots amount must be lesser than maximum.");
		this.celestialBodiesSlotsAmount.put(celestialBodyType, new Integer[] { min, max });
	}

	/**
	 * @return the neutralCelestialBodiesGenerationTable
	 */
	public Map<Class<? extends ICelestialBody>, Float> getNeutralCelestialBodiesGenerationTable()
	{
		return neutralCelestialBodiesGenerationTable;
	}

	/**
	 * @param celestialBodyType
	 *            Celestial body type.
	 * @param rate
	 *            percentage to be generated (not a true percentage but a
	 *            weight).
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
	 * @param allianceVictory
	 *            the allianceVictory to set
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
	 * @param regimicide
	 *            the regimicide to set
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
	 * @param assimilateNeutralisedPeoples
	 *            the assimilateNeutralisedPeoples to set
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
	 * @param totalConquest
	 *            the totalConquest to set
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
	 * @param economicVictory
	 *            the economicVictory to set
	 */
	public void setEconomicVictory(int populationGoal, int carbonResourceGoal)
	{
		if (populationGoal < 0) throw new IllegalArgumentException("populationGoal　must be positive or null.");
		if (carbonResourceGoal < 0) throw new IllegalArgumentException("carbonResourceGoal　must be positive or null.");
		this.economicVictory = new int[] { populationGoal, carbonResourceGoal };
	}

	/**
	 * @return the timeLimitVictory
	 */
	public Integer getTimeLimitVictory()
	{
		return timeLimitVictory;
	}

	/**
	 * @param timeLimitVictory
	 *            the timeLimitVictory to set
	 */
	public void setTimeLimitVictory(Integer timeLimitVictory)
	{
		if (timeLimitVictory < 0) throw new IllegalArgumentException("timeLimitVictory　must be positive or null.");
		this.timeLimitVictory = timeLimitVictory;
	}

	public int getProbeScope()
	{
		return probeScope;
	}

	public void setProbeScope(int probeScope)
	{
		if (probeScope < 0) throw new IllegalArgumentException("Probe scope cannot be negative.");
		this.probeScope = probeScope;
	}

	public int getSunRadius()
	{
		return sunRadius;
	}

	public void setSunRadius(int sunRadius)
	{
		if (sunRadius < 0) throw new IllegalArgumentException("Sun radius cannot be negative.");
		this.sunRadius = sunRadius;
		universeVolumeCheck(getDimX(), getDimY(), getDimZ(), sunRadius, getNeutralCelestialBodiesCount());
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("Universe (" + dimX + ";" + dimY + ";" + dimZ + "); ");
		sb.append("Neutrals " + neutralCelestialBodiesCount + "; ");
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
			sb.append("EconomicVictory(" + economicVictory[0] + ";" + economicVictory[1] + "); ");
		}
		if (timeLimitVictory > 0)
		{
			sb.append("TimeLimitVictory(" + timeLimitVictory + "); ");
		}

		return sb.toString();
	}
}