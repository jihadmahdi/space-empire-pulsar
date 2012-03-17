package org.axan.sep.common.db.orm;

import javax.annotation.OverridingMethodsMustInvokeSuper;

import org.axan.eplib.orm.nosql.AVersionedGraphRelationship;
import org.axan.eplib.orm.nosql.DBGraphException;
import org.axan.sep.common.db.ICelestialBody;
import org.axan.sep.common.db.IEncounterLog;
import org.axan.sep.common.db.IProductiveCelestialBody;
import org.axan.sep.common.db.IUnit;
import org.axan.sep.common.db.IUnitMarker;
import org.axan.sep.common.db.orm.SEPCommonDB.eRelationTypes;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;

public class EncounterLog extends AVersionedGraphRelationship<SEPCommonDB> implements IEncounterLog
{
	/*
	 * PK
	 */
	protected final String observerOwnerName;
	protected final String observerName;
	protected final String encounterOwnerName;
	protected final String encounterName;
	protected final int turn;
	protected final double step;
	
	/*
	 * DB connection
	 */
	
	EncounterLog(String observerOwnerName, String observerName, String encounterOwnerName, String encounterName, int turn, double step)
	{
		super(eRelationTypes.UnitEncounterLog, "UnitIndex", Unit.getPK(observerOwnerName, observerName), "UnitMarkerIndex", UnitMarker.getPK(turn, step, encounterOwnerName, encounterName), turn, step);
		this.turn = turn;
		this.step = step;
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
	EncounterLog(SEPCommonDB sepDB, String observerOwnerName, String observerName, String encounterOwnerName, String encounterName, int turn, double step)
	{
		super(sepDB, eRelationTypes.UnitEncounterLog, "UnitIndex", Unit.getPK(observerOwnerName, observerName), "UnitMarkerIndex", UnitMarker.getPK(turn, step, encounterOwnerName, encounterName), turn, step);
		this.turn = turn;
		this.step = step;
		this.observerOwnerName = observerOwnerName;
		this.observerName = observerName;
		this.encounterOwnerName = encounterOwnerName;
		this.encounterName = encounterName;
	}
	
	@Override
	final protected void checkForDBUpdate()
	{
		super.checkForDBUpdate();
	}
	
	@Override
	protected void initializeProperties()
	{
		super.initializeProperties();		
		properties.setProperty("turn", turn);
		properties.setProperty("step", step);
		properties.setProperty("observerOwnerName", observerOwnerName);
		properties.setProperty("observerName", observerName);
		properties.setProperty("encounterOwnerName", encounterOwnerName);
		properties.setProperty("encounterName", encounterName);
	}
	
	@Override
	final protected void register(Relationship properties)
	{
		super.register(properties);
	}
	
	@Override
	public IUnitMarker getEncounter()
	{
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		return graphDB.getUnitMarker(turn, step, encounterOwnerName, encounterName, null);
	}
}
