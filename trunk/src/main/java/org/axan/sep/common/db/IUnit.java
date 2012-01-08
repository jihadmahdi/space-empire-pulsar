package org.axan.sep.common.db;

import org.neo4j.graphdb.Node;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.SEPUtils.RealLocation;
import org.axan.sep.common.db.IGameConfig;
import java.util.Map;
import java.util.HashMap;

public interface IUnit
{
	String getOwnerName();
	String getName();		
	double getTravellingProgress();
	boolean isStopped();
	Location getDeparture();
	String getInitialDepartureName();
	Location getDestination();
	RealLocation getRealLocation();
	eUnitType getType();
	/*
	public float getSight();	
	*/
		
}
