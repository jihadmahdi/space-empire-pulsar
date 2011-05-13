package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.base.BaseDiplomacy;
import org.axan.sep.common.db.IDiplomacy;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteStatement;
import java.util.HashSet;
import java.util.Set;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;
import org.axan.sep.common.db.IGameConfig;

public class Diplomacy implements IDiplomacy
{
	private final BaseDiplomacy baseDiplomacyProxy;

	public Diplomacy(String owner, String target, Integer turn, Boolean allowToLand, String foreignPolicy)
	{
		baseDiplomacyProxy = new BaseDiplomacy(owner, target, turn, allowToLand, foreignPolicy);
	}

	public Diplomacy(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		this.baseDiplomacyProxy = new BaseDiplomacy(stmnt);
	}

	public String getOwner()
	{
		return baseDiplomacyProxy.getOwner();
	}

	public String getTarget()
	{
		return baseDiplomacyProxy.getTarget();
	}

	public Integer getTurn()
	{
		return baseDiplomacyProxy.getTurn();
	}

	public Boolean getAllowToLand()
	{
		return baseDiplomacyProxy.getAllowToLand();
	}

	public String getForeignPolicy()
	{
		return baseDiplomacyProxy.getForeignPolicy();
	}

	/** Set maxVersion to null to select last version. */
	public static <T extends IDiplomacy> Set<T> selectMaxVersion(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, Integer maxVersion, String from, String where, Object ... params) throws SQLiteDBException
	{
		return select(conn, config, expectedType, true, maxVersion, from, where, params);
	}

	public static <T extends IDiplomacy> Set<T> selectVersion(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, int version, String from, String where, Object ... params) throws SQLiteDBException
	{
		return select(conn, config, expectedType, false, version, from, where, params);
	}

	public static <T extends IDiplomacy> Set<T> selectUnversioned(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, String from, String where, Object ... params) throws SQLiteDBException
	{
		return select(conn, config, expectedType, false, null, from, where, params);
	}

	private static <T extends IDiplomacy> Set<T> select(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, boolean maxVersion, Integer version, String from, String where, Object ... params) throws SQLiteDBException
	{
		try
		{
			Set<T> results = new HashSet<T>();

			if (where != null && params != null) where = String.format(where, params);
			String versionFilter;
			if (maxVersion)
			{
				versionFilter = String.format("(Diplomacy.turn = ( SELECT MAX(LVDiplomacy.turn) FROM Diplomacy LVDiplomacy WHERE LVDiplomacy.owner = Diplomacy.owner AND LVDiplomacy.target = Diplomacy.target AND LVDiplomacy.turn = Diplomacy.turn AND LVDiplomacy.allowToLand = Diplomacy.allowToLand AND LVDiplomacy.foreignPolicy = Diplomacy.foreignPolicy%s ))", (version != null && version >= 0) ? " AND LVDiplomacy.turn <= "+version : "");
			}
			else
			{
				versionFilter = (version == null) ? "" : String.format("(Diplomacy.turn = %d)", version);
			}
			where = String.format("%s%s", (where != null && !where.isEmpty()) ? "("+where+") AND " : "", versionFilter);
			SQLiteStatement stmnt = conn.prepare(String.format("SELECT Diplomacy.* FROM Diplomacy%s%s ;", (from != null && !from.isEmpty()) ? ", "+from : "", (where != null && !where.isEmpty()) ? " WHERE "+where : ""));
			while(stmnt.step())
			{
				results.add(SQLiteORMGenerator.mapTo(expectedType.isInterface() ? (Class<T>) Diplomacy.class : expectedType, stmnt, config));
			}
			return results;
		}
		catch(Exception e)
		{
			throw new SQLiteDBException(e);
		}
	}


	public static <T extends IDiplomacy> void insertOrUpdate(SQLiteConnection conn, T diplomacy) throws SQLiteDBException
	{
		try
		{
			SQLiteStatement stmnt = conn.prepare(String.format("SELECT EXISTS ( SELECT owner FROM Diplomacy WHERE owner = %s AND target = %s AND turn = %s) AS exist ;", "'"+diplomacy.getOwner()+"'", "'"+diplomacy.getTarget()+"'", "'"+diplomacy.getTurn()+"'"));
			stmnt.step();
			if (stmnt.columnInt(0) == 0)
			{
				conn.exec(String.format("INSERT INTO Diplomacy (owner, target, turn, allowToLand, foreignPolicy) VALUES (%s, %s, %s, %s, %s);", "'"+diplomacy.getOwner()+"'", "'"+diplomacy.getTarget()+"'", "'"+diplomacy.getTurn()+"'", "'"+diplomacy.getAllowToLand()+"'", "'"+diplomacy.getForeignPolicy()+"'"));
			}
			else
			{
				conn.exec(String.format("UPDATE Diplomacy SET  allowToLand = %s,  foreignPolicy = %s WHERE  owner = %s AND target = %s AND turn = %s ;", "'"+diplomacy.getAllowToLand()+"'", "'"+diplomacy.getForeignPolicy()+"'", "'"+diplomacy.getOwner()+"'", "'"+diplomacy.getTarget()+"'", "'"+diplomacy.getTurn()+"'"));
			}
		}
		catch(Exception e)
		{
			throw new SQLiteDBException(e);
		}
	}
}
