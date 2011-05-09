package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.base.BaseVersionedDiplomacy;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteStatement;
import java.util.HashSet;
import java.util.Set;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;
import org.axan.sep.common.IGameConfig;

public class VersionedDiplomacy implements IVersionedDiplomacy
{
	private final BaseVersionedDiplomacy baseVersionedDiplomacyProxy;

	public VersionedDiplomacy(String name, String cible, Integer turn, Boolean allowToLand, String foreignPolicy)
	{
		baseVersionedDiplomacyProxy = new BaseVersionedDiplomacy(name, cible, turn, allowToLand, foreignPolicy);
	}

	public VersionedDiplomacy(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		this.baseVersionedDiplomacyProxy = new BaseVersionedDiplomacy(stmnt);
	}

	public String getName()
	{
		return baseVersionedDiplomacyProxy.getName();
	}

	public String getCible()
	{
		return baseVersionedDiplomacyProxy.getCible();
	}

	public Integer getTurn()
	{
		return baseVersionedDiplomacyProxy.getTurn();
	}

	public Boolean getAllowToLand()
	{
		return baseVersionedDiplomacyProxy.getAllowToLand();
	}

	public String getForeignPolicy()
	{
		return baseVersionedDiplomacyProxy.getForeignPolicy();
	}

	public static <T extends IVersionedDiplomacy> Set<T> select(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, boolean lastVersion, String from, String where, Object ... params) throws SQLiteDBException
	{
		try
		{
			Set<T> results = new HashSet<T>();

			if (where != null && params != null) where = String.format(where, params);
			if (lastVersion)
			{
				where = String.format("%s(VersionedDiplomacy.turn = ( SELECT MAX(LVVersionedDiplomacy.turn) FROM VersionedDiplomacy LVVersionedDiplomacy WHERE LVVersionedDiplomacy.name = VersionedDiplomacy.name AND LVVersionedDiplomacy.cible = VersionedDiplomacy.cible AND LVVersionedDiplomacy.turn = VersionedDiplomacy.turn AND LVVersionedDiplomacy.allowToLand = VersionedDiplomacy.allowToLand AND LVVersionedDiplomacy.foreignPolicy = VersionedDiplomacy.foreignPolicy ))", (where != null && !where.isEmpty()) ? "("+where+") AND " : "");
			}
			SQLiteStatement stmnt = conn.prepare(String.format("SELECT VersionedDiplomacy.* FROM VersionedDiplomacy%s%s ;", (from != null && !from.isEmpty()) ? ", "+from : "", (where != null && !where.isEmpty()) ? " WHERE "+where : ""));
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


	public static <T extends IVersionedDiplomacy> void insertOrUpdate(SQLiteConnection conn, T versionedDiplomacy) throws SQLiteDBException
	{
		try
		{
			IVersionedDiplomacy vversionedDiplomacy = (IVersionedDiplomacy.class.isInstance(versionedDiplomacy) ? IVersionedDiplomacy.class.cast(versionedDiplomacy) : null);
			SQLiteStatement stmnt = conn.prepare(String.format("SELECT EXISTS ( SELECT name FROM VersionedDiplomacy WHERE name = %s AND cible = %s AND turn = %s) AS exist ;", "'"+versionedDiplomacy.getName()+"'", "'"+versionedDiplomacy.getCible()+"'", "'"+versionedDiplomacy.getTurn()+"'"));
			stmnt.step();
			if (stmnt.columnInt(0) == 0)
			{
				conn.exec(String.format("INSERT INTO VersionedDiplomacy (name, cible, turn, allowToLand, foreignPolicy) VALUES (%s, %s, %s, %s, %s);", "'"+versionedDiplomacy.getName()+"'", "'"+versionedDiplomacy.getCible()+"'", "'"+versionedDiplomacy.getTurn()+"'", "'"+versionedDiplomacy.getAllowToLand()+"'", "'"+versionedDiplomacy.getForeignPolicy()+"'"));
			}
			else
			{
				conn.exec(String.format("UPDATE VersionedDiplomacy SET  allowToLand = %s,  foreignPolicy = %s WHERE  name = %s AND cible = %s AND turn = %s ;", "'"+versionedDiplomacy.getAllowToLand()+"'", "'"+versionedDiplomacy.getForeignPolicy()+"'", "'"+versionedDiplomacy.getName()+"'", "'"+versionedDiplomacy.getCible()+"'", "'"+versionedDiplomacy.getTurn()+"'"));
			}
		}
		catch(Exception e)
		{
			throw new SQLiteDBException(e);
		}
	}
}
