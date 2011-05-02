package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.base.BaseMovePlan;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import org.axan.sep.common.IGameConfig;
import java.util.Set;
import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;
import java.util.HashSet;
import com.almworks.sqlite4java.SQLiteStatement;
import com.almworks.sqlite4java.SQLiteConnection;

public class MovePlan implements IMovePlan
{
	private final BaseMovePlan baseMovePlanProxy;

	public MovePlan(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		this.baseMovePlanProxy = new BaseMovePlan(stmnt);
	}

	public Integer getPriority()
	{
		return baseMovePlanProxy.getPriority();
	}

	public String getOwner()
	{
		return baseMovePlanProxy.getOwner();
	}

	public Boolean getAttack()
	{
		return baseMovePlanProxy.getAttack();
	}

	public String getDestination()
	{
		return baseMovePlanProxy.getDestination();
	}

	public Integer getTurn()
	{
		return baseMovePlanProxy.getTurn();
	}

	public Integer getDelay()
	{
		return baseMovePlanProxy.getDelay();
	}

	public String getName()
	{
		return baseMovePlanProxy.getName();
	}

	public static <T extends IMovePlan> Set<T> select(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, boolean lastVersion, String where, Object ... params) throws SQLiteDBException
	{
		try
		{
			Set<T> results = new HashSet<T>();

			if (where != null && params != null) where = String.format(where, params);
			if (lastVersion)
			{
				where = String.format("%s(VersionedMovePlan.turn = ( SELECT MAX(LVVersionedMovePlan.turn) FROM VersionedMovePlan LVVersionedMovePlan WHERE LVVersionedMovePlan.priority = MovePlan.priority AND LVVersionedMovePlan.owner = MovePlan.owner AND LVVersionedMovePlan.attack = MovePlan.attack AND LVVersionedMovePlan.destination = MovePlan.destination AND LVVersionedMovePlan.turn = MovePlan.turn AND LVVersionedMovePlan.delay = MovePlan.delay AND LVVersionedMovePlan.name = MovePlan.name ))", (where != null && !where.isEmpty()) ? "("+where+") AND " : "");
			}
			SQLiteStatement stmnt = conn.prepare(String.format("SELECT * FROM MovePlan%s ;", (where != null && !where.isEmpty()) ? " WHERE "+where : ""));
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
