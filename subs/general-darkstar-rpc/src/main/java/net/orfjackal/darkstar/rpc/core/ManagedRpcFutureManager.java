/**
 * @author Escallier Pierre
 * @file ManagedRpcFutureManager.java
 * @date 22 juil. 2008
 */
package net.orfjackal.darkstar.rpc.core;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.NameNotBoundException;
import com.sun.sgs.app.ObjectNotFoundException;
import com.sun.sgs.app.util.ScalableHashMap;

import net.orfjackal.darkstar.rpc.IRpcFutureManager;

/**
 * 
 */
public class ManagedRpcFutureManager implements IRpcFutureManager, Serializable
{
	private static final String	BINDING_PREFIX	= "ManagedRpcFuture";

	private static final Logger	log				= Logger.getLogger(ManagedRpcFutureManager.class.getName());

	private final ManagedReference<ScalableHashMap<Long, ManagedRpcFuture<?>>> refRequests;

	public ManagedRpcFutureManager(String idPrefix)
	{
		ScalableHashMap<Long, ManagedRpcFuture<?>> requests = null;
		try
		{
			requests = (ScalableHashMap<Long, ManagedRpcFuture<?>>) AppContext.getDataManager().getBinding(BINDING_PREFIX+idPrefix);
		}
		catch(NameNotBoundException e)
		{
			requests = new ScalableHashMap<Long, ManagedRpcFuture<?>>();
			AppContext.getDataManager().setBinding(BINDING_PREFIX+idPrefix, requests);
		}
		
		this.refRequests = AppContext.getDataManager().createReference(requests);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.orfjackal.darkstar.rpc.IRpcFutureManager#receivedResponse(net.orfjackal.darkstar.rpc.core.Response)
	 */
	@Override
	public void receivedResponse(Response rsp)
	{
		ManagedRpcFuture<?> f = refRequests.get().get(rsp.requestId);

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
		ManagedRpcFuture<V> f = new ManagedRpcFuture<V>(rq);
		
		assert !refRequests.get().containsKey(rq.requestId);
		
		refRequests.getForUpdate().put(rq.requestId, f);
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
		Iterator<Long> it = refRequests.get().keySet().iterator();

		while(it.hasNext())
		{
			if (!refRequests.get().get(it.next()).isDone())
			{
				++i;
			}
		}
		
		return i;
	}

}
