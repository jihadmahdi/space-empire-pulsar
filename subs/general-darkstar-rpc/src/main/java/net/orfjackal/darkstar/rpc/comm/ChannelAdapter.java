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

import com.sun.sgs.app.*;
import com.sun.sgs.auth.Identity;
import com.sun.sgs.kernel.KernelRunnable;
import com.sun.sgs.kernel.TransactionScheduler;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Esko Luontola
 * @since 15.6.2008
 */
public class ChannelAdapter extends RpcChannelAdapter implements ChannelListener, Serializable
{

	private static final long			serialVersionUID	= 1L;

	private static final Logger			log					= Logger.getLogger(ChannelAdapter.class.getName());
	
	private ManagedReference<Channel>	channel;																// TODO: will not run without TransparentReferences, because Channels are managed objects

	public ChannelAdapter(boolean isMaster)
	{
		super(isMaster, true);
	}

	public ChannelAdapter(long timeoutMs, boolean isMaster)
	{
		super(timeoutMs, isMaster, true);
	}

	public void setChannel(Channel channel)
	{
		// TODO: mock the data manager (move mocks from darkstar-tref into darkstar-exp-mocks)
		this.channel = AppContext.getDataManager().createReference(channel);
		log.log(Level.INFO, "ChannelAdapter.setChannel("+channel.getName()+")");
	}

	public void receivedMessage(Channel channel, ClientSession sender, ByteBuffer message)
	{
		super.receivedMessage(channel.getName(), sender.getName(), message);
	}

	/* (non-Javadoc)
	 * @see net.orfjackal.darkstar.rpc.comm.IChannelAdapter#sendToChannel(java.nio.ByteBuffer)
	 */
	@Override
	public void sendToChannel(final ByteBuffer buf) throws IOException
	{
		assert channel.get().hasSessions();
		
		log.log(Level.INFO, "ChannelAdapter.sendToChannel");
		channel.get().send(null, buf);
	}

	/* (non-Javadoc)
	 * @see net.orfjackal.darkstar.rpc.comm.IChannelAdapter#getChannelName()
	 */
	@Override
	public String getChannelName()
	{
		return (channel==null?"none":channel.get().getName());
	}
	
}
