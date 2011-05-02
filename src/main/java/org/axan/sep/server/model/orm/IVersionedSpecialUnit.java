package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.ISpecialUnit;
import org.axan.sep.server.model.orm.base.IBaseVersionedSpecialUnit;
import org.axan.sep.common.IGameConfig;
import com.almworks.sqlite4java.SQLiteStatement;

public interface IVersionedSpecialUnit extends ISpecialUnit
{
	public String getFleetName();
	public Integer getFleetTurn();
	public String getFleetOwner();
	public Integer getTurn();
}
