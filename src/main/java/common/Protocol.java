/**
 * @author Escallier Pierre
 * @file Protocol.java
 * @date 26 mai 2009
 */
package common;

import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.axan.eplib.clientserver.rpc.RpcException;
import org.axan.eplib.gameserver.common.IServerUser.ServerPrivilegeException;
import org.axan.eplib.statemachine.StateMachine.StateMachineNotExpectedEventException;

import common.Protocol.ServerRunningGame.RunningGameCommandException;
import common.SEPUtils.RealLocation;


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
		public static class RunningGameCommandException extends Exception
		{
			private static final long	serialVersionUID	= 1L;

			public RunningGameCommandException(Throwable t)
			{
				super(t);
			}

			public RunningGameCommandException(String msg)
			{
				super(msg);
			}
		}

		/**
		 * Return the current game turn information for this player.
		 * @return GameTurnInfos current game turn information for this player.
		 * @throws RpcException On connection error.
		 * @throws StateMachineNotExpectedEventException If server is not in GameCreation state.
		 */
		PlayerGameBoard getPlayerGameBoard() throws RpcException, StateMachineNotExpectedEventException;

		/**
		 * Test if player can send a message.
		 * @param msg
		 * @throws RpcException On connection error.
		 * @throws StateMachineNotExpectedEventException If server is not in GameCreation state.
		 */
		void canSendMessage(String msg) throws RpcException, StateMachineNotExpectedEventException;
		
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
		boolean canBuild(String celestialBodyName, Class<? extends IBuilding> buildingType) throws RpcException, StateMachineNotExpectedEventException;
		
		/**
		 * Order construction of a new building on the given celestial body.
		 * @param ceslestialBodyName Name of the celestial body where to build.
		 * @param buildingType Building type.
		 * @throws RpcException On connection error.
		 * @throws StateMachineNotExpectedEventException If server is not in GameCreation state.
		 * @throws RunningGameCommandException If command is not expected or arguments are not corrects. 
		 */
		void build(String ceslestialBodyName, Class<? extends IBuilding> buildingType) throws RpcException, StateMachineNotExpectedEventException, RunningGameCommandException;
		
		/**
		 * Test if selected building can be demolished.
		 * @param ceslestialBodyName Name of the celestial body the building is build on. 
		 * @param buildingType Building type to demolish.
		 * @throws RpcException On connection error.
		 * @throws StateMachineNotExpectedEventException If server is not in GameCreation state.
		 */
		boolean canDemolish(String celestialBodyName, Class<? extends IBuilding> buildingType) throws RpcException, StateMachineNotExpectedEventException;
		
		/**
		 * Order demolition of a building on the given celestial body.
		 * @param ceslestialBodyName Name of the celestial body the building is build on.
		 * @param buildingType Building type.
		 * @throws RpcException On connection error.
		 * @throws StateMachineNotExpectedEventException If server is not in GameCreation state.
		 */
		void demolish(String ceslestialBodyName, Class<? extends IBuilding> buildingType) throws RpcException, StateMachineNotExpectedEventException, RunningGameCommandException;
		
		/**
		 * Test if starship can be made on the selected planet
		 * @param starshipToMake
		 * @throws RpcException On connection error.
		 * @throws StateMachineNotExpectedEventException If server is not in GameCreation state.
		 */
		boolean canMakeStarships(String planetName, Map<StarshipTemplate, Integer> starshipsToMake) throws RpcException, StateMachineNotExpectedEventException;
		
		/**
		 * Make given starships on the given planet.
		 * @param planetName Planet where is the starship plant.
		 * @param starshipType Startship type to make.
		 * @param quantity Quantity to make.
		 * @throws RpcException On connection error.
		 * @throws StateMachineNotExpectedEventException If server is not in GameCreation state.
		 * @throws RunningGameCommandException If command is not expected or arguments are not corrects.
		 */
		void makeStarships(String planetName, Map<StarshipTemplate, Integer> starshipsToMake) throws RpcException, StateMachineNotExpectedEventException, RunningGameCommandException;
		
		/**
		 * Test if probe can be made on the selected planet with given name.
		 * @param planetName
		 * @param probeName
		 * @return
		 * @throws RpcException
		 * @throws StateMachineNotExpectedEventException
		 * @throws RunningGameCommandException
		 */
		boolean canMakeProbes(String planetName, String probeName, int quantity) throws RpcException, StateMachineNotExpectedEventException;
		
		/**
		 * Make the given quantity of probes on the given planet, named by probeName+unit_number.
		 * @param planetName
		 * @param probeName
		 * @param quantity
		 * @throws RpcException
		 * @throws StateMachineNotExpectedEventException
		 * @throws RunningGameCommandException
		 */
		void makeProbes(String planetName, String probeName, int quantity) throws RpcException, StateMachineNotExpectedEventException, RunningGameCommandException;
		
		/**
		 * Test if probe can be made on the selected planet with given name.
		 * @param planetName
		 * @param antiProbeMissileName
		 * @return
		 * @throws RpcException
		 * @throws StateMachineNotExpectedEventException
		 * @throws RunningGameCommandException
		 */
		boolean canMakeAntiProbeMissiles(String planetName, String antiProbeMissileName, int quantity) throws RpcException, StateMachineNotExpectedEventException;
		
		/**
		 * Make the given quantity of probes on the given planet, named by probeName+unit_number.
		 * @param planetName
		 * @param antiProbeMissileName
		 * @param quantity
		 * @throws RpcException
		 * @throws StateMachineNotExpectedEventException
		 * @throws RunningGameCommandException
		 */
		void makeAntiProbeMissiles(String planetName, String antiProbeMissileName, int quantity) throws RpcException, StateMachineNotExpectedEventException, RunningGameCommandException;
		
		/**
		 * Test if fleet can be formed on this planet (starship plant existence).
		 * @param fleetToForm Planet where the fleet is supposed to be formed.
		 * @throws RpcException On connection error.
		 * @throws StateMachineNotExpectedEventException If server is not in GameCreation state.
		 */
		boolean canFormFleet(String planetName, String fleetName, Map<StarshipTemplate, Integer> fleetToFormStarships, Set<ISpecialUnit> fleetToFormSpecialUnits) throws RpcException, StateMachineNotExpectedEventException;
		
		/**
		 * Form a new fleet from the given starships composition.
		 * @param planetName Planet where is the starship plant.
		 * @param composition Starships composition (number of each starship type).
		 * @param fleetName New fleet name.
		 * @throws RpcException On connection error.
		 * @throws StateMachineNotExpectedEventException If server is not in GameCreation state.
		 * @throws RunningGameCommandException If command is not expected or arguments are not corrects.
		 */
		void formFleet(String planetName, String fleetName, Map<StarshipTemplate, Integer> fleetToFormStarships, Set<ISpecialUnit> fleetToFormSpeciaUnits) throws RpcException, StateMachineNotExpectedEventException, RunningGameCommandException;
		
		/**
		 * Test if the given fleet can be dismantled.
		 * @param fleetName
		 * @throws RpcException On connection error.
		 * @throws StateMachineNotExpectedEventException If server is not in GameCreation state.
		 */
		boolean canDismantleFleet(String fleetName) throws RpcException, StateMachineNotExpectedEventException;
		
		/**
		 * Dismantle the given fleet and land the starships in the starship plant.
		 * @param fleetName
		 * @throws RpcException On connection error.
		 * @throws StateMachineNotExpectedEventException If server is not in GameCreation state.
		 * @throws RunningGameCommandException 
		 */
		void dismantleFleet(String fleetName) throws RpcException, StateMachineNotExpectedEventException, RunningGameCommandException;
		
		/**
		 * Test if the government can be embarked.
		 * @throws RpcException On connection error.
		 * @throws StateMachineNotExpectedEventException If server is not in GameCreation state.
		 */
		boolean canEmbarkGovernment() throws RpcException, StateMachineNotExpectedEventException;
		
		/**
		 * Order to embark the government (from government module) on a government starship.
		 * @throws RpcException On connection error.
		 * @throws StateMachineNotExpectedEventException If server is not in GameCreation state.
		 * @throws RunningGameCommandException 
		 */
		void embarkGovernment() throws RpcException, StateMachineNotExpectedEventException, RunningGameCommandException;
		
		/**
		 * Test if the government can be settled according to the government starship current location.
		 * @param planetName Planet where to test if government can settle.
		 * @throws RpcException On connection error.
		 * @throws StateMachineNotExpectedEventException If server is not in GameCreation state.
		 */
		boolean canSettleGovernment(String planetName) throws RpcException, StateMachineNotExpectedEventException;
		
		/**
		 * Order to settle the government (from government starship) in the planet the government starship is currently landed.
		 * @throws RpcException On connection error.
		 * @throws StateMachineNotExpectedEventException If server is not in GameCreation state.
		 */
		void settleGovernment() throws RpcException, StateMachineNotExpectedEventException;
		
		/**
		 * Test if pulsar missile can be fired from the given celestial body.
		 * @param celestialBodyName Celestial body to check.
		 * @throws RpcException On connection error.
		 * @throws StateMachineNotExpectedEventException If server is not in GameCreation state.
		 */
		boolean canFirePulsarMissile(String celestialBodyName) throws RpcException, StateMachineNotExpectedEventException;
		
		/**
		 * Order to fire a pulsar missile from the given celestial body with the given bonus modifier.
		 * @param celestialBodyName Celestial body where the pulsar launching pad are supposed to be.
		 * @param bonusModifier Bonus modifier.
		 * @throws RpcException On connection error.
		 * @throws StateMachineNotExpectedEventException If server is not in GameCreation state.
		 */
		void firePulsarMissile(String celestialBodyName, float bonusModifier) throws RpcException, StateMachineNotExpectedEventException;
		
		/**
		 * Test if player can build a space road.
		 */
		void canBuildSpaceRoad() throws RpcException, StateMachineNotExpectedEventException;
		
		/**
		 * Order to build a space road between the given celestial bodies.
		 * @param celestialBodyNameA
		 * @param celestialBodyNameB
		 * @throws RpcException On connection error.
		 * @throws StateMachineNotExpectedEventException If server is not in GameCreation state.
		 */
		void buildSpaceRoad(String celestialBodyNameA, String celestialBodyNameB) throws RpcException, StateMachineNotExpectedEventException;
		
		/**
		 * Test if player can demolish a space road.
		 * @throws RpcException On connection error.
		 * @throws StateMachineNotExpectedEventException If server is not in GameCreation state.
		 */
		void canDemolishSpaceRoad() throws RpcException, StateMachineNotExpectedEventException;
		
		/**
		 * Order to demolish a space road.
		 * @param celestialBodyNameA
		 * @param celestialBodyNameB
		 * @throws RpcException On connection error.
		 * @throws StateMachineNotExpectedEventException If server is not in GameCreation state.
		 */
		void demolishSpaceRoad(String celestialBodyNameA, String celestialBodyNameB) throws RpcException, StateMachineNotExpectedEventException;
		
		/**
		 * Test if player can modify a carbon order.
		 * @throws RpcException On connection error.
		 * @throws StateMachineNotExpectedEventException If server is not in GameCreation state. 
		 */
		void canModifyCarbonOrder() throws RpcException, StateMachineNotExpectedEventException;
		
		/**
		 * Modify/create a carbon order from two celestial bodies.
		 * @param originCelestialBodyName
		 * @param destinationCelestialBodyName
		 * @param amount
		 * @throws RpcException On connection error.
		 * @throws StateMachineNotExpectedEventException If server is not in GameCreation state.
		 */
		void modifyCarbonOrder(String originCelestialBodyName, String destinationCelestialBodyName, int amount) throws RpcException, StateMachineNotExpectedEventException;
		
		/**
		 * Test if fleet can be moved.
		 * @throws RpcException On connection error.
		 * @throws StateMachineNotExpectedEventException If server is not in GameCreation state.
		 */
		void canMoveFleet() throws RpcException, StateMachineNotExpectedEventException;
		
		/**
		 * Order a fleet to move with optionnal delay and checkpoints list.
		 * @param fleetName
		 * @param delay
		 * @param checkpoints
		 * @throws RpcException On connection error.
		 * @throws StateMachineNotExpectedEventException If server is not in GameCreation state.
		 * @throws RunningGameCommandException 
		 */
		void moveFleet(String fleetName, Stack<Fleet.Move> checkpoints) throws RpcException, StateMachineNotExpectedEventException, RunningGameCommandException;
		
		/**
		 * Test if he given antiprobe missile can be fired on the given target.
		 * @param antiProbeMissileName
		 * @param targetProbeName
		 * @return
		 * @throws RpcException
		 * @throws StateMachineNotExpectedEventException
		 */
		boolean canFireAntiProbeMissile(String antiProbeMissileName, String targetOwnerName, String targetProbeName) throws RpcException, StateMachineNotExpectedEventException;
		
		/**
		 * Order to fire the antiprobe missile onto the given probe target.
		 * @param antiProbeMissileName
		 * @param targetProbeName
		 * @throws RpcException
		 * @throws StateMachineNotExpectedEventException
		 * @throws RunningGameCommandException 
		 */
		void fireAntiProbeMissile(String antiProbeMissileName, String targetOwnerName, String targetProbeName) throws RpcException, StateMachineNotExpectedEventException, RunningGameCommandException;
		
		/**
		 * Test if probe can be launched.
		 * @throws RpcException On connection error.
		 * @throws StateMachineNotExpectedEventException If server is not in GameCreation state.
		 */
		boolean canLaunchProbe(String probeName, RealLocation destination) throws RpcException, StateMachineNotExpectedEventException;
		
		/**
		 * Order to launch a probe to the specified destination.
		 * @param probeName
		 * @param destination
		 * @throws RpcException On connection error.
		 * @throws StateMachineNotExpectedEventException If server is not in GameCreation state.
		 * @throws RunningGameCommandException 
		 */
		void launchProbe(String probeName, RealLocation destination) throws RpcException, StateMachineNotExpectedEventException, RunningGameCommandException;
		
		/**
		 * Test if player can attack enemies fleet.
		 * @throws RpcException On connection error.
		 * @throws StateMachineNotExpectedEventException If server is not in GameCreation state.
		 */
		void canAttackEnemiesFleet() throws RpcException, StateMachineNotExpectedEventException;
		
		/**
		 * Order a celestial body to attack enemies fleet.
		 * @param celestialBodyName
		 * @throws RpcException On connection error.
		 * @throws StateMachineNotExpectedEventException If server is not in GameCreation state.
		 */
		void attackEnemiesFleet(String celestialBodyName) throws RpcException, StateMachineNotExpectedEventException;
		
		/**
		 * Test if player can change its diplomaty.
		 * @throws RpcException On connection error.
		 * @throws StateMachineNotExpectedEventException If server is not in GameCreation state.
		 */
		boolean canChangeDiplomacy(Diplomacy newDiplomacy) throws RpcException, StateMachineNotExpectedEventException;
		
		/**
		 * Change the player domestic policy.
		 * @throws RpcException On connection error.
		 * @throws StateMachineNotExpectedEventException If server is not in GameCreation state.
		 * @throws RunningGameCommandException 
		 */
		void changeDiplomacy(Diplomacy newDiplomacy) throws RpcException, StateMachineNotExpectedEventException, RunningGameCommandException;

		/**
		 * Test if turn can be reseted (not ended yet).
		 * @throws RpcException
		 * @throws StateMachineNotExpectedEventException
		 */
		boolean canResetTurn() throws RpcException, StateMachineNotExpectedEventException;
		
		/**
		 * Reset current player turn (erase commands).
		 * @throws RpcException On connection error.
		 * @throws StateMachineNotExpectedEventException If server is not in GameCreation state.
		 * @throws RunningGameCommandException If command is not expected or arguments are not corrects.
		 */
		void resetTurn() throws RpcException, StateMachineNotExpectedEventException, RunningGameCommandException;

		/**
		 * Test if turn can be ended (not already ended).
		 * @throws RpcException
		 * @throws StateMachineNotExpectedEventException
		 */
		boolean canEndTurn() throws RpcException, StateMachineNotExpectedEventException;
		
		/**
		 * Terminate the current turn.
		 * @throws RpcException On connection error.
		 * @throws StateMachineNotExpectedEventException If server is not in GameCreation state.
		 */
		void endTurn() throws RpcException, StateMachineNotExpectedEventException;
	}
	
	/**
	 * Methods that can be called by the client while the game is paused.
	 */
	public static interface ServerPausedGame extends ServerCommon
	{
		/**
		 * Return player list.
		 * @return Set<Player> currently connected players.
		 * @throws RpcException On connection error.
		 * @throws StateMachineNotExpectedEventException If server is not in GameCreation state.
		 */
		Map<Player, Boolean> getPlayerStateList() throws RpcException, StateMachineNotExpectedEventException;

		/**
		 * Send a message to the PausedGame Chat.
		 * @param msg Message.
		 * @throws RpcException On connection error.
		 * @throws StateMachineNotExpectedEventException If server is not in GameCreation state.
		 */
		void sendMessage(String msg) throws RpcException, StateMachineNotExpectedEventException;				
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
		
		/**
		 * Server broadcast paused game message from another player.
		 * @param fromPlayer Sender player. 
		 * @param msg Message.
		 * @throws RpcException On connection error.
		 */
		void receivePausedGameMessage(Player fromPlayer, String msg) throws RpcException;
		
		/**
		 * Server broadcast new turn gameboard.
		 * @param gameBoard New gameboard.
		 * @throws RpcException On connection error.
		 */
		void receiveNewTurnGameBoard(PlayerGameBoard gameBoard) throws RpcException;
	}
}
