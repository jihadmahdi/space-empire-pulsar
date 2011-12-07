package org.axan.sep.common.db;

import java.util.Set;

import org.axan.eplib.orm.SQLDataBaseException;
import org.axan.sep.common.SEPUtils.Location;

public interface IArea
{
	Location getLocation();
	boolean isSun();	
	boolean isVisible(SEPCommonDB db, String playerName) throws SQLDataBaseException;
	ICelestialBody getCelestialBody(SEPCommonDB db) throws SQLDataBaseException;
	<T extends IUnit> Set<T> getUnits(SEPCommonDB db, Class<T> expectedType) throws SQLDataBaseException;
}
