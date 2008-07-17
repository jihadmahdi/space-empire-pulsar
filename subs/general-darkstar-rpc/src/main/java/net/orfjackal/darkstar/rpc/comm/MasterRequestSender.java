/**
 * @author Escallier Pierre
 * @file MasterRequestSender.java
 * @date 12 juil. 2008
 */
package net.orfjackal.darkstar.rpc.comm;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;

import net.orfjackal.darkstar.rpc.MessageReciever;
import net.orfjackal.darkstar.rpc.ResponseReceiver;

/**
 * 
 */
public class MasterRequestSender extends SenderAdapter implements Serializable
{
	private static final long	serialVersionUID	= 1L;

	public MasterRequestSender(IChannelAdapter channelAdapter)
	{
		super(channelAdapter);
	}

	/* (non-Javadoc)
	 * @see net.orfjackal.darkstar.rpc.comm.SenderAdapter#send(byte[])
	 */
	@Override
	public void send(byte[] message) throws IOException
	{
		log.info("send("+channelAdapter.getChannelName()+"; REQUEST_TO_SLAVE: \"" + message.toString() + "\")");

		ByteBuffer buf = ByteBuffer.allocate(message.length + 1);
		buf.put(RpcGateway.REQUEST_TO_SLAVE);
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
		if (!ResponseReceiver.class.isInstance(callback))
		{
			throw new IllegalArgumentException("callback must be a ResponseReceiver.");
		}
		
		channelAdapter.setResponseReciever(ResponseReceiver.class.cast(callback));
	}

}
