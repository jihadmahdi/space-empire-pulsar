package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.base.IBaseUnitArrivalLog;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteStatement;
import java.util.HashSet;
import java.util.Set;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;
import org.axan.sep.common.IGameConfig;

public interface IUnitArrivalLog
{
	public String getUnitOwner();
	public String getUnitName();
	public Integer getUnitTurn();
	public String getUnitType();
	public Integer getInstantTime();
	public String getDestination();
	public String getVortex();
}
