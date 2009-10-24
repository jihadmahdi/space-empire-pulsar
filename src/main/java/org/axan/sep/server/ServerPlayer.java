/**
 * @author Escallier Pierre
 * @file ServerPlayer.java
 * @date 28 mai 2009
 */
package org.axan.sep.server;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.axan.eplib.clientserver.rpc.RpcException;
import org.axan.eplib.gameserver.server.GameServer.ServerUser;
import org.axan.sep.common.Player;
import org.axan.sep.common.PlayerConfig;
import org.axan.sep.common.Protocol;


/**
 * This class represent a Player from server point of view.
 */
class ServerPlayer
{	
	static final Logger log = SEPServer.log;
	
	private final String		name;
	private PlayerConfig	config;
	private ServerUser			serverUser;
	private Player			cachedClientPlayer;

	/**
	 * Full constructor.
	 */
	public ServerPlayer(ServerUser user)
	{
		this.name = user.getLogin();
		this.serverUser = user;
		this.config = new PlayerConfig();		
	}

	/**
	 * Get name.
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Compute this serverPlayer instant snapshot as a common Player object.
	 * 
	 * @return Player common player object.
	 */
	public Player getPlayer()
	{
		if (cachedClientPlayer == null)
		{
			cachedClientPlayer = new Player(name, config);
		}
		return cachedClientPlayer;
	}

	/**
	 * @param serverUser
	 */
	public void setServerUser(ServerUser serverUser)
	{
		this.serverUser = serverUser;
		sepClientInterface = null;
	}

	public boolean isConnected()
	{
		if (serverUser == null) return false;
		return true;
	}
	
	public void disconnect()
	{
		if (serverUser == null) return;
		serverUser.disconnect();
	}
	
	public void abort(Throwable t)
	{
		String msg = String.format("Player %s abort connection : %s", getName(), (t != null && t.getMessage() != null)?t.getMessage():(t != null)?t.getClass().getName():"no reason");
		
		log.log(Level.WARNING, msg);
		
		if (log.isLoggable(Level.WARNING))
		{
			t.printStackTrace();
		}
		
		disconnect();
	}

	private Protocol.Client	sepClientInterface	= null;

	public Protocol.Client getClientInterface() throws RpcException
	{
		if (sepClientInterface == null)
		{
			sepClientInterface = serverUser.getClientInterface(Protocol.Client.class);
		}
		return sepClientInterface;
	}

	/**
	 * @param playerCfg
	 */
	public void setConfig(PlayerConfig playerCfg)
	{
		this.config = playerCfg;
		this.cachedClientPlayer = null;
	}
}
