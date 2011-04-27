package org.axan.sep.server.model.orm;

import org.axan.sep.common.IGameConfig;
import org.axan.sep.server.model.orm.ProductiveCelestialBody;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.server.model.orm.base.BaseAsteroidField;

public class AsteroidField extends ProductiveCelestialBody implements IAsteroidField
{
	private final BaseAsteroidField baseAsteroidFieldProxy;

	public AsteroidField(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		super(stmnt, config);
		this.baseAsteroidFieldProxy = new BaseAsteroidField(stmnt);
	}

}
