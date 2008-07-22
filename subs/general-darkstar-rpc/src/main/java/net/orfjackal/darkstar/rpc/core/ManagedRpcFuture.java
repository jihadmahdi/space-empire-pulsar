/**
 * @author Escallier Pierre
 * @file ManagedRpcFuture.java
 * @date 22 juil. 2008
 */
package net.orfjackal.darkstar.rpc.core;

import java.io.Serializable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;

/**
 * 
 */
public class ManagedRpcFuture<V> implements RpcFuture<V>, Serializable
{

	private static class RpcFutureBean<V> implements ManagedObject, Serializable
	{
		Throwable	exception;

		final Request request;

		boolean		isDone;

		V			value;

		public RpcFutureBean(Request request)
		{
			this.request = request;
		}
	}

	private final ManagedReference<RpcFutureBean<V>>	refBean;

	public ManagedRpcFuture(Request request)
	{
		refBean = AppContext.getDataManager().createReference(new RpcFutureBean<V>(request));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.concurrent.Future#cancel(boolean)
	 */
	@Override
	public boolean cancel(boolean arg0)
	{
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.concurrent.Future#get()
	 */
	@Override
	public V get() throws InterruptedException, ExecutionException
	{
		while (refBean.get().isDone == false)
		{
			Thread.sleep(10);
		}

		return refBean.get().value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.concurrent.Future#get(long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public V get(long arg0, TimeUnit arg1) throws InterruptedException, ExecutionException, TimeoutException
	{
		long startTime = System.currentTimeMillis();
		long timeOut = TimeUnit.MILLISECONDS.convert(arg0, arg1);
		long sleepTime = Math.min(timeOut / 10, 10);

		while (refBean.get().isDone == false)
		{
			long elapsedTime = System.currentTimeMillis() - startTime;

			if (elapsedTime > timeOut)
			{
				throw new TimeoutException();
			}

			Thread.sleep(sleepTime);
		}

		return refBean.get().value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.concurrent.Future#isCancelled()
	 */
	@Override
	public boolean isCancelled()
	{
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.concurrent.Future#isDone()
	 */
	@Override
	public boolean isDone()
	{
		return refBean.get().isDone;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.orfjackal.darkstar.rpc.core.RpcFuture#markDone(net.orfjackal.darkstar.rpc.core.Response)
	 */
	@Override
	public void markDone(Response response)
	{
		RpcFutureBean<V> bean = refBean.getForUpdate();

		if (response.requestId != bean.request.requestId)
		{
			throw new IllegalArgumentException("Wrong requestId");
		}
		if (response.exception != null)
		{
			bean.exception = response.exception;
		}
		else
		{
			bean.value = (V) response.value;
			bean.isDone = true;
		}
	}

}
