package org.axan.sep.common.db;

import org.axan.sep.common.db.IVersionedUnit;
import org.axan.sep.common.db.ISpaceRoadDeliverer;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.IGameConfig;

public interface IVersionedSpaceRoadDeliverer extends IVersionedUnit, ISpaceRoadDeliverer
{
}
