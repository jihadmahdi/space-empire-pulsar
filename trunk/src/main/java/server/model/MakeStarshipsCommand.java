package server.model;

import java.util.Map;

import org.axan.eplib.utils.Basic;

import common.IStarship;
import common.Protocol.ServerRunningGame.RunningGameCommandException;

public class MakeStarshipsCommand extends GameMoveCommand
{
	private final String planetName;
	private final Map<Class<? extends IStarship>, Integer> starshipsToMake;
	
	public MakeStarshipsCommand(String playerLogin, String planetName, Map<Class<? extends IStarship>, Integer> starshipsToMake)
	{
		super(playerLogin);
		this.planetName = planetName;
		this.starshipsToMake = starshipsToMake;
	}

	@Override
	protected GameBoard apply(GameBoard originalGameBoard)
	{
		GameBoard newGameBoard = Basic.clone(originalGameBoard);
		try
		{
			newGameBoard.makeStarships(playerLogin, planetName, starshipsToMake);
		}
		catch(RunningGameCommandException e)
		{
			e.printStackTrace();
			return originalGameBoard;
		}
		return newGameBoard;
	}

}
