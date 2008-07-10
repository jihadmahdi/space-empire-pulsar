/**
 * @author Escallier Pierre
 * @file SEPServer.java
 * @date 23 juin 08
 */
package server;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.AppListener;
import com.sun.sgs.app.Channel;
import com.sun.sgs.app.ChannelManager;
import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.ClientSessionListener;
import com.sun.sgs.app.DataManager;
import com.sun.sgs.app.Delivery;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.NameNotBoundException;
import com.sun.sgs.app.Task;
import com.sun.sgs.app.TaskManager;
import com.sun.sgs.app.util.ScalableHashMap;
import common.Command;
import common.IUserAccount;
import common.SEPAccount;
import common.ServerClientProtocol;
import common.metier.ConfigPartie;
import common.metier.PartieEnCreation;

/**
 * 
 */
public class SEPServer implements Serializable, AppListener
{
	private static final String	SERVER_BINDING		= "Server";
	private static final int SERVER_NettoyerPartieEnCreationVidesPeriod = 30000;

	public static final Level	traceLevel			= Level.INFO;

	/** Serialization version number. */
	private static final long	serialVersionUID	= 1L;

	/** The {@link Logger} for this class. */
	private static final Logger	logger				= Logger.getLogger(SEPServer.class.getName());

	public static class SEPServerException extends Exception
	{

		private static final long	serialVersionUID	= 1L;

		SEPServerException()
		{
			super();
		}

		SEPServerException(String msg)
		{
			super(msg);
		}

	}

	public static class SEPServerDiffuserMessageException extends SEPServerException
	{

		private static final long	serialVersionUID	= 1L;

		public SEPServerDiffuserMessageException(String raison)
		{
			super(raison);
		}
	}
	
	public static class SEPServerCreerNouvellePartieException extends SEPServerException
	{

		private static final long	serialVersionUID	= 1L;

		public SEPServerCreerNouvellePartieException(String raison)
		{
			super(raison);
		}
	}
	
	public static class SEPServerJoindreNouvellePartieException extends SEPServerException
	{
		private static final long	serialVersionUID	= 1L;

		public SEPServerJoindreNouvellePartieException(String raison)
		{
			super(raison);
		}
	}
	
	private static class NettoyerPartieEnCreationVidesTask implements Task, Serializable
	{

		private static final long	serialVersionUID	= 1L;

		/* (non-Javadoc)
		 * @see com.sun.sgs.app.Task#run()
		 */
		@Override
		public void run() throws Exception
		{
			SEPServer.getServer().nettoyerPartieEnCreationVides();
		}
		
	}

	protected ManagedReference<ScalableHashMap<String, PartieEnCreation>>	refNouvellesParties;

