package org.axan.sep.server.model.orm;

import org.axan.sep.common.IGameConfig;
import org.axan.sep.server.model.orm.Unit;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.server.model.orm.base.BaseCarbonCarrier;

public class CarbonCarrier extends Unit implements ICarbonCarrier
{
	private final BaseCarbonCarrier baseCarbonCarrierProxy;

	public CarbonCarrier(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		super(stmnt, config);
		this.baseCarbonCarrierProxy = new BaseCarbonCarrier(stmnt);
	}

	public String getSourceCelestialBodyName()
	{
		return baseCarbonCarrierProxy.getSourceCelestialBodyName();
	}

	public Integer getSourceTurn()
	{
		return baseCarbonCarrierProxy.getSourceTurn();
	}

	public String getSourceType()
	{
		return baseCarbonCarrierProxy.getSourceType();
	}

}
