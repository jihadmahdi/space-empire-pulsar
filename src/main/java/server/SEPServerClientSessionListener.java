/**
 * @author Escallier Pierre
 * @file SEPServerClientSessionListener.java
 * @date 23 juin 08
 */
package server;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import EPLib.EPMachineEtats.EPMachineEtats;
import EPLib.EPMachineEtats.EPMachineEtats.EPMachineEtatsAlreadyExistsException;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.ClientSessionListener;
import com.sun.sgs.app.DataManager;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import common.ClientServerProtocol.eEtats;
import common.ClientServerProtocol.eEvenements;
import common.ClientServerProtocol.eTransitions;

/**
 * 
 */
public class SEPServerClientSessionListener implements Serializable, ClientSessionListener, ManagedObject
{
	/** Serialization version. */
	private static final long	serialVersionUID	= 1L;
	
	/** The {@link Logger} for this class. */
    private static final Logger logger = Logger.getLogger(SEPServerClientSessionListener.class.getName());
    
	private static final class SEPServerClientStateMachine implements Serializable
	{
		/** Serialization version. */
		private static final long	serialVersionUID	= 1L;
		
		private EPMachineEtats<eEvenements, eEtats, eTransitions> machine;
		
		public SEPServerClientStateMachine(SEPServerClientSessionListener clientSession)
		{
			String machineName = SEPServerClientStateMachine.class.getName()+"."+clientSession.getName(); 
			try
			{
				machine = EPMachineEtats.Creer(machineName);
			}
			catch (EPMachineEtatsAlreadyExistsException e)
			{
				throw new RuntimeException("Une machine-états de ce nom existe déjà : \""+machineName+"\"", e);
			}
			
			// TODO machine-états
		}
		
		protected boolean traiterEvenement(eEvenements evnt, Object ... parametres)
		{
			return machine.TraiterEvenement(evnt, parametres);
		}
	}
	
	/** Session en cours. (null si déconnecté).*/
	ManagedReference<ClientSession> refSession;
	
	/** Nom, qui ne devrait pas changer d'une session à l'autre. TODO à vérifier */
	String name;

	public String getName()
	{
		return name;
	}
	
	/* (non-Javadoc)
	 * @see com.sun.sgs.app.ClientSessionListener#disconnected(boolean)
	 */
	@Override
	public void disconnected(boolean graceful)
	{
		String log = "User "+getName()+" loggedOut "+(graceful?"gracefull":"forced");
        logger.log(Level.INFO, log);
        setSession(null);
	}

	/* (non-Javadoc)
	 * @see com.sun.sgs.app.ClientSessionListener#receivedMessage(java.nio.ByteBuffer)
	 */
	@Override
	public void receivedMessage(ByteBuffer message)
	{
		logger.log(SEPServer.traceLevel, "User "+getName()+" receivedMessage");
	}

	/**
	 * @param session
	 */
	public void setSession(ClientSession session)
	{
		logger.log(SEPServer.traceLevel, "User session change");
		DataManager dm = AppContext.getDataManager();
		dm.markForUpdate(this);
		
		if (session == null)
		{
			refSession = null;
		}
		else
		{
			refSession = dm.createReference(session);
			name = session.getName();
		}
	}

}
