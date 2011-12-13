package org.axan.sep.common.db;

import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.SEPUtils.Location;
import java.util.Map;

public interface ICelestialBody
{
	public String getName();
	public eCelestialBodyType getType();
	public Location getLocation();
	public Map<String, Object> getNode();
}
