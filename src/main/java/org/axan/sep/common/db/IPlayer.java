package org.axan.sep.common.db;

import java.util.Map;

public interface IPlayer extends Comparable<IPlayer>
{
	String getName();
	Map<String, Object> getNode();
	IPlayerConfig getConfig(SEPCommonDB db);
}
