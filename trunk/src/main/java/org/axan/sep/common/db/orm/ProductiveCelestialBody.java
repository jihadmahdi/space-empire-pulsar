package org.axan.sep.common.db.orm;

import org.axan.sep.common.db.orm.CelestialBody;

import java.io.Serializable;
import java.lang.Exception;
import org.axan.sep.common.db.orm.base.IBaseProductiveCelestialBody;
import org.axan.sep.common.db.orm.base.BaseProductiveCelestialBody;
import org.axan.sep.common.db.IProductiveCelestialBody;
import java.util.HashMap;
import java.util.Map;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.IGameConfig;
import org.neo4j.graphdb.Node;

public class ProductiveCelestialBody extends CelestialBody implements IProductiveCelestialBody, Serializable
{
	private final IBaseProductiveCelestialBody baseProductiveCelestialBodyProxy;

	ProductiveCelestialBody(IBaseProductiveCelestialBody baseProductiveCelestialBodyProxy)
	{
		super(baseProductiveCelestialBodyProxy);
		this.baseProductiveCelestialBodyProxy = baseProductiveCelestialBodyProxy;
	}

	public ProductiveCelestialBody(String name, eCelestialBodyType type, Location location, Integer initialCarbonStock, Integer maxSlots, String owner, Integer carbonStock, Integer currentCarbon)
	{
		this(new BaseProductiveCelestialBody(name, type.toString(), location == null ? null : location.x, location == null ? null : location.y, location == null ? null : location.z, initialCarbonStock, maxSlots, owner, carbonStock, currentCarbon));
	}

	public ProductiveCelestialBody(Node stmnt) throws Exception
	{
		this(new BaseProductiveCelestialBody(stmnt));
	}

	@Override
	public Integer getInitialCarbonStock()
	{
		return baseProductiveCelestialBodyProxy.getInitialCarbonStock();
	}

	@Override
	public Integer getMaxSlots()
	{
		return baseProductiveCelestialBodyProxy.getMaxSlots();
	}

	@Override
	public String getOwner()
	{
		return baseProductiveCelestialBodyProxy.getOwner();
	}

	@Override
	public Integer getCarbonStock()
	{
		return baseProductiveCelestialBodyProxy.getCarbonStock();
	}

	@Override
	public Integer getCurrentCarbon()
	{
		return baseProductiveCelestialBodyProxy.getCurrentCarbon();
	}

	@Override
	public Map<String, Object> getNode()
	{
		return baseProductiveCelestialBodyProxy.getNode();
	}

}
