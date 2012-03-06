package org.axan.sep.common.db.orm;

import org.axan.eplib.orm.nosql.DBGraphException;
import org.axan.sep.common.Protocol.eBuildingType;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.IDiplomacyMarker.eForeignPolicy;
import org.axan.sep.common.db.orm.SEPCommonDB.eRelationTypes;
import org.axan.sep.common.db.orm.base.IBasePlayer;
import org.axan.sep.common.db.orm.base.BasePlayer;
import org.axan.sep.common.db.IDiplomacy;
import org.axan.sep.common.db.IDiplomacyMarker;
import org.axan.sep.common.db.IGovernmentModule;
import org.axan.sep.common.db.IPlayer;
import org.axan.sep.common.db.IPlayerConfig;
import org.axan.sep.common.db.IUnit;
import org.axan.sep.common.db.IUnitMarker;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ReturnableEvaluator;
import org.neo4j.graphdb.StopEvaluator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.Traverser.Order;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.axan.sep.common.db.IGameConfig;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.acl.Owner;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

class Player extends AGraphObject<Node> implements IPlayer, Serializable
{
	public static final String PK = "name";
	public static final String getPK(String name)
	{
		return name;
	}
	/*
	 * PK: first pk field.
	 */
	protected String name;
	
	/*
	 * Off-DB fields
	 */
	private IPlayerConfig config;
	
	/*
	 * DB connection: DB connection and useful objects (e.g. indexes and nodes).
	 */
	private transient Index<Node> playerIndex;
	private transient Node playersFactory;	
	
	/**
	 * Off-DB constructor.
	 * Off-DB constructor is full params off-db.
	 * @param name
	 * @param config
	 */
	public Player(String name, IPlayerConfig config)
	{
		super(name);
		this.name = name;
		this.config = config;
	}
	
	public Player(SEPCommonDB sepDB, String name)
	{
		super(sepDB, name);
		this.name = name;
		this.config = null;
	}
	
	/**
	 * If object is DB connected, check for DB update.
	 */
	@Override
	final protected void checkForDBUpdate()
	{
		if (!isDBOnline()) return;
		if (isDBOutdated())
		{
			db = sepDB.getDB();
			
			playersFactory = db.getReferenceNode().getSingleRelationship(eRelationTypes.Players, Direction.OUTGOING).getEndNode();
			playerIndex = db.index().forNodes("PlayerIndex");
			IndexHits<Node> hits = playerIndex.get(Player.PK, Player.getPK(name));
			properties = hits.hasNext() ? hits.getSingle() : null;			
		}
	}
	
	/**
	 * Current object must be Off-DB to call create method.
	 * Create method connect the object to the given DB and create the object node.
	 * After this call, object is DB connected.
	 * @param sepDB
	 */
	@Override
	final protected void create(SEPCommonDB sepDB)
	{
		assertOnlineStatus(false, "Illegal state: can only call create(SEPCommonDB) method on Off-DB objects.");		
		Transaction tx = sepDB.getDB().beginTx();
		
		try
		{
			this.sepDB = sepDB;
			checkForDBUpdate();
			
			if (playerIndex.get(Player.PK, Player.getPK(name)).hasNext())
			{
				tx.failure();
				throw new DBGraphException("Constraint error: Indexed field 'name' must be unique, player[name='"+name+"'] already exist.");
			}
			
			properties = sepDB.getDB().createNode();
			Player.initializeProperties(properties, name);
			playerIndex.add(properties, PK, getPK(name));
			playersFactory.createRelationshipTo(properties, eRelationTypes.Players);
			Node nConfig = sepDB.getDB().createNode();
			PlayerConfig.initializeNode(nConfig, config.getColor(), config.getSymbol(), config.getPortrait());
			properties.createRelationshipTo(nConfig, eRelationTypes.PlayerConfig);
			
			tx.success();			
		}
		finally
		{
			tx.finish();
		}
	}	

	@Override
	public String getName()
	{
		return name;
	}
	
	@Override
	public IPlayerConfig getConfig()
	{
		if (isDBOnline())
		{
			checkForDBUpdate();
			Node nConfig = properties.getSingleRelationship(eRelationTypes.PlayerConfig, Direction.OUTGOING).getEndNode();
			config = new PlayerConfig(nConfig);
		}
		return config;
	}
	
