/**
 * @author Escallier Pierre
 * @file GovernmentModule.java
 * @date 1 juin 2009
 */
package server.model;

import common.Player;

/**
 * This is a government module build on a celestial body.
 */
class GovernmentModule implements IBuilding
{

	/* (non-Javadoc)
	 * @see server.model.IBuilding#getPlayerView(java.lang.String)
	 */
	@Override
	public common.GovernmentModule getPlayerView(int date, String playerLogin)
	{
		return new common.GovernmentModule();
	}

}
