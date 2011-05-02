package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.base.IBaseSpecialUnit;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import org.axan.sep.common.IGameConfig;
import java.util.Set;
import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;
import org.axan.sep.common.Protocol.eSpecialUnitType;
import java.util.HashSet;
import com.almworks.sqlite4java.SQLiteStatement;
import com.almworks.sqlite4java.SQLiteConnection;

public interface ISpecialUnit
{
	public String getOwner();
	public eSpecialUnitType getType();
	public String getName();
}
