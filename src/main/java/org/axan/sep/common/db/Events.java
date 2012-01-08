package org.axan.sep.common.db;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.axan.eplib.orm.sql.SQLDataBaseException;
import org.axan.eplib.utils.Profiling;
import org.axan.eplib.utils.Profiling.ExecTimeMeasures;
import org.axan.sep.common.SEPUtils;
import org.axan.sep.common.Protocol.eBuildingType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.IGameEvent.GameEventException;
import org.axan.sep.common.db.IGameEvent.IGameEventExecutor;
import org.axan.sep.common.db.orm.SEPCommonDB;

public abstract class Events
{	
	/**
	 * Create Universe on empty DB.
	 * Nothing is done if DB is not empty (@see {@link SEPCommonDB#isUniverseCreated()}
	 */
	public static class UniverseCreation implements IGameEvent, Serializable
	{
		final private Map<IPlayer, IPlayerConfig> players;
		final private Map<Location, ICelestialBody> celestialBodies;
		final private Map<IProductiveCelestialBody, IPlayer> ownershipRelations;
		
		public UniverseCreation(Map<IPlayer, IPlayerConfig> players, Map<Location, ICelestialBody> celestialBodies, Map<IProductiveCelestialBody, IPlayer> ownershipRelations)
		{
			this.players = players;
			this.celestialBodies = celestialBodies;
			this.ownershipRelations = ownershipRelations;
		}
		
		/**
		 * Check if current event need to be processed or already has been (for optimization concerns).
		 * @param db
		 * @return
		 * @throws SQLDataBaseException 
		 */
		private boolean skipCondition(SEPCommonDB db) throws SQLDataBaseException
		{
			// We cannot rely on GameConfig to check if universe has already been created, so we just check if there already is any Area instered.
			return db.isUniverseCreated();
		}
		
		@Override
		public void process(IGameEventExecutor executor, SEPCommonDB db) throws GameEventException
		{
			// TODO: Recode GameBoard#createUniverse and EvCReateUniverse to remove unnecessary parameters (celestial body locations, owners, ...) that now can be retrieved from off-db objects getters.			
			
			try
			{
				ExecTimeMeasures em = new ExecTimeMeasures();
				
				em.measures("start");
				IGameConfig config = db.getConfig();
				
				if (skipCondition(db)) return;
				
				// Generate players
				em.measures("generate players");
				for(IPlayer player : players.keySet())
				{
					db.createPlayer(player);
				}
				
				// Generate Universe				
				
				/*
				v1:
					Measures :
					generate players - make sun : 3100ms
					make sun - make celestial bodies : 7948ms
					make celestial bodies - set turn to 0 : 1467ms
					set turn to 0 - end : 16ms
					Total: 12532ms
					
					Sun pos 79; neg 68
					6 celestial bodies
					
				v1: no area creation
					Measures :
					generate players - make sun : 3223ms
					make sun - make celestial bodies : 48ms
					make celestial bodies - set turn to 0 : 1755ms
					set turn to 0 - end : 20ms
					Total: 5046ms
					
					Sun pos 79; neg 68
					6 celestial bodies
					
				v2: no sun creation
					Measures :
					generate players - make celestial bodies : 3025ms
					make celestial bodies - set turn to 0 : 1649ms
					set turn to 0 - end : 15ms
					Total: 4689ms
					
					6 celestial bodies
				 */
				
				// Make the sun
				/*
				int pos=0; int neg=0;
				em.measures("make sun");
				for(int x = -Math.min(config.getSunRadius(), sunLocation.x); x <= Math.min(config.getSunRadius(), sunLocation.x); ++x)
					for(int y = -Math.min(config.getSunRadius(), sunLocation.y); y <= Math.min(config.getSunRadius(), sunLocation.y); ++y)
						for(int z = -Math.min(config.getSunRadius(), sunLocation.z); z <= Math.min(config.getSunRadius(), sunLocation.z); ++z)						
						{
							Location parsedLoc = new Location(sunLocation.x + x, sunLocation.y + y, sunLocation.z + z);
							if (SEPUtils.getDistance(parsedLoc, sunLocation) <= config.getSunRadius())
							{
								//db.createSun(parsedLoc);
								++pos;
							}
							else
							{
								++neg;
							}
						}
				*/
				
				em.measures("make celestial bodies");
				Map<String, IPlanet> playerStartingPlanets = new HashMap<String, IPlanet>();
				for(Location location : celestialBodies.keySet())
				{
					ICelestialBody celestialBody = celestialBodies.get(location);
					
					if (!location.equals(celestialBody.getLocation()))
					{
						throw new RuntimeException("Implementation error, mapped location differs from celestial body location.");
					}
					
					ICelestialBody connectedCelestialBody = db.createCelestialBody(celestialBody);
					
					if (ownershipRelations.containsKey(celestialBody))
					{
						IPlayer player = ownershipRelations.get(celestialBody);
						
						((IProductiveCelestialBody) connectedCelestialBody).setOwner(player.getName());
						
						if (IPlanet.class.isInstance(celestialBody) && !playerStartingPlanets.containsKey(player.getName()))
						{
							IPlanet planet = (IPlanet) celestialBody;
							
							// Player starting planet
							playerStartingPlanets.put(player.getName(), planet);
							
							// If victory rule "Regimicide" is on, starting planet has a pre-built government module.						
							if (config.isRegimicide())
							{
								IGovernmentModule governmentModule = SEPCommonDB.makeGovernmentModule(planet.getName(), config.getTurn(), 1);
								governmentModule = db.createGovernmentModule(governmentModule);							
							}
						}
					}				
				}
				
				em.measures("set turn to 0");
				config.setTurn(0);
				
				em.measures("end");
				System.err.println("Measures :\n"+em.toString());
				//System.err.println("Sun pos "+pos+"; neg "+neg);
				System.err.println(celestialBodies.size()+" celestial bodies");
				System.err.println();
			}
			catch(Throwable t)
			{
				throw new GameEventException(this, t);
			}
		}

	}
	
