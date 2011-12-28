package org.axan.sep.common.db.orm;

import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.orm.SEPCommonDB.eRelationTypes;
import org.axan.sep.common.db.orm.base.IBasePlayerConfig;
import org.axan.sep.common.db.orm.base.BasePlayerConfig;
import org.axan.sep.common.db.IPlayerConfig;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.axan.sep.common.db.IGameConfig;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;

class PlayerConfig implements IPlayerConfig, Serializable
{
	// Off-DB
	private String color;
	private String symbol;
	private String portrait;
	
	PlayerConfig(Node stmnt)
	{
		this.color = stmnt.hasProperty("color") ? String.class.cast(stmnt.getProperty("color")) : null;
		this.symbol = stmnt.hasProperty("symbol") ? String.class.cast(stmnt.getProperty("symbol")) : null;
		this.portrait = stmnt.hasProperty("portrait") ? String.class.cast(stmnt.getProperty("portrait")) : null;
	}
	
	public PlayerConfig(String color, String symbol, String portrait)
	{
		this.color = color;
		this.symbol = symbol;
		this.portrait = portrait;
	}

	@Override
	public String getColor()
	{
		return color;
	}

	@Override
	public String getSymbol()
	{
		return symbol;
	}

	@Override
	public String getPortrait()
	{
		return portrait;
	}

	public static void initializeNode(Node node, String color, String symbol, String portrait)
	{
		node.setProperty("color", color);
		node.setProperty("symbol", symbol);
		node.setProperty("portrait", portrait);
	}

	private synchronized void writeObject(ObjectOutputStream out) throws IOException
	{
		out.writeUTF(color);
		out.writeUTF(symbol);
		out.writeUTF(portrait);
	}
	
	private synchronized void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		this.color = in.readUTF();
		this.symbol = in.readUTF();
		this.portrait = in.readUTF();
	}
}
