package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.base.BaseArea;
import org.axan.sep.common.IGameConfig;
import com.almworks.sqlite4java.SQLiteStatement;
import java.util.Set;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import com.almworks.sqlite4java.SQLiteConnection;
import org.axan.sep.common.SEPUtils.Location;
import java.util.HashSet;
import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;

public class Area implements IArea
{
	private final BaseArea baseAreaProxy;

	public Area(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		this.baseAreaProxy = new BaseArea(stmnt);
	}

	public Boolean getIsSun()
	{
		return baseAreaProxy.getIsSun();
	}

	public Location getLocation()
	{
		return (baseAreaProxy.getLocation_x() == null) ? null : new Location(baseAreaProxy.getLocation_x(), baseAreaProxy.getLocation_y(), baseAreaProxy.getLocation_z());
	}

	public static <T extends IArea> Set<T> select(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, boolean lastVersion, String where, Object ... params) throws SQLiteDBException
	{
		try
		{
			Set<T> results = new HashSet<T>();

			if (where != null && params != null) where = String.format(where, params);
			SQLiteStatement stmnt = conn.prepare(String.format("SELECT * FROM Area%s ;", (where != null && !where.isEmpty()) ? " WHERE "+where : ""));
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
