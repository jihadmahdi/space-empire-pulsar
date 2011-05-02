package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.base.BaseUnit;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import org.axan.sep.common.IGameConfig;
import java.util.Set;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;
import java.util.HashSet;
import com.almworks.sqlite4java.SQLiteStatement;
import com.almworks.sqlite4java.SQLiteConnection;

public class Unit implements IUnit
{
	private final BaseUnit baseUnitProxy;
	private final float sight;

	public Unit(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		this.baseUnitProxy = new BaseUnit(stmnt);
		this.sight = config.getUnitTypeSight(getType());
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

	public eUnitType getType()
	{
		return eUnitType.valueOf(baseUnitProxy.getType());
	}

	public String getName()
	{
		return baseUnitProxy.getName();
	}

	public static <T extends IUnit> Set<T> select(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, boolean lastVersion, String where, Object ... params) throws SQLiteDBException
	{
		try
		{
			Set<T> results = new HashSet<T>();

			if (where != null && params != null) where = String.format(where, params);
			if (lastVersion)
			{
				where = String.format("%s(VersionedUnit.turn = ( SELECT MAX(LVVersionedUnit.turn) FROM VersionedUnit LVVersionedUnit WHERE LVVersionedUnit.owner = Unit.owner AND LVVersionedUnit.type = Unit.type AND LVVersionedUnit.name = Unit.name ))", (where != null && !where.isEmpty()) ? "("+where+") AND " : "");
			}
			SQLiteStatement stmnt = conn.prepare(String.format("SELECT Unit.type, VersionedUnit.type, * FROM Unit LEFT JOIN VersionedUnit USING (owner, type, name) LEFT JOIN VersionedFleet USING (owner, turn, type, name) LEFT JOIN VersionedProbe USING (owner, destination_z, destination_y, destination_x, turn, type, name) LEFT JOIN VersionedCarbonCarrier USING (owner, turn, type, name) LEFT JOIN VersionedAntiProbeMissile USING (owner, turn, type, name) LEFT JOIN VersionedSpaceRoadDeliverer USING (owner, turn, type, name) LEFT JOIN VersionedPulsarMissile USING (owner, turn, type, name) LEFT JOIN PulsarMissile USING (owner, type, name) LEFT JOIN Probe USING (owner, type, name) LEFT JOIN CarbonCarrier USING (owner, type, name) LEFT JOIN AntiProbeMissile USING (owner, type, name) LEFT JOIN SpaceRoadDeliverer USING (owner, type, name) LEFT JOIN Fleet USING (owner, type, name)%s ;", (where != null && !where.isEmpty()) ? " WHERE "+where : ""));
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

}
