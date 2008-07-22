/**
 * @author Escallier Pierre
 * @file RpcFutureManager.java
 * @date 22 juil. 2008
 */
package net.orfjackal.darkstar.rpc.core;

import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import com.sun.sgs.app.ManagedReference;

import net.orfjackal.darkstar.rpc.IRpcFutureManager;

/**
 * 
 */
public class RpcFutureManager implements IRpcFutureManager
{
	private static final Logger									log			= Logger.getLogger(ManagedRpcFutureManager.class.getName());

	private final ConcurrentHashMap<Long, UnmanagedRpcFuture<?>>	requests	= new ConcurrentHashMap<Long, UnmanagedRpcFuture<?>>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.orfjackal.darkstar.rpc.IRpcFutureManager#receivedResponse(net.orfjackal.darkstar.rpc.core.Response)
	 */
	@Override
	public void receivedResponse(Response rsp)
	{
		UnmanagedRpcFuture<?> f = requests.get(rsp.requestId);

		if (f != null)
		{
			f.markDone(rsp);
		}
		else
		{
			log.warning("Unexpected response: " + rsp);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.orfjackal.darkstar.rpc.IRpcFutureManager#waitForResponseTo(net.orfjackal.darkstar.rpc.core.Request)
	 */
	@Override
	public <V> Future<V> waitForResponseTo(Request rq)
	{
		UnmanagedRpcFuture<V> f = new UnmanagedRpcFuture<V>(rq);

		assert !requests.containsKey(rq.requestId);

		requests.put(rq.requestId, f);
		return f;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.orfjackal.darkstar.rpc.IRpcFutureManager#waitingForResponse()
	 */
	@Override
	public int waitingForResponse()
	{
		int i = 0;
		Enumeration<UnmanagedRpcFuture<?>> it = requests.elements();
		while(it.hasMoreElements())
		{
			if (!it.nextElement().isDone())
			{
				++i;
			}
		}
		
		return i;
	}

}
