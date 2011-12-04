package org.axan.sep.common.db.orm;

import java.lang.Exception;
import org.axan.sep.common.db.orm.base.IBaseVersionedCarbonCarrier;
import org.axan.sep.common.db.orm.base.BaseVersionedCarbonCarrier;
import org.axan.sep.common.db.IVersionedCarbonCarrier;
import org.axan.eplib.orm.ISQLDataBaseStatement;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.IGameConfig;

public class VersionedCarbonCarrier extends VersionedUnit implements IVersionedCarbonCarrier
{
	private final IBaseVersionedCarbonCarrier baseVersionedCarbonCarrierProxy;

	VersionedCarbonCarrier(IBaseVersionedCarbonCarrier baseVersionedCarbonCarrierProxy, IGameConfig config)
	{
		super(baseVersionedCarbonCarrierProxy, config);
		this.baseVersionedCarbonCarrierProxy = baseVersionedCarbonCarrierProxy;
	}

	public VersionedCarbonCarrier(String owner, String name, eUnitType type, String sourceType, String sourceCelestialBodyName, Integer sourceTurn, Integer turn, Location departure, Double progress, Location destination, String orderOwner, String orderSource, Integer orderPriority, IGameConfig config)
	{
		this(new BaseVersionedCarbonCarrier(owner, name, type.toString(), sourceType, sourceCelestialBodyName, sourceTurn, turn, departure == null ? null : departure.x, departure == null ? null : departure.y, departure == null ? null : departure.z, progress, destination == null ? null : destination.x, destination == null ? null : destination.y, destination == null ? null : destination.z, orderOwner, orderSource, orderPriority), config);
	}

	public VersionedCarbonCarrier(ISQLDataBaseStatement stmnt, IGameConfig config) throws Exception
	{
		this(new BaseVersionedCarbonCarrier(stmnt), config);
	}

	public String getOrderOwner()
	{
		return baseVersionedCarbonCarrierProxy.getOrderOwner();
	}

	public String getOrderSource()
	{
		return baseVersionedCarbonCarrierProxy.getOrderSource();
	}

	public Integer getOrderPriority()
	{
		return baseVersionedCarbonCarrierProxy.getOrderPriority();
	}

	public String getSourceType()
	{
		return baseVersionedCarbonCarrierProxy.getSourceType();
	}

	public String getSourceCelestialBodyName()
	{
		return baseVersionedCarbonCarrierProxy.getSourceCelestialBodyName();
	}

	public Integer getSourceTurn()
	{
		return baseVersionedCarbonCarrierProxy.getSourceTurn();
	}

}
