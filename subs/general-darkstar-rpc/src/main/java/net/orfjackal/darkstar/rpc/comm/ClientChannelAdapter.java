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

package net.orfjackal.darkstar.rpc.comm;

import com.sun.sgs.client.ClientChannel;
import com.sun.sgs.client.ClientChannelListener;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Esko Luontola
 * @since 15.6.2008
 */
public class ClientChannelAdapter extends RpcChannelAdapter implements ClientChannelListener, Serializable
{
	private static final long	serialVersionUID	= 1L;

	private static final Logger	log					= Logger.getLogger(ClientChannelAdapter.class.getName());

	private ClientChannel		channel;

	public ClientChannelAdapter(boolean isMaster)
	{
		super(isMaster, false);
	}

	public ClientChannelAdapter(int timeoutMs, boolean isMaster)
	{
		super(timeoutMs, isMaster, false);
	}

	public ClientChannelListener joinedChannel(ClientChannel channel)
	{
		log.log(Level.INFO, "ClientChannelAdapter.joinedChannel("+channel.getName()+")");
		assert this.channel == null;
		this.channel = channel;
		return this;
	}

	public void leftChannel(ClientChannel channel)
	{
		log.log(Level.INFO, "ClientChannelAdapter.leftChannel("+channel.getName()+")");
		assert this.channel == channel;
		this.channel = null;
	}

	public void receivedMessage(ClientChannel channel, ByteBuffer message)
	{
		log.log(Level.INFO, "ClientChannelAdapter.receivedMessage");
		super.receivedMessage(channel.getName(), "nobody", message);
	}
	
	/* (non-Javadoc)
	 * @see net.orfjackal.darkstar.rpc.comm.IChannelAdapter#sendToChannel(java.nio.ByteBuffer)
	 */
	@Override
	public void sendToChannel(ByteBuffer buf) throws IOException
	{
		if (channel == null)
		{
			throw new IllegalStateException("No connection");
		}
		log.log(Level.INFO, "ClientChannelAdapter.sendToChannel");
		channel.send(buf);
	}

	/* (non-Javadoc)
	 * @see net.orfjackal.darkstar.rpc.comm.IChannelAdapter#getChannelName()
	 */
	@Override
	public String getChannelName()
	{
		return (channel==null?"none":channel.getName());
	}

}
