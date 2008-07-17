/**
 * @author Escallier Pierre
 * @file MasterResponseSender.java
 * @date 12 juil. 2008
 */
package net.orfjackal.darkstar.rpc.comm;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;

import net.orfjackal.darkstar.rpc.MessageReciever;
import net.orfjackal.darkstar.rpc.RequestReceiver;

/**
 * 
 */
public class MasterResponseSender extends SenderAdapter implements Serializable
{
	public MasterResponseSender(IChannelAdapter channelAdapter)
	{
		super(channelAdapter);
	}

	/* (non-Javadoc)
	 * @see net.orfjackal.darkstar.rpc.comm.SenderAdapter#send(byte[])
	 */
	@Override
	public void send(byte[] message) throws IOException
	{
		log.info("send("+channelAdapter.getChannelName()+"; RESPONSE_FROM_MASTER: \"" + message.toString() + "\")");
		ByteBuffer buf = ByteBuffer.allocate(message.length + 1);
		buf.put(RpcGateway.RESPONSE_FROM_MASTER);
		buf.put(message);
		buf.flip();
		channelAdapter.sendToChannel(buf);
	}

	/* (non-Javadoc)
	 * @see net.orfjackal.darkstar.rpc.comm.SenderAdapter#setCallback(net.orfjackal.darkstar.rpc.MessageReciever)
	 */
	@Override
	public void setCallback(MessageReciever callback)
	{
		if (!RequestReceiver.class.isInstance(callback))
		{
			throw new IllegalArgumentException("callback must be a RequestReceiver.");
		}
		
		channelAdapter.setRequestReceiver(RequestReceiver.class.cast(callback));
	}

}
