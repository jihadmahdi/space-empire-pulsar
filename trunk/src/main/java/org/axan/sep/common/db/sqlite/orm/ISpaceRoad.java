package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.base.IBaseSpaceRoad;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteStatement;
import java.util.HashSet;
import java.util.Set;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;
import org.axan.sep.common.IGameConfig;

public interface ISpaceRoad
{
	public String getName();
	public String getBuilder();
	public String getSpaceCounterAType();
	public String getSpaceCounterACelestialBodyName();
	public Integer getSpaceCounterATurn();
	public String getSpaceCounterBType();
	public String getSpaceCounterBCelestialBodyName();
	public Integer getSpaceCounterBTurn();
}
