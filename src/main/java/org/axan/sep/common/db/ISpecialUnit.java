package org.axan.sep.common.db;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteStatement;
import java.util.HashSet;
import java.util.Set;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;
import org.axan.sep.common.Protocol.eSpecialUnitType;
import org.axan.sep.common.db.IGameConfig;

public interface ISpecialUnit
{
	public String getOwner();
	public String getName();
	public eSpecialUnitType getType();
}
