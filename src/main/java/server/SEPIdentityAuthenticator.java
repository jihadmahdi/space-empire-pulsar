/**
 * @author Escallier Pierre
 * @file SEPIdentityAuthenticator.java
 * @date 23 juin 08
 */
package server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.login.CredentialException;
import javax.security.auth.login.LoginException;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.DataManager;
import com.sun.sgs.app.NameNotBoundException;
import com.sun.sgs.auth.Identity;
import com.sun.sgs.auth.IdentityAuthenticator;
import com.sun.sgs.auth.IdentityCredentials;
import com.sun.sgs.impl.auth.NamePasswordCredentials;

/**
 * 
 */
public class SEPIdentityAuthenticator implements IdentityAuthenticator
{
	public static final class SEPIdentity implements Identity, Serializable
	{
		private static final Logger	logger				= Logger.getLogger(SEPIdentity.class.getName());

		private static final long	serialVersionUID	= 1L;

		private String				name;

		public SEPIdentity(String name)
		{
			this.name = name;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.sun.sgs.auth.Identity#getName()
		 */
		@Override
		public String getName()
		{
			return name;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.sun.sgs.auth.Identity#notifyLoggedIn()
		 */
		@Override
		public void notifyLoggedIn()
		{
			logger.log(SEPServer.traceLevel, "notifyLoggedIn");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.sun.sgs.auth.Identity#notifyLoggedOut()
		 */
		@Override
		public void notifyLoggedOut()
		{
			logger.log(SEPServer.traceLevel, "notifyLoggedOut");
		}

	}

	private static final String	AUTHORIZED_USERS	= "AuthorizedUsers";

	/**
	 * 
	 */
	public SEPIdentityAuthenticator(Properties props)
	{

	}

	private Hashtable<String, String> getAuthorizedUsers()
	{
		Hashtable<String, String> authorizedUsers;

		File f = new File("authorizedUsers");

		try
		{
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
			Object obj = ois.readObject();
			authorizedUsers = (Hashtable<String, String>) obj;
		}
		catch (Exception e)
		{
			authorizedUsers = new Hashtable<String, String>();
			authorizedUsers.put("guest", "pwd");

			try
			{
				f.createNewFile();
				ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
				oos.writeObject(authorizedUsers);
			}
			catch (Exception ex)
			{
				// nop
			}
		}

		return authorizedUsers;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.sgs.auth.IdentityAuthenticator#authenticateIdentity(com.sun.sgs.auth.IdentityCredentials)
	 */
	@Override
	public Identity authenticateIdentity(IdentityCredentials credentials) throws LoginException
	{
		if ( !NamePasswordCredentials.class.isInstance(credentials))
		{
			throw new CredentialException("unsupported credentials");
		}

		NamePasswordCredentials npc = (NamePasswordCredentials) credentials;

		Hashtable<String, String> authorizedUsers = getAuthorizedUsers();

		String pwd = new String(npc.getPassword());
		if ((pwd.isEmpty()) || (pwd.compareTo(authorizedUsers.get(npc.getName())) != 0))
		{
			throw new CredentialException("Invalid password");
		}

		return new SEPIdentity(npc.getName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.sgs.auth.IdentityAuthenticator#getSupportedCredentialTypes()
	 */
	@Override
	public String[] getSupportedCredentialTypes()
	{
		return new String[] {"NameAndPasswordCredentials"};
	}

}
