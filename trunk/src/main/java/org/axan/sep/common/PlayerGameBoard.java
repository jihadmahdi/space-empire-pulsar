package org.axan.sep.common;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.axan.eplib.clientserver.ConnectionAbortedError;
import org.axan.eplib.orm.sql.ISQLDataBaseFactory;
import org.axan.eplib.orm.sql.SQLDataBaseException;
import org.axan.sep.client.SEPClient;
import org.axan.sep.common.db.EvCreateUniverse;
import org.axan.sep.common.db.IDBFactory;
import org.axan.sep.common.db.IGameConfig;
import org.axan.sep.common.db.IGameEvent;
import org.axan.sep.common.db.IGameEvent.GameEventException;
import org.axan.sep.common.db.IGameEvent.IGameEventExecutor;
import org.axan.sep.common.db.IPlayer;
import org.axan.sep.common.db.IPlayerConfig;
import org.axan.sep.common.db.orm.SEPCommonDB;

/**
 * Represent the game board for a specific player. It provide
 * informations about the universe since the creation.
 * Must be extended with a DB specific implementation.
 */
public class PlayerGameBoard implements IGameBoard
{
	private static final Logger log = Logger.getLogger(SEPClient.class.getName());
	
	private final SEPClient client;
	private PlayerGameboardView view;
	
	/** List players during game creation, must not be used once game is ran. */
	private transient Map<IPlayer, IPlayerConfig> players = new TreeMap<IPlayer, IPlayerConfig>(IPlayer.nameComparator);
	
	/** During game creation, temporary game config. */
	private transient GameConfig gameConfig = new GameConfig();

	/** DB factory. */
	private final IDBFactory dbFactory;
	
	public PlayerGameBoard(SEPClient client)
	{
		this.dbFactory = client;
		this.client = client;
	}
	
	////////// public methods
	
	public synchronized boolean isGameInCreation()
	{
		return getDB() == null;
	}
	
	public synchronized void refreshGameConfig(GameConfig gameCfg)
	{
		if (!isGameInCreation())
		{
			log.log(Level.SEVERE, "Received new game config while game is already running.");
			return;
		}
		
		gameConfig = gameCfg;
	}
	
	public synchronized void refreshPlayerList(Map<IPlayer, IPlayerConfig> playerList)
	{
		if (isGameInCreation())
		{
			players.clear();
			players.putAll(playerList);
		}
	}
	
	public IGameConfig getConfig()
	{
		if (isGameInCreation())
		{
			return gameConfig;
		}
		else
		{
			return getDBConfig();
		}
	}
	
	@Override
	public Map<IPlayer, IPlayerConfig> getPlayerList() throws GameBoardException
	{
		if (isGameInCreation()) // Game in creation
		{
			return players;
		}
		else
		{
			return getDBPlayerList();			
		}
	}
	
	/**
	 * Get player config no matter the game current state (in creation or running)
	 * @param playerName
	 * @return
	 * @throws GameBoardException 
	 */
	public synchronized IPlayerConfig getPlayerConfig(String playerName) throws GameBoardException
	{
		if (isGameInCreation())
		{
			for(IPlayer p : players.keySet())
			{
				if (p.getName().equals(playerName)) return players.get(p);
			}
			return null;
		}
		else
		{
			return getDBPlayerConfig(playerName);			
		}		
	}
	
	public synchronized void receiveNewTurnGameBoard(List<IGameEvent> newTurnEvents) throws GameBoardException
	{
		if (isGameInCreation())
		{
			IGameEvent e = newTurnEvents.get(0);
			if (!EvCreateUniverse.class.isInstance(e))
			{
				throw new GameBoardException("First game event must be EvCreateUniverse but is '"+e.getClass().getSimpleName()+"'.");
			}
			
			try
			{
				view = new PlayerGameboardView(client.getLogin(), new SEPCommonDB(dbFactory.createDB(), getConfig()), new IGameEventExecutor()
				{					
					@Override
					public void onGameEvent(IGameEvent event, Set<String> observers)
					{
						// executor filtered on current player view player.
						if (!observers.contains(client.getLogin())) return;
						view.onGameEvent(event);
					}
				});
			}
			catch(Throwable t)
			{
				log.log(Level.SEVERE, "Error running new game", t);
				throw new ConnectionAbortedError(t);
			}
		}
		
		view.onGameEvents(newTurnEvents);
		
		try
		{
			view.resolveCurrentTurn();
		}
		catch(GameEventException e)
		{
			throw new GameBoardException(e);
		}
	}
	
	public SEPCommonDB getDB()
	{
		return view == null ? null : view.getDB();
	}
	
	////////// private methods
	
	
	
	////////// Running game getters
	
	private IGameConfig getDBConfig()
	{
		return getDB().getConfig();
	}
	
	private Map<IPlayer, IPlayerConfig> getDBPlayerList()
	{
		Map<IPlayer, IPlayerConfig> result = new TreeMap<IPlayer, IPlayerConfig>();

		Set<IPlayer> ps = getDB().getPlayers();
		for(IPlayer p : ps)
		{
			IPlayerConfig pc = p.getConfig();
			result.put(p, pc);
		}			

		return result;
	}
	
	private IPlayerConfig getDBPlayerConfig(String playerName) throws GameBoardException
	{
		return getDB().getPlayerByName(playerName).getConfig();
	}
	
}
