package org.axan.sep.common.db.sqlite;

import org.axan.eplib.orm.sqlite.SQLiteDB;
import org.axan.sep.common.db.IGameConfig;
import org.axan.sep.common.PlayerGameBoard;

public class SEPClientSQLiteDB extends PlayerGameBoard
{
	private final SEPCommonSQLiteDB commonDB;
	private transient SQLiteDB db;
	
	public SEPClientSQLiteDB(SEPCommonSQLiteDB commonDB)
	{
		this.commonDB = commonDB;
		this.db = commonDB.getDB();
	}
	
	public IGameConfig getConfig()
	{
		return commonDB.getConfig();
	}
	
	public SQLiteDB getDB()
	{
		return commonDB.getDB();
	}
	
	@Override
	public int getTurn()
	{
		return getConfig().getTurn();
	}
}