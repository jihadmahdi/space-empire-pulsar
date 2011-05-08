package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.IProductiveCelestialBody;
import org.axan.sep.common.db.sqlite.orm.base.IBaseVersionedProductiveCelestialBody;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.SEPUtils.Location;

public interface IVersionedProductiveCelestialBody extends IProductiveCelestialBody
{
	public Integer getTurn();
	public String getOwner();
	public Integer getCarbonStock();
	public Integer getCurrentCarbon();
}
