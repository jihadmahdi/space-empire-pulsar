package org.axan.sep.common.db.orm;

import java.lang.Exception;
import org.axan.sep.common.db.orm.base.IBaseFleetComposition;
import org.axan.sep.common.db.orm.base.BaseFleetComposition;
import org.axan.sep.common.db.IFleetComposition;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.axan.eplib.orm.DataBaseORMGenerator;
import org.axan.eplib.orm.ISQLDataBaseStatement;
import org.axan.eplib.orm.ISQLDataBaseStatementJob;
import org.axan.eplib.orm.SQLDataBaseException;
import org.axan.eplib.orm.sqlite.SQLiteDB;
import org.axan.sep.common.db.IGameConfig;
import org.axan.sep.common.db.SEPCommonDB;

public class FleetComposition implements IFleetComposition
{
	private final IBaseFleetComposition baseFleetCompositionProxy;

	FleetComposition(IBaseFleetComposition baseFleetCompositionProxy)
	{
		this.baseFleetCompositionProxy = baseFleetCompositionProxy;
	}

	public FleetComposition(String fleetOwner, String fleetName, Integer fleetTurn, String starshipTemplate, Integer quantity)
	{
		this(new BaseFleetComposition(fleetOwner, fleetName, fleetTurn, starshipTemplate, quantity));
	}

	public FleetComposition(ISQLDataBaseStatement stmnt) throws Exception
	{
		this(new BaseFleetComposition(stmnt));
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

	public static <T extends IFleetComposition> Set<T> select(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
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
				results.add(DataBaseORMGenerator.mapTo(expectedType.isInterface() ? (Class<T>) FleetComposition.class : expectedType, stmnt));
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

	public static <T extends IFleetComposition> boolean exist(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
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
						return stmnt.step() && stmnt.columnValue(0) != null;
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


	private static <T extends IFleetComposition> String selectQuery(Class<T> expectedType, String from, String where, Object ... params)
	{
		where = (where == null) ? null : (params == null) ? where : String.format(Locale.UK, where, params);
		if (where != null) where = String.format("(%s)",where);
		return String.format("SELECT FleetComposition.* FROM FleetComposition%s%s", (from != null && !from.isEmpty()) ? ", "+from : "", (where != null && !where.isEmpty()) ? " WHERE "+where : "");
	}

	public static <T extends IFleetComposition> void insertOrUpdate(SEPCommonDB db, T fleetComposition) throws SQLDataBaseException
	{
		try
		{
			boolean exist = exist(db, fleetComposition.getClass(), null, " FleetComposition.fleetOwner = %s AND FleetComposition.fleetName = %s AND FleetComposition.fleetTurn = %s AND FleetComposition.starshipTemplate = %s", "'"+fleetComposition.getFleetOwner()+"'", "'"+fleetComposition.getFleetName()+"'", "'"+fleetComposition.getFleetTurn()+"'", "'"+fleetComposition.getStarshipTemplate()+"'");
			if (exist)
			{
				db.getDB().exec(String.format("UPDATE FleetComposition SET quantity = %s WHERE  FleetComposition.fleetOwner = %s AND FleetComposition.fleetName = %s AND FleetComposition.fleetTurn = %s AND FleetComposition.starshipTemplate = %s ;", "'"+fleetComposition.getQuantity()+"'", "'"+fleetComposition.getFleetOwner()+"'", "'"+fleetComposition.getFleetName()+"'", "'"+fleetComposition.getFleetTurn()+"'", "'"+fleetComposition.getStarshipTemplate()+"'").replaceAll("'null'", "NULL"));
			}
			else
			{
				if (!exist) db.getDB().exec(String.format("INSERT INTO FleetComposition (fleetOwner, fleetName, fleetTurn, starshipTemplate, quantity) VALUES (%s, %s, %s, %s, %s);", "'"+fleetComposition.getFleetOwner()+"'", "'"+fleetComposition.getFleetName()+"'", "'"+fleetComposition.getFleetTurn()+"'", "'"+fleetComposition.getStarshipTemplate()+"'", "'"+fleetComposition.getQuantity()+"'").replaceAll("'null'", "NULL"));
			}
		}
		catch(Exception e)
		{
			throw new SQLDataBaseException(e);
		}
	}
}
