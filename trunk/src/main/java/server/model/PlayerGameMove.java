/**
 * @author Escallier Pierre
 * @file PlayerGameMove.java
 * @date 2 juin 2009
 */
package server.model;

import java.util.Map;
import java.util.Stack;

import common.PlayerGameBoard;

/**
 * Represent a player move for a specific game board.
 */
public class PlayerGameMove
{
	private final GameBoard originalGameBoard;
	private final Stack<GameMoveCommand>	commands = new Stack<GameMoveCommand>();
	
	public PlayerGameMove(GameBoard originalGameBoard)
	{
		this.originalGameBoard = originalGameBoard;
	}

	public GameBoard getGameBoard()
	{
		if (commands.isEmpty())
		{
			return originalGameBoard;
		}
		else
		{
			return commands.peek().getFinalGameBoard();
		}
	}
}
