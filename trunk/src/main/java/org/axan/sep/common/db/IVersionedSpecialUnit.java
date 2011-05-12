package org.axan.sep.common.db;

import org.axan.sep.common.db.ISpecialUnit;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.common.Protocol.eSpecialUnitType;
import org.axan.sep.common.db.IGameConfig;

public interface IVersionedSpecialUnit extends ISpecialUnit
{
	public Integer getTurn();
	public String getFleetOwner();
	public String getFleetName();
	public Integer getFleetTurn();
}
