package org.axan.sep.common.db.orm;

import java.io.Serializable;
import java.lang.Exception;
import org.axan.sep.common.db.orm.base.IBasePlayer;
import org.axan.sep.common.db.orm.base.BasePlayer;
import org.axan.sep.common.db.IPlayer;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.axan.eplib.orm.DataBaseORMGenerator;
import org.axan.eplib.orm.ISQLDataBaseStatement;
import org.axan.eplib.orm.ISQLDataBaseStatementJob;
import org.axan.eplib.orm.SQLDataBaseException;
import org.axan.eplib.orm.sqlite.SQLiteDB;
import org.axan.sep.common.db.IGameConfig;
import org.axan.sep.common.db.SEPCommonDB;

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

	public Player(ISQLDataBaseStatement stmnt) throws Exception
	{
		this(new BasePlayer(stmnt));
	}

	@Override
	public int compareTo(IPlayer o)
	{
		return getName().compareTo(o.getName());
	}
	
	public String getName()
	{
		return basePlayerProxy.getName();
	}

	public static <T extends IPlayer> Set<T> select(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
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
				results.add(DataBaseORMGenerator.mapTo(expectedType.isInterface() ? (Class<T>) Player.class : expectedType, stmnt));
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

	public static <T extends IPlayer> boolean exist(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
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


	private static <T extends IPlayer> String selectQuery(Class<T> expectedType, String from, String where, Object ... params)
	{
		where = (where == null) ? null : (params == null) ? where : String.format(Locale.UK, where, params);
		if (where != null) where = String.format("(%s)",where);
		return String.format("SELECT Player.* FROM Player%s%s", (from != null && !from.isEmpty()) ? ", "+from : "", (where != null && !where.isEmpty()) ? " WHERE "+where : "");
	}

	public static <T extends IPlayer> void insertOrUpdate(SEPCommonDB db, T player) throws SQLDataBaseException
	{
		try
		{
			boolean exist = exist(db, player.getClass(), null, " Player.name = %s", "'"+player.getName()+"'");
			if (exist)
			{
			}
			else
			{
				if (!exist) db.getDB().exec(String.format("INSERT INTO Player (name) VALUES (%s);", "'"+player.getName()+"'").replaceAll("'null'", "NULL"));
			}
		}
		catch(Exception e)
		{
			throw new SQLDataBaseException(e);
		}
	}
}
