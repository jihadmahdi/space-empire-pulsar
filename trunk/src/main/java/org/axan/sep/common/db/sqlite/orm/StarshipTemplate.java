package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.base.BaseStarshipTemplate;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteStatement;
import java.util.HashSet;
import java.util.Set;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;
import org.axan.sep.common.IGameConfig;

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

	public static <T extends IStarshipTemplate> Set<T> select(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, String where, Object ... params) throws SQLiteDBException
	{
		try
		{
			Set<T> results = new HashSet<T>();

			if (where != null && params != null) where = String.format(where, params);
			SQLiteStatement stmnt = conn.prepare(String.format("SELECT * FROM StarshipTemplate%s ;", (where != null && !where.isEmpty()) ? " WHERE "+where : ""));
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


	public static <T extends IStarshipTemplate> void insertOrUpdate(SQLiteConnection conn, T starshipTemplate) throws SQLiteDBException
	{
		try
		{
			SQLiteStatement stmnt = conn.prepare(String.format("SELECT EXISTS ( SELECT name FROM StarshipTemplate WHERE name = %s) AS exist ;", "'"+starshipTemplate.getName()+"'"));
			stmnt.step();
			if (stmnt.columnInt(0) == 0)
			{
				conn.exec(String.format("INSERT INTO StarshipTemplate (name, specializedClass) VALUES (%s, %s);", "'"+starshipTemplate.getName()+"'", "'"+starshipTemplate.getSpecializedClass()+"'"));
			}
			else
			{
				conn.exec(String.format("UPDATE StarshipTemplate SET  specializedClass = %s WHERE  name = %s ;", "'"+starshipTemplate.getSpecializedClass()+"'", "'"+starshipTemplate.getName()+"'"));
			}
		}
		catch(Exception e)
		{
			throw new SQLiteDBException(e);
		}
	}
}
