package org.axan.sep.server;

import java.awt.Color;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.axan.eplib.orm.sqlite.SQLiteDB;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteStatementJob;
import org.axan.eplib.utils.Basic;
import org.axan.sep.common.db.IGameConfig;
import org.axan.sep.common.PlayerGameBoard;
import org.axan.sep.common.Protocol;
import org.axan.sep.common.Rules;
import org.axan.sep.common.SEPCommonImplementationException;
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
import org.axan.sep.common.db.IGovernment;
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
import org.axan.sep.common.db.sqlite.SQLitePlayerGameBoard;
import org.axan.sep.common.db.sqlite.SEPCommonSQLiteDB;
import org.axan.sep.common.db.sqlite.orm.*;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteJob;
import com.almworks.sqlite4java.SQLiteStatement;

class SQLiteGameBoard extends GameBoard implements Serializable
{

	/** Serialization version */
	private static final long serialVersionUID = 1L;
	
	private static final Logger log = Logger.getLogger(SQLiteGameBoard.class.getName());
	
	private static final Random rnd = new Random();
	
	private SEPCommonSQLiteDB commonDB;
	private transient SQLiteDB db;

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
	
	public SQLiteGameBoard(Set<org.axan.sep.common.Player> players, org.axan.sep.common.GameConfig config) throws IOException, GameConfigCopierException, SQLiteDBException
	{
		this.commonDB = new SEPCommonSQLiteDB(config);
		this.db = commonDB.getDB();
		
		// Generate Universe
		try
		{		
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
			
			for(org.axan.sep.common.Player player : players)
			{
				insertPlayer(player);								
				
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
			
			config.setTurn(1);
		}
		catch(SQLiteException e)
		{
			throw new SQLiteDBException(e);
		}
	}
	
	public IGameConfig getConfig()
	{
		return commonDB.getConfig();
	}
	
	private void compilePlayerView(final String playerLogin, SEPCommonSQLiteDB currentView, int maxTurn) throws SQLiteDBException
	{
		// GameConfig, already copied during DB creation.
		
		final Set<IArea> areas = new HashSet<IArea>();
		final Set<IPlayer> players = new HashSet<IPlayer>();
		final Set<IPlayerConfig> playerConfigs = new HashSet<IPlayerConfig>();
		final Set<ICelestialBody> celestialBodies = new HashSet<ICelestialBody>();
		final Set<IStarshipTemplate> starshipTemplates = new HashSet<IStarshipTemplate>();
		final Set<ICarbonOrder> carbonOrders = new HashSet<ICarbonOrder>();
		
		db.exec(new SQLiteJob<Void>()
		{
			@Override
			protected Void job(SQLiteConnection conn) throws Throwable
			{
				// Update areas
				areas.addAll(Area.select(conn, getConfig(), IArea.class, null, null));
				
				// Update players
				players.addAll(Player.select(conn, getConfig(), IPlayer.class, null, null));
				
				// Update player configs
				playerConfigs.addAll(PlayerConfig.select(conn, getConfig(), IPlayerConfig.class, null, null));
				
				// Update celestial bodies
				celestialBodies.addAll(ProductiveCelestialBody.selectUnversioned(conn, getConfig(), IProductiveCelestialBody.class, null, null));
				celestialBodies.addAll(Vortex.select(conn, getConfig(), IVortex.class, null, null));
				
				// Update starship templates
				starshipTemplates.addAll(StarshipTemplate.select(conn, getConfig(), IStarshipTemplate.class, null, null));
				
				// Update player carbon orders (live table, assume initial player view carbon order table is empty)
				carbonOrders.addAll(CarbonOrder.select(conn, getConfig(), ICarbonOrder.class, null, "owner = %s", playerLogin));
				
				return null;
			}
		});
		
		currentView.getDB().exec(new SQLiteJob<Void>()
		{
			@Override
			protected Void job(SQLiteConnection conn) throws Throwable
			{
				for(IArea a : areas)
				{
					Area.insertOrUpdate(conn, a);
				}
				
				for(IPlayer p : players)
				{
					Player.insertOrUpdate(conn, p);
				}
				
				for(IPlayerConfig pc : playerConfigs)
				{
					PlayerConfig.insertOrUpdate(conn, pc);
				}
				
				for(ICelestialBody cb : celestialBodies)
				{
					if (IVortex.class.isInstance(cb))
					{
						Vortex.insertOrUpdate(conn, IVortex.class.cast(cb));
					}
					else if (IProductiveCelestialBody.class.isInstance(cb))
					{
						ProductiveCelestialBody.insertOrUpdate(conn, IProductiveCelestialBody.class.cast(cb));
					}
				}
				
				for(IStarshipTemplate st : starshipTemplates)
				{
					StarshipTemplate.insertOrUpdate(conn, st);
				}
				
				for(ICarbonOrder co : carbonOrders)
				{
					CarbonOrder.insertOrUpdate(conn, co);
				}
				
				return null;
			}
		});
		
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
			final int currentTurn = i;
			
			final Map<String, IGovernment> currentTurnGovernmentsView = new HashMap<String, IGovernment>();
			final Map<String, Set<IDiplomacy>> currentTurnDiplomaciesView = new HashMap<String, Set<IDiplomacy>>();
			final Map<String, IGovernment> currentTurnGovernments = new HashMap<String, IGovernment>();
			final Map<String, Set<IDiplomacy>> currentTurnDiplomacies = new HashMap<String, Set<IDiplomacy>>();
			final Set<IVersionedProbe> vprobes = new HashSet<IVersionedProbe>();
			final Set<IVersionedProductiveCelestialBody> visiblesPcbs = new HashSet<IVersionedProductiveCelestialBody>();
			final Set<IUnitArrivalLog> arrivalLogs =  new HashSet<IUnitArrivalLog>();
			
			db.exec(new SQLiteJob<Void>()
			{
				@Override
				protected Void job(SQLiteConnection conn) throws Throwable
				{
					// Select db governments for currentTurn
					Set<IGovernment> governments = Government.selectMaxVersion(conn, getConfig(), IGovernment.class, currentTurn, null, null);
					for(IGovernment g : governments)
					{
						currentTurnGovernments.put(g.getOwner(), g);
					}
					
					// Select db diplomacies for currentTurn
					Set<IDiplomacy> diplomacies = Diplomacy.selectMaxVersion(conn, getConfig(), IDiplomacy.class, currentTurn, null, null);
					for(IDiplomacy d : diplomacies)
					{
						if (!currentTurnDiplomacies.containsKey(d.getOwner())) currentTurnDiplomacies.put(d.getOwner(), new HashSet<IDiplomacy>());
						currentTurnDiplomacies.get(d.getOwner()).add(d);
					}
					
					// Update deployed player probes
					vprobes.addAll(Unit.selectMaxVersion(conn, getConfig(), IVersionedProbe.class, currentTurn, null, "VersionedUnit.type = %s AND VersionedUnit.owner = %s AND VersionedUnit.progress = 100.0", eUnitType.Probe, playerLogin));
					
					// Update visibles celestial bodies.
					// CelestialBody visible if owned by the player
					visiblesPcbs.addAll(ProductiveCelestialBody.selectMaxVersion(conn, getConfig(), IVersionedProductiveCelestialBody.class, currentTurn, null, "owner = %s", playerLogin));					
					// CelestialBody visible if a player unit is stopped on it with currentTurn version.
					visiblesPcbs.addAll(ProductiveCelestialBody.selectVersion(conn, getConfig(), IVersionedProductiveCelestialBody.class, currentTurn, "VersionedUnit VU", "(VU.owner = %s) AND ((VU.progress = 0.0 AND VU.departure_x = CelestialBody.location_x AND VU.departure_y = CelestialBody.location_y AND VU.departure_z = CelestialBody.location_z) OR (VU.progress = 100.0 AND VU.destination_x = CelestialBody.location_x AND VU.destination_y = CelestialBody.location_y AND VU.destination_z = CelestialBody.location_z))", playerLogin));					
					// CelestialBody visible if under a deployed probe scope.
					visiblesPcbs.addAll(ProductiveCelestialBody.selectMaxVersion(conn, getConfig(), IVersionedProductiveCelestialBody.class, currentTurn, "VersionedUnit VU", "(VU.owner = %s AND VU.type = %s AND VU.turn <= VersionedProductiveCelestialBody.turn AND VU.progress = 100.0 AND ((VU.destination_x - CelestialBody.location_x) * (VU.destination_x - CelestialBody.location_x) + (VU.destination_y - CelestialBody.location_y) * (VU.destination_y - CelestialBody.location_y) + (VU.destination_z - CelestialBody.location_z) * (VU.destination_z - CelestialBody.location_z)) <= (%d * %d))", playerLogin, eUnitType.Probe, getConfig().getUnitTypeSight(eUnitType.Probe), getConfig().getUnitTypeSight(eUnitType.Probe)));
					
					// Update player UnitArrivalLog (visibles units stopped on pcb, arrival log are inserted in db only when they are published, so no filter needed here, simple copŷ).
					arrivalLogs.addAll(UnitArrivalLog.selectMaxVersion(conn, getConfig(), IUnitArrivalLog.class, currentTurn, null, "owner = %s", playerLogin));
					
					return null;
				}				
			});
						
			currentView.getDB().exec(new SQLiteJob<Void>()
			{
				@Override
				protected Void job(SQLiteConnection conn) throws Throwable
				{
					for(IVersionedProductiveCelestialBody vpcb : visiblesPcbs)
					{
						ProductiveCelestialBody.insertOrUpdate(conn, vpcb);
					}
					
					for(final IVersionedProbe vprobe : vprobes)
					{
						Unit.insertOrUpdate(conn, vprobe);
					}
					
					for( IUnitArrivalLog ual : arrivalLogs)
					{
						UnitArrivalLog.insertOrUpdate(conn, ual);
					}
					return null;
				}
			});
			
			final Set<IAssignedFleet> assignedFleets = new HashSet<IAssignedFleet>();
			final Set<IBuilding> buildings = new HashSet<IBuilding>();
			final Set<ISpaceRoad> roads = new HashSet<ISpaceRoad>();
			final Set<IFleetComposition> comps = new HashSet<IFleetComposition>();
			final Set<IMovePlan> moves = new HashSet<IMovePlan>();
			final Set<IVersionedSpecialUnit> specialsUnits = new HashSet<IVersionedSpecialUnit>();
			final Set<IVersionedUnit> seenUnits = new HashSet<IVersionedUnit>();
			final Set<IUnitEncounterLog> encounterLogs = new HashSet<IUnitEncounterLog>();			
			final Set<IVersionedUnit> vunits = new HashSet<IVersionedUnit>();
			
			for(final IVersionedProductiveCelestialBody vpcb : visiblesPcbs)
			{
				final Set<IVersionedUnit> currentPCBUnits = new HashSet<IVersionedUnit>();				
				
				db.exec(new SQLiteJob<Void>()
				{
					@Override
					protected Void job(SQLiteConnection conn) throws Throwable
					{
						// Update buildings (visibles pcbs)
						buildings.addAll(Building.selectMaxVersion(conn, getConfig(), IBuilding.class, currentTurn, null, "celestialBodyName = %s", vpcb.getName()));
						
						// Update space roads (visibles pcbs)
						roads.addAll(SpaceRoad.select(conn, getConfig(), ISpaceRoad.class, null, "(SpaceRoad.spaceCounterACelestialBodyName = %s OR SpaceRoad.spaceCounterBCelestialBodyName = %s) AND (MAX(SpaceRoad.spaceCounterATurn, SpaceRoad.spaceCounterBTurn) <= %d)", vpcb.getName(), vpcb.getName(), currentTurn));
						
						// Update units on visibles pcbs
						currentPCBUnits.addAll(Unit.selectMaxVersion(conn, getConfig(), IVersionedUnit.class, currentTurn, "CelestialBody　CB", "CB.name = %s AND VersionedUnit.progress = 0.0 AND CB.location_x = VersionedUnit.departure_x AND CB.location_y AND VersionedUnit.departure_y AND CB.location_z = VersionedUnit.departure_z", vpcb.getName()));
						
						// Update assigned fleets
						assignedFleets.addAll(AssignedFleet.select(conn, getConfig(), IAssignedFleet.class, null, "celestialBody = %s", vpcb.getName()));
						
						return null;
					}
				});
				
				vunits.addAll(currentPCBUnits);
								
				for(final IVersionedUnit vunit : currentPCBUnits)
				{
					db.exec(new SQLiteJob<Void>()
					{
						@Override
						protected Void job(SQLiteConnection conn) throws Throwable
						{
							Set<IUnitEncounterLog> result = UnitEncounterLog.selectMaxVersion(conn, getConfig(), IUnitEncounterLog.class, currentTurn, null, "owner = %s AND unitName = %s AND turn = %d", vunit.getOwner(), vunit.getName(), vunit.getTurn());
							encounterLogs.addAll(result);
							for(IUnitEncounterLog uel : result)
							{
								Set<IVersionedUnit> currentUnitEncounteredUnits = Unit.selectVersion(conn, getConfig(), IVersionedUnit.class, uel.getSeenTurn(), null, "owner = %s AND name = %s AND type = %s", uel.getSeenOwner(), uel.getSeenName(), uel.getSeenType());
								for(IVersionedUnit encounteredUnit : currentUnitEncounteredUnits)
								{									
									if (IVersionedFleet.class.isInstance(encounteredUnit))
									{
										// (Fleet) Update encountered unit FleetComposition
										comps.addAll(FleetComposition.select(conn, getConfig(), IFleetComposition.class, null, "fleetOwner = %s AND fleetName = %s AND fleetTurn = %d", encounteredUnit.getOwner(), encounteredUnit.getName(), encounteredUnit.getTurn()));
										// (Fleet) Update encountered unit special units
										specialsUnits.addAll(SpecialUnit.selectVersion(conn, getConfig(), IVersionedSpecialUnit.class, currentTurn, null, "fleetOwner = %s AND fleetName = %s AND fleetTurn = %s", encounteredUnit.getOwner(), encounteredUnit.getName(), encounteredUnit.getTurn()));
									}																		
								}
								
								if (IVersionedFleet.class.isInstance(vunit))
								{
									// (Fleet) Update MovePlan, FleetComposition
									moves.addAll(MovePlan.select(conn, getConfig(), IMovePlan.class, null, "owner = %s AND name = %s AND turn = %d", vunit.getOwner(), vunit.getName(), vunit.getTurn()));
									comps.addAll(FleetComposition.select(conn, getConfig(), IFleetComposition.class, null, "fleetOwner = %s AND fleetName = %s AND fleetTurn = %d", vunit.getOwner(), vunit.getName(), vunit.getTurn()));
									// (Fleet) Update special units
									specialsUnits.addAll(SpecialUnit.selectVersion(conn, getConfig(), IVersionedSpecialUnit.class, currentTurn, null, "fleetOwner = %s AND fleetName = %s AND fleetTurn = %s", vunit.getOwner(), vunit.getName(), vunit.getTurn()));
								}
							}
							return null;
						}
					});
					
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
			
			currentView.getDB().exec(new SQLiteJob<Void>()
			{
				@Override
				protected Void job(SQLiteConnection conn) throws Throwable
				{
					for(final IBuilding b : buildings)
					{
						Building.insertOrUpdate(conn, b);
					}
					
					for(final ISpaceRoad road : roads)
					{
						SpaceRoad.insertOrUpdate(conn, road);
					}
					
					for(IVersionedUnit vu : vunits)
					{
						Unit.insertOrUpdate(conn, vu);
					}
					
					for(IMovePlan m : moves)
					{
						MovePlan.insertOrUpdate(conn, m);
					}
					
					for(IFleetComposition c : comps)
					{
						FleetComposition.insertOrUpdate(conn, c);
					}
					
					for(IVersionedSpecialUnit vsu : specialsUnits)
					{
						SpecialUnit.insertOrUpdate(conn, vsu);
					}
					
					for(IVersionedUnit vu : seenUnits)
					{
						Unit.insertOrUpdate(conn, vu);
					}
					
					for(IUnitEncounterLog uel : encounterLogs)
					{
						UnitEncounterLog.insertOrUpdate(conn, uel);
					}
					
					for(String player : currentTurnGovernmentsView.keySet())
					{
						final IGovernment g = currentTurnGovernmentsView.get(player);
						if (g == null) continue;
						
						Government.insertOrUpdate(conn, g);								
					}
					
					for(String player : currentTurnDiplomaciesView.keySet())
					{
						final Set<IDiplomacy> diplomacies = currentTurnDiplomaciesView.get(player);
						if (diplomacies == null || diplomacies.isEmpty()) continue;
						
						for(IDiplomacy d : diplomacies)
						{
							Diplomacy.insertOrUpdate(conn, d);
						}							
					}
					
					return null;
				}
			});
			
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
	
	@Override
	public Set<org.axan.sep.common.Player> getPlayers() throws GameBoardException
	{
		try
		{
			return getDB().exec(new SQLiteJob<Set<org.axan.sep.common.Player>>()
			{
				@Override
				protected Set<org.axan.sep.common.Player> job(SQLiteConnection conn) throws Throwable
				{
					Set<org.axan.sep.common.Player> result = new HashSet<org.axan.sep.common.Player>();
					Set<IPlayer> players = Player.select(conn, getConfig(), IPlayer.class, null, null);
					Set<IPlayerConfig> playerConfigs = PlayerConfig.select(conn, getConfig(), IPlayerConfig.class, null, null);
					
					for(IPlayer p : players)
					{
						for(IPlayerConfig pc : playerConfigs)
						{
							if (p.getName().matches(pc.getName()))
							{
								// TODO: image, portrait...
								result.add(new org.axan.sep.common.Player(p.getName(), new org.axan.sep.common.PlayerConfig(Color.decode(pc.getColor()), null, null)));
								break;
							}
						}
					}
					
					return result;
				}
			});
		}
		catch(SQLiteDBException e)
		{
			throw new GameBoardException(e);
		}
	}
	
	@Override
	public PlayerGameBoard getPlayerGameBoard(final String playerLogin) throws GameBoardException
	{
		/*
		 * PlayerGameBoard basé sur la même DB que le serveur.
		 * Une méthode est capable d'insérer dans la DB toutes les entrées entre le tour précédent et le tour courrant visibles par le joueur.
		 * Pour compiler un playerGameBoard complet on empile à partir du tour 0, ainsi il n'y a qu'une seule méthode primitive.
		 */
		try
		{
			SEPCommonSQLiteDB clientDB = new SEPCommonSQLiteDB(getConfig());
			compilePlayerView(playerLogin, clientDB, getConfig().getTurn());
			return new SQLitePlayerGameBoard(clientDB);
		}
		catch(Exception e)
		{
			throw new GameBoardException(e);
		}
	}
	
	private static final SortedSet<ATurnResolvingEvent> resolvingEvents = new TreeSet<ATurnResolvingEvent>(); 
	
	@Override
	public SortedSet<ATurnResolvingEvent> getResolvingEvents()
	{
		synchronized(resolvingEvents)
		{
			if (resolvingEvents.isEmpty())
			{
				// TODO: Implement resolving events.
				// SUIS LA: Remettre le codage de resolveTurn à plus tard, implémenter les features dans l'ordre logique d'incrémentation (envoi gameboard vierge, création flotte, dépacement flotte, ...)
				resolvingEvents.add(new ATurnResolvingEvent(0, "GlobalResolver")
				{
					
					@Override
					public void run(SortedSet<ATurnResolvingEvent> eventsQueue, GameBoard sepDB) throws GameBoardException
					{
						try
						{
							SQLiteDB db = ((SQLiteGameBoard) sepDB).getDB();							
							
							db.exec(new SQLiteJob<Void>()
							{
								@Override
								protected Void job(SQLiteConnection conn) throws Throwable
								{
									// les flottes avec une feuille de route décollent
									SQLiteStatement stmnt = conn.prepare("SELECT * FROM VersionedFleet VF LEFT JOIN MovePlan MP USING (owner, name) GROUP BY (owner, name, MP.turn) ORDER BY priority DESC WHERE MP.turn >= VF.turn");									
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
							});
						}
						catch(SQLiteException e)
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
									SQLiteStatement stmnt = conn.prepare(String.format("SELECT U.type, * FROM Unit U LEFT JOIN	VersionedUnit VU USING (name, owner, type) LEFT JOIN	PulsarMissile PM USING (name, owner, type) LEFT JOIN	Probe P USING (name, owner, type) LEFT JOIN	AntiProbeMissile APM USING (name, owner, type) LEFT JOIN	CarbonCarrier CC USING (name, owner, type) LEFT JOIN	SpaceRoadDeliverer SRD USING (name, owner, type) LEFT JOIN	Fleet F USING (name, owner, type) LEFT JOIN	VersionedPulsarMissile VPM USING (name, owner, turn) LEFT JOIN	VersionedProbe VP USING (name, owner, turn) LEFT JOIN	VersionedAntiProbeMissile VAPM USING (name, owner, turn) LEFT JOIN	VersionedCarbonCarrier VCC USING (name, owner, turn) LEFT JOIN	VersionedFleet VF USING (name, owner, turn) WHERE VU.turn = %d AND VU.destination_x IS NOT NULL;", getConfig().getTurn()));
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
	
	@Override
	public SQLiteGameBoard build(final String playerLogin, final String celestialBodyName, final eBuildingType buildingType) throws GameBoardException
	{
		try
		{
			db.exec(new SQLiteJob<Void>()
			{
				@Override
				protected Void job(SQLiteConnection conn) throws Throwable
				{										
					// Select productive celestial body by name, last version
					Set<IVersionedProductiveCelestialBody> pcbs = ProductiveCelestialBody.selectMaxVersion(conn, getConfig(), IVersionedProductiveCelestialBody.class, null, "name = '%s'", celestialBodyName);
					
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
					SQLiteStatement stmnt = conn.prepare("TODO");
					stmnt.step();
					// celestialBody did not built anything else for the current game turn.
					if (stmnt.columnInt(0) >= getConfig().getTurn())
					{
						throw new GameBoardException("Celestial body '" + celestialBodyName + "' already in work for this turn.");
					}
					
					// Select number of buildings
					stmnt = conn.prepare("TODO");
					stmnt.step();
					// celestialBody has free slots
					if (stmnt.columnInt(0) >= pcb.getMaxSlots())
					{
						throw new GameBoardException("No more free slots on celestial body '" + celestialBodyName + "'");
					}
					
					int carbonCost = 0, populationCost = 0, nbBuilt = 0;
					
					// Select current building for the given type
					Set<IBuilding> bs = Building.selectMaxVersion(conn, getConfig(), IBuilding.class, null, null, "TOTO");
					
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
					conn.exec("TODO");
					
					// update carbon stock
					// update population stock
					// TODO
					//conn.exec(insertUpdateProductiveCelestialBodyStocksSQL(celestialBodyName, carbonCost, populationCost));
					//conn.exec(insertUpdateBuildingSQL(buildingType, celestialBodyName, nbBuilt+1));
					
					return null;
				}
			});
			
			return this;
		}
		catch(SQLiteDBException e)
		{
			throw new GameBoardException(e);
		}
	}
	
	// Private
	
	void insertCelestialBody(eCelestialBodyType celestialBodyType, String name, Location location) throws SQLiteException
	{
		boolean productiveCelestialBody = (celestialBodyType != eCelestialBodyType.Vortex);
		db.exec("INSERT INTO CelestialBody (name, location_x, location_y, location_z, type) VALUES ('%s', %d, %d, %d, '%s');", name, location.x, location.y, location.z, celestialBodyType);
		
		if (productiveCelestialBody)
		{
			// Fix carbon amount to the mean value.
			int[] carbonAmount = getConfig().getCelestialBodiesStartingCarbonAmount(celestialBodyType);
			int initialCarbon = rnd.nextInt(carbonAmount[1] - carbonAmount[0]) + carbonAmount[0];
			
			// Fix slots amount to the mean value.
			int[] slotsAmount = getConfig().getCelestialBodiesSlotsAmount(celestialBodyType);
			int maxSlots = rnd.nextInt(slotsAmount[1] - slotsAmount[0]) + slotsAmount[0];
			if (maxSlots <= 0) maxSlots = 1;			
			
			db.exec("INSERT INTO ProductiveCelestialBody (name, initialCarbonStock, maxSlots, type) VALUES ('%s', %d, %d, '%s')", name, initialCarbon, maxSlots, celestialBodyType);
		}
		
		switch(celestialBodyType)
		{
			case Vortex:
			{
				// TODO:
				throw new Protocol.SEPImplementationException("insertCelestialBody(Vortex, ...) not Implemented");
			}
			case AsteroidField:
			case Nebula:
			{
				db.exec("INSERT INTO %s (name, type) VALUES ('%s', '%s');", celestialBodyType, name, celestialBodyType);
				break;
			}
			
			case Planet:
			{
				int[] populationPerTurnRange = getConfig().getPopulationPerTurn();
				int populationPerTurn = rnd.nextInt(populationPerTurnRange[1] - populationPerTurnRange[0]) + populationPerTurnRange[0];
				
				int[] populationLimitRange = getConfig().getPopulationLimit();
				int maxPopulation = rnd.nextInt(populationLimitRange[1] - populationLimitRange[0]) + populationLimitRange[0];
				
				db.exec("INSERT INTO Planet (name, populationPerTurn, maxPopulation, type) VALUES ('%s', %d, %d, '%s');", name, populationPerTurn, maxPopulation, celestialBodyType);
				break;
			}
			
			default:
			{
				throw new Protocol.SEPImplementationException("'"+celestialBodyType+"' not implemented.");
			}
		}
	}

	void insertPlayer(org.axan.sep.common.Player player) throws SQLiteException
	{
		db.exec("INSERT INTO Player (name) VALUES ('%s');", player.getName());
		db.exec("INSERT INTO PlayerConfig (name, color, symbol, portrait) VALUES ('%s', '%s', NULL, NULL);", player.getName(), player.getConfig().getColor().getRGB());		
	}
	
	void insertArea(Location location, boolean isSun) throws SQLiteException
	{
		db.exec("INSERT INTO Area (location_x, location_y, location_z, isSun) VALUES (%d, %d, %d, %d);", location.x, location.y, location.z, isSun ? 1 : 0);
	}

	boolean areaExists(Location location) throws SQLiteException
	{
		return db.prepare("SELECT EXISTS ( SELECT location_x FROM Area WHERE location_x = %d AND location_y = %d AND location_z = %d );", new SQLiteStatementJob<Boolean>()
		{
			@Override
			public Boolean job(SQLiteStatement stmnt) throws SQLiteException
			{
				try
				{
					stmnt.step();
					return (stmnt.columnInt(0) != 0);
				}
				catch(SQLiteException e)
				{
					e.printStackTrace();
					throw e;
				}
			}
		}, location.x, location.y, location.z, location.x, location.y, location.z);
	}
	
	boolean areaHasCelestialBody(Location location) throws SQLiteException
	{
		return db.prepare("SELECT EXISTS ( SELECT name FROM CelestialBody WHERE location_x = %d AND location_y = %d AND location_z = %d ) ;", new SQLiteStatementJob<Boolean>()
		{
			@Override
			public Boolean job(SQLiteStatement stmnt) throws SQLiteException
			{
				return (stmnt.columnInt(0) != 0);
			}			
		},location.x, location.y, location.z,location.x, location.y, location.z);
	}
	
	boolean areaIsSun(Location location) throws SQLiteException
	{
		return db.prepare("SELECT isSun FROM Area WHERE location_x = %d AND location_y = %d AND location_z = %d;", new SQLiteStatementJob<Boolean>()
		{
			@Override
			public Boolean job(SQLiteStatement stmnt) throws SQLiteException
			{
				stmnt.step();
				return (stmnt.columnInt(0) != 0);
			}
		}, location.x, location.y, location.z, location.x, location.y, location.z);
	}
	
	boolean isTravellingTheSun(RealLocation a, RealLocation b) throws SQLiteException
	{
		// TODO: Optimize with a SQL request using "... IN ( ... )" as where clause.
		for(RealLocation pathStep : SEPUtils.getAllPathLoc(a, b))
		{
			if (areaExists(pathStep.asLocation()) && areaIsSun(pathStep.asLocation())) return true;
		}
		
		return false;
	}
	
	void insertStartingPlanet(String planetName, Location planetLocation, String ownerName) throws SQLiteException
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

		db.exec("INSERT INTO CelestialBody (name, location_x, location_y, location_z, type) VALUES ('%s', %d, %d, %d, '%s');", planetName, planetLocation.x, planetLocation.y, planetLocation.z, eCelestialBodyType.Planet);
		db.exec("INSERT INTO ProductiveCelestialBody (name, initialCarbonStock, maxSlots, type) VALUES ('%s', %d, %d, '%s');", planetName, carbonStock, slots, eCelestialBodyType.Planet);
		db.exec("INSERT INTO Planet (name, populationPerTurn, maxPopulation, type) VALUES ('%s', %d, %d, '%s');", planetName, populationPerTurn, populationLimit, eCelestialBodyType.Planet);
		db.exec("INSERT INTO VersionedProductiveCelestialBody (name, turn, carbonStock, currentCarbon, owner, type) VALUES ('%s', %d, %d, %d, '%s', '%s');", planetName, 0, carbonStock, getConfig().getPlayersPlanetsStartingCarbonResources(), ownerName, eCelestialBodyType.Planet);
		db.exec("INSERT INTO VersionedPlanet (name, turn, currentPopulation, type) VALUES ('%s', %d, %d, '%s');", planetName, 0, getConfig().getPlayersPlanetsStartingPopulation(), eCelestialBodyType.Planet);
		
		// If victory rule "Regimicide" is on, starting planet has a pre-built government module.	    
	    if (getConfig().isRegimicide())
		{
	    	// Buildin, GovernmentModule, Government
	    	db.exec("INSERT INTO Building (type, nbSlots, celestialBodyName, turn) VALUES ('%s', %d, '%s', %d);", eBuildingType.GovernmentModule, 1, planetName, 0);
	    	db.exec("INSERT INTO GovernmentModule (type, celestialBodyName, turn) VALUES ('%s', '%s', %d);", eBuildingType.GovernmentModule, planetName, 0);
	    	db.exec("INSERT INTO Government (owner, turn, planetName, planetTurn) VALUES ('%s', %d, '%s', %d);", ownerName, 0, planetName, 0);
		}
	}
	
	/// Tests
	
	SQLiteDB getDB()
	{
		return db;
	}
	
	/// Serialization
	
	private void writeObject(final java.io.ObjectOutputStream out) throws IOException
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
}
