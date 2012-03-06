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
import org.axan.sep.common.GameConfig;
import org.axan.sep.common.GameConfigCopier;
import org.axan.sep.common.PlayerGameboardView;
import org.axan.sep.common.SEPUtils;
import org.axan.sep.common.Protocol.eBuildingType;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.Rules.StarshipTemplate;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.SEPUtils.RealLocation;
import org.axan.sep.common.db.Commands.FireAntiProbeMissile;
import org.axan.sep.common.db.ICommand.GameCommandException;
import org.axan.sep.common.db.IDiplomacyMarker.eForeignPolicy;
import org.axan.sep.common.db.IGameEvent.GameEventException;
import org.axan.sep.common.db.IGameEvent.IGameEventExecutor;
import org.axan.sep.common.db.orm.SEPCommonDB;

public abstract class Events
{
	/**
	 * Implements this interface if GameEvent must be checked to be fired only once per turn (only last fired one is kept).
	 */
	public static interface IPerTurnEvent
	{
		/**
		 * Return true if current event must replace the given one.
		 */
		boolean replace(IPerTurnEvent e);
	}
	
	public static interface IConditionalEvent
	{
		boolean test(SEPCommonDB sepDB, String playerName);
	}

	/**
	 * GameEvent base class.
	 */
	public static abstract class AGameEvent implements IGameEvent
	{
		protected void checkPlayerView(String playerName, IGameEventExecutor executor) throws GameEventException
		{
			if (!isPlayerView(playerName, executor)) throw new GameEventException(this, "Unexpected player view");
		}
		
		protected void checkGlobalView(IGameEventExecutor executor) throws GameEventException
		{
			if (!isGlobalView(executor)) throw new GameEventException(this, "Global view expected");
		}
		
		public static boolean isPlayerView(String playerName, IGameEventExecutor executor)
		{
			if (playerName == null) throw new IllegalArgumentException("PlayerName cannot be null, use checkGlobalView() to check for global view.");
			return (playerName.equals(executor.getCurrentViewPlayerName()));
		}
		
		public static boolean isGlobalView(IGameEventExecutor executor)
		{
			return (executor.getCurrentViewPlayerName() == null);
		}
		
		/**
		 * Fire events to players (excluding player who issued the current command) for who the given area is visible.
		 */
		protected void fireEventForObservers(IGameEventExecutor executor, IArea area, Set<String> players, IGameEvent event)
		{		
			Set<String> observers = new HashSet<String>();
			for(String observerName : players)
			{
				if (area.isVisible(observerName))
				{
					observers.add(observerName);
				}
			}
			
			if (!observers.isEmpty())
			{
				executor.onGameEvent(event, observers);
			}
		}
		
		/**
		 * Create unit marker (if unit does not exists) and log given message.
		 * Tag $location$ in both message parameters is replaced by unit location (celestial body name or location).
		 * Tag $destination$ in unitMessage parameter is replaced by unit destination (celestial body name or location). 
		 */
		protected void logUnitMarker(IUnitMarker unitMarker, SEPCommonDB db, String markerMessage, String unitMessage) throws GameEventException
		{			
			IUnit unit = db.getUnit(unitMarker.getOwnerName(), unitMarker.getName(), null);
			if (unit != null && unitMessage != null)
			{
				//if (!unitDepartureName.equals(unit.getDepartureName())) throw new GameEventException(this, "Inconsistent unit departure name");
				if (!unit.getRealLocation().asLocation().equals(unitMarker.getRealLocation().asLocation())) throw new GameEventException(this, "Inconsistent unit location");
				if (unitMessage.contains("$destination$"))
				{
					String destination = unit.getDestinationName() != null ? unit.getDestinationName() : unit.getDestination().toString();
					unitMessage.replaceAll("\\$destination\\$", destination);
				}
				if (unitMessage.contains("$location$"))
				{
					ICelestialBody cb = db.getArea(unit.getRealLocation().asLocation()).getCelestialBody();
					String location = cb == null ? unit.getRealLocation().asLocation().toString() : cb.getName();
					unitMessage.replaceAll("\\$location\\$", location);
				}
				
				db.fireLog(unitMessage);
				db.fireAreaChangedEvent(unit.getRealLocation().asLocation());
			}
			else
			{
				// TODO: Add unit marker ? To link log message to detailed informations (e.g. fleet composition, unitMarker.toString())
				db.createUnitMarker(unitMarker);
				
				if (markerMessage != null)
				{
					if (markerMessage.contains("$location$"))
					{
						ICelestialBody cb = db.getArea(unitMarker.getRealLocation().asLocation()).getCelestialBody();
						String location = cb == null ? unitMarker.getRealLocation().asLocation().toString() : cb.getName();
						markerMessage.replaceAll("\\$location\\$", location);
					}
					
					db.fireLog(markerMessage);
				}
				
				db.fireAreaChangedEvent(unitMarker.getRealLocation().asLocation());
			}						
		}
	}	
	
