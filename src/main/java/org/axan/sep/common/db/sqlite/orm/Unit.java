package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.base.BaseUnit;
import org.axan.sep.common.db.IUnit;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteStatement;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.db.IAntiProbeMissile;
import org.axan.sep.common.db.ICarbonCarrier;
import org.axan.sep.common.db.IFleet;
import org.axan.sep.common.db.IGameConfig;
import org.axan.sep.common.db.IProbe;
import org.axan.sep.common.db.IPulsarMissile;
import org.axan.sep.common.db.ISpaceRoadDeliverer;
import org.axan.sep.common.db.IVersionedAntiProbeMissile;
import org.axan.sep.common.db.IVersionedCarbonCarrier;
import org.axan.sep.common.db.IVersionedFleet;
import org.axan.sep.common.db.IVersionedProbe;
import org.axan.sep.common.db.IVersionedPulsarMissile;
import org.axan.sep.common.db.IVersionedSpaceRoadDeliverer;
import org.axan.sep.common.db.IVersionedUnit;

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

	/** Set maxVersion to null to select last version. */
	public static <T extends IUnit> Set<T> selectMaxVersion(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, Integer maxVersion, String from, String where, Object ... params) throws SQLiteDBException
	{
		return select(conn, config, expectedType, true, maxVersion, from, where, params);
	}

	public static <T extends IUnit> Set<T> selectVersion(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, int version, String from, String where, Object ... params) throws SQLiteDBException
	{
		return select(conn, config, expectedType, false, version, from, where, params);
	}

	public static <T extends IUnit> Set<T> selectUnversioned(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, String from, String where, Object ... params) throws SQLiteDBException
	{
		return select(conn, config, expectedType, false, null, from, where, params);
	}

	private static <T extends IUnit> Set<T> select(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, boolean maxVersion, Integer version, String from, String where, Object ... params) throws SQLiteDBException
	{
		try
		{
			Set<T> results = new HashSet<T>();
			SQLiteStatement stmnt = conn.prepare(selectQuery(expectedType, maxVersion, version, from, where, params)+";");
			while(stmnt.step())
			{
				eUnitType type = eUnitType.valueOf(stmnt.columnString(0));
				String v = stmnt.columnString(1);
				if (v == null) throw new Error("Unit with no VersionedUnit !");
				boolean isVersioned = (!v.isEmpty());
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

	/** Set maxVersion to null to select last version. */
	public static <T extends IUnit> boolean existMaxVersion(SQLiteConnection conn, Class<T> expectedType, Integer maxVersion, String from, String where, Object ... params) throws SQLiteDBException
	{
		return exist(conn, expectedType, true, maxVersion, from, where, params);
	}

	public static <T extends IUnit> boolean existVersion(SQLiteConnection conn,Class<T> expectedType, int version, String from, String where, Object ... params) throws SQLiteDBException
	{
		return exist(conn, expectedType, false, version, from, where, params);
	}

	public static <T extends IUnit> boolean existUnversioned(SQLiteConnection conn, Class<T> expectedType, String from, String where, Object ... params) throws SQLiteDBException
	{
		return exist(conn, expectedType, false, null, from, where, params);
	}

	private static <T extends IUnit> boolean exist(SQLiteConnection conn, Class<T> expectedType, boolean maxVersion, Integer version, String from, String where, Object ... params) throws SQLiteDBException
	{
		try
		{
			SQLiteStatement stmnt = conn.prepare("SELECT EXISTS ( "+selectQuery(expectedType, maxVersion, version, from, where, params) + " );");
			return stmnt.step() && stmnt.columnInt(0) != 0;
		}
		catch(Exception e)
		{
			throw new SQLiteDBException(e);
		}
	}


	private static <T extends IUnit> String selectQuery(Class<T> expectedType, boolean maxVersion, Integer version, String from, String where, Object ... params)
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
		String versionFilter;
		if (maxVersion)
		{
			versionFilter = String.format("(VersionedUnit.turn = ( SELECT MAX(LVVersionedUnit.turn) FROM VersionedUnit LVVersionedUnit WHERE LVVersionedUnit.owner = Unit.owner AND LVVersionedUnit.name = Unit.name AND LVVersionedUnit.type = Unit.type%s ))", (version != null && version >= 0) ? " AND LVVersionedUnit.turn <= "+version : "");
		}
		else
		{
			versionFilter = (version == null) ? "" : String.format("(VersionedUnit.turn = %d)", version);
		}
		if (versionFilter != null && !versionFilter.isEmpty()) where = (where == null) ? versionFilter : String.format("%s AND %s", where, versionFilter);
		return String.format("SELECT Unit.type, VersionedUnit.type, VersionedUnit.*, Fleet.*, VersionedFleet.*, Probe.*, VersionedProbe.*, CarbonCarrier.*, VersionedCarbonCarrier.*, AntiProbeMissile.*, VersionedAntiProbeMissile.*, SpaceRoadDeliverer.*, VersionedSpaceRoadDeliverer.*, PulsarMissile.*, VersionedPulsarMissile.*, Unit.* FROM Unit%s LEFT JOIN VersionedUnit USING (owner, name, type) LEFT JOIN Fleet USING (owner, name, type) LEFT JOIN VersionedFleet USING (owner, name, turn, type) LEFT JOIN Probe USING (owner, name, type) LEFT JOIN VersionedProbe USING (owner, name, turn, type) LEFT JOIN CarbonCarrier USING (owner, name, type) LEFT JOIN VersionedCarbonCarrier USING (owner, name, turn, type) LEFT JOIN AntiProbeMissile USING (owner, name, type) LEFT JOIN VersionedAntiProbeMissile USING (owner, name, turn, type) LEFT JOIN SpaceRoadDeliverer USING (owner, name, type) LEFT JOIN VersionedSpaceRoadDeliverer USING (owner, name, turn, type) LEFT JOIN PulsarMissile USING (owner, name, type) LEFT JOIN VersionedPulsarMissile USING (owner, name, turn, type)%s", (from != null && !from.isEmpty()) ? ", "+from : "", (where != null && !where.isEmpty()) ? " WHERE "+where : "");
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
				conn.exec(String.format("INSERT INTO Unit (owner, name, type) VALUES (%s, %s, %s);", "'"+unit.getOwner()+"'", "'"+unit.getName()+"'", "'"+unit.getType()+"'").replaceAll("'null'", "NULL"));
				if (vunit != null)
				{
					conn.exec(String.format("INSERT INTO VersionedUnit (owner, name, turn, type, departure_x, departure_y, departure_z, progress, destination_x, destination_y, destination_z) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s);", "'"+vunit.getOwner()+"'", "'"+vunit.getName()+"'", "'"+vunit.getTurn()+"'", "'"+vunit.getType()+"'", vunit.getDeparture() == null ? "NULL" : "'"+vunit.getDeparture().x+"'", vunit.getDeparture() == null ? "NULL" : "'"+vunit.getDeparture().y+"'", vunit.getDeparture() == null ? "NULL" : "'"+vunit.getDeparture().z+"'", "'"+vunit.getProgress()+"'", vunit.getDestination() == null ? "NULL" : "'"+vunit.getDestination().x+"'", vunit.getDestination() == null ? "NULL" : "'"+vunit.getDestination().y+"'", vunit.getDestination() == null ? "NULL" : "'"+vunit.getDestination().z+"'").replaceAll("'null'", "NULL"));
				}
				switch(unit.getType())
				{
					case PulsarMissile:
					{
						IPulsarMissile pulsarMissile = IPulsarMissile.class.cast(unit);
						conn.exec(String.format("INSERT INTO PulsarMissile (owner, name, type, time, volume) VALUES (%s, %s, %s, %s, %s);", "'"+pulsarMissile.getOwner()+"'", "'"+pulsarMissile.getName()+"'", "'"+pulsarMissile.getType()+"'", "'"+pulsarMissile.getTime()+"'", "'"+pulsarMissile.getVolume()+"'").replaceAll("'null'", "NULL"));
						if (vunit != null)
						{
							IVersionedPulsarMissile versionedPulsarMissile = IVersionedPulsarMissile.class.cast(unit);
							conn.exec(String.format("INSERT INTO VersionedPulsarMissile (owner, name, turn, type, direction_x, direction_y, direction_z) VALUES (%s, %s, %s, %s, %s, %s, %s);", "'"+versionedPulsarMissile.getOwner()+"'", "'"+versionedPulsarMissile.getName()+"'", "'"+versionedPulsarMissile.getTurn()+"'", "'"+versionedPulsarMissile.getType()+"'", versionedPulsarMissile.getDirection() == null ? "NULL" : "'"+versionedPulsarMissile.getDirection().x+"'", versionedPulsarMissile.getDirection() == null ? "NULL" : "'"+versionedPulsarMissile.getDirection().y+"'", versionedPulsarMissile.getDirection() == null ? "NULL" : "'"+versionedPulsarMissile.getDirection().z+"'").replaceAll("'null'", "NULL"));
						}
						break;
					}
					case Probe:
					{
						IProbe probe = IProbe.class.cast(unit);
						conn.exec(String.format("INSERT INTO Probe (owner, name, type) VALUES (%s, %s, %s);", "'"+probe.getOwner()+"'", "'"+probe.getName()+"'", "'"+probe.getType()+"'").replaceAll("'null'", "NULL"));
						if (vunit != null)
						{
							IVersionedProbe versionedProbe = IVersionedProbe.class.cast(unit);
							conn.exec(String.format("INSERT INTO VersionedProbe (owner, name, turn, type) VALUES (%s, %s, %s, %s);", "'"+versionedProbe.getOwner()+"'", "'"+versionedProbe.getName()+"'", "'"+versionedProbe.getTurn()+"'", "'"+versionedProbe.getType()+"'").replaceAll("'null'", "NULL"));
						}
						break;
					}
					case AntiProbeMissile:
					{
						IAntiProbeMissile antiProbeMissile = IAntiProbeMissile.class.cast(unit);
						conn.exec(String.format("INSERT INTO AntiProbeMissile (owner, name, type) VALUES (%s, %s, %s);", "'"+antiProbeMissile.getOwner()+"'", "'"+antiProbeMissile.getName()+"'", "'"+antiProbeMissile.getType()+"'").replaceAll("'null'", "NULL"));
						if (vunit != null)
						{
							IVersionedAntiProbeMissile versionedAntiProbeMissile = IVersionedAntiProbeMissile.class.cast(unit);
							conn.exec(String.format("INSERT INTO VersionedAntiProbeMissile (owner, name, turn, type, targetOwner, targetName, targetTurn) VALUES (%s, %s, %s, %s, %s, %s, %s);", "'"+versionedAntiProbeMissile.getOwner()+"'", "'"+versionedAntiProbeMissile.getName()+"'", "'"+versionedAntiProbeMissile.getTurn()+"'", "'"+versionedAntiProbeMissile.getType()+"'", "'"+versionedAntiProbeMissile.getTargetOwner()+"'", "'"+versionedAntiProbeMissile.getTargetName()+"'", "'"+versionedAntiProbeMissile.getTargetTurn()+"'").replaceAll("'null'", "NULL"));
						}
						break;
					}
					case Fleet:
					{
						IFleet fleet = IFleet.class.cast(unit);
						conn.exec(String.format("INSERT INTO Fleet (owner, name, type) VALUES (%s, %s, %s);", "'"+fleet.getOwner()+"'", "'"+fleet.getName()+"'", "'"+fleet.getType()+"'").replaceAll("'null'", "NULL"));
						if (vunit != null)
						{
							IVersionedFleet versionedFleet = IVersionedFleet.class.cast(unit);
							conn.exec(String.format("INSERT INTO VersionedFleet (owner, name, turn, type) VALUES (%s, %s, %s, %s);", "'"+versionedFleet.getOwner()+"'", "'"+versionedFleet.getName()+"'", "'"+versionedFleet.getTurn()+"'", "'"+versionedFleet.getType()+"'").replaceAll("'null'", "NULL"));
						}
						break;
					}
					case CarbonCarrier:
					{
						ICarbonCarrier carbonCarrier = ICarbonCarrier.class.cast(unit);
						conn.exec(String.format("INSERT INTO CarbonCarrier (owner, name, type, sourceType, sourceCelestialBodyName, sourceTurn) VALUES (%s, %s, %s, %s, %s, %s);", "'"+carbonCarrier.getOwner()+"'", "'"+carbonCarrier.getName()+"'", "'"+carbonCarrier.getType()+"'", "'"+carbonCarrier.getSourceType()+"'", "'"+carbonCarrier.getSourceCelestialBodyName()+"'", "'"+carbonCarrier.getSourceTurn()+"'").replaceAll("'null'", "NULL"));
						if (vunit != null)
						{
							IVersionedCarbonCarrier versionedCarbonCarrier = IVersionedCarbonCarrier.class.cast(unit);
							conn.exec(String.format("INSERT INTO VersionedCarbonCarrier (owner, name, turn, type, orderOwner, orderSource, orderPriority) VALUES (%s, %s, %s, %s, %s, %s, %s);", "'"+versionedCarbonCarrier.getOwner()+"'", "'"+versionedCarbonCarrier.getName()+"'", "'"+versionedCarbonCarrier.getTurn()+"'", "'"+versionedCarbonCarrier.getType()+"'", "'"+versionedCarbonCarrier.getOrderOwner()+"'", "'"+versionedCarbonCarrier.getOrderSource()+"'", "'"+versionedCarbonCarrier.getOrderPriority()+"'").replaceAll("'null'", "NULL"));
						}
						break;
					}
					case SpaceRoadDeliverer:
					{
						ISpaceRoadDeliverer spaceRoadDeliverer = ISpaceRoadDeliverer.class.cast(unit);
						conn.exec(String.format("INSERT INTO SpaceRoadDeliverer (owner, name, type, sourceType, sourceCelestialBodyName, sourceTurn, destinationType, destinationCelestialBodyName, destinationTurn) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s);", "'"+spaceRoadDeliverer.getOwner()+"'", "'"+spaceRoadDeliverer.getName()+"'", "'"+spaceRoadDeliverer.getType()+"'", "'"+spaceRoadDeliverer.getSourceType()+"'", "'"+spaceRoadDeliverer.getSourceCelestialBodyName()+"'", "'"+spaceRoadDeliverer.getSourceTurn()+"'", "'"+spaceRoadDeliverer.getDestinationType()+"'", "'"+spaceRoadDeliverer.getDestinationCelestialBodyName()+"'", "'"+spaceRoadDeliverer.getDestinationTurn()+"'").replaceAll("'null'", "NULL"));
						if (vunit != null)
						{
							IVersionedSpaceRoadDeliverer versionedSpaceRoadDeliverer = IVersionedSpaceRoadDeliverer.class.cast(unit);
							conn.exec(String.format("INSERT INTO VersionedSpaceRoadDeliverer (owner, name, turn, type) VALUES (%s, %s, %s, %s);", "'"+versionedSpaceRoadDeliverer.getOwner()+"'", "'"+versionedSpaceRoadDeliverer.getName()+"'", "'"+versionedSpaceRoadDeliverer.getTurn()+"'", "'"+versionedSpaceRoadDeliverer.getType()+"'").replaceAll("'null'", "NULL"));
						}
						break;
					}
				}
			}
			else
			{
				conn.exec(String.format("UPDATE Unit SET type = %s WHERE  owner = %s AND name = %s ;", "'"+unit.getType()+"'", "'"+unit.getOwner()+"'", "'"+unit.getName()+"'").replaceAll("'null'", "NULL"));
				if (vunit != null)
				{
					conn.exec(String.format("UPDATE VersionedUnit SET type = %s,  departure_x = %s,  departure_y = %s,  departure_z = %s,  progress = %s,  destination_x = %s,  destination_y = %s,  destination_z = %s WHERE  owner = %s AND name = %s AND turn = %s ;", "'"+vunit.getType()+"'", vunit.getDeparture() == null ? "NULL" : "'"+vunit.getDeparture().x+"'", vunit.getDeparture() == null ? "NULL" : "'"+vunit.getDeparture().y+"'", vunit.getDeparture() == null ? "NULL" : "'"+vunit.getDeparture().z+"'", "'"+vunit.getProgress()+"'", vunit.getDestination() == null ? "NULL" : "'"+vunit.getDestination().x+"'", vunit.getDestination() == null ? "NULL" : "'"+vunit.getDestination().y+"'", vunit.getDestination() == null ? "NULL" : "'"+vunit.getDestination().z+"'", "'"+vunit.getOwner()+"'", "'"+vunit.getName()+"'", "'"+vunit.getTurn()+"'").replaceAll("'null'", "NULL"));
				}
				switch(unit.getType())
				{
					case PulsarMissile:
					{
						IPulsarMissile pulsarMissile = IPulsarMissile.class.cast(unit);
						conn.exec(String.format("UPDATE PulsarMissile SET type = %s,  time = %s,  volume = %s WHERE  owner = %s AND name = %s ;", "'"+pulsarMissile.getType()+"'", "'"+pulsarMissile.getTime()+"'", "'"+pulsarMissile.getVolume()+"'", "'"+pulsarMissile.getOwner()+"'", "'"+pulsarMissile.getName()+"'").replaceAll("'null'", "NULL"));
						if (vunit != null)
						{
							IVersionedPulsarMissile versionedPulsarMissile = IVersionedPulsarMissile.class.cast(unit);
							conn.exec(String.format("UPDATE VersionedPulsarMissile SET type = %s,  direction_x = %s,  direction_y = %s,  direction_z = %s WHERE  owner = %s AND name = %s AND turn = %s ;", "'"+versionedPulsarMissile.getType()+"'", versionedPulsarMissile.getDirection() == null ? "NULL" : "'"+versionedPulsarMissile.getDirection().x+"'", versionedPulsarMissile.getDirection() == null ? "NULL" : "'"+versionedPulsarMissile.getDirection().y+"'", versionedPulsarMissile.getDirection() == null ? "NULL" : "'"+versionedPulsarMissile.getDirection().z+"'", "'"+versionedPulsarMissile.getOwner()+"'", "'"+versionedPulsarMissile.getName()+"'", "'"+versionedPulsarMissile.getTurn()+"'").replaceAll("'null'", "NULL"));
						}
						break;
					}
					case Probe:
					{
						IProbe probe = IProbe.class.cast(unit);
						conn.exec(String.format("UPDATE Probe SET type = %s WHERE  owner = %s AND name = %s ;", "'"+probe.getType()+"'", "'"+probe.getOwner()+"'", "'"+probe.getName()+"'").replaceAll("'null'", "NULL"));
						if (vunit != null)
						{
							IVersionedProbe versionedProbe = IVersionedProbe.class.cast(unit);
							conn.exec(String.format("UPDATE VersionedProbe SET type = %s WHERE  owner = %s AND name = %s AND turn = %s ;", "'"+versionedProbe.getType()+"'", "'"+versionedProbe.getOwner()+"'", "'"+versionedProbe.getName()+"'", "'"+versionedProbe.getTurn()+"'").replaceAll("'null'", "NULL"));
						}
						break;
					}
					case AntiProbeMissile:
					{
						IAntiProbeMissile antiProbeMissile = IAntiProbeMissile.class.cast(unit);
						conn.exec(String.format("UPDATE AntiProbeMissile SET type = %s WHERE  owner = %s AND name = %s ;", "'"+antiProbeMissile.getType()+"'", "'"+antiProbeMissile.getOwner()+"'", "'"+antiProbeMissile.getName()+"'").replaceAll("'null'", "NULL"));
						if (vunit != null)
						{
							IVersionedAntiProbeMissile versionedAntiProbeMissile = IVersionedAntiProbeMissile.class.cast(unit);
							conn.exec(String.format("UPDATE VersionedAntiProbeMissile SET type = %s,  targetOwner = %s,  targetName = %s,  targetTurn = %s WHERE  owner = %s AND name = %s AND turn = %s ;", "'"+versionedAntiProbeMissile.getType()+"'", "'"+versionedAntiProbeMissile.getTargetOwner()+"'", "'"+versionedAntiProbeMissile.getTargetName()+"'", "'"+versionedAntiProbeMissile.getTargetTurn()+"'", "'"+versionedAntiProbeMissile.getOwner()+"'", "'"+versionedAntiProbeMissile.getName()+"'", "'"+versionedAntiProbeMissile.getTurn()+"'").replaceAll("'null'", "NULL"));
						}
						break;
					}
					case Fleet:
					{
						IFleet fleet = IFleet.class.cast(unit);
						conn.exec(String.format("UPDATE Fleet SET type = %s WHERE  owner = %s AND name = %s ;", "'"+fleet.getType()+"'", "'"+fleet.getOwner()+"'", "'"+fleet.getName()+"'").replaceAll("'null'", "NULL"));
						if (vunit != null)
						{
							IVersionedFleet versionedFleet = IVersionedFleet.class.cast(unit);
							conn.exec(String.format("UPDATE VersionedFleet SET type = %s WHERE  owner = %s AND name = %s AND turn = %s ;", "'"+versionedFleet.getType()+"'", "'"+versionedFleet.getOwner()+"'", "'"+versionedFleet.getName()+"'", "'"+versionedFleet.getTurn()+"'").replaceAll("'null'", "NULL"));
						}
						break;
					}
					case CarbonCarrier:
					{
						ICarbonCarrier carbonCarrier = ICarbonCarrier.class.cast(unit);
						conn.exec(String.format("UPDATE CarbonCarrier SET type = %s,  sourceType = %s,  sourceCelestialBodyName = %s,  sourceTurn = %s WHERE  owner = %s AND name = %s ;", "'"+carbonCarrier.getType()+"'", "'"+carbonCarrier.getSourceType()+"'", "'"+carbonCarrier.getSourceCelestialBodyName()+"'", "'"+carbonCarrier.getSourceTurn()+"'", "'"+carbonCarrier.getOwner()+"'", "'"+carbonCarrier.getName()+"'").replaceAll("'null'", "NULL"));
						if (vunit != null)
						{
							IVersionedCarbonCarrier versionedCarbonCarrier = IVersionedCarbonCarrier.class.cast(unit);
							conn.exec(String.format("UPDATE VersionedCarbonCarrier SET type = %s,  orderOwner = %s,  orderSource = %s,  orderPriority = %s WHERE  owner = %s AND name = %s AND turn = %s ;", "'"+versionedCarbonCarrier.getType()+"'", "'"+versionedCarbonCarrier.getOrderOwner()+"'", "'"+versionedCarbonCarrier.getOrderSource()+"'", "'"+versionedCarbonCarrier.getOrderPriority()+"'", "'"+versionedCarbonCarrier.getOwner()+"'", "'"+versionedCarbonCarrier.getName()+"'", "'"+versionedCarbonCarrier.getTurn()+"'").replaceAll("'null'", "NULL"));
						}
						break;
					}
					case SpaceRoadDeliverer:
					{
						ISpaceRoadDeliverer spaceRoadDeliverer = ISpaceRoadDeliverer.class.cast(unit);
						conn.exec(String.format("UPDATE SpaceRoadDeliverer SET type = %s,  sourceType = %s,  sourceCelestialBodyName = %s,  sourceTurn = %s,  destinationType = %s,  destinationCelestialBodyName = %s,  destinationTurn = %s WHERE  owner = %s AND name = %s ;", "'"+spaceRoadDeliverer.getType()+"'", "'"+spaceRoadDeliverer.getSourceType()+"'", "'"+spaceRoadDeliverer.getSourceCelestialBodyName()+"'", "'"+spaceRoadDeliverer.getSourceTurn()+"'", "'"+spaceRoadDeliverer.getDestinationType()+"'", "'"+spaceRoadDeliverer.getDestinationCelestialBodyName()+"'", "'"+spaceRoadDeliverer.getDestinationTurn()+"'", "'"+spaceRoadDeliverer.getOwner()+"'", "'"+spaceRoadDeliverer.getName()+"'").replaceAll("'null'", "NULL"));
						if (vunit != null)
						{
							IVersionedSpaceRoadDeliverer versionedSpaceRoadDeliverer = IVersionedSpaceRoadDeliverer.class.cast(unit);
							conn.exec(String.format("UPDATE VersionedSpaceRoadDeliverer SET type = %s WHERE  owner = %s AND name = %s AND turn = %s ;", "'"+versionedSpaceRoadDeliverer.getType()+"'", "'"+versionedSpaceRoadDeliverer.getOwner()+"'", "'"+versionedSpaceRoadDeliverer.getName()+"'", "'"+versionedSpaceRoadDeliverer.getTurn()+"'").replaceAll("'null'", "NULL"));
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
