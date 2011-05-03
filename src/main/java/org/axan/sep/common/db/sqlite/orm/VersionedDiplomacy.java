package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.base.BaseVersionedDiplomacy;
import org.axan.sep.common.IGameConfig;
import com.almworks.sqlite4java.SQLiteStatement;
import java.util.Set;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import com.almworks.sqlite4java.SQLiteConnection;
import java.util.HashSet;
import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;

public class VersionedDiplomacy implements IVersionedDiplomacy
{
	private final BaseVersionedDiplomacy baseVersionedDiplomacyProxy;

	public VersionedDiplomacy(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		this.baseVersionedDiplomacyProxy = new BaseVersionedDiplomacy(stmnt);
	}

	public Boolean getAllowToLand()
	{
		return baseVersionedDiplomacyProxy.getAllowToLand();
	}

	public Integer getTurn()
	{
		return baseVersionedDiplomacyProxy.getTurn();
	}

	public String getForeignPolicy()
	{
		return baseVersionedDiplomacyProxy.getForeignPolicy();
	}

	public String getCible()
	{
		return baseVersionedDiplomacyProxy.getCible();
	}

	public String getName()
	{
		return baseVersionedDiplomacyProxy.getName();
	}

	public static <T extends IVersionedDiplomacy> Set<T> select(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, boolean lastVersion, String where, Object ... params) throws SQLiteDBException
	{
		try
		{
			Set<T> results = new HashSet<T>();

			if (where != null && params != null) where = String.format(where, params);
			if (lastVersion)
			{
				where = String.format("%s(VersionedDiplomacy.turn = ( SELECT MAX(LVVersionedDiplomacy.turn) FROM VersionedDiplomacy LVVersionedDiplomacy WHERE LVVersionedDiplomacy.allowToLand = VersionedDiplomacy.allowToLand AND LVVersionedDiplomacy.turn = VersionedDiplomacy.turn AND LVVersionedDiplomacy.foreignPolicy = VersionedDiplomacy.foreignPolicy AND LVVersionedDiplomacy.cible = VersionedDiplomacy.cible AND LVVersionedDiplomacy.name = VersionedDiplomacy.name ))", (where != null && !where.isEmpty()) ? "("+where+") AND " : "");
			}
			SQLiteStatement stmnt = conn.prepare(String.format("SELECT * FROM VersionedDiplomacy%s ;", (where != null && !where.isEmpty()) ? " WHERE "+where : ""));
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
