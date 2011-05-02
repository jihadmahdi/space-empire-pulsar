package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.IAsteroidField;
import org.axan.sep.server.model.orm.IVersionedProductiveCelestialBody;
import org.axan.sep.server.model.orm.base.IBaseVersionedAsteroidField;
import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import com.almworks.sqlite4java.SQLiteStatement;

public interface IVersionedAsteroidField extends IAsteroidField, IVersionedProductiveCelestialBody
{
}
