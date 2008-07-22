package net.orfjackal.darkstar.rpc;
import java.util.concurrent.Future;

import com.sun.sgs.app.ManagedReference;

import net.orfjackal.darkstar.rpc.core.Request;
import net.orfjackal.darkstar.rpc.core.Response;
import net.orfjackal.darkstar.rpc.core.RpcFuture;
import net.orfjackal.darkstar.rpc.core.UnmanagedRpcFuture;

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
