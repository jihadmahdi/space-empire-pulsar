package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.IProbe;
import org.axan.sep.server.model.orm.IVersionedUnit;
import org.axan.sep.server.model.orm.base.IBaseVersionedProbe;
import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.Protocol.eUnitType;
import com.almworks.sqlite4java.SQLiteStatement;

public interface IVersionedProbe extends IProbe, IVersionedUnit
{
	public boolean isDeployed();
}
