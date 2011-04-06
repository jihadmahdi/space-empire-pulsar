/**
 * @author Escallier Pierre
 * @file PlayerGameMove.java
 * @date 2 juin 2009
 */
package org.axan.sep.server.model;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Stack;

import org.axan.eplib.utils.Basic;
import org.axan.sep.common.CommandCheckResult;
import org.axan.sep.common.IGameCommand;
import org.axan.sep.common.IGame.AttackEnemiesFleetParams;
import org.axan.sep.common.IGame.BuildParams;
import org.axan.sep.common.IGame.BuildSpaceRoadParams;
import org.axan.sep.common.IGame.ChangeDiplomacyParams;
import org.axan.sep.common.IGame.DemolishParams;
import org.axan.sep.common.IGame.DemolishSpaceRoadParams;
import org.axan.sep.common.IGame.DismantleFleetParams;
import org.axan.sep.common.IGame.EmbarkGovernmentParams;
import org.axan.sep.common.IGame.FireAntiProbeMissileParams;
import org.axan.sep.common.IGame.FormFleetParams;
import org.axan.sep.common.IGame.LaunchProbeParams;
import org.axan.sep.common.IGame.MakeAntiProbeMissilesParams;
import org.axan.sep.common.IGame.MakeProbesParams;
import org.axan.sep.common.IGame.MakeStarshipsParams;
import org.axan.sep.common.IGame.ModifyCarbonOrderParams;
import org.axan.sep.common.IGame.MoveFleetParams;
import org.axan.sep.common.IGame.SettleGovernmentParams;
import org.axan.sep.common.Protocol.ServerRunningGame.RunningGameCommandException;
import org.axan.sep.server.SEPServer.SEPImplementationException;
import org.axan.sep.server.model.ProductiveCelestialBody.CelestialBodyBuildException;




/**
 * Represent a player move for a specific game board.
 */
public class PlayerGameMove
{
	public static class BuildCommand extends GameMoveCommand<BuildParams>
	{
		public BuildCommand(String playerLogin, BuildParams params)
		{
			super(playerLogin, params);		
		}

