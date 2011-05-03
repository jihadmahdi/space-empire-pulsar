package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.IProductiveCelestialBody;
import org.axan.sep.common.db.sqlite.orm.base.IBaseVersionedProductiveCelestialBody;
import org.axan.sep.common.IGameConfig;
import com.almworks.sqlite4java.SQLiteStatement;

public interface IVersionedProductiveCelestialBody extends IProductiveCelestialBody
{
	public Integer getCurrentCarbon();
	public String getOwner();
	public Integer getCarbonStock();
	public Integer getTurn();
}
