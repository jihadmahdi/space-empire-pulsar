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
import java.util.logging.Logger;

import javax.security.auth.login.CredentialException;
import javax.security.auth.login.LoginException;

import sun.security.provider.MD5;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.DataManager;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.util.ScalableHashMap;
import com.sun.sgs.app.util.ScalableHashSet;
import com.sun.sgs.auth.Identity;
import com.sun.sgs.auth.IdentityAuthenticator;
import com.sun.sgs.auth.IdentityCredentials;
import com.sun.sgs.impl.auth.NamePasswordCredentials;

/**
 * 
 */
public class SEPIdentityAuthenticator implements IdentityAuthenticator
{	
	//private ManagedReference<ScalableHashMap<String, SEPIdentity>> refUsers;
	private Hashtable<String, SEPIdentity> users = new Hashtable<String, SEPIdentity>();

	private static final String	AUTHORIZED_USERS	= "AuthorizedUsers";

	/**
	 * 
	 */
	public SEPIdentityAuthenticator(Properties props)
	{
		/*
		DataManager dm = AppContext.getDataManager();
		
		ScalableHashMap<String, SEPIdentity> initialUsersTable = new ScalableHashMap<String, SEPIdentity>();
		initialUsersTable.put("guest", new SEPIdentity("guest", "pwd", false));
		initialUsersTable.put("admin", new SEPIdentity("admin", "admin", true));
		refUsers = dm.createReference(initialUsersTable);
		*/
		users.put("guest", new SEPIdentity("guest", "pwd", false));
		for(int i=0; i < 10; ++i)
		{
			users.put("user"+i, new SEPIdentity("user"+i, "pwd"+i, false));
		}
		users.put("admin", new SEPIdentity("admin", "no", true));
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
		String pwd = new String(npc.getPassword());
		
		if (!users.containsKey(npc.getName()))
		{
				throw new CredentialException("Invalid username");
		}
	
		SEPIdentity id = users.get(npc.getName());
		if (!id.testPwd(pwd))
		{
			throw new CredentialException("Invalid password");
		}
		
		return id;
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
