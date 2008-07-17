/**
 * @author Escallier Pierre
 * @file SlaveRequestSender.java
 * @date 11 juil. 2008
 */
package net.orfjackal.darkstar.rpc.comm;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;

import net.orfjackal.darkstar.rpc.MessageReciever;
import net.orfjackal.darkstar.rpc.RequestReceiver;
import net.orfjackal.darkstar.rpc.ResponseReceiver;

/**
 * 
 */
class SlaveRequestSender extends SenderAdapter implements Serializable
{
	private static final long	serialVersionUID	= 1L;

	public SlaveRequestSender(IChannelAdapter channelAdapter)
	{
		super(channelAdapter);
	}

	/* (non-Javadoc)
	 * @see net.orfjackal.darkstar.rpc.comm.SlaveSenderAdapter#setCallback(net.orfjackal.darkstar.rpc.MessageReciever)
	 */
	@Override
	public void setCallback(MessageReciever callback)
	{
		if (!ResponseReceiver.class.isInstance(callback))
		{
			throw new IllegalArgumentException("callback must be a RequestReceiver.");
		}
		
		channelAdapter.setResponseReciever(ResponseReceiver.class.cast(callback));
	}

	/* (non-Javadoc)
	 * @see net.orfjackal.darkstar.rpc.comm.SlaveSenderAdapter#send(byte[])
	 */
	@Override
	public void send(byte[] message) throws IOException
	{
		ByteBuffer buf = ByteBuffer.allocate(message.length + 1);
		buf.put(RpcGateway.REQUEST_TO_MASTER);
		buf.put(message);
		buf.flip();
		channelAdapter.sendToChannel(buf);
		log.info("send("+channelAdapter.getChannelName()+"; REQUEST_TO_MASTER: \"" + message.toString() + "\")");
	}

}
