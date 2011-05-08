package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.base.IBaseCarbonOrder;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteStatement;
import java.util.HashSet;
import java.util.Set;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;
import org.axan.sep.common.IGameConfig;

public interface ICarbonOrder
{
	public String getOwner();
	public String getSource();
	public Integer getPriority();
	public Integer getAmount();
	public String getDestination();
}