package org.axan.sep.common.db;

public interface ISpaceRoad
{
	/**
	 * Return source productive celestial body name.
	 */
	String getSourceName();
	
	/**
	 * Return destination productive celestial body name.
	 */
	String getDestinationName();
	
	/**
	 * Destroy space road from current DB version.
	 */
	void destroy();
}
