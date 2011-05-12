package org.axan.sep.common.db;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteStatement;
import java.util.HashSet;
import java.util.Set;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;
import org.axan.sep.common.db.IGameConfig;

public interface IUnitEncounterLog
{
	public String getOwner();
	public String getUnitName();
	public Integer getTurn();
	public String getUnitType();
	public Integer getInstantTime();
	public String getSeenOwner();
	public String getSeenName();
	public Integer getSeenTurn();
	public String getSeenType();
}
