package org.axan.sep.common.db;

public interface IAntiProbeMissile extends IAntiProbeMissileMarker, IUnit
{
	/**
	 * If fired return the anti probe missile target, if not fired return null. 
	 * @return
	 */
	IProbeMarker getTarget();
	
	/**
	 * Set the target (you also must call {@link #setDestination(org.axan.sep.common.SEPUtils.Location)} to actually fire the missile).
	 * @param target
	 */
	void setTarget(IProbeMarker target);
}
