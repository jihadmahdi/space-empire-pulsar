package org.axan.sep.common.db.orm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;

import javax.management.RuntimeErrorException;

import org.apache.commons.lang.ArrayUtils;
import org.axan.eplib.orm.DataBaseORMGenerator;
import org.axan.eplib.orm.nosql.DBGraphException;
import org.axan.eplib.utils.Basic;
import org.axan.eplib.utils.Compression;
import org.axan.sep.common.GameConfigCopier;
import org.axan.sep.common.GameConfigCopier.GameConfigCopierException;
import org.axan.sep.common.Protocol;
import org.axan.sep.common.Protocol.eBuildingType;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.IArea;
import org.axan.sep.common.db.IAsteroidField;
import org.axan.sep.common.db.IBuilding;
import org.axan.sep.common.db.ICelestialBody;
import org.axan.sep.common.db.IDefenseModule;
import org.axan.sep.common.db.IExtractionModule;
import org.axan.sep.common.db.IGameConfig;
import org.axan.sep.common.db.IGovernmentModule;
import org.axan.sep.common.db.INebula;
import org.axan.sep.common.db.IPlanet;
import org.axan.sep.common.db.IPlayer;
import org.axan.sep.common.db.IPlayerConfig;
import org.axan.sep.common.db.IProductiveCelestialBody;
import org.axan.sep.common.db.IPulsarLaunchingPad;
import org.axan.sep.common.db.ISpaceCounter;
import org.axan.sep.common.db.IStarshipPlant;
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

import scala.util.control.Exception.Finally;

public class SEPCommonDB implements Serializable, ListIterator<SEPCommonDB>
{
	private static final long serialVersionUID = 1L;

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
		
		/** Relation from Fleet node to its assigned CelestialBody node. */
		AssignedCelestialBody,
		
		/** Relation from ProductiveCelestialBody node to Building nodes. */
		Buildings,
		
		/** Relation from Player node to its PlayerConfig node. */
		PlayerConfig,
		
		/** Relation from Player node to owned CelestialBody nodes. */
		PlayerCelestialBodies
		// eCelestialBodyType value
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
							sepDB.nextVersion = Basic.clone(sepDB);
							sepDB.nextVersion.previousVersion = sepDB;

							// Increment next version turn.
							Transaction tx = sepDB.nextVersion.getConfigDB().beginTx();
							try
							{
								sepDB.nextVersion.getConfigDB().getReferenceNode().getSingleRelationship(eRelationTypes.GameConfig, Direction.OUTGOING).getEndNode().setProperty("Turn", value);
								tx.success();
							}
							finally
							{
								tx.finish();
							}
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
				else if (!void.class.equals(method.getReturnType()))
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
	private transient Index<Node> playerIndex;
	private transient Index<Node> areaIndex;
	private transient Index<Node> celestialBodyIndex;
	private transient Index<Node> vortexIndex;
	private transient Index<Node> productiveCelestialBodyIndex;
	private transient Index<Node> nebulaIndex;
	private transient Index<Node> asteroidFieldIndex;
	private transient Index<Node> planetIndex;

	private transient Node playersFactory;
	private transient Node areasFactory;
	private transient Node sun;

	private SEPCommonDB previousVersion = null;
	private SEPCommonDB nextVersion = null;

	public SEPCommonDB(GraphDatabaseService db, IGameConfig config) throws IOException, GameConfigCopierException, InterruptedException
	{
		init(db);
		GameConfigCopier.copy(IGameConfig.class, config, this.config);
	}

	public IGameConfig getConfig()
	{
		return this.config;
	}

	public synchronized GraphDatabaseService getConfigDB()
	{
		return db;
	}

	public synchronized GraphDatabaseService getDB()
	{
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
		return areasFactory.hasRelationship(eRelationTypes.Areas, Direction.OUTGOING);
	}

	public Set<IPlayer> getPlayers()
	{
		Set<IPlayer> players = new HashSet<IPlayer>();
		for(Node n: playersFactory.traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, ReturnableEvaluator.ALL_BUT_START_NODE, eRelationTypes.Players, Direction.OUTGOING))
		{
			players.add(new Player(this, (String) n.getProperty("name")));
		}

