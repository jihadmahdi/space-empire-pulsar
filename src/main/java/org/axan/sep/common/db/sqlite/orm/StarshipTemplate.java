package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.base.BaseStarshipTemplate;
import org.axan.sep.common.IGameConfig;
import com.almworks.sqlite4java.SQLiteStatement;
import java.util.Set;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import com.almworks.sqlite4java.SQLiteConnection;
import java.util.HashSet;
import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;

public class StarshipTemplate implements IStarshipTemplate
{
	private final BaseStarshipTemplate baseStarshipTemplateProxy;

	public StarshipTemplate(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		this.baseStarshipTemplateProxy = new BaseStarshipTemplate(stmnt);
	}

	public String getSpecializedClass()
	{
		return baseStarshipTemplateProxy.getSpecializedClass();
	}

	public String getName()
	{
		return baseStarshipTemplateProxy.getName();
	}

	public static <T extends IStarshipTemplate> Set<T> select(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, boolean lastVersion, String where, Object ... params) throws SQLiteDBException
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

}
