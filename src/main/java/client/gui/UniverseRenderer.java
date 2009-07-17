/**
 * @author Escallier Pierre
 * @file UniverseRenderer.java
 * @date 7 juin 2009
 */
package client.gui;

import javax.swing.JPanel;

import common.PlayerGameBoard;
import common.SEPUtils.RealLocation;

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
