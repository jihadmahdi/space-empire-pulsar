package org.axan.sep.common.db.orm;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.axan.eplib.orm.DataBaseORMGenerator;
import org.axan.eplib.orm.ISQLDataBaseStatement;
import org.axan.eplib.orm.ISQLDataBaseStatementJob;
import org.axan.eplib.orm.SQLDataBaseException;
import org.axan.sep.common.db.IPlayerConfig;
import org.axan.sep.common.db.SEPCommonDB;
import org.axan.sep.common.db.orm.base.BasePlayerConfig;
import org.axan.sep.common.db.orm.base.IBasePlayerConfig;

public class PlayerConfig implements IPlayerConfig, Serializable
{
	private final IBasePlayerConfig basePlayerConfigProxy;

	PlayerConfig(IBasePlayerConfig basePlayerConfigProxy)
	{
		this.basePlayerConfigProxy = basePlayerConfigProxy;
	}

	public PlayerConfig(String name, String color, String symbol, String portrait)
	{
		this(new BasePlayerConfig(name, color, symbol, portrait));
	}

	public PlayerConfig(ISQLDataBaseStatement stmnt) throws Exception
	{
		this(new BasePlayerConfig(stmnt));
	}

	@Override
	public String getName()
	{
		return basePlayerConfigProxy.getName();
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

	public static <T extends IPlayerConfig> T selectOne(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
	{
		Set<T> results = select(db, expectedType, from, (where==null?"":where+" ")+"LIMIT 1", params);
		if (results.isEmpty()) return null;
		return results.iterator().next();
	}

	public static <T extends IPlayerConfig> Set<T> select(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
	{
		ISQLDataBaseStatement stmnt = null;
		try
		{
			Set<T> results = new HashSet<T>();
			stmnt = db.getDB().prepare(selectQuery(expectedType, from, where, params)+";", new ISQLDataBaseStatementJob<ISQLDataBaseStatement>()
			{
				@Override
				public ISQLDataBaseStatement job(ISQLDataBaseStatement stmnt) throws SQLDataBaseException
				{
					return stmnt;
				}
			});
			while(stmnt.step())
			{
				results.add(DataBaseORMGenerator.mapTo(expectedType.isInterface() ? (Class<T>) PlayerConfig.class : expectedType, stmnt));
			}
			return results;
		}
		catch(Exception e)
		{
			throw new SQLDataBaseException(e);
		}
		finally
		{
			if (stmnt != null) stmnt.dispose();
		}
	}

	public static <T extends IPlayerConfig> boolean exist(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
	{
		try
		{
			return db.getDB().prepare(selectQuery(expectedType, from, where, params)+" ;", new ISQLDataBaseStatementJob<Boolean>()
			{
				@Override
				public Boolean job(ISQLDataBaseStatement stmnt) throws SQLDataBaseException
				{
					try
					{
						return stmnt.step() && stmnt.columnValue(0) != null;
					}
					finally
					{
						if (stmnt != null) stmnt.dispose();
					}
				}
			});
		}
		catch(Exception e)
		{
			throw new SQLDataBaseException(e);
		}
	}


	private static <T extends IPlayerConfig> String selectQuery(Class<T> expectedType, String from, String where, Object ... params)
	{
		where = (where == null) ? null : (params == null) ? where : String.format(Locale.UK, where, params);
		if (where != null) where = String.format("(%s)",where);
		return String.format("SELECT PlayerConfig.* FROM PlayerConfig%s%s", (from != null && !from.isEmpty()) ? ", "+from : "", (where != null && !where.isEmpty()) ? " WHERE "+where : "");
	}

	public static <T extends IPlayerConfig> void insertOrUpdate(SEPCommonDB db, T playerConfig) throws SQLDataBaseException
	{
		try
		{
			boolean exist = exist(db, playerConfig.getClass(), null, " PlayerConfig.name = %s", "'"+playerConfig.getName()+"'");
			if (exist)
			{
				db.getDB().exec(String.format("UPDATE PlayerConfig SET color = %s,  symbol = %s,  portrait = %s WHERE  PlayerConfig.name = %s ;", "'"+playerConfig.getColor()+"'", "'"+playerConfig.getSymbol()+"'", "'"+playerConfig.getPortrait()+"'", "'"+playerConfig.getName()+"'").replaceAll("'null'", "NULL"));
			}
			else
			{
				if (!exist) db.getDB().exec(String.format("INSERT INTO PlayerConfig (name, color, symbol, portrait) VALUES (%s, %s, %s, %s);", "'"+playerConfig.getName()+"'", "'"+playerConfig.getColor()+"'", "'"+playerConfig.getSymbol()+"'", "'"+playerConfig.getPortrait()+"'").replaceAll("'null'", "NULL"));
			}
		}
		catch(Exception e)
		{
			throw new SQLDataBaseException(e);
		}
	}
}
