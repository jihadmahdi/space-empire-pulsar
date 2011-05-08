package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.ICelestialBody;
import org.axan.sep.common.db.sqlite.orm.base.IBaseVortex;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.SEPUtils.Location;

public interface IVortex extends ICelestialBody
{
	public Integer getOnsetDate();
	public Integer getEndDate();
	public String getDestination();
}
