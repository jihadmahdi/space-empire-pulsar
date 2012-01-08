package org.axan.sep.common.db.orm;

import org.axan.sep.common.db.IDBGraph;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.Transaction;
import org.axan.eplib.orm.nosql.DBGraphException;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Direction;
import org.axan.eplib.orm.DataBaseORMGenerator;
import java.util.NoSuchElementException;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.neo4j.graphdb.ReturnableEvaluator;
import org.neo4j.graphdb.StopEvaluator;
import org.neo4j.graphdb.Traverser.Order;
import java.util.HashSet;
import java.util.Set;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.IBuilding;
import org.axan.sep.common.db.IPlayerConfig;
import org.axan.sep.common.db.IExtractionModule;
import org.axan.sep.common.db.IStarshipPlant;
import org.axan.sep.common.db.ISpaceCounter;
import org.axan.sep.common.db.IPlayer;
import org.axan.sep.common.db.INebula;
import org.axan.sep.common.db.IArea;
import org.axan.sep.common.db.IProductiveCelestialBody;
import org.axan.sep.common.db.IPlanet;
import org.axan.sep.common.db.IVortex;
import org.axan.sep.common.db.IAsteroidField;
import org.axan.sep.common.db.IGovernmentModule;
import org.axan.sep.common.db.IDefenseModule;
import org.axan.sep.common.db.ICelestialBody;
import org.axan.sep.common.db.IPulsarLaunchingPad;

