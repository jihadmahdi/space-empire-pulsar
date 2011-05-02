package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.ICelestialBody;
import org.axan.sep.server.model.orm.base.IBaseProductiveCelestialBody;
import org.axan.sep.common.IGameConfig;
import com.almworks.sqlite4java.SQLiteStatement;

public interface IProductiveCelestialBody extends ICelestialBody
{
	public Integer getInitialCarbonStock();
	public Integer getMaxSlots();
}
