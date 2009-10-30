/**
 * @author Escallier Pierre
 * @file ServerGameTurn.java
 * @date 2 juin 2009
 */
package org.axan.sep.server.model;

import java.util.Map;

import org.axan.sep.common.Player;


/**
 * Represent a server game turn.
 * It keep track of all players moves and moves resolution.
 */
class ServerGameTurn
{
	private Map<Player, PlayerGameMove> moves;
}
