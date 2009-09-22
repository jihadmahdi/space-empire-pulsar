/**
 * @author Escallier Pierre
 * @file Player.java
 * @date 28 mai 2009
 */
package common;

import java.awt.Color;
import java.awt.Image;
import java.io.Serializable;

import org.axan.eplib.gameserver.server.GameServer.ServerUser;

/**
 * 
 */
public class Player implements Serializable
{
	/**
	 * Serializable version. 
	 */
	private static final long	serialVersionUID	= 1L;
	
	
	private final String name;
	private PlayerConfig config;
	
	/**
	 * Full constructor.
	 */
	public Player(String name, PlayerConfig config)
	{
		this.name = name;
		this.config = config;
	}
	
	/**
	 * Get name.
	 */
	public String getName()
	{
		return name;
	}
	
	public PlayerConfig getConfig()
	{
		return config;
	}

	/**
	 * @param playerLogin
	 * @return
	 */
	public boolean isNamed(String playerLogin)
	{
		return name.equals(playerLogin);
	}

	public static String getName(Player player)
	{
		return (player == null ? "Unknown" : player.getName());
	}
}
