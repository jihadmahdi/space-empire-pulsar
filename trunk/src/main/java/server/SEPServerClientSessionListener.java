/**
 * @author Escallier Pierre
 * @file SEPServerClientSessionListener.java
 * @date 23 juin 08
 */
package server;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import sun.security.action.GetLongAction;

import net.orfjackal.darkstar.rpc.ServiceReference;
import net.orfjackal.darkstar.rpc.comm.ChannelAdapter;
import net.orfjackal.darkstar.rpc.comm.RpcGateway;
import net.orfjackal.darkstar.rpc.core.RpcFuture;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.Channel;
import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.ClientSessionListener;
import com.sun.sgs.app.Delivery;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.Task;
import com.sun.sgs.impl.util.ManagedSerializable;
import com.sun.sgs.kernel.KernelRunnable;
import common.Command;
import common.Game;
import common.IClientUser;
import common.IGameCommand;
import common.IGameConfig;
import common.IPlayerConfig;
import common.IServerUser;
import common.IUserAccount;

/**
 * 
 */
public class SEPServerClientSessionListener implements ManagedObject, Serializable, ClientSessionListener
{

	/** Serialization version. */
	private static final long						serialVersionUID	= 1L;

	/** The {@link Logger} for this class. */
	private static final Logger						logger				= Logger.getLogger(SEPServerClientSessionListener.class.getName());

	/** Client account. */
	private final ManagedReference<IUserAccount>	refAccount;

	/** Reference to the account current session. */
	private final ManagedReference<ClientSession>	refSession;

	/** User name. */
	private final String							name;

	/** RPC Gateway. */
	@SuppressWarnings("unused")
	private final RpcGateway						gatewayClient;

	private RpcGateway								gatewayServer;

	/** Client StateMachine. */
	private final SEPServerClientStateMachine		statemachine;

	private IClientUser								clientUser;

	public SEPServerClientSessionListener(IUserAccount account)
	{
		logger.log(Level.WARNING, "SEPServerClientSessionListener.CTOR; name==" + this.name + "; statemachine==" + this.statemachine);

		this.refAccount = AppContext.getDataManager().createReference(account);
		this.refSession = AppContext.getDataManager().createReference(account.getSession());
		this.name = getSession().getName();

		this.statemachine = new SEPServerClientStateMachine(this, new SEPServerUser(this), new ServerClientServerEventExecutor(this));
		this.statemachine.exportDOTfiles(new File("dot"));

		AppContext.getTaskManager().scheduleTask(new InitServerRpcTask(this), 2000);

		gatewayClient = initClientRpc();

		AppContext.getDataManager().setBinding(getName(), this);
	}

	IClientUser getClientUser() throws InterruptedException, ExecutionException
	{
		return clientUser;
	}

	private RpcGateway initClientRpc()
	{
		ChannelAdapter adapter = new ChannelAdapter(true);
		Channel channel = AppContext.getChannelManager().createChannel("ClientRpcChannel:" + name, adapter, Delivery.RELIABLE);
		adapter.setChannel(channel);

		RpcGateway gateway = adapter.getGateway();
		gateway.registerService(IServerUser.class, this.statemachine.getIServerUserProxy());

		logger.log(Level.INFO, "Server registered IServerUser service");

		channel.join(getSession());

		return gateway;
	}

