/**
 * @author Escallier Pierre
 * @file PlayerGameMove.java
 * @date 2 juin 2009
 */
package server.model;

import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.axan.eplib.clientserver.rpc.RpcException;
import org.axan.eplib.statemachine.StateMachine.StateMachineNotExpectedEventException;
import org.axan.eplib.utils.Basic;

import server.model.ProductiveCelestialBody.CelestialBodyBuildException;

import client.gui.RunningGamePanel;

import common.CarbonOrder;
import common.CommandCheckResult;
import common.Diplomacy;
import common.IBuilding;
import common.Player;
import common.PlayerGameBoard;
import common.Protocol;
import common.Protocol.ServerRunningGame.RunningGameCommandException;
import common.SEPUtils.RealLocation;

/**
 * Represent a player move for a specific game board.
 */
public class PlayerGameMove
{
	static class BuildCommand extends GameMoveCommand
	{

		private final String celestialBodyName;
		private final Class<? extends common.IBuilding> buildingType;
		
		public BuildCommand(String playerLogin, String celestialBodyName, Class<? extends common.IBuilding> buildingType)
		{
			super(playerLogin);
			this.celestialBodyName = celestialBodyName;
			this.buildingType = buildingType;
		}

		@Override
		protected GameBoard apply(GameBoard originalGameBoard)
		{
			GameBoard newGameBoard = Basic.clone(originalGameBoard);
			try
			{
				newGameBoard.build(playerLogin, celestialBodyName, buildingType);
			}
			catch(CelestialBodyBuildException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
				return originalGameBoard;
			}
			return newGameBoard;
		}

	}
	
	static class DemolishCommand extends GameMoveCommand
	{

		private final String celestialBodyName;
		private final Class<? extends common.IBuilding> buildingType;

		public DemolishCommand(String playerLogin, String celestialBodyName, Class<? extends common.IBuilding> buildingType)
		{
			super(playerLogin);
			this.celestialBodyName = celestialBodyName;
			this.buildingType = buildingType;
		}

		@Override
		protected GameBoard apply(GameBoard originalGameBoard)
		{
			GameBoard newGameBoard = Basic.clone(originalGameBoard);
			try
			{
				newGameBoard.demolish(playerLogin, celestialBodyName, buildingType);
			}
			catch(RunningGameCommandException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
				return originalGameBoard;
			}
			return newGameBoard;
		}

	}
	
	static class FormFleetCommand extends GameMoveCommand
	{
		private final String planetName;
		private final String fleetName;
		private final Map<common.StarshipTemplate, Integer> fleetToFormStarships;
		private final Set<common.ISpecialUnit> fleetToFormSpecialUnits;
		
		public FormFleetCommand(String playerLogin, String planetName, String fleetName, Map<common.StarshipTemplate, Integer> fleetToFormStarships, Set<common.ISpecialUnit> fleetToFormSpecialUnits)
		{
			super(playerLogin);
			this.planetName = planetName;
			this.fleetName = fleetName;
			this.fleetToFormStarships = fleetToFormStarships;
			this.fleetToFormSpecialUnits = fleetToFormSpecialUnits;
		}

		@Override
		protected GameBoard apply(GameBoard originalGameBoard)
		{
			GameBoard newGameBoard = Basic.clone(originalGameBoard);
			try
			{
				newGameBoard.formFleet(playerLogin, planetName, fleetName, fleetToFormStarships, fleetToFormSpecialUnits);
			}
			catch(RunningGameCommandException e)
			{
				e.printStackTrace();
				return originalGameBoard;
			}
			return newGameBoard;
		}

	}
	
	static class DismantleFleetCommand extends GameMoveCommand
	{
		private final String fleetName;
		
		public DismantleFleetCommand(String playerLogin, String fleetName)
		{
			super(playerLogin);
			this.fleetName = fleetName;
		}

		@Override
		protected GameBoard apply(GameBoard originalGameBoard)
		{
			GameBoard newGameBoard = Basic.clone(originalGameBoard);
			try
			{
				newGameBoard.dismantleFleet(playerLogin, fleetName);
			}
			catch(RunningGameCommandException e)
			{
				e.printStackTrace();
				return originalGameBoard;
			}
			return newGameBoard;
		}

	}
	
