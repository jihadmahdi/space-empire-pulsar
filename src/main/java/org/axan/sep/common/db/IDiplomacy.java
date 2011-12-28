package org.axan.sep.common.db;

import org.neo4j.graphdb.Node;
import org.axan.sep.common.db.IGameConfig;
import java.util.Map;
import java.util.HashMap;

public interface IDiplomacy
{
	public boolean getAllowToLand();
	public Map<String, Object> getNode();
	/*	
	public String getForeignPolicy();
	public String getOwner();
	public String getTarget();
	*/
}
