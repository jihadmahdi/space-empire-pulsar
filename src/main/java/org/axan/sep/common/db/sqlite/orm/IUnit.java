package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.base.IBaseUnit;
import org.axan.sep.common.IGameConfig;
import com.almworks.sqlite4java.SQLiteStatement;
import java.util.Set;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import org.axan.sep.common.Protocol.eUnitType;
import com.almworks.sqlite4java.SQLiteConnection;
import java.util.HashSet;
import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;

public interface IUnit
{
	public String getOwner();
	public eUnitType getType();
	public String getName();
	public float getSight();
}
