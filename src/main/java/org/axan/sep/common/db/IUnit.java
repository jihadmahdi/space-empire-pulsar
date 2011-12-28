package org.axan.sep.common.db;

import org.neo4j.graphdb.Node;
import org.axan.sep.common.db.IGameConfig;
import java.util.Map;
import java.util.HashMap;

public interface IUnit
{
	String getName();
	double getProgress();
	String getOwner();
	/*
	
	public eUnitType getType();
	public Location getDeparture();
	public Location getDestination();
	public float getSight();
	public boolean isMoving();	
	*/
}
