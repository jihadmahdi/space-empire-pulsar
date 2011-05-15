package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.base.BaseBuilding;
import org.axan.sep.common.db.IBuilding;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteStatement;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;
import org.axan.sep.common.Protocol.eBuildingType;
import org.axan.sep.common.db.IDefenseModule;
import org.axan.sep.common.db.IExtractionModule;
import org.axan.sep.common.db.IGameConfig;
import org.axan.sep.common.db.IGovernmentModule;
import org.axan.sep.common.db.IPulsarLaunchingPad;
import org.axan.sep.common.db.ISpaceCounter;
import org.axan.sep.common.db.IStarshipPlant;

public class Building implements IBuilding
{
	private final BaseBuilding baseBuildingProxy;
	private eBuildingType type;

	public Building(eBuildingType type, String celestialBodyName, Integer turn, Integer nbSlots)
	{
		baseBuildingProxy = new BaseBuilding(type.toString(), celestialBodyName, turn, nbSlots);
		this.type = type;
	}

	public Building(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		this.baseBuildingProxy = new BaseBuilding(stmnt);
		this.type = eBuildingType.valueOf(baseBuildingProxy.getType());
	}

	public eBuildingType getType()
	{
		return type;
	}

	public String getCelestialBodyName()
	{
		return baseBuildingProxy.getCelestialBodyName();
	}

	public Integer getTurn()
	{
		return baseBuildingProxy.getTurn();
	}

	public Integer getNbSlots()
	{
		return baseBuildingProxy.getNbSlots();
	}

	/** Set maxVersion to null to select last version. */
	public static <T extends IBuilding> Set<T> selectMaxVersion(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, Integer maxVersion, String from, String where, Object ... params) throws SQLiteDBException
	{
		return select(conn, config, expectedType, true, maxVersion, from, where, params);
	}

	public static <T extends IBuilding> Set<T> selectVersion(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, int version, String from, String where, Object ... params) throws SQLiteDBException
	{
		return select(conn, config, expectedType, false, version, from, where, params);
	}

	public static <T extends IBuilding> Set<T> selectUnversioned(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, String from, String where, Object ... params) throws SQLiteDBException
	{
		return select(conn, config, expectedType, false, null, from, where, params);
	}

