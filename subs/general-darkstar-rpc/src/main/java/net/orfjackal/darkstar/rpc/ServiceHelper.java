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

package net.orfjackal.darkstar.rpc;

import java.io.Serializable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.Task;

/**
 * @author Esko Luontola
 * @since 10.6.2008
 */
public final class ServiceHelper {
	
    private ServiceHelper() {
    }

    public static <V> Future<V> wrap(V value) {
        return new Wrapper<V>(value);
    }

    private static class Wrapper<V> implements Future<V> {

        private final V value;

        public Wrapper(V value) {
            this.value = value;
        }

        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        public boolean isCancelled() {
            return false;
        }

        public boolean isDone() {
            return true;
        }

        public V get() {
            return value;
        }

        public V get(long timeout, TimeUnit unit) {
            return value;
        }
    }
    
    private static final long SERVER_WAIT_FOR_FUTURE_TIMEOUT = 10000;
    
    public static <V> void serverWaitForFuture(Future<V> future, WaitForFutureListener<V> waitForFutureListener)
	{
		AppContext.getTaskManager().scheduleTask(new WaitForFuture(future, waitForFutureListener, SERVER_WAIT_FOR_FUTURE_TIMEOUT));
	}

    public static interface WaitForFutureListener<V> extends Serializable
    {
    	void onResultOK(V result);
    	void onExceptionThrown(Throwable throwable);
    	void onTimeOut(long elapsedTime);
    }
    
	private static class WaitForFuture<V> implements Task, Serializable
	{
		private static final Logger logger = Logger.getLogger(WaitForFuture.class.getName());
		
		private static final long	serialVersionUID	= 1L;

		private final Future<V>		future;

		private final long			timeOutMs;

		private Long				startTime			= null;
		
		private final WaitForFutureListener<V> callbacks;
		
		private WaitForFuture(long startTime, Future<V> future, WaitForFutureListener<V> callbacks, long timeOutMs)
		{
			this.startTime = startTime;
			this.future = future;
			this.timeOutMs = timeOutMs;
			this.callbacks = callbacks;
		}

		public WaitForFuture(Future<V> future, WaitForFutureListener<V> callbacks, long timeOutMs)
		{
			this.future = future;
			this.timeOutMs = timeOutMs;
			this.callbacks = callbacks;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			return String.format("waitForFuture %s @ %s", future.getClass().getCanonicalName(), future.toString());
		}
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see com.sun.sgs.app.Task#run()
		 */
		@Override
		public void run() throws Exception
		{
			StringBuilder trace = new StringBuilder(this.toString());

			if (startTime == null)
			{
				startTime = System.currentTimeMillis();
			}

			V result;

			try
			{
				if (future.isDone())
				{
					result = future.get();
				}
				else
				{
					throw new TimeoutException("["+this+"] little timeout");
				}
			}
			catch (TimeoutException e)
			{
				long endTime = System.currentTimeMillis();
				long elapsedTime = (endTime - startTime);

				if (elapsedTime > timeOutMs)
				{
					trace.append(": big timeout");
					logger.log(Level.INFO, trace.toString());
					
					callbacks.onTimeOut(elapsedTime);
					return;
				}

				trace.append(": little timeout");
				logger.log(Level.INFO, trace.toString());
				AppContext.getTaskManager().scheduleTask(new WaitForFuture(startTime, future, callbacks, timeOutMs), 1000);
				return;
			}
			catch(Throwable t)
			{
				trace.append(": exception thrown "+t);
				logger.log(Level.INFO, trace.toString());
				callbacks.onExceptionThrown(t);
				return;
			}

			trace.append(": success");
			logger.log(Level.INFO, trace.toString());
			callbacks.onResultOK(result);
		}
	}
}
