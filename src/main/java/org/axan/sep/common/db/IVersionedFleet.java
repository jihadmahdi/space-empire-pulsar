package org.axan.sep.common.db;

import org.axan.sep.common.db.IVersionedUnit;
import org.axan.sep.common.db.IFleet;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.IGameConfig;

public interface IVersionedFleet extends IVersionedUnit, IFleet
{
}
