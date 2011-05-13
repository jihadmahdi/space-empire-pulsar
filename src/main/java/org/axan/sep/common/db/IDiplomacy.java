package org.axan.sep.common.db;


public interface IDiplomacy
{
	public String getOwner();
	public String getTarget();
	public Integer getTurn();
	public Boolean getAllowToLand();
	public String getForeignPolicy();
}
