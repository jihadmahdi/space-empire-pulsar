package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.base.BaseBuilding;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteStatement;
import java.util.HashSet;
import java.util.Set;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;
import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.Protocol.eBuildingType;

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

	public static <T extends IBuilding> Set<T> select(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, boolean lastVersion, String where, Object ... params) throws SQLiteDBException
	{
		try
		{
			Set<T> results = new HashSet<T>();

			if (where != null && params != null) where = String.format(where, params);
			if (lastVersion)
			{
				where = String.format("%s(Building.turn = ( SELECT MAX(LVBuilding.turn) FROM Building LVBuilding WHERE LVBuilding.type = Building.type AND LVBuilding.celestialBodyName = Building.celestialBodyName AND LVBuilding.turn = Building.turn AND LVBuilding.nbSlots = Building.nbSlots ))", (where != null && !where.isEmpty()) ? "("+where+") AND " : "");
			}
			SQLiteStatement stmnt = conn.prepare(String.format("SELECT Building.type, Building.type, * FROM Building LEFT JOIN ExtractionModule USING (type, celestialBodyName, turn) LEFT JOIN GovernmentModule USING (type, celestialBodyName, turn) LEFT JOIN DefenseModule USING (type, celestialBodyName, turn) LEFT JOIN StarshipPlant USING (type, celestialBodyName, turn) LEFT JOIN SpaceCounter USING (type, celestialBodyName, turn) LEFT JOIN PulsarLaunchingPad USING (type, celestialBodyName, turn)%s ;", (where != null && !where.isEmpty()) ? " WHERE "+where : ""));
			while(stmnt.step())
			{
				eBuildingType type = eBuildingType.valueOf(stmnt.columnString(0));
				boolean isVersioned = (!stmnt.columnString(1).isEmpty());
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


	public static <T extends IBuilding> void insertOrUpdate(SQLiteConnection conn, T building) throws SQLiteDBException
	{
		try
		{
			IBuilding vbuilding = (IBuilding.class.isInstance(building) ? IBuilding.class.cast(building) : null);
			SQLiteStatement stmnt = conn.prepare(String.format("SELECT EXISTS ( SELECT type FROM Building WHERE type = %s AND celestialBodyName = %s AND turn = %s) AS exist ;", "'"+building.getType()+"'", "'"+building.getCelestialBodyName()+"'", "'"+building.getTurn()+"'"));
			stmnt.step();
			if (stmnt.columnInt(0) == 0)
			{
				conn.exec(String.format("INSERT INTO Building (type, celestialBodyName, turn, nbSlots) VALUES (%s, %s, %s, %s);", "'"+building.getType()+"'", "'"+building.getCelestialBodyName()+"'", "'"+building.getTurn()+"'", "'"+building.getNbSlots()+"'"));
				switch(building.getType())
				{
					case PulsarLaunchingPad:
					{
						IPulsarLaunchingPad pulsarLaunchingPad = IPulsarLaunchingPad.class.cast(building);
						conn.exec(String.format("INSERT INTO PulsarLaunchingPad (type, celestialBodyName, turn, firedDate) VALUES (%s, %s, %s, %s);", "'"+pulsarLaunchingPad.getType()+"'", "'"+pulsarLaunchingPad.getCelestialBodyName()+"'", "'"+pulsarLaunchingPad.getTurn()+"'", "'"+pulsarLaunchingPad.getFiredDate()+"'"));
						break;
					}
					case SpaceCounter:
					{
						ISpaceCounter spaceCounter = ISpaceCounter.class.cast(building);
						conn.exec(String.format("INSERT INTO SpaceCounter (type, celestialBodyName, turn) VALUES (%s, %s, %s);", "'"+spaceCounter.getType()+"'", "'"+spaceCounter.getCelestialBodyName()+"'", "'"+spaceCounter.getTurn()+"'"));
						break;
					}
					case GovernmentModule:
					{
						IGovernmentModule governmentModule = IGovernmentModule.class.cast(building);
						conn.exec(String.format("INSERT INTO GovernmentModule (type, celestialBodyName, turn) VALUES (%s, %s, %s);", "'"+governmentModule.getType()+"'", "'"+governmentModule.getCelestialBodyName()+"'", "'"+governmentModule.getTurn()+"'"));
						break;
					}
					case DefenseModule:
					{
						IDefenseModule defenseModule = IDefenseModule.class.cast(building);
						conn.exec(String.format("INSERT INTO DefenseModule (type, celestialBodyName, turn) VALUES (%s, %s, %s);", "'"+defenseModule.getType()+"'", "'"+defenseModule.getCelestialBodyName()+"'", "'"+defenseModule.getTurn()+"'"));
						break;
					}
					case StarshipPlant:
					{
						IStarshipPlant starshipPlant = IStarshipPlant.class.cast(building);
						conn.exec(String.format("INSERT INTO StarshipPlant (type, celestialBodyName, turn) VALUES (%s, %s, %s);", "'"+starshipPlant.getType()+"'", "'"+starshipPlant.getCelestialBodyName()+"'", "'"+starshipPlant.getTurn()+"'"));
						break;
					}
					case ExtractionModule:
					{
						IExtractionModule extractionModule = IExtractionModule.class.cast(building);
						conn.exec(String.format("INSERT INTO ExtractionModule (type, celestialBodyName, turn) VALUES (%s, %s, %s);", "'"+extractionModule.getType()+"'", "'"+extractionModule.getCelestialBodyName()+"'", "'"+extractionModule.getTurn()+"'"));
						break;
					}
				}
			}
			else
			{
				conn.exec(String.format("UPDATE Building SET  nbSlots = %s WHERE  type = %s AND celestialBodyName = %s AND turn = %s ;", "'"+building.getNbSlots()+"'", "'"+building.getType()+"'", "'"+building.getCelestialBodyName()+"'", "'"+building.getTurn()+"'"));
				switch(building.getType())
				{
					case PulsarLaunchingPad:
					{
						IPulsarLaunchingPad pulsarLaunchingPad = IPulsarLaunchingPad.class.cast(building);
						conn.exec(String.format("UPDATE PulsarLaunchingPad SET  firedDate = %s WHERE  type = %s AND celestialBodyName = %s AND turn = %s ;", "'"+pulsarLaunchingPad.getFiredDate()+"'", "'"+pulsarLaunchingPad.getType()+"'", "'"+pulsarLaunchingPad.getCelestialBodyName()+"'", "'"+pulsarLaunchingPad.getTurn()+"'"));
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
