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
import org.axan.sep.common.db.IAntiProbeMissile;
import org.axan.sep.common.db.ICarbonCarrier;
import org.axan.sep.common.db.IFleet;
import org.axan.sep.common.db.IGameConfig;
import org.axan.sep.common.db.IProbe;
import org.axan.sep.common.db.IPulsarMissile;
import org.axan.sep.common.db.ISpaceRoadDeliverer;
import org.axan.sep.common.db.IUnit;
import org.axan.sep.common.db.SEPCommonDB;
import org.axan.sep.common.db.orm.base.BaseUnit;
import org.axan.sep.common.db.orm.base.IBaseUnit;

public class Unit implements IUnit
{
	private final IBaseUnit baseUnitProxy;
	private final eUnitType type;
	private final Location departure;
	private final Location destination;
	private final float sight;

	Unit(IBaseUnit baseUnitProxy, IGameConfig config)
	{
		this.baseUnitProxy = baseUnitProxy;
		this.type = eUnitType.valueOf(baseUnitProxy.getType());
		this.departure = (baseUnitProxy.getDeparture_x() == null ? null : new Location(baseUnitProxy.getDeparture_x(), baseUnitProxy.getDeparture_y(), baseUnitProxy.getDeparture_z()));
		this.destination = (baseUnitProxy.getDestination_x() == null ? null : new Location(baseUnitProxy.getDestination_x(), baseUnitProxy.getDestination_y(), baseUnitProxy.getDestination_z()));		
		this.sight = config.getUnitTypeSight(type);
	}

	public Unit(String owner, String name, eUnitType type, Location departure, Double progress, Location destination, IGameConfig config)
	{
		this(new BaseUnit(owner, name, type.toString(), departure == null ? null : departure.x, departure == null ? null : departure.y, departure == null ? null : departure.z, progress, destination == null ? null : destination.x, destination == null ? null : destination.y, destination == null ? null : destination.z), config);
	}

	public Unit(ISQLDataBaseStatement stmnt, IGameConfig config) throws Exception
	{
		this(new BaseUnit(stmnt), config);
	}
	
	@Override
	public float getSight()
	{
		return sight;
	}

	@Override
	public String getOwner()
	{
		return baseUnitProxy.getOwner();
	}

	@Override
	public String getName()
	{
		return baseUnitProxy.getName();
	}

	@Override
	public eUnitType getType()
	{
		return type;
	}

	@Override
	public Location getDeparture()
	{
		return departure;
	}

	@Override
	public Double getProgress()
	{
		return baseUnitProxy.getProgress();
	}

	@Override
	public Location getDestination()
	{
		return destination;
	}

