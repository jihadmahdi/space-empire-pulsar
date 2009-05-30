/**
 * @author Escallier Pierre
 * @file ServerPlayer.java
 * @date 28 mai 2009
 */
package server;

import java.awt.Color;
import java.awt.Image;
import java.util.Random;
import java.util.logging.Level;

import org.axan.eplib.clientserver.rpc.RpcException;
import org.axan.eplib.gameserver.server.GameServer.ServerUser;

import common.Player;
import common.PlayerConfig;
import common.Protocol;

/**
 * This class represent a Player from server point of view.
 */
class ServerPlayer
{	
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
	
	public void disconnect(String msg)
	{
		if (serverUser == null) return;
		serverUser.stop(msg);
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
