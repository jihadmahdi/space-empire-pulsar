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
import java.net.PasswordAuthentication;
import java.nio.ByteBuffer;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.sgs.client.ClientChannel;
import com.sun.sgs.client.ClientChannelListener;
import com.sun.sgs.client.simple.SimpleClient;
import com.sun.sgs.client.simple.SimpleClientListener;

/**
 * 
 */
public class SEPConsoleClient implements SimpleClientListener
{
	
	private static final Logger		logger	= Logger.getLogger(SEPConsoleClient.class.getName());

	private final SimpleClient		client;

	private final BufferedReader	keyboard;

	private final PrintStream		display;
	
	private String status;
	
	private String userName;
	private String password;

	public SEPConsoleClient()
	{
		client = new SimpleClient(this);
		keyboard = new BufferedReader(new InputStreamReader(System.in));
		display = System.out;
	}

	private String readLine()
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
	
	private String getInput(String msg, String defaultValue)
	{
		display.println(msg+((defaultValue==null)?"":" ["+defaultValue+"]")+" : ");
		String value;
		do
		{
			value = readLine();
			if ((defaultValue != null) && ((value == null) || (value.isEmpty())))
			{
				return defaultValue;
			}
		}while(value == null);
		
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
		final SEPConsoleClient consoleClient = this;
		final ClientChannel joinedChannel = channel;

		return new ClientChannelListener()
		{
			SEPConsoleClient	client	= consoleClient;

			ClientChannel		channel	= joinedChannel;

			@Override
			public void receivedMessage(ClientChannel channel, ByteBuffer message)
			{
				logger.log(Level.INFO, "receivedMessage from channel \"" + channel.getName() + "\"");
			}

			@Override
			public void leftChannel(ClientChannel channel)
			{
				logger.log(Level.INFO, "left channel \"" + channel.getName() + "\"");
			}

		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.sgs.client.ServerSessionListener#receivedMessage(java.nio.ByteBuffer)
	 */
	@Override
	public void receivedMessage(ByteBuffer message)
	{
		logger.log(Level.INFO, "receivedMessage");
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

		consoleClient.test();
	}

	private static enum eCommande
	{
		status,
		login,
		logout,
		send		
	};

	public void test()
	{
		int choix;
		do
		{
			display.println(getStatus());
			for (int i = 0; i < eCommande.values().length; ++i)
			{
				display.println(i + 1 + "] " + eCommande.values()[i]);
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

			eCommande cmd = eCommande.values()[choix - 1];

			switch (cmd)
			{
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
			
			default:
				continue;
			}

		} while (choix != 0);
	}

	/**
	 * 
	 */
	private void send()
	{
		String msg = getInput("Message", null);
		display.println("send(\""+msg+"\")");
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
		display.println("logout("+bForce+")");
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
        	display.println("login(\""+host+"\", \""+port+"\")");
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
	private String getStatus()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("["+(client.isConnected()?"Connected":"Disconnected")+"] ");
		sb.append(status);
		return sb.toString();
	}
}
