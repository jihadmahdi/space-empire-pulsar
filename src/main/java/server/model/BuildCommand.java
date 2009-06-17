package server.model;

import org.axan.eplib.utils.Basic;

import server.model.ProductiveCelestialBody.CelestialBodyBuildException;

public class BuildCommand extends GameMoveCommand
{

	private final String celestialBodyName;
	private final Class<? extends common.IBuilding> buildingType;
	
	public BuildCommand(String playerLogin, String celestialBodyName, Class<? extends common.IBuilding> buildingType)
	{
		super(playerLogin);
		this.celestialBodyName = celestialBodyName;
		this.buildingType = buildingType;
	}

	@Override
	protected GameBoard apply(GameBoard originalGameBoard)
	{
		GameBoard newGameBoard = Basic.clone(originalGameBoard);
		try
		{
			newGameBoard.build(playerLogin, celestialBodyName, buildingType);
		}
		catch(CelestialBodyBuildException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return originalGameBoard;
		}
		return newGameBoard;
	}

}
