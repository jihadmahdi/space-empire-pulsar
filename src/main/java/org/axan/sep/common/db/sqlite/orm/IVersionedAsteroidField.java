package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.IAsteroidField;
import org.axan.sep.common.db.sqlite.orm.IVersionedProductiveCelestialBody;
import org.axan.sep.common.db.sqlite.orm.base.IBaseVersionedAsteroidField;
import org.axan.sep.common.IGameConfig;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.SEPUtils.Location;

public interface IVersionedAsteroidField extends IAsteroidField, IVersionedProductiveCelestialBody
{
}