	@Override
	public IDiplomacy getDiplomacy(String targetName)
	{		
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		Diplomacy d = new Diplomacy(sepDB, name, targetName);
		if (!d.exists()) return null;
		
		return d;
	}
	
	@Override
	public void setDiplomacy(String targetName, boolean isAllowedToLand, eForeignPolicy foreignPolicy)
	{
		assertOnlineStatus(true);
		checkForDBUpdate();		
		
		Diplomacy d = new Diplomacy(sepDB, name, targetName);
		if (!d.exists())
		{
			d = new Diplomacy(name, targetName, isAllowedToLand, foreignPolicy);
			d.create(sepDB);
		}
		else
		{
			d.setAllowedToLand(isAllowedToLand);
			d.setForeignPolicy(foreignPolicy);
		}
	}
	
	@Override
	public Set<? extends IUnit> getUnits(eUnitType type)
	{
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		Set<IUnit> result = new HashSet<IUnit>();
				
		for(Node n : properties.traverse(Order.BREADTH_FIRST, StopEvaluator.DEPTH_ONE, ReturnableEvaluator.ALL_BUT_START_NODE, eRelationTypes.PlayerUnit, Direction.OUTGOING))
		{
			if (type != null && !type.toString().equals((String) n.getProperty("type"))) continue;
			
			IUnit unit = sepDB.getUnit((String) n.getProperty("ownerName"), (String) n.getProperty("name"), eUnitType.valueOf((String) n.getProperty("type")));
			result.add(unit);
		}
		
		return result;
	}
	
	@Override
	public IDiplomacyMarker getDiplomacyMarker(String targetName)
	{
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		Diplomacy diplomacy = new Diplomacy(sepDB, getName(), targetName);
		if (diplomacy.exists()) return diplomacy;
		
		IDiplomacyMarker result = null;
		
		for(Relationship r : properties.getRelationships(Direction.OUTGOING, eRelationTypes.PlayerDiplomacyMarker))
		{
			if (!targetName.equals(r.getEndNode().getProperty("name"))) continue;
			
			IDiplomacyMarker diplomacyMarker = new DiplomacyMarker(sepDB, (Integer) r.getProperty("turn"), getName(), targetName);
			if (result == null || result.getTurn() < diplomacyMarker.getTurn()) result = diplomacyMarker;		
		}
		
		if (sepDB.hasPrevious())
		{
			IDiplomacyMarker pResult = ((Player) sepDB.previous().getPlayer(getName())).getDiplomacyMarker(targetName);
			if ((result == null && pResult != null) || (result != null && pResult != null && result.getTurn() < pResult.getTurn())) result = pResult;
		}
		
		return result;
	}
	
	@Override
	public void setDiplomacyMarker(String targetName, boolean isAllowedToLand, eForeignPolicy foreignPolicy)
	{
		assertOnlineStatus(true);
		checkForDBUpdate();

		DiplomacyMarker d = new DiplomacyMarker(getVersion(), name, targetName, isAllowedToLand, foreignPolicy);
		d.create(sepDB);		
	}
	
	@Override
	public IUnitMarker getUnitMarker(String name)
	{
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		IUnitMarker result = null;
		
		for(Node n : properties.traverse(Order.BREADTH_FIRST, StopEvaluator.DEPTH_ONE, ReturnableEvaluator.ALL_BUT_START_NODE, eRelationTypes.PlayerUnitMarker, Direction.OUTGOING, eRelationTypes.PlayerUnit, Direction.OUTGOING))
		{
			if (!name.equals(n.getProperty("name"))) continue;
			
			IUnitMarker unitMarker = null; 
			
			if (n.hasProperty("turn")) // UnitMarker
			{
				unitMarker = sepDB.getUnitMarker((Integer) n.getProperty("turn"), (Double) n.getProperty("step"), getName(), name, null);
			}
			
			IUnit unit = sepDB.getUnit(getName(), name, null);
			
			if (unit != null && (unitMarker == null || unit.getTurn() >= unitMarker.getTurn())) unitMarker = unit;
			
			if (result == null || result.getTurn() < unitMarker.getTurn()) result = unitMarker;
		}
		
		if (sepDB.hasPrevious())
		{
			IUnitMarker pResult = ((Player) sepDB.previous().getPlayer(getName())).getUnitMarker(name);
			if ((result == null && pResult != null) || (result != null && pResult != null && result.getTurn() < pResult.getTurn())) result = pResult;
		}
		
		return result;
	}
	
