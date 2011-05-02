package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.IVersionedUnit;
import org.axan.sep.server.model.orm.ISpaceRoadDeliverer;
import org.axan.sep.server.model.orm.base.IBaseVersionedSpaceRoadDeliverer;
import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.Protocol.eUnitType;
import com.almworks.sqlite4java.SQLiteStatement;

public interface IVersionedSpaceRoadDeliverer extends IVersionedUnit, ISpaceRoadDeliverer
{
}
