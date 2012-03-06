package org.axan.sep.common.db.orm;

import java.awt.Color;
import java.beans.DesignMode;
import java.io.Externalizable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;

import javax.management.RuntimeErrorException;
import javax.management.monitor.Monitor;

import org.apache.commons.lang.ArrayUtils;
import org.axan.eplib.orm.DataBaseORMGenerator;
import org.axan.eplib.orm.nosql.DBGraphException;
import org.axan.eplib.utils.Basic;
import org.axan.eplib.utils.Compression;
import org.axan.eplib.utils.Basic.GenericHolder;
import org.axan.sep.common.GameConfigCopier;
import org.axan.sep.common.GameConfigCopier.GameConfigCopierException;
import org.axan.sep.common.Protocol;
import org.axan.sep.common.Protocol.eBuildingType;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.Rules;
import org.axan.sep.common.Rules.StarshipTemplate;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.IAntiProbeMissile;
import org.axan.sep.common.db.IAntiProbeMissileMarker;
import org.axan.sep.common.db.IArea;
import org.axan.sep.common.db.IAsteroidField;
import org.axan.sep.common.db.IBuilding;
import org.axan.sep.common.db.ICelestialBody;
import org.axan.sep.common.db.IDefenseModule;
import org.axan.sep.common.db.IExtractionModule;
import org.axan.sep.common.db.IFleet;
import org.axan.sep.common.db.IFleetMarker;
import org.axan.sep.common.db.IGameConfig;
import org.axan.sep.common.db.IGovernmentModule;
import org.axan.sep.common.db.INebula;
import org.axan.sep.common.db.IPlanet;
import org.axan.sep.common.db.IPlayer;
import org.axan.sep.common.db.IPlayerConfig;
import org.axan.sep.common.db.IProbe;
import org.axan.sep.common.db.IProbeMarker;
import org.axan.sep.common.db.IProductiveCelestialBody;
import org.axan.sep.common.db.IPulsarLaunchingPad;
import org.axan.sep.common.db.ISpaceCounter;
import org.axan.sep.common.db.IStarshipPlant;
import org.axan.sep.common.db.IUnit;
import org.axan.sep.common.db.IUnitMarker;
import org.axan.sep.common.db.IVortex;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ReturnableEvaluator;
import org.neo4j.graphdb.StopEvaluator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.TraversalPosition;
import org.neo4j.graphdb.Traverser.Order;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.kernel.EmbeddedGraphDatabase;

import scala.actors.threadpool.helpers.WaitQueue.WaitNode;
import scala.util.control.Exception.Finally;

public class SEPCommonDB implements Serializable, ListIterator<SEPCommonDB>
{
	private static final long serialVersionUID = 1L;

	public static interface ILogListener
	{
		void log(String message);
	}
	
	public static interface IAreaChangeListener
	{
		void onAreaChanged(Location location);
	}
	
	public static interface IPlayerChangeListener
	{
		void onPlayerChanged(String playerName);
	}
	
	public static enum eRelationTypes implements RelationshipType
	{
		/** Relation from ReferenceNode to Config factory node. */
		GameConfig,
		
		/** Relation from ReferenceNode to Areas factory node and from Areas factory node to Area nodes. */
		Areas,
		
		/** Relation from ReferenceNode to Sun factory node and from Sun factory node to Area nodes that are part of Sun. */
		Sun,
		
		/** Relation from ReferenceNode to Players factory node and from Players factory node to Player nodes. */
		Players,
				
		/** Relation from Area node to its CelestialBody node. */
		CelestialBody,
		
		/** 
		 * Relation from CelestialBody node to its assigned Fleets node.
		 * Relationship has a "playerName" property and each player must
		 * have only one assigned fleet per celestial body.
		 */
		AssignedFleets,
		
		/**
		 * Relation from Unit node to Area node.
		 * If the Unit is stopped, Area is its current location.
		 * If the Unit is moving, Area is its last location before it moved.
		 */
		UnitDeparture,
		
		/**
		 * Relation from Unit node to Area node.
		 * If the Unit is stopped, it shall not have any UnitDestination relationship.
		 * If the Unit is moving, Area is its destination location.
		 */
		UnitDestination,
		
		/** Relation from UnitMarker or Unit node to Area node. */
		UnitMarkerRealLocation,
		
		/** Relation from ProductiveCelestialBody node to Building nodes. */
		Buildings,
		
		/**
		 * Relation from Player node to GovernmentModule node or Fleet node (which include GovernmentalStarship).
		 * One relation per player.
		 */		
		PlayerGovernment,
		
		/** Relation from Player node to its PlayerConfig node. */
		PlayerConfig,
		
		/** Relation from Player node to owned CelestialBody nodes. */
		PlayerCelestialBodies,
		// eCelestialBodyType value
		
		/** Relation from Player node to owned Unit nodes. */
		PlayerUnit,
		// eUnitType value
		
		/** Relation from Player node to owned UnitMarker node. */
		PlayerUnitMarker,
		// eUnitType value+"Marker" dynamic relationship type
		
		/** Relation from SpaceCounter node (source, i.e. builder) to SpaceCounter node (destination). */
		SpaceRoad,
		
		/** Relation from AntiProbeMissile node to Probe node (target) */
		//AntiProbeMissileTarget, NOT USED
		
		/** Relation from Unit node to UnitMarker nodes */
		UnitEncounterLog,
		
		/** Relation from Player node to EncounterLog nodes */
		PlayerEncounterLog,
		
		/** Diplomacy from Player node to Player nodes (each player is connected to every players except itself, strictly once), from diplomacy owner to diplomacy target. */
		PlayerDiplomacy,
		
		/** Diplomacy marker from Player nodes to Player nodes (only one marker per turn per owner/target pair). */
		PlayerDiplomacyMarker
	}

	private static class GameConfigInvocationHandler implements InvocationHandler
	{
		private final SEPCommonDB sepDB;
		private GraphDatabaseService db;
		private Node gameConfigNode;

		public GameConfigInvocationHandler(SEPCommonDB sepDB)
		{
			this.sepDB = sepDB;
			checkForUpdate();
		}

		private void checkForUpdate()
		{
			if (db == null || !db.equals(sepDB.getConfigDB()))
			{
				db = sepDB.getConfigDB();

				Relationship r = db.getReferenceNode().getSingleRelationship(eRelationTypes.GameConfig, Direction.OUTGOING);
				if (r == null)
				{
					Transaction tx = db.beginTx();
					try
					{
						gameConfigNode = db.createNode();
						db.getReferenceNode().createRelationshipTo(gameConfigNode, eRelationTypes.GameConfig);
						tx.success();
					}
					finally
					{
						tx.finish();
					}
				}
				else
				{
					gameConfigNode = r.getEndNode();
				}
			}
		}

