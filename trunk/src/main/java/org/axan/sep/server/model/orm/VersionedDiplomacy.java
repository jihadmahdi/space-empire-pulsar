package org.axan.sep.server.model.orm;

import com.almworks.sqlite4java.SQLiteStatement;

import org.axan.sep.common.IGameConfig;
import org.axan.sep.server.model.orm.base.BaseVersionedDiplomacy;

public class VersionedDiplomacy implements IVersionedDiplomacy
{
	private final BaseVersionedDiplomacy baseVersionedDiplomacyProxy;

	public VersionedDiplomacy(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		this.baseVersionedDiplomacyProxy = new BaseVersionedDiplomacy(stmnt);
	}

	public Boolean getAllowToLand()
	{
		return baseVersionedDiplomacyProxy.getAllowToLand();
	}

	public Integer getTurn()
	{
		return baseVersionedDiplomacyProxy.getTurn();
	}

	public String getForeignPolicy()
	{
		return baseVersionedDiplomacyProxy.getForeignPolicy();
	}

	public String getCible()
	{
		return baseVersionedDiplomacyProxy.getCible();
	}

	public String getName()
	{
		return baseVersionedDiplomacyProxy.getName();
	}

}
