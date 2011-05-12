package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.CarbonCarrier;
import org.axan.sep.common.db.sqlite.orm.VersionedUnit;
import org.axan.sep.common.db.sqlite.orm.base.BaseVersionedCarbonCarrier;
import org.axan.sep.common.db.IVersionedCarbonCarrier;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.IGameConfig;

public class VersionedCarbonCarrier implements IVersionedCarbonCarrier
{
	private final CarbonCarrier carbonCarrierProxy;
	private final VersionedUnit versionedUnitProxy;
	private final BaseVersionedCarbonCarrier baseVersionedCarbonCarrierProxy;

	public VersionedCarbonCarrier(String owner, String name, eUnitType type, String sourceType, String sourceCelestialBodyName, Integer sourceTurn, Integer turn, Location departure, Double progress, Location destination, String orderOwner, String orderSource, Integer orderPriority, float sight)
	{
		carbonCarrierProxy = new CarbonCarrier(owner, name, type, sourceType, sourceCelestialBodyName, sourceTurn, sight);
		versionedUnitProxy = new VersionedUnit(owner, name, type, turn, departure, progress, destination, sight);
		baseVersionedCarbonCarrierProxy = new BaseVersionedCarbonCarrier(owner, name, type.toString(), sourceType, sourceCelestialBodyName, sourceTurn, turn, departure == null ? null : departure.x, departure == null ? null : departure.y, departure == null ? null : departure.z, progress, destination == null ? null : destination.x, destination == null ? null : destination.y, destination == null ? null : destination.z, orderOwner, orderSource, orderPriority);
	}

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

	public String getSourceType()
	{
		return carbonCarrierProxy.getSourceType();
	}

	public String getSourceCelestialBodyName()
	{
		return carbonCarrierProxy.getSourceCelestialBodyName();
	}

	public Integer getSourceTurn()
	{
		return carbonCarrierProxy.getSourceTurn();
	}

	public String getOwner()
	{
		return carbonCarrierProxy.getOwner();
	}

	public String getName()
	{
		return carbonCarrierProxy.getName();
	}

	public eUnitType getType()
	{
		return carbonCarrierProxy.getType();
	}

	public Integer getTurn()
	{
		return versionedUnitProxy.getTurn();
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

}
