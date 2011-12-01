package org.axan.sep.common;

import java.io.Serializable;

import org.axan.sep.common.IGameBoard.GameBoardException;
import org.axan.sep.common.Protocol.eBuildingType;

public abstract class GameCommand<P>
{	
	protected final String playerLogin;
	protected final P params;
	
	public GameCommand(String playerLogin, P params)
	{
		this.playerLogin = playerLogin;
		this.params = params;
	}
	
	public P getParams()
	{
		return params;
	}

	abstract public IGameBoard apply(IGameBoard gameBoard) throws GameBoardException;
	
	///////////////////////////////////////////////////////////////
	
	public static class BuildParams implements Serializable
	{
		public final String celestialBodyName;
		public final eBuildingType buildingType;
		
		public BuildParams(String ceslestialBodyName, eBuildingType buildingType)
		{
			this.celestialBodyName = ceslestialBodyName;
			this.buildingType = buildingType;
		}
	}
	
	static class BuildCommand extends GameCommand<BuildParams> implements Serializable
	{
		public BuildCommand(String playerLogin, BuildParams params)
		{
			super(playerLogin, params);		
		}

		@Override
		public IGameBoard apply(IGameBoard gameBoard) throws GameBoardException
		{
			return gameBoard.build(playerLogin, params.celestialBodyName, params.buildingType);
		}

	}
}
