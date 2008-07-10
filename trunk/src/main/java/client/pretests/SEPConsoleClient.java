/**
 * @author Escallier Pierre
 * @file SEPConsoleClient.java
 * @date 23 juin 08
 */
package client.pretests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.PasswordAuthentication;
import java.nio.ByteBuffer;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.orfjackal.darkstar.rpc.comm.ClientChannelAdapter;
import net.orfjackal.darkstar.rpc.comm.RpcGateway;

import server.SEPServer;
import utils.SEPUtils;

import com.sun.sgs.app.Channel;
import com.sun.sgs.client.ClientChannel;
import com.sun.sgs.client.ClientChannelListener;
import com.sun.sgs.client.simple.SimpleClient;
import com.sun.sgs.client.simple.SimpleClientListener;
import common.ClientServerProtocol;
import common.Command;
import common.IServerUser;
import common.ServerClientProtocol;
import common.metier.ConfigPartie;
import common.metier.PartieEnCreation;

/**
 * 
 */
public class SEPConsoleClient implements SimpleClientListener, Runnable
{

	private static final Logger							logger			= Logger.getLogger(SEPConsoleClient.class.getName());

	private SimpleClient								client;

	private static BufferedReader					keyboard		= new BufferedReader(new InputStreamReader(System.in));

	private static PrintStream					display			= System.out;

	private String										status;

	private String										userName;

	private String										password;

	private IServerUser									serverUser;

	protected Hashtable<ClientChannel, Vector<String>>	listeChannels	= new Hashtable<ClientChannel, Vector<String>>();

	public static void setDisplay(PrintStream display)
	{
		SEPConsoleClient.display = display;
	}
	
	public static void setKeyboard(BufferedReader keyboard)
	{
		SEPConsoleClient.keyboard = keyboard;
	}
	
	private void initServices(RpcGateway gateway)
	{
		Set<IServerUser> serverUsers = gateway.remoteFindByType(IServerUser.class);
		assert serverUsers.size() == 1;
		serverUser = serverUsers.iterator().next();
	}

	private void resetServices()
	{
		serverUser = null;
	}

	public IServerUser getServerUser()
	{
		if (serverUser == null)
		{
			throw new IllegalStateException("Not connected to server");
		}
		return serverUser;
	}

	public SEPConsoleClient()
	{
		client = new SimpleClient(this);
	}

	private static String readLine()
	{
		String line = null;
		do
		{
			try
			{
				line = keyboard.readLine();
			}
			catch (IOException e)
			{
				display.println("Erreur de lecture clavier, recommencez");
			}
		} while (line == null);

		return line;
	}

