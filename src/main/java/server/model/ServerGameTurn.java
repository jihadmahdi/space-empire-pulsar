/**
 * @author Escallier Pierre
 * @file ServerGameTurn.java
 * @date 2 juin 2009
 */
package server.model;

import java.util.HashMap;
import java.util.Map;

import common.Player;

/**
 * Represent a server game turn.
 * It keep track of all players moves and moves resolution.
 */
public class ServerGameTurn
{
	private Map<Player, PlayerGameMove> moves;
}
