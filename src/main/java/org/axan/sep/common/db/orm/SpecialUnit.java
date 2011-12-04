package org.axan.sep.common.db.orm;

import java.lang.Exception;
import org.axan.sep.common.db.orm.base.IBaseSpecialUnit;
import org.axan.sep.common.db.orm.base.BaseSpecialUnit;
import org.axan.sep.common.db.ISpecialUnit;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.axan.eplib.orm.DataBaseORMGenerator;
import org.axan.eplib.orm.ISQLDataBaseStatement;
import org.axan.eplib.orm.ISQLDataBaseStatementJob;
import org.axan.eplib.orm.SQLDataBaseException;
import org.axan.eplib.orm.sqlite.SQLiteDB;
import org.axan.sep.common.Protocol.eSpecialUnitType;
import org.axan.sep.common.db.IGameConfig;
import org.axan.sep.common.db.IHero;
import org.axan.sep.common.db.IVersionedSpecialUnit;
import org.axan.sep.common.db.SEPCommonDB;

public class SpecialUnit implements ISpecialUnit
{
	private final IBaseSpecialUnit baseSpecialUnitProxy;
	private final eSpecialUnitType type;

	SpecialUnit(IBaseSpecialUnit baseSpecialUnitProxy)
	{
		this.baseSpecialUnitProxy = baseSpecialUnitProxy;
		this.type = eSpecialUnitType.valueOf(baseSpecialUnitProxy.getType());
	}

	public SpecialUnit(String owner, String name, eSpecialUnitType type)
	{
		this(new BaseSpecialUnit(owner, name, type.toString()));
	}

