/**
 * @author Escallier Pierre
 * @file UniverseRenderer.java
 * @date 7 juin 2009
 */
package org.axan.sep.client.gui;

import javax.swing.JPanel;

import org.axan.sep.client.SEPClient;
import org.axan.sep.common.PlayerGameBoard;
import org.axan.sep.common.SEPUtils.RealLocation;


/**
 * 
 */
public interface IUniverseRenderer
{
	public static interface IUniverseRendererListener
	{
		void updateSelectedArea(int x, int y, int z);
	}

	SEPClient getSepClient();
	void setSepClient(SEPClient sepClient);
	
	void setUniverseRendererListener(IUniverseRendererListener listener);
	void refreshGameBoard(PlayerGameBoard gameboard);
}
