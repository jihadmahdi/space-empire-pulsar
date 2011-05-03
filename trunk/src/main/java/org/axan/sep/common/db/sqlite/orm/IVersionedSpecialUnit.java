package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.ISpecialUnit;
import org.axan.sep.common.db.sqlite.orm.base.IBaseVersionedSpecialUnit;
import org.axan.sep.common.IGameConfig;
import com.almworks.sqlite4java.SQLiteStatement;

public interface IVersionedSpecialUnit extends ISpecialUnit
{
	public String getFleetName();
	public Integer getFleetTurn();
	public String getFleetOwner();
	public Integer getTurn();
}
