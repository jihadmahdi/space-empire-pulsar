package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.base.IBaseCelestialBody;
import org.axan.sep.common.IGameConfig;
import com.almworks.sqlite4java.SQLiteStatement;
import java.util.Set;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import com.almworks.sqlite4java.SQLiteConnection;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.SEPUtils.Location;
import java.util.HashSet;
import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;

public interface ICelestialBody
{
	public Location getLocation();
	public eCelestialBodyType getType();
	public String getName();
}
