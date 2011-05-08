package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.IProbe;
import org.axan.sep.common.db.sqlite.orm.IVersionedUnit;
import org.axan.sep.common.db.sqlite.orm.base.IBaseVersionedProbe;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.Location;

public interface IVersionedProbe extends IProbe, IVersionedUnit
{
	boolean isDeployed();
}
