package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.IProductiveCelestialBody;
import org.axan.sep.server.model.orm.base.IBasePlanet;
import org.axan.sep.common.IGameConfig;
import com.almworks.sqlite4java.SQLiteStatement;

public interface IPlanet extends IProductiveCelestialBody
{
	public Integer getMaxPopulation();
	public Integer getPopulationPerTurn();
}
