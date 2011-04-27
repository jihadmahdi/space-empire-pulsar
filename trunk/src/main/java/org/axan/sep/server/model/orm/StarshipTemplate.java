package org.axan.sep.server.model.orm;

import com.almworks.sqlite4java.SQLiteStatement;

import org.axan.sep.common.IGameConfig;
import org.axan.sep.server.model.orm.base.BaseStarshipTemplate;

public class StarshipTemplate implements IStarshipTemplate
{
	private final BaseStarshipTemplate baseStarshipTemplateProxy;

	public StarshipTemplate(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		this.baseStarshipTemplateProxy = new BaseStarshipTemplate(stmnt);
	}

	public String getSpecializedClass()
	{
		return baseStarshipTemplateProxy.getSpecializedClass();
	}

	public String getName()
	{
		return baseStarshipTemplateProxy.getName();
	}

}
