package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.base.BaseSpecialUnit;
import org.axan.sep.common.db.ISpecialUnit;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteStatement;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;
import org.axan.sep.common.Protocol.eSpecialUnitType;
import org.axan.sep.common.db.IGameConfig;
import org.axan.sep.common.db.IHero;
import org.axan.sep.common.db.IVersionedSpecialUnit;

public class SpecialUnit implements ISpecialUnit
{
	private final BaseSpecialUnit baseSpecialUnitProxy;
	private eSpecialUnitType type;

	public SpecialUnit(String owner, String name, eSpecialUnitType type)
	{
		baseSpecialUnitProxy = new BaseSpecialUnit(owner, name, type.toString());
		this.type = type;
	}

	public SpecialUnit(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		this.baseSpecialUnitProxy = new BaseSpecialUnit(stmnt);
		this.type = eSpecialUnitType.valueOf(baseSpecialUnitProxy.getType());
	}

	public String getOwner()
	{
		return baseSpecialUnitProxy.getOwner();
	}

	public String getName()
	{
		return baseSpecialUnitProxy.getName();
	}

	public eSpecialUnitType getType()
	{
		return type;
	}

	/** Set maxVersion to null to select last version. */
	public static <T extends ISpecialUnit> Set<T> selectMaxVersion(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, Integer maxVersion, String from, String where, Object ... params) throws SQLiteDBException
	{
		return select(conn, config, expectedType, true, maxVersion, from, where, params);
	}

	public static <T extends ISpecialUnit> Set<T> selectVersion(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, int version, String from, String where, Object ... params) throws SQLiteDBException
	{
		return select(conn, config, expectedType, false, version, from, where, params);
	}

	public static <T extends ISpecialUnit> Set<T> selectUnversioned(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, String from, String where, Object ... params) throws SQLiteDBException
	{
		return select(conn, config, expectedType, false, null, from, where, params);
	}

	private static <T extends ISpecialUnit> Set<T> select(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, boolean maxVersion, Integer version, String from, String where, Object ... params) throws SQLiteDBException
	{
		try
		{
			Set<T> results = new HashSet<T>();
			SQLiteStatement stmnt = conn.prepare(selectQuery(expectedType, maxVersion, version, from, where, params)+";");
			while(stmnt.step())
			{
				eSpecialUnitType type = eSpecialUnitType.valueOf(stmnt.columnString(0));
				String v = stmnt.columnString(1);
				if (v == null) throw new Error("SpecialUnit with no VersionedSpecialUnit !");
				boolean isVersioned = (!v.isEmpty());
				Class<? extends ISpecialUnit> clazz = (Class<? extends ISpecialUnit>)  Class.forName(String.format("%s.%s%s", SpecialUnit.class.getPackage().getName(), isVersioned ? "Versioned" : "", type.toString()));
				ISpecialUnit o = SQLiteORMGenerator.mapTo(clazz, stmnt, config);
				if (expectedType.isInstance(o))
				{
					results.add(expectedType.cast(o));
				}
			}
			return results;
		}
		catch(Exception e)
		{
			throw new SQLiteDBException(e);
		}
	}

	/** Set maxVersion to null to select last version. */
	public static <T extends ISpecialUnit> boolean existMaxVersion(SQLiteConnection conn, Class<T> expectedType, Integer maxVersion, String from, String where, Object ... params) throws SQLiteDBException
	{
		return exist(conn, expectedType, true, maxVersion, from, where, params);
	}

	public static <T extends ISpecialUnit> boolean existVersion(SQLiteConnection conn,Class<T> expectedType, int version, String from, String where, Object ... params) throws SQLiteDBException
	{
		return exist(conn, expectedType, false, version, from, where, params);
	}

	public static <T extends ISpecialUnit> boolean existUnversioned(SQLiteConnection conn, Class<T> expectedType, String from, String where, Object ... params) throws SQLiteDBException
	{
		return exist(conn, expectedType, false, null, from, where, params);
	}

	private static <T extends ISpecialUnit> boolean exist(SQLiteConnection conn, Class<T> expectedType, boolean maxVersion, Integer version, String from, String where, Object ... params) throws SQLiteDBException
	{
		try
		{
			SQLiteStatement stmnt = conn.prepare("SELECT EXISTS ( "+selectQuery(expectedType, maxVersion, version, from, where, params) + " );");
			return stmnt.step() && stmnt.columnInt(0) != 0;
		}
		catch(Exception e)
		{
			throw new SQLiteDBException(e);
		}
	}