public class DBGraph implements IDBGraph
{
	/*
	static protected enum eRelationTypes implements RelationshipType
	{
		Config,
		Units,
		CelestialBodies,
		Type,
		CelestialBody,
		Buildings,
		Diplomacies,
		Destination,
		Players,
		Areas
	}

	protected static Logger log = Logger.getLogger(DBGraph.class.getName());
	protected transient GraphDatabaseService db;
	protected transient Index<Node> playerIndex;
	protected transient Index<Node> nebulaIndex;
	protected transient Index<Node> areaIndex;
	protected transient Index<Node> productiveCelestialBodyIndex;
	protected transient Index<Node> planetIndex;
	protected transient Index<Node> vortexIndex;
	protected transient Index<Node> asteroidFieldIndex;
	protected transient Index<Node> celestialBodyIndex;
	protected transient Node playersFactory;
	protected transient Node areasFactory;

	public DBGraph(GraphDatabaseService db)
	{
		init(db);
	}

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
			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}

	protected IExtractionModule createExtractionModule(int builtDate, int nbSlots)
	{
		Transaction tx = db.beginTx();
		try
		{
			Node extractionModuleNode = db.createNode();
			ExtractionModule.initializeNode(extractionModuleNode, builtDate, nbSlots);
			tx.success();
			return new ExtractionModule(extractionModuleNode);
		}
		finally
		{
			tx.finish();
		}
	}

	protected IExtractionModule createExtractionModule(IExtractionModule extractionModule)
	{
		return createExtractionModule(extractionModule.getBuiltDate(), extractionModule.getNbSlots());
	}

	protected IStarshipPlant createStarshipPlant(int builtDate, int nbSlots)
	{
		Transaction tx = db.beginTx();
		try
		{
			Node starshipPlantNode = db.createNode();
			StarshipPlant.initializeNode(starshipPlantNode, builtDate, nbSlots);
			tx.success();
			return new StarshipPlant(starshipPlantNode);
		}
		finally
		{
			tx.finish();
		}
	}

	protected IStarshipPlant createStarshipPlant(IStarshipPlant starshipPlant)
	{
		return createStarshipPlant(starshipPlant.getBuiltDate(), starshipPlant.getNbSlots());
	}

	protected ISpaceCounter createSpaceCounter(int builtDate, int nbSlots)
	{
		Transaction tx = db.beginTx();
		try
		{
			Node spaceCounterNode = db.createNode();
			SpaceCounter.initializeNode(spaceCounterNode, builtDate, nbSlots);
			tx.success();
			return new SpaceCounter(spaceCounterNode);
		}
		finally
		{
			tx.finish();
		}
	}

	protected ISpaceCounter createSpaceCounter(ISpaceCounter spaceCounter)
	{
		return createSpaceCounter(spaceCounter.getBuiltDate(), spaceCounter.getNbSlots());
	}

	protected IPlayer createPlayer(String name, String playerConfigColor, String playerConfigSymbol, String playerConfigPortrait)
	{
		Transaction tx = db.beginTx();
		try
		{
			if (playerIndex.get("name", name).hasNext())
			{
				tx.failure();
				throw new DBGraphException("Constraint error: Indexed field 'name' must be unique, Player[name='"+name+"'] already exist.");
			}
			Node playerNode = db.createNode();
			Player.initializeNode(playerNode, name);
			playerIndex.add(playerNode, "name", name);
			playersFactory.createRelationshipTo(playerNode, eRelationTypes.Players);
			Node playerConfigNode = createPlayerConfig(playerConfigColor, playerConfigSymbol, playerConfigPortrait);
			playerNode.createRelationshipTo(playerConfigNode, eRelationTypes.Config);
			tx.success();
			return new Player(playerNode);
		}
		finally
		{
			tx.finish();
		}
	}

	protected IPlayer createPlayer(IPlayer player, IPlayerConfig playerConfig)
	{
		return createPlayer(player.getName(), playerConfig.getColor(), playerConfig.getSymbol(), playerConfig.getPortrait());
	}

	protected INebula createNebula(String name, int initialCarbonStock, int maxSlots, int carbonStock, int currentCarbon)
	{
		Transaction tx = db.beginTx();
		try
		{
			if (nebulaIndex.get("name", name).hasNext())
			{
				tx.failure();
				throw new DBGraphException("Constraint error: Indexed field 'name' must be unique, Nebula[name='"+name+"'] already exist.");
			}
			Node nebulaNode = db.createNode();
			Nebula.initializeNode(nebulaNode, name, initialCarbonStock, maxSlots, carbonStock, currentCarbon);
			nebulaIndex.add(nebulaNode, "name", name);
			productiveCelestialBodyIndex.add(nebulaNode, "name", name);
			celestialBodyIndex.add(nebulaNode, "name", name);
			tx.success();
			return new Nebula(nebulaNode);
		}
		finally
		{
			tx.finish();
		}
	}

	protected INebula createNebula(INebula nebula)
	{
		return createNebula(nebula.getName(), nebula.getInitialCarbonStock(), nebula.getMaxSlots(), nebula.getCarbonStock(), nebula.getCurrentCarbon());
	}

	protected IArea createArea(Location location)
	{
		Transaction tx = db.beginTx();
		try
		{
			if (areaIndex.get("location", location.toString()).hasNext())
			{
				tx.failure();
				throw new DBGraphException("Constraint error: Indexed field 'location' must be unique, Area[location='"+location.toString()+"'] already exist.");
			}
			Node areaNode = db.createNode();
			Area.initializeNode(areaNode, location);
			areaIndex.add(areaNode, "location", location.toString());
			areasFactory.createRelationshipTo(areaNode, eRelationTypes.Areas);
			tx.success();
			return new Area(areaNode);
		}
		finally
		{
			tx.finish();
		}
	}

	protected IArea createArea(IArea area)
	{
		return createArea(area.getLocation());
	}

	private Node createPlayerConfig(String color, String symbol, String portrait)
	{
		Transaction tx = db.beginTx();
		try
		{
			Node playerConfigNode = db.createNode();
			PlayerConfig.initializeNode(playerConfigNode, color, symbol, portrait);
			tx.success();
			return playerConfigNode;
		}
		finally
		{
			tx.finish();
		}
	}

	private Node createPlayerConfig(IPlayerConfig playerConfig)
	{
		return createPlayerConfig(playerConfig.getColor(), playerConfig.getSymbol(), playerConfig.getPortrait());
	}

	protected IPlanet createPlanet(String name, int initialCarbonStock, int maxSlots, int carbonStock, int currentCarbon, int populationPerTurn, int maxPopulation, int currentPopulation)
	{
		Transaction tx = db.beginTx();
		try
		{
			if (planetIndex.get("name", name).hasNext())
			{
				tx.failure();
				throw new DBGraphException("Constraint error: Indexed field 'name' must be unique, Planet[name='"+name+"'] already exist.");
			}
			Node planetNode = db.createNode();
			Planet.initializeNode(planetNode, name, initialCarbonStock, maxSlots, carbonStock, currentCarbon, populationPerTurn, maxPopulation, currentPopulation);
			planetIndex.add(planetNode, "name", name);
			productiveCelestialBodyIndex.add(planetNode, "name", name);
			celestialBodyIndex.add(planetNode, "name", name);
			tx.success();
			return new Planet(planetNode);
		}
		finally
		{
			tx.finish();
		}
	}

	protected IPlanet createPlanet(IPlanet planet)
	{
		return createPlanet(planet.getName(), planet.getInitialCarbonStock(), planet.getMaxSlots(), planet.getCarbonStock(), planet.getCurrentCarbon(), planet.getPopulationPerTurn(), planet.getMaxPopulation(), planet.getCurrentPopulation());
	}

	protected IVortex createVortex(String name, int birth, int death)
	{
		Transaction tx = db.beginTx();
		try
		{
			if (vortexIndex.get("name", name).hasNext())
			{
				tx.failure();
				throw new DBGraphException("Constraint error: Indexed field 'name' must be unique, Vortex[name='"+name+"'] already exist.");
			}
			Node vortexNode = db.createNode();
			Vortex.initializeNode(vortexNode, name, birth, death);
			vortexIndex.add(vortexNode, "name", name);
			celestialBodyIndex.add(vortexNode, "name", name);
			tx.success();
			return new Vortex(vortexNode);
		}
		finally
		{
			tx.finish();
		}
	}

	protected IVortex createVortex(IVortex vortex)
	{
		return createVortex(vortex.getName(), vortex.getBirth(), vortex.getDeath());
	}

	protected IAsteroidField createAsteroidField(String name, int initialCarbonStock, int maxSlots, int carbonStock, int currentCarbon)
	{
		Transaction tx = db.beginTx();
		try
		{
			if (asteroidFieldIndex.get("name", name).hasNext())
			{
				tx.failure();
				throw new DBGraphException("Constraint error: Indexed field 'name' must be unique, AsteroidField[name='"+name+"'] already exist.");
			}
			Node asteroidFieldNode = db.createNode();
			AsteroidField.initializeNode(asteroidFieldNode, name, initialCarbonStock, maxSlots, carbonStock, currentCarbon);
			asteroidFieldIndex.add(asteroidFieldNode, "name", name);
			productiveCelestialBodyIndex.add(asteroidFieldNode, "name", name);
			celestialBodyIndex.add(asteroidFieldNode, "name", name);
			tx.success();
			return new AsteroidField(asteroidFieldNode);
		}
		finally
		{
			tx.finish();
		}
	}

	protected IAsteroidField createAsteroidField(IAsteroidField asteroidField)
	{
		return createAsteroidField(asteroidField.getName(), asteroidField.getInitialCarbonStock(), asteroidField.getMaxSlots(), asteroidField.getCarbonStock(), asteroidField.getCurrentCarbon());
	}

	protected IGovernmentModule createGovernmentModule(int builtDate, int nbSlots)
	{
		Transaction tx = db.beginTx();
		try
		{
			Node governmentModuleNode = db.createNode();
			GovernmentModule.initializeNode(governmentModuleNode, builtDate, nbSlots);
			tx.success();
			return new GovernmentModule(governmentModuleNode);
		}
		finally
		{
			tx.finish();
		}
	}

	protected IGovernmentModule createGovernmentModule(IGovernmentModule governmentModule)
	{
		return createGovernmentModule(governmentModule.getBuiltDate(), governmentModule.getNbSlots());
	}

	protected IDefenseModule createDefenseModule(int builtDate, int nbSlots)
	{
		Transaction tx = db.beginTx();
		try
		{
			Node defenseModuleNode = db.createNode();
			DefenseModule.initializeNode(defenseModuleNode, builtDate, nbSlots);
			tx.success();
			return new DefenseModule(defenseModuleNode);
		}
		finally
		{
			tx.finish();
		}
	}

	protected IDefenseModule createDefenseModule(IDefenseModule defenseModule)
	{
		return createDefenseModule(defenseModule.getBuiltDate(), defenseModule.getNbSlots());
	}

	protected IPulsarLaunchingPad createPulsarLaunchingPad(int builtDate, int nbSlots)
	{
		Transaction tx = db.beginTx();
		try
		{
			Node pulsarLaunchingPadNode = db.createNode();
			PulsarLaunchingPad.initializeNode(pulsarLaunchingPadNode, builtDate, nbSlots);
			tx.success();
			return new PulsarLaunchingPad(pulsarLaunchingPadNode);
		}
		finally
		{
			tx.finish();
		}
	}

	protected IPulsarLaunchingPad createPulsarLaunchingPad(IPulsarLaunchingPad pulsarLaunchingPad)
	{
		return createPulsarLaunchingPad(pulsarLaunchingPad.getBuiltDate(), pulsarLaunchingPad.getNbSlots());
	}

	protected IBuilding createBuilding(IBuilding building)
	{
		if (IExtractionModule.class.isInstance(building))
		{
			return createExtractionModule((IExtractionModule) building);
		}
		else if (IGovernmentModule.class.isInstance(building))
		{
			return createGovernmentModule((IGovernmentModule) building);
		}
		else if (IDefenseModule.class.isInstance(building))
		{
			return createDefenseModule((IDefenseModule) building);
		}
		else if (IStarshipPlant.class.isInstance(building))
		{
			return createStarshipPlant((IStarshipPlant) building);
		}
		else if (ISpaceCounter.class.isInstance(building))
		{
			return createSpaceCounter((ISpaceCounter) building);
		}
		else if (IPulsarLaunchingPad.class.isInstance(building))
		{
			return createPulsarLaunchingPad((IPulsarLaunchingPad) building);
		}
		else
		{
			throw new RuntimeException("Unknown Building subtype '"+building.getClass().getSimpleName()+"'.");
		}
	}

	protected ICelestialBody createCelestialBody(ICelestialBody celestialBody)
	{
		if (IVortex.class.isInstance(celestialBody))
		{
			return createVortex((IVortex) celestialBody);
		}
		else if (IAsteroidField.class.isInstance(celestialBody))
		{
			return createAsteroidField((IAsteroidField) celestialBody);
		}
		else if (INebula.class.isInstance(celestialBody))
		{
			return createNebula((INebula) celestialBody);
		}
		else if (IPlanet.class.isInstance(celestialBody))
		{
			return createPlanet((IPlanet) celestialBody);
		}
		else
		{
			throw new RuntimeException("Unknown CelestialBody subtype '"+celestialBody.getClass().getSimpleName()+"'.");
		}
	}

	public IArea getAreaByLocation(Location location)
	{
		return new Area(areaIndex.get("location", location.toString()).getSingle());
	}

	public IPlayer getPlayerByName(String name)
	{
		return new Player(playerIndex.get("name", name).getSingle());
	}

	public IPlayerConfig getPlayerConfigByName(String name)
	{
		return new PlayerConfig(playerIndex.get("name", name).getSingle().getSingleRelationship(eRelationTypes.Config, Direction.OUTGOING).getEndNode());
	}

	public INebula getNebulaByName(String name)
	{
		return new Nebula(nebulaIndex.get("name", name).getSingle());
	}

	public IProductiveCelestialBody getProductiveCelestialBodyByName(String name)
	{
		try
		{
			return DataBaseORMGenerator.mapTo(IProductiveCelestialBody.class, productiveCelestialBodyIndex.get("name", name).getSingle());
		}
		catch(Throwable t)
		{
			if (!NoSuchElementException.class.isInstance(t))
			{
				log.log(Level.WARNING, "ORM Error, assume ProductiveCelestialBody name '"+name+"' is not found.", t);
			}
			return null;
		}
	}

	public IPlanet getPlanetByName(String name)
	{
		return new Planet(planetIndex.get("name", name).getSingle());
	}

	public IVortex getVortexByName(String name)
	{
		return new Vortex(vortexIndex.get("name", name).getSingle());
	}

	public IAsteroidField getAsteroidFieldByName(String name)
	{
		return new AsteroidField(asteroidFieldIndex.get("name", name).getSingle());
	}

	public ICelestialBody getCelestialBodyByName(String name)
	{
		try
		{
			return DataBaseORMGenerator.mapTo(ICelestialBody.class, celestialBodyIndex.get("name", name).getSingle());
		}
		catch(Throwable t)
		{
			if (!NoSuchElementException.class.isInstance(t))
			{
				log.log(Level.WARNING, "ORM Error, assume CelestialBody name '"+name+"' is not found.", t);
			}
			return null;
		}
	}

	public Set<IPlayer> getPlayers()
	{
		Set<IPlayer> result = new HashSet<IPlayer>();
		for(Node nPlayer : playersFactory.traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, ReturnableEvaluator.ALL_BUT_START_NODE, eRelationTypes.Players, Direction.OUTGOING))
		{
			result.add(new Player(nPlayer));
		}
		return result;
	}

	public Set<IArea> getAreas()
	{
		Set<IArea> result = new HashSet<IArea>();
		for(Node nArea : areasFactory.traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, ReturnableEvaluator.ALL_BUT_START_NODE, eRelationTypes.Areas, Direction.OUTGOING))
		{
			result.add(new Area(nArea));
		}
		return result;
	}

	public static IExtractionModule makeExtractionModule(int builtDate, int nbSlots)
	{
		return new ExtractionModule(builtDate, nbSlots);
	}

	public static IAsteroidField makeAsteroidField(String name, int initialCarbonStock, int maxSlots, int carbonStock, int currentCarbon)
	{
		return new AsteroidField(name, initialCarbonStock, maxSlots, carbonStock, currentCarbon);
	}

	public static IVortex makeVortex(String name, int birth, int death)
	{
		return new Vortex(name, birth, death);
	}

	public static IGovernmentModule makeGovernmentModule(int builtDate, int nbSlots)
	{
		return new GovernmentModule(builtDate, nbSlots);
	}

	public static IDefenseModule makeDefenseModule(int builtDate, int nbSlots)
	{
		return new DefenseModule(builtDate, nbSlots);
	}

	public static IStarshipPlant makeStarshipPlant(int builtDate, int nbSlots)
	{
		return new StarshipPlant(builtDate, nbSlots);
	}

	public static ISpaceCounter makeSpaceCounter(int builtDate, int nbSlots)
	{
		return new SpaceCounter(builtDate, nbSlots);
	}

	public static IPlayer makePlayer(String name)
	{
		return new Player(name);
	}

	public static INebula makeNebula(String name, int initialCarbonStock, int maxSlots, int carbonStock, int currentCarbon)
	{
		return new Nebula(name, initialCarbonStock, maxSlots, carbonStock, currentCarbon);
	}

	public static IPulsarLaunchingPad makePulsarLaunchingPad(int builtDate, int nbSlots)
	{
		return new PulsarLaunchingPad(builtDate, nbSlots);
	}

	public static IPlayerConfig makePlayerConfig(String color, String symbol, String portrait)
	{
		return new PlayerConfig(color, symbol, portrait);
	}

	public static IPlanet makePlanet(String name, int initialCarbonStock, int maxSlots, int carbonStock, int currentCarbon, int populationPerTurn, int maxPopulation, int currentPopulation)
	{
		return new Planet(name, initialCarbonStock, maxSlots, carbonStock, currentCarbon, populationPerTurn, maxPopulation, currentPopulation);
	}

	public IPlayerConfig getPlayerConfig(String name)
	{
		return new PlayerConfig(playerIndex.get("name", name).getSingle().getSingleRelationship(eRelationTypes.Config, Direction.OUTGOING).getEndNode());
	}

	public ICelestialBody getAreaCelestialBody(Location location)
	{
		return getCelestialBodyByName((String) areaIndex.get("location", location.toString()).getSingle().getSingleRelationship(eRelationTypes.CelestialBody, Direction.OUTGOING).getEndNode().getProperty("name"));
	}
	
	public void setAreaCelestialBody(Location location, ICelestialBody celestialBody)
	{
		Node nArea = areaIndex.get("location", location.toString()).getSingle();
		Relationship rCelestialBody = nArea.getSingleRelationship(eRelationTypes.CelestialBody, Direction.OUTGOING);
		Node nCelestialBody = rCelestialBody.getEndNode();
		rCelestialBody.delete();
		cleanNode(nCelestialBody);
		
		
	}

	private static void cleanNode(Node n)
	{
		// We assume node is also deindexed 
		if (!n.hasRelationship()) n.delete();
	}
	
	private static void removeNode(Node n)
	{
		for(Relationship r : n.getRelationships())
		{
			r.delete();
		}
		n.delete();
	}
	
	public ICelestialBody getVortexDestination(String name)
	{
		return getCelestialBodyByName((String) vortexIndex.get("name", name).getSingle().getSingleRelationship(eRelationTypes.Destination, Direction.OUTGOING).getEndNode().getProperty("name"));
	}
	*/
}
