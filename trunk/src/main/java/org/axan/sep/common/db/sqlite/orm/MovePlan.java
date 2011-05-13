package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.base.BaseMovePlan;
import org.axan.sep.common.db.IMovePlan;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteStatement;
import java.util.HashSet;
import java.util.Set;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;
import org.axan.sep.common.db.IGameConfig;

public class MovePlan implements IMovePlan
{
	private final BaseMovePlan baseMovePlanProxy;

	public MovePlan(String owner, String name, Integer turn, Integer priority, Integer delay, Boolean attack, String destination)
	{
		baseMovePlanProxy = new BaseMovePlan(owner, name, turn, priority, delay, attack, destination);
	}

	public MovePlan(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		this.baseMovePlanProxy = new BaseMovePlan(stmnt);
	}

	public String getOwner()
	{
		return baseMovePlanProxy.getOwner();
	}

	public String getName()
	{
		return baseMovePlanProxy.getName();
	}

	public Integer getTurn()
	{
		return baseMovePlanProxy.getTurn();
	}

	public Integer getPriority()
	{
		return baseMovePlanProxy.getPriority();
	}

	public Integer getDelay()
	{
		return baseMovePlanProxy.getDelay();
	}

	public Boolean getAttack()
	{
		return baseMovePlanProxy.getAttack();
	}

	public String getDestination()
	{
		return baseMovePlanProxy.getDestination();
	}

	public static <T extends IMovePlan> Set<T> select(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, String from, String where, Object ... params) throws SQLiteDBException
	{
		try
		{
			Set<T> results = new HashSet<T>();

			if (where != null && params != null) where = String.format(where, params);
			SQLiteStatement stmnt = conn.prepare(String.format("SELECT MovePlan.* FROM MovePlan%s%s ;", (from != null && !from.isEmpty()) ? ", "+from : "", (where != null && !where.isEmpty()) ? " WHERE "+where : ""));
			while(stmnt.step())
			{
				results.add(SQLiteORMGenerator.mapTo(expectedType.isInterface() ? (Class<T>) MovePlan.class : expectedType, stmnt, config));
			}
			return results;
		}
		catch(Exception e)
		{
			throw new SQLiteDBException(e);
		}
	}


	public static <T extends IMovePlan> void insertOrUpdate(SQLiteConnection conn, T movePlan) throws SQLiteDBException
	{
		try
		{
			SQLiteStatement stmnt = conn.prepare(String.format("SELECT EXISTS ( SELECT owner FROM MovePlan WHERE owner = %s AND name = %s AND turn = %s AND priority = %s) AS exist ;", "'"+movePlan.getOwner()+"'", "'"+movePlan.getName()+"'", "'"+movePlan.getTurn()+"'", "'"+movePlan.getPriority()+"'"));
			stmnt.step();
			if (stmnt.columnInt(0) == 0)
			{
				conn.exec(String.format("INSERT INTO MovePlan (owner, name, turn, priority, delay, attack, destination) VALUES (%s, %s, %s, %s, %s, %s, %s);", "'"+movePlan.getOwner()+"'", "'"+movePlan.getName()+"'", "'"+movePlan.getTurn()+"'", "'"+movePlan.getPriority()+"'", "'"+movePlan.getDelay()+"'", "'"+movePlan.getAttack()+"'", "'"+movePlan.getDestination()+"'"));
			}
			else
			{
				conn.exec(String.format("UPDATE MovePlan SET  delay = %s,  attack = %s,  destination = %s WHERE  owner = %s AND name = %s AND turn = %s AND priority = %s ;", "'"+movePlan.getDelay()+"'", "'"+movePlan.getAttack()+"'", "'"+movePlan.getDestination()+"'", "'"+movePlan.getOwner()+"'", "'"+movePlan.getName()+"'", "'"+movePlan.getTurn()+"'", "'"+movePlan.getPriority()+"'"));
			}
		}
		catch(Exception e)
		{
			throw new SQLiteDBException(e);
		}
	}
}
