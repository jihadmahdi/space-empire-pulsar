package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.base.BaseGovernment;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import org.axan.sep.common.IGameConfig;
import java.util.Set;
import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;
import java.util.HashSet;
import com.almworks.sqlite4java.SQLiteStatement;
import com.almworks.sqlite4java.SQLiteConnection;

public class Government implements IGovernment
{
	private final BaseGovernment baseGovernmentProxy;

	public Government(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		this.baseGovernmentProxy = new BaseGovernment(stmnt);
	}

	public String getFleetName()
	{
		return baseGovernmentProxy.getFleetName();
	}

	public Integer getFleetTurn()
	{
		return baseGovernmentProxy.getFleetTurn();
	}

	public Integer getPlanetTurn()
	{
		return baseGovernmentProxy.getPlanetTurn();
	}

	public String getOwner()
	{
		return baseGovernmentProxy.getOwner();
	}

	public Integer getTurn()
	{
		return baseGovernmentProxy.getTurn();
	}

	public String getPlanetName()
	{
		return baseGovernmentProxy.getPlanetName();
	}

	public static <T extends IGovernment> Set<T> select(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, boolean lastVersion, String where, Object ... params) throws SQLiteDBException
	{
		try
		{
			Set<T> results = new HashSet<T>();

			if (where != null && params != null) where = String.format(where, params);
			if (lastVersion)
			{
				where = String.format("%s(VersionedGovernment.turn = ( SELECT MAX(LVVersionedGovernment.turn) FROM VersionedGovernment LVVersionedGovernment WHERE LVVersionedGovernment.fleetName = Government.fleetName AND LVVersionedGovernment.fleetTurn = Government.fleetTurn AND LVVersionedGovernment.planetTurn = Government.planetTurn AND LVVersionedGovernment.owner = Government.owner AND LVVersionedGovernment.turn = Government.turn AND LVVersionedGovernment.planetName = Government.planetName ))", (where != null && !where.isEmpty()) ? "("+where+") AND " : "");
			}
			SQLiteStatement stmnt = conn.prepare(String.format("SELECT * FROM Government%s ;", (where != null && !where.isEmpty()) ? " WHERE "+where : ""));
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
