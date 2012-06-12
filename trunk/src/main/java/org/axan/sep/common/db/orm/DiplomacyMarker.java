package org.axan.sep.common.db.orm;

import org.axan.eplib.orm.nosql.AVersionedGraphRelationship;
import org.axan.sep.common.db.IDiplomacyMarker;
import org.axan.sep.common.db.orm.SEPCommonDB.eRelationTypes;
import org.neo4j.graphdb.Relationship;

public class DiplomacyMarker extends AVersionedGraphRelationship<SEPCommonDB> implements IDiplomacyMarker
{
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
		super(eRelationTypes.PlayerDiplomacyMarker, "PlayerIndex", Player.getPK(ownerName), "PlayerIndex", Player.getPK(targetName), turn);
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
		super(sepDB, eRelationTypes.PlayerDiplomacyMarker, "PlayerIndex", Player.getPK(ownerName), "PlayerIndex", Player.getPK(targetName), turn);
		this.turn = turn;
		this.ownerName = ownerName;
		this.targetName = targetName;
		
		// Null values
		this.isAllowedToLand = false;
		this.foreignPolicy = null;
	}
	
	@Override
	final protected void checkForDBUpdate()
	{
		super.checkForDBUpdate();
	}
	
	@Override
	final protected void initializeProperties()
	{
		super.initializeProperties();
		if (turn >= 0) properties.setProperty("turn", turn);
		properties.setProperty("ownerName", ownerName);
		properties.setProperty("targetName", targetName);
		properties.setProperty("isAllowedToLand", isAllowedToLand);
		properties.setProperty("foreignPolicy", foreignPolicy.toString());
	}
	
	@Override
	final protected void register(Relationship properties)
	{
		super.register(properties);
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
}
