package org.axan.sep.common.db.sqlite.orm.base;

import org.axan.sep.common.db.sqlite.orm.base.IBaseDiplomacy;
import com.almworks.sqlite4java.SQLiteStatement;
import java.util.Map;
import java.util.HashMap;
import java.lang.Exception;

public class BaseDiplomacy implements IBaseDiplomacy
{
	private final String owner;
	private final String target;
	private final Integer turn;
	private final Boolean allowToLand;
	private final String foreignPolicy;

	public BaseDiplomacy(String owner, String target, Integer turn, Boolean allowToLand, String foreignPolicy)
	{
		this.owner = owner;
		this.target = target;
		this.turn = turn;
		this.allowToLand = allowToLand;
		this.foreignPolicy = foreignPolicy;
	}

	public BaseDiplomacy(SQLiteStatement stmnt) throws Exception
	{
		Map<String, Object> row = new HashMap<String, Object>(stmnt.columnCount());
		for(int i=0; i<stmnt.columnCount(); ++i)
		{
			String col = stmnt.getColumnName(i);
			if ("owner".compareTo(col) != 0 && "target".compareTo(col) != 0 && "turn".compareTo(col) != 0 && "allowToLand".compareTo(col) != 0 && "foreignPolicy".compareTo(col) != 0) continue;
			if (row.containsKey(col) && ( (stmnt.columnString(i) == null && row.get(col) != null) || ((stmnt.columnString(i) != null && row.get(col) == null)) || (stmnt.columnString(i) != null && row.get(col) != null && stmnt.columnString(i).compareTo(row.get(col).toString()) != 0)))
			{
				if (stmnt.getColumnTableName(i).compareTo(this.getClass().getSimpleName()) != 0)
				{
					continue;
				}
			}
			Object value=stmnt.columnValue(i);
			if (value != null && col.matches("allowToLand"))
			{
				value = Boolean.valueOf(value.toString());
			}
			row.put(col, value);
		}
		this.owner = String.class.cast(row.get("owner"));
		this.target = String.class.cast(row.get("target"));
		this.turn = Integer.class.cast(row.get("turn"));
		this.allowToLand = Boolean.class.cast(row.get("allowToLand"));
		this.foreignPolicy = String.class.cast(row.get("foreignPolicy"));
	}

	public String getOwner()
	{
		return owner;
	}

	public String getTarget()
	{
		return target;
	}

	public Integer getTurn()
	{
		return turn;
	}

	public Boolean getAllowToLand()
	{
		return allowToLand;
	}

	public String getForeignPolicy()
	{
		return foreignPolicy;
	}

}
