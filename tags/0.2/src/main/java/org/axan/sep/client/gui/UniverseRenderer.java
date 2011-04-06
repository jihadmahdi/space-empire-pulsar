/**
 * @author Escallier Pierre
 * @file UniverseRenderer.java
 * @date 7 juin 2009
 */
package org.axan.sep.client.gui;

import javax.swing.JPanel;

import org.axan.sep.common.PlayerGameBoard;
import org.axan.sep.common.SEPUtils.RealLocation;


/**
 * 
 */
public interface UniverseRenderer
{
	public static interface UniverseRendererListener
	{
		void updateSelectedArea(RealLocation location);
	}

	void setListener(UniverseRendererListener listener);
	void refreshGameBoard(PlayerGameBoard gameBoard);
	JPanel getPanel();
}
