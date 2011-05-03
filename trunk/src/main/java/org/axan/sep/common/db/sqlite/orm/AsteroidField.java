package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.ProductiveCelestialBody;
import org.axan.sep.common.db.sqlite.orm.base.BaseAsteroidField;
import org.axan.sep.common.IGameConfig;
import com.almworks.sqlite4java.SQLiteStatement;

public class AsteroidField extends ProductiveCelestialBody implements IAsteroidField
{
	private final BaseAsteroidField baseAsteroidFieldProxy;

	public AsteroidField(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		super(stmnt, config);
		this.baseAsteroidFieldProxy = new BaseAsteroidField(stmnt);
	}

}
