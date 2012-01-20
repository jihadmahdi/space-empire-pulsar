package org.axan.sep.common.db;

public interface IAntiProbeMissile extends IAntiProbeMissileMarker, IUnit
{
	/**
	 * If fired return the anti probe missile target, if not fired return null. 
	 * @return
	 */
	IProbe getTarget();
}
