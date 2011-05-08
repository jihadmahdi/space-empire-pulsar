package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.Unit;
import org.axan.sep.common.db.sqlite.orm.base.BaseCarbonCarrier;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.Protocol.eUnitType;

public class CarbonCarrier extends Unit implements ICarbonCarrier
{
	private final BaseCarbonCarrier baseCarbonCarrierProxy;

	public CarbonCarrier(String owner, String name, eUnitType type, String sourceType, String sourceCelestialBodyName, Integer sourceTurn, float sight)
	{
		super(owner, name, type, sight);
		baseCarbonCarrierProxy = new BaseCarbonCarrier(owner, name, type.toString(), sourceType, sourceCelestialBodyName, sourceTurn);
	}

	public CarbonCarrier(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		super(stmnt, config);
		this.baseCarbonCarrierProxy = new BaseCarbonCarrier(stmnt);
	}

	public String getSourceType()
	{
		return baseCarbonCarrierProxy.getSourceType();
	}

	public String getSourceCelestialBodyName()
	{
		return baseCarbonCarrierProxy.getSourceCelestialBodyName();
	}

	public Integer getSourceTurn()
	{
		return baseCarbonCarrierProxy.getSourceTurn();
	}

}
