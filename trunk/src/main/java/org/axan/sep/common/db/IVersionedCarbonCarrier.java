package org.axan.sep.common.db;

import org.axan.sep.common.db.ICarbonCarrier;
import org.axan.sep.common.db.IVersionedUnit;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.IGameConfig;

public interface IVersionedCarbonCarrier extends ICarbonCarrier, IVersionedUnit
{
	public String getOrderOwner();
	public String getOrderSource();
	public Integer getOrderPriority();
}
