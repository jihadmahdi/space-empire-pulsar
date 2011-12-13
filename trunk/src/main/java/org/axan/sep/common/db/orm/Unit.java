package org.axan.sep.common.db.orm;

import java.lang.Exception;
import org.axan.sep.common.db.orm.base.IBaseUnit;
import org.axan.sep.common.db.orm.base.BaseUnit;
import org.axan.sep.common.db.IUnit;
import java.util.HashMap;
import java.util.Map;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.IGameConfig;
import org.neo4j.graphdb.Node;

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

	public Unit(Node stmnt, IGameConfig config) throws Exception
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
	public double getProgress()
	{
		return baseUnitProxy.getProgress();
	}

	@Override
	public Location getDestination()
	{
		return destination;
	}

	@Override
	public Map<String, Object> getNode()
	{
		return baseUnitProxy.getNode();
	}
	
	@Override
	public boolean isMoving()
	{
		if (getProgress() < 0 || getDestination() == null || getDeparture() == null) return false;
		return (getProgress() != 0 && getProgress() != 1);
	}		
	
	/**
	 * 1$: table.
	 */
	private static final String SQL_MOVING_CONDITIONS = "(%1$sprogress > 0) AND (%1$sdestination_x IS NOT NULL) AND (%1$sdeparture_x IS NOT NULL) AND (%1$sprogress != 1)";
	
	public static String getSQLMovingConditions()
	{
		return getSQLMovingConditions(null);
	}
	public static String getSQLMovingConditions(String table)
	{
		return String.format(SQL_MOVING_CONDITIONS, table == null || table.isEmpty() ? "" : table+".");
	}

}
