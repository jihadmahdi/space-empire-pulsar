package org.axan.sep.server.model;

import java.io.Serializable;

public class GovernmentStarship extends org.axan.sep.common.GovernmentStarship implements Serializable, ISpecialUnit
{
	public GovernmentStarship(String starshipName)
	{
		super(starshipName);
	}

	private static final long	serialVersionUID	= 1L;
	
	@Override
	public org.axan.sep.common.ISpecialUnit getPlayerView(int date, String playerLogin, boolean isVisible)
	{
		// Nothing to hide.
		return this;
	}

	@Override
	public boolean canJoinFleet()
	{
		return true;
	}
	
	@Override
	public boolean isVisibleToClients()
	{
		return true;
	}

}
