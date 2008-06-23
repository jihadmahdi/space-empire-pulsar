/**
 * @author Escallier Pierre
 * @file SEPServer.java
 * @date 23 juin 08
 */
package server;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.AppListener;
import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.ClientSessionListener;
import com.sun.sgs.app.DataManager;
import com.sun.sgs.app.NameNotBoundException;

/**
 * 
 */
public class SEPServer implements Serializable, AppListener
{
	public static final Level traceLevel = Level.INFO;
	
	/** Serialization version number */
	private static final long	serialVersionUID	= 1L;

	/** The {@link Logger} for this class. */
	private static final Logger	logger				= Logger.getLogger(SEPServer.class.getName());

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
		logger.log(Level.INFO, "SEPServer initializing");
		
		logger.log(Level.INFO, "SEPServer initialized");
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

}
