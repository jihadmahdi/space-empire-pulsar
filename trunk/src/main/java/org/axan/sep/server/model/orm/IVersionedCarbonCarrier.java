package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.ICarbonCarrier;
import org.axan.sep.server.model.orm.IVersionedUnit;
import org.axan.sep.server.model.orm.base.IBaseVersionedCarbonCarrier;
import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.Protocol.eUnitType;
import com.almworks.sqlite4java.SQLiteStatement;

public interface IVersionedCarbonCarrier extends ICarbonCarrier, IVersionedUnit
{
	public String getOrderOwner();
	public String getOrderSource();
	public Integer getOrderPriority();
}
