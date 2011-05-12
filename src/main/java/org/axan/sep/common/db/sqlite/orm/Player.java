package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.base.BasePlayer;
import org.axan.sep.common.db.IPlayer;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteStatement;
import java.util.HashSet;
import java.util.Set;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;
import org.axan.sep.common.db.IGameConfig;

public class Player implements IPlayer
{
	private final BasePlayer basePlayerProxy;

	public Player(String name)
	{
		basePlayerProxy = new BasePlayer(name);
	}

	public Player(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		this.basePlayerProxy = new BasePlayer(stmnt);
	}

	public String getName()
	{
		return basePlayerProxy.getName();
	}

	public static <T extends IPlayer> Set<T> select(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, String from, String where, Object ... params) throws SQLiteDBException
	{
		try
		{
			Set<T> results = new HashSet<T>();

			if (where != null && params != null) where = String.format(where, params);
			SQLiteStatement stmnt = conn.prepare(String.format("SELECT Player.* FROM Player%s%s ;", (from != null && !from.isEmpty()) ? ", "+from : "", (where != null && !where.isEmpty()) ? " WHERE "+where : ""));
			while(stmnt.step())
			{
				results.add(SQLiteORMGenerator.mapTo(expectedType, stmnt, config));
			}
			return results;
		}
		catch(Exception e)
		{
			throw new SQLiteDBException(e);
		}
	}


	public static <T extends IPlayer> void insertOrUpdate(SQLiteConnection conn, T player) throws SQLiteDBException
	{
		try
		{
			SQLiteStatement stmnt = conn.prepare(String.format("SELECT EXISTS ( SELECT name FROM Player WHERE name = %s) AS exist ;", "'"+player.getName()+"'"));
			stmnt.step();
			if (stmnt.columnInt(0) == 0)
			{
				conn.exec(String.format("INSERT INTO Player (name) VALUES (%s);", "'"+player.getName()+"'"));
			}
			else
			{
				conn.exec(";");
			}
		}
		catch(Exception e)
		{
			throw new SQLiteDBException(e);
		}
	}
}
