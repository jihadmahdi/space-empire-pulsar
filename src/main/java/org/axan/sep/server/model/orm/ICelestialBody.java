package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.base.IBaseCelestialBody;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.SEPUtils.Location;
import java.util.Set;
import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;
import java.util.HashSet;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import com.almworks.sqlite4java.SQLiteStatement;
import com.almworks.sqlite4java.SQLiteConnection;

public interface ICelestialBody
{
	public Location getLocation();
	public eCelestialBodyType getType();
	public String getName();
}
