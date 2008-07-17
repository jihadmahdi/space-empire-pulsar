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

import net.orfjackal.darkstar.rpc.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.impl.util.ManagedSerializable;

/**
 * @author Esko Luontola
 * @since 9.6.2008
 */
public class RpcClientImpl implements RpcClient, ResponseReceiver, Serializable
{
	/*
	private void writeObject(ObjectOutputStream s) throws IOException
	{
		s.defaultWriteObject();
		Map<Long, Request> serializableWaitingForResponse = new HashMap<Long, Request>();
		Iterator<Long> it = waitingForResponse.keySet().iterator();
		while(it.hasNext())
		{
			Long id = it.next();
			RpcFuture<?> futureRequest = waitingForResponse.get(id);
			Request request = futureRequest.getRequest();
			
			serializableWaitingForResponse.put(id, request);
		}
		
		s.writeObject(serializableWaitingForResponse);
	}

	private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException
	{
		s.defaultReadObject();
		
		waitingForResponse = new ConcurrentHashMap<Long, RpcFuture<?>>();
		
		Map<Long, Request> serializableWaitingForResponse = (Map<Long, Request>) s.readObject();
		
		Iterator<Long> it = serializableWaitingForResponse.keySet().iterator();
		while(it.hasNext())
		{
			Long id = it.next();
			Request request = serializableWaitingForResponse.get(id);
			
			waitingForResponse.put(id, new RpcFuture<Object>(request));
		}
	}
	*/

	/*
	public static class ManageableConcurantHashMap<K, V> implements Map<K, V>, Serializable
	{
		private static final class EntryImpl<K, V> implements Map.Entry<K, V>, Serializable
		{
			private static final long	serialVersionUID	= 1L;
			
			private final K key;
			private final V value;
			
			public EntryImpl(K key, V value)
			{
				this.key = key;
				this.value = value;
			}
		
			@Override
			public K getKey()
			{
				return key;
			}

			@Override
			public V getValue()
			{
				return value;
			}

			@Override
			public V setValue(V arg0)
			{
				throw new UnsupportedOperationException();
			}
			
		}
	
		private static final long	serialVersionUID	= 1L;
		
		private final ConcurrentHashMap<K, V> internalNonManagedMap = new ConcurrentHashMap<K, V>();
		private final ConcurrentHashMap<K, ManagedReference<ManagedSerializable<V>>> internalManagedMap = new ConcurrentHashMap<K, ManagedReference<ManagedSerializable<V>>>();
		private final Map<K, ?> internalMap;
		
		private final boolean isManaged;
		
		public ManageableConcurantHashMap(boolean isManaged)
		{
			this.isManaged = isManaged;
			
			if (isManaged)
			{
				internalMap = internalManagedMap;
			}
			else
			{
				internalMap = internalNonManagedMap;
			}
		}
		
		
		public static <V> V unwrapRef(ManagedReference<ManagedSerializable<V>> ref)
		{
			return ref.get().get();
		}
		
		private V tryUnwrapObject(Object object)
		{
			if (object == null) return null;
			
			if (isManaged)
			{
				return unwrapRef((ManagedReference<ManagedSerializable<V>>) object);
			}
			else
			{
				return (V) object;
			}
		}
		
		public static <V> ManagedReference<ManagedSerializable<V>> wrapValue(V value)
		{
			return AppContext.getDataManager().createReference(new ManagedSerializable<V>(value));
		}
		
		private Object tryWrapValue(V value)
		{
			if (value == null) return null;
			
			if (isManaged)
			{
				return wrapValue(value);
			}
			else
			{
				return value;
			}
		}
		
		@Override
		public void clear()
		{
			internalMap.clear();
		}

		@Override
		public boolean containsKey(Object arg0)
		{
			return internalMap.containsKey(arg0); 
		}

		@Override
		public boolean containsValue(Object arg0)
		{
			return internalMap.containsValue(tryWrapValue((V) arg0)); 
		}

		@Override
		public Set<java.util.Map.Entry<K, V>> entrySet()
		{
			Set<Entry<K, V>> resultEntrySet = new HashSet<Entry<K,V>>();
			
			Set<?> entrySet = internalMap.entrySet();
			Iterator<?> it = entrySet.iterator();
			while(it.hasNext())
			{
				Entry entry = Entry.class.cast(it.next());
				resultEntrySet.add(new EntryImpl<K,V>((K) entry.getKey(), tryUnwrapObject(entry.getValue())));
			}
			
			return resultEntrySet;
		}

		@Override
		public V get(Object arg0)
		{
			return tryUnwrapObject(internalMap.get(arg0)); 
		}

		@Override
		public boolean isEmpty()
		{
			return internalMap.isEmpty();
		}

		@Override
		public Set<K> keySet()
		{
			return internalMap.keySet();
		}

		@Override
		public V put(K arg0, V arg1)
		{
			Object previous = internalMap.get(arg0);
			V returnPrevious = null;
			if (previous != null)
			{
				returnPrevious = tryUnwrapObject(previous);
			}
			
			if (isManaged)
			{
				internalManagedMap.put(arg0, (ManagedReference<ManagedSerializable<V>>) tryWrapValue(arg1));
			}
			else
			{
				internalNonManagedMap.put(arg0, arg1);
			}
			
			return returnPrevious;
		}

		@Override
		public void putAll(Map<? extends K, ? extends V> arg0)
		{
			Iterator<? extends K> it = arg0.keySet().iterator();
			while(it.hasNext())
			{
				K key = it.next();
				V value = arg0.get(key);
				
				put(key, value);
			}
		}

		@Override
		public V remove(Object arg0)
		{
			V previous = tryUnwrapObject(internalMap.get(arg0));
			internalMap.remove(arg0);
			return previous;
		}

		@Override
		public int size()
		{
			return internalMap.size();
		}

		@Override
		public Collection<V> values()
		{
			if (!isManaged)
			{
				return internalNonManagedMap.values();
			}
			else
			{
				Collection<V> col = new HashSet<V>();
				Iterator<ManagedReference<ManagedSerializable<V>>> it = internalManagedMap.values().iterator();
				while(it.hasNext())
				{
					col.add(tryUnwrapObject(it.next()));
				}
				
				return col;
			}
		}
		
		
	}
	*/
	
    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger(RpcClientImpl.class.getName());

