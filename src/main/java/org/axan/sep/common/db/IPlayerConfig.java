package org.axan.sep.common.db;

import org.neo4j.graphdb.Node;
import org.axan.sep.common.db.IGameConfig;
import java.util.Map;
import java.util.HashMap;

public interface IPlayerConfig
{
	public String getColor();
	public String getSymbol();
	public String getPortrait();
	public Map<String, Object> getNode();
}