	private static String getInput(String msg, String defaultValue)
	{
		display.println(msg + ((defaultValue == null) ? "" : " [" + defaultValue + "]") + " : ");
		String value;
		do
		{
			value = readLine();
			if ((defaultValue != null) && ((value == null) || (value.isEmpty())))
			{
				return defaultValue;
			}
		} while (value == null);

		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.sgs.client.simple.SimpleClientListener#getPasswordAuthentication()
	 */
	@Override
	public PasswordAuthentication getPasswordAuthentication()
	{
		return new PasswordAuthentication(userName, password.toCharArray());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.sgs.client.simple.SimpleClientListener#loggedIn()
	 */
	@Override
	public void loggedIn()
	{
		logger.log(Level.INFO, "loggedIn");
		status = "loggedIn";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.sgs.client.simple.SimpleClientListener#loginFailed(java.lang.String)
	 */
	@Override
	public void loginFailed(String reason)
	{
		logger.log(Level.WARNING, "loginFailed : \"" + reason + "\"");
		status = "loginFailed";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.sgs.client.ServerSessionListener#disconnected(boolean, java.lang.String)
	 */
	@Override
	public void disconnected(boolean graceful, String reason)
	{
		resetServices();

		String msg = "disconnected " + (graceful ? "gracefull" : "forced");
		if ((reason != null) && ( !reason.isEmpty()))
		{
			msg += " : \"" + reason + "\"";
		}
		logger.log(graceful ? Level.INFO : Level.WARNING, msg);
		status = msg;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.sgs.client.ServerSessionListener#joinedChannel(com.sun.sgs.client.ClientChannel)
	 */
	@Override
	public ClientChannelListener joinedChannel(ClientChannel channel)
	{
		final ClientChannelAdapter adapter = new ClientChannelAdapter();
		ClientChannelListener listener = adapter.joinedChannel(channel);
		
		Thread runLater = new Thread(new Runnable()
		{
		
			@Override
			public void run()
			{
				try
				{
					Thread.sleep(2000);
				}
				catch(InterruptedException e)
				{
					e.printStackTrace();
				}
				
				initServices(adapter.getGateway());
			}
		});

        runLater.start();
		
		return listener;

		/*
		 * final SEPConsoleClient consoleClient = this; final ClientChannel joinedChannel = channel;
		 * 
		 * logger.log(Level.INFO, "joinedChannel \"" + channel.getName() + "\""); listeChannels.put(channel, new Vector<String>());
		 * 
		 * return new ClientChannelListener() { SEPConsoleClient client = consoleClient;
		 * 
		 * ClientChannel channel = joinedChannel;
		 * 
		 * @Override public void receivedMessage(ClientChannel channel, ByteBuffer message) { Command command; try { command = Command.decode(message); } catch (IOException e) { logger.log(Level.WARNING, "Received unreadable command from channel \"" + channel.getName() + "\" : \"" + message + "\""); return; }
		 * 
		 * ServerClientProtocol.eEvenements evt;
		 * 
		 * try { evt = ServerClientProtocol.eEvenements.valueOf(command.getCommand()); } catch (IllegalArgumentException e) { logger.log(Level.WARNING, "Received unknown command from channel \"" + channel.getName() + "\" : \"" + command.getCommand() + "\""); return; }
		 * 
		 * logger.log(SEPServer.traceLevel, "ReceivedMessage from channel \"" + channel.getName() + "\" : " + command.getCommand());
		 * 
		 * switch (evt) { case refreshChannelUserList: { refreshChannelUserList(channel, command.getParameters()); break; } default: { logger.log(Level.INFO, "Command \"" + evt.toString() + "\" from channel \"" + channel.getName() + "\" ignored."); } } }
		 * 
		 * @Override public void leftChannel(ClientChannel channel) { logger.log(Level.INFO, "left channel \"" + channel.getName() + "\""); listeChannels.remove(channel); }
		 * 
		 * };
		 */
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.sgs.client.ServerSessionListener#receivedMessage(java.nio.ByteBuffer)
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
			logger.log(Level.WARNING, "Received unreadable command : \"" + message + "\"");
			return;
		}

		ServerClientProtocol.eEvenements evt;

		try
		{
			evt = ServerClientProtocol.eEvenements.valueOf(command.getCommand());
		}
		catch (IllegalArgumentException e)
		{
			logger.log(Level.WARNING, "Received unknown command : \"" + command.getCommand() + "\"");
			return;
		}

		logger.log(SEPServer.traceLevel, "Received command : " + command.getCommand());

		/*
		 * switch (evt) { case ReponseDemandeListeParties: { reponseDemandeListeParties(command.getParameters()); break; } case ErreurCreerNouvellePartie: case ErreurJoindreNouvellePartie: { afficherErreur(evt, command.getParameters()); break; } default: { logger.log(Level.INFO, "Command \"" + evt.toString() + "\" ignored."); } }
		 */
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.sgs.client.ServerSessionListener#reconnected()
	 */
	@Override
	public void reconnected()
	{
		logger.log(Level.WARNING, "reconnected");
		status = "reconnected";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.sgs.client.ServerSessionListener#reconnecting()
	 */
	@Override
	public void reconnecting()
	{
		logger.log(Level.WARNING, "reconnecting");
		status = "reconnecting";
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		SEPConsoleClient consoleClient = new SEPConsoleClient();

		consoleClient.run();
	}

	private static enum eCommande
	{
		newClient, status, login, logout, send, demandeListeParties, creerPartie, joindrePartie, listerChannels, channelSend, listerUserChannel, channelChat, channelUserChat
	};

	public void test()
	{
		int choix;
		do
		{
			display.println(getStatus());

			for (int i = 0; i < 4/* eCommande.values().length */; ++i)
			{
				display.println(i + 1 + "] " + eCommande.values()[i]);
			}

			Method[] methods = IServerUser.class.getDeclaredMethods();
			for (int i = 0; i < methods.length; ++i)
			{
				display.println(i + 4 + "] " + methods[i].toGenericString());
			}

			display.println("0] Quitter");

			do
			{
				try
				{
					choix = Integer.valueOf(readLine());
				}
				catch (Exception e)
				{
					display.println("Saisie incorrecte");
					choix = -1;
				}
			} while (choix < 0);

			if (choix == 0) continue;

			if (choix < 4)
			{
				eCommande cmd = eCommande.values()[choix - 1];

				switch (cmd)
				{
				case newClient:
				{
					if (client.isConnected()) client.logout(true);
					client = new SimpleClient(this);
				}
				case login:
				{
					login();
					break;
				}
				case logout:
				{
					logout();
					break;
				}
				case send:
				{
					send();
					break;
				}
				case status:
				{
					continue;
				}
				case demandeListeParties:
				{
					demandeListeParties();
					break;
				}
				case creerPartie:
				{
					creerPartie();
					break;
				}
				case joindrePartie:
				{
					joindrePartie();
					break;
				}
				case listerChannels:
				{
					listerChannels();
					break;
				}
				case channelSend:
				{
					channelSend();
					break;
				}
				case listerUserChannel:
				{
					listerUserChannel();
					break;
				}
				case channelChat:
				{
					channelChat();
					break;
				}
				case channelUserChat:
				{
					channelUserChat();
					break;
				}
				default:
					continue;
				}
			}
			else
			{
				Method method = methods[choix-4];
				try
				{
					Object result = method.invoke(getServerUser());
				}
				catch (IllegalArgumentException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch (IllegalAccessException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch (InvocationTargetException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} while (choix != 0);
	}

	private void channelUserChat()
	{
		/*
		ClientChannel channel = saisirChannel();
		if (channel == null) return;

		Vector<String> users = listeChannels.get(channel);
		for (int i = 0; i < users.size(); ++i)
		{
			display.println(i + 1 + "] " + users.get(i));
		}

		int choix;
		do
		{
			choix = Integer.valueOf(getInput("n° destinataire", "1"));
		} while ((choix < 1) || (choix > users.size()));

		String msg = getInput("Message", null);

		sendCommand(ClientServerProtocol.eEvenements.ChatUserChannel, channel.getName(), users.get(choix - 1), msg);
		*/
	}

	private void channelChat()
	{
		/*
		ClientChannel channel = saisirChannel();
		if (channel == null) return;

		String msg = getInput("Message", null);
		sendCommand(ClientServerProtocol.eEvenements.ChatChannel, channel.getName(), msg);
		*/
	}

	private void listerUserChannel()
	{
		ClientChannel channel = saisirChannel();
		if (channel == null) return;

		Vector<String> users = listeChannels.get(channel);
		display.println("Utilisateur du channel \"" + channel.getName() + "\" : " + users.toString());
	}

	private ClientChannel saisirChannel()
	{
		if (listeChannels.size() < 1)
		{
			display.println("Aucun channel joint.");
			return null;
		}

		ClientChannel[] channels = listeChannels.keySet().toArray(new ClientChannel[0]);

		for (int i = 0; i < channels.length; ++i)
		{
			display.println(i + 1 + "] " + channels[i].getName());
		}

		int choix;
		do
		{
			choix = Integer.valueOf(getInput("n° channel", "1"));
		} while ((choix < 1) || (choix > channels.length));

		return channels[choix - 1];
	}

	private void channelSend()
	{
		ClientChannel channel = saisirChannel();
		if (channel == null) return;

		String msg = getInput("Message", null);
		display.println("channel.send(\"" + msg + "\")");
		try
		{
			channel.send(ByteBuffer.wrap(msg.getBytes()));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private void listerChannels()
	{
		display.println("Liste des channels joins :");
		Iterator<ClientChannel> it = listeChannels.keySet().iterator();
		while (it.hasNext())
		{
			ClientChannel channel = it.next();
			display.print(channel.getName());
			if (it.hasNext()) display.print(", ");
		}
		display.println();
	}

	private void refreshChannelUserList(ClientChannel channel, Object ... parametres)
	{
		SEPUtils.checkParametersTypes(1, parametres, "refreshChannelUserList", Vector.class);
		Vector<String> listeUsers;
		try
		{
			listeUsers = (Vector<String>) parametres[0];
		}
		catch (ClassCastException e)
		{
			throw new IllegalArgumentException("refreshChannelUserList : parameters[0] expected to be a \"Vector<String>\" instance, but is a \"" + parametres[0].getClass().getName() + "\" one.");
		}

		logger.log(Level.INFO, "refresh channel \"" + channel.getName() + "\" user list : " + listeUsers.toString());
		listeChannels.put(channel, listeUsers);
	}

	private void afficherErreur(ServerClientProtocol.eEvenements evt, Object ... parametres)
	{
		StringBuilder sb = new StringBuilder();

		sb.append(evt.toString() + " :");
		for (Object o : parametres)
		{
			if (String.class.isInstance(o))
			{
				sb.append("\n" + ((String) o));
			}
		}

		display.println(sb.toString());
	}

	private void joindrePartie()
	{
		/*
		String nomPartie = getInput("Nom partie", "partie de " + userName);

		display.println("sendCommand(" + ClientServerProtocol.eEvenements.JoindreNouvellePartie + ", \"" + nomPartie + "\")");
		sendCommand(ClientServerProtocol.eEvenements.JoindreNouvellePartie, nomPartie);
		*/
	}

	private void creerPartie()
	{
		/*
		String nomPartie = getInput("Nom partie", "partie de " + userName);
		ConfigPartie configPartie = saisirConfigPartie();

		display.println("sendCommand(" + ClientServerProtocol.eEvenements.CreerNouvellePartie + ", " + "\"" + nomPartie + "\", " + configPartie + ")");
		sendCommand(ClientServerProtocol.eEvenements.CreerNouvellePartie, nomPartie, configPartie);
		*/
	}

	private static ConfigPartie saisirConfigPartie()
	{
		ConfigPartie configPartie = new ConfigPartie();

		String valeur = getInput("Dimension X", "default");
		if (valeur.compareToIgnoreCase("default") != 0) configPartie.setDimX(Integer.valueOf(valeur));

		valeur = getInput("Dimension Y", "default");
		if (valeur.compareToIgnoreCase("default") != 0) configPartie.setDimY(Integer.valueOf(valeur));

		valeur = getInput("Dimension Z", "default");
		if (valeur.compareToIgnoreCase("default") != 0) configPartie.setDimZ(Integer.valueOf(valeur));

		valeur = getInput("Nombre de corps célestes neutres", "default");
		if (valeur.compareToIgnoreCase("default") != 0) configPartie.setNbCorpsCelestesNeutres(Integer.valueOf(valeur));

		// TODO configPartie.setQtCarboneDepartCorpsCelestes(typeCorpsCeleste, min, max)
		// TODO configPartie.setQtSlotsCorpsCelestes(typeCorpsCeleste, min, max)

		valeur = getInput("Activer Victoire Totale", "default");
		if (valeur.compareToIgnoreCase("default") != 0) configPartie.setConqueteTotale(Boolean.valueOf(valeur));

		valeur = getInput("Activer Régimicide", "default");
		if (valeur.compareToIgnoreCase("default") != 0) configPartie.setRegimicide(Boolean.valueOf(valeur));

		valeur = getInput("Activer Assimilation des peuples neutralisés", "default");
		if (valeur.compareToIgnoreCase("default") != 0) configPartie.setAssimilerPeuplesNeutralises(Boolean.valueOf(valeur));

		// TODO configPartie.setVictoireEconomique(seuilPopulation, seuilCarbone)

		valeur = getInput("Activer Victoire en Alliance", "default");
		if (valeur.compareToIgnoreCase("default") != 0) configPartie.setVictoireEnAlliance(Boolean.valueOf(valeur));

		valeur = getInput("Temps limite", "default");
		if (valeur.compareToIgnoreCase("default") != 0) configPartie.setVictoireTempsLimite(Integer.valueOf(valeur));

		return configPartie;
	}

	private void demandeListeParties()
	{
		/*
		display.println("sendCommand(" + ClientServerProtocol.eEvenements.DemandeListeParties + ")");
		sendCommand(ClientServerProtocol.eEvenements.DemandeListeParties);
		*/
	}

	private void reponseDemandeListeParties(Object ... parameters)
	{
		SEPUtils.checkParametersTypes(1, parameters, "reponseDemandeListeParties", Hashtable.class);
		Hashtable<String, PartieEnCreation> nouvellesParties;
		try
		{
			nouvellesParties = (Hashtable<String, PartieEnCreation>) parameters[0];
		}
		catch (ClassCastException e)
		{
			throw new IllegalArgumentException("reponseDemandeListeParties : parameters[0] expected to be a \"Hashtable<String, ConfigPartie>\" instance, but is a \"" + parameters[0].getClass().getName() + "\" one.");
		}

		StringBuilder sb = new StringBuilder();
		Iterator<String> itNomParties = nouvellesParties.keySet().iterator();
		while (itNomParties.hasNext())
		{
			sb.append(itNomParties.next());
			if (itNomParties.hasNext())
			{
				sb.append(", ");
			}
		}

		display.println("Liste des parties en cours de création : " + sb.toString());
	}

	/**
	 * 
	 */
	private void send()
	{
		String msg = getInput("Message", null);
		display.println("send(\"" + msg + "\")");
		try
		{
			client.send(ByteBuffer.wrap(msg.getBytes()));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	private void logout()
	{
		String force = getInput("forcer", "false");
		boolean bForce = (force.compareTo("false") != 0);
		display.println("logout(" + bForce + ")");
		client.logout(bForce);
	}

	/**
	 * 
	 */
	private void login()
	{
		String host = getInput("Entrer adresse serveur", "localhost");
		String port = getInput("Entrer port serveur", "1139");

		userName = getInput("Enter username", "guest");
		password = getInput("Enter password", "pwd");

		Properties connectProps = new Properties();
		connectProps.put("host", host);
		connectProps.put("port", port);

		try
		{
			display.println("login(\"" + host + "\", \"" + port + "\")");
			client.login(connectProps);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			disconnected(false, e.getMessage());
		}
	}

	/**
	 * @return
	 */
	public String getStatus()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("[" + (client.isConnected() ? "Connected" : "Disconnected") + "] ");
		sb.append(status);
		return sb.toString();
	}

	protected void sendCommand(ClientChannel channel, ClientServerProtocol.eEvenements evnt, Serializable ... parameters)
	{
		Command cmd = new Command(evnt.toString(), parameters);
		logger.log(Level.INFO, "Envoi commande \"" + cmd.getCommand() + "\" au channel \"" + channel.getName() + "\"");
		try
		{
			channel.send(cmd.encode());
		}
		catch (IOException e)
		{
			logger.log(Level.WARNING, "Unable to send command \"" + cmd.getCommand() + "\" to channel  \"" + channel.getName() + "\"");
		}
	}

	protected void sendCommand(ClientServerProtocol.eEvenements evnt, Serializable ... parameters)
	{
		Command cmd = new Command(evnt.toString(), parameters);
		logger.log(Level.INFO, "Envoi commande {0} au serveur", cmd.getCommand());
		try
		{
			client.send(cmd.encode());
		}
		catch (IOException e1)
		{
			logger.log(Level.WARNING, "Unable to send command \"" + cmd.getCommand() + "\"");
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run()
	{
		test();
	}
}
