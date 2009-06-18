/**
 * @author Escallier Pierre
 * @file PlayerGameMove.java
 * @date 2 juin 2009
 */
package server.model;

import java.util.Map;
import java.util.Stack;

import client.gui.RunningGamePanel;

import common.IBuilding;
import common.IStarship;
import common.Player;
import common.PlayerGameBoard;
import common.Protocol;
import common.Protocol.ServerRunningGame.RunningGameCommandException;

/**
 * Represent a player move for a specific game board.
 */
public class PlayerGameMove
{
	private final GameBoard originalGameBoard;
	private final Stack<GameMoveCommand>	commands = new Stack<GameMoveCommand>();
	private GameBoard currentGameBoard;
	private final String playerLogin;
	private boolean isTurnEnded;
	
	public PlayerGameMove(GameBoard originalGameBoard, String playerLogin)
	{
		this.originalGameBoard = originalGameBoard;
		this.playerLogin = playerLogin;
		this.isTurnEnded = false;
	}

	public GameBoard getGameBoard()
	{
		if (commands.isEmpty())
		{
			return originalGameBoard;
		}
		else
		{
			return currentGameBoard;
		}
	}

	public Stack<GameMoveCommand> getCommands()
	{
		return commands;
	}
	
	private void checkTurnIsNotEnded() throws RunningGameCommandException
	{
		if (isTurnEnded)
		{
			throw new RunningGameCommandException("Turn is already ended.");
		}
	}
	
	public void addBuildCommand(String celestialBodyName, Class<? extends IBuilding> buildingType) throws RunningGameCommandException
	{
		checkTurnIsNotEnded();
		
		if (!getGameBoard().canBuild(playerLogin, celestialBodyName, buildingType))
		{
			throw new RunningGameCommandException(playerLogin+" cannot build '"+buildingType.getSimpleName()+"' on "+celestialBodyName);
		}
		
		addGameMoveCommand(new BuildCommand(playerLogin, celestialBodyName, buildingType));
	}
	
	public void addFormFleetCommand(String planetName, String fleetName, Map<Class<? extends IStarship>, Integer> fleetToForm) throws RunningGameCommandException
	{
		checkTurnIsNotEnded();
		
		if (!getGameBoard().canFormFleet(playerLogin, planetName, fleetName, fleetToForm))
		{
			throw new RunningGameCommandException(playerLogin+" cannot form fleet '"+fleetName+"' on "+planetName);
		}
		
		addGameMoveCommand(new FormFleetCommand(playerLogin, planetName, fleetName, fleetToForm));
	}
	
	public void addMakeStarshipsCommand(String planetName, Map<Class<? extends IStarship>, Integer> starshipsToMake) throws RunningGameCommandException
	{
		checkTurnIsNotEnded();
		
		if (!getGameBoard().canMakeStarships(playerLogin, planetName, starshipsToMake))
		{
			throw new RunningGameCommandException(playerLogin+" cannot make these starships on "+planetName);
		}
		
		addGameMoveCommand(new MakeStarshipsCommand(playerLogin, planetName, starshipsToMake));
	}
	
	private void addGameMoveCommand(GameMoveCommand command) throws RunningGameCommandException
	{
		checkTurnIsNotEnded();
		
		currentGameBoard = command.apply(getGameBoard());
		commands.push(command);
	}

	public void resetTurn() throws RunningGameCommandException
	{
		checkTurnIsNotEnded();
		
		commands.removeAllElements();
	}

	public void endTurn()
	{
		isTurnEnded = true;
	}

	public boolean isTurnEnded()
	{
		return isTurnEnded;
	}	
}
