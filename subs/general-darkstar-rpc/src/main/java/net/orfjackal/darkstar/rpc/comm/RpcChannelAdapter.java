/**
 * @author Escallier Pierre
 * @file RpcChannelAdapter.java
 * @date 12 juil. 2008
 */
package net.orfjackal.darkstar.rpc.comm;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.orfjackal.darkstar.rpc.IRpcFutureManager;
import net.orfjackal.darkstar.rpc.MessageSender;
import net.orfjackal.darkstar.rpc.RequestReceiver;
import net.orfjackal.darkstar.rpc.ResponseReceiver;
import net.orfjackal.darkstar.rpc.core.Request;
import net.orfjackal.darkstar.rpc.core.Response;

/**
 * 
 */
public abstract class RpcChannelAdapter implements IChannelAdapter, Serializable
{
	private static final long	serialVersionUID	= 1L;

	private static final Logger	log					= Logger.getLogger(RpcChannelAdapter.class.getName());

	// server-to-client requests
	// MessageReciever
	private ResponseReceiver	responseReciever;

	// client-to-server requests
	// MessageReciever
	private RequestReceiver		requestReciever;

	private final RpcGateway	gateway;

	private byte				requestHeader;

	private byte				responseHeader;

	public RpcChannelAdapter(boolean isMaster, IRpcFutureManager rpcFutureManager)
	{
		this(10000, isMaster, rpcFutureManager);
	}

	public RpcChannelAdapter(long timeoutMs, boolean isMaster, IRpcFutureManager rpcFutureManager)
	{
		MessageSender requestSender;
		MessageSender responseSender;

		if ( !isMaster)
		{
			requestSender = new SlaveRequestSender(this);
			responseSender = new SlaveResponseSender(this);

			requestHeader = RpcGateway.REQUEST_TO_SLAVE;
			responseHeader = RpcGateway.RESPONSE_FROM_MASTER;
		}
		else
		{
			requestSender = new MasterRequestSender(this);
			responseSender = new MasterResponseSender(this);

			requestHeader = RpcGateway.REQUEST_TO_MASTER;
			responseHeader = RpcGateway.RESPONSE_FROM_SLAVE;
		}

		gateway = new RpcGateway(requestSender, responseSender, rpcFutureManager, timeoutMs);
	}

	public RpcGateway getGateway()
	{
		return gateway;
	}

	protected final void receivedMessage(String channelName, String senderName, ByteBuffer message)
	{
		byte header = message.get();
		byte[] ba = ByteBufferUtils.asByteArray(message);

		/*
		String messageType = "unknown";
		Request rq = null;
		Response rsp = null;

		try
		{
			rq = Request.fromBytes(ba);
			messageType = "Request";
		}
		catch (Exception e)
		{
			try
			{
				rsp = Response.fromBytes(ba);
				messageType = "Response";
			}
			catch (Exception e2)
			{
				log.log(Level.WARNING, "message is not a Request nor a Response.");
			}
		}

		log.log(Level.INFO, "receivedMessage " + ((header == requestHeader) ? "Request" : ((header == responseHeader) ? "Response" : "unknown")) + " header; " + messageType + " message");
		*/

		if (header == requestHeader)
		{
			log.info("RpcChannelAdapter.receivedMessage("+channelName+"; REQUEST: \"" + message.toString() + "\")");
			requestReciever.receivedMessage(ba);
		}
		else if (header == responseHeader)
		{
			log.info("RpcChannelAdapter.receivedMessage("+channelName+"; RESPONSE: \"" + message.toString() + "\")");
			responseReciever.receivedMessage(ba);
		}
		else
		{
			log.warning("Unexpected header " + header + " on channel " + channelName + " from sender " + senderName);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.orfjackal.darkstar.rpc.comm.IChannelAdapter#sendToChannel(java.nio.ByteBuffer)
	 */
	@Override
	abstract public void sendToChannel(ByteBuffer buf) throws IOException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.orfjackal.darkstar.rpc.comm.IChannelAdapter#setRequestReceiver(net.orfjackal.darkstar.rpc.MessageReciever)
	 */
	@Override
	public void setRequestReceiver(RequestReceiver callback)
	{
		requestReciever = callback;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.orfjackal.darkstar.rpc.comm.IChannelAdapter#setResponseReciever(net.orfjackal.darkstar.rpc.MessageReciever)
	 */
	@Override
	public void setResponseReciever(ResponseReceiver callback)
	{
		responseReciever = callback;
	}

}
