package org.axan.sep.common.db.orm;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.axan.eplib.orm.DataBaseORMGenerator;
import org.axan.eplib.orm.ISQLDataBaseStatement;
import org.axan.eplib.orm.ISQLDataBaseStatementJob;
import org.axan.eplib.orm.SQLDataBaseException;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.IArea;
import org.axan.sep.common.db.IAssignedFleet;
import org.axan.sep.common.db.ICelestialBody;
import org.axan.sep.common.db.IProbe;
import org.axan.sep.common.db.IProductiveCelestialBody;
import org.axan.sep.common.db.IUnit;
import org.axan.sep.common.db.SEPCommonDB;
import org.axan.sep.common.db.orm.base.BaseArea;
import org.axan.sep.common.db.orm.base.IBaseArea;

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

	@Override
	public Location getLocation()
	{
		return location;
	}

	@Override
	public boolean isSun()
	{
		return baseAreaProxy.getIsSun();
	}
	
	@Override
	public boolean isVisible(SEPCommonDB db, String playerName) throws SQLDataBaseException
	{
		// TODO: Pulsar effect is not implemented yet
		
		// If player own a (productive) celestial body in this area
		if (ProductiveCelestialBody.selectOne(db, IProductiveCelestialBody.class, null, "owner = ? AND location_x = ? AND location_y = ? AND location_z = ?", playerName, getLocation().x, getLocation().y, getLocation().z) != null) return true;
		// If player has assigned fleet in this area
		if (AssignedFleet.selectOne(db, IAssignedFleet.class, null, "owner = ?", playerName) != null) return true;
		// If player has stopped fleet in this area
		if (Unit.selectOne(db, IUnit.class, null, "Unit.owner = ? AND NOT ("+Unit.getSQLMovingConditions()+") AND (departure_x = ? AND departure_y = ? AND departure_z = ?)", playerName, getLocation().x, getLocation().y, getLocation().z) != null) return true;
		// If player has deployed probe to observe this area
		if (Probe.selectOne(db, IProbe.class, null, "Probe.owner = ? AND ("+Probe.getSQLDeployedConditions()+") AND ("+SQLHelper.getDistanceCondition(2, "departure", "<=")+")", playerName, getLocation().x, getLocation().y, getLocation().z, db.getConfig().getUnitTypeSight(eUnitType.Probe)) != null) return true;
		
		return false;
	}
	
	@Override
	public ICelestialBody getCelestialBody(SEPCommonDB db) throws SQLDataBaseException
	{
		return CelestialBody.selectOne(db, ICelestialBody.class, null, "location_x = ? AND location_y = ? AND location_z = ?", getLocation().x, getLocation().y, getLocation().z);
	}
	
	@Override
	public <T extends IUnit> Set<T> getUnits(SEPCommonDB db, Class<T> expectedType) throws SQLDataBaseException
	{
		return Unit.select(db, expectedType, null, "departure_x = ? AND departure_y = ? AND departure_z = ?", getLocation().x, getLocation().y, getLocation().z);
	}

	public String toString(SEPCommonDB db, String playerName)
	{
		StringBuffer sb = new StringBuffer();
		
		if (isVisible(db, playerName))
		{
			sb.append("currently observed");
		}
		else
		{
			int lastObservation = -1;
			SEPCommonDB pDB = db;
			while(pDB.hasPrevious())
			{
				pDB = pDB.previous();
				if (isVisible(pDB, playerName))
				{
					lastObservation = pDB.getConfig().getTurn();
					break;
				}
			}
			
			sb.append((lastObservation < 0)?"never been observed":"last observation on turn "+lastObservation);
		}
		sb.append("\n");
		
		if (isSun())
		{
			sb.append("Sun\n");
		}
		else
		{
			ICelestialBody cb = getCelestialBody(db);
			if (cb != null)
			{
				// SUIS LA
				sb.append(cb.toString(db, playerName)+"\n");
			}
		}
		
		Set<IUnit> units = getUnits(db, IUnit.class);
		if (units != null && !units.isEmpty())
		{
			sb.append("Units :\n");
			for(IUnit u : units)
			{
				sb.append("   ["+u.getOwner()+"] "+u.getName()+"\n");
			}
		}
		
		/*
		if (markers != null && !markers.isEmpty())
		{
			sb.append("Markers :\n");
			for(IMarker m : markers)
			{
				sb.append(m);
			}
		}
		*/
		
		return sb.toString();
	}
	
	////////// Static methods
	
	public static <T extends IArea> T selectOne(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
	{
		Set<T> results = select(db, expectedType, from, (where==null?"(1) ":"("+where+") ")+"LIMIT 1", params);
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
			}, params);
			while(stmnt.step())
			{
				results.add(DataBaseORMGenerator.mapTo(expectedType, stmnt));
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
						return stmnt.step() && stmnt.columnValue(0) != null;
					}
					finally
					{
						if (stmnt != null) stmnt.dispose();
					}
				}
			}, params);
		}
		catch(Exception e)
		{
			throw new SQLDataBaseException(e);
		}
	}


	private static <T extends IArea> String selectQuery(Class<T> expectedType, String from, String where, Object ... params)
	{
		where = (where == null) ? null : (params == null) ? where : String.format(Locale.UK, where, params);
		if (where != null && !where.isEmpty() && where.charAt(0) != '(') where = "("+where+")";
		return String.format("SELECT Area.* FROM Area%s%s", (from != null && !from.isEmpty()) ? ", "+from : "", (where != null && !where.isEmpty()) ? " WHERE "+where : "");
	}

	public static <T extends IArea> void insertOrUpdate(SEPCommonDB db, T area) throws SQLDataBaseException
	{
		try
		{
			boolean exist = exist(db, area.getClass(), null, " Area.location_x = %s AND Area.location_y = %s AND Area.location_z = %s", area.getLocation() == null ? "NULL" : "'"+area.getLocation().x+"'", area.getLocation() == null ? "NULL" : "'"+area.getLocation().y+"'", area.getLocation() == null ? "NULL" : "'"+area.getLocation().z+"'");
			if (exist)
			{
				db.getDB().exec(String.format("UPDATE Area SET isSun = %s WHERE  Area.location_x = %s AND Area.location_y = %s AND Area.location_z = %s ;", "'"+area.isSun()+"'", area.getLocation() == null ? "NULL" : "'"+area.getLocation().x+"'", area.getLocation() == null ? "NULL" : "'"+area.getLocation().y+"'", area.getLocation() == null ? "NULL" : "'"+area.getLocation().z+"'").replaceAll("'null'", "NULL"));
			}
			else
			{
				if (!exist) db.getDB().exec(String.format("INSERT INTO Area (location_x, location_y, location_z, isSun) VALUES (%s, %s, %s, %s);", area.getLocation() == null ? "NULL" : "'"+area.getLocation().x+"'", area.getLocation() == null ? "NULL" : "'"+area.getLocation().y+"'", area.getLocation() == null ? "NULL" : "'"+area.getLocation().z+"'", "'"+area.isSun()+"'").replaceAll("'null'", "NULL"));
			}
		}
		catch(Exception e)
		{
			throw new SQLDataBaseException(e);
		}
	}
}