	public static <T extends IUnit> T selectOne(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
	{
		Set<T> results = select(db, expectedType, from, (where==null?"":where+" ")+"LIMIT 1", params);
		if (results.isEmpty()) return null;
		return results.iterator().next();
	}

	public static <T extends IUnit> Set<T> select(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
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
				results.add(DataBaseORMGenerator.mapTo(expectedType.isInterface() ? (Class<T>) Unit.class : expectedType, stmnt, db.getConfig()));
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

	public static <T extends IUnit> boolean exist(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
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


	private static <T extends IUnit> String selectQuery(Class<T> expectedType, String from, String where, Object ... params)
	{
		where = (where == null) ? null : (params == null) ? where : String.format(Locale.UK, where, params);
		if (where != null) where = String.format("(%s)",where);
		String typeFilter = null;
		if (expectedType != null)
		{
			String type = expectedType.isInterface() ? expectedType.getSimpleName().substring(1) : expectedType.getSimpleName();
			typeFilter = String.format("%s.type IS NOT NULL", type);
		}
		if (typeFilter != null && !typeFilter.isEmpty()) where = (where == null) ? typeFilter : String.format("%s AND %s", where, typeFilter);
		return String.format("SELECT Unit.*, PulsarMissile.*, Probe.*, CarbonCarrier.*, AntiProbeMissile.*, SpaceRoadDeliverer.*, Fleet.* FROM Unit%s LEFT JOIN PulsarMissile USING (owner, name, type) LEFT JOIN Probe USING (owner, name, type) LEFT JOIN CarbonCarrier USING (owner, name, type) LEFT JOIN AntiProbeMissile USING (owner, name, type) LEFT JOIN SpaceRoadDeliverer USING (owner, name, type) LEFT JOIN Fleet USING (owner, name, type)%s", (from != null && !from.isEmpty()) ? ", "+from : "", (where != null && !where.isEmpty()) ? " WHERE "+where : "");
	}

	public static <T extends IUnit> void insertOrUpdate(SEPCommonDB db, T unit) throws SQLDataBaseException
	{
		try
		{
			boolean exist = exist(db, unit.getClass(), null, " Unit.owner = %s AND Unit.name = %s", "'"+unit.getOwner()+"'", "'"+unit.getName()+"'");
			if (exist)
			{
				db.getDB().exec(String.format("UPDATE Unit SET type = %s,  departure_x = %s,  departure_y = %s,  departure_z = %s,  progress = %s,  destination_x = %s,  destination_y = %s,  destination_z = %s WHERE  Unit.owner = %s AND Unit.name = %s ;", "'"+unit.getType()+"'", unit.getDeparture() == null ? "NULL" : "'"+unit.getDeparture().x+"'", unit.getDeparture() == null ? "NULL" : "'"+unit.getDeparture().y+"'", unit.getDeparture() == null ? "NULL" : "'"+unit.getDeparture().z+"'", "'"+unit.getProgress()+"'", unit.getDestination() == null ? "NULL" : "'"+unit.getDestination().x+"'", unit.getDestination() == null ? "NULL" : "'"+unit.getDestination().y+"'", unit.getDestination() == null ? "NULL" : "'"+unit.getDestination().z+"'", "'"+unit.getOwner()+"'", "'"+unit.getName()+"'").replaceAll("'null'", "NULL"));
				switch(unit.getType())
				{
					case PulsarMissile:
					{
						IPulsarMissile pulsarMissile = IPulsarMissile.class.cast(unit);
						db.getDB().exec(String.format("UPDATE PulsarMissile SET type = %s,  time = %s,  volume = %s,  direction_x = %s,  direction_y = %s,  direction_z = %s WHERE  PulsarMissile.owner = %s AND PulsarMissile.name = %s ;", "'"+pulsarMissile.getType()+"'", "'"+pulsarMissile.getTime()+"'", "'"+pulsarMissile.getVolume()+"'", pulsarMissile.getDirection() == null ? "NULL" : "'"+pulsarMissile.getDirection().x+"'", pulsarMissile.getDirection() == null ? "NULL" : "'"+pulsarMissile.getDirection().y+"'", pulsarMissile.getDirection() == null ? "NULL" : "'"+pulsarMissile.getDirection().z+"'", "'"+pulsarMissile.getOwner()+"'", "'"+pulsarMissile.getName()+"'").replaceAll("'null'", "NULL"));
						break;
					}
					case Probe:
					{
						IProbe probe = IProbe.class.cast(unit);
						db.getDB().exec(String.format("UPDATE Probe SET type = %s WHERE  Probe.owner = %s AND Probe.name = %s ;", "'"+probe.getType()+"'", "'"+probe.getOwner()+"'", "'"+probe.getName()+"'").replaceAll("'null'", "NULL"));
						break;
					}
					case AntiProbeMissile:
					{
						IAntiProbeMissile antiProbeMissile = IAntiProbeMissile.class.cast(unit);
						db.getDB().exec(String.format("UPDATE AntiProbeMissile SET type = %s,  targetOwner = %s,  targetName = %s,  targetTurn = %s WHERE  AntiProbeMissile.owner = %s AND AntiProbeMissile.name = %s ;", "'"+antiProbeMissile.getType()+"'", "'"+antiProbeMissile.getTargetOwner()+"'", "'"+antiProbeMissile.getTargetName()+"'", "'"+antiProbeMissile.getTargetTurn()+"'", "'"+antiProbeMissile.getOwner()+"'", "'"+antiProbeMissile.getName()+"'").replaceAll("'null'", "NULL"));
						break;
					}
					case Fleet:
					{
						IFleet fleet = IFleet.class.cast(unit);
						db.getDB().exec(String.format("UPDATE Fleet SET type = %s WHERE  Fleet.owner = %s AND Fleet.name = %s ;", "'"+fleet.getType()+"'", "'"+fleet.getOwner()+"'", "'"+fleet.getName()+"'").replaceAll("'null'", "NULL"));
						break;
					}
					case CarbonCarrier:
					{
						ICarbonCarrier carbonCarrier = ICarbonCarrier.class.cast(unit);
						db.getDB().exec(String.format("UPDATE CarbonCarrier SET type = %s,  sourceType = %s,  sourceCelestialBodyName = %s,  sourceTurn = %s,  orderOwner = %s,  orderSource = %s,  orderPriority = %s WHERE  CarbonCarrier.owner = %s AND CarbonCarrier.name = %s ;", "'"+carbonCarrier.getType()+"'", "'"+carbonCarrier.getSourceType()+"'", "'"+carbonCarrier.getSourceCelestialBodyName()+"'", "'"+carbonCarrier.getSourceTurn()+"'", "'"+carbonCarrier.getOrderOwner()+"'", "'"+carbonCarrier.getOrderSource()+"'", "'"+carbonCarrier.getOrderPriority()+"'", "'"+carbonCarrier.getOwner()+"'", "'"+carbonCarrier.getName()+"'").replaceAll("'null'", "NULL"));
						break;
					}
					case SpaceRoadDeliverer:
					{
						ISpaceRoadDeliverer spaceRoadDeliverer = ISpaceRoadDeliverer.class.cast(unit);
						db.getDB().exec(String.format("UPDATE SpaceRoadDeliverer SET type = %s,  sourceType = %s,  sourceCelestialBodyName = %s,  sourceTurn = %s,  destinationType = %s,  destinationCelestialBodyName = %s,  destinationTurn = %s WHERE  SpaceRoadDeliverer.owner = %s AND SpaceRoadDeliverer.name = %s ;", "'"+spaceRoadDeliverer.getType()+"'", "'"+spaceRoadDeliverer.getSourceType()+"'", "'"+spaceRoadDeliverer.getSourceCelestialBodyName()+"'", "'"+spaceRoadDeliverer.getSourceTurn()+"'", "'"+spaceRoadDeliverer.getDestinationType()+"'", "'"+spaceRoadDeliverer.getDestinationCelestialBodyName()+"'", "'"+spaceRoadDeliverer.getDestinationTurn()+"'", "'"+spaceRoadDeliverer.getOwner()+"'", "'"+spaceRoadDeliverer.getName()+"'").replaceAll("'null'", "NULL"));
						break;
					}
				}
			}
			else
			{
				if (!exist) db.getDB().exec(String.format("INSERT INTO Unit (owner, name, type, departure_x, departure_y, departure_z, progress, destination_x, destination_y, destination_z) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s);", "'"+unit.getOwner()+"'", "'"+unit.getName()+"'", "'"+unit.getType()+"'", unit.getDeparture() == null ? "NULL" : "'"+unit.getDeparture().x+"'", unit.getDeparture() == null ? "NULL" : "'"+unit.getDeparture().y+"'", unit.getDeparture() == null ? "NULL" : "'"+unit.getDeparture().z+"'", "'"+unit.getProgress()+"'", unit.getDestination() == null ? "NULL" : "'"+unit.getDestination().x+"'", unit.getDestination() == null ? "NULL" : "'"+unit.getDestination().y+"'", unit.getDestination() == null ? "NULL" : "'"+unit.getDestination().z+"'").replaceAll("'null'", "NULL"));
				switch(unit.getType())
				{
					case PulsarMissile:
					{
						IPulsarMissile pulsarMissile = IPulsarMissile.class.cast(unit);
						if (!exist) db.getDB().exec(String.format("INSERT INTO PulsarMissile (owner, name, type, time, volume, direction_x, direction_y, direction_z) VALUES (%s, %s, %s, %s, %s, %s, %s, %s);", "'"+pulsarMissile.getOwner()+"'", "'"+pulsarMissile.getName()+"'", "'"+pulsarMissile.getType()+"'", "'"+pulsarMissile.getTime()+"'", "'"+pulsarMissile.getVolume()+"'", pulsarMissile.getDirection() == null ? "NULL" : "'"+pulsarMissile.getDirection().x+"'", pulsarMissile.getDirection() == null ? "NULL" : "'"+pulsarMissile.getDirection().y+"'", pulsarMissile.getDirection() == null ? "NULL" : "'"+pulsarMissile.getDirection().z+"'").replaceAll("'null'", "NULL"));
						break;
					}
					case Probe:
					{
						IProbe probe = IProbe.class.cast(unit);
						if (!exist) db.getDB().exec(String.format("INSERT INTO Probe (owner, name, type) VALUES (%s, %s, %s);", "'"+probe.getOwner()+"'", "'"+probe.getName()+"'", "'"+probe.getType()+"'").replaceAll("'null'", "NULL"));
						break;
					}
					case AntiProbeMissile:
					{
						IAntiProbeMissile antiProbeMissile = IAntiProbeMissile.class.cast(unit);
						if (!exist) db.getDB().exec(String.format("INSERT INTO AntiProbeMissile (owner, name, type, targetOwner, targetName, targetTurn) VALUES (%s, %s, %s, %s, %s, %s);", "'"+antiProbeMissile.getOwner()+"'", "'"+antiProbeMissile.getName()+"'", "'"+antiProbeMissile.getType()+"'", "'"+antiProbeMissile.getTargetOwner()+"'", "'"+antiProbeMissile.getTargetName()+"'", "'"+antiProbeMissile.getTargetTurn()+"'").replaceAll("'null'", "NULL"));
						break;
					}
					case Fleet:
					{
						IFleet fleet = IFleet.class.cast(unit);
						if (!exist) db.getDB().exec(String.format("INSERT INTO Fleet (owner, name, type) VALUES (%s, %s, %s);", "'"+fleet.getOwner()+"'", "'"+fleet.getName()+"'", "'"+fleet.getType()+"'").replaceAll("'null'", "NULL"));
						break;
					}
					case CarbonCarrier:
					{
						ICarbonCarrier carbonCarrier = ICarbonCarrier.class.cast(unit);
						if (!exist) db.getDB().exec(String.format("INSERT INTO CarbonCarrier (owner, name, type, sourceType, sourceCelestialBodyName, sourceTurn, orderOwner, orderSource, orderPriority) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s);", "'"+carbonCarrier.getOwner()+"'", "'"+carbonCarrier.getName()+"'", "'"+carbonCarrier.getType()+"'", "'"+carbonCarrier.getSourceType()+"'", "'"+carbonCarrier.getSourceCelestialBodyName()+"'", "'"+carbonCarrier.getSourceTurn()+"'", "'"+carbonCarrier.getOrderOwner()+"'", "'"+carbonCarrier.getOrderSource()+"'", "'"+carbonCarrier.getOrderPriority()+"'").replaceAll("'null'", "NULL"));
						break;
					}
					case SpaceRoadDeliverer:
					{
						ISpaceRoadDeliverer spaceRoadDeliverer = ISpaceRoadDeliverer.class.cast(unit);
						if (!exist) db.getDB().exec(String.format("INSERT INTO SpaceRoadDeliverer (owner, name, type, sourceType, sourceCelestialBodyName, sourceTurn, destinationType, destinationCelestialBodyName, destinationTurn) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s);", "'"+spaceRoadDeliverer.getOwner()+"'", "'"+spaceRoadDeliverer.getName()+"'", "'"+spaceRoadDeliverer.getType()+"'", "'"+spaceRoadDeliverer.getSourceType()+"'", "'"+spaceRoadDeliverer.getSourceCelestialBodyName()+"'", "'"+spaceRoadDeliverer.getSourceTurn()+"'", "'"+spaceRoadDeliverer.getDestinationType()+"'", "'"+spaceRoadDeliverer.getDestinationCelestialBodyName()+"'", "'"+spaceRoadDeliverer.getDestinationTurn()+"'").replaceAll("'null'", "NULL"));
						break;
					}
				}
			}
		}
		catch(Exception e)
		{
			throw new SQLDataBaseException(e);
		}
	}
}
