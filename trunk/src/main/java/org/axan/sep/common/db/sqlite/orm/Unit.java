package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.base.BaseUnit;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteStatement;
import java.util.HashSet;
import java.util.Set;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;
import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.Protocol.eUnitType;

public class Unit implements IUnit
{
	private final BaseUnit baseUnitProxy;
	private final eUnitType type;
	private final float sight;

	public Unit(String owner, String name, eUnitType type, float sight)
	{
		baseUnitProxy = new BaseUnit(owner, name, type.toString());
		this.type = type;
		this.sight = sight; 
	}

	public Unit(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		this.baseUnitProxy = new BaseUnit(stmnt);
		this.type = eUnitType.valueOf(baseUnitProxy.getType());
		this.sight = config.getUnitTypeSight(type);
	}
	
	@Override
	public float getSight()
	{
		return sight;
	}

	public String getOwner()
	{
		return baseUnitProxy.getOwner();
	}

	public String getName()
	{
		return baseUnitProxy.getName();
	}

	public eUnitType getType()
	{
		return type;
	}

	public static <T extends IUnit> Set<T> select(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, boolean lastVersion, String from, String where, Object ... params) throws SQLiteDBException
	{
		try
		{
			Set<T> results = new HashSet<T>();

			if (where != null && params != null) where = String.format(where, params);
			if (lastVersion)
			{
				where = String.format("%s(VersionedUnit.turn = ( SELECT MAX(LVVersionedUnit.turn) FROM VersionedUnit LVVersionedUnit WHERE LVVersionedUnit.owner = Unit.owner AND LVVersionedUnit.name = Unit.name AND LVVersionedUnit.type = Unit.type ))", (where != null && !where.isEmpty()) ? "("+where+") AND " : "");
			}
			SQLiteStatement stmnt = conn.prepare(String.format("SELECT Unit.type, VersionedUnit.type, Unit.*, VersionedUnit.*, VersionedFleet.*, VersionedProbe.*, VersionedCarbonCarrier.*, VersionedAntiProbeMissile.*, VersionedSpaceRoadDeliverer.*, VersionedPulsarMissile.*, PulsarMissile.*, Probe.*, CarbonCarrier.*, AntiProbeMissile.*, SpaceRoadDeliverer.*, Fleet.* FROM Unit%s LEFT JOIN VersionedUnit USING (owner, name, type) LEFT JOIN VersionedFleet USING (owner, name, turn, type) LEFT JOIN VersionedProbe USING (owner, name, turn, type) LEFT JOIN VersionedCarbonCarrier USING (owner, name, turn, type) LEFT JOIN VersionedAntiProbeMissile USING (owner, name, turn, type) LEFT JOIN VersionedSpaceRoadDeliverer USING (owner, name, turn, type) LEFT JOIN VersionedPulsarMissile USING (owner, name, turn, type) LEFT JOIN PulsarMissile USING (owner, name, type) LEFT JOIN Probe USING (owner, name, type) LEFT JOIN CarbonCarrier USING (owner, name, type) LEFT JOIN AntiProbeMissile USING (owner, name, type) LEFT JOIN SpaceRoadDeliverer USING (owner, name, type) LEFT JOIN Fleet USING (owner, name, type)%s ;", (from != null && !from.isEmpty()) ? ", "+from : "", (where != null && !where.isEmpty()) ? " WHERE "+where : ""));
			while(stmnt.step())
			{
				eUnitType type = eUnitType.valueOf(stmnt.columnString(0));
				boolean isVersioned = (!stmnt.columnString(1).isEmpty());
				Class<? extends IUnit> clazz = (Class<? extends IUnit>)  Class.forName(String.format("%s.%s%s", Unit.class.getPackage().getName(), isVersioned ? "Versioned" : "", type.toString()));
				IUnit o = SQLiteORMGenerator.mapTo(clazz, stmnt, config);
				if (expectedType.isInstance(o))
				{
					results.add(expectedType.cast(o));
				}
			}
			return results;
		}
		catch(Exception e)
		{
			throw new SQLiteDBException(e);
		}
	}


