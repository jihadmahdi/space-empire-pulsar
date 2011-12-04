package org.axan.sep.common.db.orm;

import java.lang.Exception;
import org.axan.sep.common.db.orm.base.IBaseGovernment;
import org.axan.sep.common.db.orm.base.BaseGovernment;
import org.axan.sep.common.db.IGovernment;
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

public class Government implements IGovernment
{
	private final IBaseGovernment baseGovernmentProxy;

	Government(IBaseGovernment baseGovernmentProxy)
	{
		this.baseGovernmentProxy = baseGovernmentProxy;
	}

	public Government(String owner, Integer turn, String fleetName, Integer fleetTurn, String planetName, Integer planetTurn)
	{
		this(new BaseGovernment(owner, turn, fleetName, fleetTurn, planetName, planetTurn));
	}

	public Government(ISQLDataBaseStatement stmnt) throws Exception
	{
		this(new BaseGovernment(stmnt));
	}

	public String getOwner()
	{
		return baseGovernmentProxy.getOwner();
	}

	public Integer getTurn()
	{
		return baseGovernmentProxy.getTurn();
	}

	public String getFleetName()
	{
		return baseGovernmentProxy.getFleetName();
	}

	public Integer getFleetTurn()
	{
		return baseGovernmentProxy.getFleetTurn();
	}

	public String getPlanetName()
	{
		return baseGovernmentProxy.getPlanetName();
	}

	public Integer getPlanetTurn()
	{
		return baseGovernmentProxy.getPlanetTurn();
	}

	/** Set maxVersion to null to select last version. */
	public static <T extends IGovernment> Set<T> selectMaxVersion(SEPCommonDB db, Class<T> expectedType, Integer maxVersion, String from, String where, Object ... params) throws SQLDataBaseException
	{
		return select(db, expectedType, true, maxVersion, from, where, params);
	}

	public static <T extends IGovernment> Set<T> selectVersion(SEPCommonDB db, Class<T> expectedType, int version, String from, String where, Object ... params) throws SQLDataBaseException
	{
		return select(db, expectedType, false, version, from, where, params);
	}

	public static <T extends IGovernment> Set<T> selectUnversioned(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
	{
		return select(db, expectedType, false, null, from, where, params);
	}

	private static <T extends IGovernment> Set<T> select(SEPCommonDB db, Class<T> expectedType, boolean maxVersion, Integer version, String from, String where, Object ... params) throws SQLDataBaseException
	{
		ISQLDataBaseStatement stmnt = null;
		try
		{
			Set<T> results = new HashSet<T>();
			stmnt = db.getDB().prepare(selectQuery(expectedType, maxVersion, version, from, where, params)+";", new ISQLDataBaseStatementJob<ISQLDataBaseStatement>()
			{
				@Override
				public ISQLDataBaseStatement job(ISQLDataBaseStatement stmnt) throws SQLDataBaseException
				{
					return stmnt;
				}
			});
			while(stmnt.step())
			{
				results.add(DataBaseORMGenerator.mapTo(expectedType.isInterface() ? (Class<T>) Government.class : expectedType, stmnt));
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

	/** Set maxVersion to null to select last version. */
	public static <T extends IGovernment> boolean existMaxVersion(SEPCommonDB db, Class<T> expectedType, Integer maxVersion, String from, String where, Object ... params) throws SQLDataBaseException
	{
		return exist(db, expectedType, true, maxVersion, from, where, params);
	}

	public static <T extends IGovernment> boolean existVersion(SEPCommonDB db,Class<T> expectedType, int version, String from, String where, Object ... params) throws SQLDataBaseException
	{
		return exist(db, expectedType, false, version, from, where, params);
	}

	public static <T extends IGovernment> boolean existUnversioned(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
	{
		return exist(db, expectedType, false, null, from, where, params);
	}

	private static <T extends IGovernment> boolean exist(SEPCommonDB db, Class<T> expectedType, boolean maxVersion, Integer version, String from, String where, Object ... params) throws SQLDataBaseException
	{
		try
		{
			return db.getDB().prepare(selectQuery(expectedType, maxVersion, version, from, where, params)+" ;", new ISQLDataBaseStatementJob<Boolean>()
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


	private static <T extends IGovernment> String selectQuery(Class<T> expectedType, boolean maxVersion, Integer version, String from, String where, Object ... params)
	{
		where = (where == null) ? null : (params == null) ? where : String.format(Locale.UK, where, params);
		if (where != null) where = String.format("(%s)",where);
		String versionFilter;
		if (maxVersion)
		{
			versionFilter = String.format("(Government.turn = ( SELECT MAX(LVGovernment.turn) FROM Government LVGovernment WHERE LVGovernment.owner = Government.owner AND LVGovernment.turn = Government.turn AND LVGovernment.fleetName = Government.fleetName AND LVGovernment.fleetTurn = Government.fleetTurn AND LVGovernment.planetName = Government.planetName AND LVGovernment.planetTurn = Government.planetTurn%s ))", (version != null && version >= 0) ? " AND LVGovernment.turn <= "+version : "");
		}
		else
		{
			versionFilter = (version == null) ? "" : String.format("(Government.turn = %d)", version);
		}
		if (versionFilter != null && !versionFilter.isEmpty()) where = (where == null) ? versionFilter : String.format("%s AND %s", where, versionFilter);
		return String.format("SELECT Government.* FROM Government%s%s", (from != null && !from.isEmpty()) ? ", "+from : "", (where != null && !where.isEmpty()) ? " WHERE "+where : "");
	}

	public static <T extends IGovernment> void insertOrUpdate(SEPCommonDB db, T government) throws SQLDataBaseException
	{
		try
		{
			boolean exist = existUnversioned(db, government.getClass(), null, " Government.owner = %s AND Government.turn = %s", "'"+government.getOwner()+"'", "'"+government.getTurn()+"'");
			if (exist)
			{
				db.getDB().exec(String.format("UPDATE Government SET fleetName = %s,  fleetTurn = %s,  planetName = %s,  planetTurn = %s WHERE  Government.owner = %s AND Government.turn = %s ;", "'"+government.getFleetName()+"'", "'"+government.getFleetTurn()+"'", "'"+government.getPlanetName()+"'", "'"+government.getPlanetTurn()+"'", "'"+government.getOwner()+"'", "'"+government.getTurn()+"'").replaceAll("'null'", "NULL"));
			}
			else
			{
				if (!exist) db.getDB().exec(String.format("INSERT INTO Government (owner, turn, fleetName, fleetTurn, planetName, planetTurn) VALUES (%s, %s, %s, %s, %s, %s);", "'"+government.getOwner()+"'", "'"+government.getTurn()+"'", "'"+government.getFleetName()+"'", "'"+government.getFleetTurn()+"'", "'"+government.getPlanetName()+"'", "'"+government.getPlanetTurn()+"'").replaceAll("'null'", "NULL"));
			}
		}
		catch(Exception e)
		{
			throw new SQLDataBaseException(e);
		}
	}
}
