package org.axan.sep.common.db;

import org.neo4j.graphdb.Node;
import org.axan.sep.common.db.IGameConfig;

import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;

public interface IPlayerConfig extends Serializable
{
	String getColor();
	String getSymbol();
	String getPortrait();
}
