/**
 * @author Escallier Pierre
 * @file IRpcFuture.java
 * @date 22 juil. 2008
 */
package net.orfjackal.darkstar.rpc.core;

import java.util.concurrent.Future;

/**
 * 
 */
public interface IRpcFuture<V> extends Future<V>
{
	void markDone(Response response);
}