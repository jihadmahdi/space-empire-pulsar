/**
 * @author Escallier Pierre
 * @file SEPServerClientStateMachine.java
 * @date 7 juil. 2008
 */
package server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.sgs.app.ClientSession;

import server.SEPServer.SEPServerCreerNouvellePartieException;
import server.SEPServer.SEPServerDiffuserMessageException;
import server.SEPServer.SEPServerJoindreNouvellePartieException;
import utils.SEPUtils;
import EPLib.EPMachineEtats.EPMachineEtats;
import EPLib.EPMachineEtats.EPMachineEtatsProxy;
import EPLib.EPMachineEtats.EPMachineEtats.EPMachineEtatsException;
import EPLib.EPMachineEtats.EPMachineEtats.EPMachineEventNotExpected;

import common.Game;
import common.IGameCommand;
import common.IGameConfig;
import common.IPlayerConfig;
import common.IServerUser;
import common.IUserAccount;
import common.SEPAccount;
import common.ServerClientProtocol;
import common.ClientServerProtocol.eEtats;
import common.ClientServerProtocol.eEvenements;
import common.metier.ConfigPartie;
import common.metier.PartieEnCreation;

/**
 * 
 */
class SEPServerClientStateMachine implements Serializable
{
	/** Serialization version. */
	private static final long	serialVersionUID	= 1L;
	
	private static final Logger logger = Logger.getLogger(SEPServerClientStateMachine.class.getName());
	
	private final EPMachineEtatsProxy<eEtats> machine;
	
	public IServerUser getIServerUserProxy()
	{
		return machine.getProxy(IServerUser.class);
	}
	
	public IServerClientServerEventExecutor getIServerEventProxy()
	{
		return machine.getProxy(IServerClientServerEventExecutor.class);
	}
	
	public SEPServerClientStateMachine(SEPServerClientSessionListener sessionListener, IServerUser clientExecutor, IServerClientServerEventExecutor serverExecutor)
	{
		String machineName = SEPServerClientStateMachine.class.getName()+"."+sessionListener.getName(); 

		logger.log(Level.INFO, "EPMachineEtatsProxy.Creer(\""+machineName+"\")");
		machine = EPMachineEtatsProxy.Creer(machineName);
		machine.registerExecutor(IServerUser.class, clientExecutor);
		machine.registerExecutor(IServerClientServerEventExecutor.class, serverExecutor);
		
		try
		{
			machine.AjouterEtat(eEtats.Connected);
			machine.AjouterEtat(eEtats.OutOfGame, eEtats.Connected);
			machine.AjouterEtat(eEtats.InNewGame, eEtats.Connected);
			machine.AjouterEtat(eEtats.InGame, eEtats.Connected);
			machine.AjouterEtat(eEtats.InPausedGame, eEtats.Connected);
			
			machine.AjouterEvenement(eEtats.Connected, true, "send private {msg} to {receiverName}", IServerUser.class, "sendPrivateMessage", String.class, String.class);
			machine.AjouterEvenement(eEtats.Connected, true, "send friendlist", IServerUser.class, "askFriendList");
			machine.AjouterEvenement(eEtats.Connected, true, "add friend {newFriendName} to the friendlist", IServerUser.class, "addFriend", String.class);
			machine.AjouterEvenement(eEtats.Connected, true, "remove friend {oldFriendName} from the friendlist", IServerUser.class, "removeFriend", String.class);
			
			machine.AjouterEvenement(eEtats.OutOfGame, true, "send out of game channel {msg}", IServerUser.class, "sendOutGameChatMessage", String.class);
			machine.AjouterEvenement(eEtats.OutOfGame, true, "send new games list", IServerUser.class, "askNewGamesList");
			machine.AjouterEvenement(eEtats.OutOfGame, true, "send user current games list", IServerUser.class, "askMyCurrentGamesList");
			
			machine.AjouterTransitionSure(eEtats.OutOfGame, eEtats.InGame, "reconnect the user to the game {gameName}", IServerUser.class, "tryReconnectingGame", String.class);
			machine.AjouterTransitionSure(eEtats.OutOfGame, eEtats.InNewGame, "create new game {gameName} protected by {gamePassword} for {gameMaxPlayers}", IServerUser.class, "createGame", String.class, String.class, int.class);
			machine.AjouterTransitionSure(eEtats.OutOfGame, eEtats.InNewGame, "join user to the game {newGameName}", IServerUser.class, "joinNewGame", String.class);
			
			machine.AjouterEvenement(eEtats.InNewGame, true, "send new game channel {msg}", IServerUser.class, "sendNewGameChatMessage", String.class);
			machine.AjouterEvenement(eEtats.InNewGame, true, "change current newgame {gameConfig}", IServerUser.class, "changeNewGameConfig", IGameConfig.class);
			machine.AjouterEvenement(eEtats.InNewGame, true, "change current newgame {playerConfig}", IServerUser.class, "changeNewGamePlayerConfig", IPlayerConfig.class);
			
			machine.AjouterTransitionSure(eEtats.InNewGame, eEtats.InGame, "start the curren new game", IServerUser.class, "startNewGame");
			machine.AjouterTransition(eEtats.InNewGame, eEtats.OutOfGame, "exit the current newgame", IServerUser.class, "exitNewGame");
			
			machine.AjouterEntryEvent(eEtats.InGame, new EPMachineEtats.EPOperationCourteSansParametres()
			{
				private static final long	serialVersionUID	= 1L;

				@Override
				public String getName()
				{
					return "check the game state";
				}
			
				@Override
				public void run()
				{
					// TODO
				}
			});
			
			machine.AjouterEvenement(eEtats.InGame, true, "delegate ingame command", IServerUser.class, "onGameCommand", IGameCommand.class);
			
			machine.AjouterTransition(eEtats.InGame, eEtats.InPausedGame, "game is paused", IServerClientServerEventExecutor.class, "onGamePaused");
			
			machine.AjouterEvenement(eEtats.InPausedGame, true, "send paused game channel {msg}", IServerUser.class, "sendGameChatMessage", String.class);
			
			machine.AjouterTransition(eEtats.InPausedGame, eEtats.InGame, "game is resumed", IServerClientServerEventExecutor.class, "onGameResume", Game.class);
		}
		catch (NoSuchMethodException e)
		{
			String msg = "Erreur de création de la machine-états pour "+sessionListener.getName();
			logger.log(Level.SEVERE, msg);
			throw new RuntimeException(msg, e);
		}
		catch (EPMachineEtatsException e)
		{
			String msg = "Erreur de création de la machine-états pour "+sessionListener.getName();
			logger.log(Level.SEVERE, msg);
			throw new RuntimeException(msg, e);
		}
		
		machine.Demarrer();
	}
	
	
	
	public void exportDOTfiles(File directory)
	{
		try
		{
			EPMachineEtatsProxy.exportDOTfiles(machine, new File("dot"));
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}
}