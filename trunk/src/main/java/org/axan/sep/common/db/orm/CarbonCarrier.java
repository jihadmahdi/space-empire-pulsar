package org.axan.sep.common.db.orm;

import org.axan.sep.common.db.orm.Unit;
import java.lang.Exception;
import org.axan.sep.common.db.orm.base.IBaseCarbonCarrier;
import org.axan.sep.common.db.orm.base.BaseCarbonCarrier;
import org.axan.sep.common.db.ICarbonCarrier;
import org.axan.eplib.orm.ISQLDataBaseStatement;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.db.IGameConfig;

public class CarbonCarrier extends Unit implements ICarbonCarrier
{
	private final IBaseCarbonCarrier baseCarbonCarrierProxy;

	CarbonCarrier(IBaseCarbonCarrier baseCarbonCarrierProxy, IGameConfig config)
	{
		super(baseCarbonCarrierProxy, config);
		this.baseCarbonCarrierProxy = baseCarbonCarrierProxy;
	}

	public CarbonCarrier(String owner, String name, eUnitType type, String sourceType, String sourceCelestialBodyName, Integer sourceTurn, IGameConfig config)
	{
		this(new BaseCarbonCarrier(owner, name, type.toString(), sourceType, sourceCelestialBodyName, sourceTurn), config);
	}

	public CarbonCarrier(ISQLDataBaseStatement stmnt, IGameConfig config) throws Exception
	{
		this(new BaseCarbonCarrier(stmnt), config);
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
