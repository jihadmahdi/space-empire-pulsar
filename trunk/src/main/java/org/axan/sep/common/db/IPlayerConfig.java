package org.axan.sep.common.db;

import java.util.Map;

public interface IPlayerConfig
{
	public String getName();
	public String getColor();
	public String getSymbol();
	public String getPortrait();
	public Map<String, Object> getNode();
}
