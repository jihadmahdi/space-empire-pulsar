package org.axan.sep.common;

import org.axan.sep.common.Protocol.eCelestialBodyType;

/**
 * Represent a game creation configuration.
 */
public interface IGameConfig
{
	// TODO: Faire un JUnit pour tester la compatibilité de IGameConfig avec le proxy dans SEPSQLiteDB.
	
	public static final int									UNIVERSE_DIM							= 20;
	/**
	 * X dimension of the universe.
	 */
	public int getDimX();
	
	/**
	 * Y dimension of the universe.
	 */
	public int getDimY();

	/**
	 * Z dimension of the universe.
	 */
	public int getDimZ();

	public static final int									NEUTRAL_CELESTIAL_BODIES				= 5;
	
	/**
	 * Number of neutral celestial bodies to create.
	 */
	public int getNeutralCelestialBodiesCount();
	
	/**
	 * Players starting planet carbon.
	 */
	public int getPlayersPlanetsStartingCarbonResources();
	public void setPlayersPlanetsStartingCarbonResources(int playersPlanetsStartingCarbonResources);

	/**
	 * Players starting planet population.
	 */
	public int getPlayersPlanetsStartingPopulation();
	public void setPlayersPlanetsStartingPopulation(int playersPlanetsStartingPopulation);

	/**
	 * Starting carbon resource amount on players planets.
	 */
	public int[] getCelestialBodiesStartingCarbonAmount(eCelestialBodyType celestialBodyType);

	/**
	 * Number of slots on celestial bodies.
	 */
	public int[] getCelestialBodiesSlotsAmount(eCelestialBodyType celestialBodyType);

	/**
	 * Neutral celestial bodies generation, type table. This define the chance
	 * for a neutral celestial body to be one of the different celestial body
	 * type.
	 */
	public float getNeutralCelestialBodiesGenerationRate(eCelestialBodyType celestialBodyType);

	public static final boolean								ALLIANCE_VICTORY						= false;
	
	/**
	 * Victory rule : Alliance victory. Whatever the victory condition, every
	 * allied winner are winners.
	 */
	public boolean isAllianceVictory();

	public static final boolean								REGIMICIDE								= true;

	/**
	 * Victory rule : Regimicide. Enable government modules and government
	 * starship to represent the player in its empire. If a player lose its
	 * governement he lose the game and its people fall into enemy people or
	 * neutral according to the "assimilateNeutralisedPeoples".
	 */
	public boolean isRegimicide();

	public static final boolean								ASSIMILATE_NEUTRALISED_PEOPLES			= false;
	/**
	 * Option for the victory rule : Regimicide. If option is on, peoples whose
	 * government is destroyed are assimilated to the empire of the player who
	 * destroyed the government. If option if off, peoples whose government is
	 * destroyed are broke up in wild planets.
	 */
	public boolean isAssimilateNeutralisedPeoples();

	public static final boolean								TOTAL_CONQUEST							= true;
	/**
	 * Victory rule : Total conquest. The first player who conquer all celestial
	 * bodies and destroy all enemies fleet win the game. Note : You have to
	 * disable every other victory rules to make the most of this mode.
	 */
	public boolean isTotalConquest();

	public static final int									ECONOMIC_VICTORY_POPULATION				= 0;
	public static final int									ECONOMIC_VICTORY_CARBON					= 0;
	/**
	 * Victory rule : Economic victory. The first player who reach the carbon
	 * resource and population goals win the game. [0] population goal; [1]
	 * carbon resource goal; Null values to disable.
	 */
	public int[] getEconomicVictory();

	public static final int									TIME_LIMIT_VICTORY						= 0;
	/**
	 * Victory rule : Time limit. The game ends after a fixed number of turns,
	 * the player with the best score is the winner. Null value to disable.
	 */
	public int getTimeLimitVictory();

	/**
	 * Population generated on a planet per turn. [0] min; [1] max.
	 */
	public int[] getPopulationPerTurn();

	/**
	 * Population limit on a planet. [0] min; [1] max.
	 */
	public int[] getPopulationLimit();

	/**
	 * Deployed probe detection scope.
	 */
	public int getProbeScope();

	public static final int									SUN_RADIUS								= 3;
	/**
	 * Sun radius (0: sun volume is 1; 1: sun volume is 9; 2: sun volume is 125;
	 * n: sun volume is (2n+1)^3 );
	 */
	public int getSunRadius();

	public int getFreeAreaCount();

	public int getNaturalCarbonPerTurn();

	public int getMaxNaturalCarbon();

	public int getGovernmentStarshipCarbonPrice();
	
	public int getGovernmentStarshipPopulationPrice();

	public double getSpaceRoadPricePerArea();

	public int getSpaceRoadsSpeed();
	
	public int getCarbonMinimalFreight();
	
	///

	/**
	 * @param populationPerTurnMin
	 *            Minimum value for population generation on a planet.
	 * @param populationPerTurnMax
	 *            Maximum value for population generation on a planet.
	 */
	public void setPopulationPerTurn(int populationPerTurnMin, int populationPerTurnMax);

	/**
	 * @param populationLimitMin
	 *            Minimum value for population limit on a planet.
	 * @param populationLimitMax
	 *            Maximum value for population limit on a planet.
	 */
	public void setPopulationLimit(int populationLimitMin, int populationLimitMax);

	/**
	 * @param celestialBodiesStartingCarbonAmount
	 *            the celestialBodiesStartingCarbonAmount to set
	 */
	public void setCelestialBodiesStartingCarbonAmount(Class<? extends ICelestialBody> celestialBodyType, int min, int max);

	/**
	 * @param celestialBodiesSlotsAmount
	 *            the qtSlotsCorpsCelestes to set
	 */
	public void setCelestialBodiesSlotsAmount(Class<? extends ICelestialBody> celestialBodyType, int min, int max);

	/**
	 * @param celestialBodyType
	 *            Celestial body type.
	 * @param rate
	 *            percentage to be generated (not a true percentage but a
	 *            weight).
	 */
	public void setNeutralCelestialBodiesGenerationTable(eCelestialBodyType celestialBodyType, float rate);

	/**
	 * @param allianceVictory
	 *            the allianceVictory to set
	 */
	public void setAllianceVictory(boolean allianceVictory);

	/**
	 * @param regimicide
	 *            the regimicide to set
	 */
	public void setRegimicide(boolean regimicide);

	/**
	 * @param assimilateNeutralisedPeoples
	 *            the assimilateNeutralisedPeoples to set
	 */
	public void setAssimilateNeutralisedPeoples(boolean assimilateNeutralisedPeoples);

	/**
	 * @param totalConquest
	 *            the totalConquest to set
	 */
	public void setTotalConquest(boolean totalConquest);

	/**
	 * @param economicVictory
	 *            the economicVictory to set
	 */
	public void setEconomicVictory(int populationGoal, int carbonResourceGoal);

	/**
	 * @param timeLimitVictory
	 *            the timeLimitVictory to set
	 */
	public void setTimeLimitVictory(Integer timeLimitVictory);

	public void setProbeScope(int probeScope);

	public void setSunRadius(int sunRadius);
}

