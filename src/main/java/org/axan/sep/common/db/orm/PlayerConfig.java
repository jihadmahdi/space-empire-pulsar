package org.axan.sep.common.db.orm;

import org.axan.sep.common.db.orm.base.IBasePlayerConfig;
import org.axan.sep.common.db.orm.base.BasePlayerConfig;
import org.axan.sep.common.db.IPlayerConfig;
import org.neo4j.graphdb.Node;
import org.axan.sep.common.db.IGameConfig;
import java.util.Map;
import java.util.HashMap;

public class PlayerConfig implements IPlayerConfig, Serializable
{
	private final IBasePlayerConfig basePlayerConfigProxy;

	PlayerConfig(IBasePlayerConfig basePlayerConfigProxy)
	{
		this.basePlayerConfigProxy = basePlayerConfigProxy;
	}

	public PlayerConfig(String color, String symbol, String portrait)
	{
		this(new BasePlayerConfig(color, symbol, portrait));
	}

	public PlayerConfig(Node stmnt)
	{
		this(new BasePlayerConfig(stmnt));
	}

	@Override
	public String getColor()
	{
		return basePlayerConfigProxy.getColor();
	}

	@Override
	public String getSymbol()
	{
		return basePlayerConfigProxy.getSymbol();
	}

	@Override
	public String getPortrait()
	{
		return basePlayerConfigProxy.getPortrait();
	}

	@Override
	public Map<String, Object> getNode()
	{
		return basePlayerConfigProxy.getNode();
	}

	public static void initializeNode(Node node, String color, String symbol, String portrait)
	{
		node.setProperty("color", color);
		node.setProperty("symbol", symbol);
		node.setProperty("portrait", portrait);
	}

}