		@Override
		public Object invoke(Object proxy, final Method method, Object[] args) throws Throwable
		{
			if (method.getDeclaringClass().equals(Object.class))
			{
				return method.invoke(this, args);
			}
			else if (!method.getDeclaringClass().equals(IGameConfig.class))
			{
				throw new Protocol.SEPImplementationError("GameConfigInvocationHandler must be used with IGameConfig.class proxy.");
			}
			else
			{
				checkForUpdate();
				
				Class<?> returnType = method.getReturnType();

				// Special case: setTurn
				if (method.getName().equals("setTurn"))
				{
					if (args == null || args.length != 1 || !Integer.class.isInstance(args[0]))
					{
						throw new RuntimeException(method.getName() + " invalid call.");
					}

					int value = (Integer) args[0];

					if (value < 0)
					{
						throw new RuntimeException(method.getName() + " invalid call, turn cannot be negative.");
					}

					if (sepDB.nextVersion != null)
					{
						throw new RuntimeException(method.getName() + " invalid call, next version already exists.");
					}

					if (value > 0)
					{
						int currentTurn = sepDB.getConfig().getTurn();

						if (currentTurn == value) // No change
						{
							return null;
						}

						if (value != currentTurn + 1)
						{
							throw new RuntimeException(method.getName() + " invalid call, must increment turn by 1 every time you call this method.");
						}

						synchronized(sepDB)
						{
							//sepDB.nextVersion = Basic.clone(sepDB);
							sepDB.nextVersion = SEPCommonDB.clone(sepDB, 1, value)[0];
							sepDB.nextVersion.previousVersion = sepDB;							
						}

						return null;
					}
					else
					// value == 0						
					{
						if (sepDB.previousVersion != null)
						{
							throw new RuntimeException("Cannot set game turn to 0 because SEPCommonDB already has previous version");
						}

						// setTurn as classic setter, do not generates next version
					}
				}

				/*
				 * Setters must start with "set", have arguments and return void.
				 */
				if (method.getName().startsWith("set") && method.getReturnType().equals(void.class) && args != null && args.length > 0)
				{
					// Setter
					String key = method.getName().substring(3);

					int i;
					for(i = 0; i < args.length - 1; ++i)
					{
						if (Enum.class.isInstance(args[i]))
						{
							key += '-' + args[i].toString();
						}
						else
						{
							break;
						}
					}

					Object value = null;

					if (i + 1 == args.length)
					{
						//value = args[i] == null ? "NULL" : args[i].toString();
						value = args[i];
						set(key, value);
					}
					else
					{
						/*
						for(int j=0; i+j < args.length; ++j)
						{
							value = args[i+j] == null ? "NULL" : args[i+j].toString();
							set(String.format("%s-%s", key, j), value);
						}
						*/

						Object arr = Array.newInstance(getPrimitive(args[i].getClass()), args.length - i);
						for(int j = 0; i + j < args.length; ++j)
						{
							Array.set(arr, j, args[i + j]);
						}
						set(key, arr);
					}

					return null;
				}
				/*
				 * Getters must do not return void and have only Enum<?> arguments.
				 */
				else if (!void.class.equals(returnType))
				{
					// Getter
					String key = method.getName();
					if (method.getName().startsWith("get") || method.getName().startsWith("has"))
					{
						key = method.getName().substring(3);
					}
					else if (method.getName().startsWith("is"))
					{
						key = method.getName().substring(2);
					}

					if (args != null)
						for(int i = 0; i < args.length; ++i)
						{
							if (!Enum.class.isInstance(args[i]))
							{
								throw new Protocol.SEPImplementationError("Invalid IGameConfig: Bad argument type in method (all arguments must be Enum<?>): " + method.toGenericString());
							}

							key += '-' + args[i].toString();
						}

					Object result = (gameConfigNode.hasProperty(key)) ? gameConfigNode.getProperty(key) : null;
					
					if (result == null && returnType.isPrimitive())
					{
						result = gameConfigNode.getProperty(key);
					}
					
					if (returnType.isArray() && returnType.getComponentType().isEnum())
					{
						Object arr = Array.newInstance(returnType.getComponentType(), Array.getLength(result));
						for(int i = 0; i < Array.getLength(result); ++i)
						{
							Array.set(arr, i, Enum.valueOf(returnType.getComponentType().asSubclass(Enum.class), (String) Array.get(result, i)));
						}
						
						return arr;
					}
					
					if (returnType.isEnum())
					{
						return Enum.valueOf(returnType.asSubclass(Enum.class), (String) result);
					}					
					
					return result;
				}
				else
				{
					throw new Protocol.SEPImplementationError("Invalid IGameConfig: Cannot recognize getter nor setter in method: " + method.toGenericString());
				}
			}
		}

		private void set(String key, Object value) throws InterruptedException
		{
			Transaction tx = db.beginTx();
			try
			{
				if (value == null)
				{
					gameConfigNode.removeProperty(key);
				}
				else
				{
					if (value.getClass().isEnum())
					{
						value = value.toString();
					}
					
					gameConfigNode.setProperty(key, value);
				}
				tx.success();
			}
			finally
			{
				tx.finish();
			}
		}
	}

	private static Class<?> getPrimitive(Class<?> wrapper)
	{
		if (wrapper.isPrimitive())
			return wrapper;

		if (wrapper == Byte.class)
			return byte.class;
		if (wrapper == Short.class)
			return short.class;
		if (wrapper == Integer.class)
			return int.class;
		if (wrapper == Long.class)
			return long.class;
		if (wrapper == Float.class)
			return float.class;
		if (wrapper == Double.class)
			return double.class;
		if (wrapper == Boolean.class)
			return boolean.class;
		if (wrapper == Character.class)
			return char.class;
		if (wrapper == String.class)
			return String.class;

		return wrapper;
	}

	////////////////////////////////////////////////

	private transient IGameConfig config;

	private transient GraphDatabaseService db;
	private transient int cacheVersion;
	
	private transient Index<Node> playerIndex;
	private transient Index<Node> areaIndex;
	private transient Index<Node> celestialBodyIndex;
	private transient Index<Node> vortexIndex;
	private transient Index<Node> productiveCelestialBodyIndex;
	private transient Index<Node> nebulaIndex;
	private transient Index<Node> asteroidFieldIndex;
	private transient Index<Node> planetIndex;
	private transient Index<Node> buildingIndex;
	private transient Index<Node> unitIndex;
	private transient Index<Node> probeIndex;
	private transient Index<Node> antiProbeMissileIndex;
	private transient Index<Node> unitMarkerIndex;

	private transient Node playersFactory;
	private transient Node areasFactory;
	private transient Node sun;
	
	private transient Set<IAreaChangeListener> areaChangeListeners;
	private transient Set<ILogListener> logListeners;
	private transient Set<IPlayerChangeListener> playerChangeListeners;

	private SEPCommonDB previousVersion = null;
	private SEPCommonDB nextVersion = null;
	
	private transient GenericHolder<Boolean> initFlag;
	private transient int gameTurn;
	
	// lazy clone ctor
	private SEPCommonDB(GenericHolder<Boolean> initFlag, int gameTurn)
	{
		this.initFlag = initFlag;
		this.gameTurn = gameTurn;
	}
	
	public SEPCommonDB(GraphDatabaseService db, IGameConfig config) throws IOException, GameConfigCopierException, InterruptedException
	{
		init(db);
		GameConfigCopier.copy(IGameConfig.class, config, this.config);
	}
	
	public boolean needToRefreshCache(int cacheVersion)
	{
		return this.cacheVersion != getCacheVersion();
	}
	
	public int getCacheVersion()
	{
		waitForInit();
		return cacheVersion;
	}
	
	private int incCacheVersion()
	{
		waitForInit();
		return ++cacheVersion;
	}

