package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.base.IBaseSpaceRoad;
import org.axan.sep.common.IGameConfig;
import com.almworks.sqlite4java.SQLiteStatement;
import java.util.Set;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import com.almworks.sqlite4java.SQLiteConnection;
import java.util.HashSet;
import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;

public interface ISpaceRoad
{
	public String getSpaceCounterBCelestialBodyName();
	public Integer getSpaceCounterATurn();
	public String getSpaceCounterAType();
	public String getSpaceCounterBType();
	public Integer getSpaceCounterBTurn();
	public String getSpaceCounterACelestialBodyName();
}
