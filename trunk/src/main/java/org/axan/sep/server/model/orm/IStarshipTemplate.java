package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.base.IBaseStarshipTemplate;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import org.axan.sep.common.IGameConfig;
import java.util.Set;
import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;
import java.util.HashSet;
import com.almworks.sqlite4java.SQLiteStatement;
import com.almworks.sqlite4java.SQLiteConnection;

public interface IStarshipTemplate
{
	public String getSpecializedClass();
	public String getName();
}
