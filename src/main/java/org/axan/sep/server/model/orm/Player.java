package org.axan.sep.server.model.orm;

import com.almworks.sqlite4java.SQLiteStatement;

import org.axan.sep.common.IGameConfig;
import org.axan.sep.server.model.orm.base.BasePlayer;

public class Player implements IPlayer
{
	private final BasePlayer basePlayerProxy;

	public Player(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		this.basePlayerProxy = new BasePlayer(stmnt);
	}

	public String getName()
	{
		return basePlayerProxy.getName();
	}

}
