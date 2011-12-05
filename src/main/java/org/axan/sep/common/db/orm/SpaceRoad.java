package org.axan.sep.common.db.orm;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.axan.eplib.orm.DataBaseORMGenerator;
import org.axan.eplib.orm.ISQLDataBaseStatement;
import org.axan.eplib.orm.ISQLDataBaseStatementJob;
import org.axan.eplib.orm.SQLDataBaseException;
import org.axan.sep.common.db.ISpaceRoad;
import org.axan.sep.common.db.SEPCommonDB;
import org.axan.sep.common.db.orm.base.BaseSpaceRoad;
import org.axan.sep.common.db.orm.base.IBaseSpaceRoad;

public class SpaceRoad implements ISpaceRoad
{
	private final IBaseSpaceRoad baseSpaceRoadProxy;

	SpaceRoad(IBaseSpaceRoad baseSpaceRoadProxy)
	{
		this.baseSpaceRoadProxy = baseSpaceRoadProxy;
	}

	public SpaceRoad(String name, String builder, String spaceCounterAType, String spaceCounterACelestialBodyName, Integer spaceCounterATurn, String spaceCounterBType, String spaceCounterBCelestialBodyName, Integer spaceCounterBTurn)
	{
		this(new BaseSpaceRoad(name, builder, spaceCounterAType, spaceCounterACelestialBodyName, spaceCounterATurn, spaceCounterBType, spaceCounterBCelestialBodyName, spaceCounterBTurn));
	}

	public SpaceRoad(ISQLDataBaseStatement stmnt) throws Exception
	{
		this(new BaseSpaceRoad(stmnt));
	}

	@Override
	public String getName()
	{
		return baseSpaceRoadProxy.getName();
	}

	@Override
	public String getBuilder()
	{
		return baseSpaceRoadProxy.getBuilder();
	}

	@Override
	public String getSpaceCounterAType()
	{
		return baseSpaceRoadProxy.getSpaceCounterAType();
	}

	@Override
	public String getSpaceCounterACelestialBodyName()
	{
		return baseSpaceRoadProxy.getSpaceCounterACelestialBodyName();
	}

	@Override
	public Integer getSpaceCounterATurn()
	{
		return baseSpaceRoadProxy.getSpaceCounterATurn();
	}

	@Override
	public String getSpaceCounterBType()
	{
		return baseSpaceRoadProxy.getSpaceCounterBType();
	}

	@Override
	public String getSpaceCounterBCelestialBodyName()
	{
		return baseSpaceRoadProxy.getSpaceCounterBCelestialBodyName();
	}

	@Override
	public Integer getSpaceCounterBTurn()
	{
		return baseSpaceRoadProxy.getSpaceCounterBTurn();
	}

	public static <T extends ISpaceRoad> T selectOne(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
	{
		Set<T> results = select(db, expectedType, from, (where==null?"":where+" ")+"LIMIT 1", params);
		if (results.isEmpty()) return null;
		return results.iterator().next();
	}

	public static <T extends ISpaceRoad> Set<T> select(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
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
				results.add(DataBaseORMGenerator.mapTo(expectedType.isInterface() ? (Class<T>) SpaceRoad.class : expectedType, stmnt));
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

	public static <T extends ISpaceRoad> boolean exist(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
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


	private static <T extends ISpaceRoad> String selectQuery(Class<T> expectedType, String from, String where, Object ... params)
	{
		where = (where == null) ? null : (params == null) ? where : String.format(Locale.UK, where, params);
		if (where != null) where = String.format("(%s)",where);
		return String.format("SELECT SpaceRoad.* FROM SpaceRoad%s%s", (from != null && !from.isEmpty()) ? ", "+from : "", (where != null && !where.isEmpty()) ? " WHERE "+where : "");
	}

	public static <T extends ISpaceRoad> void insertOrUpdate(SEPCommonDB db, T spaceRoad) throws SQLDataBaseException
	{
		try
		{
			boolean exist = exist(db, spaceRoad.getClass(), null, " SpaceRoad.name = %s AND SpaceRoad.builder = %s", "'"+spaceRoad.getName()+"'", "'"+spaceRoad.getBuilder()+"'");
			if (exist)
			{
				db.getDB().exec(String.format("UPDATE SpaceRoad SET spaceCounterAType = %s,  spaceCounterACelestialBodyName = %s,  spaceCounterATurn = %s,  spaceCounterBType = %s,  spaceCounterBCelestialBodyName = %s,  spaceCounterBTurn = %s WHERE  SpaceRoad.name = %s AND SpaceRoad.builder = %s ;", "'"+spaceRoad.getSpaceCounterAType()+"'", "'"+spaceRoad.getSpaceCounterACelestialBodyName()+"'", "'"+spaceRoad.getSpaceCounterATurn()+"'", "'"+spaceRoad.getSpaceCounterBType()+"'", "'"+spaceRoad.getSpaceCounterBCelestialBodyName()+"'", "'"+spaceRoad.getSpaceCounterBTurn()+"'", "'"+spaceRoad.getName()+"'", "'"+spaceRoad.getBuilder()+"'").replaceAll("'null'", "NULL"));
			}
			else
			{
				if (!exist) db.getDB().exec(String.format("INSERT INTO SpaceRoad (name, builder, spaceCounterAType, spaceCounterACelestialBodyName, spaceCounterATurn, spaceCounterBType, spaceCounterBCelestialBodyName, spaceCounterBTurn) VALUES (%s, %s, %s, %s, %s, %s, %s, %s);", "'"+spaceRoad.getName()+"'", "'"+spaceRoad.getBuilder()+"'", "'"+spaceRoad.getSpaceCounterAType()+"'", "'"+spaceRoad.getSpaceCounterACelestialBodyName()+"'", "'"+spaceRoad.getSpaceCounterATurn()+"'", "'"+spaceRoad.getSpaceCounterBType()+"'", "'"+spaceRoad.getSpaceCounterBCelestialBodyName()+"'", "'"+spaceRoad.getSpaceCounterBTurn()+"'").replaceAll("'null'", "NULL"));
			}
		}
		catch(Exception e)
		{
			throw new SQLDataBaseException(e);
		}
	}
}