	private static <T extends IBuilding> Set<T> select(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, boolean maxVersion, Integer version, String from, String where, Object ... params) throws SQLiteDBException
	{
		try
		{
			Set<T> results = new HashSet<T>();
			SQLiteStatement stmnt = conn.prepare(selectQuery(expectedType, maxVersion, version, from, where, params)+";");
			while(stmnt.step())
			{
				eBuildingType type = eBuildingType.valueOf(stmnt.columnString(0));
				String v = stmnt.columnString(1);
				if (v == null) throw new Error("Building with no Building !");
				boolean isVersioned = (!v.isEmpty());
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

	/** Set maxVersion to null to select last version. */
	public static <T extends IBuilding> boolean existMaxVersion(SQLiteConnection conn, Class<T> expectedType, Integer maxVersion, String from, String where, Object ... params) throws SQLiteDBException
	{
		return exist(conn, expectedType, true, maxVersion, from, where, params);
	}

	public static <T extends IBuilding> boolean existVersion(SQLiteConnection conn,Class<T> expectedType, int version, String from, String where, Object ... params) throws SQLiteDBException
	{
		return exist(conn, expectedType, false, version, from, where, params);
	}

	public static <T extends IBuilding> boolean existUnversioned(SQLiteConnection conn, Class<T> expectedType, String from, String where, Object ... params) throws SQLiteDBException
	{
		return exist(conn, expectedType, false, null, from, where, params);
	}

	private static <T extends IBuilding> boolean exist(SQLiteConnection conn, Class<T> expectedType, boolean maxVersion, Integer version, String from, String where, Object ... params) throws SQLiteDBException
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


	private static <T extends IBuilding> String selectQuery(Class<T> expectedType, boolean maxVersion, Integer version, String from, String where, Object ... params)
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
			versionFilter = String.format("(Building.turn = ( SELECT MAX(LVBuilding.turn) FROM Building LVBuilding WHERE LVBuilding.type = Building.type AND LVBuilding.celestialBodyName = Building.celestialBodyName AND LVBuilding.turn = Building.turn AND LVBuilding.nbSlots = Building.nbSlots%s ))", (version != null && version >= 0) ? " AND LVBuilding.turn <= "+version : "");
		}
		else
		{
			versionFilter = (version == null) ? "" : String.format("(Building.turn = %d)", version);
		}
		if (versionFilter != null && !versionFilter.isEmpty()) where = (where == null) ? versionFilter : String.format("%s AND %s", where, versionFilter);
		return String.format("SELECT Building.type, Building.type, Building.*, ExtractionModule.*, GovernmentModule.*, DefenseModule.*, StarshipPlant.*, SpaceCounter.*, PulsarLaunchingPad.* FROM Building%s LEFT JOIN ExtractionModule USING (type, celestialBodyName, turn) LEFT JOIN GovernmentModule USING (type, celestialBodyName, turn) LEFT JOIN DefenseModule USING (type, celestialBodyName, turn) LEFT JOIN StarshipPlant USING (type, celestialBodyName, turn) LEFT JOIN SpaceCounter USING (type, celestialBodyName, turn) LEFT JOIN PulsarLaunchingPad USING (type, celestialBodyName, turn)%s", (from != null && !from.isEmpty()) ? ", "+from : "", (where != null && !where.isEmpty()) ? " WHERE "+where : "");
	}

	public static <T extends IBuilding> void insertOrUpdate(SQLiteConnection conn, T building) throws SQLiteDBException
	{
		try
		{
			SQLiteStatement stmnt = conn.prepare(String.format("SELECT EXISTS ( SELECT type FROM Building WHERE type = %s AND celestialBodyName = %s AND turn = %s) AS exist ;", "'"+building.getType()+"'", "'"+building.getCelestialBodyName()+"'", "'"+building.getTurn()+"'"));
			stmnt.step();
			if (stmnt.columnInt(0) == 0)
			{
				conn.exec(String.format("INSERT INTO Building (type, celestialBodyName, turn, nbSlots) VALUES (%s, %s, %s, %s);", "'"+building.getType()+"'", "'"+building.getCelestialBodyName()+"'", "'"+building.getTurn()+"'", "'"+building.getNbSlots()+"'").replaceAll("'null'", "NULL"));
				switch(building.getType())
				{
					case PulsarLaunchingPad:
					{
						IPulsarLaunchingPad pulsarLaunchingPad = IPulsarLaunchingPad.class.cast(building);
						conn.exec(String.format("INSERT INTO PulsarLaunchingPad (type, celestialBodyName, turn, firedDate) VALUES (%s, %s, %s, %s);", "'"+pulsarLaunchingPad.getType()+"'", "'"+pulsarLaunchingPad.getCelestialBodyName()+"'", "'"+pulsarLaunchingPad.getTurn()+"'", "'"+pulsarLaunchingPad.getFiredDate()+"'").replaceAll("'null'", "NULL"));
						break;
					}
					case SpaceCounter:
					{
						ISpaceCounter spaceCounter = ISpaceCounter.class.cast(building);
						conn.exec(String.format("INSERT INTO SpaceCounter (type, celestialBodyName, turn) VALUES (%s, %s, %s);", "'"+spaceCounter.getType()+"'", "'"+spaceCounter.getCelestialBodyName()+"'", "'"+spaceCounter.getTurn()+"'").replaceAll("'null'", "NULL"));
						break;
					}
					case GovernmentModule:
					{
						IGovernmentModule governmentModule = IGovernmentModule.class.cast(building);
						conn.exec(String.format("INSERT INTO GovernmentModule (type, celestialBodyName, turn) VALUES (%s, %s, %s);", "'"+governmentModule.getType()+"'", "'"+governmentModule.getCelestialBodyName()+"'", "'"+governmentModule.getTurn()+"'").replaceAll("'null'", "NULL"));
						break;
					}
					case DefenseModule:
					{
						IDefenseModule defenseModule = IDefenseModule.class.cast(building);
						conn.exec(String.format("INSERT INTO DefenseModule (type, celestialBodyName, turn) VALUES (%s, %s, %s);", "'"+defenseModule.getType()+"'", "'"+defenseModule.getCelestialBodyName()+"'", "'"+defenseModule.getTurn()+"'").replaceAll("'null'", "NULL"));
						break;
					}
					case StarshipPlant:
					{
						IStarshipPlant starshipPlant = IStarshipPlant.class.cast(building);
						conn.exec(String.format("INSERT INTO StarshipPlant (type, celestialBodyName, turn) VALUES (%s, %s, %s);", "'"+starshipPlant.getType()+"'", "'"+starshipPlant.getCelestialBodyName()+"'", "'"+starshipPlant.getTurn()+"'").replaceAll("'null'", "NULL"));
						break;
					}
					case ExtractionModule:
					{
						IExtractionModule extractionModule = IExtractionModule.class.cast(building);
						conn.exec(String.format("INSERT INTO ExtractionModule (type, celestialBodyName, turn) VALUES (%s, %s, %s);", "'"+extractionModule.getType()+"'", "'"+extractionModule.getCelestialBodyName()+"'", "'"+extractionModule.getTurn()+"'").replaceAll("'null'", "NULL"));
						break;
					}
				}
			}
			else
			{
				conn.exec(String.format("UPDATE Building SET nbSlots = %s WHERE  type = %s AND celestialBodyName = %s AND turn = %s ;", "'"+building.getNbSlots()+"'", "'"+building.getType()+"'", "'"+building.getCelestialBodyName()+"'", "'"+building.getTurn()+"'").replaceAll("'null'", "NULL"));
				switch(building.getType())
				{
					case PulsarLaunchingPad:
					{
						IPulsarLaunchingPad pulsarLaunchingPad = IPulsarLaunchingPad.class.cast(building);
						conn.exec(String.format("UPDATE PulsarLaunchingPad SET firedDate = %s WHERE  type = %s AND celestialBodyName = %s AND turn = %s ;", "'"+pulsarLaunchingPad.getFiredDate()+"'", "'"+pulsarLaunchingPad.getType()+"'", "'"+pulsarLaunchingPad.getCelestialBodyName()+"'", "'"+pulsarLaunchingPad.getTurn()+"'").replaceAll("'null'", "NULL"));
						break;
					}
					case SpaceCounter:
					{
						ISpaceCounter spaceCounter = ISpaceCounter.class.cast(building);
						conn.exec(";");
						break;
					}
					case GovernmentModule:
					{
						IGovernmentModule governmentModule = IGovernmentModule.class.cast(building);
						conn.exec(";");
						break;
					}
					case DefenseModule:
					{
						IDefenseModule defenseModule = IDefenseModule.class.cast(building);
						conn.exec(";");
						break;
					}
					case StarshipPlant:
					{
						IStarshipPlant starshipPlant = IStarshipPlant.class.cast(building);
						conn.exec(";");
						break;
					}
					case ExtractionModule:
					{
						IExtractionModule extractionModule = IExtractionModule.class.cast(building);
						conn.exec(";");
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
