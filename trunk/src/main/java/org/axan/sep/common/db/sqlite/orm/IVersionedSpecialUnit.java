package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.ISpecialUnit;
import org.axan.sep.common.db.sqlite.orm.base.IBaseVersionedSpecialUnit;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.Protocol.eSpecialUnitType;

public interface IVersionedSpecialUnit extends ISpecialUnit
{
	public Integer getTurn();
	public String getFleetOwner();
	public String getFleetName();
	public Integer getFleetTurn();
}
