package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.base.IBaseCarbonOrder;
import org.axan.sep.common.IGameConfig;
import com.almworks.sqlite4java.SQLiteStatement;
import java.util.Set;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import com.almworks.sqlite4java.SQLiteConnection;
import java.util.HashSet;
import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;

public interface ICarbonOrder
{
	public String getSource();
	public Integer getAmount();
	public Integer getPriority();
	public String getOwner();
	public String getDestination();
}
