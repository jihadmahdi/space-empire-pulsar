/**
 * @author Escallier Pierre
 * @file SGSTestSessionListener.java
 * @date 11 juin 08
 */
package server.pretests;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import EPLib.EPMachineEtats.EPMachineEtats;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.ClientSessionListener;
import com.sun.sgs.app.DataManager;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.NameNotBoundException;
import common.Command;
import common.ServerClientProtocol;
import common.ClientServerProtocol.eEtats;
import common.ClientServerProtocol.eEvenements;
import common.metier.Partie;

/**
 * 
 */
class SGSTestClientSessionListener implements Serializable, ClientSessionListener, ManagedObject
{
	/** The version of the serialized form of this class. */
	private static final long										serialVersionUID	= 1L;

	/** The {@link Logger} for this class. */
	private static final Logger										logger				= Logger.getLogger(SGSTestClientSessionListener.class.getName());

	/** The message encoding. */
	public static final String										MESSAGE_CHARSET		= "UTF-8";

	/** The prefix for player bindings in the {@code DataManager}. */
	protected static final String									PLAYER_BIND_PREFIX	= "Player.";

	private EPMachineEtats<eEtats,eEvenements>	machineClientSession= null;

	/** The session this {@code ClientSessionListener} is listening to. */
	private ManagedReference<ClientSession>							sessionRef			= null;

	/** player binding. */
	private String													name				= null;

	protected static EPMachineEtats<eEtats, eEvenements> genererMachineEtats(final SGSTestClientSessionListener clientSession, String nom)
	{
		try
		{
			EPMachineEtats<eEtats, eEvenements> machineClientSession = EPMachineEtats.Creer("SGSTestClientSession" + nom);

			machineClientSession.AjouterEtat(eEtats.AttenteCommande);
			machineClientSession.AjouterEtat(eEtats.AttenteCreationPartie);
			machineClientSession.AjouterEtat(eEtats.PartieEnCours);

			machineClientSession.AjouterEvenement(eEvenements.DemandeListeParties, new EPMachineEtats.EPOperationCourteSansParametres()
			{
				/** The version of the serialized form of this class. */
				private static final long										serialVersionUID	= 1L;

				@Override
				public void run()
				{
					DataManager dm = AppContext.getDataManager();
					ManagedObject server = dm.getBinding("Server");
					
					HashSet<Partie> parties = ((SGSTest) server).getListeParties();
					Command cmd = new Command(ServerClientProtocol.eEvenements.ReponseDemandeListeParties.toString(), parties);

					try
					{
						clientSession.sessionRef.get().send(cmd.encode());
					}
					catch (IOException e)
					{
						logger.log(Level.WARNING, "Unable to send to {0} the command {1}", new Object[] {this, cmd.getCommand()});
						return;
					}
				}

				@Override
				public String getName()
				{
					return "DemandeListePartie";
				}

			}, eEtats.AttenteCommande, true);
		
			machineClientSession.AjouterEvenement(eEvenements.CreerNouvellePartie, new EPMachineEtats.EPOperationCourteAvecParametres()
			{
			
				private static final long	serialVersionUID	= 1L;

				@Override
				public void run(Object ... objects)
				{
					if (objects.length < 1)
					{
						return;
					}
					String nomPartie = (String) objects[0];
					
					DataManager dm = AppContext.getDataManager();
					SGSTest server = (SGSTest) dm.getBinding("Server");
					
					HashSet<Partie> parties = server.getListeParties();
					
					if (!server.creerPartie(nomPartie))
					{
						logger.log(Level.INFO, "{0} tried to create game {1}, but it fails", new Object[]{clientSession.name, nomPartie});
						return;
					}
					
					
					Command cmd = new Command(ServerClientProtocol.eEvenements.ReponseDemandeListeParties.toString(), parties);

					try
					{
						clientSession.sessionRef.get().send(cmd.encode());
					}
					catch (IOException e)
					{
						logger.log(Level.WARNING, "Unable to send to {0} the command {1}", new Object[] {this, cmd.getCommand()});
						return;
					}
				}
			
				@Override
				public String getName()
				{
					return "CreerNouvellePartie";
				}
			
			}, eEtats.AttenteCommande, true);
			
			return machineClientSession;
		}
		catch (Exception e)
		{
			throw new RuntimeException("Erreur de création de la machine Etat: " + e);
		}
	}

	/**
	 * Find or create the player object for the given session, and mark the player as logged in on that session.
	 * 
	 * @param session
	 *            which session to find or create a player for.
	 * @return a player for the given session
	 */
	public static SGSTestClientSessionListener loggedIn(ClientSession session)
	{
		String playerBinding = PLAYER_BIND_PREFIX+session.getName();
		
		// Try to find player object, if non existent then create
		DataManager dm = AppContext.getDataManager();
		SGSTestClientSessionListener player;
		
		try
		{
			player = (SGSTestClientSessionListener) dm.getBinding(playerBinding);
			logger.log(Level.INFO, "Known player loggedIn: {0}", playerBinding);
		}
		catch(NameNotBoundException ex)
		{
			// This is a new player
			logger.log(Level.INFO, "New created player loggedIn: {0}", playerBinding);
			player = new SGSTestClientSessionListener(playerBinding);
			dm.setBinding(playerBinding, player);
		}
		player.setSession(session);
		return player;
	}

	/**
	 * Creates a new {@code HelloChannelsSessionListener} for the session.
	 * 
	 * @param session
	 *            the session this listener is associated with
	 * @param channel1
	 *            a reference to a channel to join
	 */
	public SGSTestClientSessionListener(String name)
	{
		if (name == null) throw new NullPointerException("null session");

		this.name = name;
		if (this.machineClientSession == null)
		{
			this.machineClientSession = genererMachineEtats(this, name);
		}
		this.machineClientSession.Demarrer();
	}

	/**
	 * Mark this player as logged in on the given session.
	 * 
	 * @param session
	 *            the sesion this player is loggin in on.
	 */
	protected void setSession(ClientSession session)
	{
		DataManager dm = AppContext.getDataManager();
		dm.markForUpdate(this);

		if (session == null)
		{
			sessionRef = null;
		}
		else
		{
			sessionRef = dm.createReference(session);
		}

		logger.log(Level.INFO, "Set session for {0} to {1}", new Object[] {this, session});
	}

	/**
	 * Returns the session for this listener.
	 * 
	 * @return the session for this listener
	 */
	protected ClientSession getSession()
	{
		// We created the ref with a non-null session, so no need to check it.
		if (sessionRef == null) return null;
		return sessionRef.get();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Logs when data arrives from the client, and echoes the message back.
	 */
	public void receivedMessage(ByteBuffer message)
	{
		Command command;
		try
		{
			command = Command.decode(message);
		}
		catch (IOException e)
		{
			logger.log(Level.WARNING, "{0} received unreadable command: {1}", new Object[] {this, message});
			return;
		}

		logger.log(Level.INFO, "{0} received command: {1}", new Object[] {this, command.getCommand()});

		machineClientSession.TraiterEvenement(eEvenements.valueOf(command.getCommand()), command.getParameters());
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Logs when the client disconnects.
	 */
	public void disconnected(boolean graceful)
	{
		setSession(null);
		ClientSession session = getSession();
		String grace = graceful ? "graceful" : "forced";
		logger.log(Level.INFO, "User {0} has logged out {1}", new Object[] {session.getName(), grace});
	}
}
