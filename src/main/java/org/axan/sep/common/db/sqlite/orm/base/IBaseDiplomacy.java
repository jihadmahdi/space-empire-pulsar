package org.axan.sep.common.db.sqlite.orm.base;


public interface IBaseDiplomacy
{
	public String getOwner();
	public String getTarget();
	public Integer getTurn();
	public Boolean getAllowToLand();
	public String getForeignPolicy();
}
