package org.axan.sep.server;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.axan.eplib.clientserver.rpc.RpcException;
import org.axan.eplib.orm.ISQLDataBase;
import org.axan.eplib.orm.ISQLDataBaseStatement;
import org.axan.eplib.orm.ISQLDataBaseStatementJob;
import org.axan.eplib.orm.SQLDataBaseException;
import org.axan.eplib.orm.sqlite.SQLiteDB;
import org.axan.eplib.statemachine.StateMachine.StateMachineNotExpectedEventException;
import org.axan.eplib.utils.Basic;
import org.axan.sep.common.IGameBoard;
import org.axan.sep.common.PlayerGameBoard;
import org.axan.sep.common.Protocol;
import org.axan.sep.common.Rules;
import org.axan.sep.common.SEPUtils;
import org.axan.sep.common.GameConfigCopier.GameConfigCopierException;
import org.axan.sep.common.IGameBoard.GameBoardException;
import org.axan.sep.common.Protocol.eBuildingType;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.SEPUtils.RealLocation;
import org.axan.sep.common.db.IArea;
import org.axan.sep.common.db.IAssignedFleet;
import org.axan.sep.common.db.IBuilding;
import org.axan.sep.common.db.ICarbonOrder;
import org.axan.sep.common.db.ICelestialBody;
import org.axan.sep.common.db.IDiplomacy;
import org.axan.sep.common.db.IFleetComposition;
import org.axan.sep.common.db.IGameConfig;
import org.axan.sep.common.db.IGovernment;
import org.axan.sep.common.db.IGovernmentModule;
import org.axan.sep.common.db.IMovePlan;
import org.axan.sep.common.db.IPlayer;
import org.axan.sep.common.db.IPlayerConfig;
import org.axan.sep.common.db.IProductiveCelestialBody;
import org.axan.sep.common.db.ISpaceRoad;
import org.axan.sep.common.db.IStarshipTemplate;
import org.axan.sep.common.db.IUnitArrivalLog;
import org.axan.sep.common.db.IUnitEncounterLog;
import org.axan.sep.common.db.IVersionedFleet;
import org.axan.sep.common.db.IVersionedPlanet;
import org.axan.sep.common.db.IVersionedProbe;
import org.axan.sep.common.db.IVersionedProductiveCelestialBody;
import org.axan.sep.common.db.IVersionedSpecialUnit;
import org.axan.sep.common.db.IVersionedUnit;
import org.axan.sep.common.db.IVortex;
import org.axan.sep.common.db.SEPCommonDB;
import org.axan.sep.common.db.orm.Area;
import org.axan.sep.common.db.orm.AssignedFleet;
import org.axan.sep.common.db.orm.Building;
import org.axan.sep.common.db.orm.CarbonOrder;
import org.axan.sep.common.db.orm.Diplomacy;
import org.axan.sep.common.db.orm.FleetComposition;
import org.axan.sep.common.db.orm.Government;
import org.axan.sep.common.db.orm.GovernmentModule;
import org.axan.sep.common.db.orm.MovePlan;
import org.axan.sep.common.db.orm.Player;
import org.axan.sep.common.db.orm.PlayerConfig;
import org.axan.sep.common.db.orm.ProductiveCelestialBody;
import org.axan.sep.common.db.orm.SpaceRoad;
import org.axan.sep.common.db.orm.SpecialUnit;
import org.axan.sep.common.db.orm.StarshipTemplate;
import org.axan.sep.common.db.orm.Unit;
import org.axan.sep.common.db.orm.UnitArrivalLog;
import org.axan.sep.common.db.orm.UnitEncounterLog;
import org.axan.sep.common.db.orm.VersionedAsteroidField;
import org.axan.sep.common.db.orm.VersionedNebula;
import org.axan.sep.common.db.orm.VersionedPlanet;
import org.axan.sep.common.db.orm.Vortex;

/**
 * Server IGameBoard class.
 * Represents the game board on server side (all players).
 * Must be implemented by db. 
 */
class GameBoard implements Serializable, IGameBoard
{
	private static final long serialVersionUID = 1L;
	
	/// UPDATE

	///

	static abstract class ATurnResolvingEvent implements Comparable<ATurnResolvingEvent>
	{
		private final Integer priority;
		private final String name;

		public ATurnResolvingEvent(int priority, String name)
		{
			this.priority = priority;
			this.name = name;
		}

		@Override
		public int compareTo(ATurnResolvingEvent o)
		{
			return this.priority.compareTo(o.priority);
		}

		/**
		 * This method must test is the current event is actually fired in the
		 * current DB state. And proceed if and only if it is. It can add new
		 * event to the eventQueue.
		 * 
		 * @param eventsQueue
		 *            Current event queue.
		 * @param db
		 *            Current DB.
		 */
		public abstract void run(SortedSet<ATurnResolvingEvent> eventsQueue, GameBoard sepDB) throws GameBoardException;
	}
 