	static class MakeStarshipsCommand extends GameMoveCommand
	{
		private final String planetName;
		private final Map<common.StarshipTemplate, Integer> starshipsToMake;
		
		public MakeStarshipsCommand(String playerLogin, String planetName, Map<common.StarshipTemplate, Integer> starshipsToMake)
		{
			super(playerLogin);
			this.planetName = planetName;
			this.starshipsToMake = starshipsToMake;
		}

		@Override
		protected GameBoard apply(GameBoard originalGameBoard)
		{
			GameBoard newGameBoard = Basic.clone(originalGameBoard);
			try
			{
				newGameBoard.makeStarships(playerLogin, planetName, starshipsToMake);
			}
			catch(RunningGameCommandException e)
			{
				e.printStackTrace();
				return originalGameBoard;
			}
			return newGameBoard;
		}

	}
	
	static class MakeProbesCommand extends GameMoveCommand
	{
		private final String planetName;
		private final String probeName;
		private final int quantity;
		
		public MakeProbesCommand(String playerLogin, String planetName, String probeName, int quantity)
		{
			super(playerLogin);
			this.planetName = planetName;
			this.probeName = probeName;
			this.quantity = quantity;
		}
		
		@Override
		protected GameBoard apply(GameBoard originalGameBoard)
		{
			GameBoard newGameBoard = Basic.clone(originalGameBoard);
			try
			{
				newGameBoard.makeProbes(playerLogin, planetName, probeName, quantity);
			}
			catch(RunningGameCommandException e)
			{
				e.printStackTrace();
				return originalGameBoard;
			}
			return newGameBoard;
		}
	}
	
	static class MakeAntiProbeMissilesCommand extends GameMoveCommand
	{
		private final String planetName;
		private final String antiProbeMissileName;
		private final int quantity;
		
		public MakeAntiProbeMissilesCommand(String playerLogin, String planetName, String antiProbeMissileName, int quantity)
		{
			super(playerLogin);
			this.planetName = planetName;
			this.antiProbeMissileName = antiProbeMissileName;
			this.quantity = quantity;
		}
		
		@Override
		protected GameBoard apply(GameBoard originalGameBoard)
		{
			GameBoard newGameBoard = Basic.clone(originalGameBoard);
			try
			{
				newGameBoard.makeAntiProbeMissiles(playerLogin, planetName, antiProbeMissileName, quantity);
			}
			catch(RunningGameCommandException e)
			{
				e.printStackTrace();
				return originalGameBoard;
			}
			return newGameBoard;
		}
	}
	
	static class EmbarkGovernmentCommand extends GameMoveCommand
	{
		public EmbarkGovernmentCommand(String playerLogin)
		{
			super(playerLogin);
		}

		@Override
		protected GameBoard apply(GameBoard originalGameBoard)
		{
			GameBoard newGameBoard = Basic.clone(originalGameBoard);
			try
			{
				newGameBoard.embarkGovernment(playerLogin);
			}
			catch(RunningGameCommandException e)
			{
				e.printStackTrace();
				return originalGameBoard;
			}
			return newGameBoard;
		}

	}
	
	static class MoveFleetCommand extends GameMoveCommand
	{
		private final String fleetName;
		private final Stack<common.Fleet.Move> checkpoints;
		
		public MoveFleetCommand(String playerLogin, String fleetName, Stack<common.Fleet.Move> checkpoints)
		{
			super(playerLogin);
			this.fleetName = fleetName;
			this.checkpoints = checkpoints;
		}
		
		@Override
		protected GameBoard apply(GameBoard originalGameBoard)
		{
			GameBoard newGameBoard = Basic.clone(originalGameBoard);
			try
			{
				newGameBoard.moveFleet(playerLogin, fleetName, checkpoints);
			}
			catch(RunningGameCommandException e)
			{
				e.printStackTrace();
				return originalGameBoard;
			}
			return newGameBoard;
		}
	}
	
