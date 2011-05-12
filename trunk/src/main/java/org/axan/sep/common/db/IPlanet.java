package org.axan.sep.common.db;

import org.axan.sep.common.db.IProductiveCelestialBody;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.IGameConfig;

public interface IPlanet extends IProductiveCelestialBody
{
	public Integer getPopulationPerTurn();
	public Integer getMaxPopulation();
}
