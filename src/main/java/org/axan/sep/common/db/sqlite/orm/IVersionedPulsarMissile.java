package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.IPulsarMissile;
import org.axan.sep.common.db.sqlite.orm.IVersionedUnit;
import org.axan.sep.common.db.sqlite.orm.base.IBaseVersionedPulsarMissile;
import org.axan.sep.common.IGameConfig;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.Location;

public interface IVersionedPulsarMissile extends IPulsarMissile, IVersionedUnit
{
	public Location getDirection();
}