	public SpecialUnit(ISQLDataBaseStatement stmnt) throws Exception
	{
		this(new BaseSpecialUnit(stmnt));
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
	public static <T extends ISpecialUnit> Set<T> selectMaxVersion(SEPCommonDB db, Class<T> expectedType, Integer maxVersion, String from, String where, Object ... params) throws SQLDataBaseException
	{
		return select(db, expectedType, true, maxVersion, from, where, params);
	}

	public static <T extends ISpecialUnit> Set<T> selectVersion(SEPCommonDB db, Class<T> expectedType, int version, String from, String where, Object ... params) throws SQLDataBaseException
	{
		return select(db, expectedType, false, version, from, where, params);
	}

	public static <T extends ISpecialUnit> Set<T> selectUnversioned(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
	{
		return select(db, expectedType, false, null, from, where, params);
	}

	private static <T extends ISpecialUnit> Set<T> select(SEPCommonDB db, Class<T> expectedType, boolean maxVersion, Integer version, String from, String where, Object ... params) throws SQLDataBaseException
	{
		ISQLDataBaseStatement stmnt = null;
		try
		{
			Set<T> results = new HashSet<T>();
			stmnt = db.getDB().prepare(selectQuery(expectedType, maxVersion, version, from, where, params)+";", new ISQLDataBaseStatementJob<ISQLDataBaseStatement>()
			{
				@Override
				public ISQLDataBaseStatement job(ISQLDataBaseStatement stmnt) throws SQLDataBaseException
				{
					return stmnt;
				}
			});
			while(stmnt.step())
			{
				eSpecialUnitType type = eSpecialUnitType.valueOf(stmnt.columnString(0));
				String v = stmnt.columnString(1);
				if (v == null) throw new Error("SpecialUnit with no VersionedSpecialUnit !");
				boolean isVersioned = (!v.isEmpty());
				Class<? extends ISpecialUnit> clazz = (Class<? extends ISpecialUnit>)  Class.forName(String.format("%s.%s%s", SpecialUnit.class.getPackage().getName(), isVersioned ? "Versioned" : "", type.toString()));
				ISpecialUnit o = DataBaseORMGenerator.mapTo(clazz, stmnt);
				if (expectedType.isInstance(o))
				{
					results.add(expectedType.cast(o));
				}
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

	/** Set maxVersion to null to select last version. */
	public static <T extends ISpecialUnit> boolean existMaxVersion(SEPCommonDB db, Class<T> expectedType, Integer maxVersion, String from, String where, Object ... params) throws SQLDataBaseException
	{
		return exist(db, expectedType, true, maxVersion, from, where, params);
	}

	public static <T extends ISpecialUnit> boolean existVersion(SEPCommonDB db,Class<T> expectedType, int version, String from, String where, Object ... params) throws SQLDataBaseException
	{
		return exist(db, expectedType, false, version, from, where, params);
	}

	public static <T extends ISpecialUnit> boolean existUnversioned(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
	{
		return exist(db, expectedType, false, null, from, where, params);
	}

	private static <T extends ISpecialUnit> boolean exist(SEPCommonDB db, Class<T> expectedType, boolean maxVersion, Integer version, String from, String where, Object ... params) throws SQLDataBaseException
	{
		try
		{
			return db.getDB().prepare(selectQuery(expectedType, maxVersion, version, from, where, params)+" ;", new ISQLDataBaseStatementJob<Boolean>()
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

	public static <T extends ISpecialUnit> void insertOrUpdate(SEPCommonDB db, T specialUnit) throws SQLDataBaseException
	{
		try
		{
			boolean exist = existUnversioned(db, specialUnit.getClass(), null, " SpecialUnit.owner = %s AND SpecialUnit.name = %s", "'"+specialUnit.getOwner()+"'", "'"+specialUnit.getName()+"'");
			IVersionedSpecialUnit vspecialUnit = (IVersionedSpecialUnit.class.isInstance(specialUnit) ? IVersionedSpecialUnit.class.cast(specialUnit) : null);
			boolean vexist = existVersion(db, vspecialUnit.getClass(), vspecialUnit.getTurn(), null, " VersionedSpecialUnit.owner = %s AND VersionedSpecialUnit.name = %s AND VersionedSpecialUnit.turn = %s", "'"+vspecialUnit.getOwner()+"'", "'"+vspecialUnit.getName()+"'", "'"+vspecialUnit.getTurn()+"'");
			if (vexist && !exist) throw new Error("Versioned SpecialUnit cannot exist without unversioned entry.");
			if (exist)
			{
				db.getDB().exec(String.format("UPDATE SpecialUnit SET type = %s WHERE  SpecialUnit.owner = %s AND SpecialUnit.name = %s ;", "'"+specialUnit.getType()+"'", "'"+specialUnit.getOwner()+"'", "'"+specialUnit.getName()+"'").replaceAll("'null'", "NULL"));
				if (vexist && vspecialUnit != null)
				{
					db.getDB().exec(String.format("UPDATE VersionedSpecialUnit SET type = %s,  fleetOwner = %s,  fleetName = %s,  fleetTurn = %s WHERE  VersionedSpecialUnit.owner = %s AND VersionedSpecialUnit.name = %s AND VersionedSpecialUnit.turn = %s ;", "'"+vspecialUnit.getType()+"'", "'"+vspecialUnit.getFleetOwner()+"'", "'"+vspecialUnit.getFleetName()+"'", "'"+vspecialUnit.getFleetTurn()+"'", "'"+vspecialUnit.getOwner()+"'", "'"+vspecialUnit.getName()+"'", "'"+vspecialUnit.getTurn()+"'").replaceAll("'null'", "NULL"));
				}
				switch(specialUnit.getType())
				{
					case Hero:
					{
						IHero hero = IHero.class.cast(specialUnit);
						db.getDB().exec(String.format("UPDATE Hero SET type = %s,  experience = %s WHERE  Hero.owner = %s AND Hero.name = %s ;", "'"+hero.getType()+"'", "'"+hero.getExperience()+"'", "'"+hero.getOwner()+"'", "'"+hero.getName()+"'").replaceAll("'null'", "NULL"));
						break;
					}
				}
			}
			if (!exist || !vexist)
			{
				if (!exist) db.getDB().exec(String.format("INSERT INTO SpecialUnit (owner, name, type) VALUES (%s, %s, %s);", "'"+specialUnit.getOwner()+"'", "'"+specialUnit.getName()+"'", "'"+specialUnit.getType()+"'").replaceAll("'null'", "NULL"));
				if (vspecialUnit != null)
				{
					db.getDB().exec(String.format("INSERT INTO VersionedSpecialUnit (owner, name, turn, type, fleetOwner, fleetName, fleetTurn) VALUES (%s, %s, %s, %s, %s, %s, %s);", "'"+vspecialUnit.getOwner()+"'", "'"+vspecialUnit.getName()+"'", "'"+vspecialUnit.getTurn()+"'", "'"+vspecialUnit.getType()+"'", "'"+vspecialUnit.getFleetOwner()+"'", "'"+vspecialUnit.getFleetName()+"'", "'"+vspecialUnit.getFleetTurn()+"'").replaceAll("'null'", "NULL"));
				}
				switch(specialUnit.getType())
				{
					case Hero:
					{
						IHero hero = IHero.class.cast(specialUnit);
						if (!exist) db.getDB().exec(String.format("INSERT INTO Hero (owner, name, type, experience) VALUES (%s, %s, %s, %s);", "'"+hero.getOwner()+"'", "'"+hero.getName()+"'", "'"+hero.getType()+"'", "'"+hero.getExperience()+"'").replaceAll("'null'", "NULL"));
						break;
					}
				}
			}
		}
		catch(Exception e)
		{
			throw new SQLDataBaseException(e);
		}
	}
}
