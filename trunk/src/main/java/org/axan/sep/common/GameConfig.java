/**
 * @author Escallier Pierre
 * @file GameConfig.java
 * @date 29 mai 2009
 */
package org.axan.sep.common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.RealLocation;
import org.axan.sep.common.db.IGameConfig;




/**
 * Represent a game creation configuration.
 */
public class GameConfig implements IGameConfig, Serializable
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
	private int												playersPlanetsStartingCarbonResources	= 2*1000;

	/**
	 * Players starting planet population.
	 */
	private int												playersPlanetsStartingPopulation		= 50*1000;

	/**
	 * Starting carbon resource amount on players planets.
	 */
	private Map<eCelestialBodyType, int[]>	celestialBodiesStartingCarbonAmount		= new Hashtable<eCelestialBodyType, int[]>();

	/**
	 * Number of slots on celestial bodies.
	 */
	private Map<eCelestialBodyType, int[]>	celestialBodiesSlotsAmount				= new Hashtable<eCelestialBodyType, int[]>();

	/**
	 * Neutral celestial bodies generation, type table. This define the chance
	 * for a neutral celestial body to be one of the different celestial body
	 * type.
	 */
	private Map<eCelestialBodyType, Float>		neutralCelestialBodiesGenerationTable	= new Hashtable<eCelestialBodyType, Float>();

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
	private int[]											populationPerTurn						= new int[] { 2500, 7500 };

	/**
	 * Population limit on a planet. [0] min; [1] max.
	 */
	private int[]											populationLimit							= new int[] { 50*1000, 150*1000	};

	/**
	 * Deployed probe detection scope.
	 */
	private int												probeScope								= 3;

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

		setPopulationPerTurn(2000, 7500);
		setPopulationLimit(50*1000, 150*1000);

		setCelestialBodiesStartingCarbonAmount(eCelestialBodyType.Planet, 50*1000, 100*1000);
		setCelestialBodiesStartingCarbonAmount(eCelestialBodyType.AsteroidField, 60*1000, 300*1000);
		setCelestialBodiesStartingCarbonAmount(eCelestialBodyType.Nebula, 100*1000, 500*1000);

		setCelestialBodiesSlotsAmount(eCelestialBodyType.Planet, 4, 10);
		setCelestialBodiesSlotsAmount(eCelestialBodyType.AsteroidField, 3, 6);
		setCelestialBodiesSlotsAmount(eCelestialBodyType.Nebula, 2, 4);

		setNeutralCelestialBodiesGenerationRate(eCelestialBodyType.Planet, (float) 0.30);
		setNeutralCelestialBodiesGenerationRate(eCelestialBodyType.AsteroidField, (float) 0.50);
		setNeutralCelestialBodiesGenerationRate(eCelestialBodyType.Nebula, (float) 0.20);

		setPlayersPlanetsStartingPopulation(50*1000);
		setPlayersPlanetsStartingCarbonResources(2*1000);

		setAllianceVictory(ALLIANCE_VICTORY);
		setRegimicide(REGIMICIDE);
		setAssimilateNeutralisedPeoples(ASSIMILATE_NEUTRALISED_PEOPLES);
		setTotalConquest(TOTAL_CONQUEST);
		setEconomicVictory(ECONOMIC_VICTORY_POPULATION, ECONOMIC_VICTORY_CARBON);
		setTimeLimitVictory(TIME_LIMIT_VICTORY);

		setProbeScope(3);
		setSunRadius(SUN_RADIUS);
		
		for(eUnitType u : eUnitType.values())
		{
			setUnitTypeSight(u, (float) 1);
			setUnitTypeSpeed(u, (float) 1);
		}
	}

	/*
	public GameConfig(int dimX, int dimY, int dimZ, int neutralCelesialBodiesCount, int populationPerTurnMin, int populationPerTurnMax, int populationLimitMin, int populationLimitMax, Map<eCelestialBodyType, Integer[]> celestialBodiesStartingCarbonAmount, Map<eCelestialBodyType, Integer[]> celestialBodiesSlotsAmount, Map<eCelestialBodyType, Float> neutralCelestialBodiesGenerationTable, boolean allianceVictory, boolean regimicide, boolean assimilateNeutralisedPeoples, boolean totalConquest, int economicVictoryCarbon, int economicVictoryPopulation, int timeLimitVictory, int probeScope, int sunRadius, int playersPlanetsStartingCarbonResources, int playersPlanetsStartingPopulation)
	{
		// TODO: call with right parameters

		setDimX(dimX);
		setDimY(dimY);
		setDimZ(dimZ);

		setNeutralCelestialBodiesCount(neutralCelesialBodiesCount);

		setPopulationPerTurn(populationPerTurnMin, populationPerTurnMax);
		setPopulationLimit(populationLimitMin, populationLimitMax);

		if (celestialBodiesStartingCarbonAmount != null)
			for(Map.Entry<eCelestialBodyType, Integer[]> e : celestialBodiesStartingCarbonAmount.entrySet())
			{
				if (e.getValue() == null) continue;
				if (e.getValue().length < 2) continue;
				if (e.getValue()[0] == null || e.getValue()[1] == null) continue;

				setCelestialBodiesStartingCarbonAmount(e.getKey(), e.getValue()[0], e.getValue()[1]);
			}

		setPlayersPlanetsStartingCarbonResources(playersPlanetsStartingCarbonResources);
		setPlayersPlanetsStartingPopulation(playersPlanetsStartingPopulation);
		
		if (celestialBodiesSlotsAmount != null) for(Map.Entry<eCelestialBodyType, Integer[]> e : celestialBodiesSlotsAmount.entrySet())
		{
			if (e.getValue() == null) continue;
			if (e.getValue().length < 2) continue;
			if (e.getValue()[0] == null || e.getValue()[1] == null) continue;

			setCelestialBodiesSlotsAmount(e.getKey(), e.getValue()[0], e.getValue()[1]);
		}

		if (neutralCelestialBodiesGenerationTable != null)
			for(Map.Entry<eCelestialBodyType, Float> e : neutralCelestialBodiesGenerationTable.entrySet())
			{
				if (e.getValue() == null) continue;

				setNeutralCelestialBodiesGenerationRate(e.getKey(), e.getValue());
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
	*/

	/**
	 * @return the dimX
	 */
	@Override
	public int getDimX()
	{
		return dimX;
	}

	/**
	 * @param dimX
	 *            the dimX to set
	 */
	@Override
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
	@Override
	public int getDimY()
	{
		return dimY;
	}

	/**
	 * @param dimY
	 *            the dimY to set
	 */
	@Override
	public void setDimY(int dimY)
	{
		if (dimY <= 0) throw new IllegalArgumentException("dimY must be greater than 0.");
		universeVolumeCheck(getDimX(), dimY, getDimZ(), getSunRadius(), getNeutralCelestialBodiesCount());
		this.dimY = dimY;
	}

	/**
	 * @return the dimZ
	 */
	@Override
	public int getDimZ()
	{
		return dimZ;
	}

	/**
	 * @param dimZ
	 *            the dimZ to set
	 */
	@Override
	public void setDimZ(int dimZ)
	{
		if (dimZ <= 0) throw new IllegalArgumentException("dimZ must be greater than 0.");
		universeVolumeCheck(getDimX(), getDimY(), dimZ, getSunRadius(), getNeutralCelestialBodiesCount());
		this.dimZ = dimZ;
	}

	/**
	 * @return the neutralCelestialBodiesCount
	 */
	@Override
	public int getNeutralCelestialBodiesCount()
	{
		return neutralCelestialBodiesCount;
	}

	/**
	 * @param neutralCelestialBodiesCount
	 *            the neutralCelestialBodiesCount to set
	 */
	@Override
	public void setNeutralCelestialBodiesCount(int neutralCelestialBodiesCount)
	{
		if (neutralCelestialBodiesCount < 0) throw new IllegalArgumentException("neutralCelestialBodiesCount cannot must be greater or equal to 0.");
		universeVolumeCheck(getDimX(), getDimY(), getDimZ(), getSunRadius(), neutralCelestialBodiesCount);
		this.neutralCelestialBodiesCount = neutralCelestialBodiesCount;
	}

	@Override
	public int getPlayersPlanetsStartingCarbonResources()
	{
		return playersPlanetsStartingCarbonResources;
	}

	@Override
	public void setPlayersPlanetsStartingCarbonResources(int playersPlanetsStartingCarbonResources)
	{
		if (playersPlanetsStartingCarbonResources < 0) throw new IllegalArgumentException("Players starting carbon amount cannot be negative.");
		if (playersPlanetsStartingCarbonResources > celestialBodiesStartingCarbonAmount.get(eCelestialBodyType.Planet)[0])
			throw new IllegalArgumentException("Players starting carbon amount must be lesser than planets minimum starting carbon value.");
		this.playersPlanetsStartingCarbonResources = playersPlanetsStartingCarbonResources;
	}

	@Override
	public int getPlayersPlanetsStartingPopulation()
	{
		return playersPlanetsStartingPopulation;
	}

	@Override
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
	@Override
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
	@Override
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
	@Override
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
	@Override
	public int[] getPopulationLimit()
	{
		return populationLimit;
	}

	/**
	 * @return the celestialBodiesStartingCarbonAmount
	 */
	@Override
	public int[] getCelestialBodiesStartingCarbonAmount(eCelestialBodyType celestialBodyType)
	{
		return celestialBodiesStartingCarbonAmount.get(celestialBodyType);
	}
	
	
	/**
	 * @return the celestialBodiesSlotsAmount
	 */
	@Override
	public int[] getCelestialBodiesSlotsAmount(eCelestialBodyType celestialBodyType)
	{
		return celestialBodiesSlotsAmount.get(celestialBodyType);
	}

	/**
	 * @param celestialBodiesStartingCarbonAmount
	 *            the celestialBodiesStartingCarbonAmount to set
	 */
	@Override
	public void setCelestialBodiesStartingCarbonAmount(eCelestialBodyType celestialBodyType, int min, int max)
	{
		if (min <= 0) throw new IllegalArgumentException("minimum carbon amount must be positive or null.");
		if (max <= 0) throw new IllegalArgumentException("minimum carbon amount must be positive or null.");
		if (max < min) throw new IllegalArgumentException("minimum carbon amount must be lesser than maximum.");

		if (celestialBodyType == eCelestialBodyType.Planet && min < playersPlanetsStartingCarbonResources)
			throw new IllegalArgumentException("Planets minimum carbon amount must be greater or equal to players starting carbon amount.");

		this.celestialBodiesStartingCarbonAmount.put(celestialBodyType, new int[] { min, max });
	}

	/**
	 * @param celestialBodiesSlotsAmount
	 *            the qtSlotsCorpsCelestes to set
	 */
	@Override
	public void setCelestialBodiesSlotsAmount(eCelestialBodyType celestialBodyType, int min, int max)
	{
		if (min <= 0) throw new IllegalArgumentException("minimum slots amount must be positive or null.");
		if (max <= 0) throw new IllegalArgumentException("minimum slots amount must be positive or null.");
		if (max < min) throw new IllegalArgumentException("minimum slots amount must be lesser than maximum.");
		this.celestialBodiesSlotsAmount.put(celestialBodyType, new int[] { min, max });
	}
	
	@Override
	public void setNeutralCelestialBodiesGenerationTable(eCelestialBodyType celestialBodyType, float rate)
	{
		neutralCelestialBodiesGenerationTable.put(celestialBodyType, rate);
	}
	
	/**
	 * @return the neutralCelestialBodiesGenerationTable
	 */
	@Override
	public Float getNeutralCelestialBodiesGenerationRate(eCelestialBodyType celestialBodyType)
	{
		return neutralCelestialBodiesGenerationTable.get(celestialBodyType);
	}

	/**
	 * @param celestialBodyType
	 *            Celestial body type.
	 * @param rate
	 *            percentage to be generated (not a true percentage but a
	 *            weight).
	 */
	@Override
	public void setNeutralCelestialBodiesGenerationRate(eCelestialBodyType celestialBodyType, Float rate)
	{
		if (rate == null) return;
		if (rate < 0) throw new IllegalArgumentException("rate cannot be negative.");
		this.neutralCelestialBodiesGenerationTable.put(celestialBodyType, rate);
	}

	/**
	 * @return the allianceVictory
	 */
	@Override
	public boolean isAllianceVictory()
	{
		return allianceVictory;
	}

	/**
	 * @param allianceVictory
	 *            the allianceVictory to set
	 */
	@Override
	public void setAllianceVictory(boolean allianceVictory)
	{
		this.allianceVictory = allianceVictory;
	}

	/**
	 * @return the regimicide
	 */
	@Override
	public boolean isRegimicide()
	{
		return regimicide;
	}

	/**
	 * @param regimicide
	 *            the regimicide to set
	 */
	@Override
	public void setRegimicide(boolean regimicide)
	{
		this.regimicide = regimicide;
	}

	/**
	 * @return the assimilerPeuplesNeutralises
	 */
	@Override
	public boolean isAssimilateNeutralisedPeoples()
	{
		return assimilateNeutralisedPeoples;
	}

	/**
	 * @param assimilateNeutralisedPeoples
	 *            the assimilateNeutralisedPeoples to set
	 */
	@Override
	public void setAssimilateNeutralisedPeoples(boolean assimilateNeutralisedPeoples)
	{
		this.assimilateNeutralisedPeoples = assimilateNeutralisedPeoples;
	}

	/**
	 * @return the conqueteTotale
	 */
	@Override
	public boolean isTotalConquest()
	{
		return totalConquest;
	}

	/**
	 * @param totalConquest
	 *            the totalConquest to set
	 */
	@Override
	public void setTotalConquest(boolean totalConquest)
	{
		this.totalConquest = totalConquest;
	}

	/**
	 * @return the economicVictory
	 */
	@Override
	public int[] getEconomicVictory()
	{
		return economicVictory;
	}

	/**
	 * @param economicVictory
	 *            the economicVictory to set
	 */
	@Override
	public void setEconomicVictory(int populationGoal, int carbonResourceGoal)
	{
		if (populationGoal < 0) throw new IllegalArgumentException("populationGoal　must be positive or null.");
		if (carbonResourceGoal < 0) throw new IllegalArgumentException("carbonResourceGoal　must be positive or null.");
		this.economicVictory = new int[] { populationGoal, carbonResourceGoal };
	}

	/**
	 * @return the timeLimitVictory
	 */
	@Override
	public int getTimeLimitVictory()
	{
		return timeLimitVictory;
	}

	/**
	 * @param timeLimitVictory
	 *            the timeLimitVictory to set
	 */
	@Override
	public void setTimeLimitVictory(int timeLimitVictory)
	{
		if (timeLimitVictory < 0) throw new IllegalArgumentException("timeLimitVictory　must be positive or null.");
		this.timeLimitVictory = timeLimitVictory;
	}

	@Override
	public int getProbeScope()
	{
		return probeScope;
	}

	@Override
	public void setProbeScope(int probeScope)
	{
		if (probeScope < 0) throw new IllegalArgumentException("Probe scope cannot be negative.");
		this.probeScope = probeScope;
	}

	@Override
	public int getSunRadius()
	{
		return sunRadius;
	}

	@Override
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
		StringBuilder sb = new StringBuilder();
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

	int naturalCarbonPerTurn = 2000;
	
	@Override
	public int getNaturalCarbonPerTurn()
	{
		// TODO : Add to CTOR
		return naturalCarbonPerTurn;
	}
	
	@Override
	public void setNaturalCarbonPerTurn(int naturalCarbonPerTurn)
	{
		this.naturalCarbonPerTurn = naturalCarbonPerTurn;
	}
	
	private int maxNaturalCarbon = 2000;
	
	@Override
	public int getMaxNaturalCarbon()
	{
		// TODO : Add to CTOR
		return maxNaturalCarbon;
	}
	
	@Override
	public void setMaxNaturalCarbon(int maxNaturalCarbon)
	{
		this.maxNaturalCarbon = maxNaturalCarbon;
	}

	private int governmentStarshipCarbonPrice = 200;
	@Override
	public int getGovernmentStarshipCarbonPrice()
	{
		// TODO : Add to CTOR
		return governmentStarshipCarbonPrice;
	}
	
	@Override
	public void setGovernmentStarshipCarbonPrice(int governmentStarshipCarbonPrice)
	{
		this.governmentStarshipCarbonPrice = governmentStarshipCarbonPrice;
	}
	
	private int governmentStarshipPopulationPrice = 200;
	@Override
	public int getGovernmentStarshipPopulationPrice()
	{
		// TODO : Add to CTOR
		return governmentStarshipPopulationPrice;
	}
	@Override
	public void setGovernmentStarshipPopulationPrice(int governmentStarshipPopulationPrice)
	{
		this.governmentStarshipPopulationPrice = governmentStarshipPopulationPrice;
	}

	private double spaceRoadPricePerArea = 200;
	@Override
	public double getSpaceRoadPricePerArea()
	{
		// TODO : Add to CTOR
		return spaceRoadPricePerArea;
	}
	
	@Override
	public void setSpaceRoadPricePerArea(double spaceRoadPricePerArea)
	{
		this.spaceRoadPricePerArea = spaceRoadPricePerArea;
	}

	private int spaceRoadsSpeed = 5;
	@Override
	public int getSpaceRoadsSpeed()
	{
		// TODO : Add to CTOR
		return spaceRoadsSpeed;
	}
	
	@Override
	public void setSpaceRoadsSpeed(int spaceRoadsSpeed)
	{
		this.spaceRoadsSpeed = spaceRoadsSpeed;
	}
	
	private int carbonMinimalFreight = 1000;
	@Override
	public int getCarbonMinimalFreight()
	{
		// TODO : Add to CTOR
		return carbonMinimalFreight;
	}
	
	@Override
	public void setCarbonMinimalFreight(int carbonMinimalFreight)
	{
		this.carbonMinimalFreight = carbonMinimalFreight;
	}

	private int turn = 0;
	@Override
	public int getTurn()
	{
		return turn;
	}
	
	@Override
	public void setTurn(int turn)
	{
		this.turn = turn;
	}

	private Map<eUnitType, Float> unitsSight = new HashMap<eUnitType, Float>();
	@Override
	public Float getUnitTypeSight(eUnitType unitType)
	{
		return unitsSight.get(unitType);
	}

	@Override
	public void setUnitTypeSight(eUnitType unitType, Float sight)
	{
		unitsSight.put(unitType, sight);
	}
	
	private Map<eUnitType, Float> unitsSpeed = new HashMap<eUnitType, Float>();
	@Override
	public Float getUnitTypeSpeed(eUnitType unitType)
	{
		return unitsSpeed.get(unitType);
	}
	
	@Override
	public void setUnitTypeSpeed(eUnitType unitType, Float speed)
	{
		unitsSpeed.put(unitType, speed);
	}

	private float vortexScope = 1;
	@Override
	public float getVortexScope()
	{
		return vortexScope;
	}
	
	@Override
	public void setVortexScope(float scope)
	{
		vortexScope = scope;
	}
	
	private int[] vortexLifetime = new int[] {3, 10};
	
	@Override
	public int[] getVortexLifetime()
	{
		return vortexLifetime;
	}
	
	@Override
	public void setVortexLifetime(int min, int max)
	{
		vortexLifetime = new int[] {min, max};
	}
}