	@Override
	public Set<? extends IUnitMarker> getUnitsMarkers(eUnitType type)
	{
		return getUnitsMarkers(type, new HashMap<String, IUnitMarker>());
	}
	
	private static transient Map<String, Map<String, IUnitMarker>> previousUnitsMarkers = null;	
	
	private Set<? extends IUnitMarker> getUnitsMarkers(eUnitType type, Map<String, IUnitMarker> result)
	{
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		for(Node n : properties.traverse(Order.BREADTH_FIRST, StopEvaluator.DEPTH_ONE, ReturnableEvaluator.ALL_BUT_START_NODE, eRelationTypes.PlayerUnitMarker, Direction.OUTGOING, eRelationTypes.PlayerUnit, Direction.OUTGOING))
		{
			if (type != null && !type.toString().equals((String) n.getProperty("type"))) continue;
			
			IUnitMarker unitMarker = null;
			
			if (n.hasProperty("turn")) // UnitMarker
			{
				unitMarker = sepDB.getUnitMarker((Integer) n.getProperty("turn"), (Double) n.getProperty("step"), getName(), (String) n.getProperty("name"), type);
			}
			
			IUnit unit = sepDB.getUnit(getName(), (String) n.getProperty("name"), type);
			
			if (unit != null && (unitMarker == null || unit.getTurn() >= unitMarker.getTurn())) unitMarker = unit;
			
			String pk = Unit.getPK(getName(), unitMarker.getName());
									
			
			
			if (!result.containsKey(pk) || (result.get(pk).getTurn() < unitMarker.getTurn())) result.put(pk, unitMarker);
		}
		
		if (previousUnitsMarkers == null)
		{
			previousUnitsMarkers = new HashMap<String, Map<String, IUnitMarker>>();
		}
		
		int hash = sepDB.hashCode();
		String cacheKey = String.format("%d-%s", hash, getName());
		if (!previousUnitsMarkers.containsKey(cacheKey))
		{	
			previousUnitsMarkers.put(cacheKey, new HashMap<String, IUnitMarker>());
						
			if (sepDB.hasPrevious())
			{
				((Player) sepDB.previous().getPlayer(getName())).getUnitsMarkers(null, previousUnitsMarkers.get(cacheKey));
			}
		}
		
		for(String pk : previousUnitsMarkers.get(cacheKey).keySet())
		{
			IUnitMarker unitMarker = previousUnitsMarkers.get(cacheKey).get(pk);
			
			if ((!result.containsKey(pk) || (result.get(pk).getTurn() < unitMarker.getTurn())) && (type == null || type.equals(unitMarker.getType())))
			{
				result.put(pk, unitMarker);
			}			
		}
		
		return new HashSet<IUnitMarker>(result.values());
	}
	
	@Override
	public IGovernmentModule getGovernmentModule()
	{
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		Relationship r = properties.getSingleRelationship(eRelationTypes.PlayerGovernment, Direction.OUTGOING);
		if (r == null) return null;
		
		Node n = r.getEndNode();
		if (!n.hasProperty("type")) return null;
		
		if (!eBuildingType.GovernmentModule.toString().equals((String) n.getProperty("type"))) return null;
		
		return (IGovernmentModule) sepDB.getBuilding((String) n.getProperty("productiveCelestialBodyName"), eBuildingType.GovernmentModule);
	}

	public static void initializeProperties(Node properties, String name)
	{
		properties.setProperty("name", name);
	}

	private synchronized void writeObject(ObjectOutputStream out) throws IOException
	{
		out.writeUTF(name);
		out.writeObject(config);
	}
	
	private synchronized void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		this.name = in.readUTF();
		this.config = (IPlayerConfig) in.readObject();
	}
}
