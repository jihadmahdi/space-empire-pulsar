package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.base.BaseBuilding;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import org.axan.sep.common.IGameConfig;
import java.util.Set;
import org.axan.sep.common.Protocol.eBuildingType;
import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;
import java.util.HashSet;
import com.almworks.sqlite4java.SQLiteStatement;
import com.almworks.sqlite4java.SQLiteConnection;

public class Building implements IBuilding
{
	private final BaseBuilding baseBuildingProxy;

	public Building(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		this.baseBuildingProxy = new BaseBuilding(stmnt);
	}

	public Integer getNbSlots()
	{
		return baseBuildingProxy.getNbSlots();
	}

	public String getCelestialBodyName()
	{
		return baseBuildingProxy.getCelestialBodyName();
	}

	public Integer getTurn()
	{
		return baseBuildingProxy.getTurn();
	}

	public eBuildingType getType()
	{
		return eBuildingType.valueOf(baseBuildingProxy.getType());
	}

	public static <T extends IBuilding> Set<T> select(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, boolean lastVersion, String where, Object ... params) throws SQLiteDBException
	{
		try
		{
			Set<T> results = new HashSet<T>();

			if (where != null && params != null) where = String.format(where, params);
			if (lastVersion)
			{
				where = String.format("%s(Building.turn = ( SELECT MAX(LVBuilding.turn) FROM Building LVBuilding WHERE LVBuilding.nbSlots = Building.nbSlots AND LVBuilding.celestialBodyName = Building.celestialBodyName AND LVBuilding.turn = Building.turn AND LVBuilding.type = Building.type ))", (where != null && !where.isEmpty()) ? "("+where+") AND " : "");
			}
			SQLiteStatement stmnt = conn.prepare(String.format("SELECT Building.type, Building.type, * FROM Building LEFT JOIN ExtractionModule USING (celestialBodyName, turn, type) LEFT JOIN GovernmentModule USING (celestialBodyName, turn, type) LEFT JOIN DefenseModule USING (celestialBodyName, turn, type) LEFT JOIN StarshipPlant USING (celestialBodyName, turn, type) LEFT JOIN SpaceCounter USING (celestialBodyName, turn, type) LEFT JOIN PulsarLaunchingPad USING (celestialBodyName, turn, type)%s ;", (where != null && !where.isEmpty()) ? " WHERE "+where : ""));
			while(stmnt.step())
			{
				eBuildingType type = eBuildingType.valueOf(stmnt.columnString(0));
				Class<? extends IBuilding> clazz = (Class<? extends IBuilding>)  Class.forName(String.format("%s.%s%s", Building.class.getPackage().getName(), "", type.toString()));
				IBuilding o = SQLiteORMGenerator.mapTo(clazz, stmnt, config);
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
