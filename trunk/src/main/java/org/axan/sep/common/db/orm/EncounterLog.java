package org.axan.sep.common.db.orm;

import javax.annotation.OverridingMethodsMustInvokeSuper;

import org.axan.eplib.orm.nosql.DBGraphException;
import org.axan.sep.common.db.ICelestialBody;
import org.axan.sep.common.db.IEncounterLog;
import org.axan.sep.common.db.IProductiveCelestialBody;
import org.axan.sep.common.db.IUnit;
import org.axan.sep.common.db.orm.SEPCommonDB.eRelationTypes;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;

public class EncounterLog extends AGraphObject<Node> implements IEncounterLog
{
	static final String PK = "[turn] observerOwnerName@observerName observation of encounterOwnerName@encounterName";
	static final String getPK(int turn, String observerOwnerName, String observerName, String encounterOwnerName, String encounterName)
	{
		return String.format("[%d] %s@%s observation of %s@%s", turn, observerOwnerName, observerName, encounterOwnerName, encounterName);
	}
	
	/*
	 * PK
	 */
	protected final String observerOwnerName;
	protected final String observerName;
	protected final String encounterOwnerName;
	protected final String encounterName;
	protected final int turn;
	
	/*
	 * DB connection
	 */
	protected Index<Node> encounterLogIndex;
	
	EncounterLog(String observerOwnerName, String observerName, String encounterOwnerName, String encounterName, int turn)
	{
		super(getPK(turn, observerOwnerName, observerName, encounterOwnerName, encounterName));
		this.turn = turn;
		this.observerOwnerName = observerOwnerName;
		this.observerName = observerName;
		this.encounterOwnerName = encounterOwnerName;
		this.encounterName = encounterName;
	}
	
	/**
	 * On-DB constructor
	 * @param sepDB
	 * @param observer
	 * @param encounter
	 */
	EncounterLog(SEPCommonDB sepDB, String observerOwnerName, String observerName, String encounterOwnerName, String encounterName, int turn)
	{
		super(sepDB, getPK(turn, observerOwnerName, observerName, encounterOwnerName, encounterName));
		this.turn = turn;
		this.observerOwnerName = observerOwnerName;
		this.observerName = observerName;
		this.encounterOwnerName = encounterOwnerName;
		this.encounterName = encounterName;
	}
	
	/**
	 * If object is DB connected, check for DB update.
	 */
	@Override
	@OverridingMethodsMustInvokeSuper
	protected void checkForDBUpdate()
	{
		if (!isDBOnline()) return;
		if (isDBOutdated())
		{
			db = sepDB.getDB();
			
			encounterLogIndex = db.index().forNodes("EncounterLogIndex");
			IndexHits<Node> hits = encounterLogIndex.get(PK, getPK(turn, observerOwnerName, observerName, encounterOwnerName, encounterName));
						
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
	@OverridingMethodsMustInvokeSuper
	protected void create(SEPCommonDB sepDB)
	{
		assertOnlineStatus(false, "Illegal state: can only call create(SEPCommonDB) method on Off-DB objects.");
		Transaction tx = sepDB.getDB().beginTx();
		
		try
		{
			this.sepDB = sepDB;
			checkForDBUpdate();
			
			if (encounterLogIndex.get(PK, getPK(turn, observerOwnerName, observerName, encounterOwnerName, encounterName)).hasNext())
			{
				tx.failure();
				throw new DBGraphException("Constraint error: Indexed fields  '"+PK+"' must be unique, encounterLog["+getPK(turn, observerOwnerName, observerName, encounterOwnerName, encounterName)+"] already exist.");
			}			

			properties = sepDB.getDB().createNode();
			EncounterLog.initializeProperties(properties, turn, observerOwnerName, observerName, encounterOwnerName, encounterName);			
			encounterLogIndex.add(properties, PK, getPK(turn, observerOwnerName, observerName, encounterOwnerName, encounterName));
			
			Node nObserver = db.index().forNodes("UnitIndex").get("ownerName@name", String.format("%s@%s", observerOwnerName, observerName)).getSingle();
			if (nObserver == null)
			{
				tx.failure();
				throw new DBGraphException("Constraint error: Cannot find observer Unit '"+observerOwnerName+"@"+observerName+"'");
			}
			nObserver.createRelationshipTo(properties, eRelationTypes.UnitEncounterLog);
			
			tx.success();			
		}
		finally
		{
			tx.finish();
		}
	}
	
	public static void initializeProperties(Node properties, int turn, String observerOwnerName, String observerName, String encounterOwnerName, String encounterName)
	{
		properties.setProperty("turn", turn);
		properties.setProperty("observerOwnerName", observerOwnerName);
		properties.setProperty("observerName", observerName);
		properties.setProperty("encounterOwnerName", encounterOwnerName);
		properties.setProperty("encounterName", encounterName);
	}
}
