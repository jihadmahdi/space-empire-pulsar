package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.base.BaseSpaceRoad;
import org.axan.sep.common.db.ISpaceRoad;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteStatement;
import java.util.HashSet;
import java.util.Set;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;
import org.axan.sep.common.db.IGameConfig;

public class SpaceRoad implements ISpaceRoad
{
	private final BaseSpaceRoad baseSpaceRoadProxy;

	public SpaceRoad(String name, String builder, String spaceCounterAType, String spaceCounterACelestialBodyName, Integer spaceCounterATurn, String spaceCounterBType, String spaceCounterBCelestialBodyName, Integer spaceCounterBTurn)
	{
		baseSpaceRoadProxy = new BaseSpaceRoad(name, builder, spaceCounterAType, spaceCounterACelestialBodyName, spaceCounterATurn, spaceCounterBType, spaceCounterBCelestialBodyName, spaceCounterBTurn);
	}

	public SpaceRoad(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		this.baseSpaceRoadProxy = new BaseSpaceRoad(stmnt);
	}

	public String getName()
	{
		return baseSpaceRoadProxy.getName();
	}

	public String getBuilder()
	{
		return baseSpaceRoadProxy.getBuilder();
	}

	public String getSpaceCounterAType()
	{
		return baseSpaceRoadProxy.getSpaceCounterAType();
	}

	public String getSpaceCounterACelestialBodyName()
	{
		return baseSpaceRoadProxy.getSpaceCounterACelestialBodyName();
	}

	public Integer getSpaceCounterATurn()
	{
		return baseSpaceRoadProxy.getSpaceCounterATurn();
	}

	public String getSpaceCounterBType()
	{
		return baseSpaceRoadProxy.getSpaceCounterBType();
	}

	public String getSpaceCounterBCelestialBodyName()
	{
		return baseSpaceRoadProxy.getSpaceCounterBCelestialBodyName();
	}

	public Integer getSpaceCounterBTurn()
	{
		return baseSpaceRoadProxy.getSpaceCounterBTurn();
	}

	public static <T extends ISpaceRoad> Set<T> select(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, String from, String where, Object ... params) throws SQLiteDBException
	{
		try
		{
			Set<T> results = new HashSet<T>();

			if (where != null && params != null) where = String.format(where, params);
			SQLiteStatement stmnt = conn.prepare(String.format("SELECT SpaceRoad.* FROM SpaceRoad%s%s ;", (from != null && !from.isEmpty()) ? ", "+from : "", (where != null && !where.isEmpty()) ? " WHERE "+where : ""));
			while(stmnt.step())
			{
				results.add(SQLiteORMGenerator.mapTo(expectedType.isInterface() ? (Class<T>) SpaceRoad.class : expectedType, stmnt, config));
			}
			return results;
		}
		catch(Exception e)
		{
			throw new SQLiteDBException(e);
		}
	}


	public static <T extends ISpaceRoad> void insertOrUpdate(SQLiteConnection conn, T spaceRoad) throws SQLiteDBException
	{
		try
		{
			SQLiteStatement stmnt = conn.prepare(String.format("SELECT EXISTS ( SELECT name FROM SpaceRoad WHERE name = %s AND builder = %s) AS exist ;", "'"+spaceRoad.getName()+"'", "'"+spaceRoad.getBuilder()+"'"));
			stmnt.step();
			if (stmnt.columnInt(0) == 0)
			{
				conn.exec(String.format("INSERT INTO SpaceRoad (name, builder, spaceCounterAType, spaceCounterACelestialBodyName, spaceCounterATurn, spaceCounterBType, spaceCounterBCelestialBodyName, spaceCounterBTurn) VALUES (%s, %s, %s, %s, %s, %s, %s, %s);", "'"+spaceRoad.getName()+"'", "'"+spaceRoad.getBuilder()+"'", "'"+spaceRoad.getSpaceCounterAType()+"'", "'"+spaceRoad.getSpaceCounterACelestialBodyName()+"'", "'"+spaceRoad.getSpaceCounterATurn()+"'", "'"+spaceRoad.getSpaceCounterBType()+"'", "'"+spaceRoad.getSpaceCounterBCelestialBodyName()+"'", "'"+spaceRoad.getSpaceCounterBTurn()+"'"));
			}
			else
			{
				conn.exec(String.format("UPDATE SpaceRoad SET  spaceCounterAType = %s,  spaceCounterACelestialBodyName = %s,  spaceCounterATurn = %s,  spaceCounterBType = %s,  spaceCounterBCelestialBodyName = %s,  spaceCounterBTurn = %s WHERE  name = %s AND builder = %s ;", "'"+spaceRoad.getSpaceCounterAType()+"'", "'"+spaceRoad.getSpaceCounterACelestialBodyName()+"'", "'"+spaceRoad.getSpaceCounterATurn()+"'", "'"+spaceRoad.getSpaceCounterBType()+"'", "'"+spaceRoad.getSpaceCounterBCelestialBodyName()+"'", "'"+spaceRoad.getSpaceCounterBTurn()+"'", "'"+spaceRoad.getName()+"'", "'"+spaceRoad.getBuilder()+"'"));
			}
		}
		catch(Exception e)
		{
			throw new SQLiteDBException(e);
		}
	}
}
