package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.base.IBaseUnitEncounterLog;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import org.axan.sep.common.IGameConfig;
import java.util.Set;
import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;
import java.util.HashSet;
import com.almworks.sqlite4java.SQLiteStatement;
import com.almworks.sqlite4java.SQLiteConnection;

public interface IUnitEncounterLog
{
	public Integer getSeenTurn();
	public String getSeenOwner();
	public String getUnitType();
	public Integer getUnitTurn();
	public String getSeenType();
	public String getUnitName();
	public Integer getInstantTime();
	public String getUnitOwner();
	public String getSeenName();
}
