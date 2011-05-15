package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.base.BaseStarshipTemplate;
import org.axan.sep.common.db.IStarshipTemplate;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteStatement;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;
import org.axan.sep.common.db.IGameConfig;

public class StarshipTemplate implements IStarshipTemplate
{
	private final BaseStarshipTemplate baseStarshipTemplateProxy;

	public StarshipTemplate(String name, String specializedClass)
	{
		baseStarshipTemplateProxy = new BaseStarshipTemplate(name, specializedClass);
	}

	public StarshipTemplate(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		this.baseStarshipTemplateProxy = new BaseStarshipTemplate(stmnt);
	}

	public String getName()
	{
		return baseStarshipTemplateProxy.getName();
	}

	public String getSpecializedClass()
	{
		return baseStarshipTemplateProxy.getSpecializedClass();
	}

	public static <T extends IStarshipTemplate> Set<T> select(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, String from, String where, Object ... params) throws SQLiteDBException
	{
		try
		{
			Set<T> results = new HashSet<T>();
			SQLiteStatement stmnt = conn.prepare(selectQuery(expectedType, from, where, params)+";");
			while(stmnt.step())
			{
				results.add(SQLiteORMGenerator.mapTo(expectedType.isInterface() ? (Class<T>) StarshipTemplate.class : expectedType, stmnt, config));
			}
			return results;
		}
		catch(Exception e)
		{
			throw new SQLiteDBException(e);
		}
	}

	public static <T extends IStarshipTemplate> boolean exist(SQLiteConnection conn, Class<T> expectedType, String from, String where, Object ... params) throws SQLiteDBException
	{
		try
		{
			SQLiteStatement stmnt = conn.prepare("SELECT EXISTS ( "+selectQuery(expectedType, from, where, params) + " );");
			return stmnt.step() && stmnt.columnInt(0) != 0;
		}
		catch(Exception e)
		{
			throw new SQLiteDBException(e);
		}
	}


	private static <T extends IStarshipTemplate> String selectQuery(Class<T> expectedType, String from, String where, Object ... params)
	{
		where = (where == null) ? null : (params == null) ? where : String.format(Locale.UK, where, params);
		if (where != null) where = String.format("(%s)",where);
		return String.format("SELECT StarshipTemplate.* FROM StarshipTemplate%s%s", (from != null && !from.isEmpty()) ? ", "+from : "", (where != null && !where.isEmpty()) ? " WHERE "+where : "");
	}

	public static <T extends IStarshipTemplate> void insertOrUpdate(SQLiteConnection conn, T starshipTemplate) throws SQLiteDBException
	{
		try
		{
			SQLiteStatement stmnt = conn.prepare(String.format("SELECT EXISTS ( SELECT name FROM StarshipTemplate WHERE name = %s) AS exist ;", "'"+starshipTemplate.getName()+"'"));
			stmnt.step();
			if (stmnt.columnInt(0) == 0)
			{
				conn.exec(String.format("INSERT INTO StarshipTemplate (name, specializedClass) VALUES (%s, %s);", "'"+starshipTemplate.getName()+"'", "'"+starshipTemplate.getSpecializedClass()+"'").replaceAll("'null'", "NULL"));
			}
			else
			{
				conn.exec(String.format("UPDATE StarshipTemplate SET specializedClass = %s WHERE  name = %s ;", "'"+starshipTemplate.getSpecializedClass()+"'", "'"+starshipTemplate.getName()+"'").replaceAll("'null'", "NULL"));
			}
		}
		catch(Exception e)
		{
			throw new SQLiteDBException(e);
		}
	}
}
