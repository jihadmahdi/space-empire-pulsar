package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.ProductiveCelestialBody;
import org.axan.sep.common.db.sqlite.orm.base.BasePlanet;
import org.axan.sep.common.IGameConfig;
import com.almworks.sqlite4java.SQLiteStatement;

public class Planet extends ProductiveCelestialBody implements IPlanet
{
	private final BasePlanet basePlanetProxy;

	public Planet(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		super(stmnt, config);
		this.basePlanetProxy = new BasePlanet(stmnt);
	}

	public Integer getMaxPopulation()
	{
		return basePlanetProxy.getMaxPopulation();
	}

	public Integer getPopulationPerTurn()
	{
		return basePlanetProxy.getPopulationPerTurn();
	}

}
