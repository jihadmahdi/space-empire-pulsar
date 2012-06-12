package org.axan.sep.common.db.orm;

import javax.annotation.OverridingMethodsMustInvokeSuper;

import org.axan.eplib.orm.nosql.AVersionedGraphRelationship;
import org.axan.eplib.orm.nosql.DBGraphException;
import org.axan.sep.common.Protocol.eBuildingType;
import org.axan.sep.common.db.ISpaceRoad;
import org.axan.sep.common.db.orm.SEPCommonDB.eRelationTypes;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;

public class SpaceRoad extends AVersionedGraphRelationship<SEPCommonDB> implements ISpaceRoad
{
	/*
	 * PK
	 */
	protected String sourceName;
	protected String destinationName;
	
	/*
	 * Off-DB fields.
	 */
	
	/*
	 * DB connection
	 */
	
	/**
	 * Off-DB constructor.
	 * @param sourceName
	 * @param destinationName
	 * @param speed
	 */
	public SpaceRoad(String sourceName, String destinationName)
	{
		super(eRelationTypes.SpaceRoad, "BuildingIndex", Building.getPK(sourceName, eBuildingType.SpaceCounter), "BuildingIndex", Building.getPK(destinationName, eBuildingType.SpaceCounter));
		this.sourceName = sourceName;
		this.destinationName = destinationName;
	}
	
	public SpaceRoad(SEPCommonDB sepDB, String sourceName, String destinationName)
	{
		super(sepDB, eRelationTypes.SpaceRoad, "BuildingIndex", Building.getPK(sourceName, eBuildingType.SpaceCounter), "BuildingIndex", Building.getPK(destinationName, eBuildingType.SpaceCounter));
		this.sourceName = sourceName;
		this.destinationName = destinationName;
	}
	
	/**
	 * If object is DB connected, check for DB update.
	 */
	@Override
	final protected void checkForDBUpdate()
	{
		super.checkForDBUpdate();
	}

	@Override
	final protected void initializeProperties()
	{
		super.initializeProperties();
		properties.setProperty("sourceName", sourceName);
		properties.setProperty("destinationName", destinationName);
	}
	
	@Override
	final protected void register(Relationship properties)
	{
		super.register(properties);
	}
	
	@Override
	@OverridingMethodsMustInvokeSuper
	public void destroy()
	{
		delete();
	}
	
	@Override
	public String getSourceName()
	{
		return sourceName;
	}
	
	@Override
	public String getDestinationName()
	{
		return destinationName;
	}
	
	@Override
	public String toString()
	{
		return sourceName+" -> "+destinationName;
	}	
}
