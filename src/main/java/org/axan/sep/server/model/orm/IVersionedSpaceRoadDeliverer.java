package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.base.IBaseVersionedSpaceRoadDeliverer;

public interface IVersionedSpaceRoadDeliverer
{
	public String getOwner();
	public Integer getTurn();
	public String getType();
	public String getName();
}