	static class SettleGovernmentCommand extends GameMoveCommand
	{
		private final String planetName;
		
		public SettleGovernmentCommand(String playerLogin, String planetName)
		{
			super(playerLogin);
			this.planetName = planetName;
		}
		
		@Override
		protected GameBoard apply(GameBoard originalGameBoard)
		{
			GameBoard newGameBoard = Basic.clone(originalGameBoard);
			try
			{
				newGameBoard.settleGovernment(playerLogin, planetName);
			}
			catch(RunningGameCommandException e)
			{
				e.printStackTrace();
				return originalGameBoard;
			}
			return newGameBoard;
		}
	}
	
	static class LaunchProbeCommand extends GameMoveCommand
	{
		private final String probeName;
		private final RealLocation destination;
		
		public LaunchProbeCommand(String playerLogin, String probeName, RealLocation destination)
		{
			super(playerLogin);
			this.probeName = probeName;
			this.destination = destination;
		}
		
		@Override
		protected GameBoard apply(GameBoard originalGameBoard)
		{
			GameBoard newGameBoard = Basic.clone(originalGameBoard);
			try
			{
				newGameBoard.launchProbe(playerLogin, probeName, destination);
			}
			catch(RunningGameCommandException e)
			{
				e.printStackTrace();
				return originalGameBoard;
			}
			return newGameBoard;
		}
	}
	
	static class FireAntiProbeMissileCommand extends GameMoveCommand
	{
		private final String antiProbeMissileName;
		private final String targetProbeName;
		private final String targetOwnerName;
		
		public FireAntiProbeMissileCommand(String playerLogin, String antiProbeMissileName, String targetOwnerName, String targetProbeName)
		{
			super(playerLogin);
			this.antiProbeMissileName = antiProbeMissileName;
			this.targetProbeName = targetProbeName;
			this.targetOwnerName = targetOwnerName;
		}
		
		@Override
		protected GameBoard apply(GameBoard originalGameBoard)
		{
			GameBoard newGameBoard = Basic.clone(originalGameBoard);
			try
			{
				newGameBoard.fireAntiProbeMissile(playerLogin, antiProbeMissileName, targetOwnerName, targetProbeName);
			}
			catch(RunningGameCommandException e)
			{
				e.printStackTrace();
				return originalGameBoard;
			}
			return newGameBoard;
		}
	}
	
	static class ChangeDiplomacyCommand extends GameMoveCommand
	{
		private final common.Diplomacy newDiplomacy;
		
		public ChangeDiplomacyCommand(String playerLogin, common.Diplomacy newDiplomacy)
		{
			super(playerLogin);
			this.newDiplomacy = newDiplomacy;
		}
		
		@Override
		protected GameBoard apply(GameBoard originalGameBoard)
		{
			GameBoard newGameBoard = Basic.clone(originalGameBoard);
			try
			{
				newGameBoard.changeDiplomacy(playerLogin, newDiplomacy);
			}
			catch(RunningGameCommandException e)
			{
				e.printStackTrace();
				return originalGameBoard;
			}
			return newGameBoard;
		}
	}
		
	static class AttackEnemiesFleetCommand extends GameMoveCommand
	{
		private final String celestialBodyName;
		
		public AttackEnemiesFleetCommand(String playerLogin, String celestialBodyName)
		{
			super(playerLogin);
			this.celestialBodyName = celestialBodyName;
		}
		
		@Override
		protected GameBoard apply(GameBoard originalGameBoard)
		{
			GameBoard newGameBoard = Basic.clone(originalGameBoard);
			try
			{
				newGameBoard.attackEnemiesFleet(playerLogin, celestialBodyName);
			}
			catch(RunningGameCommandException e)
			{
				e.printStackTrace();
				return originalGameBoard;
			}
			return newGameBoard;
		}
	}
		
	static class BuildSpaceRoadCommand extends GameMoveCommand
	{
		private final String productiveCelestialBodyNameA;
		private final String productiveCelestialBodyNameB;
		
