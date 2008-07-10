/**
 * @author Escallier Pierre
 * @file IUserAccount.java
 * @date 9 juil. 2008
 */
package common;

import com.sun.sgs.app.ClientSession;

/**
 * 
 */
public interface IUserAccount
{

	/**
	 * @return
	 */
	ClientSession getSession();

	/**
	 * @param session
	 */
	void setSession(ClientSession session);

}
