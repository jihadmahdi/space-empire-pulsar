package org.axan.sep.common.db;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.axan.eplib.orm.sql.SQLDataBaseException;
import org.axan.eplib.utils.Profiling;
import org.axan.eplib.utils.Profiling.ExecTimeMeasures;
import org.axan.sep.common.SEPUtils;
import org.axan.sep.common.Protocol.eBuildingType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.SEPUtils.RealLocation;
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
					
					db.fireAreaChangedEvent(location);
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
				
				db.fireAreaChangedEvent(productiveCelestialBody.getLocation());
			}
		}
	}
	
	/**
	 * Moves traveling units.
	 */
	public static class UnitsMoves implements IGameEvent, Serializable
	{
		@Override
		public void process(IGameEventExecutor executor, SEPCommonDB db) throws GameEventException
		{
			//Set<IUnit> stoppedUnits = new HashSet<IUnit>();
			Set<IUnit> movingUnits = new HashSet<IUnit>();
			Set<IUnit> allUnits = db.getUnits();
			
			for(IUnit u : allUnits)
			{
				if (!u.isStopped())
				{
					movingUnits.add(u);
				}
				/*
				else
				{
					stoppedUnits.add(u);
				}
				*/
			}
			
			double step = 0D;
			
			Map<IUnit, Map<IUnit, Double>> encounters = new HashMap<IUnit, Map<IUnit,Double>>();
			
			if (!movingUnits.isEmpty()) while(step < 1)
			{
				IUnit fastestUnit = null;
				double minDistance = Double.POSITIVE_INFINITY;
				
				for(IUnit u : movingUnits)
				{
					if (fastestUnit == null || u.getSpeed() > fastestUnit.getSpeed()) fastestUnit = u;
					
					for(IUnit v : allUnits)
					{
						if (u == v) continue;
						if (u.getOwnerName().equals(v.getOwnerName())) continue;
						
						// NOTE: Following code should never run on player DB view (as there is no other player units on playerDB view)
						
						RealLocation uLoc = SEPUtils.getMobileLocation(u.getDeparture().asRealLocation(), u.getDestination().asRealLocation(), u.getSpeed(), u.getTravelingProgress(), step, true);
						RealLocation vLoc = SEPUtils.getMobileLocation(v.getDeparture().asRealLocation(), v.getDestination().asRealLocation(), v.getSpeed(), v.getTravelingProgress(), step, true);
						double d = SEPUtils.getDistance(uLoc, vLoc);
						
						if (u.getSight() >= d)
						{
							if (!encounters.containsKey(u)) encounters.put(u, new HashMap<IUnit, Double>());
							encounters.get(u).put(v, step);
						}
						
						if (v.getSight() >= d)
						{
							if (!encounters.containsKey(v)) encounters.put(v, new HashMap<IUnit, Double>());
							encounters.get(v).put(u, step);
						}
						
						if (d > u.getSight() || d > v.getSight()) minDistance = Math.min(minDistance, d);
					}
				}
				
				step += minDistance / fastestUnit.getSpeed();
			}
			
			for(IUnit u : encounters.keySet())
			{
				for(IUnit v : encounters.get(u).keySet())
				{
					step = encounters.get(u).get(v);					
					u.logEncounter(v, step);
				}
			}
			
			for(IUnit u : movingUnits)
			{
				Location oldLocation = u.getRealLocation().asLocation();
				
				double progress = u.getTravelingProgress() + u.getSpeed() / SEPUtils.getDistance(u.getDeparture(), u.getDestination());
				
				if (progress < 1)
				{
					u.setTravelingProgress(progress);
					db.getArea(u.getRealLocation().asLocation());
				}
				else
				{
					u.setDeparture(u.getDestination());
					u.setDestination(null);
					u.onArrival(executor);
				}
				
				db.fireAreaChangedEvent(oldLocation);
				db.fireAreaChangedEvent(u.getRealLocation().asLocation());
			}
		}
	}
	
	public static interface IConditionalEvent extends IGameEvent
	{
		boolean test(SEPCommonDB sepDB, String playerName);
	}	
	
	public static class AntiProbeMissileExplosion implements IConditionalEvent, Serializable
	{
		final private Location location;
		final private String antiProbeMissileOwnerName;
		final private String antiProbeMissileName;
		final private String targetOwnerName;
		final private String targetName;
		
		public AntiProbeMissileExplosion(Location location, String antiProbeMissileOwnerName, String antiProbeMissileName, String targetOwnerName, String targetName)
		{
			this.location = location;
			this.antiProbeMissileOwnerName = antiProbeMissileOwnerName;
			this.antiProbeMissileName = antiProbeMissileName;
			this.targetOwnerName = targetOwnerName;
			this.targetName = targetName;
		}
		
		@Override
		public boolean test(SEPCommonDB sepDB, String playerName)
		{
			IArea area = sepDB.getArea(location);
			return area.isVisible(playerName);
		}
		
		@Override
		public void process(IGameEventExecutor executor, SEPCommonDB db) throws GameEventException
		{
			IProbe target = db.getProbe(targetOwnerName, targetName);
			if (target != null)
			{
				if (!target.getRealLocation().asLocation().equals(location)) throw new GameEventException(this, "Probe location differs "+target.getRealLocation().asLocation()+" != "+location);
				target.destroy();
			}
			
			IAntiProbeMissile antiProbeMissile = db.getAntiProbeMissile(antiProbeMissileOwnerName, antiProbeMissileName);
			if (antiProbeMissile != null)
			{
				if (!antiProbeMissile.getRealLocation().asLocation().equals(location)) throw new GameEventException(this, "AntiProbeMissile location differs "+antiProbeMissile.getRealLocation().asLocation()+" != "+location);
				antiProbeMissile.destroy();
			}
			
			db.fireAreaChangedEvent(location);
		}
	}
}
