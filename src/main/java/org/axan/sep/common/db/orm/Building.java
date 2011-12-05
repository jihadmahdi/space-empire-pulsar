package org.axan.sep.common.db.orm;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.axan.eplib.orm.DataBaseORMGenerator;
import org.axan.eplib.orm.ISQLDataBaseStatement;
import org.axan.eplib.orm.ISQLDataBaseStatementJob;
import org.axan.eplib.orm.SQLDataBaseException;
import org.axan.sep.common.Protocol.eBuildingType;
import org.axan.sep.common.db.IBuilding;
import org.axan.sep.common.db.IDefenseModule;
import org.axan.sep.common.db.IExtractionModule;
import org.axan.sep.common.db.IGovernmentModule;
import org.axan.sep.common.db.IPulsarLaunchingPad;
import org.axan.sep.common.db.ISpaceCounter;
import org.axan.sep.common.db.IStarshipPlant;
import org.axan.sep.common.db.SEPCommonDB;
import org.axan.sep.common.db.orm.base.BaseBuilding;
import org.axan.sep.common.db.orm.base.IBaseBuilding;

public class Building implements IBuilding
{
	private final IBaseBuilding baseBuildingProxy;
	private final eBuildingType type;

	Building(IBaseBuilding baseBuildingProxy)
	{
		this.baseBuildingProxy = baseBuildingProxy;
		this.type = eBuildingType.valueOf(baseBuildingProxy.getType());
	}

	public Building(eBuildingType type, String celestialBodyName, Integer turn, Integer nbSlots)
	{
		this(new BaseBuilding(type.toString(), celestialBodyName, turn, nbSlots));
	}

	public Building(ISQLDataBaseStatement stmnt) throws Exception
	{
		this(new BaseBuilding(stmnt));
	}

	@Override
	public eBuildingType getType()
	{
		return type;
	}

	@Override
	public String getCelestialBodyName()
	{
		return baseBuildingProxy.getCelestialBodyName();
	}

	@Override
	public Integer getTurn()
	{
		return baseBuildingProxy.getTurn();
	}

	@Override
	public Integer getNbSlots()
	{
		return baseBuildingProxy.getNbSlots();
	}

	public static <T extends IBuilding> T selectOne(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
	{
		Set<T> results = select(db, expectedType, from, (where==null?"":where+" ")+"LIMIT 1", params);
		if (results.isEmpty()) return null;
		return results.iterator().next();
	}

	public static <T extends IBuilding> Set<T> select(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
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
				results.add(DataBaseORMGenerator.mapTo(expectedType.isInterface() ? (Class<T>) Building.class : expectedType, stmnt));
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

	public static <T extends IBuilding> boolean exist(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
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


	private static <T extends IBuilding> String selectQuery(Class<T> expectedType, String from, String where, Object ... params)
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
		return String.format("SELECT Building.*, ExtractionModule.*, GovernmentModule.*, DefenseModule.*, StarshipPlant.*, SpaceCounter.*, PulsarLaunchingPad.* FROM Building%s LEFT JOIN ExtractionModule USING (type, celestialBodyName, turn) LEFT JOIN GovernmentModule USING (type, celestialBodyName, turn) LEFT JOIN DefenseModule USING (type, celestialBodyName, turn) LEFT JOIN StarshipPlant USING (type, celestialBodyName, turn) LEFT JOIN SpaceCounter USING (type, celestialBodyName, turn) LEFT JOIN PulsarLaunchingPad USING (type, celestialBodyName, turn)%s", (from != null && !from.isEmpty()) ? ", "+from : "", (where != null && !where.isEmpty()) ? " WHERE "+where : "");
	}

	public static <T extends IBuilding> void insertOrUpdate(SEPCommonDB db, T building) throws SQLDataBaseException
	{
		try
		{
			boolean exist = exist(db, building.getClass(), null, " Building.type = %s AND Building.celestialBodyName = %s AND Building.turn = %s", "'"+building.getType()+"'", "'"+building.getCelestialBodyName()+"'", "'"+building.getTurn()+"'");
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
				if (!exist) db.getDB().exec(String.format("INSERT INTO Building (type, celestialBodyName, turn, nbSlots) VALUES (%s, %s, %s, %s);", "'"+building.getType()+"'", "'"+building.getCelestialBodyName()+"'", "'"+building.getTurn()+"'", "'"+building.getNbSlots()+"'").replaceAll("'null'", "NULL"));
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
