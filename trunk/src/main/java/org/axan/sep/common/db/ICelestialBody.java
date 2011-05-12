package org.axan.sep.common.db;

import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.IGameConfig;

public interface ICelestialBody
{
	public String getName();
	public eCelestialBodyType getType();
	public Location getLocation();
}
