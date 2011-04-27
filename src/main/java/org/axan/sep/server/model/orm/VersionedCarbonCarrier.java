package org.axan.sep.server.model.orm;

import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.RealLocation;
import org.axan.sep.server.model.orm.CarbonCarrier;
import org.axan.sep.server.model.orm.VersionedUnit;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.server.model.orm.base.BaseVersionedCarbonCarrier;

public class VersionedCarbonCarrier implements IVersionedCarbonCarrier
{
	private final CarbonCarrier carbonCarrierProxy;
	private final VersionedUnit versionedUnitProxy;
	private final BaseVersionedCarbonCarrier baseVersionedCarbonCarrierProxy;

	public VersionedCarbonCarrier(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		this.carbonCarrierProxy = new CarbonCarrier(stmnt, config);
		this.versionedUnitProxy = new VersionedUnit(stmnt, config);
		this.baseVersionedCarbonCarrierProxy = new BaseVersionedCarbonCarrier(stmnt);
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

	public String getSourceCelestialBodyName()
	{
		return carbonCarrierProxy.getSourceCelestialBodyName();
	}

	public Integer getSourceTurn()
	{
		return carbonCarrierProxy.getSourceTurn();
	}

	public String getSourceType()
	{
		return carbonCarrierProxy.getSourceType();
	}

	public String getOwner()
	{
		return carbonCarrierProxy.getOwner();
	}

	public eUnitType getType()
	{
		return carbonCarrierProxy.getType();
	}
	
	@Override
	public float getSight()
	{
		return versionedUnitProxy.getSight();
	}

	@Override
	public float getSpeed()
	{
		return versionedUnitProxy.getSpeed();
	}

	public String getName()
	{
		return carbonCarrierProxy.getName();
	}

	public Double getProgress()
	{
		return versionedUnitProxy.getProgress();
	}

	@Override
	public RealLocation getDeparture()
	{
		return versionedUnitProxy.getDeparture();
	}
	
	@Override
	public RealLocation getDestination()
	{
		return versionedUnitProxy.getDestination();
	}

	public Integer getTurn()
	{
		return versionedUnitProxy.getTurn();
	}

}
