package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.base.IBaseArea;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.SEPUtils.Location;
import java.util.Set;
import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;
import java.util.HashSet;
import com.almworks.sqlite4java.SQLiteStatement;
import com.almworks.sqlite4java.SQLiteConnection;

public interface IArea
{
	public Boolean getIsSun();
	public Location getLocation();
}
