package org.axan.sep.common.db.orm;

import javax.annotation.OverridingMethodsMustInvokeSuper;

import org.axan.eplib.orm.nosql.DBGraphException;
import org.axan.sep.common.db.IDiplomacyMarker;
import org.axan.sep.common.db.IDiplomacyMarker.eForeignPolicy;
import org.axan.sep.common.db.orm.SEPCommonDB.eRelationTypes;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;

public class DiplomacyMarker extends AGraphObject<Relationship> implements IDiplomacyMarker
{
	protected static final String PK = "[turn] ownerNameVtargetName";
	protected static final String getPK(int turn, String ownerName, String targetName)
	{
		return String.format("[%d] %sV%s", turn, ownerName, targetName);
	}
	
	/*
	 * PK
	 */
	protected final int turn;
	protected final String ownerName;
	protected final String targetName;
	
	/*
	 * Off-DB fields.
	 */
	protected boolean isAllowedToLand;
	protected eForeignPolicy foreignPolicy;
	
	/*
	 * DB connection
	 */
	protected Index<Relationship> diplomacyMarkerIndex;
		
	/**
	 * Off-DB constructor.
	 * @param turn
	 * @param ownerName
	 * @param targetName
	 * @param isAllowedToLand
	 * @param foreignPolicy
	 */
	public DiplomacyMarker(int turn, String ownerName, String targetName, boolean isAllowedToLand, eForeignPolicy foreignPolicy)
	{
		super(getPK(turn, ownerName, targetName));
		this.turn = turn;
		this.ownerName = ownerName;
		this.targetName = targetName;
		this.isAllowedToLand = isAllowedToLand;
		this.foreignPolicy = foreignPolicy;
	}
	
	/**
	 * On-DB constructor.
	 * @param sepDB
	 * @param turn
	 * @param ownerName
	 * @param targetName
	 */
	public DiplomacyMarker(SEPCommonDB sepDB, int turn, String ownerName, String targetName)
	{
		super(sepDB, getPK(turn, ownerName, targetName));
		this.turn = turn;
		this.ownerName = ownerName;
		this.targetName = targetName;
		
		// Null values
		this.isAllowedToLand = false;
		this.foreignPolicy = null;
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
			
			diplomacyMarkerIndex = db.index().forRelationships("DiplomacyMarkerIndex");
			IndexHits<Relationship> hits = diplomacyMarkerIndex.get(PK, getPK(turn, ownerName, targetName));
			
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
		
			if (diplomacyMarkerIndex.get(PK, getPK(turn, ownerName, targetName)).hasNext())
			{
				tx.failure();
				throw new DBGraphException("Constraint error: Indexed field '"+PK+"' must be unique, diplomacyMarker["+getPK(turn, ownerName, targetName)+"] already exist.");
			}			
						
			diplomacyMarkerIndex.add(properties, PK, getPK(turn, ownerName, targetName));
			
			Node nOwner = db.index().forNodes("PlayerIndex").get("name", ownerName).getSingle();
			if (nOwner == null)
			{
				tx.failure();
				throw new DBGraphException("Constraint error: Cannot find owner Player '"+ownerName+"'");
			}
			
			Node nTarget = db.index().forNodes("PlayerIndex").get("name", targetName).getSingle();
			if (nTarget == null)
			{
				tx.failure();
				throw new DBGraphException("Constraint error: Cannot find target Player '"+targetName+"'");
			}
			
			Relationship existingDiplomacy=null;
			for(Relationship r : nOwner.getRelationships(eRelationTypes.PlayerDiplomacy, Direction.OUTGOING))
			{
				if (targetName.equals(r.getEndNode().getProperty("name")))
				{
					existingDiplomacy = r;
					break;
				}
			}
			
			if (existingDiplomacy != null)
			{
				tx.failure();
				throw new DBGraphException("Constraint error: Diplomacy relationship from '"+ownerName+"' to '"+targetName+"' already exists.");
			}
			
			properties = nOwner.createRelationshipTo(nTarget, eRelationTypes.PlayerDiplomacyMarker);
			DiplomacyMarker.initializeProperties(properties, turn, ownerName, targetName, isAllowedToLand, foreignPolicy);
			diplomacyMarkerIndex.add(properties, PK, getPK(turn, ownerName, targetName));			
			
			tx.success();			
		}
		finally
		{
			tx.finish();
		}
	}
	
	@Override
	public int getTurn()
	{
		return turn;
	}
	
	@Override
	public String getOwnerName()
	{
		return ownerName;
	}

	@Override
	public String getTargetName()
	{
		return targetName;
	}

	@Override
	public boolean isAllowedToLand()
	{
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		return (Boolean) properties.getProperty("isAllowedToLand");
	}

	@Override
	public eForeignPolicy getForeignPolicy()
	{
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		return eForeignPolicy.valueOf((String) properties.getProperty("foreignPolicy"));
	}

	public static void initializeProperties(Relationship properties, int turn, String ownerName, String targetName, boolean isAllowedToLand, eForeignPolicy foreignPolicy)
	{
		properties.setProperty("turn", turn);
		properties.setProperty("ownerName", ownerName);
		properties.setProperty("targetName", targetName);
		properties.setProperty("isAllowedToLand", isAllowedToLand);
		properties.setProperty("foreignPolicy", foreignPolicy.toString());
	}
}
