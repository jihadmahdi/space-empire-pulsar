package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.ICelestialBody;
import org.axan.sep.common.db.sqlite.orm.base.IBaseProductiveCelestialBody;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.SEPUtils.Location;

public interface IProductiveCelestialBody extends ICelestialBody
{
	public Integer getInitialCarbonStock();
	public Integer getMaxSlots();
}
