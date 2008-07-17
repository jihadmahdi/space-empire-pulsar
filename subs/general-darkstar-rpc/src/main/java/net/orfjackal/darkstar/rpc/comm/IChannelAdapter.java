/**
 * @author Escallier Pierre
 * @file IChannel.java
 * @date 11 juil. 2008
 */
package net.orfjackal.darkstar.rpc.comm;

import java.io.IOException;
import java.nio.ByteBuffer;

import net.orfjackal.darkstar.rpc.RequestReceiver;
import net.orfjackal.darkstar.rpc.ResponseReceiver;


/**
 * 
 */
public interface IChannelAdapter
{

	/**
	 * @param buf
	 */
	void sendToChannel(ByteBuffer buf) throws IOException;

	/**
	 * @param callback
	 */
	void setResponseReciever(ResponseReceiver callback);
	
	/**
	 * 
	 * @param callback
	 */
	void setRequestReceiver(RequestReceiver callback);

	String getChannelName();
}
