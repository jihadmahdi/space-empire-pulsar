package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.base.IBaseUnitArrivalLog;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import org.axan.sep.common.IGameConfig;
import java.util.Set;
import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;
import java.util.HashSet;
import com.almworks.sqlite4java.SQLiteStatement;
import com.almworks.sqlite4java.SQLiteConnection;

public interface IUnitArrivalLog
{
	public String getUnitType();
	public Integer getUnitTurn();
	public String getDestination();
	public String getUnitName();
	public Integer getInstantTime();
	public String getUnitOwner();
	public String getVortex();
}
