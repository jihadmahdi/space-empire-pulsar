package org.axan.sep.common.db;

public interface IProbe extends IProbeMarker, IUnit
{
	@Override
	public IProbeMarker getMarker(double step);
}
