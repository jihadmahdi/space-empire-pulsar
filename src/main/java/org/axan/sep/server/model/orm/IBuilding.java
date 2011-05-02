package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.base.IBaseBuilding;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import org.axan.sep.common.IGameConfig;
import java.util.Set;
import org.axan.sep.common.Protocol.eBuildingType;
import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;
import java.util.HashSet;
import com.almworks.sqlite4java.SQLiteStatement;
import com.almworks.sqlite4java.SQLiteConnection;

public interface IBuilding
{
	public Integer getNbSlots();
	public String getCelestialBodyName();
	public Integer getTurn();
	public eBuildingType getType();
}