		public BuildSpaceRoadCommand(String playerLogin, String productiveCelestialBodyNameA, String productiveCelestialBodyNameB)
		{
			super(playerLogin);
			this.productiveCelestialBodyNameA = productiveCelestialBodyNameA;
			this.productiveCelestialBodyNameB = productiveCelestialBodyNameB;
		}
		
		@Override
		protected GameBoard apply(GameBoard originalGameBoard)
		{
			GameBoard newGameBoard = Basic.clone(originalGameBoard);
			try
			{
				newGameBoard.buildSpaceRoad(playerLogin, productiveCelestialBodyNameA, productiveCelestialBodyNameB);
			}
			catch(RunningGameCommandException e)
			{
				e.printStackTrace();
				return originalGameBoard;
			}
			return newGameBoard;
		}
	}
	
	static class DemolishSpaceRoadCommand extends GameMoveCommand
	{
		private final String productiveCelestialBodyNameA;
		private final String productiveCelestialBodyNameB;
		
		public DemolishSpaceRoadCommand(String playerLogin, String productiveCelestialBodyNameA, String productiveCelestialBodyNameB)
		{
			super(playerLogin);
			this.productiveCelestialBodyNameA = productiveCelestialBodyNameA;
			this.productiveCelestialBodyNameB = productiveCelestialBodyNameB;
		}
		
		@Override
		protected GameBoard apply(GameBoard originalGameBoard)
		{
			GameBoard newGameBoard = Basic.clone(originalGameBoard);
			try
			{
				newGameBoard.demolishSpaceRoad(playerLogin, productiveCelestialBodyNameA, productiveCelestialBodyNameB);
			}
			catch(RunningGameCommandException e)
			{
				e.printStackTrace();
				return originalGameBoard;
			}
			return newGameBoard;
		}
	}
	
	static class ModifyCarbonOrder extends GameMoveCommand
	{
		private final String originCelestialBodyName;
		private final Stack<CarbonOrder> nextCarbonOrders;
		
		public ModifyCarbonOrder(String playerLogin, String originCelestialBodyName, Stack<CarbonOrder> nextCarbonOrders)
		{
			super(playerLogin);
			this.originCelestialBodyName = originCelestialBodyName;
			this.nextCarbonOrders = nextCarbonOrders;
		}
		
		@Override
		protected GameBoard apply(GameBoard originalGameBoard)
		{
			GameBoard newGameBoard = Basic.clone(originalGameBoard);
			try
			{
				newGameBoard.modifyCarbonOrder(playerLogin, originCelestialBodyName, nextCarbonOrders);
			}
			catch(RunningGameCommandException e)
			{
				e.printStackTrace();
				return originalGameBoard;
			}
			return newGameBoard;
		}
	}
	
	//////////////////
	
	private final GameBoard originalGameBoard;
	private final Stack<GameMoveCommand>	commands = new Stack<GameMoveCommand>();
	private GameBoard currentGameBoard;
	private final String playerLogin;
	private boolean isTurnEnded;
	
	public PlayerGameMove(GameBoard originalGameBoard, String playerLogin)
	{
		this.originalGameBoard = originalGameBoard;
		this.playerLogin = playerLogin;
		this.isTurnEnded = false;
	}

	public GameBoard getGameBoard()
	{
		if (commands.isEmpty())
		{
			return originalGameBoard;
		}
		else
		{
			return currentGameBoard;
		}
	}

	public Stack<GameMoveCommand> getCommands()
	{
		return commands;
	}
	
	private void checkTurnIsNotEnded() throws RunningGameCommandException
	{
		if (isTurnEnded)
		{
			throw new RunningGameCommandException("Turn is already ended.");
		}
	}
	
	private void checkCommandResult(CommandCheckResult result) throws RunningGameCommandException
	{
		if (!result.isPossible())
		{
			throw new RunningGameCommandException(result.getReason());
		}
	}
	
	public void addBuildCommand(String celestialBodyName, Class<? extends IBuilding> buildingType) throws RunningGameCommandException
	{
		checkTurnIsNotEnded();
		
		CommandCheckResult result = getGameBoard().canBuild(playerLogin, celestialBodyName, buildingType);
		checkCommandResult(result);
		
		addGameMoveCommand(new BuildCommand(playerLogin, celestialBodyName, buildingType));
	}

