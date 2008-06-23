/**
 * @author Escallier Pierre
 * @file SEPServerClientSessionListener.java
 * @date 23 juin 08
 */
package server;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

import server.SEPServer.SEPServerCreerNouvellePartieException;
import utils.SEPUtils;
import EPLib.EPMachineEtats.EPMachineEtats;
import EPLib.EPMachineEtats.EPMachineEtats.EPMachineEtatsException;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.ClientSessionListener;
import com.sun.sgs.app.DataManager;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import common.Command;
import common.ServerClientProtocol;
import common.ClientServerProtocol.eEtats;
import common.ClientServerProtocol.eEvenements;
import common.metier.ConfigPartie;

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
		
		private final EPMachineEtats<eEtats, eEvenements> machine;
		private final SEPServerClientSessionListener clientSession;
		private final String nom;
		
		public SEPServerClientStateMachine(final SEPServerClientSessionListener clientSession, final String nom)
		{
			this.clientSession = clientSession;
			this.nom = nom;
			
			String machineName = SEPServerClientStateMachine.class.getName()+"."+nom; 

			logger.log(Level.INFO, "EPMachineEtats.Creer(\""+machineName+"\")");
			machine = EPMachineEtats.Creer(machineName);
			
			try
			{
				machine.AjouterEtat(eEtats.AttenteCommande);
				machine.AjouterEtat(eEtats.AttenteCreationPartie);
				machine.AjouterEtat(eEtats.PartieEnCours);
				
				machine.AjouterEntryEvent(new EPMachineEtats.EPOperationCourteSansParametres()
				{
					private static final long	serialVersionUID	= 1L;

					@Override
					public void run()
					{
						// TODO	
					}
				
					@Override
					public String getName()
					{
						return "Tenter reconnexion";
					}
				
				}, eEtats.AttenteCommande);
				machine.AjouterEvenement(eEvenements.DemandeListeParties, new EPMachineEtats.EPOperationCourteSansParametres()
				{
					private static final long	serialVersionUID	= 1L;

					@Override
					public void run()
					{
						SEPServer server = SEPServer.getServer();
						Hashtable<String, ConfigPartie> nouvellesParties = server.getNouvellesParties();
						clientSession.sendCommand(ServerClientProtocol.eEvenements.ReponseDemandeListeParties, nouvellesParties);
					}
				
					@Override
					public String getName()
					{
						return "Envoyer liste parties";
					}
				
				}, eEtats.AttenteCommande, true);
				
				machine.AjouterTransition(eEvenements.CreerNouvellePartie, new EPMachineEtats.EPOperationCourteAvecParametres()
				{
					private static final long	serialVersionUID	= 1L;

					@Override
					public void run(Object ... objects)
					{
						SEPUtils.checkParametersTypes(1, objects, "CreerNouvellePartie", ConfigPartie.class);
						SEPServer server = SEPServer.getServer();
						ConfigPartie cfgPartie = (ConfigPartie) objects[0];
						
						try
						{
							server.creerNouvellePartie(cfgPartie);
						}
						catch (SEPServerCreerNouvellePartieException e)
						{
							clientSession.sendCommand(ServerClientProtocol.eEvenements.ErreurCreerNouvellePartie, e.getMessage());
							return;
						}
						
						server.joindreNouvellePartie(clientSession, cfgPartie);
					}
				
					@Override
					public String getName()
					{
						return "Créer et joindre nouvelle partie";
					}
				
				}, eEtats.AttenteCommande, eEtats.AttenteCreationPartie);
				
			}
			catch (EPMachineEtatsException e)
			{
				String msg = "Erreur de création de la machine-états pour "+clientSession.getName();
				logger.log(Level.SEVERE, msg);
				throw new RuntimeException(msg, e);
			}
			
			machine.Demarrer();
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
	
	SEPServerClientStateMachine machineEtats;

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
		Command command;
		try
		{
			command = Command.decode(message);
		}
		catch (IOException e)
		{
			logger.log(Level.WARNING, "Received unreadable command from user "+getName()+" : \""+message+"\"");
			return;
		}

		logger.log(SEPServer.traceLevel, "Received command from user "+getName()+" : "+command.getCommand());
		
		machineEtats.traiterEvenement(eEvenements.valueOf(command.getCommand()), command.getParameters());
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
			if (machineEtats == null)
			{
				machineEtats = new SEPServerClientStateMachine(this, name);
			}
		}
	}

	protected void sendCommand(ServerClientProtocol.eEvenements evnt, Serializable ... parameters)
    {
    	Command cmd = new Command(evnt.toString(), parameters);
        logger.log(Level.INFO, "Envoi commande {0} au serveur", cmd.getCommand());
        try
		{       	
        	refSession.get().send(cmd.encode());
		}
		catch (IOException e1)
		{
			logger.log(Level.WARNING, "Unable to send to "+getName()+" the command \""+cmd.getCommand()+"\"");
		}
    }
}
