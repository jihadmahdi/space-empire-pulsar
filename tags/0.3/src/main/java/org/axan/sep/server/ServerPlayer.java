/**
 * @author Escallier Pierre
 * @file ServerPlayer.java
 * @date 28 mai 2009
 */
package org.axan.sep.server;

import java.util.Random;
import java.util.logging.Level;

import org.axan.eplib.clientserver.rpc.RpcException;
import org.axan.eplib.gameserver.server.GameServer.ServerUser;
import org.axan.sep.common.Protocol;


/**
 * This class represent a Player from server point of view.
 */
class ServerPlayer
{
	private static final Random rnd = new Random();
	
	private ServerUser			serverUser;	
	
	/**
	 * Full constructor.
	 */
	public ServerPlayer(ServerUser user)
	{
		this.serverUser = user;
	}

	/**
	 * Get name.
	 */
	public String getName()
	{
		return serverUser.getLogin();
	}

	/**
	 * Compute this serverPlayer instant snapshot as a common Player object.
	 * 
	 * @return Player common player object.
	 *//*
	public Player getPlayer()
	{
		return player;
	}*/

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
		
		SEPServer.log.log(Level.WARNING, msg);
		
		if (SEPServer.log.isLoggable(Level.WARNING))
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
	 *//*
	public void setConfig(PlayerConfig playerCfg)
	{
		if (playerCfg.getName().compareTo(getName()) != 0) throw new Error("Player.name != PlayerConfig.name");
		this.config = playerCfg;		
	}
	
	public PlayerConfig getConfig()
	{
		return config;
	}*/
}
