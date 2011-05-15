package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.base.BaseArea;
import org.axan.sep.common.db.IArea;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteStatement;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.IGameConfig;

public class Area implements IArea
{
	private final BaseArea baseAreaProxy;
	private Location location;

	public Area(Location location, Boolean isSun)
	{
		baseAreaProxy = new BaseArea(location == null ? null : location.x, location == null ? null : location.y, location == null ? null : location.z, isSun);
		this.location = location;
	}

	public Area(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		this.baseAreaProxy = new BaseArea(stmnt);
		this.location = (baseAreaProxy.getLocation_x() == null ? null : new Location(baseAreaProxy.getLocation_x(), baseAreaProxy.getLocation_y(), baseAreaProxy.getLocation_z()));
	}

	public Location getLocation()
	{
		return location;
	}

	public Boolean getIsSun()
	{
		return baseAreaProxy.getIsSun();
	}

	public static <T extends IArea> Set<T> select(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, String from, String where, Object ... params) throws SQLiteDBException
	{
		try
		{
			Set<T> results = new HashSet<T>();
			SQLiteStatement stmnt = conn.prepare(selectQuery(expectedType, from, where, params)+";");
			while(stmnt.step())
			{
				results.add(SQLiteORMGenerator.mapTo(expectedType.isInterface() ? (Class<T>) Area.class : expectedType, stmnt, config));
			}
			return results;
		}
		catch(Exception e)
		{
			throw new SQLiteDBException(e);
		}
	}

	public static <T extends IArea> boolean exist(SQLiteConnection conn, Class<T> expectedType, String from, String where, Object ... params) throws SQLiteDBException
	{
		try
		{
			SQLiteStatement stmnt = conn.prepare("SELECT EXISTS ( "+selectQuery(expectedType, from, where, params) + " );");
			return stmnt.step() && stmnt.columnInt(0) != 0;
		}
		catch(Exception e)
		{
			throw new SQLiteDBException(e);
		}
	}


	private static <T extends IArea> String selectQuery(Class<T> expectedType, String from, String where, Object ... params)
	{
		where = (where == null) ? null : (params == null) ? where : String.format(Locale.UK, where, params);
		if (where != null) where = String.format("(%s)",where);
		return String.format("SELECT Area.* FROM Area%s%s", (from != null && !from.isEmpty()) ? ", "+from : "", (where != null && !where.isEmpty()) ? " WHERE "+where : "");
	}

	public static <T extends IArea> void insertOrUpdate(SQLiteConnection conn, T area) throws SQLiteDBException
	{
		try
		{
			SQLiteStatement stmnt = conn.prepare(String.format("SELECT EXISTS ( SELECT location_x FROM Area WHERE location_x = %s AND location_y = %s AND location_z = %s) AS exist ;", area.getLocation() == null ? "NULL" : "'"+area.getLocation().x+"'", area.getLocation() == null ? "NULL" : "'"+area.getLocation().y+"'", area.getLocation() == null ? "NULL" : "'"+area.getLocation().z+"'"));
			stmnt.step();
			if (stmnt.columnInt(0) == 0)
			{
				conn.exec(String.format("INSERT INTO Area (location_x, location_y, location_z, isSun) VALUES (%s, %s, %s, %s);", area.getLocation() == null ? "NULL" : "'"+area.getLocation().x+"'", area.getLocation() == null ? "NULL" : "'"+area.getLocation().y+"'", area.getLocation() == null ? "NULL" : "'"+area.getLocation().z+"'", "'"+area.getIsSun()+"'").replaceAll("'null'", "NULL"));
			}
			else
			{
				conn.exec(String.format("UPDATE Area SET isSun = %s WHERE  location_x = %s AND location_y = %s AND location_z = %s ;", "'"+area.getIsSun()+"'", area.getLocation() == null ? "NULL" : "'"+area.getLocation().x+"'", area.getLocation() == null ? "NULL" : "'"+area.getLocation().y+"'", area.getLocation() == null ? "NULL" : "'"+area.getLocation().z+"'").replaceAll("'null'", "NULL"));
			}
		}
		catch(Exception e)
		{
			throw new SQLiteDBException(e);
		}
	}
}
