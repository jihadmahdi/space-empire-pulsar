package org.axan.sep.common.db.orm;

import org.axan.sep.common.db.orm.base.IBasePlayer;
import org.axan.sep.common.db.orm.base.BasePlayer;
import org.axan.sep.common.db.IPlayer;
import org.neo4j.graphdb.Node;
import org.axan.sep.common.db.IGameConfig;

import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;

public class Player implements IPlayer, Serializable
{
	private final IBasePlayer basePlayerProxy;

	Player(IBasePlayer basePlayerProxy)
	{
		this.basePlayerProxy = basePlayerProxy;
	}

	public Player(String name)
	{
		this(new BasePlayer(name));
	}

	public Player(Node stmnt)
	{
		this(new BasePlayer(stmnt));
	}

	@Override
	public int compareTo(IPlayer o)
	{
		return getName().compareTo(o.getName());
	}
	
	@Override
	public String getName()
	{
		return basePlayerProxy.getName();
	}

	@Override
	public IPlayerConfig getConfig(SEPCommonDB db)
	{
		return db.getPlayerConfig(getName());
	}

	@Override
	public Map<String, Object> getNode()
	{
		return basePlayerProxy.getNode();
	}

	public static void initializeNode(Node node, String name)
	{
		node.setProperty("name", name);
	}

}