	public void addLogListener(ILogListener listener)
	{
		waitForInit();
		logListeners.add(listener);
	}
	
	public void removeLogListener(ILogListener listener)
	{
		waitForInit();
		logListeners.remove(listener);
	}
	
	public void fireLog(String message)
	{
		for(ILogListener listener : logListeners)
		{
			listener.log(message);
		}
	}
	
	public void addPlayerChangeListener(IPlayerChangeListener listener)
	{
		waitForInit();
		playerChangeListeners.add(listener);
	}
	
	public void removePlayerChangeListener(IPlayerChangeListener listener)
	{
		waitForInit();
		playerChangeListeners.remove(listener);
	}
	
	public void firePlayerChangeEvent(String playerName)
	{
		incCacheVersion();
		
		for(IPlayerChangeListener listener : playerChangeListeners)
		{
			listener.onPlayerChanged(playerName);
		}
	}
	
	public void addAreaChangeListener(IAreaChangeListener listener)
	{
		waitForInit();
		areaChangeListeners.add(listener);
	}
	
	public void removeAreaChangeListener(IAreaChangeListener listener)
	{
		waitForInit();
		areaChangeListeners.remove(listener);
	}
	
	public void fireAreaChangedEvent(Location location)
	{
		incCacheVersion();
		
		for(IAreaChangeListener listener : areaChangeListeners)
		{
			listener.onAreaChanged(location);
		}
	}
	
	public IGameConfig getConfig()
	{
		waitForInit();
		return this.config;
	}

	private GraphDatabaseService getConfigDB()
	{
		//waitForInit();
		if (initFlag == null) return db;
		synchronized(initFlag)
		{
			return db;
		}
	}

	public synchronized GraphDatabaseService getDB()
	{
		waitForInit();
		return db;
	}

	//////////////// Querying interface

	/**
	 * Return true if universe already created (actually if at least one Area
	 * has been created).
	 * 
	 * @return
	 */
	public boolean isUniverseCreated()
	{
		waitForInit();
		return areasFactory.hasRelationship(eRelationTypes.Areas, Direction.OUTGOING);
	}

	public Set<String> getPlayersNames()
	{
		waitForInit();
		Set<String> playersNames = new HashSet<String>();
		for(Node n: playersFactory.traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, ReturnableEvaluator.ALL_BUT_START_NODE, eRelationTypes.Players, Direction.OUTGOING))
		{
			playersNames.add((String) n.getProperty("name"));
		}

