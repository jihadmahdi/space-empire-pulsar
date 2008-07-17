/*
 * Copyright (c) 2008, Esko Luontola. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.orfjackal.darkstar.rpc.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Esko Luontola
 * @since 10.6.2008
 */
public final class RpcFuture<V> implements Future<V>, Serializable
{
	private static final HashMap<Long, RpcFuture<?>> requests = new HashMap<Long, RpcFuture<?>>();
	
	public static RpcFuture<?> getById(Long id)
	{
		return requests.get(id);
	}
	
	public static int waitingForResponse()
	{
		int cpt = 0;
		
		Iterator<RpcFuture<?>> it = requests.values().iterator();
		while(it.hasNext())
		{
			RpcFuture<?> rq = it.next();
			if (!rq.isDone()) ++cpt;
		}
		
		return cpt;
	}
	
	private final Request				request;
	private Throwable exception;
	private V value = null;
	private Boolean isDone = false;
	
	public static <V> RpcFuture<V> refresh(RpcFuture<V> old)
	{
		RpcFuture<V> refreshed = (RpcFuture<V>) getById(old.request.requestId);
		return refreshed;
	}

	public RpcFuture(Request request)
	{
		this.request = request;
		
		requests.put(request.requestId, this);
	}

	public void markDone(Response response)
	{
		if (response.requestId != request.requestId)
		{
			throw new IllegalArgumentException("Wrong requestId");
		}
		if (response.exception != null)
		{
			setException(response.exception);
		}
		else
		{
			value = (V) response.value;
			isDone = true;
		}
	}

	private void setException(Throwable t)
	{
		this.exception = t;
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.Future#cancel(boolean)
	 */
	@Override
	public boolean cancel(boolean arg0)
	{
		return false;
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.Future#get()
	 */
	@Override
	public V get() throws InterruptedException, ExecutionException
	{
		while(isDone == false)
		{
			Thread.sleep(10);
		}
		
		return value;
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.Future#get(long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public V get(long arg0, TimeUnit arg1) throws InterruptedException, ExecutionException, TimeoutException
	{
		long startTime = System.currentTimeMillis();
		long timeOut = TimeUnit.MILLISECONDS.convert(arg0, arg1);
		long sleepTime = Math.min(timeOut / 10, 10);
		
		while(isDone == false)
		{
			long elapsedTime = System.currentTimeMillis() - startTime;
			
			if (elapsedTime > timeOut)
			{
				throw new TimeoutException();
			}
			
			Thread.sleep(sleepTime);
		}
		
		return value;
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.Future#isCancelled()
	 */
	@Override
	public boolean isCancelled()
	{
		return false;
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.Future#isDone()
	 */
	@Override
	public boolean isDone()
	{
		return isDone;
	}
}