    public void addDemolishCommand(String celestialBodyName, Class<? extends IBuilding> buildingType) throws RunningGameCommandException
	{
		checkTurnIsNotEnded();

		CommandCheckResult result = getGameBoard().canDemolish(playerLogin, celestialBodyName, buildingType);
		checkCommandResult(result);

		addGameMoveCommand(new DemolishCommand(playerLogin, celestialBodyName, buildingType));
	}
	
	public void addFormFleetCommand(String planetName, String fleetName, Map<common.StarshipTemplate, Integer> fleetToFormStarships, Set<common.ISpecialUnit> fleetToFormSpecialUnits) throws RunningGameCommandException
	{
		checkTurnIsNotEnded();
		
		CommandCheckResult result = getGameBoard().canFormFleet(playerLogin, planetName, fleetName, fleetToFormStarships, fleetToFormSpecialUnits);
		checkCommandResult(result);
		
		addGameMoveCommand(new FormFleetCommand(playerLogin, planetName, fleetName, fleetToFormStarships, fleetToFormSpecialUnits));
	}
	
	public void addDismantleFleetCommand(String fleetName) throws RunningGameCommandException
	{
		checkTurnIsNotEnded();
		
		CommandCheckResult result = getGameBoard().canDismantleFleet(playerLogin, fleetName);
		checkCommandResult(result);
		
		addGameMoveCommand(new DismantleFleetCommand(playerLogin, fleetName));
	}
	
	public void addMakeStarshipsCommand(String planetName, Map<common.StarshipTemplate, Integer> starshipsToMake) throws RunningGameCommandException
	{
		checkTurnIsNotEnded();
		
		CommandCheckResult result = getGameBoard().canMakeStarships(playerLogin, planetName, starshipsToMake);
		checkCommandResult(result);
		
		addGameMoveCommand(new MakeStarshipsCommand(playerLogin, planetName, starshipsToMake));
	}
	
	public void addMakeProbesCommand(String planetName, String probeName, int quantity) throws RunningGameCommandException
	{
		checkTurnIsNotEnded();
		
		CommandCheckResult result = getGameBoard().canMakeProbes(playerLogin, planetName, probeName, quantity);
		checkCommandResult(result);
		
		addGameMoveCommand(new MakeProbesCommand(playerLogin, planetName, probeName, quantity));
	}
	
	public void addMakeAntiProbeMissilesCommand(String planetName, String antiProbeMissileName, int quantity) throws RunningGameCommandException
	{
		checkTurnIsNotEnded();
		
		CommandCheckResult result = getGameBoard().canMakeAntiProbeMissiles(playerLogin, planetName, antiProbeMissileName, quantity);
		checkCommandResult(result);
		
		addGameMoveCommand(new MakeAntiProbeMissilesCommand(playerLogin, planetName, antiProbeMissileName, quantity));
	}
	
	public void addLaunchProbeCommand(String probeName, RealLocation destination) throws RunningGameCommandException
	{
		checkTurnIsNotEnded();
			
		CommandCheckResult result = getGameBoard().canLaunchProbe(playerLogin, probeName, destination);
		checkCommandResult(result);
		
		addGameMoveCommand(new LaunchProbeCommand(playerLogin, probeName, destination));
	}
	
	public void addFireAntiProbeMissileCommand(String antiProbeMissileName, String targetOwnerName, String targetProbeName) throws RunningGameCommandException
	{
		checkTurnIsNotEnded();
		
		CommandCheckResult result = getGameBoard().canFireAntiProbeMissile(playerLogin, antiProbeMissileName, targetOwnerName, targetProbeName);
		checkCommandResult(result);
		
		addGameMoveCommand(new FireAntiProbeMissileCommand(playerLogin, antiProbeMissileName, targetOwnerName, targetProbeName));
	}
	
