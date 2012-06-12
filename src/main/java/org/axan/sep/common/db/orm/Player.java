package org.axan.sep.common.db.orm;

import org.axan.eplib.orm.nosql.AGraphNode;
import org.axan.eplib.orm.nosql.AVersionedGraphNode;
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

import javax.annotation.OverridingMethodsMustInvokeSuper;

class Player extends AGraphNode<SEPCommonDB> implements IPlayer, Serializable
{
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
	private transient Node playersFactory;	
	
	/**
	 * Off-DB constructor.
	 * Off-DB constructor is full params off-db.
	 * @param name
	 * @param config
	 */
	public Player(String name, IPlayerConfig config)
	{
		super(getPK(name));
		this.name = name;
		this.config = config;
	}
	
	public Player(SEPCommonDB sepDB, String name)
	{
		super(sepDB, getPK(name));
		this.name = name;
		this.config = null;
	}
	
	/**
	 * If object is DB connected, check for DB update.
	 */
	@Override
	final protected void checkForDBUpdate()
	{
		super.checkForDBUpdate();
		
		if (!isDBOnline()) return;
		if (isDBOutdated() || playersFactory == null)
		{
			playersFactory = graphDB.getDB().getReferenceNode().getSingleRelationship(eRelationTypes.Players, Direction.OUTGOING).getEndNode(); // checked						
		}		
	}
	
	@Override
	final protected void initializeProperties()
	{
		super.initializeProperties();
		properties.setProperty("name", name);
	}
	
	/**
	 * Register properties (add Node to indexes and create relationships).
	 * @param properties
	 */
	@Override
	@OverridingMethodsMustInvokeSuper
	final protected void register(Node properties)
	{
		assertOnlineStatus(true);
		Transaction tx = graphDB.getDB().beginTx();
		
		try
		{
			super.register(properties);
			
			playersFactory.createRelationshipTo(properties, eRelationTypes.Players); // checked			
			Node nConfig = graphDB.getDB().createNode();
			PlayerConfig.initializeNode(nConfig, config.getColor(), config.getSymbol(), config.getPortrait());
			properties.createRelationshipTo(nConfig, eRelationTypes.PlayerConfig); // checked
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
			Node nConfig = properties.getSingleRelationship(eRelationTypes.PlayerConfig, Direction.OUTGOING).getEndNode(); // checked
			config = new PlayerConfig(nConfig);
		}
		return config;
	}
	
	@Override
	public IDiplomacy getDiplomacy(String targetName)
	{		
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		Diplomacy d = new Diplomacy(graphDB, name, targetName);
		if (!d.exists()) return null;
		
		return d;
	}
	