	private static <T extends ISpecialUnit> String selectQuery(Class<T> expectedType, boolean maxVersion, Integer version, String from, String where, Object ... params)
	{
		where = (where == null) ? null : (params == null) ? where : String.format(Locale.UK, where, params);
		if (where != null) where = String.format("(%s)",where);
		String typeFilter = null;
		if (expectedType != null)
		{
			String type = expectedType.isInterface() ? expectedType.getSimpleName().substring(1) : expectedType.getSimpleName();
			typeFilter = String.format("%s.type IS NOT NULL", type);
		}
		if (typeFilter != null && !typeFilter.isEmpty()) where = (where == null) ? typeFilter : String.format("%s AND %s", where, typeFilter);
		String versionFilter;
		if (maxVersion)
		{
			versionFilter = String.format("(VersionedSpecialUnit.turn = ( SELECT MAX(LVVersionedSpecialUnit.turn) FROM VersionedSpecialUnit LVVersionedSpecialUnit WHERE LVVersionedSpecialUnit.owner = SpecialUnit.owner AND LVVersionedSpecialUnit.name = SpecialUnit.name AND LVVersionedSpecialUnit.type = SpecialUnit.type%s ))", (version != null && version >= 0) ? " AND LVVersionedSpecialUnit.turn <= "+version : "");
		}
		else
		{
			versionFilter = (version == null) ? "" : String.format("(VersionedSpecialUnit.turn = %d)", version);
		}
		if (versionFilter != null && !versionFilter.isEmpty()) where = (where == null) ? versionFilter : String.format("%s AND %s", where, versionFilter);
		return String.format("SELECT SpecialUnit.type, VersionedSpecialUnit.type, VersionedSpecialUnit.*, SpecialUnit.*, Hero.* FROM SpecialUnit%s LEFT JOIN VersionedSpecialUnit USING (owner, name, type) LEFT JOIN Hero USING (owner, name, type)%s", (from != null && !from.isEmpty()) ? ", "+from : "", (where != null && !where.isEmpty()) ? " WHERE "+where : "");
	}

	public static <T extends ISpecialUnit> void insertOrUpdate(SQLiteConnection conn, T specialUnit) throws SQLiteDBException
	{
		try
		{
			IVersionedSpecialUnit vspecialUnit = (IVersionedSpecialUnit.class.isInstance(specialUnit) ? IVersionedSpecialUnit.class.cast(specialUnit) : null);
			SQLiteStatement stmnt = conn.prepare(String.format("SELECT EXISTS ( SELECT owner FROM SpecialUnit WHERE owner = %s AND name = %s) AS exist ;", "'"+specialUnit.getOwner()+"'", "'"+specialUnit.getName()+"'"));
			stmnt.step();
			if (stmnt.columnInt(0) == 0)
			{
				conn.exec(String.format("INSERT INTO SpecialUnit (owner, name, type) VALUES (%s, %s, %s);", "'"+specialUnit.getOwner()+"'", "'"+specialUnit.getName()+"'", "'"+specialUnit.getType()+"'").replaceAll("'null'", "NULL"));
				if (vspecialUnit != null)
				{
					conn.exec(String.format("INSERT INTO VersionedSpecialUnit (owner, name, turn, type, fleetOwner, fleetName, fleetTurn) VALUES (%s, %s, %s, %s, %s, %s, %s);", "'"+vspecialUnit.getOwner()+"'", "'"+vspecialUnit.getName()+"'", "'"+vspecialUnit.getTurn()+"'", "'"+vspecialUnit.getType()+"'", "'"+vspecialUnit.getFleetOwner()+"'", "'"+vspecialUnit.getFleetName()+"'", "'"+vspecialUnit.getFleetTurn()+"'").replaceAll("'null'", "NULL"));
				}
				switch(specialUnit.getType())
				{
					case Hero:
					{
						IHero hero = IHero.class.cast(specialUnit);
						conn.exec(String.format("INSERT INTO Hero (owner, name, type, experience) VALUES (%s, %s, %s, %s);", "'"+hero.getOwner()+"'", "'"+hero.getName()+"'", "'"+hero.getType()+"'", "'"+hero.getExperience()+"'").replaceAll("'null'", "NULL"));
						break;
					}
				}
			}
			else
			{
				conn.exec(String.format("UPDATE SpecialUnit SET type = %s WHERE  owner = %s AND name = %s ;", "'"+specialUnit.getType()+"'", "'"+specialUnit.getOwner()+"'", "'"+specialUnit.getName()+"'").replaceAll("'null'", "NULL"));
				if (vspecialUnit != null)
				{
					conn.exec(String.format("UPDATE VersionedSpecialUnit SET type = %s,  fleetOwner = %s,  fleetName = %s,  fleetTurn = %s WHERE  owner = %s AND name = %s AND turn = %s ;", "'"+vspecialUnit.getType()+"'", "'"+vspecialUnit.getFleetOwner()+"'", "'"+vspecialUnit.getFleetName()+"'", "'"+vspecialUnit.getFleetTurn()+"'", "'"+vspecialUnit.getOwner()+"'", "'"+vspecialUnit.getName()+"'", "'"+vspecialUnit.getTurn()+"'").replaceAll("'null'", "NULL"));
				}
				switch(specialUnit.getType())
				{
					case Hero:
					{
						IHero hero = IHero.class.cast(specialUnit);
						conn.exec(String.format("UPDATE Hero SET type = %s,  experience = %s WHERE  owner = %s AND name = %s ;", "'"+hero.getType()+"'", "'"+hero.getExperience()+"'", "'"+hero.getOwner()+"'", "'"+hero.getName()+"'").replaceAll("'null'", "NULL"));
						break;
					}
				}
			}
		}
		catch(Exception e)
		{
			throw new SQLiteDBException(e);
		}
	}
}