		@Override
		protected GameBoard apply(GameBoard originalGameBoard)
		{
			GameBoard newGameBoard = Basic.clone(originalGameBoard);
			try
			{
				newGameBoard.build(playerLogin, params.celestialBodyName, params.buildingType);
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
	
	static class DemolishCommand extends GameMoveCommand<DemolishParams>
	{		
		public DemolishCommand(String playerLogin, DemolishParams params)
		{
			super(playerLogin, params);
		}

		@Override
		protected GameBoard apply(GameBoard originalGameBoard)
		{
			GameBoard newGameBoard = Basic.clone(originalGameBoard);
			try
			{
				newGameBoard.demolish(playerLogin, params.celestialBodyName, params.buildingType);
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
	
	static class FormFleetCommand extends GameMoveCommand<FormFleetParams>
	{
		public FormFleetCommand(String playerLogin, FormFleetParams params)
		{
			super(playerLogin, params);			
		}

		@Override
		protected GameBoard apply(GameBoard originalGameBoard)
		{
			GameBoard newGameBoard = Basic.clone(originalGameBoard);
			try
			{
				newGameBoard.formFleet(playerLogin, params.productiveCelestialBodyName, params.fleetName, params.fleetToFormStarships, params.fleetToFormSpecialUnits);
			}
			catch(RunningGameCommandException e)
			{
				e.printStackTrace();
				return originalGameBoard;
			}
			return newGameBoard;
		}

	}
	
	static class DismantleFleetCommand extends GameMoveCommand<DismantleFleetParams>
	{
		public DismantleFleetCommand(String playerLogin, DismantleFleetParams params)
		{
			super(playerLogin, params);
		}

		@Override
		protected GameBoard apply(GameBoard originalGameBoard)
		{
			GameBoard newGameBoard = Basic.clone(originalGameBoard);
			try
			{
				newGameBoard.dismantleFleet(playerLogin, params.fleetName);
			}
			catch(RunningGameCommandException e)
			{
				e.printStackTrace();
				return originalGameBoard;
			}
			return newGameBoard;
		}

	}
	
	static class MakeStarshipsCommand extends GameMoveCommand<MakeStarshipsParams>
	{
		public MakeStarshipsCommand(String playerLogin, MakeStarshipsParams params)
		{
			super(playerLogin, params);			
		}

		@Override
		protected GameBoard apply(GameBoard originalGameBoard)
		{
			GameBoard newGameBoard = Basic.clone(originalGameBoard);
			try
			{
				newGameBoard.makeStarships(playerLogin, params.planetName, params.starshipsToMake);
			}
			catch(RunningGameCommandException e)
			{
				e.printStackTrace();
				return originalGameBoard;
			}
			return newGameBoard;
		}

	}
	
	static class MakeProbesCommand extends GameMoveCommand<MakeProbesParams>
	{
		public MakeProbesCommand(String playerLogin, MakeProbesParams params)
		{
			super(playerLogin, params);
		}
		
		@Override
		protected GameBoard apply(GameBoard originalGameBoard)
		{
			GameBoard newGameBoard = Basic.clone(originalGameBoard);
			try
			{
				newGameBoard.makeProbes(playerLogin, params.planetName, params.probeName, params.quantity);
			}
			catch(RunningGameCommandException e)
			{
				e.printStackTrace();
				return originalGameBoard;
			}
			return newGameBoard;
		}
	}
	
	static class MakeAntiProbeMissilesCommand extends GameMoveCommand<MakeAntiProbeMissilesParams>
	{
		public MakeAntiProbeMissilesCommand(String playerLogin, MakeAntiProbeMissilesParams params)
		{
			super(playerLogin, params);
		}
		
		@Override
		protected GameBoard apply(GameBoard originalGameBoard)
		{
			GameBoard newGameBoard = Basic.clone(originalGameBoard);
			try
			{
				newGameBoard.makeAntiProbeMissiles(playerLogin, params.planetName, params.antiProbeMissileName, params.quantity);
			}
			catch(RunningGameCommandException e)
			{
				e.printStackTrace();
				return originalGameBoard;
			}
			return newGameBoard;
		}
	}
	
	static class EmbarkGovernmentCommand extends GameMoveCommand<EmbarkGovernmentParams>
	{
		public EmbarkGovernmentCommand(String playerLogin, EmbarkGovernmentParams params)
		{
			super(playerLogin, params);
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
	
	static class MoveFleetCommand extends GameMoveCommand<MoveFleetParams>
	{
		public MoveFleetCommand(String playerLogin, MoveFleetParams params)
		{
			super(playerLogin, params);
		}
		
		@Override
		protected GameBoard apply(GameBoard originalGameBoard)
		{
			GameBoard newGameBoard = Basic.clone(originalGameBoard);
			try
			{
				newGameBoard.moveFleet(playerLogin, params.fleetName, params.checkpoints);
			}
			catch(RunningGameCommandException e)
			{
				e.printStackTrace();
				return originalGameBoard;
			}
			return newGameBoard;
		}
	}
	
	static class SettleGovernmentCommand extends GameMoveCommand<SettleGovernmentParams>
	{
		public SettleGovernmentCommand(String playerLogin, SettleGovernmentParams params)
		{
			super(playerLogin, params);
		}
		
		@Override
		protected GameBoard apply(GameBoard originalGameBoard)
		{
			GameBoard newGameBoard = Basic.clone(originalGameBoard);
			try
			{
				newGameBoard.settleGovernment(playerLogin, params.planetName);
			}
			catch(RunningGameCommandException e)
			{
				e.printStackTrace();
				return originalGameBoard;
			}
			return newGameBoard;
		}
	}
	
	static class LaunchProbeCommand extends GameMoveCommand<LaunchProbeParams>
	{
		public LaunchProbeCommand(String playerLogin, LaunchProbeParams params)
		{
			super(playerLogin, params);
		}
		
		@Override
		protected GameBoard apply(GameBoard originalGameBoard)
		{
			GameBoard newGameBoard = Basic.clone(originalGameBoard);
			try
			{
				newGameBoard.launchProbe(playerLogin, params.probeName, params.destination);
			}
			catch(RunningGameCommandException e)
			{
				e.printStackTrace();
				return originalGameBoard;
			}
			return newGameBoard;
		}
	}
	
	static class FireAntiProbeMissileCommand extends GameMoveCommand<FireAntiProbeMissileParams>
	{
		public FireAntiProbeMissileCommand(String playerLogin, FireAntiProbeMissileParams params)
		{
			super(playerLogin, params);
		}
		
		@Override
		protected GameBoard apply(GameBoard originalGameBoard)
		{
			GameBoard newGameBoard = Basic.clone(originalGameBoard);
			try
			{
				newGameBoard.fireAntiProbeMissile(playerLogin, params.antiProbeMissileName, params.targetOwnerName, params.targetProbeName);
			}
			catch(RunningGameCommandException e)
			{
				e.printStackTrace();
				return originalGameBoard;
			}
			return newGameBoard;
		}
	}
	
	static class ChangeDiplomacyCommand extends GameMoveCommand<ChangeDiplomacyParams>
	{
		public ChangeDiplomacyCommand(String playerLogin, ChangeDiplomacyParams params)
		{
			super(playerLogin, params);
		}
		
		@Override
		protected GameBoard apply(GameBoard originalGameBoard)
		{
			GameBoard newGameBoard = Basic.clone(originalGameBoard);
			try
			{
				newGameBoard.changeDiplomacy(playerLogin, params.newPolicies);
			}
			catch(RunningGameCommandException e)
			{
				e.printStackTrace();
				return originalGameBoard;
			}
			return newGameBoard;
		}
	}
		
	static class AttackEnemiesFleetCommand extends GameMoveCommand<AttackEnemiesFleetParams>
	{
		public AttackEnemiesFleetCommand(String playerLogin, AttackEnemiesFleetParams params)
		{
			super(playerLogin, params);
		}
		
		@Override
		protected GameBoard apply(GameBoard originalGameBoard)
		{
			GameBoard newGameBoard = Basic.clone(originalGameBoard);
			try
			{
				newGameBoard.attackEnemiesFleet(playerLogin, params.celestialBodyName);
			}
			catch(RunningGameCommandException e)
			{
				e.printStackTrace();
				return originalGameBoard;
			}
			return newGameBoard;
		}
	}
		
	static class BuildSpaceRoadCommand extends GameMoveCommand<BuildSpaceRoadParams>
	{
		public BuildSpaceRoadCommand(String playerLogin, BuildSpaceRoadParams params)
		{
			super(playerLogin, params);
		}
		
		@Override
		protected GameBoard apply(GameBoard originalGameBoard)
		{
			GameBoard newGameBoard = Basic.clone(originalGameBoard);
			try
			{
				newGameBoard.buildSpaceRoad(playerLogin, params.productiveCelestialBodyNameA, params.productiveCelestialBodyNameB);
			}
			catch(RunningGameCommandException e)
			{
				e.printStackTrace();
				return originalGameBoard;
			}
			return newGameBoard;
		}
	}
	
	static class DemolishSpaceRoadCommand extends GameMoveCommand<DemolishSpaceRoadParams>
	{
		public DemolishSpaceRoadCommand(String playerLogin, DemolishSpaceRoadParams params)
		{
			super(playerLogin, params);			
		}
		
		@Override
		protected GameBoard apply(GameBoard originalGameBoard)
		{
			GameBoard newGameBoard = Basic.clone(originalGameBoard);
			try
			{
				newGameBoard.demolishSpaceRoad(playerLogin, params.sourceName, params.destinationName);
			}
			catch(RunningGameCommandException e)
			{
				e.printStackTrace();
				return originalGameBoard;
			}
			return newGameBoard;
		}
	}
	
	static class ModifyCarbonOrderCommand extends GameMoveCommand<ModifyCarbonOrderParams>
	{
		public ModifyCarbonOrderCommand(String playerLogin, ModifyCarbonOrderParams params)
		{
			super(playerLogin, params);
		}
		
		@Override
		protected GameBoard apply(GameBoard originalGameBoard)
		{
			GameBoard newGameBoard = Basic.clone(originalGameBoard);
			try
			{
				newGameBoard.modifyCarbonOrder(playerLogin, params.originCelestialBodyName, params.nextCarbonOrders);
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
	
	/*
	public void addBuildCommand(String celestialBodyName, Class<? extends org.axan.sep.common.ABuilding> buildingType) throws RunningGameCommandException
	{
		checkTurnIsNotEnded();
		
		CommandCheckResult result = getGameBoard().canBuild(playerLogin, celestialBodyName, buildingType);
		checkCommandResult(result);
		
		addGameMoveCommand(new BuildCommand(playerLogin, celestialBodyName, buildingType));
	}

    public void addDemolishCommand(String celestialBodyName, Class<? extends org.axan.sep.common.ABuilding> buildingType) throws RunningGameCommandException
	{
		checkTurnIsNotEnded();

		CommandCheckResult result = getGameBoard().canDemolish(playerLogin, celestialBodyName, buildingType);
		checkCommandResult(result);

		addGameMoveCommand(new DemolishCommand(playerLogin, celestialBodyName, buildingType));
	}
	
	public void addFormFleetCommand(String planetName, String fleetName, Map<org.axan.sep.common.StarshipTemplate, Integer> fleetToFormStarships, Set<String> fleetToFormSpecialUnits) throws RunningGameCommandException
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
	
	public void addMakeStarshipsCommand(String planetName, Map<org.axan.sep.common.StarshipTemplate, Integer> starshipsToMake) throws RunningGameCommandException
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
	
	public void addMoveFleetCommand(String fleetName, Stack<org.axan.sep.common.Fleet.Move> checkpoints) throws RunningGameCommandException
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
	
	public void addChangeDiplomacyCommand(Map<String,PlayerPolicies> newPolicies) throws RunningGameCommandException
	{
		checkTurnIsNotEnded();

		CommandCheckResult result = getGameBoard().canChangeDiplomacy(playerLogin, newPolicies);
		checkCommandResult(result);

		addGameMoveCommand(new ChangeDiplomacyCommand(playerLogin, newPolicies));
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
	*/
	
	private void executeCommand(GameMoveCommand command) throws RunningGameCommandException
	{
		checkTurnIsNotEnded();
		
		Method method;
		CommandCheckResult result;
		try
		{
			String methodName = "can"+command.getClass().getSimpleName();
			if (methodName.endsWith("Command")) methodName = methodName.replaceAll("Command", "");
			method = GameBoard.class.getMethod(methodName, String.class, command.getParams().getClass());
			result = (CommandCheckResult) method.invoke(getGameBoard(), command.playerLogin, command.getParams());
		}
		catch(Throwable t)
		{
			throw new RunningGameCommandException(t);
		}
		
		checkCommandResult(result);
		
		currentGameBoard = command.apply(getGameBoard());
		commands.push(command);
	}

	public void resetTurn() throws RunningGameCommandException
	{
		checkTurnIsNotEnded();
		
		commands.removeAllElements();
	}

	public void endTurn(List<IGameCommand> commands) throws SEPImplementationException, RunningGameCommandException
	{
		GameBoard savedGameBoard = getGameBoard();
		
		for(IGameCommand command : commands)
		{
			if (command == null) continue;
			
			GameMoveCommand<?> gameMoveCommand;
			
			String serverClassName = PlayerGameMove.class.getCanonicalName()+"$"+command.getClass().getSimpleName()+"Command";
			try
			{
				Class<? extends GameMoveCommand<?>> serverCommandClass = (Class<? extends GameMoveCommand<Object>>) Class.forName(serverClassName);
				Constructor<? extends GameMoveCommand<?>> serverCommandClassConstructor;
				serverCommandClassConstructor = serverCommandClass.getConstructor(String.class, command.getParams().getClass());
				gameMoveCommand = serverCommandClassConstructor.newInstance(playerLogin, command.getParams());
			}
			catch(Throwable t)
			{
				throw new SEPImplementationException("Client command '"+command.getClass().getSimpleName()+"' cannot match server expected class '"+serverClassName+"'", t);
			}
			
			try
			{
				executeCommand(gameMoveCommand);
			}
			catch(Throwable t)
			{				
				this.commands.removeAll(commands);
				currentGameBoard = savedGameBoard;
				
				if (RunningGameCommandException.class.isInstance(t))
				{
					throw RunningGameCommandException.class.cast(t);
				}
				else
				{
					throw new RunningGameCommandException(t);
				}
			}
		}
		
		isTurnEnded = true;
	}

	public boolean isTurnEnded()
	{
		return isTurnEnded;
	}		
			
}
