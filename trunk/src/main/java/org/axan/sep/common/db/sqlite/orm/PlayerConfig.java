package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.base.BasePlayerConfig;
import org.axan.sep.common.db.IPlayerConfig;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteStatement;
import java.util.HashSet;
import java.util.Set;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;
import org.axan.sep.common.db.IGameConfig;

public class PlayerConfig implements IPlayerConfig
{
	private final BasePlayerConfig basePlayerConfigProxy;

	public PlayerConfig(String name, String color, Byte[] symbol, Byte[] portrait)
	{
		basePlayerConfigProxy = new BasePlayerConfig(name, color, symbol, portrait);
	}

	public PlayerConfig(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		this.basePlayerConfigProxy = new BasePlayerConfig(stmnt);
	}

	public String getName()
	{
		return basePlayerConfigProxy.getName();
	}

	public String getColor()
	{
		return basePlayerConfigProxy.getColor();
	}

	public Byte[] getSymbol()
	{
		return basePlayerConfigProxy.getSymbol();
	}

	public Byte[] getPortrait()
	{
		return basePlayerConfigProxy.getPortrait();
	}

	public static <T extends IPlayerConfig> Set<T> select(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, String from, String where, Object ... params) throws SQLiteDBException
	{
		try
		{
			Set<T> results = new HashSet<T>();

			if (where != null && params != null) where = String.format(where, params);
			SQLiteStatement stmnt = conn.prepare(String.format("SELECT PlayerConfig.* FROM PlayerConfig%s%s ;", (from != null && !from.isEmpty()) ? ", "+from : "", (where != null && !where.isEmpty()) ? " WHERE "+where : ""));
			while(stmnt.step())
			{
				results.add(SQLiteORMGenerator.mapTo(expectedType.isInterface() ? (Class<T>) PlayerConfig.class : expectedType, stmnt, config));
			}
			return results;
		}
		catch(Exception e)
		{
			throw new SQLiteDBException(e);
		}
	}


	public static <T extends IPlayerConfig> void insertOrUpdate(SQLiteConnection conn, T playerConfig) throws SQLiteDBException
	{
		try
		{
			SQLiteStatement stmnt = conn.prepare(String.format("SELECT EXISTS ( SELECT name FROM PlayerConfig WHERE name = %s) AS exist ;", "'"+playerConfig.getName()+"'"));
			stmnt.step();
			if (stmnt.columnInt(0) == 0)
			{
				conn.exec(String.format("INSERT INTO PlayerConfig (name, color, symbol, portrait) VALUES (%s, %s, %s, %s);", "'"+playerConfig.getName()+"'", "'"+playerConfig.getColor()+"'", "'"+playerConfig.getSymbol()+"'", "'"+playerConfig.getPortrait()+"'"));
			}
			else
			{
				conn.exec(String.format("UPDATE PlayerConfig SET  color = %s,  symbol = %s,  portrait = %s WHERE  name = %s ;", "'"+playerConfig.getColor()+"'", "'"+playerConfig.getSymbol()+"'", "'"+playerConfig.getPortrait()+"'", "'"+playerConfig.getName()+"'"));
			}
		}
		catch(Exception e)
		{
			throw new SQLiteDBException(e);
		}
	}
}