	private RpcGateway initServerRpcGateway()
	{
		ChannelAdapter adapter = new ChannelAdapter(false);
		Channel channel = AppContext.getChannelManager().createChannel("ServerRpcChannel:" + name, adapter, Delivery.RELIABLE);
		channel.join(getSession());

		adapter.setChannel(channel);
		return adapter.getGateway();
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
		refAccount.get().setSession(null);
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
		return (refSession == null) ? null : refSession.get();
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

	private static class SEPServerUser implements IServerUser, Serializable
	{
		private static final long	serialVersionUID	= 1L;
		private final ManagedReference<SEPServerClientSessionListener> refClientSessionListener;
		
		public SEPServerUser(SEPServerClientSessionListener clientSessionListener)
		{
			this.refClientSessionListener = AppContext.getDataManager().createReference(clientSessionListener);
		}
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see common.IServerUser#addFriend(java.lang.String)
		 */
		@Override
		public void addFriend(String newFriendName)
		{
			// TODO Auto-generated method stub
			logger.log(Level.INFO, "addFriend");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see common.IServerUser#askFriendList()
		 */
		@Override
		public void askFriendList() throws InterruptedException, ExecutionException
		{
			// TODO Auto-generated method stub
			logger.log(Level.INFO, "askFriendList");
			refClientSessionListener.get().clientUser.onGamePaused();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see common.IServerUser#askMyCurrentGamesList()
		 */
		@Override
		public void askMyCurrentGamesList()
		{
			// TODO Auto-generated method stub
			logger.log(Level.INFO, "askMyCurrentGamesList");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see common.IServerUser#askNewGameDatas()
		 */
		@Override
		public void askNewGameDatas()
		{
			// TODO Auto-generated method stub
			logger.log(Level.INFO, "askNewGameDatas");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see common.IServerUser#askNewGamesList()
		 */
		@Override
		public void askNewGamesList()
		{
			// TODO Auto-generated method stub
			logger.log(Level.INFO, "askNewGamesList");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see common.IServerUser#changeNewGameConfig(common.IGameConfig)
		 */
		@Override
		public void changeNewGameConfig(IGameConfig gameConfig)
		{
			// TODO Auto-generated method stub
			logger.log(Level.INFO, "changeNewGameConfig");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see common.IServerUser#changeNewGamePlayerConfig(common.IPlayerConfig)
		 */
		@Override
		public void changeNewGamePlayerConfig(IPlayerConfig playerConfig)
		{
			// TODO Auto-generated method stub
			logger.log(Level.INFO, "changeNewGamePlayerConfig");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see common.IServerUser#createGame(java.lang.String, java.lang.String, int)
		 */
		@Override
		public void createGame(String gameName, String gamePassword, int gameMaxPlayers)
		{
			// TODO Auto-generated method stub
			logger.log(Level.INFO, "createGame");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see common.IServerUser#exitNewGame()
		 */
		@Override
		public void exitNewGame()
		{
			// TODO Auto-generated method stub
			logger.log(Level.INFO, "exitNewGame");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see common.IServerUser#joinNewGame(java.lang.String)
		 */
		@Override
		public void joinNewGame(String newGameName)
		{
			// TODO Auto-generated method stub
			logger.log(Level.INFO, "joinNewGame");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see common.IServerUser#onGameCommand(common.GameCommand)
		 */
		@Override
		public void onGameCommand(IGameCommand command)
		{
			// TODO Auto-generated method stub
			logger.log(Level.INFO, "onGameCommand");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see common.IServerUser#removeFriend(java.lang.String)
		 */
		@Override
		public void removeFriend(String oldFriendName)
		{
			// TODO Auto-generated method stub
			logger.log(Level.INFO, "removeFriend");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see common.IServerUser#sendGameChatMessage(java.lang.String)
		 */
		@Override
		public void sendGameChatMessage(String msg)
		{
			// TODO Auto-generated method stub
			logger.log(Level.INFO, "sendGameChatMessage");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see common.IServerUser#sendNewGameChatMessage(java.lang.String)
		 */
		@Override
		public void sendNewGameChatMessage(String msg)
		{
			// TODO Auto-generated method stub
			logger.log(Level.INFO, "sendNewGameChatMessage");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see common.IServerUser#sendOutGameChatMessage(java.lang.String)
		 */
		@Override
		public void sendOutGameChatMessage(String msg)
		{
			// TODO Auto-generated method stub
			logger.log(Level.INFO, "sendOutGameChatMessage");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see common.IServerUser#sendPrivateMessage(java.lang.String, java.lang.String)
		 */
		@Override
		public void sendPrivateMessage(String receiverName, String msg)
		{
			// TODO Auto-generated method stub
			logger.log(Level.INFO, "sendPrivateMessage");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see common.IServerUser#startNewGame()
		 */
		@Override
		public void startNewGame()
		{
			// TODO Auto-generated method stub
			logger.log(Level.INFO, "startNewGame");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see common.IServerUser#tryReconnectingGame(java.lang.String)
		 */
		@Override
		public void tryReconnectingGame(String gameName)
		{
			// TODO Auto-generated method stub
			logger.log(Level.INFO, "tryReconnectingGame");
		}
	}
	
	private static class ServerClientServerEventExecutor implements IServerClientServerEventExecutor, Serializable
	{
		private static final long	serialVersionUID	= 1L;
		
		private final ManagedReference<SEPServerClientSessionListener> refClientSessionListener;

		public ServerClientServerEventExecutor(SEPServerClientSessionListener clientSessionListener)
		{
			this.refClientSessionListener = AppContext.getDataManager().createReference(clientSessionListener);
		}
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see server.IServerClientServerEventExecutor#onGamePaused()
		 */
		@Override
		public void onGamePaused()
		{
			// TODO Auto-generated method stub
			logger.log(Level.INFO, "onGamePaused");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see server.IServerClientServerEventExecutor#onGameResume(common.Game)
		 */
		@Override
		public void onGameResume(Game game)
		{
			// TODO Auto-generated method stub
			logger.log(Level.INFO, "onGameResume");
		}
	}

	private static class InitServerRpcTask implements Task, Serializable
	{
		private static final long										serialVersionUID	= 1L;

		private final ManagedReference<SEPServerClientSessionListener>	refListener;

		public InitServerRpcTask(SEPServerClientSessionListener listener)
		{
			this.refListener = AppContext.getDataManager().createReference(listener);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.sun.sgs.app.Task#run()
		 */
		@Override
		public void run() throws Exception
		{
			SEPServerClientSessionListener listener = refListener.getForUpdate();

			listener.gatewayServer = listener.initServerRpcGateway();

			logger.log(Level.INFO, "Server querying IClientUser service");

			Future<Set<ServiceReference<IClientUser>>> futureClientUsers = listener.gatewayServer.nonBlockingRemoteFindByType(IClientUser.class);

			long timeout = Long.MAX_VALUE;
			WaitForClientUserTask waitForClientUserTask = new WaitForClientUserTask(futureClientUsers, listener, timeout);
			AppContext.getTaskManager().scheduleTask(waitForClientUserTask);
		}
	}

	private static class WaitForClientUserTask implements Task, Serializable
	{

		private static final long										serialVersionUID	= 1L;

		private final Future<Set<ServiceReference<IClientUser>>>		futureClientUsers;

		private final ManagedReference<SEPServerClientSessionListener>	refListener;

		private final long												timeOutMs;

		private Long													startTime			= null;

		private WaitForClientUserTask(long startTime, Future<Set<ServiceReference<IClientUser>>> futureClientUsers, SEPServerClientSessionListener listener, long timeOutMs)
		{
			this.startTime = startTime;
			this.futureClientUsers = futureClientUsers;
			this.refListener = AppContext.getDataManager().createReference(listener);
			this.timeOutMs = timeOutMs;
		}

		public WaitForClientUserTask(Future<Set<ServiceReference<IClientUser>>> futureClientUsers, SEPServerClientSessionListener listener, long timeOutMs)
		{
			this.futureClientUsers = futureClientUsers;
			this.refListener = AppContext.getDataManager().createReference(listener);
			this.timeOutMs = timeOutMs;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.sun.sgs.app.Task#run()
		 */
		@Override
		public void run() throws Exception
		{
			StringBuilder trace = new StringBuilder("WaitForClientUserTask");

			if (startTime == null)
			{
				startTime = System.currentTimeMillis();
			}

			Set<ServiceReference<IClientUser>> refs;

			try
			{
				Future<Set<ServiceReference<IClientUser>>> refreshedFutureClientUsers = RpcFuture.refresh((RpcFuture<Set<ServiceReference<IClientUser>>>) futureClientUsers);

				if (refreshedFutureClientUsers.isDone())
				{
					refs = refreshedFutureClientUsers.get();
				}
				else
				{
					throw new TimeoutException("little timeout");
				}
			}
			catch (TimeoutException e)
			{
				long endTime = System.currentTimeMillis();
				long elapsedTime = (endTime - startTime);

				if (elapsedTime > timeOutMs)
				{
					trace.append(": big timeout");
					logger.log(Level.INFO, trace.toString());

					throw new TimeoutException("Querying for services failed (elapsedTime: " + elapsedTime + ")");
				}

				trace.append(": little timeout");
				logger.log(Level.INFO, trace.toString());
				AppContext.getTaskManager().scheduleTask(new WaitForClientUserTask(startTime, futureClientUsers, refListener.get(), timeOutMs), 1000);
				return;
			}

			trace.append(": success");
			logger.log(Level.INFO, trace.toString());
			AppContext.getTaskManager().scheduleTask(new ClientUserCallback(refListener.get(), refs));
		}

	}

	private static class ClientUserCallback implements Task, KernelRunnable, Serializable
	{
		private static final long										serialVersionUID	= 1L;

		private final ManagedReference<SEPServerClientSessionListener>	refListener;

		private final Set<ServiceReference<IClientUser>>				refs;

		/**
		 * 
		 */
		public ClientUserCallback(SEPServerClientSessionListener listener, Set<ServiceReference<IClientUser>> refs)
		{
			this.refListener = AppContext.getDataManager().createReference(listener);
			this.refs = refs;
		}

		@Override
		public void run() throws Exception
		{
			logger.log(Level.INFO, "ClientUserCallback");

			SEPServerClientSessionListener listener = refListener.getForUpdate();
			Set<IClientUser> clientUsers = listener.gatewayServer.asProxies(refs);

			assert clientUsers.size() == 1;

			logger.log(Level.INFO, "InitClientUser Complete");
			listener.clientUser = clientUsers.iterator().next();

			logger.log(Level.INFO, "clientUser.onGamePaused");
			listener.clientUser.onGamePaused();
		}

		@Override
		public String getBaseTaskType()
		{
			return Task.class.getName();
		}
	}
}
