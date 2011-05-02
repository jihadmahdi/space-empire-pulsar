package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.base.BasePlayerConfig;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import org.axan.sep.common.IGameConfig;
import java.util.Set;
import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;
import java.util.HashSet;
import com.almworks.sqlite4java.SQLiteStatement;
import com.almworks.sqlite4java.SQLiteConnection;

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

	public static <T extends IPlayerConfig> Set<T> select(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, boolean lastVersion, String where, Object ... params) throws SQLiteDBException
	{
		try
		{
			Set<T> results = new HashSet<T>();

			if (where != null && params != null) where = String.format(where, params);
			SQLiteStatement stmnt = conn.prepare(String.format("SELECT * FROM PlayerConfig%s ;", (where != null && !where.isEmpty()) ? " WHERE "+where : ""));
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

}
