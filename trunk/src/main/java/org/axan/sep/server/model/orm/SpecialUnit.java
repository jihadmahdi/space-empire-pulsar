package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.base.BaseSpecialUnit;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import org.axan.sep.common.IGameConfig;
import java.util.Set;
import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;
import org.axan.sep.common.Protocol.eSpecialUnitType;
import java.util.HashSet;
import com.almworks.sqlite4java.SQLiteStatement;
import com.almworks.sqlite4java.SQLiteConnection;

public class SpecialUnit implements ISpecialUnit
{
	private final BaseSpecialUnit baseSpecialUnitProxy;

	public SpecialUnit(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		this.baseSpecialUnitProxy = new BaseSpecialUnit(stmnt);
	}

	public String getOwner()
	{
		return baseSpecialUnitProxy.getOwner();
	}

	public eSpecialUnitType getType()
	{
		return eSpecialUnitType.valueOf(baseSpecialUnitProxy.getType());
	}

	public String getName()
	{
		return baseSpecialUnitProxy.getName();
	}

	public static <T extends ISpecialUnit> Set<T> select(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, boolean lastVersion, String where, Object ... params) throws SQLiteDBException
	{
		try
		{
			Set<T> results = new HashSet<T>();

			if (where != null && params != null) where = String.format(where, params);
			if (lastVersion)
			{
				where = String.format("%s(VersionedSpecialUnit.turn = ( SELECT MAX(LVVersionedSpecialUnit.turn) FROM VersionedSpecialUnit LVVersionedSpecialUnit WHERE LVVersionedSpecialUnit.owner = SpecialUnit.owner AND LVVersionedSpecialUnit.type = SpecialUnit.type AND LVVersionedSpecialUnit.name = SpecialUnit.name ))", (where != null && !where.isEmpty()) ? "("+where+") AND " : "");
			}
			SQLiteStatement stmnt = conn.prepare(String.format("SELECT SpecialUnit.type, VersionedSpecialUnit.type, * FROM SpecialUnit LEFT JOIN VersionedSpecialUnit USING (owner, type, name) LEFT JOIN Hero USING (owner, type, name)%s ;", (where != null && !where.isEmpty()) ? " WHERE "+where : ""));
			while(stmnt.step())
			{
				eSpecialUnitType type = eSpecialUnitType.valueOf(stmnt.columnString(0));
				boolean isVersioned = (!stmnt.columnString(1).isEmpty());
				Class<? extends ISpecialUnit> clazz = (Class<? extends ISpecialUnit>)  Class.forName(String.format("%s.%s%s", SpecialUnit.class.getPackage().getName(), isVersioned ? "Versioned" : "", type.toString()));
				ISpecialUnit o = SQLiteORMGenerator.mapTo(clazz, stmnt, config);
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
