package org.axan.sep.server.model.orm;

import com.almworks.sqlite4java.SQLiteStatement;

import org.axan.sep.common.IGameConfig;
import org.axan.sep.server.model.orm.base.BasePlayerConfig;

public class PlayerConfig implements IPlayerConfig
{
	private final BasePlayerConfig basePlayerConfigProxy;

	public PlayerConfig(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		this.basePlayerConfigProxy = new BasePlayerConfig(stmnt);
	}

	public Byte[] getSymbol()
	{
		return basePlayerConfigProxy.getSymbol();
	}

	public String getColor()
	{
		return basePlayerConfigProxy.getColor();
	}

	public Byte[] getPortrait()
	{
		return basePlayerConfigProxy.getPortrait();
	}

	public String getName()
	{
		return basePlayerConfigProxy.getName();
	}

}
