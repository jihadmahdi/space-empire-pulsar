package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.CelestialBody;
import org.axan.sep.common.db.sqlite.orm.base.BaseVortex;
import org.axan.sep.common.db.IVortex;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteStatement;
import java.util.HashSet;
import java.util.Set;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.IAsteroidField;
import org.axan.sep.common.db.IGameConfig;
import org.axan.sep.common.db.INebula;
import org.axan.sep.common.db.IPlanet;
import org.axan.sep.common.db.IVortex;

public class Vortex extends CelestialBody implements IVortex
{
	private final BaseVortex baseVortexProxy;

	public Vortex(String name, eCelestialBodyType type, Location location, Integer onsetDate, Integer endDate, String destination)
	{
		super(name, type, location);
		baseVortexProxy = new BaseVortex(name, type.toString(), location == null ? null : location.x, location == null ? null : location.y, location == null ? null : location.z, onsetDate, endDate, destination);
	}

	public Vortex(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		super(stmnt, config);
		this.baseVortexProxy = new BaseVortex(stmnt);
	}

	public Integer getOnsetDate()
	{
		return baseVortexProxy.getOnsetDate();
	}

	public Integer getEndDate()
	{
		return baseVortexProxy.getEndDate();
	}

	public String getDestination()
	{
		return baseVortexProxy.getDestination();
	}

	public static <T extends IVortex> Set<T> select(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, String from, String where, Object ... params) throws SQLiteDBException
	{
		try
		{
			Set<T> results = new HashSet<T>();

			if (where != null && params != null) where = String.format(where, params);
			SQLiteStatement stmnt = conn.prepare(String.format("SELECT Vortex.* FROM Vortex%s%s ;", (from != null && !from.isEmpty()) ? ", "+from : "", (where != null && !where.isEmpty()) ? " WHERE "+where : ""));
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


	public static <T extends IVortex> void insertOrUpdate(SQLiteConnection conn, T vortex) throws SQLiteDBException
	{
		try
		{
			SQLiteStatement stmnt = conn.prepare(String.format("SELECT EXISTS ( SELECT name FROM Vortex WHERE name = %s) AS exist ;", "'"+vortex.getName()+"'"));
			stmnt.step();
			if (stmnt.columnInt(0) == 0)
			{
				conn.exec(String.format("INSERT INTO Vortex (name, type, onsetDate, endDate, destination) VALUES (%s, %s, %s, %s, %s);", "'"+vortex.getName()+"'", "'"+vortex.getType()+"'", "'"+vortex.getOnsetDate()+"'", "'"+vortex.getEndDate()+"'", "'"+vortex.getDestination()+"'"));
				switch(vortex.getType())
				{
					case Vortex:
					{
						break;
					}
					case Planet:
					{
						break;
					}
					case AsteroidField:
					{
						break;
					}
					case Nebula:
					{
						break;
					}
				}
			}
			else
			{
				conn.exec(String.format("UPDATE Vortex SET  type = %s,  onsetDate = %s,  endDate = %s,  destination = %s WHERE  name = %s ;", "'"+vortex.getType()+"'", "'"+vortex.getOnsetDate()+"'", "'"+vortex.getEndDate()+"'", "'"+vortex.getDestination()+"'", "'"+vortex.getName()+"'"));
				switch(vortex.getType())
				{
					case Vortex:
					{
						break;
					}
					case Planet:
					{
						break;
					}
					case AsteroidField:
					{
						break;
					}
					case Nebula:
					{
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
