/**
 * @author Escallier Pierre
 * @file SEPAccount.java
 * @date 9 juil. 2008
 */
package common;

import java.io.Serializable;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;

/**
 * 
 */
public class SEPAccount implements IUserAccount, ManagedObject, Serializable
{
	private static final long	serialVersionUID	= 1L;

	private final String uid;
	private ManagedReference<ClientSession> refSession;
	
	/**
	 * @param uid
	 */
	public SEPAccount(String uid)
	{
		this.uid = uid;
	}

	/* (non-Javadoc)
	 * @see common.IUserAccount#getSession()
	 */
	@Override
	public ClientSession getSession()
	{
		return (refSession==null)?null:refSession.get();
	}

	/* (non-Javadoc)
	 * @see common.IUserAccount#setSession(com.sun.sgs.app.ClientSession)
	 */
	@Override
	public void setSession(ClientSession session)
	{
		if ((getSession() != null) && (getSession().isConnected()))
		{
			throw new IllegalStateException("Connected client session cannot be replaced");
		}
		
		this.refSession = AppContext.getDataManager().createReference(session);
	}

}
