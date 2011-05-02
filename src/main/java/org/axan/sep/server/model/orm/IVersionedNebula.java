package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.IVersionedProductiveCelestialBody;
import org.axan.sep.server.model.orm.INebula;
import org.axan.sep.server.model.orm.base.IBaseVersionedNebula;
import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import com.almworks.sqlite4java.SQLiteStatement;

public interface IVersionedNebula extends IVersionedProductiveCelestialBody, INebula
{
}
