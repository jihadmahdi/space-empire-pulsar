/**
 * @author Escallier Pierre
 * @file ServerGame.java
 * @date 1 juin 2009
 */
package org.axan.sep.server.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Logger;

import org.axan.sep.common.GameConfig;
import org.axan.sep.common.Player;
import org.axan.sep.server.SEPServer;
import org.axan.sep.server.model.ISEPServerDataBase.SEPServerDataBaseException;

/**
 * Represent a running game at a specific turn. It also provide previous turns
 * archives.
 */
public class ServerGame implements Serializable
{
	private static final long							serialVersionUID	= 1L;

	private static final Logger							log					= SEPServer.log;

	/** Resolved game turns. */
	private final Stack<GameBoard>						gameBoards			= new Stack<GameBoard>();

	private transient Map<String, PlayerGameMove>	playersCurrentMove	= new HashMap<String, PlayerGameMove>();

	public ServerGame(Set<Player> playerList, GameConfig gameConfig)
	{
		GameBoard initialGameBoard = new GameBoard(playerList, gameConfig);
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

	public void save(ObjectOutputStream oos) throws IOException
	{
		oos.writeObject(this);
	}

	private void writeObject(java.io.ObjectOutputStream out) throws IOException
	{
		out.defaultWriteObject();

		out.writeInt(playersCurrentMove == null ? 0 : playersCurrentMove.keySet().size());
		if (playersCurrentMove != null) for(String playerName : playersCurrentMove.keySet())
		{
			out.writeObject(playerName);
		}
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();

		if (playersCurrentMove == null) {playersCurrentMove = new HashMap<String, PlayerGameMove>();}
		playersCurrentMove.clear();

		int nbPlayers = in.readInt();
		for(int i = 0; i < nbPlayers; ++i)
		{
			String playerName = String.class.cast(in.readObject());
			playersCurrentMove.put(playerName, new PlayerGameMove(gameBoards.peek(), playerName));
		}
	}

	private void readObjectNoData() throws ObjectStreamException
	{

	}

	public static ServerGame load(ObjectInputStream ois) throws IOException, ClassNotFoundException
	{	
		return ServerGame.class.cast(ois.readObject());		
	}
}