	/**
	 * Increment turn.
	 * Should only be used in {@link PlayerGameboardView#resolveCurrentTurn()}.
	 */
	public static class IncrementTurn extends AGameEvent implements Serializable
	{
		@Override
		public void process(IGameEventExecutor executor, SEPCommonDB db) throws GameEventException
		{
			int newTurn = db.getConfig().getTurn();
			db.getConfig().setTurn(++newTurn);
		}
	}
	
	/**
	 * Update diplomacy view for the given owner and target.
	 */
	public static class UpdateDiplomacyMarker extends AGameEvent implements IPerTurnEvent, Serializable
	{
		private final String ownerName;
		private final String targetName;
		private final boolean isAllowedToLand;
		private final eForeignPolicy foreignPolicy;
		
		transient private IDiplomacy diplomacy;
		
		public UpdateDiplomacyMarker(String ownerName, String targetName, boolean isAllowedToLand, eForeignPolicy foreignPolicy)
		{
			this.ownerName = ownerName;
			this.targetName = targetName;
			this.isAllowedToLand = isAllowedToLand;
			this.foreignPolicy = foreignPolicy;
		}
		
		@Override
		public boolean replace(IPerTurnEvent e)
		{
			if (!UpdateDiplomacyMarker.class.isInstance(e)) return false;
			UpdateDiplomacyMarker udm = (UpdateDiplomacyMarker) e;
			return (ownerName.equals(udm.ownerName) && targetName.equals(udm.targetName));
		}
		
		@Override
		public void process(IGameEventExecutor executor, SEPCommonDB db) throws GameEventException
		{
			if (isPlayerView(ownerName, executor)) throw new GameEventException(this, "Player diplomacy update should not fire UpdateDiplomacyMarker event on Player view.");
			
			if (ownerName.equals(ownerName)) throw new GameEventException(this, "Player cannot set diplomacy toward himself");
			IPlayer owner = db.getPlayer(ownerName);
			if (owner == null) throw new GameEventException(this, "Unknown player '"+ownerName+"'");
			
			IDiplomacyMarker diplomacyMarker = owner.getDiplomacyMarker(targetName);
			if (diplomacyMarker.getTurn() > db.getConfig().getTurn()) throw new GameEventException(this, "No marker should be more recent than current turn.");
			
			owner.setDiplomacyMarker(targetName, isAllowedToLand, foreignPolicy);
			
			db.fireLog(String.format("%s changed diplomacy toward %s : %s, $diplomacy.foreignPolicy.%s$", ownerName, targetName, isAllowedToLand ? "allowed to land" : "not allowed to land", foreignPolicy.toString()));
			db.firePlayerChangeEvent(ownerName);
		}
	}
	
	/**
	 * Update the area with the same location of the given source (areaUpdate).
	 */
	public static class UpdateArea extends AGameEvent implements IPerTurnEvent, Serializable
	{
		private final IArea areaUpdate;
		
		public UpdateArea(IArea areaUpdate)
		{
			this.areaUpdate = areaUpdate;
		}
		
		@Override
		public boolean replace(IPerTurnEvent e)
		{
			if (!UpdateArea.class.isInstance(e)) return false;
			UpdateArea ua = (UpdateArea) e;
			return areaUpdate.getLocation().equals(ua.areaUpdate.getLocation());
		}
		
		@Override
		public void process(IGameEventExecutor executor, SEPCommonDB db) throws GameEventException
		{
			if (isGlobalView(executor)) return;
			
			IArea area = db.getArea(areaUpdate.getLocation());
			area.update(areaUpdate);
			
			db.fireAreaChangedEvent(areaUpdate.getLocation());
		}
	}
	
	/**
	 * Fire log message, and update areas if any.
	 */
	public static class LogMessage extends AGameEvent implements Serializable
	{
		private final String message;
		private final Set<IArea> areasUpdate;
		
