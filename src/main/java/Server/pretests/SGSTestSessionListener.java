/**
 * @author Escallier Pierre
 * @file SGSTestSessionListener.java
 * @date 11 juin 08
 */
package Server.pretests;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.Channel;
import com.sun.sgs.app.ChannelManager;
import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.ClientSessionListener;
import com.sun.sgs.app.DataManager;
import com.sun.sgs.app.ManagedReference;

/**
 * 
 */
class SGSTestSessionListener implements Serializable, ClientSessionListener
{
/** The version of the serialized form of this class. */
private static final long serialVersionUID = 1L;

/** The {@link Logger} for this class. */
private static final Logger logger =
    Logger.getLogger(SGSTestSessionListener.class.getName());

/** The session this {@code ClientSessionListener} is listening to. */
private final ManagedReference<ClientSession> sessionRef;

/**
 * Creates a new {@code HelloChannelsSessionListener} for the session.
 *
 * @param session the session this listener is associated with
 * @param channel1 a reference to a channel to join
 */
public SGSTestSessionListener(ClientSession session,
                                    ManagedReference<Channel> channel1)
{
    if (session == null)
        throw new NullPointerException("null session");

    DataManager dataMgr = AppContext.getDataManager();
    sessionRef = dataMgr.createReference(session);
    
    // Join the session to all channels.  We obtain the channel
    // in two different ways, by reference and by name.
    ChannelManager channelMgr = AppContext.getChannelManager();
    
    // We were passed a reference to the first channel.
    channel1.get().join(session);
    
    // We look up the second channel by name.
    Channel channel2 = channelMgr.getChannel(SGSTest.CHANNEL_2_NAME);
    channel2.join(session);
}

/**
 * Returns the session for this listener.
 * 
 * @return the session for this listener
 */
protected ClientSession getSession() {
    // We created the ref with a non-null session, so no need to check it.
    return sessionRef.get();
}

/**
 * {@inheritDoc}
 * <p>
 * Logs when data arrives from the client, and echoes the message back.
 */
public void receivedMessage(ByteBuffer message) {
    ClientSession session = getSession();
    String sessionName = session.getName();

    if (logger.isLoggable(Level.FINE)) {
        logger.log(Level.FINE, "Message from {0}", sessionName);
    }
    session.send(message);
}

/**
 * {@inheritDoc}
 * <p>
 * Logs when the client disconnects.
 */
public void disconnected(boolean graceful) {
    ClientSession session = getSession();
    String grace = graceful ? "graceful" : "forced";
    logger.log(Level.INFO,
        "User {0} has logged out {1}",
        new Object[] { session.getName(), grace }
    );
}
}