		return playersNames;
	}
	
	public Set<IPlayer> getPlayers()
	{
		waitForInit();
		Set<IPlayer> players = new HashSet<IPlayer>();
		for(String playerName : getPlayersNames())
		{
			players.add(new Player(this, playerName));
		}
		
		return players;
	}

	public IPlayer getPlayer(String name)
	{
		waitForInit();
		Player player = new Player(this, name);
		return player.exists() ? player : null;
	}

	/**
	 * Return a Planet pojo instance of the given player starting planet, but
	 * connected to current DB.
	 * 
	 * @param playerName
	 * @return
	 * @throws SQLDataBaseException
	 */
	public IPlanet getStartingPlanet(String playerName)
	{
		waitForInit();
		SEPCommonDB db = this;
		while (db.getConfig().getTurn() > 1)
		{
			db = db.previous();
		}
		Iterator<Node> it = db.playerIndex.get(Player.PK, Player.getPK(playerName)).getSingle().traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, new ReturnableEvaluator()
		{			
			@Override
			public boolean isReturnableNode(TraversalPosition currentPos)
			{
				if (currentPos.isStartNode()) return false;
				Node node = currentPos.currentNode();
				if (!node.hasProperty("type")) return false;
				if (!((String) node.getProperty("type")).equals(eCelestialBodyType.Planet.toString())) return false;
				return true;
			}
		}, eRelationTypes.PlayerCelestialBodies, Direction.OUTGOING).iterator();
		if (!it.hasNext())
		{
			return null;
		}
		return new Planet(this, (String) it.next().getProperty("name"));
	}
	
	/**
	 * Return list of all already created areas.
	 * @return
	 */
	public Set<IArea> getAreas()
	{
		waitForInit();
		Set<IArea> result = new HashSet<IArea>();
		for(Node n : areasFactory.traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, ReturnableEvaluator.ALL_BUT_START_NODE, eRelationTypes.Areas, Direction.OUTGOING))
		{
			Location location = Location.valueOf((String) n.getProperty("location"));
			result.add(getArea(location));
		}
		return result;
	}

	/*
	public Set<IArea> getAreasByZ(int z)
	{
		waitForInit();
		Map<String, IArea> areas = new HashMap<String, IArea>();
		
		// Lucene reserved characters: + - && || ! ( ) { } [ ] ^ " ~ * ? : \
		for(Node n: areaIndex.query("location", String.format("\\[*;%d\\]", z)))
		{
			areas.put((String) n.getProperty("location"), getArea(Location.valueOf((String) n.getProperty("location"))));
		}
		
		for(Location sunArea : Rules.getSunAreasByZ(getConfig(), z))
		{
			if (!areas.containsKey(sunArea.toString()))
			{
				areas.put(sunArea.toString(), getArea(sunArea));
			}
		}
		
		return new HashSet<IArea>(areas.values());
	}
	*/

	//////////////// CRUD: Create

	public IPlayer createPlayer(IPlayer player)
	{
		waitForInit();
		return createPlayer(player.getName(), player.getConfig().getColor(), player.getConfig().getSymbol(), player.getConfig().getPortrait());
	}

	public IPlayer createPlayer(String name, Color configColor, String configSymbol, String configPortrait)
	{
		waitForInit();
		Player result = new Player(name, new PlayerConfig(configColor, configSymbol, configPortrait));
		result.create(this);
		return result;
	}

	static public IPlayer makePlayer(String name, IPlayerConfig config)
	{
		return new Player(name, config);
	}

	static public IPlayerConfig makePlayerConfig(Color color, String symbol, String portrait)
	{
		return new PlayerConfig(color, symbol, portrait);
	}

	/*
	public IArea createArea(IArea area)
	{
		return createArea(area.getLocation(), area.isSun());
	}
	*/

	/**
	 * Create sun area. To create non-sun area, just call {@link #getArea(Location)}.
	 * @param location
	 * @return
	 *
	public IArea createSun(Location location)
	{
		waitForInit();
		Area result = new Area(location, true);
		result.create(this);
		return result;
	}
	*/
	
	/**
	 * Return given location area. If area does not exist in DB, it is created.
	 * @param location
	 * @return
	 */
	public IArea getArea(Location location)
	{
		waitForInit();
		Area area = new Area(this, location);
		
		synchronized(Area.class)
		{
			if (area.exists()) return area;
			
			area = new Area(location);
			area.create(this);
		}
		
		return area;
	}
	
	/**
	 * Return the given location area if it is already exists in DB, or null if not.
	 * @param location
	 * @return
	 *
	public IArea getArea(Location location)
	{
		Area area = new Area(this, location);
		return area.exists() ? area : null;
	}
	*/

	public IVortex createVortex(IVortex vortex)
	{
		waitForInit();
		Vortex result = new Vortex(vortex.getName(), vortex.getLocation(), vortex.getBirth(), vortex.getDeath());
		result.create(this);
		return result;
	}

	public IVortex getVortex(String name)
	{
		waitForInit();
		Vortex vortex = new Vortex(this, name);
		return vortex.exists() ? vortex : null;
	}

	static public IVortex makeVortex(String name, Location location, int birth, int death)
	{
		return new Vortex(name, location, birth, death);
	}

	public IAsteroidField createAsteroidField(IAsteroidField asteroidField)
	{
		waitForInit();
		AsteroidField result = new AsteroidField(asteroidField.getName(), asteroidField.getLocation(), asteroidField.getInitialCarbonStock(), asteroidField.getMaxSlots(), asteroidField.getCarbonStock(), asteroidField.getCurrentCarbon());
		result.create(this);
		return result;
	}

	public IAsteroidField getAsteroidField(String name)
	{
		waitForInit();
		AsteroidField asteroidField = new AsteroidField(this, name);
		return asteroidField.exists() ? asteroidField : null;
	}

	static public IAsteroidField makeAsteroidField(String name, Location location, int initialCarbonStock, int maxSlots, int carbonStock, int currentCarbon)
	{
		return new AsteroidField(name, location, initialCarbonStock, maxSlots, carbonStock, currentCarbon);
	}

	public INebula createNebula(INebula nebula)
	{
		waitForInit();
		Nebula result = new Nebula(nebula.getName(), nebula.getLocation(), nebula.getInitialCarbonStock(), nebula.getMaxSlots(), nebula.getCarbonStock(), nebula.getCurrentCarbon());
		result.create(this);
		return result;
	}

	public INebula getNebula(String name)
	{
		waitForInit();
		Nebula nebula = new Nebula(this, name);
		return nebula.exists() ? nebula : null;
	}

	static public INebula makeNebula(String name, Location location, int initialCarbonStock, int maxSlots, int carbonStock, int currentCarbon)
	{
		return new Nebula(name, location, initialCarbonStock, maxSlots, carbonStock, currentCarbon);
	}

	public IPlanet createPlanet(IPlanet planet)
	{
		waitForInit();
		Planet result = new Planet(planet.getName(), planet.getLocation(), planet.getInitialCarbonStock(), planet.getMaxSlots(), planet.getCarbonStock(), planet.getCurrentCarbon(), planet.getPopulationPerTurn(), planet.getMaxPopulation(), planet.getCurrentPopulation());
		result.create(this);
		return result;
	}

	public IPlanet getPlanet(String name)
	{
		waitForInit();
		Planet planet = new Planet(this, name);
		return planet.exists() ? planet : null;
	}

	static public IPlanet makePlanet(String name, Location location, int initialCarbonStock, int maxSlots, int carbonStock, int currentCarbon, int populationPerTurn, int maxPopulation, int currentPopulation)
	{
		return new Planet(name, location, initialCarbonStock, maxSlots, carbonStock, currentCarbon, populationPerTurn, maxPopulation, currentPopulation);
	}

	public ICelestialBody createCelestialBody(ICelestialBody celestialBody)
	{
		waitForInit();
		switch (celestialBody.getType())
		{
			case Vortex:
				return createVortex((IVortex) celestialBody);
			case AsteroidField:
				return createAsteroidField((IAsteroidField) celestialBody);
			case Nebula:
				return createNebula((INebula) celestialBody);
			case Planet:
				return createPlanet((IPlanet) celestialBody);
			default:
			{
				throw new RuntimeException("Unknown CelestialBody subtype '" + celestialBody.getType() + "'.");
			}
		}
	}

	/**
	 * Return set of all productives celestial bodies
	 * @return
	 */
	public Set<IProductiveCelestialBody> getProductiveCelestialBodies()
	{
		waitForInit();
		Set<IProductiveCelestialBody> result = new HashSet<IProductiveCelestialBody>();
		for(Node n : productiveCelestialBodyIndex.query(CelestialBody.PK, CelestialBody.queryAll()))
		{
			result.add((IProductiveCelestialBody) getCelestialBody((String) n.getProperty("name")));
		}
		
		return result;
	}	
	
	public ICelestialBody getCelestialBody(String name)
	{
		waitForInit();
		IndexHits<Node> hit = celestialBodyIndex.get(CelestialBody.PK, CelestialBody.getPK(name));
		if (!hit.hasNext()) return null;
		
		Node n = hit.getSingle();
		
		SEPCommonDB.assertProperty(n, "type");
		eCelestialBodyType type = eCelestialBodyType.valueOf((String) n.getProperty("type"));

		switch (type)
		{
			case Vortex:
				return new Vortex(this, name);
			case AsteroidField:
				return new AsteroidField(this, name);
			case Nebula:
				return new Nebula(this, name);
			case Planet:
				return new Planet(this, name);
			default:
			{
				throw new RuntimeException("Unknown CelestialBody subtype '" + type + "'.");
			}
		}
	}
	
	public Set<String> getCelestialBodiesNames()
	{
		waitForInit();
		Set<String> result = new HashSet<String>();
		for(Node n : celestialBodyIndex.query(CelestialBody.PK, CelestialBody.queryAll()))
		{
			result.add((String) n.getProperty("name"));
		}
		return result;
	}

	public IDefenseModule createDefenseModule(IDefenseModule defenseModule)
	{
		waitForInit();
		DefenseModule result = new DefenseModule(defenseModule.getProductiveCelestialBodyName(), defenseModule.getBuiltDate(), defenseModule.getNbSlots());
		result.create(this);
		return result;
	}

	public static IDefenseModule makeDefenseModule(String productiveCelestialBodyName, int builtDate, int nbSlots)
	{
		return new DefenseModule(productiveCelestialBodyName, builtDate, nbSlots);
	}

	public IExtractionModule createExtractionModule(IExtractionModule extractionModule)
	{
		waitForInit();
		ExtractionModule result = new ExtractionModule(extractionModule.getProductiveCelestialBodyName(), extractionModule.getBuiltDate(), extractionModule.getNbSlots());
		result.create(this);
		return result;
	}

	public static IExtractionModule makeExtractionModule(String productiveCelestialBodyName, int builtDate, int nbSlots)
	{
		return new ExtractionModule(productiveCelestialBodyName, builtDate, nbSlots);
	}

	public IGovernmentModule createGovernmentModule(IGovernmentModule governmentModule)
	{
		waitForInit();
		GovernmentModule result = new GovernmentModule(governmentModule.getProductiveCelestialBodyName(), governmentModule.getBuiltDate(), governmentModule.getNbSlots());
		result.create(this);
		return result;
	}

	public static IGovernmentModule makeGovernmentModule(String productiveCelestialBodyName, int builtDate, int nbSlots)
	{
		return new GovernmentModule(productiveCelestialBodyName, builtDate, nbSlots);
	}

	public IPulsarLaunchingPad createPulsarLaunchingPad(IPulsarLaunchingPad pulsarLaunchingPad)
	{
		waitForInit();
		PulsarLaunchingPad result = new PulsarLaunchingPad(pulsarLaunchingPad.getProductiveCelestialBodyName(), pulsarLaunchingPad.getBuiltDate(), pulsarLaunchingPad.getNbSlots());
		result.create(this);
		return result;
	}

	public static IPulsarLaunchingPad makePulsarLaunchingPad(String productiveCelestialBodyName, int builtDate, int nbSlots)
	{
		return new PulsarLaunchingPad(productiveCelestialBodyName, builtDate, nbSlots);
	}

	public ISpaceCounter createSpaceCounter(ISpaceCounter spaceCounter)
	{
		waitForInit();
		SpaceCounter result = new SpaceCounter(spaceCounter.getProductiveCelestialBodyName(), spaceCounter.getBuiltDate(), spaceCounter.getNbSlots());
		result.create(this);
		return result;
	}

	public static ISpaceCounter makeSpaceCounter(String productiveCelestialBodyName, int builtDate, int nbSlots)
	{
		return new SpaceCounter(productiveCelestialBodyName, builtDate, nbSlots);
	}

	public IStarshipPlant createStarshipPlant(IStarshipPlant starshipPlant)
	{
		waitForInit();
		StarshipPlant result = new StarshipPlant(starshipPlant.getProductiveCelestialBodyName(), starshipPlant.getBuiltDate(), starshipPlant.getNbSlots());
		result.create(this);
		return result;
	}

	public static IStarshipPlant makeStarshipPlant(String productiveCelestialBodyName, int builtDate, int nbSlots)
	{
		return new StarshipPlant(productiveCelestialBodyName, builtDate, nbSlots);
	}
	
	public IBuilding createBuilding(String productiveCelestialBodyName, int builtDate, eBuildingType buildingType)
	{
		waitForInit();
		switch (buildingType)
		{
			case DefenseModule:
				return createDefenseModule(new DefenseModule(productiveCelestialBodyName, builtDate, 1));
			case ExtractionModule:
				return createExtractionModule(new ExtractionModule(productiveCelestialBodyName, builtDate, 1));
			case GovernmentModule:
				return createGovernmentModule(new GovernmentModule(productiveCelestialBodyName, builtDate, 1));
			case PulsarLaunchingPad:
				return createPulsarLaunchingPad(new PulsarLaunchingPad(productiveCelestialBodyName, builtDate, 1));
			case SpaceCounter:
				return createSpaceCounter(new SpaceCounter(productiveCelestialBodyName, builtDate, 1));
			case StarshipPlant:
				return createStarshipPlant(new StarshipPlant(productiveCelestialBodyName, builtDate, 1));
			default:
			{
				throw new RuntimeException("Unknown Building type '" + buildingType + "'.");
			}
		}
	}
	
	/*
	 * Create building base from given building.
	 * @param building
	 * @return
	 /
	public IBuilding createBuilding(IBuilding building)
	{
		waitForInit();
		switch (building.getType())
		{
			case DefenseModule:
				return createDefenseModule((IDefenseModule) building);
			case ExtractionModule:
				return createExtractionModule((IExtractionModule) building);
			case GovernmentModule:
				return createGovernmentModule((IGovernmentModule) building);
			case PulsarLaunchingPad:
				return createPulsarLaunchingPad((IPulsarLaunchingPad) building);
			case SpaceCounter:
				return createSpaceCounter((ISpaceCounter) building);
			case StarshipPlant:
				return createStarshipPlant((IStarshipPlant) building);
			default:
			{
				throw new RuntimeException("Unknown Building type '" + building.getType() + "'.");
			}
		}
	}
	*/
	
	public void deleteBuilding(String productiveCelestialBodyName, eBuildingType type)
	{
		waitForInit();
		IBuilding building = getBuilding(productiveCelestialBodyName, type);
		
	}

	/**
	 * Return building identified by given productiveCelestialBodyName and type, or null if such building does not exist.
	 * @param productiveCelestialBodyName
	 * @param type
	 * @return
	 */
	public IBuilding getBuilding(String productiveCelestialBodyName, eBuildingType type)
	{
		waitForInit();
		if (!buildingIndex.get(Building.PK, Building.getPK(productiveCelestialBodyName, type)).hasNext())
		{
			return null;
		}
		
		switch (type)
		{
			case DefenseModule:
				return new DefenseModule(this, productiveCelestialBodyName);
			case ExtractionModule:
				return new ExtractionModule(this, productiveCelestialBodyName);
			case GovernmentModule:
				return new GovernmentModule(this, productiveCelestialBodyName);
			case PulsarLaunchingPad:
				return new PulsarLaunchingPad(this, productiveCelestialBodyName);
			case SpaceCounter:
				return new SpaceCounter(this, productiveCelestialBodyName);
			case StarshipPlant:
				return new StarshipPlant(this, productiveCelestialBodyName);
			default:
			{
				throw new RuntimeException("Unknown Building type '" + type + "'.");
			}
		}
	}	

	public IFleet createFleet(IFleet fleet)
	{
		waitForInit();
		Fleet result = new Fleet(fleet.getOwnerName(), fleet.getName(), fleet.getInitialDepartureName(), fleet.getStarships());
		result.create(this);
		return result;
	}
	
	public IFleet getFleet(String ownerName, String name)
	{
		waitForInit();
		Fleet fleet = new Fleet(this, ownerName, name);
		return fleet.exists() ? fleet : null;
	}

	public static IFleet makeFleet(String ownerName, String name, String productiveCelestialBodyName, Map<StarshipTemplate, Integer> starships)
	{
		return new Fleet(ownerName, name, productiveCelestialBodyName, starships);
	}
	
	public IProbe createProbe(IProbe probe)
	{
		waitForInit();
		Probe result = new Probe(probe.getOwnerName(), probe.getSerieName(), probe.getSerialNumber(), probe.getInitialDepartureName());
		result.create(this);
		return result;
	}
	
	public IProbe getProbe(String ownerName, String name)
	{
		waitForInit();
		Probe probe = new Probe(this, ownerName, name);
		return probe.exists() ? probe : null;
	}
	
	public List<IProbe> getProbeSerie(String ownerName, String serieName)
	{
		waitForInit();
		List<IProbe> result = new LinkedList<IProbe>();
		IndexHits<Node> hit = probeIndex.query(Unit.PK, Probe.querySeriePK(ownerName, serieName));
		if (!hit.hasNext()) return result;
		
		for(Node n : hit)
		{
			result.add(getProbe(ownerName, (String) n.getProperty("name")));
		}
		
		Collections.sort(result, new Comparator<IProbe>()
		{
			@Override
			public int compare(IProbe o1, IProbe o2)
			{
				return ((Integer) o1.getSerialNumber()).compareTo(o2.getSerialNumber());
			}
		});
		
		return result;
	}

	public static IProbe makeProbe(String ownerName, String serieName, int serialNumber, String productiveCelestialBodyName)
	{
		return new Probe(ownerName, serieName, serialNumber, productiveCelestialBodyName);
	}
	
	public IAntiProbeMissile createAntiProbeMissile(IAntiProbeMissile antiProbeMissile)
	{
		waitForInit();
		AntiProbeMissile result = new AntiProbeMissile(antiProbeMissile.getOwnerName(), antiProbeMissile.getSerieName(), antiProbeMissile.getSerialNumber(), antiProbeMissile.getInitialDepartureName());
		result.create(this);
		return result;
	}
	
	public IAntiProbeMissile getAntiProbeMissile(String ownerName, String name)
	{
		waitForInit();
		AntiProbeMissile antiProbeMissile = new AntiProbeMissile(this, ownerName, name);
		return antiProbeMissile.exists() ? antiProbeMissile : null;
	}
	
	public List<IAntiProbeMissile> getAntiProbeMissileSerie(String ownerName, String serieName)
	{
		waitForInit();
		List<IAntiProbeMissile> result = new LinkedList<IAntiProbeMissile>();
		IndexHits<Node> hit = antiProbeMissileIndex.query(Unit.PK, AntiProbeMissile.querySeriePK(ownerName, serieName));
		if (!hit.hasNext()) return result;
		
		for(Node n : hit)
		{
			result.add(getAntiProbeMissile(ownerName, (String) n.getProperty("name")));
		}
		
		Collections.sort(result, new Comparator<IAntiProbeMissile>()
		{
			@Override
			public int compare(IAntiProbeMissile o1, IAntiProbeMissile o2)
			{
				return ((Integer) o1.getSerialNumber()).compareTo(o2.getSerialNumber());
			}
		});
		
		return result;
	}

	public static IAntiProbeMissile makeAntiProbeMissile(String ownerName, String serieName, int serialNumber, String productiveCelestialBodyName)
	{
		return new AntiProbeMissile(ownerName, serieName, serialNumber, productiveCelestialBodyName);
	}
	
	public Set<IUnit> getUnits()
	{
		waitForInit();
		
		Set<IUnit> result = new HashSet<IUnit>();
		for(IPlayer player : getPlayers())
		{
			result.addAll(player.getUnits(null));
		}
		
		return result;
	}	
	
	/**
	 * Return unit identified by given ownerName, name and type. Return null if such unit does not exist.
	 * type may be null if any unit type is accepted.
	 * @param ownerName
	 * @param name
	 * @param type
	 * @return
	 */
	public IUnit getUnit(String ownerName, String name, eUnitType type)
	{
		waitForInit();
		IndexHits<Node> hit = unitIndex.get(Unit.PK, Unit.getPK(ownerName, name));
		if (!hit.hasNext())
		{
			return null;
		}
		
		Node n = hit.next(); 		
		if (type != null && (!n.hasProperty("type") || !type.toString().equals((String) n.getProperty("type"))))
		{
			return null;
		}
		
		SEPCommonDB.assertProperty(n, "type");
		if (type == null) type = eUnitType.valueOf((String) n.getProperty("type"));
				
		switch (type)
		{
			case Fleet:
				return new Fleet(this, ownerName, name);
			case Probe:
				return new Probe(this, ownerName, name);
			case AntiProbeMissile:
				return new AntiProbeMissile(this, ownerName, name);
			default:
			{
				throw new RuntimeException("Unknown Unit type '" + type + "'.");
			}
		}
	}
	
	public Set<IUnitMarker> getUnitsMarkers()
	{
		waitForInit();
		
		Set<IUnitMarker> result = new HashSet<IUnitMarker>();
		for(IPlayer player : getPlayers())
		{
			result.addAll(player.getUnitsMarkers(null));
		}
		
		return result;
	}
	
	/**
	 * Return unit marker identified by given ownerName, name and type. Return null if such unit marker does not exist.
	 * type may be null if any unit type is accepted.
	 * @param ownerName
	 * @param name
	 * @param type
	 * @return
	 */
	public IUnitMarker getUnitMarker(int turn, double step, String ownerName, String name, eUnitType type)
	{
		waitForInit();
		IndexHits<Node> hit = unitMarkerIndex.get(UnitMarker.PK, UnitMarker.getPK(turn, step, ownerName, name));
		if (!hit.hasNext())
		{
			return null;
		}
		
		Node n = hit.next();
		if (type != null && (!n.hasProperty("type") || !type.toString().equals((String) n.getProperty("type"))))
		{
			return null;
		}
		
		if (type == null) type = eUnitType.valueOf((String) n.getProperty("type"));
				
		switch (type)
		{
			case Fleet:
				return new FleetMarker(this, turn, step, ownerName, name);
			case Probe:
				return new ProbeMarker(this, turn, step, ownerName, name);
			case AntiProbeMissile:
				return new AntiProbeMissileMarker(this, turn, step, ownerName, name);
			default:
			{
				throw new RuntimeException("Unknown Unit type '" + type + "'.");
			}
		}
	}
	
	/**
	 * Create given unitMarker in current DB if not exists.
	 * If the marker already exists, assert it is strictly the same.
	 * Warning: If unitMarker is a IUnit instance, be aware that step 0 marker is used.
	 * To use another step you must explicitly call {@link IUnit#getMarker(double)}.
	 * @param unitMarker
	 * @return the UnitMarker object connected to current DB.
	 */
	public <T extends IUnitMarker> T createUnitMarker(IUnitMarker unitMarker)
	{
		if (IUnit.class.isInstance(unitMarker)) throw new IllegalArgumentException("unitMarker must not implement IUnit, use IUnit.getMarker(step) to get a pure IUnitMarker object.");
		
		waitForInit();		
		
		String name = unitMarker.getName();
		if (name.contains("assigned"))
		{
			name = name;
		}
		IUnitMarker existingMarker = getUnitMarker(unitMarker.getTurn(), unitMarker.getStep(), unitMarker.getOwnerName(), unitMarker.getName(), unitMarker.getType());
		if (existingMarker != null)
		{
			if (!existingMarker.equals(unitMarker))
			{
				throw new DBGraphException("Unit marker already exist and is not equal");
			}
			
			return (T) existingMarker;
		}
		
		UnitMarker result;
		
		switch (unitMarker.getType())
		{
			case Fleet:
			{
				IFleetMarker fleetMarker = (IFleetMarker) unitMarker;
				result = new FleetMarker(fleetMarker.getTurn(), fleetMarker.getStep(), fleetMarker.getOwnerName(), fleetMarker.getName(), fleetMarker.isStopped(), fleetMarker.getRealLocation(), fleetMarker.getSpeed(), fleetMarker.getStarships(), fleetMarker.isAssignedFleet());
				break;
			}
			case Probe:
			{
				IProbeMarker probeMarker = (IProbeMarker) unitMarker;
				result = new ProbeMarker(probeMarker.getTurn(), probeMarker.getStep(), probeMarker.getOwnerName(), probeMarker.getSerieName(), probeMarker.getSerialNumber(), probeMarker.isStopped(), probeMarker.getRealLocation(), probeMarker.getSpeed(), probeMarker.isDeployed());
				break;
			}
			case AntiProbeMissile:
			{
				IAntiProbeMissileMarker antiProbeMissileMarker = (IAntiProbeMissileMarker) unitMarker;
				result = new AntiProbeMissileMarker(antiProbeMissileMarker.getTurn(), antiProbeMissileMarker.getStep(), antiProbeMissileMarker.getOwnerName(), antiProbeMissileMarker.getSerieName(), antiProbeMissileMarker.getSerialNumber(), antiProbeMissileMarker.isStopped(), antiProbeMissileMarker.getRealLocation(), antiProbeMissileMarker.getSpeed(), antiProbeMissileMarker.isFired());
				break;
			}
			default:
			{
				throw new RuntimeException("Unknown Unit type '" + unitMarker.getType() + "'.");
			}
		}
		
		result.create(this);
		return (T) result;
	}
	
	public static <T extends IUnit> T makeUnit(T unit)
	{
		switch(unit.getType())
		{
			case AntiProbeMissile:
			{
				IAntiProbeMissile apm = (IAntiProbeMissile) unit;
				return (T) makeAntiProbeMissile(apm.getOwnerName(), apm.getSerieName(), apm.getSerialNumber(), apm.getInitialDepartureName());
			}
			
			case Fleet:
			{
				IFleet fleet = (IFleet) unit;
				return (T) makeFleet(fleet.getOwnerName(), fleet.getName(), fleet.getInitialDepartureName(), fleet.getStarships());
			}
			
			case Probe:
			{
				IProbe probe = (IProbe) unit;
				return (T) makeProbe(probe.getOwnerName(), probe.getSerieName(), probe.getSerialNumber(), probe.getInitialDepartureName());
			}
			
			default:
			{
				throw new RuntimeException("Unknown Unit type '"+unit.getType()+"'.");
			}
		}
	}
	
	public IFleet getCreateAssignedFleet(String productiveCelestialBodyName, String playerName)
	{
		waitForInit();
		ProductiveCelestialBody pcb = (ProductiveCelestialBody) getCelestialBody(productiveCelestialBodyName);
		return pcb.getAssignedFleet(playerName, true);
	}

	/*
	public void updateGovernment(final IGovernment government)
	{
		Transaction tx = db.beginTx();
		try
		{
			Node nPlayer = playerIndex.get("name", government.getOwner()).getSingle();
			if (nPlayer == null)
			{
				tx.failure();
				throw new RuntimeException("Player '"+government.getOwner()+"' unknwon.");
			}
			
			Node targetNode = null;
			if (government.getFleetName() != null)
			{
				Node nFleet = nPlayer.traverse(Order.DEPTH_FIRST, new StopEvaluator()
				{
					
					@Override
					public boolean isStopNode(TraversalPosition currentPos)
					{
						return (currentPos.depth() > 1 || currentPos.returnedNodesCount() > 0);
					}
				}, new ReturnableEvaluator()
				{
					
					@Override
					public boolean isReturnableNode(TraversalPosition currentPos)
					{
						Node n = currentPos.currentNode();
						return (n.getProperty("name").equals(government.getFleetName()));
					}
				}, eUnitType.Fleet, Direction.OUTGOING).iterator().next();
				
				if (nFleet == null)
				{
					tx.failure();
					throw new RuntimeException("Fleet '"+government.getFleetName()+"' unknown for player '"+government.getOwner()+"'.");
				}
				
				targetNode = nFleet;
			}
			
			if (targetNode == null && government.getPlanetName() != null)
			{
				Node nPlanet = celestialBodyIndex.get("name", government.getPlanetName()).getSingle();
				if (nPlanet == null || !government.getOwner().equals(nPlanet.getProperty("owner")) || !eCelestialBodyType.Planet.equals(eCelestialBodyType.valueOf((String) nPlanet.getProperty("type"))))
				{
					tx.failure();
					throw new RuntimeException("Celestial body '"+government.getPlanetName()+"' is not a planet, or is not owned by player '"+government.getOwner()+"'.");
				}
				
				targetNode = nPlanet;
			}
			
			if (targetNode == null)
			{
				tx.failure();
				throw new RuntimeException("Could not found target government.");
			}
			
			Relationship currentGovernment = nPlayer.getSingleRelationship(eRelationsTypes.Government, Direction.OUTGOING);
			if (currentGovernment != null)
			{
				currentGovernment.delete();
			}
			
			nPlayer.createRelationshipTo(targetNode, eRelationsTypes.Government);
			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}
	*/
	/*
	public ICelestialBody getCelestialBody(Location location)
	{				
		try
		{
			Node nCelestialBody = areaIndex.get("location", location.toString()).getSingle().getSingleRelationship(DBGraph.eRelationTypes.CelestialBody, Direction.OUTGOING).getEndNode();
			return DataBaseORMGenerator.mapTo(ICelestialBody.class, nCelestialBody);
		}
		catch(Throwable t)
		{
			if (!NoSuchElementException.class.isInstance(t))
			{
				log.log(Level.WARNING, "ORM Error, assume there is no CelestialBody at location '"+location.toString()+"'.", t);
			}
			return null;
		}
	}
	*/

	////////// Private Querying methods

	//////////////// ListIterator implementation
		
	@Override
	public boolean hasNext()
	{
		//return (initFlag == null || initFlag.get()) ? nextVersion != null : false;
		return nextVersion != null;
	}

	@Override
	public boolean hasPrevious()
	{
		return previousVersion != null;
	}

	@Override
	public SEPCommonDB next()
	{
		return nextVersion;
	}

	@Override
	public SEPCommonDB previous()
	{
		return previousVersion;
	}

	// Assume that DB are saved for each turn, and no saves are made in between.
	@Override
	public int nextIndex()
	{
		return getConfig().getTurn() + 1;
	}

	@Override
	public int previousIndex()
	{
		return getConfig().getTurn() - 1;
	}

	@Override
	public void add(SEPCommonDB e)
	{
		throw new UnsupportedOperationException(SEPCommonDB.class.getName() + " iterator cannot be used to add elements.");
	}

	@Override
	public void remove()
	{
		throw new UnsupportedOperationException(SEPCommonDB.class.getName() + " iterator cannot be used to remove elements.");
	}

	@Override
	public void set(SEPCommonDB e)
	{
		throw new UnsupportedOperationException(SEPCommonDB.class.getName() + " iterator cannot be used to set elements.");
	}

	//////////////// Serialization, Cloning
	
	private void waitForInit()
	{
		if (initFlag == null) return;
		
		synchronized(initFlag)
		{
			while(!initFlag.get())
			{
				try
				{
					initFlag.wait(1000);
				}
				catch(InterruptedException ie)
				{
					Thread.currentThread().interrupt();
				}
			}
		}
	}	

	public static SEPCommonDB[] clone(final SEPCommonDB source, int nbCopies, final int copyGameTurn)
	{
		SEPCommonDB[] copies = new SEPCommonDB[nbCopies];
		
		try
		{
			final GenericHolder<Boolean> written = new GenericHolder<Boolean>(false);
			final File tmpFile = File.createTempFile("cloneBuf", ".tmp");
			
			new Thread(new Runnable()
			{			
				@Override
				public void run()
				{
					try
					{
						FileOutputStream fos = new FileOutputStream(tmpFile);
						ObjectOutputStream oos = new ObjectOutputStream(fos);
						
						source.writeExternal(oos);
						
						oos.close();
						fos.close();
					}
					catch(Exception e)
					{
						throw new RuntimeException(e);
					}
					finally
					{
						synchronized(written)
						{
							written.set(true);
							written.notifyAll();
						}						
					}
				}
			}).start();			
			
			for(int i=0; i < nbCopies; ++i)
			{
				final SEPCommonDB copy = new SEPCommonDB(new GenericHolder<Boolean>(false), copyGameTurn);
				
				new Thread(new Runnable()
				{
					
					@Override
					public void run()
					{
						synchronized(written)
						{
							while(!written.get())
							{
								try
								{
									written.wait();
								}
								catch(InterruptedException ie) {}
							}
						}
						
						try
						{
							FileInputStream fis = new FileInputStream(tmpFile);
							ObjectInputStream ois = new ObjectInputStream(fis);
							
							copy.readExternal(ois);
							
							ois.close();
							fis.close();
						}
						catch(Throwable t)
						{
							throw new RuntimeException(t);
						}
					}
				}).start();
				
				copies[i] = copy;
								
				tmpFile.deleteOnExit();
			}
			
			return copies;
		}
		catch (Exception e)
		{
			throw new Error("Cannot serialise/deserialise object from class "+source.getClass().getCanonicalName(), e);
		}
	}
	
	protected void init(GraphDatabaseService db)
	{
		if (initFlag != null)
		{
			synchronized(initFlag)
			{
				doInit(db);				
				
				if (config == null)
				{
					config = (IGameConfig) Proxy.newProxyInstance(IGameConfig.class.getClassLoader(), new Class<?>[] {IGameConfig.class}, new GameConfigInvocationHandler(this));
				}
				
				// Set game turn.
				Transaction tx = db.beginTx();
				try
				{
					db.getReferenceNode().getSingleRelationship(eRelationTypes.GameConfig, Direction.OUTGOING).getEndNode().setProperty("Turn", gameTurn);
					tx.success();
				}
				finally
				{
					tx.finish();
				}
				
				initFlag.set(true);
				initFlag.notify();
			}
		}
		else
		{
			doInit(db);
		}
		
		if (config == null)
		{
			config = (IGameConfig) Proxy.newProxyInstance(IGameConfig.class.getClassLoader(), new Class<?>[] {IGameConfig.class}, new GameConfigInvocationHandler(this));
		}
	}
	
	private void doInit(final GraphDatabaseService db)
	{
		this.areaChangeListeners = new HashSet<IAreaChangeListener>();
		this.logListeners = new HashSet<ILogListener>();
		this.playerChangeListeners = new HashSet<IPlayerChangeListener>();
		this.cacheVersion = 0;
		
		SEPCommonDB pDB = previous();
		
		if (pDB != null)
		{
			this.areaChangeListeners.addAll(pDB.areaChangeListeners);
			this.logListeners.addAll(pDB.logListeners);
			this.playerChangeListeners.addAll(pDB.playerChangeListeners);
			pDB.areaChangeListeners.clear();
			pDB.logListeners.clear();
			pDB.playerChangeListeners.clear();
		}
		
		this.db = db;
		Runtime.getRuntime().addShutdownHook(new Thread()
		{
			@Override
			public void run()
			{
				db.shutdown();
			}
		});

		Transaction tx = db.beginTx();
		try
		{
			playerIndex = db.index().forNodes("PlayerIndex");
			nebulaIndex = db.index().forNodes("NebulaIndex");
			areaIndex = db.index().forNodes("AreaIndex");
			productiveCelestialBodyIndex = db.index().forNodes("ProductiveCelestialBodyIndex");
			planetIndex = db.index().forNodes("PlanetIndex");
			vortexIndex = db.index().forNodes("VortexIndex");
			asteroidFieldIndex = db.index().forNodes("AsteroidFieldIndex");
			celestialBodyIndex = db.index().forNodes("CelestialBodyIndex");
			buildingIndex = db.index().forNodes("BuildingIndex");
			unitIndex = db.index().forNodes("UnitIndex");
			probeIndex = db.index().forNodes("ProbeIndex");
			antiProbeMissileIndex = db.index().forNodes("AntiProbeMissileIndex");
			unitMarkerIndex = db.index().forNodes("UnitMarkerIndex");
			Relationship playersFactoryRel = db.getReferenceNode().getSingleRelationship(eRelationTypes.Players, Direction.OUTGOING);
			if (playersFactoryRel == null)
			{
				playersFactory = db.createNode();
				db.getReferenceNode().createRelationshipTo(playersFactory, eRelationTypes.Players);
			}
			else
			{
				playersFactory = playersFactoryRel.getEndNode();
			}
			Relationship areasFactoryRel = db.getReferenceNode().getSingleRelationship(eRelationTypes.Areas, Direction.OUTGOING);
			if (areasFactoryRel == null)
			{
				areasFactory = db.createNode();
				db.getReferenceNode().createRelationshipTo(areasFactory, eRelationTypes.Areas);
			}
			else
			{
				areasFactory = areasFactoryRel.getEndNode();
			}

			Relationship sunRel = db.getReferenceNode().getSingleRelationship(eRelationTypes.Sun, Direction.OUTGOING);
			if (sunRel == null)
			{
				sun = db.createNode();
				db.getReferenceNode().createRelationshipTo(playersFactory, eRelationTypes.Sun);
			}
			else
			{
				sun = sunRel.getEndNode();
			}

			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}
	
	private synchronized void writeObject(java.io.ObjectOutputStream out) throws IOException
	{
		out.defaultWriteObject();		
		writeExternal(out);
	}
	
	private void writeExternal(java.io.ObjectOutput out) throws IOException
	{	
		this.initFlag = new GenericHolder<Boolean>(false);
		EmbeddedGraphDatabase edb = (EmbeddedGraphDatabase) db;
		//File backupDirectory = File.createTempFile("dbCopy", "");		
		File zipFile = File.createTempFile("dbCopy", ".zip");

		//TODO: Use org.neo4j.backup.OnlineBackup abilities (enterprise version required)

		File dbDirectory = new File(edb.getStoreDir());		
		edb.shutdown();
		Compression.zipDirectory(dbDirectory, zipFile);

		// Complete reload after shutdown
		init(new EmbeddedGraphDatabase(dbDirectory.getAbsolutePath()));

		FileInputStream fis = new FileInputStream(zipFile);
		out.writeLong(fis.getChannel().size());
		byte[] bb = new byte[512];
		int red = 0;
		do
		{
			red = fis.read(bb);
			if (red > 0)
			{
				out.write(bb, 0, red);
			}
		} while (red > 0);

		fis.close();
	}
	
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
		readExternal(in);
	}
	
	private void readExternal(java.io.ObjectInput in) throws IOException, ClassNotFoundException
	{
		File dbDirectory = File.createTempFile("dbRestore", "");
		dbDirectory.delete();
		dbDirectory.mkdir();
		File zipFile = File.createTempFile("dbRestore", ".zip");
		FileOutputStream fos = new FileOutputStream(zipFile);

		long size = in.readLong();
		byte[] bb = new byte[512];
		long red = 0;
		do
		{
			int toRead = Math.min(512, (int) (size - red));
			// in.read() call blocks untill input is available. 
			toRead = in.read(bb, 0, toRead);
			fos.write(bb, 0, toRead);
			red += toRead;
		} while (red < size);

		fos.close();

		Compression.unzip(zipFile, dbDirectory);

		init(new EmbeddedGraphDatabase(dbDirectory.getAbsolutePath()));
	}

	private void readObjectNoData() throws ObjectStreamException
	{

	}

	public static void assertProperty(Node node, String property)
	{
		if (!node.hasProperty(property))
		{
			System.err.println("Node property assertion failed, cannot find "+property+" property :");
			for(String key : node.getPropertyKeys())
			{
				System.err.println(key+" == "+node.getProperty(key));
			}
		}
	}
}
