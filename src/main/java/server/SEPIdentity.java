/**
 * @author Escallier Pierre
 * @file SEPIdentity.java
 * @date 7 juil. 2008
 */
package server;

import java.io.Serializable;
import java.util.logging.Logger;
import com.sun.sgs.auth.Identity;

/**
 * 
 */
public class SEPIdentity implements Identity, Serializable
{
	private static final Logger	logger				= Logger.getLogger(SEPIdentity.class.getName());

	private static final long	serialVersionUID	= 1L;

	private final String		name;
	private final String		cryptedPwd;
	private final boolean		isAdmin;
	
	public SEPIdentity(String name, String clearPwd, boolean isAdmin)
	{
		this.name = name;
		this.cryptedPwd = String.valueOf(clearPwd.hashCode());
		this.isAdmin = isAdmin;
	}
	
	public boolean isAdmin()
	{
		return isAdmin;
	}
	
	public boolean testPwd(String clearPwd)
	{
		return (String.valueOf(clearPwd.hashCode()).compareTo(this.cryptedPwd) == 0);
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
	
	public String toString()
	{
		return getName();
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
	
	public String getUID()
	{
		return name+"@"+cryptedPwd;
	}

}
