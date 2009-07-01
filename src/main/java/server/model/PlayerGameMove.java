/**
 * @author Escallier Pierre
 * @file PlayerGameMove.java
 * @date 2 juin 2009
 */
package server.model;

import java.util.Map;
import java.util.Stack;

import org.axan.eplib.utils.Basic;

import server.model.ProductiveCelestialBody.CelestialBodyBuildException;

import client.gui.RunningGamePanel;

import common.IBuilding;
import common.IStarship;
import common.Player;
import common.PlayerGameBoard;
import common.Protocol;
import common.Protocol.ServerRunningGame.RunningGameCommandException;

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
		private final Map<Class<? extends IStarship>, Integer> fleetToForm;
		
		public FormFleetCommand(String playerLogin, String planetName, String fleetName, Map<Class<? extends IStarship>, Integer> fleetToForm)
		{
			super(playerLogin);
			this.planetName = planetName;
			this.fleetName = fleetName;
			this.fleetToForm = fleetToForm;
		}

		@Override
		protected GameBoard apply(GameBoard originalGameBoard)
		{
			GameBoard newGameBoard = Basic.clone(originalGameBoard);
			try
			{
				newGameBoard.formFleet(playerLogin, planetName, fleetName, fleetToForm);
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
		private final Map<Class<? extends IStarship>, Integer> starshipsToMake;
		
		public MakeStarshipsCommand(String playerLogin, String planetName, Map<Class<? extends IStarship>, Integer> starshipsToMake)
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
	
	public void addBuildCommand(String celestialBodyName, Class<? extends IBuilding> buildingType) throws RunningGameCommandException
	{
		checkTurnIsNotEnded();
		
		if (!getGameBoard().canBuild(playerLogin, celestialBodyName, buildingType))
		{
			throw new RunningGameCommandException(playerLogin+" cannot build '"+buildingType.getSimpleName()+"' on "+celestialBodyName);
		}
		
		addGameMoveCommand(new BuildCommand(playerLogin, celestialBodyName, buildingType));
	}

    public void addDemolishCommand(String celestialBodyName, Class<? extends IBuilding> buildingType) throws RunningGameCommandException
	{
		checkTurnIsNotEnded();

		if (!getGameBoard().canDemolish(playerLogin, celestialBodyName, buildingType))
		{
			throw new RunningGameCommandException(playerLogin+" cannot demolish '"+buildingType.getSimpleName()+"' on "+celestialBodyName);
		}

		addGameMoveCommand(new DemolishCommand(playerLogin, celestialBodyName, buildingType));
	}
	
	public void addFormFleetCommand(String planetName, String fleetName, Map<Class<? extends IStarship>, Integer> fleetToForm) throws RunningGameCommandException
	{
		checkTurnIsNotEnded();
		
		if (!getGameBoard().canFormFleet(playerLogin, planetName, fleetName, fleetToForm))
		{
			throw new RunningGameCommandException(playerLogin+" cannot form fleet '"+fleetName+"' on "+planetName);
		}
		
		addGameMoveCommand(new FormFleetCommand(playerLogin, planetName, fleetName, fleetToForm));
	}
	
	public void addDismantleFleetCommand(String fleetName) throws RunningGameCommandException
	{
		checkTurnIsNotEnded();
		
		if (!getGameBoard().canDismantleFleet(playerLogin, fleetName))
		{
			throw new RunningGameCommandException(playerLogin+" cannot dismantle fleet '"+fleetName+"'");			
		}
		
		addGameMoveCommand(new DismantleFleetCommand(playerLogin, fleetName));
	}
	
	public void addMakeStarshipsCommand(String planetName, Map<Class<? extends IStarship>, Integer> starshipsToMake) throws RunningGameCommandException
	{
		checkTurnIsNotEnded();
		
		if (!getGameBoard().canMakeStarships(playerLogin, planetName, starshipsToMake))
		{
			throw new RunningGameCommandException(playerLogin+" cannot make these starships on "+planetName);
		}
		
		addGameMoveCommand(new MakeStarshipsCommand(playerLogin, planetName, starshipsToMake));
	}
	
	public void addMakeProbesCommand(String planetName, String probeName, int quantity) throws RunningGameCommandException
	{
		checkTurnIsNotEnded();
		
		if (!getGameBoard().canMakeProbes(playerLogin, planetName, probeName, quantity))
		{
			throw new RunningGameCommandException(playerLogin+" cannot make "+quantity+" probes named '"+probeName+"' on '"+planetName+"'");
		}
		
		addGameMoveCommand(new MakeProbesCommand(playerLogin, planetName, probeName, quantity));
	}
	
	public void addMakeAntiProbeMissilesCommand(String planetName, String antiProbeMissileName, int quantity) throws RunningGameCommandException
	{
		checkTurnIsNotEnded();
		
		if (!getGameBoard().canMakeAntiProbeMissiles(playerLogin, planetName, antiProbeMissileName, quantity))
		{
			throw new RunningGameCommandException(playerLogin+" cannot make "+quantity+" anti-probe missiles named '"+antiProbeMissileName+"' on '"+planetName+"'");
		}
		
		addGameMoveCommand(new MakeAntiProbeMissilesCommand(playerLogin, planetName, antiProbeMissileName, quantity));
	}
	
	public void addEmbarkGovernmentCommand(String playerLogin) throws RunningGameCommandException
	{
		checkTurnIsNotEnded();
		
		if (!getGameBoard().canEmbarkGovernment(playerLogin))
		{
			throw new RunningGameCommandException(playerLogin+" cannot embark the government.");
		}
		
		addGameMoveCommand(new EmbarkGovernmentCommand(playerLogin));		
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