	/**
	 * Generates carbon and population on productives celestial bodies.
	 */
	public static class RessourcesGeneration implements IGameEvent, Serializable
	{
		
		@Override
		public void process(IGameEventExecutor executor, SEPCommonDB db) throws GameEventException
		{
			for(IProductiveCelestialBody productiveCelestialBody : db.getProductiveCelestialBodies())
			{
				String productiveCelestialBodyName = productiveCelestialBody.getName();
				IGovernmentModule governmentModule = (IGovernmentModule) db.getBuilding(productiveCelestialBodyName, eBuildingType.GovernmentModule);
				
				int extractedCarbon = 0;
				if (productiveCelestialBody.getCarbonStock() > 0)
				{
					IExtractionModule extractionModule = (IExtractionModule) db.getBuilding(productiveCelestialBodyName, eBuildingType.ExtractionModule);
					if (extractionModule != null && extractionModule.getCarbonProductionPerTurn() > 0)
					{
						extractedCarbon = extractionModule.getCarbonProductionPerTurn();
					}
					else
					{
						extractedCarbon = db.getConfig().getNaturalCarbonPerTurn();
					}
					
					if (governmentModule != null)
					{
						extractedCarbon = (int) (extractedCarbon * 1.5); // TODO: put in game config, look also government module toString()
					}
					
					if (extractionModule == null || extractionModule.getCarbonProductionPerTurn() <= 0)
					{
						extractedCarbon = Math.min(Math.min(Math.max(0, db.getConfig().getMaxNaturalCarbon() - productiveCelestialBody.getCurrentCarbon()), productiveCelestialBody.getCarbonStock()), extractedCarbon);
					}
				}
				
				productiveCelestialBody.extractCarbon(extractedCarbon);
				
				if (IPlanet.class.isInstance(productiveCelestialBody))
				{
					IPlanet planet = (IPlanet) productiveCelestialBody;
					
					int generatedPopulation = planet.getPopulationPerTurn();
					
					if (governmentModule != null)
					{
						generatedPopulation = (int) (generatedPopulation * 1.5);
					}
					
					generatedPopulation = Math.min(planet.getMaxPopulation() - planet.getCurrentPopulation(), generatedPopulation);
					
					planet.generatePopulation(generatedPopulation);
				}
			}
		}
	}
}
