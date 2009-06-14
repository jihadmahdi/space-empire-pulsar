/**
 * @author Escallier Pierre
 * @file ServerGame.java
 * @date 1 juin 2009
 */
package server.model;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.axan.eplib.utils.Basic;

import common.GameConfig;
import common.IBuilding;
import common.Player;
import common.PlayerGameBoard;

import server.SEPServer;
import server.model.Area.AreaIllegalDefinitionException;

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
			playersCurrentMove.put(p.getName(), new PlayerGameMove(initialGameBoard));
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
}
