package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.IVersionedUnit;
import org.axan.sep.common.db.sqlite.orm.ISpaceRoadDeliverer;
import org.axan.sep.common.db.sqlite.orm.base.IBaseVersionedSpaceRoadDeliverer;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.Location;

public interface IVersionedSpaceRoadDeliverer extends IVersionedUnit, ISpaceRoadDeliverer
{
}
