/**
 * @author Escallier Pierre
 * @file SGSTest.java
 * @date 4 avr. 08
 */
package Server.pretests;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import Server.metier.Partie;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.AppListener;
import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.ClientSessionListener;
import com.sun.sgs.app.DataManager;

/**
 * 
 */
public class SGSTest implements Serializable, AppListener
{
    /** The version of the serialized form of this class. */
    private static final long serialVersionUID = 1L;

    /** The {@link Logger} for this class. */
    private static final Logger logger =
        Logger.getLogger(SGSTest.class.getName());
    
    private HashSet<Partie> listeParties = new HashSet<Partie>();
    
    /**
     * {@inheritDoc}
     * <p>
     * Creates the channels.  Channels persist across server restarts,
     * so they only need to be created here in {@code initialize}.
     */
    public void initialize(Properties props) {
        logger.info("Initializing Server");
        DataManager dm = AppContext.getDataManager();
        dm.setBinding("Server", this);
        logger.info("Server initialized (bounded)");
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns a {@link HelloChannelsSessionListener} for the
     * logged-in session.
     */
    public ClientSessionListener loggedIn(ClientSession session) {
        logger.log(Level.INFO, "User {0} has logged in", session.getName());
        
        return SGSTestClientSessionListener.loggedIn(session);
    }
    
    protected HashSet<Partie> getListeParties()
    {
    	return listeParties;
    }

	/**
	 * @param nomPartie
	 * @return
	 */
	public boolean creerPartie(String nomPartie)
	{
		if (listeParties.contains(nomPartie)) return false;
		DataManager dm = AppContext.getDataManager();
		dm.markForUpdate(this);
		listeParties.add(new Partie(nomPartie));
		return true;
	}

}