		public LogMessage(String message, IArea ... areasUpdate)
		{
			this.message = message;
			if (areasUpdate != null && areasUpdate.length > 0)
			{
				this.areasUpdate = new HashSet<IArea>(Arrays.asList(areasUpdate));
			}
			else
			{
				this.areasUpdate = null;
			}
		}
		
		@Override
		public void process(IGameEventExecutor executor, SEPCommonDB db) throws GameEventException
		{
			if (isGlobalView(executor))
			{
				for(IArea area : areasUpdate)
				{
					fireEventForObservers(executor, area, db.getPlayersNames(), new UpdateArea(area));
					db.fireAreaChangedEvent(area.getLocation());
				}
			}
			
			db.fireLog(message);
		}
	}
	
	/**
	 * Create a diplomacy object for given owner/target pair.
	 * Must only be called on owner DB view and global DB view.
	 */
	public static class CreateDiplomacy extends AGameEvent implements Serializable
	{
		private final String ownerName;
		private final String targetName;
		private final boolean isAllowedToLand;
		private final eForeignPolicy foreignPolicy;
		
		public CreateDiplomacy(String ownerName, String targetName, boolean isAllowedToLand, eForeignPolicy foreignPolicy)
		{
			this.ownerName = ownerName;
			this.targetName = targetName;
			this.isAllowedToLand = isAllowedToLand;
			this.foreignPolicy = foreignPolicy;
		}
		
		@Override
		public void process(IGameEventExecutor executor, SEPCommonDB db) throws GameEventException
		{
			if (!isGlobalView(executor))
			{
				checkPlayerView(ownerName, executor);
			}
			
			IPlayer owner = db.getPlayer(ownerName);
			if (owner == null) throw new GameEventException(this, "Cannot find player '"+ownerName+"'");

			owner.setDiplomacy(targetName, isAllowedToLand, foreignPolicy);			
		}
	}
	
	/**
	 * Create Universe on empty DB.
	 * Nothing is done if DB is not empty (@see {@link SEPCommonDB#isUniverseCreated()}
	 */
	public static class UniverseCreation extends AGameEvent implements Serializable
	{
		final private Map<String, IPlayerConfig> players;
		//final private Map<Location, ICelestialBody> celestialBodies;
		final private Set<ICelestialBody> celestialBodies;
		final private Map<IProductiveCelestialBody, String> ownershipRelations;
		final private GameConfig config;
		
