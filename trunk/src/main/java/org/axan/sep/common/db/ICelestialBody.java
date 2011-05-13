package org.axan.sep.common.db;

import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.SEPUtils.Location;

public interface ICelestialBody
{
	public String getName();
	public eCelestialBodyType getType();
	public Location getLocation();
}