	@Override
	public void setDiplomacy(String targetName, boolean isAllowedToLand, eForeignPolicy foreignPolicy)
	{
		assertOnlineStatus(true);
		checkForDBUpdate();		
		
		Diplomacy d = new Diplomacy(graphDB, name, targetName);
		if (!d.exists())
		{
			d = new Diplomacy(name, targetName, isAllowedToLand, foreignPolicy);
			d.create(graphDB);
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
				
		for(Relationship r : AVersionedGraphNode.getLastRelationships(graphDB, properties, graphDB.getVersion(), eRelationTypes.PlayerUnit, Direction.OUTGOING))
		{
			Node n = r.getOtherNode(properties);
			if (type != null && !type.toString().equals((String) n.getProperty("type"))) continue;
			
			IUnit unit = graphDB.getUnit((String) n.getProperty("ownerName"), (String) n.getProperty("name"), eUnitType.valueOf((String) n.getProperty("type")));
			result.add(unit);
		}
		
		return result;
	}
	
	@Override
	public IDiplomacyMarker getDiplomacyMarker(String targetName)
	{
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		Diplomacy diplomacy = new Diplomacy(graphDB, getName(), targetName);
		if (diplomacy.exists()) return diplomacy;
		
		IDiplomacyMarker result = null;
		
		for(Relationship r : AVersionedGraphNode.getLastRelationships(graphDB, properties, graphDB.getVersion(), Direction.OUTGOING, eRelationTypes.PlayerDiplomacyMarker))
		{
			if (!targetName.equals(r.getEndNode().getProperty("name"))) continue;
			
			IDiplomacyMarker diplomacyMarker = new DiplomacyMarker(graphDB, (Integer) r.getProperty("turn"), getName(), targetName);
			if (result == null || result.getTurn() < diplomacyMarker.getTurn()) result = diplomacyMarker;		
		}
		
		if (graphDB.hasPrevious())
		{
			IDiplomacyMarker pResult = ((Player) graphDB.previous().getPlayer(getName())).getDiplomacyMarker(targetName);
			if ((result == null && pResult != null) || (result != null && pResult != null && result.getTurn() < pResult.getTurn())) result = pResult;
		}
		
		return result;
	}
	
	@Override
	public void setDiplomacyMarker(String targetName, boolean isAllowedToLand, eForeignPolicy foreignPolicy)
	{
		assertOnlineStatus(true);
		checkForDBUpdate();

		DiplomacyMarker d = new DiplomacyMarker(graphDB.getVersion(), name, targetName, isAllowedToLand, foreignPolicy);
		d.create(graphDB);		
	}
	
	@Override
	public IUnitMarker getUnitMarker(String name)
	{
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		IUnitMarker result = null;
		
		for(Relationship r : AVersionedGraphNode.getLastRelationships(graphDB, properties, graphDB.getVersion(), Direction.OUTGOING, eRelationTypes.PlayerUnitMarker, eRelationTypes.PlayerUnit))
		{
			Node n = r.getEndNode();
			if (!name.equals(n.getProperty("name"))) continue;
			
			IUnitMarker unitMarker = null; 
			
			if (n.hasProperty("turn")) // UnitMarker
			{
				unitMarker = graphDB.getUnitMarker((Integer) n.getProperty("turn"), (Double) n.getProperty("step"), getName(), name, null);
			}
			
			IUnit unit = graphDB.getUnit(getName(), name, null);
			
			if (unit != null && (unitMarker == null || unit.getTurn() >= unitMarker.getTurn())) unitMarker = unit;
			
			if (result == null || result.getTurn() < unitMarker.getTurn()) result = unitMarker;
		}
		
		if (graphDB.hasPrevious())
		{
			IUnitMarker pResult = ((Player) graphDB.previous().getPlayer(getName())).getUnitMarker(name);
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
		
		for(Relationship r : AVersionedGraphNode.getLastRelationships(graphDB, properties, graphDB.getVersion(), Direction.OUTGOING, eRelationTypes.PlayerUnitMarker, eRelationTypes.PlayerUnit))
		{
			Node n = r.getEndNode();
			if (type != null && !type.toString().equals((String) n.getProperty("type"))) continue;
			
			IUnitMarker unitMarker = null;
			
			if (n.hasProperty("turn")) // UnitMarker
			{
				unitMarker = graphDB.getUnitMarker((Integer) n.getProperty("turn"), (Double) n.getProperty("step"), getName(), (String) n.getProperty("name"), type);
			}
			
			IUnit unit = graphDB.getUnit(getName(), (String) n.getProperty("name"), type);
			
			if (unit != null && (unitMarker == null || unit.getTurn() >= unitMarker.getTurn())) unitMarker = unit;
			
			String pk = Unit.getPK(getName(), unitMarker.getName());
									
			
			
			if (!result.containsKey(pk) || (result.get(pk).getTurn() < unitMarker.getTurn())) result.put(pk, unitMarker);
		}
		
		if (previousUnitsMarkers == null)
		{
			previousUnitsMarkers = new HashMap<String, Map<String, IUnitMarker>>();
		}
		
		int hash = graphDB.hashCode();
		String cacheKey = String.format("%d-%s", hash, getName());
		if (!previousUnitsMarkers.containsKey(cacheKey))
		{	
			previousUnitsMarkers.put(cacheKey, new HashMap<String, IUnitMarker>());
						
			if (graphDB.hasPrevious())
			{
				((Player) graphDB.previous().getPlayer(getName())).getUnitsMarkers(null, previousUnitsMarkers.get(cacheKey));
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
		
		Relationship r = AVersionedGraphNode.getLastSingleRelationship(graphDB, properties, graphDB.getVersion(), eRelationTypes.PlayerGovernment, Direction.OUTGOING);
		if (r == null) return null;
		
		Node n = r.getEndNode();
		if (!n.hasProperty("type")) return null;
		
		if (!eBuildingType.GovernmentModule.toString().equals((String) n.getProperty("type"))) return null;
		
		return (IGovernmentModule) graphDB.getBuilding((String) n.getProperty("productiveCelestialBodyName"), eBuildingType.GovernmentModule);
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
