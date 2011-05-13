package org.axan.sep.common.db;

import org.axan.sep.common.SEPUtils.Location;

public interface IVersionedPulsarMissile extends IPulsarMissile, IVersionedUnit
{
	public Location getDirection();
}
