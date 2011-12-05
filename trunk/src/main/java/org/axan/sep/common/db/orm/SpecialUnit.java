package org.axan.sep.common.db.orm;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.axan.eplib.orm.DataBaseORMGenerator;
import org.axan.eplib.orm.ISQLDataBaseStatement;
import org.axan.eplib.orm.ISQLDataBaseStatementJob;
import org.axan.eplib.orm.SQLDataBaseException;
import org.axan.sep.common.Protocol.eSpecialUnitType;
import org.axan.sep.common.db.IHero;
import org.axan.sep.common.db.ISpecialUnit;
import org.axan.sep.common.db.SEPCommonDB;
import org.axan.sep.common.db.orm.base.BaseSpecialUnit;
import org.axan.sep.common.db.orm.base.IBaseSpecialUnit;

public class SpecialUnit implements ISpecialUnit
{
	private final IBaseSpecialUnit baseSpecialUnitProxy;
	private final eSpecialUnitType type;

	SpecialUnit(IBaseSpecialUnit baseSpecialUnitProxy)
	{
		this.baseSpecialUnitProxy = baseSpecialUnitProxy;
		this.type = eSpecialUnitType.valueOf(baseSpecialUnitProxy.getType());
	}

	public SpecialUnit(String owner, String name, eSpecialUnitType type, String fleetName)
	{
		this(new BaseSpecialUnit(owner, name, type.toString(), fleetName));
	}

	public SpecialUnit(ISQLDataBaseStatement stmnt) throws Exception
	{
		this(new BaseSpecialUnit(stmnt));
	}

	@Override
	public String getOwner()
	{
		return baseSpecialUnitProxy.getOwner();
	}

	@Override
	public String getName()
	{
		return baseSpecialUnitProxy.getName();
	}

	@Override
	public eSpecialUnitType getType()
	{
		return type;
	}

	@Override
	public String getFleetName()
	{
		return baseSpecialUnitProxy.getFleetName();
	}

	public static <T extends ISpecialUnit> T selectOne(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
	{
		Set<T> results = select(db, expectedType, from, (where==null?"":where+" ")+"LIMIT 1", params);
		if (results.isEmpty()) return null;
		return results.iterator().next();
	}

	public static <T extends ISpecialUnit> Set<T> select(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
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
				results.add(DataBaseORMGenerator.mapTo(expectedType.isInterface() ? (Class<T>) SpecialUnit.class : expectedType, stmnt));
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

	public static <T extends ISpecialUnit> boolean exist(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
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


	private static <T extends ISpecialUnit> String selectQuery(Class<T> expectedType, String from, String where, Object ... params)
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
		return String.format("SELECT SpecialUnit.*, Hero.* FROM SpecialUnit%s LEFT JOIN Hero USING (owner, name, type)%s", (from != null && !from.isEmpty()) ? ", "+from : "", (where != null && !where.isEmpty()) ? " WHERE "+where : "");
	}

	public static <T extends ISpecialUnit> void insertOrUpdate(SEPCommonDB db, T specialUnit) throws SQLDataBaseException
	{
		try
		{
			boolean exist = exist(db, specialUnit.getClass(), null, " SpecialUnit.owner = %s AND SpecialUnit.name = %s", "'"+specialUnit.getOwner()+"'", "'"+specialUnit.getName()+"'");
			if (exist)
			{
				db.getDB().exec(String.format("UPDATE SpecialUnit SET type = %s,  fleetName = %s WHERE  SpecialUnit.owner = %s AND SpecialUnit.name = %s ;", "'"+specialUnit.getType()+"'", "'"+specialUnit.getFleetName()+"'", "'"+specialUnit.getOwner()+"'", "'"+specialUnit.getName()+"'").replaceAll("'null'", "NULL"));
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
			else
			{
				if (!exist) db.getDB().exec(String.format("INSERT INTO SpecialUnit (owner, name, type, fleetName) VALUES (%s, %s, %s, %s);", "'"+specialUnit.getOwner()+"'", "'"+specialUnit.getName()+"'", "'"+specialUnit.getType()+"'", "'"+specialUnit.getFleetName()+"'").replaceAll("'null'", "NULL"));
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
