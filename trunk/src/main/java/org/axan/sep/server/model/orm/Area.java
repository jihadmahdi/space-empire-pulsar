package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.base.BaseArea;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.SEPUtils.Location;
import java.util.Set;
import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;
import java.util.HashSet;
import com.almworks.sqlite4java.SQLiteStatement;
import com.almworks.sqlite4java.SQLiteConnection;

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
