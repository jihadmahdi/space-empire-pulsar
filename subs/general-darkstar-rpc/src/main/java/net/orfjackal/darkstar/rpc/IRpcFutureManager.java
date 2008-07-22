package net.orfjackal.darkstar.rpc;
import java.util.concurrent.Future;

import net.orfjackal.darkstar.rpc.core.Request;
import net.orfjackal.darkstar.rpc.core.Response;

/**
 * @author Escallier Pierre
 * @file IRpcFuture.java
 * @date 18 juil. 2008
 */

/**
 * 
 */
public interface IRpcFutureManager
{	
	/** Return the number of request waiting for response. */
	int waitingForResponse();
	
	/**
	 * @param rq
	 * @return
	 */
	<V> Future<V> waitForResponseTo(Request rq);

	/**
	 * @param rsp
	 */
	void receivedResponse(Response rsp);
}
