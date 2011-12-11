/**
 * @author Escallier Pierre
 * @file UniverseRenderer.java
 * @date 7 juin 2009
 */
package org.axan.sep.client.gui;

import java.util.List;

import org.axan.sep.client.SEPClient;
import org.axan.sep.common.PlayerGameBoard;
import org.axan.sep.common.db.IGameEvent;


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
	void receiveNewTurnGameBoard(List<IGameEvent> newTurnEvents);
}
