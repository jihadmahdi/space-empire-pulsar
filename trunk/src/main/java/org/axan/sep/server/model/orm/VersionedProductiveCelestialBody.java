package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.ProductiveCelestialBody;
import org.axan.sep.server.model.orm.base.BaseVersionedProductiveCelestialBody;
import org.axan.sep.common.IGameConfig;
import com.almworks.sqlite4java.SQLiteStatement;

public class VersionedProductiveCelestialBody extends ProductiveCelestialBody implements IVersionedProductiveCelestialBody
{
	private final BaseVersionedProductiveCelestialBody baseVersionedProductiveCelestialBodyProxy;

	public VersionedProductiveCelestialBody(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		super(stmnt, config);
		this.baseVersionedProductiveCelestialBodyProxy = new BaseVersionedProductiveCelestialBody(stmnt);
	}

	public Integer getCurrentCarbon()
	{
		return baseVersionedProductiveCelestialBodyProxy.getCurrentCarbon();
	}

	public String getOwner()
	{
		return baseVersionedProductiveCelestialBodyProxy.getOwner();
	}

	public Integer getCarbonStock()
	{
		return baseVersionedProductiveCelestialBodyProxy.getCarbonStock();
	}

	public Integer getTurn()
	{
		return baseVersionedProductiveCelestialBodyProxy.getTurn();
	}

}
