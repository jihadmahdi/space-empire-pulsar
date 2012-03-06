package org.axan.sep.common.db.orm;

import org.axan.eplib.orm.nosql.DBGraphException;
import org.axan.sep.common.db.IDiplomacy;
import org.axan.sep.common.db.IDiplomacyMarker;
import org.axan.sep.common.db.orm.SEPCommonDB.eRelationTypes;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;

class Diplomacy extends DiplomacyMarker implements IDiplomacy
{
	protected static final String PK = "ownerNameVtargetName";
	protected static final String getPK(String ownerName, String targetName)
	{
		return String.format("%sV%s", ownerName, targetName);
	}
	
	/*
	 * PK: inherited
	 */
	
	/*
	 * Off-DB fields.
	 */
	
	
	/*
	 * DB connection
	 */
	protected Index<Relationship> diplomacyIndex;
	
	/**
	 * Off-DB constructor.
	 * @param ownerName
	 * @param targetName
	 * @param isAllowedToLand
	 * @param foreignPolicy
	 */
	public Diplomacy(String ownerName, String targetName, boolean isAllowedToLand, eForeignPolicy foreignPolicy)
	{
		super(-1, ownerName, targetName, isAllowedToLand, foreignPolicy);
	}
	
	public Diplomacy(SEPCommonDB sepDB, String ownerName, String targetName)
	{
		super(sepDB, -1, ownerName, targetName);
	}
	
	/**
	 * If object is DB connected, check for DB update.
	 */
	@Override
	protected void checkForDBUpdate()
	{
		if (!isDBOnline()) return;
		if (isDBOutdated())
		{
			db = sepDB.getDB();
			
			diplomacyIndex = db.index().forRelationships("DiplomacyIndex");
			IndexHits<Relationship> hits = diplomacyIndex.get(PK, getPK(ownerName, targetName));
			
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
	protected void create(SEPCommonDB sepDB)
	{
		assertOnlineStatus(false, "Illegal state: can only call create(SEPCommonDB) method on Off-DB objects.");
		
		Transaction tx = sepDB.getDB().beginTx();
		
		try
		{
			this.sepDB = sepDB;
			checkForDBUpdate();
			
			if (diplomacyIndex.get(PK, getPK(ownerName, targetName)).hasNext())
			{
				tx.failure();
				throw new DBGraphException("Constraint error: Indexed field '"+PK+"' must be unique, unit["+getPK(ownerName, targetName)+"] already exist.");
			}			

			Node nOwner = db.index().forNodes("PlayerIndex").get(Player.PK, Player.getPK(ownerName)).getSingle();
			if (nOwner == null)
			{
				tx.failure();
				throw new DBGraphException("Constraint error: Cannot find owner Player '"+ownerName+"'");
			}
			
			Node nTarget = db.index().forNodes("PlayerIndex").get(Player.PK, Player.getPK(targetName)).getSingle();
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
			
			properties = nOwner.createRelationshipTo(nTarget, eRelationTypes.PlayerDiplomacy);
			Diplomacy.initializeProperties(properties, ownerName, targetName, isAllowedToLand, foreignPolicy);
			diplomacyIndex.add(properties, PK, getPK(ownerName, targetName));			
			
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
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		return sepDB.getConfig().getTurn();
	}
	
	@Override
	public boolean isAllowedToLand()
	{
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		return (Boolean) properties.getProperty("isAllowedToLand");
	}
	
	@Override
	public void setAllowedToLand(boolean isAllowedToLand)
	{
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		Transaction tx = db.beginTx();
		
		try
		{
			properties.setProperty("isAllowedToLand", isAllowedToLand);
			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}
	
	@Override
	public eForeignPolicy getForeignPolicy()
	{
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		return eForeignPolicy.valueOf((String) properties.getProperty("foreignPolicy"));
	}	
	
	@Override
	public void setForeignPolicy(eForeignPolicy foreignPolicy)
	{
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		Transaction tx = db.beginTx();
		
		try
		{
			properties.setProperty("foreignPolicy", foreignPolicy.toString());
			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}
	
	@Override
	public IDiplomacyMarker getMarker()
	{
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		return new DiplomacyMarker(getTurn(), ownerName, targetName, isAllowedToLand(), getForeignPolicy());
	}
	
	public static void initializeProperties(Relationship properties, String ownerName, String targetName, boolean isAllowedToLand, eForeignPolicy foreignPolicy)
	{
		properties.setProperty("ownerName", ownerName);
		properties.setProperty("targetName", targetName);
		properties.setProperty("isAllowedToLand", isAllowedToLand);
		properties.setProperty("foreignPolicy", foreignPolicy.toString());
	}
}
