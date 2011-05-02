package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.base.IBaseSpaceRoad;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import org.axan.sep.common.IGameConfig;
import java.util.Set;
import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;
import java.util.HashSet;
import com.almworks.sqlite4java.SQLiteStatement;
import com.almworks.sqlite4java.SQLiteConnection;

public interface ISpaceRoad
{
	public String getSpaceCounterBCelestialBodyName();
	public Integer getSpaceCounterATurn();
	public String getSpaceCounterAType();
	public String getSpaceCounterBType();
	public Integer getSpaceCounterBTurn();
	public String getSpaceCounterACelestialBodyName();
}
