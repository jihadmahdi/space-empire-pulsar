package org.axan.sep.server.model;

import org.axan.eplib.utils.Basic;
import org.axan.sep.common.IGame.BuildParams;
import org.axan.sep.common.Protocol.ServerRunningGame.RunningGameCommandException;


/**
 * Represent a game move command (ie: build, embark, ...).
 */
public abstract class GameMoveCommand<P>
{
	protected String playerLogin;
	protected final P params;
	
	public GameMoveCommand(String playerLogin, P params)
	{
		this.playerLogin = playerLogin;
		this.params = params;
	}
	
	public P getParams()
	{
		return params;
	}

	abstract protected GameBoard apply(GameBoard originalGameBoard) throws RunningGameCommandException;
	
	/////////////
	
	public static class BuildCommand extends GameMoveCommand<BuildParams>
	{
		public BuildCommand(String playerLogin, BuildParams params)
		{
			super(playerLogin, params);		
		}

		@Override
		protected GameBoard apply(GameBoard originalGameBoard) throws RunningGameCommandException
		{
			GameBoard newGameBoard = Basic.clone(originalGameBoard);
			try
			{
				newGameBoard.build(playerLogin, params.celestialBodyName, params.buildingType);
			}
			catch(Exception e)
			{
				throw new RunningGameCommandException(e);
			}
			/*
			try
			{
				newGameBoard.build(playerLogin, params.celestialBodyName, params.buildingType);
			}
			catch(CelestialBodyBuildException e)
			{
				e.printStackTrace();
				return originalGameBoard;
			}
			*/
			return newGameBoard;
		}

	}
	
	/*
	
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
	*/
}
