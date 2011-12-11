package org.axan.sep.common.db.orm;

import org.axan.eplib.orm.ISQLDataBaseStatement;
import org.axan.sep.common.Protocol.eBuildingType;
import org.axan.sep.common.db.ISpaceCounter;
import org.axan.sep.common.db.orm.base.BaseSpaceCounter;
import org.axan.sep.common.db.orm.base.IBaseSpaceCounter;

public class SpaceCounter extends Building implements ISpaceCounter
{
	private final IBaseSpaceCounter baseSpaceCounterProxy;

	SpaceCounter(IBaseSpaceCounter baseSpaceCounterProxy)
	{
		super(baseSpaceCounterProxy);
		this.baseSpaceCounterProxy = baseSpaceCounterProxy;
	}

	public SpaceCounter(eBuildingType type, String celestialBodyName, Integer turn, Integer nbSlots)
	{
		this(new BaseSpaceCounter(type.toString(), celestialBodyName, turn, nbSlots));
	}

	public SpaceCounter(ISQLDataBaseStatement stmnt) throws Exception
	{
		this(new BaseSpaceCounter(stmnt));
	}

}
