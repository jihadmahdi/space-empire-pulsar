package org.axan.sep.common.db.orm;

import org.axan.sep.common.db.orm.base.IBaseDiplomacy;
import org.axan.sep.common.db.orm.base.BaseDiplomacy;
import org.axan.sep.common.db.IDiplomacy;
import org.neo4j.graphdb.Node;
import org.axan.sep.common.db.IGameConfig;
import java.util.Map;
import java.util.HashMap;

class Diplomacy implements IDiplomacy
{
	private final IBaseDiplomacy baseDiplomacyProxy;

	Diplomacy(IBaseDiplomacy baseDiplomacyProxy)
	{
		this.baseDiplomacyProxy = baseDiplomacyProxy;
	}

	public Diplomacy(boolean allowToLand)
	{
		this(new BaseDiplomacy(allowToLand));
	}

	public Diplomacy(Node stmnt)
	{
		this(new BaseDiplomacy(stmnt));
	}

	@Override
	public boolean getAllowToLand()
	{
		return baseDiplomacyProxy.getAllowToLand();
	}

	@Override
	public Map<String, Object> getNode()
	{
		return baseDiplomacyProxy.getNode();
	}

	public static void initializeNode(Node node, boolean allowToLand)
	{
		node.setProperty("allowToLand", allowToLand);
	}

}
