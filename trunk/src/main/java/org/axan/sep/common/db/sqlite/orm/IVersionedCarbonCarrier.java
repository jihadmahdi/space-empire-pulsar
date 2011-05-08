package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.ICarbonCarrier;
import org.axan.sep.common.db.sqlite.orm.IVersionedUnit;
import org.axan.sep.common.db.sqlite.orm.base.IBaseVersionedCarbonCarrier;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.Location;

public interface IVersionedCarbonCarrier extends ICarbonCarrier, IVersionedUnit
{
	public String getOrderOwner();
	public String getOrderSource();
	public Integer getOrderPriority();
}
