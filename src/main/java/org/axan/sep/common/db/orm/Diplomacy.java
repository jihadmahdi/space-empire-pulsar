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
	/*
	 * PK: inherited
	 */
	
	/*
	 * Off-DB fields.
	 */
	
	
	/*
	 * DB connection
	 */
	
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
	
	@Override
	public int getTurn()
	{
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		return graphDB.getVersion();
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
		
		Transaction tx = graphDB.getDB().beginTx();
		
		try
		{
			if (isAllowedToLand != isAllowedToLand())
			{
				prepareUpdate();
				properties.setProperty("isAllowedToLand", isAllowedToLand);
			}
						
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
		
		Transaction tx = graphDB.getDB().beginTx();
		
		try
		{
			if (!foreignPolicy.equals(getForeignPolicy()))
			{
				prepareUpdate();
				properties.setProperty("foreignPolicy", foreignPolicy.toString());
			}
						
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
}
