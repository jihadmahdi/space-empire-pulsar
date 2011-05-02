package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.IVersionedUnit;
import org.axan.sep.server.model.orm.IFleet;
import org.axan.sep.server.model.orm.base.IBaseVersionedFleet;
import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.Protocol.eUnitType;
import com.almworks.sqlite4java.SQLiteStatement;

public interface IVersionedFleet extends IVersionedUnit, IFleet
{
}
