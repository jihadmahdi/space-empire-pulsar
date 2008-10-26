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

import java.io.Serializable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Esko Luontola
 * @since 10.6.2008
 */
/**
 * @param <V>
 */
public final class UnmanagedRpcFuture<V> implements IRpcFuture<V>, Serializable
{	
	private static final long	serialVersionUID	= 1L;
	
	private final Request				request;
	private Throwable exception;
	private V value = null;
	private Boolean isDone = false;

	public UnmanagedRpcFuture(Request request)
	{
		this.request = request;
	}

	/*
	 * (non-Javadoc)
	 * @see net.orfjackal.darkstar.rpc.core.IRpcFuture#markDone(net.orfjackal.darkstar.rpc.core.Response)
	 */
	public void markDone(Response response)
	{
		if (response.requestId != request.requestId)
		{
			throw new IllegalArgumentException("Wrong requestId");
		}
		if (response.exception != null)
		{
			this.exception = response.exception;
		}
		else
		{
			value = (V) response.value;
			isDone = true;
		}
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