/**
 * @author Escallier Pierre
 * @file SEPServerClientSessionListener.java
 * @date 23 juin 08
 */
package server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.orfjackal.darkstar.rpc.RpcClient;
import net.orfjackal.darkstar.rpc.comm.ChannelAdapter;
import net.orfjackal.darkstar.rpc.comm.RpcGateway;

import server.SEPServer.SEPServerCreerNouvellePartieException;
import server.SEPServer.SEPServerDiffuserMessageException;
import server.SEPServer.SEPServerJoindreNouvellePartieException;
import utils.SEPUtils;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.Channel;
import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.ClientSessionListener;
import com.sun.sgs.app.DataManager;
import com.sun.sgs.app.Delivery;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;

import common.ClientServerProtocol;
import common.Command;
import common.Game;
import common.IGameCommand;
import common.IGameConfig;
import common.IPlayerConfig;
import common.IServerUser;
import common.IUserAccount;
import common.ServerClientProtocol;
import common.ClientServerProtocol.eEtats;
import common.ClientServerProtocol.eEvenements;
import common.metier.ConfigPartie;
import common.metier.PartieEnCreation;

/**
 * 
 */
public class SEPServerClientSessionListener implements Serializable, ClientSessionListener, IServerUser, IServerClientServerEventExecutor
{
	/** Serialization version. */
	private static final long	serialVersionUID	= 1L;

	/** The {@link Logger} for this class. */
	private static final Logger	logger				= Logger.getLogger(SEPServerClientSessionListener.class.getName());

	/** Client account. */
	private final ManagedReference<IUserAccount>	refAccount;

	/** Reference to the account current session. */
	private final ManagedReference<ClientSession>	refSession;

	/** User name. */
	private final String		name;

	/** RPC Gateway. */
	private final RpcGateway	gateway;
	
	/** Client StateMachine. */
	private final SEPServerClientStateMachine statemachine;
	
	public SEPServerClientSessionListener(IUserAccount account)
	{
		this.refAccount = AppContext.getDataManager().createReference(account);
		this.refSession = AppContext.getDataManager().createReference(account.getSession());
		this.name = getSession().getName();
		this.statemachine = new SEPServerClientStateMachine(this, this, this);
		this.statemachine.exportDOTfiles(new File("dot"));
		
		ChannelAdapter adapter = new ChannelAdapter();
		Channel channel = AppContext.getChannelManager().createChannel("RpcChannel:" + name, adapter, Delivery.RELIABLE);
		adapter.setChannel(channel);

		gateway = adapter.getGateway();
		//gateway.registerService(IServerUser.class, );

		channel.join(getSession());
	}

