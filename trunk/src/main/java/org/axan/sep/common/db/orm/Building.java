package org.axan.sep.common.db.orm;

import java.lang.Exception;
import org.axan.sep.common.db.orm.base.IBaseBuilding;
import org.axan.sep.common.db.orm.base.BaseBuilding;
import org.axan.sep.common.db.IBuilding;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.axan.eplib.orm.DataBaseORMGenerator;
import org.axan.eplib.orm.ISQLDataBaseStatement;
import org.axan.eplib.orm.ISQLDataBaseStatementJob;
import org.axan.eplib.orm.SQLDataBaseException;
import org.axan.eplib.orm.sqlite.SQLiteDB;
import org.axan.sep.common.Protocol.eBuildingType;
import org.axan.sep.common.db.IDefenseModule;
import org.axan.sep.common.db.IExtractionModule;
import org.axan.sep.common.db.IGameConfig;
import org.axan.sep.common.db.IGovernmentModule;
import org.axan.sep.common.db.IPulsarLaunchingPad;
import org.axan.sep.common.db.ISpaceCounter;
import org.axan.sep.common.db.IStarshipPlant;
import org.axan.sep.common.db.SEPCommonDB;

public class Building implements IBuilding
{
	private final IBaseBuilding baseBuildingProxy;
	private final eBuildingType type;

	Building(IBaseBuilding baseBuildingProxy)
	{
		this.baseBuildingProxy = baseBuildingProxy;
		try
		{
			String stype = baseBuildingProxy.getType();
			if (stype == null || stype.length() < 2 || !Character.isUpperCase(stype.charAt(0)))
			{
				stype = stype;
			}
			this.type = eBuildingType.valueOf(baseBuildingProxy.getType());
		}
		catch(Throwable t)
		{
			throw new Error(t);
		}
	}

	public Building(eBuildingType type, String celestialBodyName, Integer turn, Integer nbSlots)
	{
		this(new BaseBuilding(type.toString(), celestialBodyName, turn, nbSlots));
	}