	public void resolveCurrentTurn()
	{
		SortedSet<ATurnResolvingEvent> resolvingEvents = new TreeSet<ATurnResolvingEvent>(getResolvingEvents());

		while (!resolvingEvents.isEmpty())
		{
			ATurnResolvingEvent event = resolvingEvents.first();
			resolvingEvents.remove(event);
			try
			{
				event.run(resolvingEvents, this);
			}
			catch(GameBoardException e)
			{
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
	}
		
	private static final Logger log = Logger.getLogger(GameBoard.class.getName());
	
	private static final Random rnd = new Random();
	
	private SEPCommonDB commonDB;
	private transient ISQLDataBase db;

	// Game
	private String	nextCelestialBodyName	= "A";

	private String generateCelestialBodyName()
	{
		String result = nextCelestialBodyName;
		if (nextCelestialBodyName.toLowerCase().charAt(nextCelestialBodyName.length() - 1) == 'z')
		{
			nextCelestialBodyName += "a";
		}
		else
		{
			nextCelestialBodyName = nextCelestialBodyName.substring(0, nextCelestialBodyName.length() - 1) + (char) (nextCelestialBodyName.charAt(nextCelestialBodyName.length() - 1) + 1);
		}
		return result;
	}
	
	// Game DataBase
	
	public GameBoard(ISQLDataBase db, Map<Player, PlayerConfig> players, org.axan.sep.common.GameConfig config) throws IOException, GameConfigCopierException, SQLDataBaseException
	{
		this.commonDB = new SEPCommonDB(db, config);
		this.db = commonDB.getDB();
		
		// Generate Universe	
		// Make the sun
		Location sunLocation = new Location(config.getDimX() / 2, config.getDimY() / 2, config.getDimZ() / 2);

		for(int x = -Math.min(config.getSunRadius(), sunLocation.x); x <= Math.min(config.getSunRadius(), sunLocation.x); ++x)
			for(int y = -Math.min(config.getSunRadius(), sunLocation.y); y <= Math.min(config.getSunRadius(), sunLocation.y); ++y)
				for(int z = -Math.min(config.getSunRadius(), sunLocation.z); z <= Math.min(config.getSunRadius(), sunLocation.z); ++z)
				{
					Location parsedLoc = new Location(sunLocation.x + x, sunLocation.y + y, sunLocation.z + z);
					if (SEPUtils.getDistance(parsedLoc, sunLocation) <= config.getSunRadius())
					{
						insertArea(parsedLoc, true);
					}
				}

		// Add the players starting planets.
		Set<Location> playersPlanetLocations = new HashSet<Location>();
		
		for(Player player : players.keySet())
		{
			insertPlayer(player, players.get(player));								
			
			// Found a location to pop the planet.
			Location planetLocation;
			boolean locationOk;
			do
			{
				locationOk = false;
				planetLocation = new Location(rnd.nextInt(config.getDimX()), rnd.nextInt(config.getDimY()), rnd.nextInt(config.getDimZ()));

				if (areaExists(planetLocation)) continue;

				locationOk = true;
				for(Location l : playersPlanetLocations)
				{
					if (isTravellingTheSun(planetLocation.asRealLocation(), l.asRealLocation()))
					{
						locationOk = false;
						break;
					}										

					if (!locationOk) break;
				}
			} while(!locationOk);

			insertArea(planetLocation, false);
			insertStartingPlanet(generateCelestialBodyName(), planetLocation, player.getName());
			playersPlanetLocations.add(planetLocation);									
		}

		// Add neutral celestial bodies
		for(int i = 0; i < config.getNeutralCelestialBodiesCount(); ++i)
		{
			// Found a location to pop the celestial body
			Location celestialBodyLocation;
			do
			{
				celestialBodyLocation = new Location(rnd.nextInt(config.getDimX()), rnd.nextInt(config.getDimY()), rnd.nextInt(config.getDimZ()));
			} while(areaExists(celestialBodyLocation) && areaHasCelestialBody(celestialBodyLocation));

			eCelestialBodyType celestialBodyType = Basic.getKeyFromRandomTable(config.getNeutralCelestialBodiesGenerationTable());

			String nextName = generateCelestialBodyName();
			
			if (!areaExists(celestialBodyLocation)) insertArea(celestialBodyLocation, false);
			insertCelestialBody(celestialBodyType, nextName, celestialBodyLocation);
		}
		
		getConfig().setTurn(1);
	}
	
	public SEPCommonDB getSEPDB()
	{
		return commonDB;
	}
	
	public IGameConfig getConfig()
	{
		return commonDB.getConfig();
	}
	
	private void compilePlayerView(String playerLogin, SEPCommonDB currentView, int maxTurn) throws SQLDataBaseException
	{
		// GameConfig, already copied during DB creation.
		
		Set<IArea> areas = new HashSet<IArea>();
		Set<IPlayer> players = new HashSet<IPlayer>();
		Set<IPlayerConfig> playerConfigs = new HashSet<IPlayerConfig>();
		Set<ICelestialBody> celestialBodies = new HashSet<ICelestialBody>();
		Set<IStarshipTemplate> starshipTemplates = new HashSet<IStarshipTemplate>();
		Set<ICarbonOrder> carbonOrders = new HashSet<ICarbonOrder>();
		
		// Update areas
		areas.addAll(Area.select(commonDB, IArea.class, null, null));
		
		// Update players
		players.addAll(Player.select(commonDB, IPlayer.class, null, null));
		
		// Update player configs
		playerConfigs.addAll(PlayerConfig.select(commonDB, IPlayerConfig.class, null, null));
		
		// Update celestial bodies
		celestialBodies.addAll(ProductiveCelestialBody.selectUnversioned(commonDB, IProductiveCelestialBody.class, null, null));
		celestialBodies.addAll(Vortex.select(commonDB, IVortex.class, null, null));
		
		// Update starship templates
		starshipTemplates.addAll(StarshipTemplate.select(commonDB, IStarshipTemplate.class, null, null));
		
		// Update player carbon orders (live table, assume initial player view carbon order table is empty)
		carbonOrders.addAll(CarbonOrder.select(commonDB, ICarbonOrder.class, null, "owner = '%s'", playerLogin));		
		
		for(IArea a : areas)
		{
			Area.insertOrUpdate(currentView, a);
		}
		
		for(IPlayer p : players)
		{
			Player.insertOrUpdate(currentView, p);
		}
		
		for(IPlayerConfig pc : playerConfigs)
		{
			PlayerConfig.insertOrUpdate(currentView, pc);
		}
		
		for(ICelestialBody cb : celestialBodies)
		{
			if (IVortex.class.isInstance(cb))
			{
				Vortex.insertOrUpdate(currentView, IVortex.class.cast(cb));
			}
			else if (IProductiveCelestialBody.class.isInstance(cb))
			{
				ProductiveCelestialBody.insertOrUpdate(currentView, IProductiveCelestialBody.class.cast(cb));
			}
		}
		
		for(IStarshipTemplate st : starshipTemplates)
		{
			StarshipTemplate.insertOrUpdate(currentView, st);
		}
		
		for(ICarbonOrder co : carbonOrders)
		{
			CarbonOrder.insertOrUpdate(currentView, co);
		}
		
		// TODO:
		//X	GameConfig, already copied during DB creation.
		//X	Update turn.
		//X	Area
		//X	Player
		//X		PlayerConfig (on assume que la config joueur est une info publique).
		//X	CelestialBody
		//X	StarshipTemplate
		//X	CarbonOrder (current player, live table create/delete, simple copy)
		//NOT REQUIRED, automated when updating versioned one:	Unit (only units with the first version <= currentTurn)
		//NOT REQUIRED, idem:	SpecialUnit (first version <= currentTurn)		
		//	AssignedFleet
		
		for(int i = 0; i <= maxTurn; ++i)
		{
			int currentTurn = i;
			
			Map<String, IGovernment> currentTurnGovernmentsView = new HashMap<String, IGovernment>();
			Map<String, Set<IDiplomacy>> currentTurnDiplomaciesView = new HashMap<String, Set<IDiplomacy>>();
			Map<String, IGovernment> currentTurnGovernments = new HashMap<String, IGovernment>();
			Map<String, Set<IDiplomacy>> currentTurnDiplomacies = new HashMap<String, Set<IDiplomacy>>();
			Set<IVersionedProbe> vprobes = new HashSet<IVersionedProbe>();
			Set<IVersionedProductiveCelestialBody> visiblesPcbs = new HashSet<IVersionedProductiveCelestialBody>();
			Set<IUnitArrivalLog> arrivalLogs =  new HashSet<IUnitArrivalLog>();
			
			// Select db governments for currentTurn
			Set<IGovernment> governments = Government.selectMaxVersion(commonDB, IGovernment.class, currentTurn, null, null);
			for(IGovernment g : governments)
			{
				currentTurnGovernments.put(g.getOwner(), g);
			}
			
			// Select db diplomacies for currentTurn
			Set<IDiplomacy> diplomacies = Diplomacy.selectMaxVersion(commonDB, IDiplomacy.class, currentTurn, null, null);
			for(IDiplomacy d : diplomacies)
			{
				if (!currentTurnDiplomacies.containsKey(d.getOwner())) currentTurnDiplomacies.put(d.getOwner(), new HashSet<IDiplomacy>());
				currentTurnDiplomacies.get(d.getOwner()).add(d);
			}
			
			// Update deployed player probes
			vprobes.addAll(Unit.selectMaxVersion(commonDB, IVersionedProbe.class, currentTurn, null, "VersionedUnit.type = '%s' AND VersionedUnit.owner = '%s' AND VersionedUnit.progress = 100.0", eUnitType.Probe, playerLogin));
			
			// Update visibles celestial bodies.
			// CelestialBody visible if owned by the player
			visiblesPcbs.addAll(ProductiveCelestialBody.selectMaxVersion(commonDB, IVersionedProductiveCelestialBody.class, currentTurn, null, "owner = '%s'", playerLogin));					
			// CelestialBody visible if a player unit is stopped on it with currentTurn version.
			visiblesPcbs.addAll(ProductiveCelestialBody.selectVersion(commonDB, IVersionedProductiveCelestialBody.class, currentTurn, "VersionedUnit VU", "(VU.owner = '%s') AND ((VU.progress = 0.0 AND VU.departure_x = CelestialBody.location_x AND VU.departure_y = CelestialBody.location_y AND VU.departure_z = CelestialBody.location_z) OR (VU.progress = 100.0 AND VU.destination_x = CelestialBody.location_x AND VU.destination_y = CelestialBody.location_y AND VU.destination_z = CelestialBody.location_z))", playerLogin));					
			// CelestialBody visible if under a deployed probe scope.
			visiblesPcbs.addAll(ProductiveCelestialBody.selectMaxVersion(commonDB, IVersionedProductiveCelestialBody.class, currentTurn, "VersionedUnit VU", "(VU.owner = '%s' AND VU.type = '%s' AND VU.turn <= VersionedProductiveCelestialBody.turn AND VU.progress = 100.0 AND ((VU.destination_x - CelestialBody.location_x) * (VU.destination_x - CelestialBody.location_x) + (VU.destination_y - CelestialBody.location_y) * (VU.destination_y - CelestialBody.location_y) + (VU.destination_z - CelestialBody.location_z) * (VU.destination_z - CelestialBody.location_z)) <= (%f * %f))", playerLogin, eUnitType.Probe, getConfig().getUnitTypeSight(eUnitType.Probe), getConfig().getUnitTypeSight(eUnitType.Probe)));
			
			// Update player UnitArrivalLog (visibles units stopped on pcb, arrival log are inserted in db only when they are published, so no filter needed here, simple copŷ).
			arrivalLogs.addAll(UnitArrivalLog.selectMaxVersion(commonDB, IUnitArrivalLog.class, currentTurn, null, "owner = '%s'", playerLogin));
			
			for(IVersionedProductiveCelestialBody vpcb : visiblesPcbs)
			{
				ProductiveCelestialBody.insertOrUpdate(currentView, vpcb);
			}
			
			for(IVersionedProbe vprobe : vprobes)
			{
				Unit.insertOrUpdate(currentView, vprobe);
			}
			
			for( IUnitArrivalLog ual : arrivalLogs)
			{
				UnitArrivalLog.insertOrUpdate(currentView, ual);
			}
			
			Set<IAssignedFleet> assignedFleets = new HashSet<IAssignedFleet>();
			Set<IBuilding> buildings = new HashSet<IBuilding>();
			Set<ISpaceRoad> roads = new HashSet<ISpaceRoad>();
			Set<IFleetComposition> comps = new HashSet<IFleetComposition>();
			Set<IMovePlan> moves = new HashSet<IMovePlan>();
			Set<IVersionedSpecialUnit> specialsUnits = new HashSet<IVersionedSpecialUnit>();
			Set<IVersionedUnit> seenUnits = new HashSet<IVersionedUnit>();
			Set<IUnitEncounterLog> encounterLogs = new HashSet<IUnitEncounterLog>();			
			Set<IVersionedUnit> vunits = new HashSet<IVersionedUnit>();
			
			for(IVersionedProductiveCelestialBody vpcb : visiblesPcbs)
			{
				Set<IVersionedUnit> currentPCBUnits = new HashSet<IVersionedUnit>();				
				
				// Update buildings (visibles pcbs)
				buildings.addAll(Building.selectMaxVersion(commonDB, IBuilding.class, currentTurn, null, "Building.celestialBodyName = '%s'", vpcb.getName()));
				
				// Update space roads (visibles pcbs)
				roads.addAll(SpaceRoad.select(commonDB, ISpaceRoad.class, null, "(SpaceRoad.spaceCounterACelestialBodyName = '%s' OR SpaceRoad.spaceCounterBCelestialBodyName = '%s') AND (MAX(SpaceRoad.spaceCounterATurn, SpaceRoad.spaceCounterBTurn) <= %d)", vpcb.getName(), vpcb.getName(), currentTurn));
				
				// Update units on visibles pcbs
				currentPCBUnits.addAll(Unit.selectMaxVersion(commonDB, IVersionedUnit.class, currentTurn, "CelestialBody CB", "CB.name = '%s' AND VersionedUnit.progress = 0.0 AND CB.location_x = VersionedUnit.departure_x AND CB.location_y AND VersionedUnit.departure_y AND CB.location_z = VersionedUnit.departure_z", vpcb.getName()));
				
				// Update assigned fleets
				assignedFleets.addAll(AssignedFleet.select(commonDB, IAssignedFleet.class, null, "celestialBody = '%s'", vpcb.getName()));
								
				vunits.addAll(currentPCBUnits);
								
				for(IVersionedUnit vunit : currentPCBUnits)
				{
					Set<IUnitEncounterLog> result = UnitEncounterLog.selectMaxVersion(commonDB, IUnitEncounterLog.class, currentTurn, null, "owner = '%s' AND unitName = '%s' AND turn = %d", vunit.getOwner(), vunit.getName(), vunit.getTurn());
					encounterLogs.addAll(result);
					for(IUnitEncounterLog uel : result)
					{
						Set<IVersionedUnit> currentUnitEncounteredUnits = Unit.selectVersion(commonDB, IVersionedUnit.class, uel.getSeenTurn(), null, "owner = '%s' AND name = '%s' AND type = '%s'", uel.getSeenOwner(), uel.getSeenName(), uel.getSeenType());
						for(IVersionedUnit encounteredUnit : currentUnitEncounteredUnits)
						{									
							if (IVersionedFleet.class.isInstance(encounteredUnit))
							{
								// (Fleet) Update encountered unit FleetComposition
								comps.addAll(FleetComposition.select(commonDB, IFleetComposition.class, null, "fleetOwner = '%s' AND fleetName = '%s' AND fleetTurn = %d", encounteredUnit.getOwner(), encounteredUnit.getName(), encounteredUnit.getTurn()));
								// (Fleet) Update encountered unit special units
								specialsUnits.addAll(SpecialUnit.selectVersion(commonDB, IVersionedSpecialUnit.class, currentTurn, null, "fleetOwner = '%s' AND fleetName = '%s' AND fleetTurn = '%s'", encounteredUnit.getOwner(), encounteredUnit.getName(), encounteredUnit.getTurn()));
							}																		
						}
						
						if (IVersionedFleet.class.isInstance(vunit))
						{
							// (Fleet) Update MovePlan, FleetComposition
							moves.addAll(MovePlan.select(commonDB, IMovePlan.class, null, "owner = '%s' AND name = '%s' AND turn = %d", vunit.getOwner(), vunit.getName(), vunit.getTurn()));
							comps.addAll(FleetComposition.select(commonDB, IFleetComposition.class, null, "fleetOwner = '%s' AND fleetName = '%s' AND fleetTurn = %d", vunit.getOwner(), vunit.getName(), vunit.getTurn()));
							// (Fleet) Update special units
							specialsUnits.addAll(SpecialUnit.selectVersion(commonDB, IVersionedSpecialUnit.class, currentTurn, null, "fleetOwner = '%s' AND fleetName = '%s' AND fleetTurn = '%s'", vunit.getOwner(), vunit.getName(), vunit.getTurn()));
						}
					}
					
					if (IVersionedFleet.class.isInstance(vunit))
					{						
						// Update governments (units)
						IGovernment g = currentTurnGovernments.get(vunit.getOwner());
						if (g != null && vunit.getName().matches(g.getFleetName()) && vunit.getTurn() == g.getTurn())
						{
							currentTurnGovernmentsView.put(g.getOwner(), g);
						}
					}
				}
				
				// Update governments (visibles pcbs)
				// 	Update diplomacies (visibles governments)
				IGovernment g = currentTurnGovernments.get(vpcb.getOwner());
				if (g != null && eCelestialBodyType.Planet.equals(vpcb.getType()) && vpcb.getName().matches(g.getPlanetName()) && vpcb.getTurn() == g.getPlanetTurn())
				{
					currentTurnGovernmentsView.put(g.getOwner(), g);
					currentTurnDiplomaciesView.put(g.getOwner(), currentTurnDiplomacies.get(g.getOwner()));
				}
			}
			
			for(IBuilding b : buildings)
			{
				Building.insertOrUpdate(currentView, b);
			}
			
			for(ISpaceRoad road : roads)
			{
				SpaceRoad.insertOrUpdate(currentView, road);
			}
			
			for(IVersionedUnit vu : vunits)
			{
				Unit.insertOrUpdate(currentView, vu);
			}
			
			for(IMovePlan m : moves)
			{
				MovePlan.insertOrUpdate(currentView, m);
			}
			
			for(IFleetComposition c : comps)
			{
				FleetComposition.insertOrUpdate(currentView, c);
			}
			
			for(IVersionedSpecialUnit vsu : specialsUnits)
			{
				SpecialUnit.insertOrUpdate(currentView, vsu);
			}
			
			for(IVersionedUnit vu : seenUnits)
			{
				Unit.insertOrUpdate(currentView, vu);
			}
			
			for(IUnitEncounterLog uel : encounterLogs)
			{
				UnitEncounterLog.insertOrUpdate(currentView, uel);
			}
			
			for(String player : currentTurnGovernmentsView.keySet())
			{
				IGovernment g = currentTurnGovernmentsView.get(player);
				if (g == null) continue;
				
				Government.insertOrUpdate(currentView, g);								
			}
			
			for(String player : currentTurnDiplomaciesView.keySet())
			{
				diplomacies = currentTurnDiplomaciesView.get(player);
				if (diplomacies == null || diplomacies.isEmpty()) continue;
				
				for(IDiplomacy d : diplomacies)
				{
					Diplomacy.insertOrUpdate(currentView, d);
				}							
			}
			
			//X Update visibles celestial bodies.
			//X		Update buildings (visibles pcbs)
			//X		Update space roads (visibles pcbs)
			//X		Update units (deployed probes)
			//X		Update units (visibles pcbs)
			//X			(Fleet)	MovePlan, FleetComposition
			//X			(Fleet) Update special units (visibles units)
			//X		Update governments (visibles pcbs)
			//X		Update governments (units)
			//X			Update diplomacies (visibles governments)
			//X	Update ArrivalLog (visibles units stopped on pcb)			
			//X	Update EncounterLog (visibles units stopped on pcb)
			//X	Update units (encounter logged)
			//X		(Fleet)	FleetComposition
			//X		(Fleet) Update special units		
		}
		
		// Update turn.
		currentView.getConfig().setTurn(maxTurn);
	}
	
	public Player getPlayer(String playerLogin) throws GameBoardException
	{		
		Set<Player> rs;
		
		try
		{
			rs = Player.select(commonDB, Player.class, null, "name = %s", playerLogin);			
		}
		catch(SQLDataBaseException e)
		{
			throw new GameBoardException("SQLDataBaseException", e);
		}
		
		if (rs == null || rs.isEmpty()) throw new GameBoardException("Player "+playerLogin+" is unkown");
		
		return rs.iterator().next();
	}
	
	@Override
	public Map<Player, PlayerConfig> getPlayerList() throws GameBoardException
	{
		Map<Player, PlayerConfig> result = new TreeMap<Player, PlayerConfig>();
		
		try
		{
			Set<Player> ps = Player.select(commonDB, Player.class, null, null);
			Set<PlayerConfig> pcs = PlayerConfig.select(commonDB, PlayerConfig.class, null, null);
			
			for(Player p : ps)
			{
				for(PlayerConfig pc : pcs)
				{
					if (p.getName().compareTo(pc.getName()) == 0)
					{
						result.put(p, pc);
						pcs.remove(pc);
						break;
					}
				}
			}
			
			return result;
		}
		catch(SQLDataBaseException e)
		{
			throw new GameBoardException(e);
		}
	}
	
	/**
	 * Must return a FULL (including previous turn) player game board. 
	 * @param playerLogin
	 * @throws SEPServerDataBaseException 
	 */
	public PlayerGameBoard getPlayerGameBoard(ISQLDataBase db, String playerLogin) throws GameBoardException
	{
		SEPServer.log.log(Level.INFO, "getGameBoard(" + playerLogin + ")");
		
		/*
		 * PlayerGameBoard basé sur la même DB que le serveur.
		 * Une méthode est capable d'insérer dans la DB toutes les entrées entre le tour précédent et le tour courrant visibles par le joueur.
		 * Pour compiler un playerGameBoard complet on empile à partir du tour 0, ainsi il n'y a qu'une seule méthode primitive.
		 */
		try
		{
			SEPCommonDB clientDB = new SEPCommonDB(db, getConfig());
			compilePlayerView(playerLogin, clientDB, getConfig().getTurn());
			return new PlayerGameBoard(clientDB);
		}
		catch(Exception e)
		{
			throw new GameBoardException(e);
		}
	}
	
	private static final SortedSet<ATurnResolvingEvent> resolvingEvents = new TreeSet<ATurnResolvingEvent>(); 
	
	public SortedSet<ATurnResolvingEvent> getResolvingEvents()
	{
		synchronized(resolvingEvents)
		{
			if (resolvingEvents.isEmpty())
			{
				// TODO: Implement resolving events.
				// SUIS LA: Remettre le codage de resolveTurn à plus tard, implémenter les features dans l'ordre logique d'incrémentation (envoi gameboard vierge, création flotte, déplacement flotte, ...)
				resolvingEvents.add(new ATurnResolvingEvent(0, "GlobalResolver")
				{
					
					@Override
					public void run(SortedSet<ATurnResolvingEvent> eventsQueue, GameBoard sepDB) throws GameBoardException
					{
						try
						{
							ISQLDataBase db = sepDB.getDB();							
							
							// les flottes avec une feuille de route décollent
							db.prepare("SELECT * FROM VersionedFleet VF LEFT JOIN MovePlan MP USING (owner, name) GROUP BY (owner, name, MP.turn) ORDER BY priority DESC WHERE MP.turn >= VF.turn", new ISQLDataBaseStatementJob<Void>()
							{
								@Override
								public Void job(ISQLDataBaseStatement stmnt) throws SQLDataBaseException
								{
									while(stmnt.step())									
									{
										//conn.exec(Strng.format())
										//INSERT INTO VersionedFleet 
										/*
										TABLE VersionedFleet (
										     owner TEXT NOT NULL,
										     name TEXT NOT NULL,
										     turn INTEGER NOT NULL,
										     type TEXT NOT NULL,
										 */
									}
									return null;
								}
							});
							
							/*								 									 
							
							flottes <- db.selectionner flottes immobiles avec feuille de route
							POUR CHAQUE flotte FAIRE
								db.inserer nouvelle version flotte (destination, progress)
							FPOUR
							
							//les unités se déplacent
							unites_mobiles <- db.selectionner unités en déplacement (dernière version de chaque unité)
							TANTQUE le temps s'ecoule FAIRE
								POUR CHAQUE unité FAIRE
									
									//les unités se rencontrent en mouvement
									SI l'unité rencontre une autre unité ALORS
										pour chaque unité: unité.loggerRencontre(step, autre unité)
									FSI
									
									// les unités sont attirés par des vortex (elles arrivent à destination imprévue)
									SI l'unité rencontre un vortex ALORS
										unité.loggerArrival(step, vortex)
										unité.changer position sur vortex.destination
										unité.immobiliser
									FSI
									SI l'unité arrive à destination ALORS
										SWITCH(unité.type)
											CASE (apm):
												// les apm détruisent les probes (qui communiquent leur destruction imminante)
												SI apm.cible visible ALORS
													cible.owner.communiquer destruction imminante
													détruire cible
													détruire apm
												FSI
												BREAK;
											CASE (probe):
												// les probes se déploient et communiquent aussitot leur log
												déployer probe
												unité.loggerArrival(step)
												communiquer log
												BREAK;
											CASE (spaceRoadDeliverer):
												// les spaceRoadDeliverer livre une space road
												spaceRoadDeliverer.livre la space road
												unité.loggerArrival(step)
												BREAK;
											CASE (carbonCarrier):
												// les carbonCarrier livrent leur carbone
												carbonCarrier.livre le carbone
												unité.loggerArrival(step)
												BREAK;
											CASE (Fleet):
												// les flottes déclenchent des conflits
												SI (flotte.attaque) ALORS
													flotte.déclarer conflit(corps céleste)
												FSI
												// les corps célestes peuvent engager un conflit quand une flotte indésirable arrive
												corpsCelestes.reagirArriveFlotte(flotte)
										FSWITCH
									FSI
									
								FPOUR
							FTANTQUE
							
							POUR CHAQUE productiveCelestialBody FAIRE
								SI un joueur à déclaré un conflit FAIRE
									noter le conflit
								FSI
							FPOUR
							
							POUR CHAQUE productiveCelestialBody FAIRE
								SI celestialBody en conflit ALORS
									// ResoudreConflit
									resoudre attitudes diplomatiques
									POUR CHAQUE round de combat FAIRE
										jouer le round
										mettre à jour le log du combat
									FPOUR
									mettre à jour l'état des flottes (endommagées, détruites)
									mettre à jour les logs de combat des joueurs dont une unité au moins à survécue
									// revérifier attitudes diplomatiques et relancer un conflit au besoin ?
									publier les logs de combat
								FSI
							FPOUR
							
							POUR CHAQUE unité immobile non posée
								unité.poser
								unité.publier log
							FPOUR
							
							? les unités immobiles loggent les départs/arrivées des autres unités
							génération du carbone et de la population sur les corps célestes
							incrémentation de la date
							 */
						}
						catch(SQLDataBaseException e)
						{
							throw new GameBoardException(e);
						}
					}
				});
				
				/*
				resolvingEvents.add(new ATurnResolvingEvent(0, "OnTimeTick")
				{					
					@Override
					public void run(SortedSet<ATurnResolvingEvent> eventsQueue, ISEPServerDataBase sepDB) throws SEPServerDataBaseException
					{
						try
						{
							SQLiteDB db = ((SEPSQLiteDB) sepDB).getDB();							
							
							db.exec(new SQLiteJob<Void>()
							{
								@Override
								protected Void job(SQLiteConnection conn) throws Throwable
								{
									SQLiteDBStatement stmnt = conn.prepare(String.format("SELECT U.type, * FROM Unit U LEFT JOIN	VersionedUnit VU USING (name, owner, type) LEFT JOIN	PulsarMissile PM USING (name, owner, type) LEFT JOIN	Probe P USING (name, owner, type) LEFT JOIN	AntiProbeMissile APM USING (name, owner, type) LEFT JOIN	CarbonCarrier CC USING (name, owner, type) LEFT JOIN	SpaceRoadDeliverer SRD USING (name, owner, type) LEFT JOIN	Fleet F USING (name, owner, type) LEFT JOIN	VersionedPulsarMissile VPM USING (name, owner, turn) LEFT JOIN	VersionedProbe VP USING (name, owner, turn) LEFT JOIN	VersionedAntiProbeMissile VAPM USING (name, owner, turn) LEFT JOIN	VersionedCarbonCarrier VCC USING (name, owner, turn) LEFT JOIN	VersionedFleet VF USING (name, owner, turn) WHERE VU.turn = %d AND VU.destination_x IS NOT NULL;", getConfig().getTurn()));
									Vector<VersionedUnit> movingUnits = new Vector<VersionedUnit>();
									while(stmnt.step())
									{
										Class<? extends org.axan.sep.common.db.sqlite.orm.IVersionedUnit> vuClazz = SEPSQLiteDB.getVersionedUnitClass(stmnt.columnString(0));
										movingUnits.add(new VersionedUnit(SQLiteORMGenerator.mapTo(vuClazz, stmnt), getConfig()));
									}
																		
									// Commencer par prédire la collision avec les vortex
									stmnt = conn.prepare(String.format("SELECT type,* FROM Vortex WHERE onsetDate <= %d AND %d < endDate", getConfig().getTurn(), getConfig().getTurn()));
									Vector<Vortex> vortex = new Vector<Vortex>();
									while(stmnt.step())
									{
										vortex.add(new Vortex(SQLiteORMGenerator.mapTo(org.axan.sep.common.db.sqlite.orm.Vortex.class, stmnt)));
									}																		
									
									Map<VersionedUnit, Vortex> vortexEncounters = new HashMap<VersionedUnit, Vortex>();
									
									double step = 0;
									VersionedUnit fasterUnit = null;
									double minDistance = Double.POSITIVE_INFINITY;
									VersionedUnit u, v;
									while(step < 1)
									{
										for(int i=0; i < movingUnits.size(); ++i)
										{
											u = movingUnits.elementAt(i);
											
											if (vortexEncounters.containsKey(u)) continue;
											
											double uStep = u.getProgress() + u.getSpeed()*step;
											RealLocation uLocation = SEPUtils.getMobileLocation(u.getDeparture(), u.getDestination(), uStep, true);
											
											double nearestVortexDistance = Double.POSITIVE_INFINITY;
											
											for(Vortex vor : vortex)
											{
												double distanceUvortex = SEPUtils.getDistance(uLocation, vor.getLocation().asRealLocation());												
												
												if (distanceUvortex <= getConfig().getVortexScope() && distanceUvortex < nearestVortexDistance)
												{
													nearestVortexDistance = distanceUvortex;
													vortexEncounters.put(u, vor);
												}
											}
											
											if (vortexEncounters.containsKey(u))
											{
												// TODO: u.loggerRencontre(vor, step);
											}
											
											if (fasterUnit == null || u.getSpeed() > fasterUnit.getSpeed())
											{
												fasterUnit = u;
											}
											
											for(int j=i+1; j < movingUnits.size(); ++j)
											{
												v = movingUnits.elementAt(j);
												
												if (vortexEncounters.containsKey(v)) continue;
												
												double vStep = v.getProgress() + v.getSpeed()*step;
												RealLocation vLocation = SEPUtils.getMobileLocation(v.getDeparture(), v.getDestination(), vStep, true);
												
												double distanceUV = SEPUtils.getDistance(uLocation, vLocation);
												boolean spotted = false;
												
												if (u.getSight() >= distanceUV)
												{
													// TODO: u.loggerRencontre(v, step);
													spotted = true;
												}
												if (v.getSight() >= distanceUV)
												{
													// TODO: v.loggerRencontre(u, step);
													spotted = true;
												}
												
												if (!spotted && minDistance < distanceUV) minDistance = distanceUV;
											}
										}
										
										step += minDistance / fasterUnit.getSpeed();
									}
									
									/*
									distance = f(unit1, unit2, t) // avec t écoulement du temps pour 1 tour.
									
									
									Déplacer les unités mobiles
									Ecrire leur journal de bord (rencontres, vortex, ...)
									
									---
									
									movingUnits <- getMovingUnits(turn);									
									step <- 0
									TANTQUE(step < 1)		
										// A chaque step garder trace de la collision survenant le plus tôt, et faire le step suivant directement à cette date (si elle survient dans le tour courant)
										fasterUnit <- null
										minDistance <- INFINI
										
										POUR CHAQUE movingUnits u FAIRE
											SI u.speed > fasterUnit.speed ALORS fasterUnit <- u;
											POUR CHAQUE movingUnits v FAIRE
												SI u = v ALORS continue;
												d <- Distance(u, v, step);
												
												SI u.sight >= d ALORS u.loggerRencontre(v, step);
												SI v.sight >= d ALORS v.loggerRencontre(u, step);
												
												SI (d > u.sight OU d > v.sight) ALORS minDistance <- Min(minDistance, d);												
											FPOUR
										FPOUR
										
										// Le plus petit incrément susceptible d'etre utile (mais peut etre pas vu que minDistance et fasterUnit ne sont pas vraiment liés).
										step += minDistance / fasterUnit.speed;
										 
									FPOUR
									
									
									*//*
									return null;
								}
							});
						}
						catch(SQLiteException e)
						{
							throw new SEPServerDataBaseException(e);
						}
					}
				});
				
				/*
				
	 *
	 * OnUnitArrival		Une unité spéciale arrive à destination.
	 * 	Les missiles pulsar engendrent un pulsar,
	 * 	les probes se déploient,
	 * 	les missiles anti-probes explosent en détruisant éventuellement une probe,
	 * 	les flottes déclenchent un conflit, se posent, repartent, et peuvent communiquer leur journal de bord.
	 * 	les spaceRoadDeliverer spawnent une spaceRoad, et peuvent communiquer leur journal de bord.
	 * 	les carbonCarrier spawn du carbone, éventuellement repartent, et peuvent communiquer leur journal de bord.
	 * 
	 * OnConflict			Un conflit est déclaré sur un cors céleste.
	 * 	On résoud le conflit concerné, en mettant à jour les journals de bords des flottes concernées (+ log du corps céleste champs de bataille communiqué en direct au joueur).
	 * 
	 * OnTimeTickEnd		Le temps à fini de s'écouler.
	 * 	On génère le carbone et la population pour le tour écoulé, on incrémente la date.
				 */
			}
		}
		
		return resolvingEvents;
	}
	
	// Game commands
	
	//TODO: Factoriser une interface pour server.GameBoard et common.PlayerGameBoard afin de pouvoir utiliser les meme IGameCommand depuis le moteur serveur et le moteur client.
	public GameBoard build(String playerLogin, String celestialBodyName, eBuildingType buildingType) throws GameBoardException
	{
		try
		{
			// Select productive celestial body by name, last version
			Set<IVersionedProductiveCelestialBody> pcbs = ProductiveCelestialBody.selectMaxVersion(commonDB, IVersionedProductiveCelestialBody.class, null, "name = '%s'", celestialBodyName);
			
			// celestialBodyName IS A ProductiveCelestialBody
			if (!pcbs.isEmpty())
			{
				throw new GameBoardException("Celestial body '" + celestialBodyName + "' does not exist, is not a productive one, or is not owned by '" + playerLogin + "'.");
			}
			
			IVersionedProductiveCelestialBody pcb = pcbs.iterator().next();
			IVersionedPlanet p = null;
			
			if (IVersionedPlanet.class.isInstance(pcb))
			{
				p = IVersionedPlanet.class.cast(pcb);
			}
			
			// Select last build date
			ISQLDataBaseStatement stmnt = null; // TODO
			/*
			stmnt.step();
			// celestialBody did not built anything else for the current game turn.
			if (stmnt.columnInt(0) >= getConfig().getTurn())
			{
				throw new GameBoardException("Celestial body '" + celestialBodyName + "' already in work for this turn.");
			}
			*/
			// Select number of buildings
			stmnt = null; // TODO
			/*
			stmnt.step();
			// celestialBody has free slots
			if (stmnt.columnInt(0) >= pcb.getMaxSlots())
			{
				throw new GameBoardException("No more free slots on celestial body '" + celestialBodyName + "'");
			}
			*/
			int carbonCost = 0, populationCost = 0, nbBuilt = 0;
			
			// Select current building for the given type
			Set<IBuilding> bs = Building.selectMaxVersion(commonDB, IBuilding.class, null, null, "TOTO");
			
			IBuilding b = null;
			if (!bs.isEmpty())
			{
				b = bs.iterator().next();
				
				if (!Rules.getBuildingCanBeUpgraded(b.getType()))
				{
					throw new GameBoardException(buildingType + " cannot be upgraded.");							
				}										
				
				nbBuilt = b.getNbSlots();
			}
			else
			{
				nbBuilt = 0;												
			}

			carbonCost = Rules.getBuildingUpgradeCarbonCost(buildingType, nbBuilt+1); 
			populationCost = Rules.getBuildingUpgradePopulationCost(buildingType, nbBuilt+1);
			
			// build/upgrade can be afforded
			if (populationCost > 0 && p == null)
			{
				throw new GameBoardException("Celestial body '" + celestialBodyName + "' is not a planet and '"+buildingType+"' cost population.");
			}
			
			if (pcb.getCurrentCarbon() < carbonCost)
			{
				throw new GameBoardException("Celestial body '" + celestialBodyName + "' is not a planet and '"+buildingType+"' cost population.");
			}
			
			// update new building
			// TODO
			
			// update carbon stock
			// update population stock
			// TODO
			//conn.exec(insertUpdateProductiveCelestialBodyStocksSQL(celestialBodyName, carbonCost, populationCost));
			//conn.exec(insertUpdateBuildingSQL(buildingType, celestialBodyName, nbBuilt+1));
			
			return this;
		}
		catch(SQLDataBaseException e)
		{
			throw new GameBoardException(e);
		}
	}
	
	// Private
	
	void insertCelestialBody(eCelestialBodyType celestialBodyType, String name, Location location) throws SQLDataBaseException
	{
		ICelestialBody cb;
		
		boolean productiveCelestialBody = (celestialBodyType != eCelestialBodyType.Vortex);
		
		int[] carbonAmount;
		int initialCarbon;
		int[] slotsAmount;
		int maxSlots;
		
		if (productiveCelestialBody)
		{
			// Fix carbon amount to the mean value.
			carbonAmount = getConfig().getCelestialBodiesStartingCarbonAmount(celestialBodyType);
			initialCarbon = rnd.nextInt(carbonAmount[1] - carbonAmount[0]) + carbonAmount[0];
			
			// Fix slots amount to the mean value.
			slotsAmount = getConfig().getCelestialBodiesSlotsAmount(celestialBodyType);
			maxSlots = rnd.nextInt(slotsAmount[1] - slotsAmount[0]) + slotsAmount[0];
			if (maxSlots <= 0) maxSlots = 1;
			
			switch(celestialBodyType)
			{
				case AsteroidField:
				{
					cb = new VersionedAsteroidField(name, celestialBodyType, location, initialCarbon, maxSlots, getConfig().getTurn(), null, initialCarbon, 0);
					break;
				}
				case Nebula:
				{
					cb = new VersionedNebula(name, celestialBodyType, location, initialCarbon, maxSlots, getConfig().getTurn(), null, initialCarbon, 0);
					break;
				}
				
				case Planet:
				{
					int[] populationPerTurnRange = getConfig().getPopulationPerTurn();
					int populationPerTurn = rnd.nextInt(populationPerTurnRange[1] - populationPerTurnRange[0]) + populationPerTurnRange[0];
					
					int[] populationLimitRange = getConfig().getPopulationLimit();
					int maxPopulation = rnd.nextInt(populationLimitRange[1] - populationLimitRange[0]) + populationLimitRange[0];
										
					cb = new VersionedPlanet(name, celestialBodyType, location, initialCarbon, maxSlots, getConfig().getTurn(), null, initialCarbon, 0, populationPerTurn, maxPopulation, 0);
					break;
				}
				
				default:
				{
					throw new Protocol.SEPImplementationException("'"+celestialBodyType+"' not implemented.");
				}
			}
		}
		else
		{
			switch(celestialBodyType)
			{
				case Vortex:
				{
					/*
					int turn = getConfig().getTurn();
					int[] lifetimeRange = getConfig().getVortexLifetime();
					int lifetime = rnd.nextInt(lifetimeRange[1] - lifetimeRange[0]) + lifetimeRange[0];				
					
					cb = new Vortex(name, celestialBodyType, location, turn, turn+lifetime, );
					*/
					// TODO:
					throw new Protocol.SEPImplementationException("insertCelestialBody(Vortex, ...) not Implemented");
				}
				
				default:
				{
					throw new Protocol.SEPImplementationException("'"+celestialBodyType+"' not implemented.");
				}
			}
		}
		
		if (cb != null)
		{
			if (productiveCelestialBody)
			{
				ProductiveCelestialBody.insertOrUpdate(commonDB, IProductiveCelestialBody.class.cast(cb));
			}
			else
			{
				Vortex.insertOrUpdate(commonDB, IVortex.class.cast(cb));
			}
		}
	}

	void insertPlayer(Player player, PlayerConfig config) throws SQLDataBaseException
	{		
		Player.insertOrUpdate(commonDB, player);
		// TODO: Symbol & Portrait
		PlayerConfig.insertOrUpdate(commonDB, config);		
	}
	
	void insertArea(Location location, boolean isSun) throws SQLDataBaseException
	{
		Area.insertOrUpdate(commonDB, new Area(location, isSun));
	}

	boolean areaExists(Location location) throws SQLDataBaseException
	{
		return Area.exist(commonDB, IArea.class, null, "location_x = %d AND location_y = %d AND location_z = %d", location.x, location.y, location.z);
	}
	
	boolean areaHasCelestialBody(Location location) throws SQLDataBaseException
	{
		if (ProductiveCelestialBody.existUnversioned(commonDB, IProductiveCelestialBody.class, null, "location_x = %d AND location_y = %d AND location_z = %d", location.x, location.y, location.z)) return true;
		return Vortex.exist(commonDB, IVortex.class, null, "location_x = %d AND location_y = %d AND location_z = %d", location.x, location.y, location.z);
	}
	
	boolean areaIsSun(Location location) throws SQLDataBaseException
	{
		Set<IArea> areas = Area.select(commonDB, IArea.class, null, "location_x = %d AND location_y = %d AND location_z = %d", location.x, location.y, location.z);
		return (!areas.isEmpty() && areas.iterator().next().getIsSun());
	}
	
	boolean isTravellingTheSun(RealLocation a, RealLocation b) throws SQLDataBaseException
	{
		// TODO: Optimize with a SQL request using "... IN ( ... )" as where clause.
		for(RealLocation pathStep : SEPUtils.getAllPathLoc(a, b))
		{
			if (areaExists(pathStep.asLocation()) && areaIsSun(pathStep.asLocation())) return true;
		}
		
		return false;
	}
	
	void insertStartingPlanet(String planetName, Location planetLocation, String ownerName) throws SQLDataBaseException
	{			
		// Fix carbon amount to the mean value.
		int[] carbonAmount = getConfig().getCelestialBodiesStartingCarbonAmount(eCelestialBodyType.Planet);
		int carbonStock = (carbonAmount[1] - carbonAmount[0]) / 2 + carbonAmount[0];

		// Fix slots amount to the mean value.
		int[] slotsAmount = getConfig().getCelestialBodiesSlotsAmount(eCelestialBodyType.Planet);
		int slots = (slotsAmount[1] - slotsAmount[0]) / 2 + slotsAmount[0];
		if (slots <= 0) slots = 1;

		int[] populationPerTurnRange = getConfig().getPopulationPerTurn();
		int populationPerTurn = (populationPerTurnRange[1] - populationPerTurnRange[0])/2 + populationPerTurnRange[0];
		
		int[] populationLimitRange = getConfig().getPopulationLimit();
		int populationLimit = (populationLimitRange[1] - populationLimitRange[0])/2 + populationLimitRange[0];

		int currentCarbon = getConfig().getPlayersPlanetsStartingCarbonResources();
		int currentPopulation = getConfig().getPlayersPlanetsStartingPopulation();
		
		IVersionedPlanet planet = new VersionedPlanet(planetName, eCelestialBodyType.Planet, planetLocation, carbonStock, slots, getConfig().getTurn(), ownerName, carbonStock, currentCarbon, populationPerTurn, populationLimit, currentPopulation);		
		IGovernmentModule governmentModule;
		IGovernment government;
		
		// If victory rule "Regimicide" is on, starting planet has a pre-built government module.	    
	    if (getConfig().isRegimicide())
		{
	    	// Buildin, GovernmentModule, Government
	    	governmentModule = new GovernmentModule(eBuildingType.GovernmentModule, planet.getName(), getConfig().getTurn(), 1);
	    	government = new Government(ownerName, getConfig().getTurn(), null, null, planetName, getConfig().getTurn());
		}
	    else
	    {
	    	governmentModule = null;
	    	government = null;
	    }
	    
	    ProductiveCelestialBody.insertOrUpdate(commonDB, planet);
		if (governmentModule != null) Building.insertOrUpdate(commonDB, governmentModule);
		if (government != null) Government.insertOrUpdate(commonDB, government);
	}
	
	/// Tests
	
	ISQLDataBase getDB()
	{
		return db;
	}
	
	/// Serialization
	
	private void writeObject(java.io.ObjectOutputStream out) throws IOException
	{
		out.defaultWriteObject();
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
		
		this.db = this.commonDB.getDB();
	}
	
	private void readObjectNoData() throws ObjectStreamException
	{

	}

	/*
	Map<Unit, Double> movingUnitsSpeeds = new HashMap<Unit, Double>();
	double maxSpeed = Double.MIN_VALUE;

	for(Unit unit: db.getUnits())
	{
		if (unit.isMoving() || unit.startMove())
		{
			SpaceRoad spaceRoad = db.getSpaceRoad(unit.getSourceLocation(), unit.getDestinationLocation());
			double unitSpeed = (spaceRoad == null ? unit.getSpeed() : Math.max(spaceRoad.getSpeed(), unit.getSpeed()));
			movingUnitsSpeeds.put(unit, unitSpeed);
			maxSpeed = Math.max(maxSpeed, unitSpeed);
		}
	}

	// Unit moves

	Set<Unit> currentStepMovedUnits = new HashSet<Unit>();
	Set<Probe> deployedProbes = db.getDeployedProbes();

	double step = 1 / maxSpeed;
	for(float currentStep = 0; currentStep < 1; currentStep += step)
	{
		currentStepMovedUnits.clear();

		for(Entry<Unit, Double> e: movingUnitsSpeeds.entrySet())
		{
			Unit u = e.getKey();
			double speed = e.getValue();

			double distance = SEPUtils.getDistance(u.getSourceLocation(), u.getDestinationLocation());
			double progressInOneTurn = (distance != 0 ? (speed / distance) : 100);
			u.setTravellingProgress(Math.min(1, u.getTravellingProgress() + progressInOneTurn * step));
			RealLocation currentStepLocation = u.getRealLocation();

			for(Unit movedUnit: currentStepMovedUnits)
			{
				RealLocation movedUnitCurrentLocation = movedUnit.getRealLocation();

				if (movedUnit.getOwnerName().equals(u.getOwnerName()))
					continue;
				if (SEPUtils.getDistance(currentStepLocation, movedUnitCurrentLocation) <= 1)
				{
					UnitSeenLogEntry log = new UnitSeenLogEntry(db.getDate(), currentStep, movedUnitCurrentLocation, movedUnit.getPlayerView(db.getDate(), u.getOwnerName(), true));
					u.addTravellingLogEntry(log);
					log = new UnitSeenLogEntry(db.getDate(), currentStep, currentStepLocation, u.getPlayerView(db.getDate(), movedUnit.getOwnerName(), true));
					movedUnit.addTravellingLogEntry(log);
				}
			}

			for(Probe probe: deployedProbes)
			{
				if (probe.getOwnerName().equals(u.getOwnerName()))
					continue;
				RealLocation probeLocation = probe.getRealLocation();
				distance = SEPUtils.getDistance(probeLocation, currentStepLocation);

				if (distance <= db.getGameConfig().getProbeScope())
				{
					UnitSeenLogEntry log = new UnitSeenLogEntry(db.getDate(), currentStep, currentStepLocation, u.getPlayerView(db.getDate(), probe.getOwnerName(), true));
					db.writeLog(probe.getOwnerName(), log);
				}

				if (distance <= 1)
				{
					UnitSeenLogEntry log = new UnitSeenLogEntry(db.getDate(), currentStep, probeLocation, probe.getPlayerView(db.getDate(), u.getOwnerName(), true));
					u.addTravellingLogEntry(log);
				}
			}

			currentStepMovedUnits.add(u);
		}
	}

	Set<AntiProbeMissile> explodingAntiProbeMissiles = new HashSet<AntiProbeMissile>();

	Map<Unit, ProductiveCelestialBody> justFinishedToMove = new HashMap<Unit, ProductiveCelestialBody>();

	for(Entry<Unit, Double> e: movingUnitsSpeeds.entrySet())
	{
		Unit u = e.getKey();

		RealLocation endTurnLocation = u.getRealLocation();

		IMarker.Key key = new IMarker.Key("own unit(" + u.getName() + ") travelling marker", UnitMarker.class, u.getOwnerName());

		db.removeMarker(u.getOwnerName(), key);

		if (endTurnLocation.asLocation().equals(u.getDestinationLocation().asLocation()))
		{
			u.endMove();

			ProductiveCelestialBody productiveCelestialBody = db.getCelestialBody(endTurnLocation.asLocation(), ProductiveCelestialBody.class);

			if (productiveCelestialBody != null)
			{
				productiveCelestialBody.controlNewcomer(u);
			}

			if (AntiProbeMissile.class.isInstance(u))
			{
				explodingAntiProbeMissiles.add(AntiProbeMissile.class.cast(u));
			}

			justFinishedToMove.put(u, productiveCelestialBody);
		}
		else
		{
			db.insertMarker(u.getOwnerName(), new UnitMarker(db.getDate(), key, u.getPlayerView(db.getDate(), u.getOwnerName(), true)));
		}
	}

	// Explode anti-probe missiles.
	for(AntiProbeMissile apm: explodingAntiProbeMissiles)
	{
		Probe targetProbe = db.getUnit(Probe.class, apm.getTargetOwnerName(), apm.getTargetName());
		if (targetProbe != null && SEPUtils.getDistance(apm.getRealLocation(), targetProbe.getRealLocation()) <= 1)
		{
			// TODO: New event for targetProbe owner (probe destroyed)				
			db.removeUnit(targetProbe.getKey());
		}

		// TODO: New event for apm owner, destroy probe marker.
		db.removeUnit(apm.getKey());
	}

	//////////////////////////////////					

	// Carbon & Population generation
	for(ProductiveCelestialBody productiveCelestialBody: db.getCelestialBodies(ProductiveCelestialBody.class))
	{
		GovernmentModule governmentModule = productiveCelestialBody.getBuilding(GovernmentModule.class);

		int generatedCarbon = 0;

		if (productiveCelestialBody.getCarbonStock() > 0)
		{
			ExtractionModule extractionModule = productiveCelestialBody.getBuilding(ExtractionModule.class);
			if (extractionModule != null && extractionModule.getCarbonProductionPerTurn() > 0)
			{
				generatedCarbon = extractionModule.getCarbonProductionPerTurn();
			}
			else
			{
				generatedCarbon = db.getGameConfig().getNaturalCarbonPerTurn();
			}

			if (governmentModule != null)
			{
				generatedCarbon = (int) (generatedCarbon * 1.5);
			}

			if (extractionModule == null || extractionModule.getCarbonProductionPerTurn() <= 0)
			{
				generatedCarbon = Math.min(Math.max(0, db.getGameConfig().getMaxNaturalCarbon() - productiveCelestialBody.getCarbon()), generatedCarbon);
			}
		}

		productiveCelestialBody.setCarbon(productiveCelestialBody.getCarbon() + generatedCarbon);
		productiveCelestialBody.decreaseCarbonStock(generatedCarbon);

		if (Planet.class.isInstance(productiveCelestialBody))
		{
			Planet planet = Planet.class.cast(productiveCelestialBody);

			int generatedPopulation = planet.getPopulationPerTurn();

			if (governmentModule != null)
			{
				generatedPopulation = (int) (generatedPopulation * 1.5);
			}

			generatedPopulation = Math.min(planet.getPopulationLimit() - planet.getPopulation(), generatedPopulation);

			planet.setPopulation(planet.getPopulation() + generatedPopulation);
		}
	}

	Set<ProductiveCelestialBody> productiveCelestialBodies = db.getCelestialBodies(ProductiveCelestialBody.class);

	// Travelling Logs for pacifist landing.
	for(Unit u: justFinishedToMove.keySet())
	{
		if (justFinishedToMove.get(u) == null || justFinishedToMove.get(u).getConflictInitiators().isEmpty())
		{
			for(ALogEntry log: u.getTravellingLogs())
			{
				db.writeLog(u.getOwnerName(), log);
			}
		}
	}

	// Conflicts
	for(ProductiveCelestialBody productiveCelestialBody: productiveCelestialBodies)
	{
		if (!productiveCelestialBody.getConflictInitiators().isEmpty())
		{
			resolveConflict(productiveCelestialBody, 1);
		}
	}

	// Carbon freight
	for(ProductiveCelestialBody productiveCelstialBody: productiveCelestialBodies)
	{
		SpaceCounter spaceCounter = productiveCelstialBody.getBuilding(SpaceCounter.class);
		if (spaceCounter == null)
			continue;

		spaceCounter.prepareCarbonDelivery(db, productiveCelstialBody);
	}
	
	*/

	/*
	public void resolveConflict(ProductiveCelestialBody productiveCelestialBody, int round)
	{
		// Initiate conflicts table : PlayerName/PlayerName: boolean
		Map<String, Map<String, Boolean>> conflictDiplomacy = resolveConflictDiplomacy(productiveCelestialBody);

		// List merged (unassigned fleets + fleets) forces for each players
		// Backup and remove original fleets (including unassigned ones). 
		Map<String, Fleet> mergedFleets = new Hashtable<String, Fleet>();
		Map<String, Set<Fleet>> originalPlayersFleets = new Hashtable<String, Set<Fleet>>();

		Location location = productiveCelestialBody.getLocation();

		for(String p: db.getPlayersKeySet())
		{
			if (productiveCelestialBody.getUnasignedFleet(p) != null)
			{
				Fleet unasignedFleet = productiveCelestialBody.getUnasignedFleet(p);
				mergedFleets.put(p, new Fleet(db, p + " forces in conflict on " + productiveCelestialBody.getName() + " at turn " + db.getDate(), p, location.asRealLocation(), unasignedFleet.getStarships(), unasignedFleet.getSpecialUnits(), false, null, null));
				productiveCelestialBody.removeFromUnasignedFleet(p, unasignedFleet.getStarships(), unasignedFleet.getSpecialUnits());
			}
		}

		Set<Fleet> fleetsToRemove = new HashSet<Fleet>();
		for(Fleet f: db.getUnits(location, Fleet.class))
		{
			if (!mergedFleets.containsKey(f.getOwnerName()))
			{
				mergedFleets.put(f.getOwnerName(), new Fleet(db, f.getOwnerName() + " forces in conflict on " + productiveCelestialBody.getName() + " at turn " + db.getDate(), f.getOwnerName(), location.asRealLocation(), f.getStarships(), f.getSpecialUnits(), false, null, null));
			}
			else
			{
				mergedFleets.get(f.getOwnerName()).merge(f.getStarships(), f.getSpecialUnits());
			}

			fleetsToRemove.add(f);
		}

		for(Fleet f: fleetsToRemove)
		{
			if (!originalPlayersFleets.containsKey(f.getOwnerName()))
				originalPlayersFleets.put(f.getOwnerName(), new HashSet<Fleet>());
			originalPlayersFleets.get(f.getOwnerName()).add(f);
			db.removeUnit(f.getKey());
		}

		DefenseModule defenseModule = productiveCelestialBody.getBuilding(DefenseModule.class);
		if (defenseModule != null)
		{
			if (!mergedFleets.containsKey(productiveCelestialBody.getOwnerName()))
			{
				mergedFleets.put(productiveCelestialBody.getOwnerName(), new Fleet(db, productiveCelestialBody.getOwnerName() + " forces in conflict on " + productiveCelestialBody.getName() + " at turn " + db.getDate(), productiveCelestialBody.getOwnerName(), location.asRealLocation(), null, null, false, null, null));
			}

			Set<ISpecialUnit> specialUnits = new HashSet<ISpecialUnit>();
			specialUnits.add(defenseModule.getSpecialUnit());
			mergedFleets.get(productiveCelestialBody.getOwnerName()).merge(null, specialUnits);
		}

		// Run battle				
		Map<String, Fleet> survivalFleets = resolveBattle(conflictDiplomacy, mergedFleets);

		for(String survivor: survivalFleets.keySet())
		{
			Map<String, org.axan.sep.common.Fleet> startingForces = new HashMap<String, org.axan.sep.common.Fleet>();
			for(String player: mergedFleets.keySet())
			{
				startingForces.put(player, mergedFleets.get(player).getPlayerView(db.getDate(), survivor, true));
			}

			Map<String, org.axan.sep.common.Fleet> survivingForces = new HashMap<String, org.axan.sep.common.Fleet>();
			for(String player: survivalFleets.keySet())
			{
				survivalFleets.get(player).rest();
				survivingForces.put(player, survivalFleets.get(player).getPlayerView(db.getDate(), survivor, true));
			}

			ConflictLogEntry conflictLog = new ConflictLogEntry(db.getDate(), (float) .999, round, productiveCelestialBody.getName(), startingForces, conflictDiplomacy, survivingForces);

			db.writeLog(survivor, conflictLog);
		}

		// Restore original fleets from survived ones.
		for(Map.Entry<String, Fleet> e: survivalFleets.entrySet())
		{
			String playerName = e.getKey();
			Fleet survivalFleet = e.getValue();

			Set<Fleet> originalFleets = originalPlayersFleets.get(playerName);
			Vector<Fleet> originalFleetsCopy = new Vector<Fleet>();
			Map<String, Map<StarshipTemplate, Integer>> resultantFleetsStarships = new HashMap<String, Map<StarshipTemplate, Integer>>();
			Map<String, Set<ISpecialUnit>> resultantFleetsSpecialUnits = new HashMap<String, Set<ISpecialUnit>>();
			Map<StarshipTemplate, Integer> resultantUnasignedStarships = new HashMap<StarshipTemplate, Integer>();
			Set<ISpecialUnit> resultantUnasignedSpecialUnits = new HashSet<ISpecialUnit>();

			for(ISpecialUnit specialUnit: survivalFleet.getSpecialUnits())
			{
				boolean found = false;
				for(Fleet originalFleet: originalFleets)
				{
					if (originalFleet.getSpecialUnits().contains(specialUnit))
					{
						if (!resultantFleetsSpecialUnits.containsKey(originalFleet.getName()))
							resultantFleetsSpecialUnits.put(originalFleet.getName(), new HashSet<ISpecialUnit>());
						resultantFleetsSpecialUnits.get(originalFleet.getName()).add(specialUnit);
						found = true;
						break;
					}
				}

				if (!found)
				{
					resultantUnasignedSpecialUnits.add(specialUnit);
				}
			}

			for(StarshipTemplate starshipTemplate: survivalFleet.getStarships().keySet())
			{
				int totalNb = survivalFleet.getStarships().get(starshipTemplate);

				while (totalNb > 0)
				{
					originalFleetsCopy.clear();
					originalFleetsCopy.addAll(originalFleets);

					int alreadyRecovered = 0;
					int maxToRecover = 0;

					Fleet originalFleet = null;
					do
					{
						if (originalFleetsCopy.isEmpty())
						{
							originalFleet = null;
							break;
						}

						int i = rnd.nextInt(originalFleetsCopy.size());
						originalFleet = originalFleetsCopy.remove(i);

						if (!resultantFleetsStarships.containsKey(originalFleet.getName()))
							resultantFleetsStarships.put(originalFleet.getName(), new HashMap<StarshipTemplate, Integer>());
						if (!resultantFleetsStarships.get(originalFleet.getName()).containsKey(starshipTemplate))
							resultantFleetsStarships.get(originalFleet.getName()).put(starshipTemplate, 0);

						alreadyRecovered = resultantFleetsStarships.get(originalFleet.getName()).get(starshipTemplate);
						maxToRecover = originalFleet.getStarships().containsKey(starshipTemplate) ? originalFleet.getStarships().get(starshipTemplate) : 0;

					} while (maxToRecover <= alreadyRecovered);

					if (originalFleet == null)
					{
						if (!resultantUnasignedStarships.containsKey(starshipTemplate))
							resultantUnasignedStarships.put(starshipTemplate, 0);
						resultantUnasignedStarships.put(starshipTemplate, resultantUnasignedStarships.get(starshipTemplate) + totalNb);
						totalNb = 0;
					}
					else
					{
						int maxNb = Math.min(maxToRecover - alreadyRecovered, totalNb);
						int recoveredNb = rnd.nextInt(maxNb) + 1;
						totalNb -= recoveredNb;

						resultantFleetsStarships.get(originalFleet.getName()).put(starshipTemplate, resultantFleetsStarships.get(originalFleet.getName()).get(starshipTemplate) + recoveredNb);
					}
				}
			}

			for(Fleet originalFleet: originalFleets)
			{
				Fleet recoveredFleet = null;
				if (resultantFleetsStarships.get(originalFleet.getName()) != null)
				{
					recoveredFleet = new Fleet(db, originalFleet.getKey(), originalFleet.getSourceLocation(), resultantFleetsStarships.get(originalFleet.getName()), resultantFleetsSpecialUnits.get(originalFleet.getName()), false, originalFleet.getCheckpoints(), originalFleet.getTravellingLogs());
					if (!recoveredFleet.hasNoMoreStarships())
						db.insertUnit(recoveredFleet);
				}

				if (resultantFleetsSpecialUnits.get(originalFleet.getName()) != null && (recoveredFleet == null || recoveredFleet.hasNoMoreStarships()))
				{
					resultantUnasignedSpecialUnits.addAll(resultantFleetsSpecialUnits.get(originalFleet.getName()));
					productiveCelestialBody.mergeToUnasignedFleet(playerName, null, resultantFleetsSpecialUnits.get(originalFleet.getName()));
				}
			}

			productiveCelestialBody.mergeToUnasignedFleet(playerName, resultantUnasignedStarships, resultantUnasignedSpecialUnits);
		}

		// End conflict, enventually change the celestial body owner, and check for new conflict.
		productiveCelestialBody.endConflict();

		if (productiveCelestialBody.getOwnerName() == null || !survivalFleets.keySet().contains(productiveCelestialBody.getOwnerName()))
		{
			productiveCelestialBody.demolishBuilding(DefenseModule.class);

			Random rnd = new Random();
			String newOwner = survivalFleets.keySet().toArray(new String[survivalFleets.keySet().size()])[rnd.nextInt(survivalFleets.keySet().size())];
			productiveCelestialBody.changeOwner(newOwner);

			productiveCelestialBody.addConflictInititor(newOwner);
			resolveConflict(productiveCelestialBody, ++round);
		}
	}
	*/

	/*
	public static Map<String, Fleet> resolveBattle(Map<String, Map<String, Boolean>> conflictDiplomacy, Map<String, Fleet> forces)
	{
		Map<String, Fleet> survivors = new Hashtable<String, Fleet>(forces);

		boolean isFinished;
		do
		{
			// Look if there's still some alive player wanting to fight with another alive one.
			isFinished = true;
			for(String p: survivors.keySet())
			{
				for(String t: survivors.keySet())
				{
					if (conflictDiplomacy.containsKey(p) && conflictDiplomacy.get(p).containsKey(t) && conflictDiplomacy.get(p).get(t))
						isFinished = false;

					if (!isFinished)
						break;
				}

				if (!isFinished)
					break;
			}

			// If yes, play the battle round to next victim.
			if (!isFinished)
			{
				boolean thereIsVictims = false;

				do
				{
					Iterator<Entry<String, Fleet>> it = survivors.entrySet().iterator();
					while (it.hasNext())
					{
						Entry<String, Fleet> e = it.next();
						if (e.getValue().isDestroyed())
						{
							thereIsVictims = true;
							it.remove();
						}
					}

					if (thereIsVictims)
						continue;

					Map<String, Map<eStarshipSpecializationClass, Double>> attackPromises = new HashMap<String, Map<eStarshipSpecializationClass, Double>>();
					Map<String, Fleet> mergedEnnemyFleets = new HashMap<String, Fleet>();

					for(String attacker: survivors.keySet())
					{
						Fleet attackerFleet = survivors.get(attacker);

						Fleet mergedEnnemyFleet = Fleet.computeMergedAttackers(attacker, conflictDiplomacy, survivors);
						mergedEnnemyFleets.put(attacker, mergedEnnemyFleet);

						for(eStarshipSpecializationClass specialization: eStarshipSpecializationClass.values())
						{
							Map<String, Boolean> ennemies = new HashMap<String, Boolean>(conflictDiplomacy.get(attacker));

							SpecializedEquivalentFleet attackerSubFleet = attackerFleet.getSpecializedFleet(specialization);
							if (attackerSubFleet == null)
								continue;

							SpecializedEquivalentFleet targetSubFleet = mergedEnnemyFleet.getNextTarget(specialization);
							double attack = attackerFleet.getBattleSkillsModifier().getFixedAttackBonus() + (attackerSubFleet.getAttack() * (1 + (specialization.getTdT() == targetSubFleet.getSpecialization() ? attackerSubFleet.getAttackSpecializationBonus() : (specialization.getBN() == targetSubFleet.getSpecialization() ? -1 * targetSubFleet.getDefenseSpecializationBonus() : 0))));

							while ((attack > 0) && (!ennemies.isEmpty())) // We would not lose remaining attack, because in the worst case, the current round stops with the last specialized sub fleet death. 
							{
								int i = rnd.nextInt(ennemies.size());
								String ennemy = ennemies.keySet().toArray(new String[ennemies.size()])[i];

								if (!ennemies.get(ennemy) || !survivors.containsKey(ennemy))
								{
									ennemies.remove(ennemy);
									continue;
								}
								else
								{
									SpecializedEquivalentFleet ennemySubFleet = survivors.get(ennemy).getSpecializedFleet(targetSubFleet.getSpecialization());
									if (ennemySubFleet == null || ennemySubFleet.getDefense() == 0)
									{
										ennemies.remove(ennemy);
										continue;
									}

									double r = (ennemies.size() > 1) ? rnd.nextDouble() : 1.0;
									double hit = r * attack;

									if (!attackPromises.containsKey(ennemy))
										attackPromises.put(ennemy, new HashMap<eStarshipSpecializationClass, Double>());
									if (!attackPromises.get(ennemy).containsKey(targetSubFleet.getSpecialization()))
										attackPromises.get(ennemy).put(targetSubFleet.getSpecialization(), 0.0);
									attackPromises.get(ennemy).put(targetSubFleet.getSpecialization(), attackPromises.get(ennemy).get(targetSubFleet.getSpecialization()) + hit);
									attack -= hit;
								}
							}
						}
					}

					double bestTime = Double.POSITIVE_INFINITY;

					for(Map.Entry<String, Map<eStarshipSpecializationClass, Double>> e: attackPromises.entrySet())
					{
						String target = e.getKey();
						for(Map.Entry<eStarshipSpecializationClass, Double> f: e.getValue().entrySet())
						{
							eStarshipSpecializationClass specialization = f.getKey();
							double attack = f.getValue();

							bestTime = Math.min(bestTime, ((double) survivors.get(target).getSpecializedFleet(specialization).getDefense()) / attack);
						}
					}

					for(Map.Entry<String, Map<eStarshipSpecializationClass, Double>> e: attackPromises.entrySet())
					{
						String target = e.getKey();

						for(Map.Entry<eStarshipSpecializationClass, Double> f: e.getValue().entrySet())
						{
							eStarshipSpecializationClass specialization = f.getKey();
							double attack = f.getValue();
							double dmg = bestTime * attack;

							survivors.get(target).getSpecializedFleet(specialization).takeDamage(dmg);
						}
					}

				} while (!thereIsVictims);

				/*
				DO
				
					Map[Target, eStarshipSpecializationClass, Attack] attackPromises;
					Map[Target, Fleet] mergedAttackers; 										
					
					FOREACH belligerent
						Fleet defender <- forces(belligerent)
						mergedAttackers[belligerent] <- computeMergeAttackers(belligerent, conflictDiplomacy, forces)						
						FOREACH starshipClass							
							eStarshipSpecializationClass targetClass <- defenser.getNextTargetClass(starshipClass);
							attackPromises[defender, targetClass, mergedAttackers[belligerent].Attack * (1 + starshipClass.isTdT(targetClass) ? mergedAttackers[belligerent].Weapon : starshipClass.isBN(targetClass) ? -defenser.getNextTargetArmor(starshipClass) : 0)];							
						NEXT
					NEXT
					
					double bestTime <- +INF;
					
					FOREACH attackPromise IN attackPromises
						bestTime <- Min(bestTime, attackPromise.target.Defense / attackPromise.Attack);
					NEXT
					
					FOREACH attackPromise IN attackPromises
						attackPromise.target.takeDamage(bestTime * attackPromise.Attack);
					NEXT
				
				WHILE(!attackPromise.target.isEmpty())					
				 *//*
			}
		} while (!isFinished);

		return survivors;
	}
	*/

	/*
	private Map<String, Map<String, Boolean>> resolveConflictDiplomacy(ProductiveCelestialBody productiveCelestialBody)
	{
		Stack<String> initiators = productiveCelestialBody.getConflictInitiators();
		Set<String> playersKeySet = db.getPlayersKeySet();
		String celestialBodyOwnerName = productiveCelestialBody.getOwnerName();

		Map<String, Map<String, Boolean>> conflicts = new Hashtable<String, Map<String, Boolean>>();
		Stack<String> seenInitiators = new Stack<String>();

		while (!initiators.isEmpty())
		{
			boolean fought = false;
			String initiator = initiators.pop();
			if (seenInitiators.contains(initiator))
				continue;

			for(String target: playersKeySet)
			{
				if (target.equals(initiator))
					continue;

				boolean initiatorPolicy = false;

				if (initiator.equals(celestialBodyOwnerName))
				{
					initiatorPolicy = !db.getPlayerPolicies(initiator).getPolicies(target).isAllowedToLandFleetInHomeTerritory();
				}
				else
				{
					eForeignPolicy fp = db.getPlayerPolicies(initiator).getPolicies(target).getForeignPolicy();
					initiatorPolicy = (fp == eForeignPolicy.HOSTILE || fp == eForeignPolicy.HOSTILE_IF_OWNER && target.equals(celestialBodyOwnerName));
				}

				boolean resultPolicy = conflicts.containsKey(target) && conflicts.get(target).containsKey(initiator) ? conflicts.get(target).get(initiator) || initiatorPolicy : initiatorPolicy;

				if (!conflicts.containsKey(initiator))
					conflicts.put(initiator, new Hashtable<String, Boolean>());
				conflicts.get(initiator).put(target, resultPolicy);

				if (resultPolicy)
				{
					fought = true;
					initiators.push(target);
				}
			}

			if (fought)
			{
				for(String target: playersKeySet)
				{
					if (target.equals(initiator))
						continue;

					boolean targetPolicy = false;

					if (target.equals(celestialBodyOwnerName))
					{
						targetPolicy = !db.getPlayerPolicies(target).getPolicies(initiator).isAllowedToLandFleetInHomeTerritory();
					}
					else
					{
						eForeignPolicy fp = db.getPlayerPolicies(target).getPolicies(initiator).getForeignPolicy();
						targetPolicy = (fp == eForeignPolicy.HOSTILE || fp == eForeignPolicy.HOSTILE_IF_OWNER && initiator.equals(celestialBodyOwnerName));
					}

					boolean resultPolicy = conflicts.get(initiator).get(target) || targetPolicy;
					if (!conflicts.containsKey(target))
						conflicts.put(target, new Hashtable<String, Boolean>());
					conflicts.get(target).put(initiator, resultPolicy);
					conflicts.get(initiator).put(target, resultPolicy);

					if (resultPolicy)
					{
						initiators.push(target);
					}
				}
			}

			seenInitiators.push(initiator);
		}

		return conflicts;
	}
	*/

	////////////

	// TODO: Replace with Map<Class<? extends IClientCommand>, ServerCommandProcessor> commandProcessors = db.getGameCommandProcessors();
	
	/*
	public void demolish(String playerLogin, String celestialBodyName, Class<? extends org.axan.sep.common.ABuilding> buildingType) throws RunningGameCommandException
	{
		DemolishCheckResult demolishCheckResult = checkDemolish(playerLogin, celestialBodyName, buildingType);
		demolishCheckResult.productiveCelestialBody.downgradeBuilding(demolishCheckResult.existingBuilding);
	}

	public CommandCheckResult canDemolish(String playerLogin, DemolishParams p)
	{
		try
		{
			checkDemolish(playerLogin, p.celestialBodyName, p.buildingType);
		}
		catch(Throwable t)
		{
			return new CommandCheckResult(t);
		}
		return new CommandCheckResult();
	}

	private static class DemolishCheckResult
	{
		final ProductiveCelestialBody productiveCelestialBody;
		final ABuilding existingBuilding;

		public DemolishCheckResult(ProductiveCelestialBody productiveCelestialBody, ABuilding building)
		{
			this.productiveCelestialBody = productiveCelestialBody;
			this.existingBuilding = building;
		}
	}

	public DemolishCheckResult checkDemolish(String playerName, String celestialBodyName, Class<? extends org.axan.sep.common.ABuilding> buildingType) throws RunningGameCommandException
	{
		ProductiveCelestialBody productiveCelestialBody = db.getCelestialBody(celestialBodyName, ProductiveCelestialBody.class, playerName);
		if (productiveCelestialBody == null)
			throw new RunningGameCommandException("Celestial body '" + celestialBodyName + "' does not exist, is not a productive one, or is not owned by '" + playerName + "'.");

		ABuilding building = productiveCelestialBody.getBuildingFromClientType(buildingType);

		// If no building of this type exist.
		if (building == null || building.getBuildSlotsCount() == 0)
			throw new RunningGameCommandException("No building type '" + buildingType.getSimpleName() + "' built yet.");

		if (!building.canDowngrade())
			throw new RunningGameCommandException("Cannot demolish building type '" + buildingType.getSimpleName() + "'");

		return new DemolishCheckResult(productiveCelestialBody, building);
	}

	public void embarkGovernment(String playerName) throws RunningGameCommandException
	{
		EmbarkGovernmentCheckResult embarkGovernmentCheckResult = checkEmbarkGovernment(playerName);
		embarkGovernmentCheckResult.planet.removeBuilding(GovernmentModule.class);
		Set<ISpecialUnit> specialUnitsToMake = new HashSet<ISpecialUnit>();
		specialUnitsToMake.add(new GovernmentStarship(playerName + " government starship"));
		embarkGovernmentCheckResult.planet.mergeToUnasignedFleet(playerName, null, specialUnitsToMake);
		embarkGovernmentCheckResult.planet.setCarbon(embarkGovernmentCheckResult.planet.getCarbon() - embarkGovernmentCheckResult.carbonCost);
		embarkGovernmentCheckResult.planet.setPopulation(embarkGovernmentCheckResult.planet.getPopulation() - embarkGovernmentCheckResult.populationCost);
	}

	public CommandCheckResult canEmbarkGovernment(String playerLogin, EmbarkGovernmentParams p)
	{
		EmbarkGovernmentCheckResult check;
		try
		{
			check = checkEmbarkGovernment(playerLogin);
		}
		catch(Throwable t)
		{
			return new CommandCheckResult(t);
		}
		return new CommandCheckResult(check.carbonCost, check.populationCost);
	}

	private static class EmbarkGovernmentCheckResult
	{
		final Planet planet;
		final int carbonCost;
		final int populationCost;

		public EmbarkGovernmentCheckResult(Planet planet, int carbonCost, int populationCost)
		{
			this.planet = planet;
			this.carbonCost = carbonCost;
			this.populationCost = populationCost;
		}
	}

	public EmbarkGovernmentCheckResult checkEmbarkGovernment(String playerLogin) throws RunningGameCommandException
	{
		Planet planet = db.locateGovernmentModule(playerLogin);

		// If player has no government module.
		if (planet == null)
			throw new RunningGameCommandException("Cannot locate '" + playerLogin + "' government module.");

		GovernmentModule governmentModule = planet.getBuilding(GovernmentModule.class);

		if (governmentModule == null)
			throw new RunningGameCommandException("No government module on the planet '" + planet.getName() + "' (unexpected error)");

		StarshipPlant starshipPlant = planet.getBuilding(StarshipPlant.class);

		// If planet has no starshipPlant.
		if (starshipPlant == null)
			throw new RunningGameCommandException("No starship plant on planet '" + planet.getName() + "'");

		// If starship has just been build this turn.
		if (starshipPlant.getLastBuildDate() >= db.getDate())
			throw new RunningGameCommandException("Starship plant is still in construction.");

		int carbonCost = db.getGameConfig().getGovernmentStarshipCarbonPrice();
		int populationCost = db.getGameConfig().getGovernmentStarshipPopulationPrice();

		if (carbonCost > planet.getCarbon())
			throw new RunningGameCommandException("Not enough carbon.");

		if (populationCost > planet.getPopulation())
			throw new RunningGameCommandException("Not enough population.");

		return new EmbarkGovernmentCheckResult(planet, carbonCost, populationCost);
	}

	public CommandCheckResult canFirePulsarMissile(String playerName, String celestialBodyName)
	{
		ProductiveCelestialBody productiveCelestialBody = db.getCelestialBody(celestialBodyName, ProductiveCelestialBody.class, playerName);
		if (productiveCelestialBody == null)
			return new CommandCheckResult(new IllegalArgumentException("Celestial body '" + celestialBodyName + "' does not exist, or is not a productive one."));

		ABuilding building = productiveCelestialBody.getBuildingFromClientType(org.axan.sep.common.PulsarLauchingPad.class);

		// If no building of this type exist.
		if (building == null)
			return new CommandCheckResult("No pulsar launcher found.");

		// Building type check		
		if (!PulsarLauchingPad.class.isInstance(building))
			return new CommandCheckResult("No pulsar launcher found.");

		PulsarLauchingPad pulsarLaunchingPad = PulsarLauchingPad.class.cast(building);
		if (pulsarLaunchingPad.getUnusedCount() <= 0)
			return new CommandCheckResult("No available pulsar launcher found.");

		// TODO
		return new CommandCheckResult();
	}

	public void settleGovernment(String playerLogin, String planetName) throws RunningGameCommandException
	{
		SettleGovernmentCheckResult settleGovernmentCheckResult = checkSettleGovernment(playerLogin, planetName);
		settleGovernmentCheckResult.governmentalFleet.removeGovernment();
		try
		{
			settleGovernmentCheckResult.planet.updateBuilding(settleGovernmentCheckResult.governmentModule);
		}
		catch(CelestialBodyBuildException e)
		{
			throw new SEPServer.SEPImplementationException("Unexpected exception", e);
		}
	}

	public CommandCheckResult canSettleGovernment(String playerLogin, SettleGovernmentParams p)
	{
		try
		{
			checkSettleGovernment(playerLogin, p.planetName);
		}
		catch(Throwable t)
		{
			return new CommandCheckResult(t);
		}
		return new CommandCheckResult();
	}

	private static class SettleGovernmentCheckResult
	{
		final Planet planet;
		final Fleet governmentalFleet;
		final GovernmentModule governmentModule;

		public SettleGovernmentCheckResult(Planet planet, Fleet governmentalFleet, GovernmentModule governmentModule)
		{
			this.planet = planet;
			this.governmentalFleet = governmentalFleet;
			this.governmentModule = governmentModule;
		}
	}

	public SettleGovernmentCheckResult checkSettleGovernment(String playerName, String planetName) throws RunningGameCommandException
	{
		Planet planet = db.getCelestialBody(planetName, Planet.class, playerName);

		// Check if government fleet is on the planet.
		Fleet governmentalFleet = null;

		for(Fleet f: db.getUnits(planet.getLocation(), Fleet.class, playerName))
		{
			if (f.isGovernmentFleet())
			{
				governmentalFleet = f;
				break;
			}
		}

		if (governmentalFleet == null)
			throw new RunningGameCommandException("'" + playerName + "' government cannot be found on planet '" + planetName + "'");

		if (planet.getFreeSlotsCount() <= 0)
			throw new RunningGameCommandException("No free slot available on '" + planet.getName() + "'");

		GovernmentModule governmentModule = new GovernmentModule(db.getDate());
		return new SettleGovernmentCheckResult(planet, governmentalFleet, governmentModule);
	}
*/
	
	/*
	{
		BuildCheckResult buildCheckResult = checkBuild(playerLogin, celestialBodyName, buildingType);
		buildCheckResult.productiveCelestialBody.updateBuilding(buildCheckResult.newBuilding);
		buildCheckResult.productiveCelestialBody.setCarbon(buildCheckResult.productiveCelestialBody.getCarbon() - buildCheckResult.carbonCost);

		if (buildCheckResult.populationCost > 0)
		{
			Planet planet = Planet.class.cast(buildCheckResult.productiveCelestialBody);
			planet.setPopulation(planet.getPopulation() - buildCheckResult.populationCost);
		}

		buildCheckResult.productiveCelestialBody.setLastBuildDate(db.getDate());
	}

	public CommandCheckResult canBuild(String playerLogin, BuildParams p)
	{
		BuildCheckResult check;
		try
		{
			check = checkBuild(playerLogin, p.celestialBodyName, p.buildingType);
		}
		catch(Throwable t)
		{
			return new CommandCheckResult(t);
		}
		return new CommandCheckResult(check.carbonCost, check.populationCost);
	}

	private static class BuildCheckResult
	{
		final ProductiveCelestialBody productiveCelestialBody;
		final ABuilding newBuilding;
		final int carbonCost;
		final int populationCost;

		public BuildCheckResult(ProductiveCelestialBody productiveCelestialBody, int carbonCost, int populationCost, ABuilding newBuilding)
		{
			this.productiveCelestialBody = productiveCelestialBody;
			this.carbonCost = carbonCost;
			this.populationCost = populationCost;
			this.newBuilding = newBuilding;
		}
	}

	private BuildCheckResult checkBuild(String playerName, String celestialBodyName, eBuildingType buildingType)
	{
		db.
		// celestialBodyName IS A ProductiveCelestialBody
		// celestialBody did not built anything else for the current game turn.
		// celestialBody has free slots
		// IF buildingType already exists THEN
		//	building can be upgraded
		// ENDIF
		// build/upgrade can be afforded
		
		ProductiveCelestialBody productiveCelestialBody = db.getCelestialBody(celestialBodyName, ProductiveCelestialBody.class, playerName);
		if (productiveCelestialBody == null)
			throw new CelestialBodyBuildException("Celestial body '" + celestialBodyName + "' does not exist, is not a productive one, or is not owned by '" + playerName + "'.");

		// If this productive celestial body build was already used this turn.
		if (productiveCelestialBody.getLastBuildDate() >= db.getDate())
			throw new CelestialBodyBuildException("Celestial body '" + celestialBodyName + "' already in work for this turn.");

		// If there is no more free slots.
		if (productiveCelestialBody.getFreeSlotsCount() < 1)
			throw new CelestialBodyBuildException("No more free slots on celestial body '" + celestialBodyName + "'");

		// Price check & Celestial body type / building type check
		int carbonCost = 0;
		int populationCost = 0;

		ABuilding building = productiveCelestialBody.getBuildingFromClientType(buildingType);
		ABuilding newBuilding;

		if (building != null)
		{
			if (!building.canUpgrade())
			{
				throw new CelestialBodyBuildException(building.getClass().getSimpleName() + " cannot be upgraded.");
			}

			newBuilding = building.getUpgraded(db.getDate());
			carbonCost = building.getUpgradeCarbonCost();
			populationCost = building.getUpgradePopulationCost();
		}
		else
		{
			carbonCost = org.axan.sep.common.ABuilding.getFirstCarbonCost(buildingType);
			populationCost = org.axan.sep.common.ABuilding.getFirstPopulationCost(buildingType);
			newBuilding = ABuilding.getFirstBuild(buildingType, db.getDate());
		}

		if (carbonCost > productiveCelestialBody.getCarbon())
			throw new CelestialBodyBuildException("Not enough carbon.");
		if (populationCost > 0)
		{
			if (!Planet.class.isInstance(productiveCelestialBody))
				throw new CelestialBodyBuildException("Only planet can afford population costs, '" + celestialBodyName + "' is not a planet.");
			;
			Planet planet = Planet.class.cast(productiveCelestialBody);
			if (populationCost > planet.getPopulation())
				throw new CelestialBodyBuildException("Not enough population.");
		}

		return new BuildCheckResult(productiveCelestialBody, carbonCost, populationCost, newBuilding);
	}

/*
	public CommandCheckResult canFireAntiProbeMissile(String playerLogin, FireAntiProbeMissileParams p)
	{
		try
		{
			checkFireAntiProbeMissile(playerLogin, p.antiProbeMissileName, p.targetOwnerName, p.targetProbeName);
		}
		catch(Throwable t)
		{
			return new CommandCheckResult(t);
		}
		return new CommandCheckResult();
	}

	public void fireAntiProbeMissile(String playerLogin, String antiProbeMissileName, String targetOwnerName, String targetProbeName) throws RunningGameCommandException
	{
		FireAntiProbeMissileCheckResult fireAntiProbeMissileCheckResult = checkFireAntiProbeMissile(playerLogin, antiProbeMissileName, targetOwnerName, targetProbeName);
		fireAntiProbeMissileCheckResult.antiProbeMissile.fire(targetOwnerName, targetProbeName, fireAntiProbeMissileCheckResult.source, fireAntiProbeMissileCheckResult.destination);
	}

	private static class FireAntiProbeMissileCheckResult
	{
		final AntiProbeMissile antiProbeMissile;
		final RealLocation source;
		final RealLocation destination;

		public FireAntiProbeMissileCheckResult(AntiProbeMissile antiProbeMissile, RealLocation source, RealLocation destination)
		{
			this.antiProbeMissile = antiProbeMissile;
			this.source = source;
			this.destination = destination;
		}
	}

	public FireAntiProbeMissileCheckResult checkFireAntiProbeMissile(String playerLogin, String antiProbeMissileName, String targetOwnerName, String targetProbeName) throws RunningGameCommandException
	{
		AntiProbeMissile antiProbeMissile = db.getUnit(AntiProbeMissile.class, playerLogin, antiProbeMissileName);
		if (antiProbeMissile == null)
			throw new RunningGameCommandException("AntiProbeMissile '" + antiProbeMissileName + "' does not exist.");

		if (antiProbeMissile.isMoving())
			throw new RunningGameCommandException("AntiProbeMissile '" + antiProbeMissileName + "' has already been fired.");

		if (antiProbeMissile.isFired())
			throw new RunningGameCommandException("AntiProbeMissile '" + antiProbeMissileName + "' is already fired.");

		RealLocation destination;
		Probe targetProbe = db.getUnit(Probe.class, targetOwnerName, targetProbeName);
		if (targetProbe != null)
		{
			destination = targetProbe.getCurrentLocationView(db.getDate(), playerLogin, false);
		}
		else
		{
			UnitMarker um = db.getUnitMarker(playerLogin, targetOwnerName, targetProbeName);
			if (um == null || um.getUnit() == null || !org.axan.sep.common.Probe.class.isInstance(um.getUnit()))
			{
				throw new RunningGameCommandException("AntiProbeMissile '" + antiProbeMissileName + "' target '" + targetProbeName + "' does not exist.");
			}

			org.axan.sep.common.Probe probe = org.axan.sep.common.Probe.class.cast(um.getUnit());

			if (probe.isMoving())
				throw new RunningGameCommandException("AntiProbeMissile '" + antiProbeMissileName + "' cannot be fired on moving target '" + targetProbeName + "'");

			destination = probe.getCurrentLocation();
		}

		if (destination == null)
			throw new RunningGameCommandException("AntiProbeMissile '" + antiProbeMissileName + "' target '" + targetProbeName + "' does not exist.");

		return new FireAntiProbeMissileCheckResult(antiProbeMissile, antiProbeMissile.getRealLocation(), destination);
	}

	public CommandCheckResult canLaunchProbe(String playerLogin, LaunchProbeParams p)
	{
		try
		{
			checkLaunchProbe(playerLogin, p.probeName, p.destination);
		}
		catch(Throwable t)
		{
			return new CommandCheckResult(t);
		}
		return new CommandCheckResult();
	}

	public void launchProbe(String playerLogin, String probeName, RealLocation destination) throws RunningGameCommandException
	{
		LaunchProbeCheckResult launchProbeCheckResult = checkLaunchProbe(playerLogin, probeName, destination);
		launchProbeCheckResult.probe.launch(launchProbeCheckResult.destination);
	}

	private static class LaunchProbeCheckResult
	{
		final Probe probe;
		final RealLocation destination;

		public LaunchProbeCheckResult(Probe probe, RealLocation destination)
		{
			this.probe = probe;
			this.destination = destination;
		}
	}

	public LaunchProbeCheckResult checkLaunchProbe(String playerLogin, String probeName, RealLocation destination) throws RunningGameCommandException
	{
		Probe probe = db.getUnit(Probe.class, playerLogin, probeName);
		if (probe == null)
			throw new RunningGameCommandException("Probe '" + probeName + "' does not exist.");

		if (probe.isMoving())
			throw new RunningGameCommandException("Probe '" + probeName + "' has already been launched.");

		if (probe.isDeployed())
			throw new RunningGameCommandException("Probe '" + probeName + "' is already deployed.");

		if (destination.x < 0 || destination.x >= db.getGameConfig().getDimX())
			throw new RunningGameCommandException("Probe '" + probeName + "' destination is incorrect (x).");
		if (destination.y < 0 || destination.y >= db.getGameConfig().getDimY())
			throw new RunningGameCommandException("Probe '" + probeName + "' destination is incorrect (y).");
		if (destination.z < 0 || destination.z >= db.getGameConfig().getDimZ())
			throw new RunningGameCommandException("Probe '" + probeName + "' destination is incorrect (z).");

		if (isTravellingTheSun(probe.getRealLocation(), destination))
		{
			throw new RunningGameCommandException("Impossible path : " + probe.getRealLocation() + " to " + destination + ", cannot travel the sun.");
		}

		return new LaunchProbeCheckResult(probe, destination);
	}

	public CommandCheckResult canMoveFleet(String playerLogin, MoveFleetParams p)
	{
		try
		{
			checkMoveFleet(playerLogin, p.fleetName, p.checkpoints);
		}
		catch(Throwable t)
		{
			return new CommandCheckResult(t);
		}
		return new CommandCheckResult();
	}

	public void moveFleet(String playerLogin, String fleetName, Stack<org.axan.sep.common.Fleet.Move> checkpoints) throws RunningGameCommandException
	{
		MoveFleetCheckResult moveFleetCheckResult = checkMoveFleet(playerLogin, fleetName, checkpoints);
		moveFleetCheckResult.fleet.updateMoveOrder(moveFleetCheckResult.locatedCheckpoints);
	}

	private static class MoveFleetCheckResult
	{
		final Fleet fleet;
		final Stack<org.axan.sep.common.Fleet.Move> locatedCheckpoints;

		public MoveFleetCheckResult(Fleet fleet, Stack<org.axan.sep.common.Fleet.Move> locatedCheckpoints)
		{
			this.fleet = fleet;
			this.locatedCheckpoints = locatedCheckpoints;
		}
	}

	public MoveFleetCheckResult checkMoveFleet(String playerLogin, String fleetName, Stack<org.axan.sep.common.Fleet.Move> checkpoints) throws RunningGameCommandException
	{
		Fleet fleet = db.getUnit(Fleet.class, playerLogin, fleetName);
		if (fleet == null)
			throw new RunningGameCommandException("Fleet '" + fleetName + "' does not exist.");

		// Check paths
		Stack<org.axan.sep.common.Fleet.Move> locatedCheckpoints = new Stack<org.axan.sep.common.Fleet.Move>();

		RealLocation currentStart = (fleet.isMoving() ? fleet.getDestinationLocation() : fleet.getRealLocation());
		for(org.axan.sep.common.Fleet.Move move: checkpoints)
		{
			Location destinationLocation = db.getCelestialBody(move.getDestinationName()).getLocation();
			if (destinationLocation == null)
				throw new RunningGameCommandException("Unexpected error : checkpoint destination '" + move.getDestinationName() + "' not found.");

			if (isTravellingTheSun(currentStart, destinationLocation.asRealLocation()))
			{
				throw new RunningGameCommandException("Impossible path : " + currentStart + " to " + destinationLocation.asRealLocation() + ", cannot travel the sun.");
			}

			currentStart = destinationLocation.asRealLocation();

			locatedCheckpoints.add(new org.axan.sep.common.Fleet.Move(move, destinationLocation.asRealLocation()));
		}

		return new MoveFleetCheckResult(fleet, locatedCheckpoints);
	}

	public CommandCheckResult canFormFleet(String playerLogin, FormFleetParams p)
	{
		try
		{
			checkFormFleet(playerLogin, p.productiveCelestialBodyName, p.fleetName, p.fleetToFormStarships, p.fleetToFormSpecialUnits);
		}
		catch(Throwable t)
		{
			return new CommandCheckResult(t);
		}
		return new CommandCheckResult();
	}

	public void formFleet(String playerLogin, String planetName, String fleetName, Map<org.axan.sep.common.StarshipTemplate, Integer> fleetToFormStarships, Set<String> fleetToFormSpecialUnits) throws RunningGameCommandException
	{
		FormFleetCheckResult formFleetCheckResult = checkFormFleet(playerLogin, planetName, fleetName, fleetToFormStarships, fleetToFormSpecialUnits);
		db.insertUnit(formFleetCheckResult.newFleet);
		formFleetCheckResult.productiveCelestialBody.removeFromUnasignedFleet(playerLogin, fleetToFormStarships, formFleetCheckResult.newFleet.getSpecialUnits());
	}

	private static class FormFleetCheckResult
	{
		final ProductiveCelestialBody productiveCelestialBody;
		final Fleet newFleet;

		public FormFleetCheckResult(ProductiveCelestialBody productiveCelestialBody, Fleet newFleet)
		{
			this.productiveCelestialBody = productiveCelestialBody;
			this.newFleet = newFleet;
		}
	}

	private FormFleetCheckResult checkFormFleet(String playerLogin, String productiveCelestialBodyName, String fleetName, Map<org.axan.sep.common.StarshipTemplate, Integer> fleetToFormStarships, Set<String> fleetToFormSpecialUnits) throws RunningGameCommandException
	{
		ProductiveCelestialBody productiveCelestialBody = db.getCelestialBody(productiveCelestialBodyName, ProductiveCelestialBody.class);
		if (productiveCelestialBody == null)
			throw new RunningGameCommandException("Celestial body '" + productiveCelestialBodyName + "' does not exist.");

		Fleet unasignedFleet = productiveCelestialBody.getUnasignedFleet(playerLogin);
		if (unasignedFleet == null)
			throw new RunningGameCommandException("No available unasigned fleet on celestial body '" + productiveCelestialBodyName + "'");

		Fleet fleet = db.getUnit(Fleet.class, playerLogin, fleetName);
		if (fleet != null)
			throw new RunningGameCommandException("Fleet named '" + fleetName + "' already exist.");

		// Starship availability check		
		for(Entry<org.axan.sep.common.StarshipTemplate, Integer> e: fleetToFormStarships.entrySet())
		{
			if (e.getValue() <= 0)
				continue;

			int qt = e.getValue();
			if (!unasignedFleet.getStarships().containsKey(e.getKey()))
				throw new RunningGameCommandException("Unasigned fleet does not have required starship type '" + e.getKey().getName() + "'");
			if (unasignedFleet.getStarships().get(e.getKey()) < qt)
				throw new RunningGameCommandException("Unasigned flee does not have enough starship type '" + e.getKey() + "'");
		}

		// Special units availability check
		Set<ISpecialUnit> specialUnits = new HashSet<ISpecialUnit>();

		if (fleetToFormSpecialUnits != null)
			for(String specialUnitName: fleetToFormSpecialUnits)
			{
				ISpecialUnit specialUnit = unasignedFleet.getSpecialUnit(specialUnitName);

				if (specialUnit == null)
					throw new RunningGameCommandException("Cannot find special unit '" + specialUnitName + "' on '" + productiveCelestialBodyName + "'");
				if (!specialUnit.canJoinFleet())
					throw new RunningGameCommandException("Special unit '" + specialUnitName + "' cannot join fleet.");

				specialUnits.add(specialUnit);
			}

		Fleet newFleet = new Fleet(db, fleetName, playerLogin, productiveCelestialBody.getLocation().asRealLocation(), fleetToFormStarships, specialUnits, false, null, null);

		return new FormFleetCheckResult(productiveCelestialBody, newFleet);
	}

	public CommandCheckResult canDismantleFleet(String playerLogin, DismantleFleetParams p)
	{
		try
		{
			checkDismantleFleet(playerLogin, p.fleetName);
		}
		catch(Throwable t)
		{
			return new CommandCheckResult(t);
		}
		return new CommandCheckResult();
	}

	public void dismantleFleet(String playerName, String fleetName) throws RunningGameCommandException
	{
		DismantleFleetCheckResult dismantleFleetCheckResult = checkDismantleFleet(playerName, fleetName);

		db.removeUnit(dismantleFleetCheckResult.fleet.getKey());
		dismantleFleetCheckResult.productiveCelestialBody.mergeToUnasignedFleet(playerName, dismantleFleetCheckResult.fleet.getStarships(), dismantleFleetCheckResult.fleet.getSpecialUnits());
	}

	private static class DismantleFleetCheckResult
	{
		final ProductiveCelestialBody productiveCelestialBody;
		final Fleet fleet;

		public DismantleFleetCheckResult(ProductiveCelestialBody productiveCelestialBody, Fleet fleet)
		{
			this.productiveCelestialBody = productiveCelestialBody;
			this.fleet = fleet;
		}
	}

	private DismantleFleetCheckResult checkDismantleFleet(String playerName, String fleetName) throws RunningGameCommandException
	{
		Fleet fleet = db.getUnit(Fleet.class, playerName, fleetName);
		if (fleet == null)
			throw new RunningGameCommandException("Fleet '" + fleetName + "' does not exist.");

		ProductiveCelestialBody productiveCelestialBody = db.getCelestialBody(fleet.getRealLocation().asLocation(), ProductiveCelestialBody.class);
		if (productiveCelestialBody == null)
			throw new RunningGameCommandException("Fleet is in travel.");

		return new DismantleFleetCheckResult(productiveCelestialBody, fleet);
	}

	public CommandCheckResult canMakeProbes(String playerLogin, MakeProbesParams p)
	{
		MakeProbesCheckResult check;
		try
		{
			check = checkMakeProbes(playerLogin, p.planetName, p.probeName, p.quantity);
		}
		catch(Throwable t)
		{
			return new CommandCheckResult(t);
		}
		return new CommandCheckResult(check.carbonCost, check.populationCost);
	}

	public void makeProbes(String playerLogin, String planetName, String probeName, int quantity) throws RunningGameCommandException
	{
		MakeProbesCheckResult makeProbesCheckResult = checkMakeProbes(playerLogin, planetName, probeName, quantity);
		for(Probe p: makeProbesCheckResult.newProbes)
		{
			db.insertUnit(p);
		}
		makeProbesCheckResult.planet.setCarbon(makeProbesCheckResult.planet.getCarbon() - makeProbesCheckResult.carbonCost);
		makeProbesCheckResult.planet.setPopulation(makeProbesCheckResult.planet.getPopulation() - makeProbesCheckResult.populationCost);
	}

	private static class MakeProbesCheckResult
	{
		final Planet planet;
		final int carbonCost;
		final int populationCost;
		final Set<Probe> newProbes;

		public MakeProbesCheckResult(Planet planet, int carbonCost, int populationCost, Set<Probe> newProbes)
		{
			this.planet = planet;
			this.carbonCost = carbonCost;
			this.populationCost = populationCost;
			this.newProbes = newProbes;
		}
	}

	private MakeProbesCheckResult checkMakeProbes(String playerName, String planetName, String probeName, int quantity) throws RunningGameCommandException
	{
		Planet planet = db.getCelestialBody(planetName, Planet.class, playerName);
		if (planet == null)
			throw new RunningGameCommandException("Celestial body '" + planetName + "' does not exist.");

		StarshipPlant starshipPlant = planet.getBuilding(StarshipPlant.class);

		// If planet has no starshipPlant.
		if (starshipPlant == null)
			throw new RunningGameCommandException("No starship plant on planet '" + planetName + "'");

		// If starship has just been build this turn.
		if (starshipPlant.getLastBuildDate() >= db.getDate())
			throw new RunningGameCommandException("Starship plant is still in construction.");

		// Price check
		int carbonCost = org.axan.sep.common.Probe.PRICE_CARBON * quantity;
		int populationCost = org.axan.sep.common.Probe.PRICE_POPULATION * quantity;

		if (carbonCost == 0 && populationCost == 0)
			throw new RunningGameCommandException("Seems like quantity is null.");
		if (quantity < 0)
			throw new RunningGameCommandException("Quantity cannot be lesser than 0.");

		if (planet.getCarbon() < carbonCost)
			throw new RunningGameCommandException("Not enough carbon.");
		if (planet.getPopulation() < populationCost)
			throw new RunningGameCommandException("Not enough population.");

		if (db.getUnit(Probe.class, playerName, probeName) != null || db.getUnit(Probe.class, playerName, probeName + "1") != null)
		{
			throw new RunningGameCommandException("Probe serial '" + probeName + "' already exist.");
		}

		Set<Probe> newProbes = new HashSet<Probe>();
		for(int i = 0; i < quantity; ++i)
		{
			newProbes.add(new Probe(db, probeName + i, playerName, planet.getLocation().asRealLocation(), false, null));
		}

		return new MakeProbesCheckResult(planet, carbonCost, populationCost, newProbes);
	}

	public CommandCheckResult canMakeAntiProbeMissiles(String playerLogin, MakeAntiProbeMissilesParams p)
	{
		MakeAntiProbeMissilesCheckResult check;
		try
		{
			check = checkMakeAntiProbeMissiles(playerLogin, p.planetName, p.antiProbeMissileName, p.quantity);
		}
		catch(Throwable t)
		{
			return new CommandCheckResult(t);
		}
		return new CommandCheckResult(check.carbonCost, check.populationCost);
	}

	public void makeAntiProbeMissiles(String playerLogin, String planetName, String antiProbeMissileName, int quantity) throws RunningGameCommandException
	{
		MakeAntiProbeMissilesCheckResult makeAntiProbeMissilesCheckResult = checkMakeAntiProbeMissiles(playerLogin, planetName, antiProbeMissileName, quantity);
		for(AntiProbeMissile p: makeAntiProbeMissilesCheckResult.newAntiProbeMissiles)
		{
			db.insertUnit(p);
		}
		makeAntiProbeMissilesCheckResult.planet.setCarbon(makeAntiProbeMissilesCheckResult.planet.getCarbon() - makeAntiProbeMissilesCheckResult.carbonCost);
		makeAntiProbeMissilesCheckResult.planet.setPopulation(makeAntiProbeMissilesCheckResult.planet.getPopulation() - makeAntiProbeMissilesCheckResult.populationCost);
	}

	private static class MakeAntiProbeMissilesCheckResult
	{
		final Planet planet;
		final int carbonCost;
		final int populationCost;
		final Set<AntiProbeMissile> newAntiProbeMissiles;

		public MakeAntiProbeMissilesCheckResult(Planet planet, int carbonCost, int populationCost, Set<AntiProbeMissile> newAntiProbeMissiles)
		{
			this.planet = planet;
			this.carbonCost = carbonCost;
			this.populationCost = populationCost;
			this.newAntiProbeMissiles = newAntiProbeMissiles;
		}
	}

	private MakeAntiProbeMissilesCheckResult checkMakeAntiProbeMissiles(String playerName, String planetName, String antiProbeMissileName, int quantity) throws RunningGameCommandException
	{
		Planet planet = db.getCelestialBody(planetName, Planet.class, playerName);
		if (planet == null)
			throw new RunningGameCommandException("Celestial body '" + planetName + "' does not exist.");

		StarshipPlant starshipPlant = planet.getBuilding(StarshipPlant.class);

		// If planet has no starshipPlant.
		if (starshipPlant == null)
			throw new RunningGameCommandException("No starship plant on planet '" + planetName + "'");

		// If starship has just been build this turn.
		if (starshipPlant.getLastBuildDate() >= db.getDate())
			throw new RunningGameCommandException("Starship plant is still in construction.");

		// Price check
		int carbonCost = org.axan.sep.common.AntiProbeMissile.PRICE_CARBON * quantity;
		int populationCost = org.axan.sep.common.AntiProbeMissile.PRICE_POPULATION * quantity;

		if (carbonCost == 0 && populationCost == 0)
			throw new RunningGameCommandException("Seems like quantity is null.");
		if (quantity < 0)
			throw new RunningGameCommandException("Quantity cannot be lesser than 0.");

		if (planet.getCarbon() < carbonCost)
			throw new RunningGameCommandException("Not enough carbon.");
		if (planet.getPopulation() < populationCost)
			throw new RunningGameCommandException("Not enough population.");

		if (db.getUnit(Probe.class, playerName, antiProbeMissileName) != null || db.getUnit(Probe.class, playerName, antiProbeMissileName + "1") != null)
		{
			throw new RunningGameCommandException("AntiProbeMissile serial '" + antiProbeMissileName + "' already exist.");
		}

		Set<AntiProbeMissile> newAntiProbeMissiles = new HashSet<AntiProbeMissile>();
		for(int i = 0; i < quantity; ++i)
		{
			newAntiProbeMissiles.add(new AntiProbeMissile(db, antiProbeMissileName + i, playerName, planet.getLocation().asRealLocation(), false, null));
		}

		return new MakeAntiProbeMissilesCheckResult(planet, carbonCost, populationCost, newAntiProbeMissiles);
	}

	public CommandCheckResult canMakeStarships(String playerLogin, MakeStarshipsParams p)
	{
		MakeStarshipsCheckResult check;
		try
		{
			check = checkMakeStarships(playerLogin, p.planetName, p.starshipsToMake);
		}
		catch(Throwable t)
		{
			return new CommandCheckResult(t);
		}
		return new CommandCheckResult(check.carbonCost, check.populationCost);
	}

	public void makeStarships(String playerLogin, String planetName, Map<org.axan.sep.common.StarshipTemplate, Integer> starshipsToMake) throws RunningGameCommandException
	{
		MakeStarshipsCheckResult makeStarshipsCheckResult = checkMakeStarships(playerLogin, planetName, starshipsToMake);

		Fleet unassignedFleet = makeStarshipsCheckResult.planet.getUnasignedFleet(playerLogin);
		makeStarshipsCheckResult.planet.mergeToUnasignedFleet(playerLogin, starshipsToMake, null);

		makeStarshipsCheckResult.planet.setCarbon(makeStarshipsCheckResult.planet.getCarbon() - makeStarshipsCheckResult.carbonCost);
		makeStarshipsCheckResult.planet.setPopulation(makeStarshipsCheckResult.planet.getPopulation() - makeStarshipsCheckResult.populationCost);
	}

	private static class MakeStarshipsCheckResult
	{
		final Planet planet;
		final int carbonCost;
		final int populationCost;

		public MakeStarshipsCheckResult(Planet planet, int carbonCost, int populationCost)
		{
			this.planet = planet;
			this.carbonCost = carbonCost;
			this.populationCost = populationCost;
		}
	}

	private MakeStarshipsCheckResult checkMakeStarships(String playerName, String planetName, Map<org.axan.sep.common.StarshipTemplate, Integer> starshipsToMake) throws RunningGameCommandException
	{
		Planet planet = db.getCelestialBody(planetName, Planet.class, playerName);
		if (planet == null)
			throw new RunningGameCommandException("Celestial body '" + planetName + "' does not exist.");

		StarshipPlant starshipPlant = planet.getBuilding(StarshipPlant.class);

		// If planet has no starshipPlant.
		if (starshipPlant == null)
			throw new RunningGameCommandException("No starship plant on planet '" + planetName + "'");

		// If starship has just been build this turn.
		if (starshipPlant.getLastBuildDate() >= db.getDate())
			throw new RunningGameCommandException("Starship plant is still in construction.");

		// Price check
		int carbonCost = 0;
		int populationCost = 0;

		for(Entry<org.axan.sep.common.StarshipTemplate, Integer> e: starshipsToMake.entrySet())
		{
			if (e.getValue() <= 0)
				continue;

			int carbonPrice = 0;
			int populationPrice = 0;

			carbonPrice = e.getKey().getCarbonPrice();
			populationPrice = e.getKey().getPopulationPrice();

			if (carbonPrice == 0 && populationPrice == 0)
			{
				throw new RunningGameCommandException("Implementation error : Price are not defined for Starship template '" + e.getKey().getName() + "'");
			}

			carbonCost += carbonPrice * e.getValue();
			populationCost += populationPrice * e.getValue();
		}

		if (carbonCost == 0 && populationCost == 0)
			throw new RunningGameCommandException("Seems like no starships are selected (cost is null).");

		if (planet.getCarbon() < carbonCost)
			throw new RunningGameCommandException("Not enough carbon.");
		if (planet.getPopulation() < populationCost)
			throw new RunningGameCommandException("Not enough population.");

		return new MakeStarshipsCheckResult(planet, carbonCost, populationCost);
	}

	public CommandCheckResult canChangeDiplomacy(String playerLogin, ChangeDiplomacyParams p)
	{
		try
		{
			checkChangeDiplomacy(playerLogin, p.newPolicies);
		}
		catch(Throwable t)
		{
			return new CommandCheckResult(t);
		}
		return new CommandCheckResult();
	}

	public void changeDiplomacy(String playerLogin, Map<String, PlayerPolicies> newPolicies) throws RunningGameCommandException
	{
		ChangeDiplomacyCheckResult changeDiplomacyCheckResult = checkChangeDiplomacy(playerLogin, newPolicies);

		db.getPlayerPolicies(playerLogin).update(newPolicies);
	}

	private static class ChangeDiplomacyCheckResult
	{
		public ChangeDiplomacyCheckResult()
		{

		}
	}

	private ChangeDiplomacyCheckResult checkChangeDiplomacy(String playerLogin, Map<String, PlayerPolicies> newPolicies) throws RunningGameCommandException
	{
		if (newPolicies.get(playerLogin) != null)
			throw new RunningGameCommandException("Cannot have a diplomacy toward ourselves.");
		return new ChangeDiplomacyCheckResult();
	}

	public CommandCheckResult canAttackEnemiesFleet(String playerLogin, AttackEnemiesFleetParams p)
	{
		try
		{
			checkAttackEnemiesFleet(playerLogin, p.celestialBodyName);
		}
		catch(Throwable t)
		{
			return new CommandCheckResult(t);
		}

		return new CommandCheckResult();
	}

	public void attackEnemiesFleet(String playerLogin, String celestialBodyName) throws RunningGameCommandException
	{
		AttackEnemiesFleetCheckResult attackEnemiesFleetCheckResult = checkAttackEnemiesFleet(playerLogin, celestialBodyName);
		attackEnemiesFleetCheckResult.productiveCelestialBody.addConflictInititor(playerLogin);
	}

	private static class AttackEnemiesFleetCheckResult
	{
		final ProductiveCelestialBody productiveCelestialBody;

		public AttackEnemiesFleetCheckResult(ProductiveCelestialBody productiveCelestialBody)
		{
			this.productiveCelestialBody = productiveCelestialBody;
		}
	}

	private AttackEnemiesFleetCheckResult checkAttackEnemiesFleet(String playerName, String celestialBodyName) throws RunningGameCommandException
	{
		ProductiveCelestialBody productiveCelestialBody = db.getCelestialBody(celestialBodyName, ProductiveCelestialBody.class);
		if (productiveCelestialBody == null)
			throw new RunningGameCommandException("Celestial body '" + celestialBodyName + "' is not a productive celestial body.");

		return new AttackEnemiesFleetCheckResult(productiveCelestialBody);
	}

	/////

	public CommandCheckResult canBuildSpaceRoad(String playerLogin, BuildSpaceRoadParams p)
	{
		BuildSpaceRoadCheckResult check;
		try
		{
			check = checkBuildSpaceRoad(playerLogin, p.productiveCelestialBodyNameA, p.productiveCelestialBodyNameB);
		}
		catch(Throwable t)
		{
			return new CommandCheckResult(t);
		}
		return new CommandCheckResult(check.price, 0);
	}

	public void buildSpaceRoad(String playerLogin, String productiveCelestialBodyNameA, String productiveCelestialBodyNameB) throws RunningGameCommandException
	{
		BuildSpaceRoadCheckResult buildSpaceRoadCheckResult = checkBuildSpaceRoad(playerLogin, productiveCelestialBodyNameA, productiveCelestialBodyNameB);

		db.insertUnit(buildSpaceRoadCheckResult.deliverer);
		buildSpaceRoadCheckResult.deliverer.launch(buildSpaceRoadCheckResult.destinationLocation.asRealLocation());
		buildSpaceRoadCheckResult.payer.setCarbon(buildSpaceRoadCheckResult.payer.getCarbon() - buildSpaceRoadCheckResult.price);
	}

	private static class BuildSpaceRoadCheckResult
	{
		final SpaceRoadDeliverer deliverer;
		final Location destinationLocation;
		final ProductiveCelestialBody payer;
		final int price;

		public BuildSpaceRoadCheckResult(ProductiveCelestialBody payer, int price, SpaceRoadDeliverer deliverer, Location destinationLocation)
		{
			this.payer = payer;
			this.price = price;
			this.deliverer = deliverer;
			this.destinationLocation = destinationLocation;
		}
	}

	private BuildSpaceRoadCheckResult checkBuildSpaceRoad(String playerLogin, String sourceName, String destinationName) throws RunningGameCommandException
	{
		ProductiveCelestialBody source = db.getCelestialBody(sourceName, ProductiveCelestialBody.class);
		if (source == null)
			throw new RunningGameCommandException("Celestial body '" + sourceName + "' is not a productive celestial body.");

		SpaceCounter sourceSpaceCounter = source.getBuilding(SpaceCounter.class);
		if (sourceSpaceCounter == null)
			throw new RunningGameCommandException("'" + sourceName + "' has no space counter build.");

		ProductiveCelestialBody destination = db.getCelestialBody(destinationName, ProductiveCelestialBody.class);
		if (destination == null)
			throw new RunningGameCommandException("Celestial body '" + sourceName + "' is not a productive celestial body.");

		SpaceCounter destinationSpaceCounter = destination.getBuilding(SpaceCounter.class);
		if (destinationSpaceCounter == null)
			throw new RunningGameCommandException("'" + destinationName + "' has no space counter build.");

		if (sourceSpaceCounter.hasSpaceRoadTo(destinationName))
		{
			throw new RunningGameCommandException("'" + sourceName + "' already has a space road to '" + destinationName + "'");
		}

		if (sourceSpaceCounter.hasSpaceRoadLinkedFrom(sourceName))
		{
			throw new RunningGameCommandException("'" + sourceName + "' already has a space road linked from '" + sourceName + "'");
		}

		if (isTravellingTheSun(source.getLocation().asRealLocation(), destination.getLocation().asRealLocation()))
		{
			throw new RunningGameCommandException("Impossible path : " + source.getLocation() + " to " + destination.getLocation() + ", cannot travel the sun.");
		}

		double distance = SEPUtils.getDistance(source.getLocation().asRealLocation(), destination.getLocation().asRealLocation());
		int price = (int) (db.getGameConfig().getSpaceRoadPricePerArea() * distance);

		if (!playerLogin.equals(source.getOwnerName()) || sourceSpaceCounter == null || sourceSpaceCounter.getAvailableRoadsBuilder() <= 0 || source.getCarbon() < price)
		{
			throw new RunningGameCommandException("None of the space road end can pay nor have free builder.");
		}

		String delivererId = sourceName + " to " + destinationName + " space road deliverer";
		if (db.getUnit(SpaceRoadDeliverer.class, playerLogin, delivererId) != null)
			throw new RunningGameCommandException("Space road from '" + sourceName + "' to '" + destinationName + "' is already in delivery.");
		SpaceRoadDeliverer deliverer = new SpaceRoadDeliverer(db, sourceName + " to " + destinationName + " space road deliverer", playerLogin, source.getLocation().asRealLocation(), sourceName, destinationName, null);

		return new BuildSpaceRoadCheckResult(source, price, deliverer, destination.getLocation());
	}

	public CommandCheckResult canDemolishSpaceRoad(String playerLogin, DemolishSpaceRoadParams p)
	{
		try
		{
			checkDemolishSpaceRoad(playerLogin, p.sourceName, p.destinationName);
		}
		catch(Throwable t)
		{
			return new CommandCheckResult(t);
		}
		return new CommandCheckResult();
	}

	public void demolishSpaceRoad(String playerLogin, String sourceName, String destinationName) throws RunningGameCommandException
	{
		DemolishSpaceRoadCheckResult demolishSpaceRoadCheckResult = checkDemolishSpaceRoad(playerLogin, sourceName, destinationName);
		demolishSpaceRoadCheckResult.source.cutSpaceRoadLinkWith(destinationName);
	}

	private static class DemolishSpaceRoadCheckResult
	{
		final SpaceCounter source;

		public DemolishSpaceRoadCheckResult(SpaceCounter source)
		{
			this.source = source;
		}
	}

	private DemolishSpaceRoadCheckResult checkDemolishSpaceRoad(String playerLogin, String sourceName, String destinationName) throws RunningGameCommandException
	{
		ProductiveCelestialBody source = db.getCelestialBody(sourceName, ProductiveCelestialBody.class);
		if (source == null)
			throw new RunningGameCommandException("Celestial body '" + sourceName + "' is not a productive celestial body.");

		SpaceCounter sourceSpaceCounter = source.getBuilding(SpaceCounter.class);
		if (sourceSpaceCounter == null)
			throw new RunningGameCommandException("'" + sourceName + "' has no space counter build.");

		if (!sourceSpaceCounter.hasSpaceRoadTo(destinationName) && !sourceSpaceCounter.hasSpaceRoadLinkedFrom(destinationName))
		{
			throw new RunningGameCommandException("'" + sourceName + "' has no space road link with '" + destinationName + "'");
		}

		return new DemolishSpaceRoadCheckResult(sourceSpaceCounter);
	}

	////

	public CommandCheckResult canModifyCarbonOrder(String playerLogin, ModifyCarbonOrderParams p)
	{
		try
		{
			checkModifyCarbonOrder(playerLogin, p.originCelestialBodyName, p.nextCarbonOrders);
		}
		catch(Throwable t)
		{
			return new CommandCheckResult(t);
		}
		return new CommandCheckResult();
	}

	public void modifyCarbonOrder(String playerLogin, String originCelestialBodyName, Stack<CarbonOrder> nextCarbonOrders) throws RunningGameCommandException
	{
		ModifyCarbonOrderCheckResult modifyCarbonOrderCheckResult = checkModifyCarbonOrder(playerLogin, originCelestialBodyName, nextCarbonOrders);
		modifyCarbonOrderCheckResult.spaceCounter.modifyCarbonOrder(nextCarbonOrders);
	}

	private static class ModifyCarbonOrderCheckResult
	{
		final SpaceCounter spaceCounter;

		public ModifyCarbonOrderCheckResult(SpaceCounter spaceCounter)
		{
			this.spaceCounter = spaceCounter;
		}
	}

	private ModifyCarbonOrderCheckResult checkModifyCarbonOrder(String playerLogin, String originCelestialBodyName, Stack<CarbonOrder> nextCarbonOrders) throws RunningGameCommandException
	{
		ProductiveCelestialBody source = db.getCelestialBody(originCelestialBodyName, ProductiveCelestialBody.class);
		if (source == null)
			throw new RunningGameCommandException("Celestial body '" + originCelestialBodyName + "' is not a productive celestial body.");

		if (!playerLogin.equals(source.getOwnerName()))
		{
			throw new RunningGameCommandException("'" + playerLogin + "' is not the celestial body '" + originCelestialBodyName + "' owner.");
		}

		SpaceCounter sourceSpaceCounter = source.getBuilding(SpaceCounter.class);
		if (sourceSpaceCounter == null)
			throw new RunningGameCommandException("'" + originCelestialBodyName + "' has no space counter build.");

		for(CarbonOrder order: nextCarbonOrders)
		{
			String destinationCelestialBodyName = order.getDestinationName();
			int amount = order.getAmount();

			ProductiveCelestialBody destination = db.getCelestialBody(destinationCelestialBodyName, ProductiveCelestialBody.class);
			if (destination == null)
				throw new RunningGameCommandException("Celestial body '" + destinationCelestialBodyName + "' is not a productive celestial body.");

			SpaceCounter destinationSpaceCounter = destination.getBuilding(SpaceCounter.class);
			if (destinationSpaceCounter == null)
				throw new RunningGameCommandException("'" + destinationCelestialBodyName + "' has no space counter build.");

			if (amount < db.getGameConfig().getCarbonMinimalFreight())
				throw new RunningGameCommandException("Carbon amount must be greater than 0.");

			if (isTravellingTheSun(source.getLocation().asRealLocation(), destination.getLocation().asRealLocation()))
			{
				throw new RunningGameCommandException("Impossible path : " + source.getLocation() + " to " + destination.getLocation() + ", cannot travel the sun.");
			}
		}

		return new ModifyCarbonOrderCheckResult(sourceSpaceCounter);
	}

	////
	*/
}
