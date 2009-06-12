/**
 * @author Escallier Pierre
 * @file Protocol.java
 * @date 26 mai 2009
 */
package common;

import java.util.Set;

import org.axan.eplib.clientserver.rpc.RpcException;
import org.axan.eplib.gameserver.common.IServerUser.ServerPrivilegeException;
import org.axan.eplib.statemachine.StateMachine.StateMachineNotExpectedEventException;


/**
 * Define the game protocol (RPC services interfaces).
 */
public interface Protocol
{
	/**
	 * Methods that can be called by the client at any game stage.
	 */
	public static interface ServerCommon
	{
		/**
		 * Return player list.
		 * @return Set<Player> currently connected players.
		 * @throws RpcException On connection error.
		 * @throws StateMachineNotExpectedEventException If server is not in GameCreation state.
		 */
		Set<Player> getPlayerList() throws RpcException, StateMachineNotExpectedEventException;
		
		/**
		 * Return the current game config.
		 * @return GameConfig current game config.
		 * @throws RpcException On connection error.
		 * @throws StateMachineNotExpectedEventException If server is not in GameCreation state.
		 */
		GameConfig getGameConfig() throws RpcException, StateMachineNotExpectedEventException;
	}
	
	/**
	 * Methods that can be called by the client while the game is pending creation.
	 */
	public static interface ServerGameCreation extends ServerCommon
	{
		/**
		 * Update the player config and broadcast it to other players.
		 * @param playerCfg New player config.
		 * @throws RpcException On connection error.
		 * @throws StateMachineNotExpectedEventException If server is not in GameCreation state.
		 */
		void updatePlayerConfig(PlayerConfig playerCfg) throws RpcException, StateMachineNotExpectedEventException;
		
		/**
		 * Send a message to the GameCreation Chat.
		 * @param msg Message.
		 * @throws RpcException On connection error.
		 * @throws StateMachineNotExpectedEventException If server is not in GameCreation state.
		 */
		void sendMessage(String msg) throws RpcException, StateMachineNotExpectedEventException;
		
		/**
		 * Updage the game configuration.
		 * @param gameCfg new game config.
		 * @throws RpcException On connection error.
		 * @throws StateMachineNotExpectedEventException If server is not in GameCreation state.
		 * @throws ServerPrivilegeException If player is not authorised (admin).
		 */
		void updateGameConfig(GameConfig gameCfg) throws RpcException, StateMachineNotExpectedEventException, ServerPrivilegeException;
	}
	
	/**
	 * Methods that can be called by the client while the game is running.
	 */
	public static interface ServerRunningGame extends ServerCommon
	{

		/**
		 * Return the current game turn information for this player.
		 * @return GameTurnInfos current game turn information for this player.
		 * @throws RpcException On connection error.
		 * @throws StateMachineNotExpectedEventException If server is not in GameCreation state.
		 */
		PlayerGameBoard getGameBoard() throws RpcException, StateMachineNotExpectedEventException;

		/**
		 * Send a message to the RunningGame Chat.
		 * @param msg Message.
		 * @throws RpcException On connection error.
		 * @throws StateMachineNotExpectedEventException If server is not in GameCreation state.
		 */
		void sendMessage(String msg) throws RpcException, StateMachineNotExpectedEventException;
		
		/**
		 * Test if building type can be build on selected celestial body.
		 * @param ceslestialBodyName Name of the celestial body where to build.
		 * @param buildingType Building type.
		 * @throws RpcException On connection error.
		 * @throws StateMachineNotExpectedEventException If server is not in GameCreation state.
		 */
		void canBuild(String celestialBodyName, Class<? extends IBuilding> buildingType) throws RpcException, StateMachineNotExpectedEventException;
		
		/**
		 * Order construction of a new building on the given celestial body.
		 * @param ceslestialBodyName Name of the celestial body where to build.
		 * @param buildingType Building type.
		 * @throws RpcException On connection error.
		 * @throws StateMachineNotExpectedEventException If server is not in GameCreation state.
		 */
		void build(String ceslestialBodyName, Class<? extends IBuilding> buildingType) throws RpcException, StateMachineNotExpectedEventException;
		
		/**
		 * Test if selected building can be demolished.
		 * @param ceslestialBodyName Name of the celestial body the building is build on. 
		 * @param buildingType Building type to demolish.
		 * @throws RpcException On connection error.
		 * @throws StateMachineNotExpectedEventException If server is not in GameCreation state.
		 */
		void canDemolish(String celestialBodyName, Class<? extends IBuilding> buildingType) throws RpcException, StateMachineNotExpectedEventException;
		
		/**
		 * Order demolition of a building on the given celestial body.
		 * @param ceslestialBodyName Name of the celestial body the building is build on.
		 * @param buildingType Building type.
		 * @throws RpcException On connection error.
		 * @throws StateMachineNotExpectedEventException If server is not in GameCreation state.
		 */
		void demolish(String ceslestialBodyName, Class<? extends IBuilding> buildingType) throws RpcException, StateMachineNotExpectedEventException;
		
		/**
		 * Test if starship can be made on the selected planet
		 * @param planetName
		 */
		void canMakeStarship(String planetName);
		
		/**
		 * Make given starships on the given planet.
		 * @param planetName Planet where is the starship plant.
		 * @param starshipType Startship type to make.
		 * @param quantity Quantity to make.
		 */
		void makeStarship(String planetName, Class<? extends IStarship> starshipType, int quantity);
		
		can
		// constituer flotte(corps céleste, constitution vaisseaux, nom nouvelle flotte)
		
		/**
		 * Order to embark the government (from government module) on a government starship.
		 * @throws RpcException On connection error.
		 * @throws StateMachineNotExpectedEventException If server is not in GameCreation state.
		 */
		void embarkGovernment() throws RpcException, StateMachineNotExpectedEventException;
		
		/**
		 * Order to settle the government (from government starship) in the planet the government starship is currently landed.
		 * @throws RpcException On connection error.
		 * @throws StateMachineNotExpectedEventException If server is not in GameCreation state.
		 */
		void settleGovernment() throws RpcException, StateMachineNotExpectedEventException;
	}
	
	/**
	 * Methods that can be called by the client while the game is paused.
	 */
	public static interface ServerPausedGame extends ServerCommon
	{
		
	}
	
	/**
	 * Methods that can be called by the server.
	 */
	public static interface Client
	{

		/**
		 * Server notify the client to refresh the player list.
		 * @param playerList New player list.
		 * @throws RpcException On connection error.
		 */
		void refreshPlayerList(Set<Player> playerList) throws RpcException;

		/**
		 * Server notify the client to refresh the game config. Client is expected to display it in GameCreation panel.
		 * @param gameCfg New game config.
		 * @throws RpcException On connection error.
		 */
		void refreshGameConfig(GameConfig gameCfg) throws RpcException;
		
		/**
		 * Server broadcast game creation message from another player.
		 * @param fromPlayer Sender player. 
		 * @param msg Message.
		 * @throws RpcException On connection error.
		 */
		void receiveGameCreationMessage(Player fromPlayer, String msg) throws RpcException;

		/**
		 * Server broadcast running game message from another player.
		 * @param fromPlayer Sender player. 
		 * @param msg Message.
		 * @throws RpcException On connection error.
		 */
		void receiveRunningGameMessage(Player fromPlayer, String msg) throws RpcException;
		
	}
}
