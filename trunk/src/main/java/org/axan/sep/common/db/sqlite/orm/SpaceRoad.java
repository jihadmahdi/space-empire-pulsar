package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.base.BaseSpaceRoad;
import org.axan.sep.common.IGameConfig;
import com.almworks.sqlite4java.SQLiteStatement;
import java.util.Set;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import com.almworks.sqlite4java.SQLiteConnection;
import java.util.HashSet;
import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;

public class SpaceRoad implements ISpaceRoad
{
	private final BaseSpaceRoad baseSpaceRoadProxy;

	public SpaceRoad(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		this.baseSpaceRoadProxy = new BaseSpaceRoad(stmnt);
	}

	public String getSpaceCounterBCelestialBodyName()
	{
		return baseSpaceRoadProxy.getSpaceCounterBCelestialBodyName();
	}

	public Integer getSpaceCounterATurn()
	{
		return baseSpaceRoadProxy.getSpaceCounterATurn();
	}

	public String getSpaceCounterAType()
	{
		return baseSpaceRoadProxy.getSpaceCounterAType();
	}

	public String getSpaceCounterBType()
	{
		return baseSpaceRoadProxy.getSpaceCounterBType();
	}

	public Integer getSpaceCounterBTurn()
	{
		return baseSpaceRoadProxy.getSpaceCounterBTurn();
	}

	public String getSpaceCounterACelestialBodyName()
	{
		return baseSpaceRoadProxy.getSpaceCounterACelestialBodyName();
	}

	public static <T extends ISpaceRoad> Set<T> select(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, boolean lastVersion, String where, Object ... params) throws SQLiteDBException
	{
		try
		{
			Set<T> results = new HashSet<T>();

			if (where != null && params != null) where = String.format(where, params);
			SQLiteStatement stmnt = conn.prepare(String.format("SELECT * FROM SpaceRoad%s ;", (where != null && !where.isEmpty()) ? " WHERE "+where : ""));
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
