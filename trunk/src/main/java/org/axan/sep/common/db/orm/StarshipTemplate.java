package org.axan.sep.common.db.orm;

import java.lang.Exception;
import org.axan.sep.common.db.orm.base.IBaseStarshipTemplate;
import org.axan.sep.common.db.orm.base.BaseStarshipTemplate;
import org.axan.sep.common.db.IStarshipTemplate;
import java.util.HashMap;
import java.util.Map;
import org.axan.sep.common.db.IGameConfig;
import org.neo4j.graphdb.Node;

public class StarshipTemplate implements IStarshipTemplate
{
	private final IBaseStarshipTemplate baseStarshipTemplateProxy;

	StarshipTemplate(IBaseStarshipTemplate baseStarshipTemplateProxy)
	{
		this.baseStarshipTemplateProxy = baseStarshipTemplateProxy;
	}

	public StarshipTemplate(String name, String specializedClass)
	{
		this(new BaseStarshipTemplate(name, specializedClass));
	}

	public StarshipTemplate(Node stmnt) throws Exception
	{
		this(new BaseStarshipTemplate(stmnt));
	}

	@Override
	public String getName()
	{
		return baseStarshipTemplateProxy.getName();
	}

	@Override
	public String getSpecializedClass()
	{
		return baseStarshipTemplateProxy.getSpecializedClass();
	}

	@Override
	public Map<String, Object> getNode()
	{
		return baseStarshipTemplateProxy.getNode();
	}

}
