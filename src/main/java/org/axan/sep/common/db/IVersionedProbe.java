package org.axan.sep.common.db;


public interface IVersionedProbe extends IProbe, IVersionedUnit
{
	boolean isDeployed();
}
