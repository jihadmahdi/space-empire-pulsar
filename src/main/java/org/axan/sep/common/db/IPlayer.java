package org.axan.sep.common.db;

import org.neo4j.graphdb.Node;
import org.axan.sep.common.db.IGameConfig;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

public interface IPlayer extends Serializable
{
	static class NameComparator implements Comparator<IPlayer>, Serializable
	{		
		@Override
		public int compare(IPlayer o1, IPlayer o2)
		{
			return o1.getName().compareTo(o2.getName());
		}
	}
	
	public static final NameComparator nameComparator = new NameComparator();
	
	String getName();
	IPlayerConfig getConfig();
	<T extends IUnit> Set<T> getUnits(Class<T> expectedType);
}
