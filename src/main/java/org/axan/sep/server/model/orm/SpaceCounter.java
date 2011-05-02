package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.Building;
import org.axan.sep.server.model.orm.base.BaseSpaceCounter;
import org.axan.sep.common.IGameConfig;
import com.almworks.sqlite4java.SQLiteStatement;

public class SpaceCounter extends Building implements ISpaceCounter
{
	private final BaseSpaceCounter baseSpaceCounterProxy;

	public SpaceCounter(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		super(stmnt, config);
		this.baseSpaceCounterProxy = new BaseSpaceCounter(stmnt);
	}

}
