package org.axan.sep.common.db.orm;

import org.axan.eplib.orm.ISQLDataBaseStatement;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.ICarbonCarrier;
import org.axan.sep.common.db.IGameConfig;
import org.axan.sep.common.db.orm.base.BaseCarbonCarrier;
import org.axan.sep.common.db.orm.base.IBaseCarbonCarrier;

public class CarbonCarrier extends Unit implements ICarbonCarrier
{
	private final IBaseCarbonCarrier baseCarbonCarrierProxy;

	CarbonCarrier(IBaseCarbonCarrier baseCarbonCarrierProxy, IGameConfig config)
	{
		super(baseCarbonCarrierProxy, config);
		this.baseCarbonCarrierProxy = baseCarbonCarrierProxy;
	}

	public CarbonCarrier(String owner, String name, eUnitType type, Location departure, Double progress, Location destination, String sourceType, String sourceCelestialBodyName, Integer sourceTurn, String orderOwner, String orderSource, Integer orderPriority, IGameConfig config)
	{
		this(new BaseCarbonCarrier(owner, name, type.toString(), departure == null ? null : departure.x, departure == null ? null : departure.y, departure == null ? null : departure.z, progress, destination == null ? null : destination.x, destination == null ? null : destination.y, destination == null ? null : destination.z, sourceType, sourceCelestialBodyName, sourceTurn, orderOwner, orderSource, orderPriority), config);
	}

	public CarbonCarrier(ISQLDataBaseStatement stmnt, IGameConfig config) throws Exception
	{
		this(new BaseCarbonCarrier(stmnt), config);
	}

	@Override
	public String getSourceType()
	{
		return baseCarbonCarrierProxy.getSourceType();
	}

	@Override
	public String getSourceCelestialBodyName()
	{
		return baseCarbonCarrierProxy.getSourceCelestialBodyName();
	}

	@Override
	public Integer getSourceTurn()
	{
		return baseCarbonCarrierProxy.getSourceTurn();
	}

	@Override
	public String getOrderOwner()
	{
		return baseCarbonCarrierProxy.getOrderOwner();
	}

	@Override
	public String getOrderSource()
	{
		return baseCarbonCarrierProxy.getOrderSource();
	}

	@Override
	public Integer getOrderPriority()
	{
		return baseCarbonCarrierProxy.getOrderPriority();
	}

}
