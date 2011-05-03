package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.IProductiveCelestialBody;
import org.axan.sep.common.db.sqlite.orm.base.IBasePlanet;
import org.axan.sep.common.IGameConfig;
import com.almworks.sqlite4java.SQLiteStatement;

public interface IPlanet extends IProductiveCelestialBody
{
	public Integer getMaxPopulation();
	public Integer getPopulationPerTurn();
}
