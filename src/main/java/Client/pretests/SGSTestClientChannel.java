/**
 * @author Escallier Pierre
 * @file SGSTestClientChannel.java
 * @date 11 juin 08
 */
package Client.pretests;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import com.sun.sgs.client.ClientChannel;
import com.sun.sgs.client.ClientChannelListener;

/**
 * 
 */
public class SGSTestClientChannel extends SGSTestClient
{
    /** The version of the serialized form of this class. */
    private static final long serialVersionUID = 1L;

    /** Map that associates a channel name with a {@link ClientChannel}. */
    protected final Map<String, ClientChannel> channelsByName =
        new HashMap<String, ClientChannel>();

    /** The UI selector among direct messaging and different channels. */
    protected JComboBox channelSelector;

    /** The data model for the channel selector. */
    protected DefaultComboBoxModel channelSelectorModel;

    /** Sequence generator for counting channels. */
    protected final AtomicInteger channelNumberSequence =
        new AtomicInteger(1);

    // Main

    /**
     * Runs an instance of this client.
     *
     * @param args the command-line arguments (unused)
     */
    public static void main(String[] args) {
        new SGSTestClientChannel().login();
    }

    // HelloChannelClient methods

    /**
     * Creates a new client UI.
     */
    public SGSTestClientChannel() {
        super(SGSTestClientChannel.class.getSimpleName());
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation adds a channel selector component next
     * to the input text field to allow users to choose between
     * direct-to-server messages and channel broadcasts.
     */
    @Override
    protected void populateInputPanel(JPanel panel) {
        super.populateInputPanel(panel);

        channelSelectorModel = new DefaultComboBoxModel();
        channelSelectorModel.addElement("<DIRECT>");
        channelSelector = new JComboBox(channelSelectorModel);
        channelSelector.setFocusable(false);
        panel.add(channelSelector, BorderLayout.WEST);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns a listener that formats and displays received channel
     * messages in the output text pane.
     */
    @Override
    public ClientChannelListener joinedChannel(ClientChannel channel) {
        String channelName = channel.getName();
        channelsByName.put(channelName, channel);
        appendOutput("Joined to channel " + channelName);
        channelSelectorModel.addElement(channelName);
        return new HelloChannelListener();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        if (! simpleClient.isConnected())
            return;

        String text = getInputText();
        String channelName =
            (String) channelSelector.getSelectedItem();
        if (channelName.equalsIgnoreCase("<DIRECT>")) {
            send(text);
        } else {
            ClientChannel channel = channelsByName.get(channelName);
            try {
                channel.send(encodeString(text));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * A simple listener for channel events.
     */
    public class HelloChannelListener
        implements ClientChannelListener
    {
        /**
         * An example of per-channel state, recording the number of
         * channel joins when the client joined this channel.
         */
        private final int channelNumber;

        /**
         * Creates a new {@code HelloChannelListener}. Note that
         * the listener will be given the channel on its callback
         * methods, so it does not need to record the channel as
         * state during the join.
         */
        public HelloChannelListener() {
            channelNumber = channelNumberSequence.getAndIncrement();
        }

        /**
         * {@inheritDoc}
         * <p>
         * Displays a message when this client leaves a channel.
         */
        public void leftChannel(ClientChannel channel) {
            appendOutput("Removed from channel " + channel.getName());
        }

        /**
         * {@inheritDoc}
         * <p>
         * Formats and displays messages received on a channel.
         */
        public void receivedMessage(ClientChannel channel, ByteBuffer message) {
            appendOutput("[" + channel.getName() + "/ " + channelNumber +
                "] " + decodeString(message));
        }
    }

}