	public SEPServer()
	{
		logger.log(traceLevel, "SEPServer constructor");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.sgs.app.AppListener#initialize(java.util.Properties)
	 */
	@Override
	public void initialize(Properties props)
	{
		System.out.println("SEPServer initializing.");
		logger.log(Level.INFO, "SEPServer initializing.");

		DataManager dm = AppContext.getDataManager();
		TaskManager tm = AppContext.getTaskManager();

		logger.log(Level.INFO, "Binding Server in DataManager.");
		dm.setBinding(SERVER_BINDING, this);

		logger.log(Level.INFO, "Create empty nouvellesParties hashmap.");
		refNouvellesParties = dm.createReference(new ScalableHashMap<String, PartieEnCreation>());
	
		logger.log(Level.INFO, "Create nettoyerPartieEnCreationVides Task");
		tm.schedulePeriodicTask(new NettoyerPartieEnCreationVidesTask(), SERVER_NettoyerPartieEnCreationVidesPeriod, SERVER_NettoyerPartieEnCreationVidesPeriod);
		
		logger.log(Level.INFO, "SEPServer initialized.");
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.sgs.app.AppListener#loggedIn(com.sun.sgs.app.ClientSession)
	 */
	@Override
	public ClientSessionListener loggedIn(ClientSession session)
	{
		IdentityManager im = AppContext.getManager(IdentityManager.class);
		SEPIdentity id = (SEPIdentity) im.getIdentity();
		
		logger.log(Level.INFO, "Identity: {0} {1} admin", new Object[] {id, (id.isAdmin()?"is":"is not")});
		
		// On regarde si l'utilisateur a déjà un profil enregistré (qui sert également de ClientSessionListener)
		/*
		if (session.getName().compareTo("admin") == 0)
		{
			return new AdminSessionListener(this, session);
		}
		*/
		
		DataManager dm = AppContext.getDataManager();
		IUserAccount account = null;
		try
		{
			account = (IUserAccount) dm.getBinding(id.getUID());
			ClientSession previousSession = account.getSession();
			if ((previousSession != null) && (previousSession.isConnected()))
			{
				logger.log(Level.WARNING, "Connection refused, session already connected for user \""+id+"\"");
				return null;
			}
				
			logger.log(Level.INFO, "Known User loggedIn : \"" + id + "\"");
		}
		catch (NameNotBoundException ex)
		{
			logger.log(Level.WARNING, "New User loggedIn : \"" + id + "\"");
			account = new SEPAccount(id.getUID());
			logger.log(Level.WARNING, "setBinding("+id.getUID()+", "+account+")");
			dm.setBinding(id.getUID(), account);
		}

		account.setSession(session);
		
		return new SEPServerClientSessionListener(account);
	}

	protected static SEPServer getServer()
	{
		DataManager dm = AppContext.getDataManager();
		ManagedObject obj = dm.getBinding(SERVER_BINDING);

		if ( !SEPServer.class.isInstance(obj))
		{
			String msg = "Object binded to \"" + SERVER_BINDING + "\" is not a SEPServer object";
			logger.log(Level.SEVERE, msg);
			throw new RuntimeException(msg);
		}

		return (SEPServer) obj;
	}

	protected Hashtable<String, PartieEnCreation> getNouvellesParties()
	{
		return new Hashtable<String, PartieEnCreation>(refNouvellesParties.get());
	}

	/**
	 * @param configPartie
	 * @return
	 * @throws SEPServerCreerNouvellePartieException
	 */
	public void creerNouvellePartie(String nomPartie, ConfigPartie configPartie) throws SEPServerCreerNouvellePartieException
	{
		ScalableHashMap<String, PartieEnCreation> nouvellesParties = refNouvellesParties.getForUpdate();
		if (nouvellesParties.containsKey(nomPartie))
		{
			throw new SEPServerCreerNouvellePartieException("Impossible de créer nouvelle partie \"" + nomPartie + "\", une partie du même nom est déjà en création.");
		}
		// TODO : Vérifier que le nom de partie ne soit pas non plus pris par une partie en cours.
		
		AppContext.getChannelManager().createChannel(nomPartie, new SEPCreationPartieChannelListener(), Delivery.RELIABLE);
		
		PartieEnCreation nouvellePartie = new PartieEnCreation(nomPartie, configPartie);
		
		logger.log(Level.INFO, "Nouvelle partie en cours de création : \""+nomPartie+"\"");
		nouvellesParties.put(nomPartie, nouvellePartie);
	}

	/**
	 * @param clientSession
	 * @param cfgPartie
	 * @throws SEPServerJoindreNouvellePartieException 
	 */
	public void joindreNouvellePartie(SEPServerClientSessionListener clientSession, String nomPartie) throws SEPServerJoindreNouvellePartieException
	{
		// TODO : créer un channel pour la partie en création.
		// Chaque partie (en création ou en cours) possède un channel à son nom.
		// Faire une classe PartieEnCreation pour gérer tout ça (découpler la classe ConfigPartie de l'état de partie en création) ?
		
		// On vérifie que la partie en cours de création existe bel et bien.
		ScalableHashMap<String, PartieEnCreation> nouvellesParties = refNouvellesParties.get();
		if (!nouvellesParties.containsKey(nomPartie))
		{
			throw new SEPServerJoindreNouvellePartieException("Impossible de joindre la partie \""+nomPartie+"\" car elle n'existe pas (ou n'est plus en cours de création).");
		}
		
		Channel channel = null;
		try
		{
			channel = AppContext.getChannelManager().getChannel(nomPartie);
		}
		catch(NameNotBoundException e)
		{
			logger.log(Level.WARNING, "La partie en cours \""+nomPartie+"\" existe sans channel.");
			channel = AppContext.getChannelManager().createChannel(nomPartie, new SEPCreationPartieChannelListener(), Delivery.RELIABLE);
		}
		
		logger.log(Level.INFO, "{0} join la partie en cours de création \"{1}\"", new Object[]{clientSession.getName(), channel.getName()});
		channel.join(clientSession.getSession());
		
		Vector<String> listeUsers = getChannelUserList(channel);
		if (!listeUsers.contains(clientSession.getName()))
		{
			listeUsers.add(clientSession.getName());
		}
		
		sendCommand(channel, null, ServerClientProtocol.eEvenements.refreshChannelUserList, listeUsers);
	}

	public Vector<String> getChannelUserList(Channel channel)
	{
		Vector<String> listeUsers = new Vector<String>();
		Iterator<ClientSession> it = channel.getSessions();
		while(it.hasNext())
		{
			listeUsers.add(it.next().getName());
		}
		
		return listeUsers;
	}
	
	public void nettoyerPartieEnCreationVides()
	{
		ChannelManager cm = AppContext.getChannelManager();
		ScalableHashMap<String, PartieEnCreation> nouvellesParties = refNouvellesParties.getForUpdate();
		Iterator<PartieEnCreation> it = nouvellesParties.values().iterator();
		boolean nettoyerPartieCourante = false;
		while(it.hasNext())
		{
			nettoyerPartieCourante = false;
			
			PartieEnCreation partie = it.next();
			try
			{
				Channel channel = cm.getChannel(partie.getNom());
				nettoyerPartieCourante = !channel.hasSessions();
			}
			catch(NameNotBoundException e)
			{
				nettoyerPartieCourante = true;
			}
			
			if (nettoyerPartieCourante)
			{
				logger.log(Level.INFO, "Nettoyage de la partie \""+partie.getNom()+"\"");
				nouvellesParties.remove(partie.getNom());
			}
		}
	}
	
	static protected ByteBuffer prepareCommand(ServerClientProtocol.eEvenements evnt, Serializable ...parameters) throws IOException
	{
		Command cmd = new Command(evnt.toString(), parameters);
		return cmd.encode();
	}
	
	static protected void sendCommand(SEPServerClientSessionListener clientSessionListener, ServerClientProtocol.eEvenements evnt, Serializable ... parameters)
	{
		sendCommand(clientSessionListener.getSession(), evnt, parameters);
	}
	static protected void sendCommand(ClientSession clientSession, ServerClientProtocol.eEvenements evnt, Serializable ... parameters)
    {
    	logger.log(Level.INFO, "Envoi commande {0} au serveur", evnt.toString());
        try
		{       	
        	clientSession.send(prepareCommand(evnt, parameters));
		}
		catch (IOException e1)
		{
			logger.log(Level.WARNING, "Unable to send to "+clientSession.getName()+" the command \""+evnt.toString()+"\"");
		}
    }
	
	static protected void sendCommand(Channel channel, ClientSession sender, ServerClientProtocol.eEvenements evnt, Serializable ... parameters)
	{
		logger.log(Level.INFO, "Envoi commande {0} depuis {1} sur le channel {2}", new Object[]{evnt.toString(), (sender==null)?"Serveur":sender.getName(), channel.getName()});
		try
		{
			channel.send(sender, prepareCommand(evnt, parameters));
		}
		catch(IOException e)
		{
			logger.log(Level.WARNING, "Unable to send "+evnt.toString()+" from "+((sender==null)?"Server":sender.getName())+" to channel "+channel.getName());
		}
	}

	/**
	 * @param clientSession
	 * @param nomChannel
	 * @param msg
	 * @throws SEPServerDiffuserMessageException 
	 */
	public void diffuserMessage(SEPServerClientSessionListener clientSessionListener, String nomChannel, String msg) throws SEPServerDiffuserMessageException
	{
		Channel channel = null;
		try
		{
			channel = AppContext.getChannelManager().getChannel(nomChannel);
		}
		catch(NameNotBoundException e)
		{
			throw new SEPServerDiffuserMessageException("Channel introuvable \""+nomChannel+"\"");
		}
		
		Iterator<ClientSession> it = channel.getSessions();
		while(it.hasNext())
		{
			// L'utilisateur est bien présent sur le channel
			if (it.next().getName().compareTo(clientSessionListener.getName()) == 0)
			{
				sendCommand(channel, clientSessionListener.getSession(), ServerClientProtocol.eEvenements.ChatChannel, msg);
				return;
			}
		}
		throw new SEPServerDiffuserMessageException("Le client \""+clientSessionListener.getName()+"\" n'est pas présent dans le channel \""+channel.getName()+"\"");
	}

	/**
	 * @param clientSession
	 * @param nomChannel
	 * @param user
	 * @param msg
	 * @throws SEPServerDiffuserMessageException 
	 */
	public void diffuserMessagePrive(SEPServerClientSessionListener clientSessionListener, String nomChannel, String nomUser, String msg) throws SEPServerDiffuserMessageException
	{
		Channel channel = null;
		try
		{
			channel = AppContext.getChannelManager().getChannel(nomChannel);
		}
		catch(NameNotBoundException e)
		{
			throw new SEPServerDiffuserMessageException("Channel introuvable \""+nomChannel+"\"");
		}
		
		boolean senderFound = false;
		boolean receiverFound = false;
		
		Iterator<ClientSession> it = channel.getSessions();
		ClientSession receiver=null;
		while(it.hasNext())
		{
			ClientSession user = it.next();
			if (user.getName().compareTo(clientSessionListener.getName()) == 0)
			{
				senderFound = true;
				if (receiverFound) break;
			}
			if (user.getName().compareTo(nomUser) == 0)
			{
				receiver = user;
				receiverFound = true;
				if (senderFound) break;
			}
		}
		
		if (senderFound && receiverFound)
		{
			sendCommand(receiver, ServerClientProtocol.eEvenements.ChatUserChannel, clientSessionListener.getName(), msg);
			return;
		}
		
		throw new SEPServerDiffuserMessageException("Le client \""+clientSessionListener.getName()+"\" et/ou le destinataire \"" + nomUser +"\" n'est pas présent dans le channel \""+channel.getName()+"\"");
	}
}
