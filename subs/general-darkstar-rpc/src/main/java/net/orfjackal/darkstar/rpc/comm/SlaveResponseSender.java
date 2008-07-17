/**
 * @author Escallier Pierre
 * @file SlaveResponseSender.java
 * @date 11 juil. 2008
 */
package net.orfjackal.darkstar.rpc.comm;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;

import net.orfjackal.darkstar.rpc.MessageReciever;
import net.orfjackal.darkstar.rpc.MessageSender;
import net.orfjackal.darkstar.rpc.RequestReceiver;
import net.orfjackal.darkstar.rpc.ResponseReceiver;

/**
 * 
 */
public class SlaveResponseSender extends SenderAdapter implements Serializable
{

	private static final long	serialVersionUID	= 1L;

	public SlaveResponseSender(IChannelAdapter channelAdapter)
	{
		super(channelAdapter);
	}

	/* (non-Javadoc)
	 * @see net.orfjackal.darkstar.rpc.comm.SlaveSenderAdapter#send(byte[])
	 */
	@Override
	public void send(byte[] message) throws IOException
	{
		log.info("send("+channelAdapter.getChannelName()+"; RESPONSE_FROM_SLAVE: \"" + message.toString() + "\")");
		ByteBuffer buf = ByteBuffer.allocate(message.length + 1);
		buf.put(RpcGateway.RESPONSE_FROM_SLAVE);
		buf.put(message);
		buf.flip();
		channelAdapter.sendToChannel(buf);
	}

	/* (non-Javadoc)
	 * @see net.orfjackal.darkstar.rpc.comm.SlaveSenderAdapter#setCallback(net.orfjackal.darkstar.rpc.MessageReciever)
	 */
	@Override
	public void setCallback(MessageReciever callback)
	{
		if (!RequestReceiver.class.isInstance(callback))
		{
			throw new IllegalArgumentException("callback must be a ResponseReceiver.");
		}
		
		channelAdapter.setRequestReceiver(RequestReceiver.class.cast(callback));
	}
	

}
