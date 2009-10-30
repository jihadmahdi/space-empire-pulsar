/**
 * @author Escallier Pierre
 * @file ServerGame.java
 * @date 1 juin 2009
 */
package org.axan.sep.server.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.axan.sep.common.GameConfig;
import org.axan.sep.common.Player;



/**
 * Represent a running game at a specific turn. It also provide previous turns archives.
 */
public class ServerGame
{	
	/** Resolved game turns. */
	private final Stack<GameBoard>				gameBoards = new Stack<GameBoard>();
	
	private final Map<String, PlayerGameMove>		playersCurrentMove = new HashMap<String, PlayerGameMove>();
	
	public ServerGame(Set<Player> playerList, GameConfig gameConfig)
	{
		GameBoard initialGameBoard = new GameBoard(playerList, gameConfig, 0);
		gameBoards.push(initialGameBoard);
		for(Player p : playerList)
		{
			playersCurrentMove.put(p.getName(), new PlayerGameMove(initialGameBoard, p.getName()));
		}
	}

	public GameBoard getGameBoard(String playerLogin)
	{
		return playersCurrentMove.get(playerLogin).getGameBoard();
	}
	
	public PlayerGameMove getPlayerGameMove(String playerLogin)
	{
		return playersCurrentMove.get(playerLogin);
	}
	
	public void resolveCurrentTurn()
	{
		GameBoard currentGameBoard = gameBoards.peek();
		
		// Merge players instant actions gameboards.
		for(String playerLogin : playersCurrentMove.keySet())
		{
			PlayerGameMove playerGameMove = playersCurrentMove.get(playerLogin);
			Stack<GameMoveCommand> playerCommands = playerGameMove.getCommands();
			
			for(GameMoveCommand cmd : playerCommands)
			{
				currentGameBoard = cmd.apply(currentGameBoard);
			}
		}
		
		// Resolve the turn on the merged gameboard.
		currentGameBoard.resolveCurrentTurn();
		
		// Save new turn gameboard.
		gameBoards.push(currentGameBoard);
		
		for(String playerLogin : playersCurrentMove.keySet())
		{
			playersCurrentMove.put(playerLogin, new PlayerGameMove(currentGameBoard, playerLogin));
		}		
	}
}
