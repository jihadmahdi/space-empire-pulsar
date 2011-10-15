/**
 * @author Escallier Pierre
 * @file Player.java
 * @date 28 mai 2009
 */
package org.axan.sep.common;

import java.io.Serializable;

/**
 * 
 */
@Deprecated // Use common.db.IPlayer instead.
public class Player implements Serializable, Comparable<Player>
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
	
	@Override
	public String toString()
	{
		return getName();
	}

	@Override
	public int compareTo(Player o)
	{
		return toString().compareTo(o.toString());
	}
}
