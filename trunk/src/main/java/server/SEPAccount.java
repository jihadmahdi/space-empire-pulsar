/**
 * @author Escallier Pierre
 * @file SEPAccount.java
 * @date 9 juil. 2008
 */
package server;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import sun.text.normalizer.CharTrie.FriendAgent;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.NameNotBoundException;
import com.sun.sgs.app.ObjectNotFoundException;

import common.FriendList;
import common.IUserAccount;
import common.FriendList.FriendInfo.FriendState;

/**
 * 
 */
public class SEPAccount implements IUserAccount, ManagedObject, Serializable
{
	private static final long	serialVersionUID	= 1L;
	private static final String ACCOUNT_BINDING_PREFIX = "Account@";
	private static final Logger logger = Logger.getLogger(SEPAccount.class.getName());

	private final String uid;
	private ManagedReference<ClientSession> refSession;
	private ManagedReference<SEPServerClientSessionListener> refSessionListener;
	
	private final FriendList friendList = new FriendList();
	
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
		try
		{
			return (refSession==null)?null:refSession.get();
		}
		catch(ObjectNotFoundException e)
		{
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see common.IUserAccount#setSession(com.sun.sgs.app.ClientSession)
	 */
	@Override
	public void setSession(ClientSession session)
	{
		if (session == null)
		{
			this.refSession = null;
			return;
		}
		
		if ((getSession() != null) && (getSession().isConnected()))
		{
			throw new IllegalStateException("Connected client session cannot be replaced");
		}
		
		this.refSession = AppContext.getDataManager().createReference(session);
	}
	
	protected void removeFriend(String friendName)
	{
		if (!friendList.containsKey(friendName)) return;
		friendList.remove(friendName);
	}
	
	protected void addFriend(String friendName) throws NameNotBoundException
	{
		if (friendList.containsKey(friendName)) return;
		
		SEPAccount friendAccount = null;
		friendAccount = getAccount(friendName);
		
		friendList.put(friendAccount.uid, null);
	}

	/**
	 * @param uid2
	 * @return
	 */
	public static SEPAccount getAccount(String uid) throws NameNotBoundException
	{
		return (SEPAccount) AppContext.getDataManager().getBinding(ACCOUNT_BINDING_PREFIX+uid);
	}

	/**
	 * @param account
	 */
	public static void saveAccount(SEPAccount account)
	{
		logger.log(Level.WARNING, "setBinding("+ACCOUNT_BINDING_PREFIX+account.uid+", "+account+")");
		AppContext.getDataManager().setBinding(ACCOUNT_BINDING_PREFIX+account.uid, account);
	}

	/**
	 * @return
	 */
	public FriendList getFriendList()
	{
		Iterator<String> it = friendList.keySet().iterator();
		while(it.hasNext())
		{
			String friendName = it.next();
			SEPAccount friendAccount;
			try
			{
				friendAccount = SEPAccount.getAccount(friendName);
			}
			catch(NameNotBoundException e)
			{
				friendList.put(friendName, new FriendList.FriendInfo(FriendState.UNKNOWN));
				continue;
			}
			catch(ObjectNotFoundException e)
			{
				friendList.put(friendName, new FriendList.FriendInfo(FriendState.UNKNOWN));
				continue;
			}
			
			FriendState friendState;
			if (!friendAccount.isFriend(uid))
			{
				friendState = FriendState.PENDING;
			}
			else
			{
				friendState = friendAccount.getState();
			}
			
			friendList.put(friendName, new FriendList.FriendInfo(friendState));
		}
		
		return friendList;
		//return (FriendList) Collections.unmodifiableMap(friendList);
	}

	/**
	 * @return
	 */
	protected FriendState getState()
	{
		ClientSession session = getSession();
		if ((session == null) || (!session.isConnected()))
		{
			return FriendState.NOT_CONNECTED;
		}
		
		SEPServerClientSessionListener sessionListener = getSessionListener();
		if (sessionListener == null)
		{
			return FriendState.UNKNOWN;
		}
		
		return sessionListener.getState();
	}

	/**
	 * @param uid2
	 * @return
	 */
	private boolean isFriend(String uid)
	{
		return friendList.containsKey(uid);
	}

	/**
	 * @param serverClientSessionListener
	 */
	protected void setSessionListener(SEPServerClientSessionListener sessionListener)
	{
		assert sessionListener.getName().compareTo(uid) == 0;
		
		refSessionListener = AppContext.getDataManager().createReference(sessionListener);
	}

	protected SEPServerClientSessionListener getSessionListener()
	{
		return (refSessionListener == null) ? null : refSessionListener.get();
	}
}
