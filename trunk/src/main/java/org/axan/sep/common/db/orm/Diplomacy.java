package org.axan.sep.common.db.orm;

import java.lang.Exception;
import org.axan.sep.common.db.orm.base.IBaseDiplomacy;
import org.axan.sep.common.db.orm.base.BaseDiplomacy;
import org.axan.sep.common.db.IDiplomacy;
import java.util.HashMap;
import java.util.Map;
import org.axan.sep.common.db.IGameConfig;
import org.neo4j.graphdb.Node;

public class Diplomacy implements IDiplomacy
{
	private final IBaseDiplomacy baseDiplomacyProxy;

	Diplomacy(IBaseDiplomacy baseDiplomacyProxy)
	{
		this.baseDiplomacyProxy = baseDiplomacyProxy;
	}

	public Diplomacy(String owner, String target, Boolean allowToLand, String foreignPolicy)
	{
		this(new BaseDiplomacy(owner, target, allowToLand, foreignPolicy));
	}

	public Diplomacy(Node stmnt) throws Exception
	{
		this(new BaseDiplomacy(stmnt));
	}

	@Override
	public String getOwner()
	{
		return baseDiplomacyProxy.getOwner();
	}

	@Override
	public String getTarget()
	{
		return baseDiplomacyProxy.getTarget();
	}

	@Override
	public Boolean getAllowToLand()
	{
		return baseDiplomacyProxy.getAllowToLand();
	}

	@Override
	public String getForeignPolicy()
	{
		return baseDiplomacyProxy.getForeignPolicy();
	}

	@Override
	public Map<String, Object> getNode()
	{
		return baseDiplomacyProxy.getNode();
	}

}