	public static <T extends IUnit> void insertOrUpdate(SQLiteConnection conn, T unit) throws SQLiteDBException
	{
		try
		{
			IVersionedUnit vunit = (IVersionedUnit.class.isInstance(unit) ? IVersionedUnit.class.cast(unit) : null);
			SQLiteStatement stmnt = conn.prepare(String.format("SELECT EXISTS ( SELECT owner FROM Unit WHERE owner = %s AND name = %s) AS exist ;", "'"+unit.getOwner()+"'", "'"+unit.getName()+"'"));
			stmnt.step();
			if (stmnt.columnInt(0) == 0)
			{
				conn.exec(String.format("INSERT INTO Unit (owner, name, type) VALUES (%s, %s, %s);", "'"+unit.getOwner()+"'", "'"+unit.getName()+"'", "'"+unit.getType()+"'"));
				if (vunit != null)
				{
					conn.exec(String.format("INSERT INTO VersionedUnit (owner, name, turn, type, departure_x, departure_y, departure_z, progress, destination_x, destination_y, destination_z) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s);", "'"+vunit.getOwner()+"'", "'"+vunit.getName()+"'", "'"+vunit.getTurn()+"'", "'"+vunit.getType()+"'", vunit.getDeparture() == null ? "NULL" : "'"+vunit.getDeparture().x+"'", vunit.getDeparture() == null ? "NULL" : "'"+vunit.getDeparture().y+"'", vunit.getDeparture() == null ? "NULL" : "'"+vunit.getDeparture().z+"'", "'"+vunit.getProgress()+"'", vunit.getDestination() == null ? "NULL" : "'"+vunit.getDestination().x+"'", vunit.getDestination() == null ? "NULL" : "'"+vunit.getDestination().y+"'", vunit.getDestination() == null ? "NULL" : "'"+vunit.getDestination().z+"'"));
				}
				switch(unit.getType())
				{
					case PulsarMissile:
					{
						IPulsarMissile pulsarMissile = IPulsarMissile.class.cast(unit);
						conn.exec(String.format("INSERT INTO PulsarMissile (owner, name, type, time, volume) VALUES (%s, %s, %s, %s, %s);", "'"+pulsarMissile.getOwner()+"'", "'"+pulsarMissile.getName()+"'", "'"+pulsarMissile.getType()+"'", "'"+pulsarMissile.getTime()+"'", "'"+pulsarMissile.getVolume()+"'"));
						if (vunit != null)
						{
							IVersionedPulsarMissile versionedPulsarMissile = IVersionedPulsarMissile.class.cast(unit);
							conn.exec(String.format("INSERT INTO VersionedPulsarMissile (owner, name, turn, type, direction_x, direction_y, direction_z) VALUES (%s, %s, %s, %s, %s, %s, %s);", "'"+versionedPulsarMissile.getOwner()+"'", "'"+versionedPulsarMissile.getName()+"'", "'"+versionedPulsarMissile.getTurn()+"'", "'"+versionedPulsarMissile.getType()+"'", versionedPulsarMissile.getDirection() == null ? "NULL" : "'"+versionedPulsarMissile.getDirection().x+"'", versionedPulsarMissile.getDirection() == null ? "NULL" : "'"+versionedPulsarMissile.getDirection().y+"'", versionedPulsarMissile.getDirection() == null ? "NULL" : "'"+versionedPulsarMissile.getDirection().z+"'"));
						}
						break;
					}
					case Probe:
					{
						IProbe probe = IProbe.class.cast(unit);
						conn.exec(String.format("INSERT INTO Probe (owner, name, type) VALUES (%s, %s, %s);", "'"+probe.getOwner()+"'", "'"+probe.getName()+"'", "'"+probe.getType()+"'"));
						if (vunit != null)
						{
							IVersionedProbe versionedProbe = IVersionedProbe.class.cast(unit);
							conn.exec(String.format("INSERT INTO VersionedProbe (owner, name, turn, type) VALUES (%s, %s, %s, %s);", "'"+versionedProbe.getOwner()+"'", "'"+versionedProbe.getName()+"'", "'"+versionedProbe.getTurn()+"'", "'"+versionedProbe.getType()+"'"));
						}
						break;
					}
					case AntiProbeMissile:
					{
						IAntiProbeMissile antiProbeMissile = IAntiProbeMissile.class.cast(unit);
						conn.exec(String.format("INSERT INTO AntiProbeMissile (owner, name, type) VALUES (%s, %s, %s);", "'"+antiProbeMissile.getOwner()+"'", "'"+antiProbeMissile.getName()+"'", "'"+antiProbeMissile.getType()+"'"));
						if (vunit != null)
						{
							IVersionedAntiProbeMissile versionedAntiProbeMissile = IVersionedAntiProbeMissile.class.cast(unit);
							conn.exec(String.format("INSERT INTO VersionedAntiProbeMissile (owner, name, turn, type, targetOwner, targetName, targetTurn) VALUES (%s, %s, %s, %s, %s, %s, %s);", "'"+versionedAntiProbeMissile.getOwner()+"'", "'"+versionedAntiProbeMissile.getName()+"'", "'"+versionedAntiProbeMissile.getTurn()+"'", "'"+versionedAntiProbeMissile.getType()+"'", "'"+versionedAntiProbeMissile.getTargetOwner()+"'", "'"+versionedAntiProbeMissile.getTargetName()+"'", "'"+versionedAntiProbeMissile.getTargetTurn()+"'"));
						}
						break;
					}
					case Fleet:
					{
						IFleet fleet = IFleet.class.cast(unit);
						conn.exec(String.format("INSERT INTO Fleet (owner, name, type) VALUES (%s, %s, %s);", "'"+fleet.getOwner()+"'", "'"+fleet.getName()+"'", "'"+fleet.getType()+"'"));
						if (vunit != null)
						{
							IVersionedFleet versionedFleet = IVersionedFleet.class.cast(unit);
							conn.exec(String.format("INSERT INTO VersionedFleet (owner, name, turn, type) VALUES (%s, %s, %s, %s);", "'"+versionedFleet.getOwner()+"'", "'"+versionedFleet.getName()+"'", "'"+versionedFleet.getTurn()+"'", "'"+versionedFleet.getType()+"'"));
						}
						break;
					}
					case CarbonCarrier:
					{
						ICarbonCarrier carbonCarrier = ICarbonCarrier.class.cast(unit);
						conn.exec(String.format("INSERT INTO CarbonCarrier (owner, name, type, sourceType, sourceCelestialBodyName, sourceTurn) VALUES (%s, %s, %s, %s, %s, %s);", "'"+carbonCarrier.getOwner()+"'", "'"+carbonCarrier.getName()+"'", "'"+carbonCarrier.getType()+"'", "'"+carbonCarrier.getSourceType()+"'", "'"+carbonCarrier.getSourceCelestialBodyName()+"'", "'"+carbonCarrier.getSourceTurn()+"'"));
						if (vunit != null)
						{
							IVersionedCarbonCarrier versionedCarbonCarrier = IVersionedCarbonCarrier.class.cast(unit);
							conn.exec(String.format("INSERT INTO VersionedCarbonCarrier (owner, name, turn, type, orderOwner, orderSource, orderPriority) VALUES (%s, %s, %s, %s, %s, %s, %s);", "'"+versionedCarbonCarrier.getOwner()+"'", "'"+versionedCarbonCarrier.getName()+"'", "'"+versionedCarbonCarrier.getTurn()+"'", "'"+versionedCarbonCarrier.getType()+"'", "'"+versionedCarbonCarrier.getOrderOwner()+"'", "'"+versionedCarbonCarrier.getOrderSource()+"'", "'"+versionedCarbonCarrier.getOrderPriority()+"'"));
						}
						break;
					}
					case SpaceRoadDeliverer:
					{
						ISpaceRoadDeliverer spaceRoadDeliverer = ISpaceRoadDeliverer.class.cast(unit);
						conn.exec(String.format("INSERT INTO SpaceRoadDeliverer (owner, name, type, sourceType, sourceCelestialBodyName, sourceTurn, destinationType, destinationCelestialBodyName, destinationTurn) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s);", "'"+spaceRoadDeliverer.getOwner()+"'", "'"+spaceRoadDeliverer.getName()+"'", "'"+spaceRoadDeliverer.getType()+"'", "'"+spaceRoadDeliverer.getSourceType()+"'", "'"+spaceRoadDeliverer.getSourceCelestialBodyName()+"'", "'"+spaceRoadDeliverer.getSourceTurn()+"'", "'"+spaceRoadDeliverer.getDestinationType()+"'", "'"+spaceRoadDeliverer.getDestinationCelestialBodyName()+"'", "'"+spaceRoadDeliverer.getDestinationTurn()+"'"));
						if (vunit != null)
						{
							IVersionedSpaceRoadDeliverer versionedSpaceRoadDeliverer = IVersionedSpaceRoadDeliverer.class.cast(unit);
							conn.exec(String.format("INSERT INTO VersionedSpaceRoadDeliverer (owner, name, turn, type) VALUES (%s, %s, %s, %s);", "'"+versionedSpaceRoadDeliverer.getOwner()+"'", "'"+versionedSpaceRoadDeliverer.getName()+"'", "'"+versionedSpaceRoadDeliverer.getTurn()+"'", "'"+versionedSpaceRoadDeliverer.getType()+"'"));
						}
						break;
					}
				}
			}
			else
			{
				conn.exec(String.format("UPDATE Unit SET  type = %s WHERE  owner = %s AND name = %s ;", "'"+unit.getType()+"'", "'"+unit.getOwner()+"'", "'"+unit.getName()+"'"));
				if (vunit != null)
				{
					conn.exec(String.format("UPDATE VersionedUnit SET  type = %s,  departure_x = %s,  departure_y = %s,  departure_z = %s,  progress = %s,  destination_x = %s,  destination_y = %s,  destination_z = %s WHERE  owner = %s AND name = %s AND turn = %s ;", "'"+vunit.getType()+"'", vunit.getDeparture() == null ? "NULL" : "'"+vunit.getDeparture().x+"'", vunit.getDeparture() == null ? "NULL" : "'"+vunit.getDeparture().y+"'", vunit.getDeparture() == null ? "NULL" : "'"+vunit.getDeparture().z+"'", "'"+vunit.getProgress()+"'", vunit.getDestination() == null ? "NULL" : "'"+vunit.getDestination().x+"'", vunit.getDestination() == null ? "NULL" : "'"+vunit.getDestination().y+"'", vunit.getDestination() == null ? "NULL" : "'"+vunit.getDestination().z+"'", "'"+vunit.getOwner()+"'", "'"+vunit.getName()+"'", "'"+vunit.getTurn()+"'"));
				}
				switch(unit.getType())
				{
					case PulsarMissile:
					{
						IPulsarMissile pulsarMissile = IPulsarMissile.class.cast(unit);
						conn.exec(String.format("UPDATE PulsarMissile SET  type = %s,  time = %s,  volume = %s WHERE  owner = %s AND name = %s ;", "'"+pulsarMissile.getType()+"'", "'"+pulsarMissile.getTime()+"'", "'"+pulsarMissile.getVolume()+"'", "'"+pulsarMissile.getOwner()+"'", "'"+pulsarMissile.getName()+"'"));
						if (vunit != null)
						{
							IVersionedPulsarMissile versionedPulsarMissile = IVersionedPulsarMissile.class.cast(unit);
							conn.exec(String.format("UPDATE VersionedPulsarMissile SET  type = %s,  direction_x = %s,  direction_y = %s,  direction_z = %s WHERE  owner = %s AND name = %s AND turn = %s ;", "'"+versionedPulsarMissile.getType()+"'", versionedPulsarMissile.getDirection() == null ? "NULL" : "'"+versionedPulsarMissile.getDirection().x+"'", versionedPulsarMissile.getDirection() == null ? "NULL" : "'"+versionedPulsarMissile.getDirection().y+"'", versionedPulsarMissile.getDirection() == null ? "NULL" : "'"+versionedPulsarMissile.getDirection().z+"'", "'"+versionedPulsarMissile.getOwner()+"'", "'"+versionedPulsarMissile.getName()+"'", "'"+versionedPulsarMissile.getTurn()+"'"));
						}
						break;
					}
					case Probe:
					{
						IProbe probe = IProbe.class.cast(unit);
						conn.exec(String.format("UPDATE Probe SET  type = %s WHERE  owner = %s AND name = %s ;", "'"+probe.getType()+"'", "'"+probe.getOwner()+"'", "'"+probe.getName()+"'"));
						if (vunit != null)
						{
							IVersionedProbe versionedProbe = IVersionedProbe.class.cast(unit);
							conn.exec(String.format("UPDATE VersionedProbe SET  type = %s WHERE  owner = %s AND name = %s AND turn = %s ;", "'"+versionedProbe.getType()+"'", "'"+versionedProbe.getOwner()+"'", "'"+versionedProbe.getName()+"'", "'"+versionedProbe.getTurn()+"'"));
						}
						break;
					}
					case AntiProbeMissile:
					{
						IAntiProbeMissile antiProbeMissile = IAntiProbeMissile.class.cast(unit);
						conn.exec(String.format("UPDATE AntiProbeMissile SET  type = %s WHERE  owner = %s AND name = %s ;", "'"+antiProbeMissile.getType()+"'", "'"+antiProbeMissile.getOwner()+"'", "'"+antiProbeMissile.getName()+"'"));
						if (vunit != null)
						{
							IVersionedAntiProbeMissile versionedAntiProbeMissile = IVersionedAntiProbeMissile.class.cast(unit);
							conn.exec(String.format("UPDATE VersionedAntiProbeMissile SET  type = %s,  targetOwner = %s,  targetName = %s,  targetTurn = %s WHERE  owner = %s AND name = %s AND turn = %s ;", "'"+versionedAntiProbeMissile.getType()+"'", "'"+versionedAntiProbeMissile.getTargetOwner()+"'", "'"+versionedAntiProbeMissile.getTargetName()+"'", "'"+versionedAntiProbeMissile.getTargetTurn()+"'", "'"+versionedAntiProbeMissile.getOwner()+"'", "'"+versionedAntiProbeMissile.getName()+"'", "'"+versionedAntiProbeMissile.getTurn()+"'"));
						}
						break;
					}
					case Fleet:
					{
						IFleet fleet = IFleet.class.cast(unit);
						conn.exec(String.format("UPDATE Fleet SET  type = %s WHERE  owner = %s AND name = %s ;", "'"+fleet.getType()+"'", "'"+fleet.getOwner()+"'", "'"+fleet.getName()+"'"));
						if (vunit != null)
						{
							IVersionedFleet versionedFleet = IVersionedFleet.class.cast(unit);
							conn.exec(String.format("UPDATE VersionedFleet SET  type = %s WHERE  owner = %s AND name = %s AND turn = %s ;", "'"+versionedFleet.getType()+"'", "'"+versionedFleet.getOwner()+"'", "'"+versionedFleet.getName()+"'", "'"+versionedFleet.getTurn()+"'"));
						}
						break;
					}
					case CarbonCarrier:
					{
						ICarbonCarrier carbonCarrier = ICarbonCarrier.class.cast(unit);
						conn.exec(String.format("UPDATE CarbonCarrier SET  type = %s,  sourceType = %s,  sourceCelestialBodyName = %s,  sourceTurn = %s WHERE  owner = %s AND name = %s ;", "'"+carbonCarrier.getType()+"'", "'"+carbonCarrier.getSourceType()+"'", "'"+carbonCarrier.getSourceCelestialBodyName()+"'", "'"+carbonCarrier.getSourceTurn()+"'", "'"+carbonCarrier.getOwner()+"'", "'"+carbonCarrier.getName()+"'"));
						if (vunit != null)
						{
							IVersionedCarbonCarrier versionedCarbonCarrier = IVersionedCarbonCarrier.class.cast(unit);
							conn.exec(String.format("UPDATE VersionedCarbonCarrier SET  type = %s,  orderOwner = %s,  orderSource = %s,  orderPriority = %s WHERE  owner = %s AND name = %s AND turn = %s ;", "'"+versionedCarbonCarrier.getType()+"'", "'"+versionedCarbonCarrier.getOrderOwner()+"'", "'"+versionedCarbonCarrier.getOrderSource()+"'", "'"+versionedCarbonCarrier.getOrderPriority()+"'", "'"+versionedCarbonCarrier.getOwner()+"'", "'"+versionedCarbonCarrier.getName()+"'", "'"+versionedCarbonCarrier.getTurn()+"'"));
						}
						break;
					}
					case SpaceRoadDeliverer:
					{
						ISpaceRoadDeliverer spaceRoadDeliverer = ISpaceRoadDeliverer.class.cast(unit);
						conn.exec(String.format("UPDATE SpaceRoadDeliverer SET  type = %s,  sourceType = %s,  sourceCelestialBodyName = %s,  sourceTurn = %s,  destinationType = %s,  destinationCelestialBodyName = %s,  destinationTurn = %s WHERE  owner = %s AND name = %s ;", "'"+spaceRoadDeliverer.getType()+"'", "'"+spaceRoadDeliverer.getSourceType()+"'", "'"+spaceRoadDeliverer.getSourceCelestialBodyName()+"'", "'"+spaceRoadDeliverer.getSourceTurn()+"'", "'"+spaceRoadDeliverer.getDestinationType()+"'", "'"+spaceRoadDeliverer.getDestinationCelestialBodyName()+"'", "'"+spaceRoadDeliverer.getDestinationTurn()+"'", "'"+spaceRoadDeliverer.getOwner()+"'", "'"+spaceRoadDeliverer.getName()+"'"));
						if (vunit != null)
						{
							IVersionedSpaceRoadDeliverer versionedSpaceRoadDeliverer = IVersionedSpaceRoadDeliverer.class.cast(unit);
							conn.exec(String.format("UPDATE VersionedSpaceRoadDeliverer SET  type = %s WHERE  owner = %s AND name = %s AND turn = %s ;", "'"+versionedSpaceRoadDeliverer.getType()+"'", "'"+versionedSpaceRoadDeliverer.getOwner()+"'", "'"+versionedSpaceRoadDeliverer.getName()+"'", "'"+versionedSpaceRoadDeliverer.getTurn()+"'"));
						}
						break;
					}
				}
			}
		}
		catch(Exception e)
		{
			throw new SQLiteDBException(e);
		}
	}
}