    //private final Map<Long, RpcFuture<?>> waitingForResponse;
    //private final Map<Long, ManagedReference<ManagedSerializable<RpcFuture<?>>>> waitingForResponse = new ConcurrentHashMap<Long, ManagedReference<ManagedSerializable<RpcFuture<?>>>>();
    private final MessageSender requestSender;
    private long nextRequestId = 1L;

    public RpcClientImpl(MessageSender requestSender, boolean isManaged) {
        requestSender.setCallback(this);
        this.requestSender = requestSender;
        //this.waitingForResponse = new ManageableConcurantHashMap<Long, RpcFuture<?>>(isManaged);
    }

    public ServiceReference<ServiceProvider> getServiceProvider() {
        return new ServiceReference<ServiceProvider>(ServiceProvider.class, ServiceProvider.SERVICE_ID);
    }

    public void remoteInvokeNoResponse(long serviceId, String methodName, Class<?>[] paramTypes, Object[] parameters) {
        sendRequest(serviceId, methodName, paramTypes, parameters);
    }

    public <V> Future<V> remoteInvoke(long serviceId, String methodName, Class<?>[] paramTypes, Object[] parameters) {
        Request rq = sendRequest(serviceId, methodName, paramTypes, parameters);
        return waitForResponseTo(rq);
    }

    private Request sendRequest(long serviceId, String methodName, Class<?>[] paramTypes, Object[] parameters) {
        Request rq = new Request(nextRequestId(), serviceId, methodName, paramTypes, parameters);
        try {
            requestSender.send(rq.toBytes());
        } catch (IOException e) {
            throw new RuntimeException("Unable to invoke method " + methodName + " on service " + serviceId, e);
        }
        return rq;
    }

    private <V> Future<V> waitForResponseTo(Request rq) {
        RpcFuture<V> f = new RpcFuture<V>(rq);
        
        assert (RpcFuture.getById(rq.requestId) != null);
        //assert !waitingForResponse.containsKey(rq.requestId);
        
        //waitingForResponse.put(rq.requestId, f);
        return f;
    }

    public void receivedMessage(byte[] message) {
        Response rsp = Response.fromBytes(message);
        RpcFuture<?> f = RpcFuture.getById(rsp.requestId);
        //RpcFuture<?> f = waitingForResponse.remove(rsp.requestId);
        if (f != null) {
        	f.markDone(rsp);
        } else {
            log.warning("Unexpected response: " + rsp);
        }
    }

    private synchronized long nextRequestId() {
        return nextRequestId++;
    }

    public int waitingForResponse() {
    	return RpcFuture.waitingForResponse();
    	//return waitingForResponse.size();
    }
}