		return players;
	}

	public IPlayer getPlayerByName(String name)
	{
		return new Player(this, name);
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
		SEPCommonDB db = this;
		while (db.getConfig().getTurn() > 1)
			db = db.previous();
		Iterator<Node> it = db.playerIndex.get("name", playerName).getSingle().traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, ReturnableEvaluator.ALL_BUT_START_NODE, eCelestialBodyType.Planet, Direction.OUTGOING).iterator();
		if (!it.hasNext())
			return null;
		return new Planet(this, (String) it.next().getProperty("name"));
	}

	public Set<IArea> getAreasByZ(int z)
	{
		Set<IArea> areas = new HashSet<IArea>();
		// Lucene reserved characters: + - && || ! ( ) { } [ ] ^ " ~ * ? : \
		for(Node n: areaIndex.query("location", String.format("\\[*;%d\\]", z)))
		{
			areas.add(new Area(this, Location.valueOf((String) n.getProperty("location"))));
		}
		return areas;
	}

	//////////////// CRUD: Create

	public IPlayer createPlayer(IPlayer player)
	{
		return createPlayer(player.getName(), player.getConfig().getColor(), player.getConfig().getSymbol(), player.getConfig().getPortrait());
	}

	public IPlayer createPlayer(String name, String configColor, String configSymbol, String configPortrait)
	{
		Player result = new Player(name, new PlayerConfig(configColor, configSymbol, configPortrait));
		result.create(this);
		return result;
	}

	static public IPlayer makePlayer(String name, IPlayerConfig config)
	{
		return new Player(name, config);
	}

	static public IPlayerConfig makePlayerConfig(String color, String symbol, String portrait)
	{
		return new PlayerConfig(color, symbol, portrait);
	}

	public IArea createArea(IArea area)
	{
		return createArea(area.getLocation(), area.isSun());
	}

	public IArea createArea(Location location, boolean isSun)
	{
		Area result = new Area(location, isSun);
		result.create(this);
		return result;
	}

	public IVortex createVortex(IVortex vortex)
	{
		Vortex result = new Vortex(vortex.getName(), vortex.getLocation(), vortex.getBirth(), vortex.getDeath());
		result.create(this);
		return result;
	}

	public IVortex getVortex(String name)
	{
		return new Vortex(this, name);
	}

	static public IVortex makeVortex(String name, Location location, int birth, int death)
	{
		return new Vortex(name, location, birth, death);
	}

	public IAsteroidField createAsteroidField(IAsteroidField asteroidField)
	{
		AsteroidField result = new AsteroidField(asteroidField.getName(), asteroidField.getLocation(), asteroidField.getInitialCarbonStock(), asteroidField.getMaxSlots(), asteroidField.getCarbonStock(), asteroidField.getCurrentCarbon());
		result.create(this);
		return result;
	}

	public IAsteroidField getAsteroidField(String name)
	{
		return new AsteroidField(this, name);
	}

	static public IAsteroidField makeAsteroidField(String name, Location location, int initialCarbonStock, int maxSlots, int carbonStock, int currentCarbon)
	{
		return new AsteroidField(name, location, initialCarbonStock, maxSlots, carbonStock, currentCarbon);
	}

	public INebula createNebula(INebula nebula)
	{
		Nebula result = new Nebula(nebula.getName(), nebula.getLocation(), nebula.getInitialCarbonStock(), nebula.getMaxSlots(), nebula.getCarbonStock(), nebula.getCurrentCarbon());
		result.create(this);
		return result;
	}

	public INebula getNebula(String name)
	{
		return new Nebula(this, name);
	}

	static public INebula makeNebula(String name, Location location, int initialCarbonStock, int maxSlots, int carbonStock, int currentCarbon)
	{
		return new Nebula(name, location, initialCarbonStock, maxSlots, carbonStock, currentCarbon);
	}

	public IPlanet createPlanet(IPlanet planet)
	{
		Planet result = new Planet(planet.getName(), planet.getLocation(), planet.getInitialCarbonStock(), planet.getMaxSlots(), planet.getCarbonStock(), planet.getCurrentCarbon(), planet.getPopulationPerTurn(), planet.getMaxPopulation(), planet.getCurrentPopulation());
		result.create(this);
		return result;
	}

	public IPlanet getPlanet(String name)
	{
		return new Planet(this, name);
	}

	static public IPlanet makePlanet(String name, Location location, int initialCarbonStock, int maxSlots, int carbonStock, int currentCarbon, int populationPerTurn, int maxPopulation, int currentPopulation)
	{
		return new Planet(name, location, initialCarbonStock, maxSlots, carbonStock, currentCarbon, populationPerTurn, maxPopulation, currentPopulation);
	}

	public ICelestialBody createCelestialBody(ICelestialBody celestialBody)
	{
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

	public ICelestialBody getCelestialBody(String name)
	{
		eCelestialBodyType type = eCelestialBodyType.valueOf((String) celestialBodyIndex.get("name", name).getSingle().getProperty("type"));

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

	public IDefenseModule createDefenseModule(IDefenseModule defenseModule)
	{
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
		StarshipPlant result = new StarshipPlant(starshipPlant.getProductiveCelestialBodyName(), starshipPlant.getBuiltDate(), starshipPlant.getNbSlots());
		result.create(this);
		return result;
	}

	public static IStarshipPlant makeStarshipPlant(String productiveCelestialBodyName, int builtDate, int nbSlots)
	{
		return new StarshipPlant(productiveCelestialBodyName, builtDate, nbSlots);
	}
	
	public IBuilding createBuilding(IBuilding building)
	{	
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

	public IBuilding getBuilding(String productiveCelestialBodyName, eBuildingType type)
	{	
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

	/*
	Use IProductiveCelestialBody#setOwner(String) instead.
	public void updateOwnership(IProductiveCelestialBody productiveCelestialBody, IPlayer owner)
	{
		Node nProductiveCelestialBody = productiveCelestialBodyIndex.get("name", productiveCelestialBody.getName()).getSingle();
		Node nOwner = playerIndex.get("name", owner.getName()).getSingle();
		
		Transaction tx = db.beginTx();
		
		try
		{
			Relationship ownership = nProductiveCelestialBody.getSingleRelationship(eRelationTypes.CelestialBody, Direction.INCOMING);
			if (ownership != null)
			{
				ownership.delete();
				ownership = null;
			}
			
			nOwner.createRelationshipTo(nProductiveCelestialBody, eRelationTypes.CelestialBody);
			
			tx.success();
		}
		finally
		{
			tx.finish();
		}		
	}
	*/

	/*		
	public void createBuilding(IBuilding building, IProductiveCelestialBody productiveCelestialBody)
	{
		Transaction tx = db.beginTx();
		
		try
		{
			Node nProductiveCelestialBody = celestialBodyIndex.get("name", productiveCelestialBody.getName()).getSingle();
			super.createBuilding(building);
		}
		finally
		{
			tx.finish();
		}
	}
	
	/*
	public void createBuilding(IBuilding building)
	{
		Transaction tx = db.beginTx();
		try
		{
			Node nCelestialBody = celestialBodyIndex.get("name", building.getCelestialBodyName()).getSingle();
			if (nCelestialBody == null)
			{
				tx.failure();
				throw new RuntimeException("Unknown celestial body '"+building.getCelestialBodyName()+"'.");
			}
			
			if (nCelestialBody.traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, ReturnableEvaluator.ALL_BUT_START_NODE, building.getType(), Direction.OUTGOING).iterator().hasNext())
			{
				tx.failure();
				throw new RuntimeException("Building '"+building.getType()+"' already exists on '"+building.getCelestialBodyName()+"'.");
			}
			
			Node nBuidling = db.createNode();
			initializeNode(nBuidling, building.getNode());
			nCelestialBody.createRelationshipTo(nBuidling, building.getType());
			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}
	*/

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

	//////////////// Serialization

	protected synchronized void init(final GraphDatabaseService db)
	{
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

		// Already refresh itself if SEPCommonDB#db change.
		if (config == null)
		{
			config = (IGameConfig) Proxy.newProxyInstance(IGameConfig.class.getClassLoader(), new Class<?>[] {IGameConfig.class}, new GameConfigInvocationHandler(this));
		}
	}

	private synchronized void writeObject(final java.io.ObjectOutputStream out) throws IOException
	{
		out.defaultWriteObject();
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
}
