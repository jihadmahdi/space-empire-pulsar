package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.base.IBaseUnitEncounterLog;
import org.axan.sep.common.IGameConfig;
import com.almworks.sqlite4java.SQLiteStatement;
import java.util.Set;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import com.almworks.sqlite4java.SQLiteConnection;
import java.util.HashSet;
import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;

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
