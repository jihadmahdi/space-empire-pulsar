/**
 * @author Escallier Pierre
 * @file SEPServer.java
 * @date 23 juin 08
 */
package server;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.AppListener;
import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.ClientSessionListener;
import com.sun.sgs.app.DataManager;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.NameNotBoundException;
import com.sun.sgs.app.util.ScalableHashMap;
import common.metier.ConfigPartie;

/**
 * 
 */
public class SEPServer implements Serializable, AppListener
{
	private static final String SERVER_BINDING = "Server";
	
	public static final Level traceLevel = Level.INFO;
	
	/** Serialization version number */
	private static final long	serialVersionUID	= 1L;

	/** The {@link Logger} for this class. */
	private static final Logger	logger				= Logger.getLogger(SEPServer.class.getName());
	
	public static class SEPServerException extends Exception
	{

		private static final long	serialVersionUID	= 1L;
		SEPServerException() {super();}
		SEPServerException(String msg) {super(msg);}
		
	}
	public static class SEPServerCreerNouvellePartieException extends SEPServerException
	{

		private static final long	serialVersionUID	= 1L;
		
		public SEPServerCreerNouvellePartieException(String raison)
		{
			super(raison);
		}
	}
	
	protected ManagedReference<ScalableHashMap<String, ConfigPartie>> refNouvellesParties;

	public SEPServer()
	{
		logger.log(traceLevel, "SEPServer constructor");
		DataManager dm = AppContext.getDataManager();

		String boundName = dm.nextBoundName(null);
		StringBuilder sb = new StringBuilder();
		while (boundName != null)
		{
			sb.append(boundName);
			boundName = dm.nextBoundName(boundName);
			if (boundName != null)
			{
				sb.append(", ");
			}
		}

		logger.log(traceLevel, "SEPServer bounds list: " + sb.toString());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.sgs.app.AppListener#initialize(java.util.Properties)
	 */
	@Override
	public void initialize(Properties props)
	{
		logger.log(Level.INFO, "SEPServer initializing.");
		
		DataManager dm = AppContext.getDataManager();
		
		logger.log(Level.INFO, "Binding Server in DataManager.");
		dm.setBinding(SERVER_BINDING, this);
		
		logger.log(Level.INFO, "Create empty nouvellesParties hashmap.");
		refNouvellesParties = dm.createReference(new ScalableHashMap<String, ConfigPartie>());
		
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
		// On regarde si l'utilisateur a déjà un profil enregistré (qui sert également de ClientSessionListener)
		SEPServerClientSessionListener joueur;
		DataManager dm = AppContext.getDataManager();
		try
		{
			joueur = (SEPServerClientSessionListener) dm.getBinding(session.getName());
			logger.log(Level.INFO, "Known User loggedIn : \"" + session.getName() + "\"");
		}
		catch (NameNotBoundException ex)
		{
			// Sinon on créé un nouveau profil.
			logger.log(Level.WARNING, "New User loggedIn : \"" + session.getName() + "\"");
			joueur = new SEPServerClientSessionListener();
		}

		joueur.setSession(session);
		return joueur;
	}
	
	protected static SEPServer getServer()
	{
		DataManager dm = AppContext.getDataManager();
		ManagedObject obj = dm.getBinding(SERVER_BINDING);
		
		if (!SEPServer.class.isInstance(obj))
		{
			String msg = "Object binded to \""+SERVER_BINDING+"\" is not a SEPServer object";
			logger.log(Level.SEVERE, msg);
			throw new RuntimeException(msg);
		}
		
		return (SEPServer) obj;
	}
	
	protected Hashtable<String, ConfigPartie> getNouvellesParties()
	{
		return new Hashtable<String, ConfigPartie>(refNouvellesParties.get());
	}

	/**
	 * @param configPartie
	 * @return
	 * @throws SEPServerCreerNouvellePartieException 
	 */
	public void creerNouvellePartie(ConfigPartie configPartie) throws SEPServerCreerNouvellePartieException
	{
		ScalableHashMap<String, ConfigPartie> nouvellesParties = refNouvellesParties.getForUpdate();
		if (nouvellesParties.containsKey(configPartie.getNom()))
		{
			throw new SEPServerCreerNouvellePartieException("Impossible de créer nouvelle partie \""+configPartie.getNom()+"\", une partie du même nom est déjà en création.");
		}
		
		nouvellesParties.put(configPartie.getNom(), configPartie);
	}

	/**
	 * @param clientSession
	 * @param cfgPartie
	 */
	public void joindreNouvellePartie(SEPServerClientSessionListener clientSession, ConfigPartie cfgPartie)
	{
		// TODO : créer un channel pour la partie en création.
		// Chaque partie (en création ou en cours) possède un channel à son nom.
		// Faire une classe PartieEnCreation pour gérer tout ça (découpler la classe ConfigPartie de l'état de partie en création) ?
	}
}
