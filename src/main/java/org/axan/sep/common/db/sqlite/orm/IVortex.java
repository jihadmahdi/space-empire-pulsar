package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.ICelestialBody;
import org.axan.sep.common.db.sqlite.orm.base.IBaseVortex;
import org.axan.sep.common.IGameConfig;
import com.almworks.sqlite4java.SQLiteStatement;

public interface IVortex extends ICelestialBody
{
	public Integer getOnsetDate();
	public Integer getEndDate();
	public String getDestination();
}
