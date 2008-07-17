/**
 * @author Escallier Pierre
 * @file SlaveSenderAdapter.java
 * @date 11 juil. 2008
 */
package net.orfjackal.darkstar.rpc.comm;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.logging.Logger;

import net.orfjackal.darkstar.rpc.MessageReciever;
import net.orfjackal.darkstar.rpc.MessageSender;

/**
 * 
 */
public abstract class SenderAdapter implements MessageSender, Serializable
{
	private static final long	serialVersionUID	= 1L;

	protected static final Logger log = Logger.getLogger(SenderAdapter.class.getName()); 
	
	protected final IChannelAdapter	channelAdapter;
	
	public SenderAdapter(IChannelAdapter channelAdapter)
	{
		this.channelAdapter = channelAdapter;
	}
	
	abstract public void send(byte[] message) throws IOException;

	abstract public void setCallback(MessageReciever callback);
}
