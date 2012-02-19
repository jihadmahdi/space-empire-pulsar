package org.axan.sep.common.db;

import org.axan.sep.common.db.IBuilding;
import org.neo4j.graphdb.Node;
import org.axan.sep.common.db.IGameConfig;
import java.util.Map;
import java.util.HashMap;

public interface IPulsarLaunchingPad extends IBuilding
{
	/** Number of pulsar already fired (consumed slots). */
	int getNbFired();
	// public Integer getFiredDate();
}
