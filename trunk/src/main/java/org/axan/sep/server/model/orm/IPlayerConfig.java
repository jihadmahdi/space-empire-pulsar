package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.base.IBasePlayerConfig;

public interface IPlayerConfig
{
	public Byte[] getSymbol();
	public String getColor();
	public Byte[] getPortrait();
	public String getName();
}