	public Building(ISQLDataBaseStatement stmnt) throws Exception
	{
		this(new BaseBuilding(stmnt));
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
	public static <T extends IBuilding> Set<T> selectMaxVersion(SEPCommonDB db, Class<T> expectedType, Integer maxVersion, String from, String where, Object ... params) throws SQLDataBaseException
	{
		return select(db, expectedType, true, maxVersion, from, where, params);
	}

	public static <T extends IBuilding> Set<T> selectVersion(SEPCommonDB db, Class<T> expectedType, int version, String from, String where, Object ... params) throws SQLDataBaseException
	{
		return select(db, expectedType, false, version, from, where, params);
	}

	public static <T extends IBuilding> Set<T> selectUnversioned(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
	{
		return select(db, expectedType, false, null, from, where, params);
	}

	private static <T extends IBuilding> Set<T> select(SEPCommonDB db, Class<T> expectedType, boolean maxVersion, Integer version, String from, String where, Object ... params) throws SQLDataBaseException
	{
		ISQLDataBaseStatement stmnt = null;
		try
		{
			Set<T> results = new HashSet<T>();
			stmnt = db.getDB().prepare(selectQuery(expectedType, maxVersion, version, from, where, params)+";", new ISQLDataBaseStatementJob<ISQLDataBaseStatement>()
			{
				@Override
				public ISQLDataBaseStatement job(ISQLDataBaseStatement stmnt) throws SQLDataBaseException
				{
					return stmnt;
				}
			});
			while(stmnt.step())
			{
				eBuildingType type;
				String stype;
				try
				{
					stype = stmnt.columnString(0);
					type = eBuildingType.valueOf(stype);
				}
				catch(Throwable t)
				{
					throw new Error(t);
				}
				String v = stmnt.columnString(1);
				if (v == null) throw new Error("Building with no Building !");
				boolean isVersioned = (!v.isEmpty());
				Class<? extends IBuilding> clazz = (Class<? extends IBuilding>)  Class.forName(String.format("%s.%s%s", Building.class.getPackage().getName(), "", type.toString()));
				IBuilding o = DataBaseORMGenerator.mapTo(clazz, stmnt);
				if (expectedType.isInstance(o))
				{
					results.add(expectedType.cast(o));
				}
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

	/** Set maxVersion to null to select last version. */
	public static <T extends IBuilding> boolean existMaxVersion(SEPCommonDB db, Class<T> expectedType, Integer maxVersion, String from, String where, Object ... params) throws SQLDataBaseException
	{
		return exist(db, expectedType, true, maxVersion, from, where, params);
	}

	public static <T extends IBuilding> boolean existVersion(SEPCommonDB db,Class<T> expectedType, int version, String from, String where, Object ... params) throws SQLDataBaseException
	{
		return exist(db, expectedType, false, version, from, where, params);
	}

	public static <T extends IBuilding> boolean existUnversioned(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
	{
		return exist(db, expectedType, false, null, from, where, params);
	}

	private static <T extends IBuilding> boolean exist(SEPCommonDB db, Class<T> expectedType, boolean maxVersion, Integer version, String from, String where, Object ... params) throws SQLDataBaseException
	{
		try
		{
			return db.getDB().prepare(selectQuery(expectedType, maxVersion, version, from, where, params)+" ;", new ISQLDataBaseStatementJob<Boolean>()
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

	public static <T extends IBuilding> void insertOrUpdate(SEPCommonDB db, T building) throws SQLDataBaseException
	{
		try
		{
			boolean exist = existUnversioned(db, building.getClass(), null, " Building.type = %s AND Building.celestialBodyName = %s AND Building.turn = %s", "'"+building.getType()+"'", "'"+building.getCelestialBodyName()+"'", "'"+building.getTurn()+"'");
			if (exist)
			{
				db.getDB().exec(String.format("UPDATE Building SET nbSlots = %s WHERE  Building.type = %s AND Building.celestialBodyName = %s AND Building.turn = %s ;", "'"+building.getNbSlots()+"'", "'"+building.getType()+"'", "'"+building.getCelestialBodyName()+"'", "'"+building.getTurn()+"'").replaceAll("'null'", "NULL"));
				switch(building.getType())
				{
					case PulsarLaunchingPad:
					{
						IPulsarLaunchingPad pulsarLaunchingPad = IPulsarLaunchingPad.class.cast(building);
						db.getDB().exec(String.format("UPDATE PulsarLaunchingPad SET firedDate = %s WHERE  PulsarLaunchingPad.type = %s AND PulsarLaunchingPad.celestialBodyName = %s AND PulsarLaunchingPad.turn = %s ;", "'"+pulsarLaunchingPad.getFiredDate()+"'", "'"+pulsarLaunchingPad.getType()+"'", "'"+pulsarLaunchingPad.getCelestialBodyName()+"'", "'"+pulsarLaunchingPad.getTurn()+"'").replaceAll("'null'", "NULL"));
						break;
					}
					case SpaceCounter:
					{
						ISpaceCounter spaceCounter = ISpaceCounter.class.cast(building);
						break;
					}
					case GovernmentModule:
					{
						IGovernmentModule governmentModule = IGovernmentModule.class.cast(building);
						break;
					}
					case DefenseModule:
					{
						IDefenseModule defenseModule = IDefenseModule.class.cast(building);
						break;
					}
					case StarshipPlant:
					{
						IStarshipPlant starshipPlant = IStarshipPlant.class.cast(building);
						break;
					}
					case ExtractionModule:
					{
						IExtractionModule extractionModule = IExtractionModule.class.cast(building);
						break;
					}
				}
			}
			else
			{
				try
				{
					if (!exist) db.getDB().exec(String.format("INSERT INTO Building (type, celestialBodyName, turn, nbSlots) VALUES (%s, %s, %s, %s);", "'"+building.getType()+"'", "'"+building.getCelestialBodyName()+"'", "'"+building.getTurn()+"'", "'"+building.getNbSlots()+"'").replaceAll("'null'", "NULL"));
				}
				catch(Throwable t)
				{
					throw new Error(t);
				}
				switch(building.getType())
				{
					case PulsarLaunchingPad:
					{
						IPulsarLaunchingPad pulsarLaunchingPad = IPulsarLaunchingPad.class.cast(building);
						if (!exist) db.getDB().exec(String.format("INSERT INTO PulsarLaunchingPad (type, celestialBodyName, turn, firedDate) VALUES (%s, %s, %s, %s);", "'"+pulsarLaunchingPad.getType()+"'", "'"+pulsarLaunchingPad.getCelestialBodyName()+"'", "'"+pulsarLaunchingPad.getTurn()+"'", "'"+pulsarLaunchingPad.getFiredDate()+"'").replaceAll("'null'", "NULL"));
						break;
					}
					case SpaceCounter:
					{
						ISpaceCounter spaceCounter = ISpaceCounter.class.cast(building);
						if (!exist) db.getDB().exec(String.format("INSERT INTO SpaceCounter (type, celestialBodyName, turn) VALUES (%s, %s, %s);", "'"+spaceCounter.getType()+"'", "'"+spaceCounter.getCelestialBodyName()+"'", "'"+spaceCounter.getTurn()+"'").replaceAll("'null'", "NULL"));
						break;
					}
					case GovernmentModule:
					{
						IGovernmentModule governmentModule = IGovernmentModule.class.cast(building);
						if (!exist) db.getDB().exec(String.format("INSERT INTO GovernmentModule (type, celestialBodyName, turn) VALUES (%s, %s, %s);", "'"+governmentModule.getType()+"'", "'"+governmentModule.getCelestialBodyName()+"'", "'"+governmentModule.getTurn()+"'").replaceAll("'null'", "NULL"));
						break;
					}
					case DefenseModule:
					{
						IDefenseModule defenseModule = IDefenseModule.class.cast(building);
						if (!exist) db.getDB().exec(String.format("INSERT INTO DefenseModule (type, celestialBodyName, turn) VALUES (%s, %s, %s);", "'"+defenseModule.getType()+"'", "'"+defenseModule.getCelestialBodyName()+"'", "'"+defenseModule.getTurn()+"'").replaceAll("'null'", "NULL"));
						break;
					}
					case StarshipPlant:
					{
						IStarshipPlant starshipPlant = IStarshipPlant.class.cast(building);
						if (!exist) db.getDB().exec(String.format("INSERT INTO StarshipPlant (type, celestialBodyName, turn) VALUES (%s, %s, %s);", "'"+starshipPlant.getType()+"'", "'"+starshipPlant.getCelestialBodyName()+"'", "'"+starshipPlant.getTurn()+"'").replaceAll("'null'", "NULL"));
						break;
					}
					case ExtractionModule:
					{
						IExtractionModule extractionModule = IExtractionModule.class.cast(building);
						if (!exist) db.getDB().exec(String.format("INSERT INTO ExtractionModule (type, celestialBodyName, turn) VALUES (%s, %s, %s);", "'"+extractionModule.getType()+"'", "'"+extractionModule.getCelestialBodyName()+"'", "'"+extractionModule.getTurn()+"'").replaceAll("'null'", "NULL"));
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
