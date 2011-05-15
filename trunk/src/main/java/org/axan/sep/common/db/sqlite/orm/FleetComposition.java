package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.base.BaseFleetComposition;
import org.axan.sep.common.db.IFleetComposition;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteStatement;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;
import org.axan.sep.common.db.IGameConfig;

public class FleetComposition implements IFleetComposition
{
	private final BaseFleetComposition baseFleetCompositionProxy;

	public FleetComposition(String fleetOwner, String fleetName, Integer fleetTurn, String starshipTemplate, Integer quantity)
	{
		baseFleetCompositionProxy = new BaseFleetComposition(fleetOwner, fleetName, fleetTurn, starshipTemplate, quantity);
	}

	public FleetComposition(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		this.baseFleetCompositionProxy = new BaseFleetComposition(stmnt);
	}

	public String getFleetOwner()
	{
		return baseFleetCompositionProxy.getFleetOwner();
	}

	public String getFleetName()
	{
		return baseFleetCompositionProxy.getFleetName();
	}

	public Integer getFleetTurn()
	{
		return baseFleetCompositionProxy.getFleetTurn();
	}

	public String getStarshipTemplate()
	{
		return baseFleetCompositionProxy.getStarshipTemplate();
	}

	public Integer getQuantity()
	{
		return baseFleetCompositionProxy.getQuantity();
	}

	public static <T extends IFleetComposition> Set<T> select(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, String from, String where, Object ... params) throws SQLiteDBException
	{
		try
		{
			Set<T> results = new HashSet<T>();
			SQLiteStatement stmnt = conn.prepare(selectQuery(expectedType, from, where, params)+";");
			while(stmnt.step())
			{
				results.add(SQLiteORMGenerator.mapTo(expectedType.isInterface() ? (Class<T>) FleetComposition.class : expectedType, stmnt, config));
			}
			return results;
		}
		catch(Exception e)
		{
			throw new SQLiteDBException(e);
		}
	}

	public static <T extends IFleetComposition> boolean exist(SQLiteConnection conn, Class<T> expectedType, String from, String where, Object ... params) throws SQLiteDBException
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


	private static <T extends IFleetComposition> String selectQuery(Class<T> expectedType, String from, String where, Object ... params)
	{
		where = (where == null) ? null : (params == null) ? where : String.format(Locale.UK, where, params);
		if (where != null) where = String.format("(%s)",where);
		return String.format("SELECT FleetComposition.* FROM FleetComposition%s%s", (from != null && !from.isEmpty()) ? ", "+from : "", (where != null && !where.isEmpty()) ? " WHERE "+where : "");
	}

	public static <T extends IFleetComposition> void insertOrUpdate(SQLiteConnection conn, T fleetComposition) throws SQLiteDBException
	{
		try
		{
			SQLiteStatement stmnt = conn.prepare(String.format("SELECT EXISTS ( SELECT fleetOwner FROM FleetComposition WHERE fleetOwner = %s AND fleetName = %s AND fleetTurn = %s AND starshipTemplate = %s) AS exist ;", "'"+fleetComposition.getFleetOwner()+"'", "'"+fleetComposition.getFleetName()+"'", "'"+fleetComposition.getFleetTurn()+"'", "'"+fleetComposition.getStarshipTemplate()+"'"));
			stmnt.step();
			if (stmnt.columnInt(0) == 0)
			{
				conn.exec(String.format("INSERT INTO FleetComposition (fleetOwner, fleetName, fleetTurn, starshipTemplate, quantity) VALUES (%s, %s, %s, %s, %s);", "'"+fleetComposition.getFleetOwner()+"'", "'"+fleetComposition.getFleetName()+"'", "'"+fleetComposition.getFleetTurn()+"'", "'"+fleetComposition.getStarshipTemplate()+"'", "'"+fleetComposition.getQuantity()+"'").replaceAll("'null'", "NULL"));
			}
			else
			{
				conn.exec(String.format("UPDATE FleetComposition SET quantity = %s WHERE  fleetOwner = %s AND fleetName = %s AND fleetTurn = %s AND starshipTemplate = %s ;", "'"+fleetComposition.getQuantity()+"'", "'"+fleetComposition.getFleetOwner()+"'", "'"+fleetComposition.getFleetName()+"'", "'"+fleetComposition.getFleetTurn()+"'", "'"+fleetComposition.getStarshipTemplate()+"'").replaceAll("'null'", "NULL"));
			}
		}
		catch(Exception e)
		{
			throw new SQLiteDBException(e);
		}
	}
}