		public UniverseCreation(GameConfig config, Map<String, IPlayerConfig> players, Set<ICelestialBody> celestialBodies, Map<IProductiveCelestialBody, String> ownershipRelations)
		{
			this.config = config;
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
			executor = null; // Ensure not to use executor for UniverseCreation event.			
			
			try
			{
				IGameConfig config = db.getConfig();							
				
				if (skipCondition(db)) return;
				
				GameConfigCopier.copy(IGameConfig.class, this.config, config);
				
				// Generate players
				for(String playerName : players.keySet())
				{
					IPlayerConfig playerCfg = players.get(playerName);
					db.createPlayer(playerName, playerCfg.getColor(), playerCfg.getSymbol(), playerCfg.getPortrait());
				}				
				
				// Generate Universe
				
				Map<String, IPlanet> playerStartingPlanets = new HashMap<String, IPlanet>();
				for(ICelestialBody celestialBody : celestialBodies)
				{
					ICelestialBody connectedCelestialBody = db.createCelestialBody(celestialBody);
					
					if (ownershipRelations.containsKey(celestialBody))
					{
						String playerName = ownershipRelations.get(celestialBody);
						
						((IProductiveCelestialBody) connectedCelestialBody).setOwner(playerName);
						
						if (IPlanet.class.isInstance(celestialBody) && !playerStartingPlanets.containsKey(playerName))
						{
							IPlanet planet = (IPlanet) celestialBody;
							
							// Player starting planet
							playerStartingPlanets.put(playerName, planet);
							
							// If victory rule "Regimicide" is on, starting planet has a pre-built government module.						
							if (config.isRegimicide())
							{
								IGovernmentModule governmentModule = SEPCommonDB.makeGovernmentModule(planet.getName(), config.getTurn(), 1);
								governmentModule = db.createGovernmentModule(governmentModule);							
							}
						}
					}
					
					db.fireAreaChangedEvent(celestialBody.getLocation());
				}
				
				config.setTurn(0);								
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
	public static class RessourcesGeneration extends AGameEvent implements Serializable
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
				
				String logMessage = String.format("%s extracted %dC", productiveCelestialBody.getName(), extractedCarbon);
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
					
					logMessage = String.format("%s and generated %dP", logMessage, generatedPopulation);
					planet.generatePopulation(generatedPopulation);
				}
				
				db.fireLog(logMessage);
				db.fireAreaChangedEvent(productiveCelestialBody.getLocation());
			}
		}
	}
	
	/**
	 * Log a unit move and fire areas update
	 * Also fire UpdateArea events for given areas.
	 */
	public static class UnitMoveUpdate extends AGameEvent implements Serializable
	{
		private final IUnitMarker unitMarker;
		private final String markerMessage;
		private final String unitMessage;
		private final Set<IArea> areasUpdate;
		
		public UnitMoveUpdate(IUnitMarker unitMarker, String markerMessage, String unitMessage, IArea ... areasUpdate)
		{
			this.unitMarker = unitMarker;
			this.markerMessage = markerMessage;
			this.unitMessage = unitMessage;
			this.areasUpdate = new HashSet<IArea>(Arrays.asList(areasUpdate));			
		}
		
		@Override
		public void process(IGameEventExecutor executor, SEPCommonDB db) throws GameEventException
		{
			if (isGlobalView(executor))
			{
				for(IArea area : areasUpdate)
				{
					executor.onGameEvent(new UpdateArea(area), db.getPlayersNames());
				}
			}
			logUnitMarker(unitMarker, db, markerMessage, unitMessage);
		}
	}
	
	/**
	 * Log a make unit/starships event.
	 * Also fire UpdateArea event for given area.
	 */
	public static class UnitMade extends UnitMoveUpdate implements Serializable
	{
		private final Map<StarshipTemplate, Integer> newcomers;
		
		private static int getQuantity(Map<StarshipTemplate, Integer> newcomers)
		{
			int qt = 0;
			for(StarshipTemplate t : newcomers.keySet()) qt += newcomers.get(t);
			return qt;
		}
		
		public UnitMade(IUnitMarker unitMarker, IArea area)
		{
			super(unitMarker, String.format("%s@%s made on $location$", unitMarker.getOwnerName(), unitMarker.getName()), null, area);
			this.newcomers = null;
		}
		
		public UnitMade(IUnitMarker unitMarker, IArea area, Map<StarshipTemplate, Integer> newcomers)
		{
			super(unitMarker, String.format("%s@%s joined by %d new starships", unitMarker.getOwnerName(), unitMarker.getName(), getQuantity(newcomers)), null, area);
			this.newcomers = new HashMap<StarshipTemplate, Integer>();
			this.newcomers.putAll(newcomers);
		}
	}
	
	/**
	 * Log a dismantle unit event.
	 * Also fire UpdateArea event for given area.
	 */
	public static class UnitDismantled extends UnitMoveUpdate implements Serializable
	{
		public UnitDismantled(IUnitMarker unitMarker, IArea area)
		{
			super(unitMarker, String.format("%s@%s dismantled on $location$", unitMarker.getOwnerName(), unitMarker.getName()), null, area);			
		}
	}
	
	/**
	 * Log a unit encounter (on unit log publication, or celestial body direct observation).
	 */
	public static class EncounterLogPublication extends AGameEvent implements Serializable
	{
		private final String productiveCelestialBodyName;
		
		private final String unitOwnerName;
		private final String unitName;
		
		private final IUnitMarker encounteredUnitMarker;
		
		public EncounterLogPublication(String productiveCelestialBodyName, IUnitMarker encounteredUnitMarker)
		{
			this.productiveCelestialBodyName = productiveCelestialBodyName;
			this.encounteredUnitMarker = encounteredUnitMarker;
			
			this.unitOwnerName = null;
			this.unitName = null;
		}
		
		public EncounterLogPublication(String unitOwnerName, String unitName, IUnitMarker encounteredUnitMarker)
		{
			this.unitOwnerName = unitOwnerName;
			this.unitName = unitName;			
			this.encounteredUnitMarker = encounteredUnitMarker;
			
			this.productiveCelestialBodyName = null;
		}
		
		@Override
		public void process(IGameEventExecutor executor, SEPCommonDB db) throws GameEventException
		{
			if (productiveCelestialBodyName != null)
			{
				ICelestialBody cb = db.getCelestialBody(productiveCelestialBodyName);
				if (!IProductiveCelestialBody.class.isInstance(cb)) throw new GameEventException(this, "Celestial body is not a productive celestial body");
				IProductiveCelestialBody pcb = (IProductiveCelestialBody) cb;
				
				if (!isGlobalView(executor))
				{
					checkPlayerView(pcb.getOwner(), executor);
				}
				
				IUnitMarker um = db.createUnitMarker(encounteredUnitMarker);
				// TODO: Add unit marker ? To link log message to detailed informations (e.g. fleet composition, unitMarker.toString())
				db.fireLog(String.format("Unit %s@%s travelled over %s", um.getOwnerName(), um.getName(), productiveCelestialBodyName));
				db.fireAreaChangedEvent(pcb.getLocation());
				return;
			}
			
			if (!isGlobalView(executor))
			{
				checkPlayerView(unitOwnerName, executor);
			}
			
			IUnit unit = db.getUnit(unitOwnerName, unitName, null);
			if (unit == null) throw new GameEventException(this, "Cannot find unit "+unitOwnerName+"@"+unitName);
			
			IUnitMarker um = db.createUnitMarker(encounteredUnitMarker);
			// TODO: Add unit marker ? To link log message to detailed informations (e.g. fleet composition, unitMarker.toString())
			db.fireLog(String.format("Unit %s repport it saw %s@%s traveling on %s at turn %d", unitName, um.getOwnerName(), um.getName(), um.getRealLocation().asLocation(), um.getTurn()));
			db.fireAreaChangedEvent(um.getRealLocation().asLocation());
		}
	}
		
	/**
	 * Moves traveling units.
	 */
	public static class UnitsMoves extends AGameEvent implements Serializable
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
				else if (IFleet.class.isInstance(u))
				{
					IFleet f = (IFleet) u;
					if (f.nextDestination())
					{
						movingUnits.add(f);						
					}
				}
				/*
				else
				{
					stoppedUnits.add(u);
				}
				*/
			}
			
			if (isGlobalView(executor))
			{			
				double step = 0D;
				
				Map<IUnit, Map<IUnit, Double>> unitsEncounters = new HashMap<IUnit, Map<IUnit,Double>>();
				Map<String, Map<IUnit, Double>> productiveCelestialBodiesEncounters = new HashMap<String, Map<IUnit,Double>>();				
				
				if (!movingUnits.isEmpty()) while(step < 1)
				{
					IUnit fastestUnit = null;
					double minDistance = Double.POSITIVE_INFINITY;
					
					for(IUnit u : movingUnits)
					{
						if (fastestUnit == null || u.getSpeed() > fastestUnit.getSpeed()) fastestUnit = u;
						
						RealLocation uLoc = SEPUtils.getMobileLocation(u.getDeparture().asRealLocation(), u.getDestination().asRealLocation(), u.getSpeed(), u.getTravelingProgress(), step, true);
						
						ICelestialBody cb = db.getArea(uLoc.asLocation()).getCelestialBody();
						if (cb != null && IProductiveCelestialBody.class.isInstance(cb))
						{
							IProductiveCelestialBody pcb = (IProductiveCelestialBody) cb;
							if (!u.getOwnerName().equals(pcb.getOwner()))
							{
								if (!productiveCelestialBodiesEncounters.containsKey(pcb.getName())) productiveCelestialBodiesEncounters.put(pcb.getName(), new HashMap<IUnit, Double>());
								productiveCelestialBodiesEncounters.get(pcb.getName()).put(u, step);
							}
						}
						
						for(IUnit v : allUnits)
						{
							if (u == v) continue;
							if (u.getOwnerName().equals(v.getOwnerName())) continue;
														
							RealLocation vLoc = v.isStopped() ? v.getRealLocation() : SEPUtils.getMobileLocation(v.getDeparture().asRealLocation(), v.getDestination().asRealLocation(), v.getSpeed(), v.getTravelingProgress(), step, true);
							double d = SEPUtils.getDistance(uLoc, vLoc);
							
							// Traveling unit can only report about other traveling units or deployed probes.
							if (u.getSight() >= d && (!v.isStopped() || (v.getType() == eUnitType.Probe && ((IProbe) v).isDeployed())))
							{
								if (!unitsEncounters.containsKey(u)) unitsEncounters.put(u, new HashMap<IUnit, Double>());
								unitsEncounters.get(u).put(v, step);
							}
							
							if (v.getSight() >= d)
							{
								if (!unitsEncounters.containsKey(v)) unitsEncounters.put(v, new HashMap<IUnit, Double>());
								unitsEncounters.get(v).put(u, step);
							}
							
							if (d > u.getSight() || d > v.getSight()) minDistance = Math.min(minDistance, d);
						}
					}
					
					step += minDistance / fastestUnit.getSpeed();
				}
			
				for(IUnit u : unitsEncounters.keySet())
				{
					for(IUnit v : unitsEncounters.get(u).keySet())
					{
						step = unitsEncounters.get(u).get(v);
												
						if (eUnitType.Probe == u.getType() && ((IProbe) u).isDeployed())
						{
							// if observer is a deployed probe, directly publish the encounter.
							EncounterLogPublication encounterLogPublication = new EncounterLogPublication(u.getOwnerName(), u.getName(), v.getMarker(step));
							executor.onGameEvent(encounterLogPublication, new HashSet<String>(Arrays.asList(u.getOwnerName())));
						}
						else
						{						
							// else, just log the encounter (logs will be published on unit arrival).
							u.logEncounter(v.getMarker(step));
						}
					}
				}
				
				for(String productiveCelestialBodyName : productiveCelestialBodiesEncounters.keySet())
				{
					IProductiveCelestialBody pcb = (IProductiveCelestialBody) db.getCelestialBody(productiveCelestialBodyName);
					for(IUnit v : productiveCelestialBodiesEncounters.get(productiveCelestialBodyName).keySet())
					{
						step = productiveCelestialBodiesEncounters.get(productiveCelestialBodyName).get(v);
						EncounterLogPublication encounterLogPublication = new EncounterLogPublication(productiveCelestialBodyName, v.getMarker(step));
						executor.onGameEvent(encounterLogPublication, new HashSet<String>(Arrays.asList(pcb.getOwner())));
					}
				}
			}
			
			for(IUnit u : movingUnits)
			{
				Location oldLocation = u.getRealLocation().asLocation();
				RealLocation departure = u.getDeparture().asRealLocation();
				RealLocation destination = u.getDestination().asRealLocation();
				RealLocation newLocation = SEPUtils.getMobileLocation(departure, destination, u.getSpeed(), u.getTravelingProgress(), 1, true);
				double progress = u.getTravelingProgress() + SEPUtils.getProgressStep(departure, destination, u.getSpeed(), 1);
				
				String markerMessage = null, unitMessage = null;
				boolean updateAreas = false;
				
				if (u.getTravelingProgress() == 0)
				{
					updateAreas = true;
					markerMessage = String.format("%s@%s is taking of from %s", u.getOwnerName(), u.getName(), u.getDepartureName());
					unitMessage = String.format("%s@%s is taking of from %s to $destination$", u.getOwnerName(), u.getName(), u.getDepartureName());
				}
				
				if (progress < 1)
				{
					u.setTravelingProgress(progress);
					//db.getArea(u.getRealLocation().asLocation());
				}
				else
				{
					updateAreas = true;
					markerMessage = String.format("%s@%s arrived to $location$", u.getOwnerName(), u.getName());
										
					u.setDeparture(u.getDestination());
					u.setDestination(null);
					u.onArrival(executor);					
				}
				
				if (isGlobalView(executor))
				{
					// Logging unit lift-off					
					UnitMoveUpdate umu = new UnitMoveUpdate(u.getMarker(1), markerMessage, unitMessage, db.getArea(oldLocation), db.getArea(newLocation.asLocation()));
					fireEventForObservers(executor, db.getArea(u.getDeparture()), db.getPlayersNames(), umu);
				}
				
				db.fireAreaChangedEvent(oldLocation);
				db.fireAreaChangedEvent(newLocation.asLocation());
			}
		}
	}	
	
	public static class AntiProbeMissileExplosion extends AGameEvent implements IConditionalEvent, Serializable
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
				// Target is already deployed when APM is fired, so the target location should not change unless target disappeared.
				if (!target.getRealLocation().asLocation().equals(location)) throw new GameEventException(this, "Probe location differs "+target.getRealLocation().asLocation()+" != "+location);
				target.destroy();
			}
			
			IAntiProbeMissile antiProbeMissile = db.getAntiProbeMissile(antiProbeMissileOwnerName, antiProbeMissileName);
			if (!antiProbeMissile.getRealLocation().asLocation().equals(location)) throw new GameEventException(this, "AntiProbeMissile location differs "+antiProbeMissile.getRealLocation().asLocation()+" != "+location);
			antiProbeMissile.destroy();
			
			db.fireLog(String.format("%s@%s %s target Probe %s@%s", antiProbeMissile.getOwnerName(), antiProbeMissile.getName(), target == null ? "did not found" : "destroyed", targetOwnerName, targetName));
			db.fireAreaChangedEvent(location);
		}
	}
}
