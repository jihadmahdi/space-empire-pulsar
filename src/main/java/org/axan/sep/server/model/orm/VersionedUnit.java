package org.axan.sep.server.model.orm;

import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;
import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.RealLocation;
import org.axan.sep.server.model.SEPSQLiteDB;
import org.axan.sep.server.model.orm.Unit;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.server.model.orm.base.BaseVersionedUnit;

public class VersionedUnit extends Unit implements IVersionedUnit
{
	private final BaseVersionedUnit baseVersionedUnitProxy;

	public static IVersionedUnit create(eUnitType unitType, SQLiteStatement stmnt, IGameConfig config)
	{
		try
		{
			Class<? extends IVersionedUnit> vuClass = (Class<? extends IVersionedUnit>)  Class.forName(String.format("%s.Versioned%s", VersionedUnit.class.getPackage().getName(), unitType.toString()));
			return SQLiteORMGenerator.mapTo(vuClass, stmnt, IGameConfig.class.cast(config));
		}
		catch(Throwable t)
		{
			throw new RuntimeException(t);
		}
	}
	
	public VersionedUnit(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		super(stmnt, config);
		this.baseVersionedUnitProxy = new BaseVersionedUnit(stmnt);
	}
	
	private RealLocation departure; 
	@Override
	public RealLocation getDeparture()
	{
		if (departure == null)
		{
			if (baseVersionedUnitProxy.getDeparture_x() == null || baseVersionedUnitProxy.getDeparture_y() == null || baseVersionedUnitProxy.getDeparture_z() == null) return null;
			departure = new RealLocation(baseVersionedUnitProxy.getDeparture_x(), baseVersionedUnitProxy.getDeparture_y(), baseVersionedUnitProxy.getDeparture_z());			
		}
		
		return departure;
	}
	
	private RealLocation destination; 
	@Override
	public RealLocation getDestination()
	{
		if (departure == null)
		{
			if (baseVersionedUnitProxy.getDestination_x() == null || baseVersionedUnitProxy.getDestination_y() == null || baseVersionedUnitProxy.getDestination_z() == null) return null;
			destination = new RealLocation(baseVersionedUnitProxy.getDestination_x(), baseVersionedUnitProxy.getDestination_y(), baseVersionedUnitProxy.getDestination_z());			
		}
		
		return destination;
	}

	public Double getProgress()
	{
		return baseVersionedUnitProxy.getProgress();
	}

	public Integer getTurn()
	{
		return baseVersionedUnitProxy.getTurn();
	}

}
