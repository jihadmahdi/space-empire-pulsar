package org.axan.sep.common.db.orm;

import java.lang.Exception;
import org.axan.sep.common.db.orm.base.IBaseArea;
import org.axan.sep.common.db.orm.base.BaseArea;
import org.axan.sep.common.db.IArea;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.axan.eplib.orm.DataBaseORMGenerator;
import org.axan.eplib.orm.ISQLDataBaseStatement;
import org.axan.eplib.orm.ISQLDataBaseStatementJob;
import org.axan.eplib.orm.SQLDataBaseException;
import org.axan.eplib.orm.sqlite.SQLiteDB;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.IGameConfig;
import org.axan.sep.common.db.SEPCommonDB;

public class Area implements IArea
{
	private final IBaseArea baseAreaProxy;
	private final Location location;

	Area(IBaseArea baseAreaProxy)
	{
		this.baseAreaProxy = baseAreaProxy;
		this.location = (baseAreaProxy.getLocation_x() == null ? null : new Location(baseAreaProxy.getLocation_x(), baseAreaProxy.getLocation_y(), baseAreaProxy.getLocation_z()));
	}

	public Area(Location location, Boolean isSun)
	{
		this(new BaseArea(location == null ? null : location.x, location == null ? null : location.y, location == null ? null : location.z, isSun));
	}

	public Area(ISQLDataBaseStatement stmnt) throws Exception
	{
		this(new BaseArea(stmnt));
	}

	public Location getLocation()
	{
		return location;
	}

	public Boolean getIsSun()
	{
		return baseAreaProxy.getIsSun();
	}
	
	/**
	 * Select the first object of the resultset.
	 * @param <T>
	 * @param db
	 * @param expectedType
	 * @param from
	 * @param where
	 * @param params
	 * @return
	 * @throws SQLDataBaseException
	 */
	public static <T extends IArea> T selectOne(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
	{
		Set<T> results = select(db, expectedType, from, where, params);
		if (results.isEmpty()) return null;
		return results.iterator().next();
	}
	
	public static <T extends IArea> Set<T> select(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
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
				results.add(DataBaseORMGenerator.mapTo(expectedType.isInterface() ? (Class<T>) Area.class : expectedType, stmnt));
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

	public static <T extends IArea> boolean exist(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
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
						if (stmnt.step())
						{
							Object o = stmnt.columnValue(0);
							return o != null;
						}
						
						return false;
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
	
	
	//////////


	private static <T extends IArea> String selectQuery(Class<T> expectedType, String from, String where, Object ... params)
	{
		where = (where == null) ? null : (params == null) ? where : String.format(Locale.UK, where, params);
		if (where != null) where = String.format("(%s)",where);
		return String.format("SELECT Area.* FROM Area%s%s", (from != null && !from.isEmpty()) ? ", "+from : "", (where != null && !where.isEmpty()) ? " WHERE "+where : "");
	}

	public static <T extends IArea> void insertOrUpdate(SEPCommonDB db, T area) throws SQLDataBaseException
	{
		try
		{
			boolean exist = exist(db, area.getClass(), null, " Area.location_x = %s AND Area.location_y = %s AND Area.location_z = %s", area.getLocation() == null ? "NULL" : "'"+area.getLocation().x+"'", area.getLocation() == null ? "NULL" : "'"+area.getLocation().y+"'", area.getLocation() == null ? "NULL" : "'"+area.getLocation().z+"'");
			if (exist)
			{
				db.getDB().exec(String.format("UPDATE Area SET isSun = %s WHERE  Area.location_x = %s AND Area.location_y = %s AND Area.location_z = %s ;", "'"+area.getIsSun()+"'", area.getLocation() == null ? "NULL" : "'"+area.getLocation().x+"'", area.getLocation() == null ? "NULL" : "'"+area.getLocation().y+"'", area.getLocation() == null ? "NULL" : "'"+area.getLocation().z+"'").replaceAll("'null'", "NULL"));
			}
			else
			{
				if (!exist) db.getDB().exec(String.format("INSERT INTO Area (location_x, location_y, location_z, isSun) VALUES (%s, %s, %s, %s);", area.getLocation() == null ? "NULL" : "'"+area.getLocation().x+"'", area.getLocation() == null ? "NULL" : "'"+area.getLocation().y+"'", area.getLocation() == null ? "NULL" : "'"+area.getLocation().z+"'", "'"+area.getIsSun()+"'").replaceAll("'null'", "NULL"));
			}
		}
		catch(Exception e)
		{
			throw new SQLDataBaseException(e);
		}
	}
}
