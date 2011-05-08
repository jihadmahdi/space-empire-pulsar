package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.Building;
import org.axan.sep.common.db.sqlite.orm.base.BaseSpaceCounter;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.Protocol.eBuildingType;

public class SpaceCounter extends Building implements ISpaceCounter
{
	private final BaseSpaceCounter baseSpaceCounterProxy;

	public SpaceCounter(eBuildingType type, String celestialBodyName, Integer turn, Integer nbSlots)
	{
		super(type, celestialBodyName, turn, nbSlots);
		baseSpaceCounterProxy = new BaseSpaceCounter(type.toString(), celestialBodyName, turn, nbSlots);
	}

	public SpaceCounter(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		super(stmnt, config);
		this.baseSpaceCounterProxy = new BaseSpaceCounter(stmnt);
	}

}
