package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.CarbonCarrier;
import org.axan.sep.common.db.sqlite.orm.VersionedUnit;
import org.axan.sep.common.db.sqlite.orm.base.BaseVersionedCarbonCarrier;
import org.axan.sep.common.IGameConfig;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.Location;

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
	
	@Override
	public float getSight()
	{
		return versionedUnitProxy.getSight();
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

	public String getName()
	{
		return carbonCarrierProxy.getName();
	}

	public Location getDeparture()
	{
		return versionedUnitProxy.getDeparture();
	}

	public Double getProgress()
	{
		return versionedUnitProxy.getProgress();
	}

	public Location getDestination()
	{
		return versionedUnitProxy.getDestination();
	}

	public Integer getTurn()
	{
		return versionedUnitProxy.getTurn();
	}

}