	public void addEmbarkGovernmentCommand(String playerLogin) throws RunningGameCommandException
	{
		checkTurnIsNotEnded();
		
		CommandCheckResult result = getGameBoard().canEmbarkGovernment(playerLogin);
		checkCommandResult(result);
		
		addGameMoveCommand(new EmbarkGovernmentCommand(playerLogin));		
	}
	
	public void addMoveFleetCommand(String fleetName, Stack<common.Fleet.Move> checkpoints) throws RunningGameCommandException
	{
		checkTurnIsNotEnded();
		
		CommandCheckResult result = getGameBoard().canMoveFleet(playerLogin, fleetName, checkpoints);
		checkCommandResult(result);
		
		addGameMoveCommand(new MoveFleetCommand(playerLogin, fleetName, checkpoints));
	}
	
	public void addSettleGovernmentCommand(String planetName) throws RunningGameCommandException
	{
		checkTurnIsNotEnded();
		
		CommandCheckResult result = getGameBoard().canSettleGovernment(playerLogin, planetName);
		checkCommandResult(result);
		
		addGameMoveCommand(new SettleGovernmentCommand(playerLogin, planetName));
	}
	
	public void addChangeDiplomacyCommand(Diplomacy newDiplomacy) throws RunningGameCommandException
	{
		checkTurnIsNotEnded();
		
		if (!playerLogin.equals(newDiplomacy.getOwnerName()))
		{
			throw new RunningGameCommandException(playerLogin+" sent a diplomacy plan with wrong signature '"+newDiplomacy.getOwnerName()+"'.");
		}
		
		CommandCheckResult result = getGameBoard().canChangeDiplomacy(playerLogin, newDiplomacy);
		checkCommandResult(result);
				
		addGameMoveCommand(new ChangeDiplomacyCommand(playerLogin, newDiplomacy));
	}
	
	public void addAttackEnemiesFleetCommand(String celestialBodyName) throws RunningGameCommandException
	{
		checkTurnIsNotEnded();
		
		CommandCheckResult result = getGameBoard().canAttackEnemiesFleet(playerLogin, celestialBodyName);
		checkCommandResult(result);
		
		addGameMoveCommand(new AttackEnemiesFleetCommand(playerLogin, celestialBodyName));
	}
	
	public void addBuildSpaceRoadCommand(String sourceName, String destinationName) throws RunningGameCommandException
	{
		checkTurnIsNotEnded();
		
		CommandCheckResult result = getGameBoard().canBuildSpaceRoad(playerLogin, sourceName, destinationName);
		checkCommandResult(result);
		
		addGameMoveCommand(new BuildSpaceRoadCommand(playerLogin, sourceName, destinationName));
	}
	
	public void addDemolishSpaceRoadCommand(String sourceName, String destinationName) throws RunningGameCommandException
	{
		checkTurnIsNotEnded();
		
		CommandCheckResult result = getGameBoard().canDemolishSpaceRoad(playerLogin, sourceName, destinationName);
		checkCommandResult(result);
		
		addGameMoveCommand(new DemolishSpaceRoadCommand(playerLogin, sourceName, destinationName));
	}
	
	public void addModifyCarbonOrderCommand(String originCelestialBodyName, Stack<CarbonOrder> nextCarbonOrders) throws RunningGameCommandException
	{
		checkTurnIsNotEnded();
		
		CommandCheckResult result = getGameBoard().canModifyCarbonOrder(playerLogin, originCelestialBodyName, nextCarbonOrders);
		checkCommandResult(result);
		
		addGameMoveCommand(new ModifyCarbonOrder(playerLogin, originCelestialBodyName, nextCarbonOrders));
	}
	
	private void addGameMoveCommand(GameMoveCommand command) throws RunningGameCommandException
	{
		checkTurnIsNotEnded();
		
		currentGameBoard = command.apply(getGameBoard());
		commands.push(command);
	}

	public void resetTurn() throws RunningGameCommandException
	{
		checkTurnIsNotEnded();
		
		commands.removeAllElements();
	}

	public void endTurn()
	{
		isTurnEnded = true;
	}

	public boolean isTurnEnded()
	{
		return isTurnEnded;
	}		
			
}