	public String getName()
	{
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.sgs.app.ClientSessionListener#disconnected(boolean)
	 */
	@Override
	public void disconnected(boolean graceful)
	{
		String log = "User " + getName() + " loggedOut " + (graceful ? "gracefull" : "forced");
		logger.log(Level.INFO, log);
		// setSession(null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.sgs.app.ClientSessionListener#receivedMessage(java.nio.ByteBuffer)
	 */
	@Override
	public void receivedMessage(ByteBuffer message)
	{
		Command command;
		try
		{
			command = Command.decode(message);
		}
		catch (IOException e)
		{
			logger.log(Level.WARNING, "Received unreadable command from user " + getName() + " : \"" + message + "\"");
			return;
		}

		logger.log(SEPServer.traceLevel, "Received command from user " + getName() + " : " + command.getCommand());

		/*
		 * try { machineEtats.traiterEvenement(eEvenements.valueOf(command.getCommand()), command.getParameters()); } catch (EPMachineEventNotExpected e) { logger.log(Level.WARNING, e.getMessage()); }
		 */
	}

	/**
	 * @return
	 */
	public ClientSession getSession()
	{
		return (refSession==null)?null:refSession.get();
	}

	/**
	 * @param session
	 */
	/*
	 * public void setSession(ClientSession session) { logger.log(SEPServer.traceLevel, "User session change"); DataManager dm = AppContext.getDataManager(); dm.markForUpdate(this);
	 * 
	 * if (session == null) { refSession = null; } else { refSession = dm.createReference(session); name = session.getName();
	 * 
	 * / if (machineEtats == null) { machineEtats = new SEPServerClientStateMachine(this, name); }
	 */
	/*
	 * if (gateway == null) { gateway = initGateway(session); gateway.registerService(IServerUser.class, new SEPServerServerUser()); } } }
	 */

	/**
	 * @return
	 */
	/*
	 * public ClientSession getSession() { if (refSession == null) return null; return refSession.get(); }
	 */

	
	/* (non-Javadoc)
	 * @see common.IServerUser#addFriend(java.lang.String)
	 */
	@Override
	public void addFriend(String newFriendName)
	{
		// TODO Auto-generated method stub
		logger.log(Level.INFO, "addFriend");
	}

	/* (non-Javadoc)
	 * @see common.IServerUser#askFriendList()
	 */
	@Override
	public void askFriendList()
	{
		// TODO Auto-generated method stub
		logger.log(Level.INFO, "askFriendList");
	}

	/* (non-Javadoc)
	 * @see common.IServerUser#askMyCurrentGamesList()
	 */
	@Override
	public void askMyCurrentGamesList()
	{
		// TODO Auto-generated method stub
		logger.log(Level.INFO, "askMyCurrentGamesList");
	}

	/* (non-Javadoc)
	 * @see common.IServerUser#askNewGameDatas()
	 */
	@Override
	public void askNewGameDatas()
	{
		// TODO Auto-generated method stub
		logger.log(Level.INFO, "askNewGameDatas");
	}

	/* (non-Javadoc)
	 * @see common.IServerUser#askNewGamesList()
	 */
	@Override
	public void askNewGamesList()
	{
		// TODO Auto-generated method stub
		logger.log(Level.INFO, "askNewGamesList");
	}

	/* (non-Javadoc)
	 * @see common.IServerUser#changeNewGameConfig(common.IGameConfig)
	 */
	@Override
	public void changeNewGameConfig(IGameConfig gameConfig)
	{
		// TODO Auto-generated method stub
		logger.log(Level.INFO, "changeNewGameConfig");
	}

	/* (non-Javadoc)
	 * @see common.IServerUser#changeNewGamePlayerConfig(common.IPlayerConfig)
	 */
	@Override
	public void changeNewGamePlayerConfig(IPlayerConfig playerConfig)
	{
		// TODO Auto-generated method stub
		logger.log(Level.INFO, "changeNewGamePlayerConfig");
	}

	/* (non-Javadoc)
	 * @see common.IServerUser#createGame(java.lang.String, java.lang.String, int)
	 */
	@Override
	public void createGame(String gameName, String gamePassword, int gameMaxPlayers)
	{
		// TODO Auto-generated method stub
		logger.log(Level.INFO, "createGame");
	}

	/* (non-Javadoc)
	 * @see common.IServerUser#exitNewGame()
	 */
	@Override
	public void exitNewGame()
	{
		// TODO Auto-generated method stub
		logger.log(Level.INFO, "exitNewGame");
	}

	/* (non-Javadoc)
	 * @see common.IServerUser#joinNewGame(java.lang.String)
	 */
	@Override
	public void joinNewGame(String newGameName)
	{
		// TODO Auto-generated method stub
		logger.log(Level.INFO, "joinNewGame");
	}

	/* (non-Javadoc)
	 * @see common.IServerUser#onGameCommand(common.GameCommand)
	 */
	@Override
	public void onGameCommand(IGameCommand command)
	{
		// TODO Auto-generated method stub
		logger.log(Level.INFO, "onGameCommand");
	}

	/* (non-Javadoc)
	 * @see common.IServerUser#removeFriend(java.lang.String)
	 */
	@Override
	public void removeFriend(String oldFriendName)
	{
		// TODO Auto-generated method stub
		logger.log(Level.INFO, "removeFriend");
	}

	/* (non-Javadoc)
	 * @see common.IServerUser#sendGameChatMessage(java.lang.String)
	 */
	@Override
	public void sendGameChatMessage(String msg)
	{
		// TODO Auto-generated method stub
		logger.log(Level.INFO, "sendGameChatMessage");
	}

	/* (non-Javadoc)
	 * @see common.IServerUser#sendNewGameChatMessage(java.lang.String)
	 */
	@Override
	public void sendNewGameChatMessage(String msg)
	{
		// TODO Auto-generated method stub
		logger.log(Level.INFO, "sendNewGameChatMessage");
	}

	/* (non-Javadoc)
	 * @see common.IServerUser#sendOutGameChatMessage(java.lang.String)
	 */
	@Override
	public void sendOutGameChatMessage(String msg)
	{
		// TODO Auto-generated method stub
		logger.log(Level.INFO, "sendOutGameChatMessage");
	}

	/* (non-Javadoc)
	 * @see common.IServerUser#sendPrivateMessage(java.lang.String, java.lang.String)
	 */
	@Override
	public void sendPrivateMessage(String receiverName, String msg)
	{
		// TODO Auto-generated method stub
		logger.log(Level.INFO, "sendPrivateMessage");
	}

	/* (non-Javadoc)
	 * @see common.IServerUser#startNewGame()
	 */
	@Override
	public void startNewGame()
	{
		// TODO Auto-generated method stub
		logger.log(Level.INFO, "startNewGame");
	}

	/* (non-Javadoc)
	 * @see common.IServerUser#tryReconnectingGame(java.lang.String)
	 */
	@Override
	public void tryReconnectingGame(String gameName)
	{
		// TODO Auto-generated method stub
		logger.log(Level.INFO, "tryReconnectingGame");
	}

	/* (non-Javadoc)
	 * @see server.IServerClientServerEventExecutor#onGamePaused()
	 */
	@Override
	public void onGamePaused()
	{
		// TODO Auto-generated method stub
		logger.log(Level.INFO, "onGamePaused");
	}

	/* (non-Javadoc)
	 * @see server.IServerClientServerEventExecutor#onGameResume(common.Game)
	 */
	@Override
	public void onGameResume(Game game)
	{
		// TODO Auto-generated method stub
		logger.log(Level.INFO, "onGameResume");
	}
}
