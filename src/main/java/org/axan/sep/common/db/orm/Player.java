package org.axan.sep.common.db.orm;

import org.axan.eplib.orm.nosql.DBGraphException;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.orm.SEPCommonDB.eRelationTypes;
import org.axan.sep.common.db.orm.base.IBasePlayer;
import org.axan.sep.common.db.orm.base.BasePlayer;
import org.axan.sep.common.db.IPlayer;
import org.axan.sep.common.db.IPlayerConfig;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.axan.sep.common.db.IGameConfig;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;

class Player extends AGraphObject implements IPlayer, Serializable
{
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
			IndexHits<Node> hits = playerIndex.get("name", name);
			node = hits.hasNext() ? hits.getSingle() : null;			
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
			
			if (playerIndex.get("name", name.toString()).hasNext())
			{
				tx.failure();
				throw new DBGraphException("Constraint error: Indexed field 'name' must be unique, player[name='"+name+"'] already exist.");
			}
			
			node = sepDB.getDB().createNode();
			Player.initializeNode(node, name);
			playerIndex.add(node, "name", name);
			playersFactory.createRelationshipTo(node, eRelationTypes.Players);
			Node nConfig = sepDB.getDB().createNode();
			PlayerConfig.initializeNode(nConfig, config.getColor(), config.getSymbol(), config.getPortrait());
			node.createRelationshipTo(nConfig, eRelationTypes.PlayerConfig);
			
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
			Node nConfig = node.getSingleRelationship(eRelationTypes.PlayerConfig, Direction.OUTGOING).getEndNode();
			config = new PlayerConfig(nConfig);
		}
		return config;
	}

	public static void initializeNode(Node node, String name)
	{
		node.setProperty("name", name);
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
