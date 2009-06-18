package server.model;

import java.util.Map;

import org.axan.eplib.utils.Basic;

import server.model.ProductiveCelestialBody.CelestialBodyBuildException;

import common.IStarship;
import common.Protocol.ServerRunningGame.RunningGameCommandException;

public class FormFleetCommand extends GameMoveCommand
{
	private final String planetName;
	private final String fleetName;
	private final Map<Class<? extends IStarship>, Integer> fleetToForm;
	
	public FormFleetCommand(String playerLogin, String planetName, String fleetName, Map<Class<? extends IStarship>, Integer> fleetToForm)
	{
		super(playerLogin);
		this.planetName = planetName;
		this.fleetName = fleetName;
		this.fleetToForm = fleetToForm;
	}

	@Override
	protected GameBoard apply(GameBoard originalGameBoard)
	{
		GameBoard newGameBoard = Basic.clone(originalGameBoard);
		try
		{
			newGameBoard.formFleet(playerLogin, planetName, fleetName, fleetToForm);
		}
		catch(RunningGameCommandException e)
		{
			e.printStackTrace();
			return originalGameBoard;
		}
		return newGameBoard;
	}

}
