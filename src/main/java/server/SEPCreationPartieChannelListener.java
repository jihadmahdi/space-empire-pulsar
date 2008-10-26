/**
 * @author Escallier Pierre
 * @file SEPCreationPartieChannelListener.java
 * @date 30 juin 08
 */
package server;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.sgs.app.Channel;
import com.sun.sgs.app.ChannelListener;
import com.sun.sgs.app.ClientSession;

/**
 * 
 */
public class SEPCreationPartieChannelListener implements ChannelListener, Serializable
{

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;
	
	private static final Logger logger = Logger.getLogger(SEPCreationPartieChannelListener.class.getName());

	/* (non-Javadoc)
	 * @see com.sun.sgs.app.ChannelListener#receivedMessage(com.sun.sgs.app.Channel, com.sun.sgs.app.ClientSession, java.nio.ByteBuffer)
	 */
	@Override
	public void receivedMessage(Channel channel, ClientSession sender, ByteBuffer message)
	{
		logger.log(Level.INFO, "Channel {0} received message from {1} : {2}", new Object[] {channel.getName(), sender.getName(), message.toString()});
		
	}

}
