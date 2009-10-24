/**
 * @author Escallier Pierre
 * @file GUIUtils.java
 * @date 7 juin 2009
 */
package org.axan.sep.client.gui.lib;

import java.awt.Color;

/**
 * 
 */
public abstract class GUIUtils
{
	public static String getHTMLColor(Color c)
	{
		return String.format("%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
	}
}
