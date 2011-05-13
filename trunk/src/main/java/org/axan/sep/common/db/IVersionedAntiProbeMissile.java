package org.axan.sep.common.db;


public interface IVersionedAntiProbeMissile extends IAntiProbeMissile, IVersionedUnit
{
	public String getTargetOwner();
	public String getTargetName();
	public Integer getTargetTurn();
}
