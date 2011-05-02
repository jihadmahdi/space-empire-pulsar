package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.IVersionedProductiveCelestialBody;
import org.axan.sep.server.model.orm.IPlanet;
import org.axan.sep.server.model.orm.base.IBaseVersionedPlanet;
import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import com.almworks.sqlite4java.SQLiteStatement;

public interface IVersionedPlanet extends IVersionedProductiveCelestialBody, IPlanet
{
	public Integer getCurrentPopulation();
}
