/**
 * @author Escallier Pierre
 * @file PlayerConfig.java
 * @date 29 mai 2009
 */
package org.axan.sep.common;

import java.awt.Color;
import java.awt.Image;
import java.io.Serializable;
import java.util.Random;

/**
 * Represent a GameCreation player config.
 * It is an immuable class.
 */
@Deprecated // Use common.db.IPlayerConfig instead.
public class PlayerConfig implements Serializable
{
	private static final Random	random	= new Random();
	private static final Color[] colors = new Color[]{Color.black, Color.blue, Color.cyan, Color.darkGray, Color.gray, Color.green, Color.lightGray, Color.magenta, Color.orange, Color.pink, Color.red, Color.yellow, Color.white};

	/**
	 * Serializable version. 
	 */
	private static final long	serialVersionUID	= 1L;
	
	private final Color color;
	private final Image portrait;
	private final Image symbol;
	
	/**
	 * Default constructor. 
	 */
	public PlayerConfig()
	{
		this(colors[random.nextInt(colors.length)], null, null);
	}
	
	public PlayerConfig(Color color, Image portrait, Image symbol)
	{
		this.color = color;
		this.portrait = portrait;
		this.symbol = symbol;
	}
	
	/**
	 * Get color.
	 */
	public Color getColor()
	{
		return color;
	}
	
	/**
	 * Get portrait.
	 */
	public Image getPortrait()
	{
		return portrait;
	}
	
	/**
	 * Get symbol.
	 */
	public Image getSymbol()
	{
		return symbol;
	}
}
