package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.IVersionedProductiveCelestialBody;
import org.axan.sep.common.db.sqlite.orm.INebula;
import org.axan.sep.common.db.sqlite.orm.base.IBaseVersionedNebula;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.SEPUtils.Location;

public interface IVersionedNebula extends IVersionedProductiveCelestialBody, INebula
{
}
