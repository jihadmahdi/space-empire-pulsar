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
import org.axan.sep.common.db.orm.SEPCommonDB.IAreaChangeListener;


/**
 * 
 */
public interface IUniverseRenderer extends IAreaChangeListener
{
	public static interface IAreaSelectionListener
	{
		void updateSelectedArea(int x, int y, int z);
	}
	
	public static interface IUniverseRendererListener extends IAreaSelectionListener
	{
		
	}

	SEPClient getSepClient();
	void setSepClient(SEPClient sepClient);

	void setAreaSelectionListener(IAreaSelectionListener listener);
	void unsetAreaSelectionListener(IAreaSelectionListener listener);
	boolean isAreaSelectionListener(IAreaSelectionListener listener);
	
	void setUniverseRendererListener(IUniverseRendererListener listener);
	void receiveNewTurnGameBoard(List<IGameEvent> newTurnEvents);
}
