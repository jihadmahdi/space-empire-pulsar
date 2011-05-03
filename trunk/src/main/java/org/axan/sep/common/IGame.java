package org.axan.sep.common;

import java.io.Serializable;
import java.util.List;

import org.axan.sep.common.IGameCommand.GameCommandException;
import org.axan.sep.common.Protocol.eBuildingType;

public interface IGame
{
	
	/**
	 * Add a command to the current gameBoard and execute it to preview the next gameBoard.
	 * @param command Command to execute.
	 * @throws LocalGameCommandException If the command cannot be applied to the current gameBoard state. 
	 */
	public abstract void executeCommand(IGameCommand command) throws GameCommandException;	

	/**
	 * Clear all commands and reset to initial gameBoard.
	 */
	public abstract void resetTurn() throws GameCommandException;
	public abstract AbstractGameCommandCheck canResetTurn();

	/**
	 * Undo last command.
	 * @throws GameCommandException
	 */
	public abstract void undo() throws GameCommandException;
	public abstract AbstractGameCommandCheck canUndo();		

	/**
	 * Re-do last undone command.
	 * @throws GameCommandException
	 */
	public abstract void redo() throws GameCommandException;
	public abstract AbstractGameCommandCheck canRedo();

	/**
	 * Get last gameBoard state (all known commands applied).
	 * @return
	 */
	public abstract PlayerGameBoard getGameBoard();

	/**
	 * Return a list of all applied commands.
	 * @return
	 */
	public abstract List<IGameCommand> getCommands();

	/**
	 * End turn (after turn is ended, the game must reject any command including undo/redo.
	 * @throws GameCommandException
	 */
	public abstract void endTurn() throws GameCommandException;
	public abstract AbstractGameCommandCheck canEndTurn();

///////////////////// Commands
	/*
	///// AttackEnemiesFleet
	
	public static class AttackEnemiesFleetParams implements Serializable
	{
		public final String celestialBodyName;
		
		public AttackEnemiesFleetParams(String celestialBodyName)
		{
			this.celestialBodyName = celestialBodyName;
		}
	}
	
	public static class AttackEnemiesFleetCheck extends AbstractGameCommandCheck
	{
		private ProductiveCelestialBody productiveCelestialBody;
		
		public AttackEnemiesFleetCheck(PlayerGameBoard nextGameBoard)
		{
			super(nextGameBoard);
		}
	}
	
	public static class Test extends AbstractGameCommand<AttackEnemiesFleetParams, AttackEnemiesFleetCheck>
	{

		public Test(AttackEnemiesFleetParams params)
		{
			super(params);
			// TODO Auto-generated constructor stub
		}

		@Override
		protected PlayerGameBoard apply(AttackEnemiesFleetCheck check) throws GameCommandException
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		protected AttackEnemiesFleetCheck check(PlayerGameBoard gameBoard)
		{
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
	public static class AttackEnemiesFleet extends AbstractGameCommand<AttackEnemiesFleetParams, AttackEnemiesFleetCheck> implements Serializable
	{

		public AttackEnemiesFleet(String celestialBodyName)
		{
			super(new AttackEnemiesFleetParams(celestialBodyName));
		}

		@Override
		protected PlayerGameBoard apply(AttackEnemiesFleetCheck check) throws GameCommandException
		{
			check.productiveCelestialBody.setAttackEnemiesFleetFlag(true);
			return check.getGameBoard();
		}

		@Override
		protected AttackEnemiesFleetCheck check(PlayerGameBoard gameBoard)
		{
			AttackEnemiesFleetCheck result = new AttackEnemiesFleetCheck(gameBoard);
			try
			{
				result.productiveCelestialBody = gameBoard.getCelestialBody(params.celestialBodyName, ProductiveCelestialBody.class);
			}
			catch(PlayerGameBoardQueryException e)
			{
				//result.setImpossibilityReason("Celestial body '"+params.celestialBodyName+"' does not exist or is not a productive one.");
				result.setException(e);
				return result;
			}
			
			
			if (result.productiveCelestialBody == null) result.setImpossibilityReason("Celestial body '"+params.celestialBodyName+"' is not a productive celestial body.");
			
			return result;
		}
		
	}
*/
	
	///// Build
	
	public static class BuildParams implements Serializable
	{
		public final String celestialBodyName;
		public final eBuildingType buildingType;
		
		public BuildParams(String ceslestialBodyName, eBuildingType buildingType)
		{
			this.celestialBodyName = ceslestialBodyName;
			this.buildingType = buildingType;
		}
	}
	
/*
	
	public static class BuildCheck extends AbstractGameCommandCheck
	{
		
		ProductiveCelestialBody	productiveCelestialBody;
		ABuilding					newBuilding;
		int						carbonCost;
		int						populationCost;

		public BuildCheck(PlayerGameBoard nextGameBoard)
		{
			super(nextGameBoard);	
		}		
	}
	
	public static class Build extends AbstractGameCommand<BuildParams, BuildCheck> implements Serializable
	{

		public Build(String ceslestialBodyName, eBuildingType buildingType)
		{
			super(new BuildParams(ceslestialBodyName, buildingType));
		}
		
		@Override
		protected PlayerGameBoard apply(BuildCheck check) throws GameCommandException
		{
			try
			{
				check.productiveCelestialBody.updateBuilding(check.newBuilding);
			}
			catch(CelestialBodyBuildException e)
			{
				throw new GameCommandException(e);
			}
			check.productiveCelestialBody.setCarbon(check.productiveCelestialBody.getCarbon() - check.carbonCost);
			
			if (check.populationCost > 0)
			{
				Planet planet = Planet.class.cast(check.productiveCelestialBody);
				planet.setPopulation(planet.getPopulation() - check.populationCost);
			}
			
			check.productiveCelestialBody.setAlreadyBuiltThisTurn(true);
			return check.getGameBoard();
		}

		@Override
		protected BuildCheck check(PlayerGameBoard gameBoard)
		{
			BuildCheck check = new BuildCheck(gameBoard);
			
			try
			{
				check.productiveCelestialBody = gameBoard.getCelestialBody(params.celestialBodyName, ProductiveCelestialBody.class);
			}
			catch(PlayerGameBoardQueryException e)
			{
				check.setException(e);
				return check;
			}
			
			if (check.productiveCelestialBody == null)
			{
				check.setImpossibilityReason("Celestial body '"+params.celestialBodyName+"' does not exist or is not a productive one.");
				return check;
			}
			
			if (!gameBoard.getPlayerName().equals(check.productiveCelestialBody.getOwnerName()))
			{
				check.setImpossibilityReason("Celestial body '"+params.celestialBodyName+"' is not owned by '"+gameBoard.getPlayerName()+"'");
				return check;
			}
			
			// If this productive celestial body build was already used this turn.
			if (check.productiveCelestialBody.hasAlreadyBuiltThisTurn())
			{
				check.setImpossibilityReason("Celestial body '" + params.celestialBodyName + "' already in work for this turn.");
				return check;
			}						

			// If there is no more free slots.
			if (check.productiveCelestialBody.getFreeSlotsCount() < 1)
			{
				check.setImpossibilityReason("No more free slots on celestial body '" + params.celestialBodyName + "'");
				return check;
			}

			// Price check & Celestial body type / building type check
			check.carbonCost = 0;
			check.populationCost = 0;

			ABuilding building = check.productiveCelestialBody.getBuilding(params.buildingType);			
			
			if (building != null)
			{	
				if (!building.canUpgrade())
				{
					check.setImpossibilityReason(building.getClass().getSimpleName()+" cannot be upgraded.");
					return check;
				}
				
				check.newBuilding = building.getUpgraded(gameBoard.getDate());
				check.carbonCost = building.getUpgradeCarbonCost();
				check.populationCost = building.getUpgradePopulationCost();				
			}
			else
			{
				check.carbonCost = Rules.getBuildingUpgradeCarbonCost(params.buildingType, 1); 
				check.populationCost = Rules.getBuildingUpgradePopulationCost(params.buildingType, 1);
				check.newBuilding = ABuilding.getFirstBuild(params.buildingType, gameBoard.getDate());
			}
			
			if (check.carbonCost > check.productiveCelestialBody.getCarbon())
			{
				check.setImpossibilityReason("Not enough carbon ("+check.carbonCost+"C, "+check.populationCost+"P)");
				return check;
			}
			if (check.populationCost > 0)
			{
				if (!Planet.class.isInstance(check.productiveCelestialBody))
				{
					check.setImpossibilityReason("Only planet can afford population costs, '" + params.celestialBodyName + "' is not a planet.");
					return check;
				}
				Planet planet = Planet.class.cast(check.productiveCelestialBody);
				if (check.populationCost > planet.getPopulation())
				{
					check.setImpossibilityReason("Not enough population ("+check.carbonCost+"C, "+check.populationCost+"P)");
					return check;
				}
			}
			
			check.setPrice(check.carbonCost, check.populationCost);
			
			return check;
		}
		
	}
	
	///// Demolish
	
	public static class DemolishParams implements Serializable
	{
		public final String celestialBodyName;
		public final Class<? extends ABuilding> buildingType;
		
		public DemolishParams(String ceslestialBodyName, Class<? extends ABuilding> buildingType)
		{
			this.celestialBodyName = ceslestialBodyName;
			this.buildingType = buildingType;
		}
	}
	
	public static class DemolishCheck extends AbstractGameCommandCheck
	{
		ProductiveCelestialBody	productiveCelestialBody;
		ABuilding				existingBuilding;

		public DemolishCheck(PlayerGameBoard gameBoard)
		{
			super(gameBoard);
		}
	}
	
	public static class Demolish extends AbstractGameCommand<DemolishParams, DemolishCheck> implements Serializable
	{

		public Demolish(String ceslestialBodyName, Class<? extends ABuilding> buildingType)
		{
			super(new DemolishParams(ceslestialBodyName, buildingType));
		}
		
		@Override
		protected PlayerGameBoard apply(DemolishCheck check) throws GameCommandException
		{
			check.productiveCelestialBody.downgradeBuilding(check.existingBuilding);
			return check.getGameBoard();
		}

		@Override
		protected DemolishCheck check(PlayerGameBoard gameBoard)
		{
			DemolishCheck check = new DemolishCheck(gameBoard);
			
			try
			{
				check.productiveCelestialBody = gameBoard.getCelestialBody(params.celestialBodyName, ProductiveCelestialBody.class);
			}
			catch(PlayerGameBoardQueryException e)
			{
				check.setException(e);
				return check;
			}
			
			if (check.productiveCelestialBody == null)
			{
				check.setImpossibilityReason("Celestial body '"+params.celestialBodyName+"' does not exist or is not a productive one.");
				return check;
			}
			
			if (!gameBoard.getPlayerName().equals(check.productiveCelestialBody.getOwnerName()))
			{
				check.setImpossibilityReason("Celestial body '"+params.celestialBodyName+"' is not owned by '"+gameBoard.getPlayerName()+"'");
				return check;
			}
			
			check.existingBuilding = check.productiveCelestialBody.getBuilding(params.buildingType);
			if (check.existingBuilding == null || check.existingBuilding.getBuildSlotsCount() == 0)
			{
				check.setImpossibilityReason("No building type '" + params.buildingType.getSimpleName() + "' built yet.");
				return check;
			}
			
			if (!check.existingBuilding.canDowngrade())
			{
				check.setImpossibilityReason("Cannot demolish building type '" + params.buildingType.getSimpleName() + "'");
				return check;
			}
			
			return check;
		}
		
	}
	
	///// DismantleFleet
	
	public static class DismantleFleetParams implements Serializable
	{
		public final String fleetName;
		
		public DismantleFleetParams(String fleetName)
		{
			this.fleetName = fleetName;
		}
	}
	
	public static class DismantleFleetCheck extends AbstractGameCommandCheck
	{
		ProductiveCelestialBody productiveCelestialBody;
		Fleet fleet;
		Fleet unasignedFleet;
		Area area;
		
		public DismantleFleetCheck(PlayerGameBoard gameBoard)
		{
			super(gameBoard);
		}
	}
	
	public static class DismantleFleet extends AbstractGameCommand<DismantleFleetParams, DismantleFleetCheck> implements Serializable
	{

		public DismantleFleet(String fleetName)
		{
			super(new DismantleFleetParams(fleetName));
		}
		
		@Override
		protected PlayerGameBoard apply(DismantleFleetCheck check) throws GameCommandException
		{
			try
			{
				check.getGameBoard().removeUnit(check.getGameBoard().getPlayerName(), check.fleet.getName(), Fleet.class);
				if (check.unasignedFleet == null)
				{
					check.area.addUnit(new Fleet(true, -1, Fleet.getUnasignedFleetName(check.productiveCelestialBody.getName()), check.getGameBoard().getPlayerName(), null, null, check.fleet.getCurrentLocation(), 0.0, 0.0, check.fleet.getStarships(), check.fleet.getSpecialUnits(), null, null, true));
				}
				else
				{
					check.unasignedFleet.merge(check.fleet.getStarships(), check.fleet.getSpecialUnits());
				}
			}
			catch(PlayerGameBoardQueryException e)
			{
				throw new GameCommandException(e);
			}
			
			return check.getGameBoard();
		}

		@Override
		protected DismantleFleetCheck check(PlayerGameBoard gameBoard)
		{
			DismantleFleetCheck check = new DismantleFleetCheck(gameBoard);						
			
			try
			{
				check.fleet = gameBoard.getUnit(gameBoard.getPlayerName(), params.fleetName, Fleet.class);
			}
			catch(PlayerGameBoardQueryException e)
			{
				check.setException(e);
				return check;
			}
			
			check.area = gameBoard.getArea(check.fleet.getCurrentLocation());
			if (check.area == null)
			{
				check.setImpossibilityReason("Cannot find fleet area.");
				return check;
			}
			
			try
			{
				check.productiveCelestialBody = gameBoard.getCelestialBody(check.fleet.getCurrentLocation(), ProductiveCelestialBody.class);
			}
			catch(PlayerGameBoardQueryException e)
			{
				check.setException(e);
				return check;
			}
			
			if (check.productiveCelestialBody == null)
			{
				check.setImpossibilityReason("Fleet is in travel");
				return check;
			}
			
			try
			{
				check.unasignedFleet = gameBoard.getUnit(gameBoard.getPlayerName(), Fleet.getUnasignedFleetName(check.productiveCelestialBody.getName()), Fleet.class);
			}
			catch(PlayerGameBoardQueryException e)
			{
				check.setException(e);
				return check;
			}
			// (check.unasignedFleet == null) is OK.
			
			return check;
		}
		
	}
	
	///// SettleGovernment
	
	public static class SettleGovernmentParams implements Serializable
	{
		public final String planetName;
		
		public SettleGovernmentParams(String planetName)
		{
			this.planetName = planetName;
		}
	}
	
	public static class SettleGovernmentCheck extends AbstractGameCommandCheck
	{
		Planet planet;
		Fleet governmentalFleet;
		GovernmentModule governmentModule;
		
		public SettleGovernmentCheck(PlayerGameBoard gameBoard)
		{
			super(gameBoard);
		}		
	}
	
	public static class SettleGovernment extends AbstractGameCommand<SettleGovernmentParams, SettleGovernmentCheck> implements Serializable
	{
		public SettleGovernment(String planetName)
		{
			super(new SettleGovernmentParams(planetName));
		}

		@Override
		protected PlayerGameBoard apply(SettleGovernmentCheck check) throws GameCommandException
		{
			check.governmentalFleet.removeGovernment();
			try
			{
				check.planet.updateBuilding(check.governmentModule);
			}
			catch(CelestialBodyBuildException e)
			{
				throw new GameCommandException(e);
			}
			
			return check.getGameBoard();
		}

		@Override
		protected SettleGovernmentCheck check(PlayerGameBoard gameBoard)
		{
			SettleGovernmentCheck check = new SettleGovernmentCheck(gameBoard);
			
			try
			{
				check.planet = gameBoard.getCelestialBody(params.planetName, Planet.class);
			}
			catch(PlayerGameBoardQueryException e)
			{
				check.setException(e);
				return check;
			}
			
			if (check.planet == null)
			{
				check.setImpossibilityReason("Cannot find planet '"+params.planetName+"'");
				return check;
			}
			
			Area a;
			try
			{
				a = gameBoard.getArea(gameBoard.getCelestialBodyLocation(params.planetName));
			}
			catch(PlayerGameBoardQueryException e)
			{
				check.setException(e);
				return check;
			}
			
			if (a == null)
			{
				check.setImpossibilityReason("Cannot find planet '"+params.planetName+"' area.");
				return check;
			}
			
			check.governmentalFleet = gameBoard.getGovernmentalFleet(gameBoard.getPlayerName());			
			
			if (check.governmentalFleet == null)
			{
				check.setImpossibilityReason("Cannot find governmental fleet on planet '"+params.planetName+"'");
				return check;
			}
			
			if (check.planet.getFreeSlotsCount() <= 0)
			{
				check.setImpossibilityReason("No free slot available on '"+params.planetName+"'");
				return check;
			}
			
			check.governmentModule = new GovernmentModule(gameBoard.getDate());
			
			return check;
		}
		
	}
	
	///// FireAntiProbeMissile
	
	public static class FireAntiProbeMissileParams implements Serializable
	{
		public final String antiProbeMissileName;
		public final String targetOwnerName;
		public final String targetProbeName;

		public FireAntiProbeMissileParams(String antiProbeMissileName, String targetOwnerName, String targetProbeName)
		{
			this.antiProbeMissileName = antiProbeMissileName;
			this.targetOwnerName = targetOwnerName;
			this.targetProbeName = targetProbeName;
		}
	}
	
	public static class FireAntiProbeMissileCheck extends AbstractGameCommandCheck
	{
		AntiProbeMissile antiProbeMissile;
		RealLocation destination;
		
		public FireAntiProbeMissileCheck(PlayerGameBoard gameBoard)
		{
			super(gameBoard);
		}
	}
	
	public static class FireAntiProbeMissile extends AbstractGameCommand<FireAntiProbeMissileParams, FireAntiProbeMissileCheck> implements Serializable
	{

		public FireAntiProbeMissile(String antiProbeMissileName, String targetOwnerName, String targetProbeName)
		{
			super(new FireAntiProbeMissileParams(antiProbeMissileName, targetOwnerName, targetProbeName));
		}
		
		@Override
		protected PlayerGameBoard apply(FireAntiProbeMissileCheck check) throws GameCommandException
		{
			check.antiProbeMissile.fire(params.targetOwnerName, params.targetProbeName, check.destination);
			return check.getGameBoard();
		}

		@Override
		protected FireAntiProbeMissileCheck check(PlayerGameBoard gameBoard)
		{
			FireAntiProbeMissileCheck check = new FireAntiProbeMissileCheck(gameBoard);
			
			try
			{
				check.antiProbeMissile = gameBoard.getUnit(gameBoard.getPlayerName(), params.antiProbeMissileName, AntiProbeMissile.class);
			}
			catch(PlayerGameBoardQueryException e)
			{
				check.setException(e);
				return check;
			}
			
			if (check.antiProbeMissile == null)
			{
				check.setImpossibilityReason("Cannot find anti-probe missile '"+params.antiProbeMissileName+"'");
				return check;
			}
			
			if (check.antiProbeMissile.isMoving())
			{
				check.setImpossibilityReason("Anti-probe missile '"+params.antiProbeMissileName+"' has already been fired.");
				return check;
			}
			
			if (check.antiProbeMissile.isFired())
			{
				check.setImpossibilityReason("Anti-probe missile '"+params.antiProbeMissileName+"' is already fired.");
				return check;
			}
			
			Probe targetProbe = null;
			try
			{
				// This includes MarkedUnits
				targetProbe = gameBoard.getUnit(params.targetOwnerName, params.targetProbeName, Probe.class);
			}
			catch(PlayerGameBoardQueryException e)
			{
				check.setException(e);
				return check;
			}
			
			if (targetProbe == null)
			{				
				check.setImpossibilityReason("Cannot find target probe '"+params.targetOwnerName+"@"+params.targetProbeName+"'");
				return check;
			}
			
			if (targetProbe.isMoving())
			{
				check.setImpossibilityReason("Anti-probe missile cannot be fired on moving target '"+params.targetOwnerName+"@"+params.targetProbeName+"'");
				return check;
			}
			
			check.destination = targetProbe.getCurrentLocation();
			if (check.destination == null)
			{
				check.setImpossibilityReason("Anti-probe missile target '"+params.targetOwnerName+"@"+params.targetProbeName+"' cannot be located.");
			}
			
			return check;
		}
		
	}
	
	///// LaunchProbe
	
	public static class LaunchProbeParams implements Serializable
	{
		public final String probeName;
		public final RealLocation destination;
		
		public LaunchProbeParams(String probeName, RealLocation destination)
		{
			this.probeName = probeName;
			this.destination = destination == null ? null : new RealLocation(destination.x, destination.y, destination.z);
		}
	}
	
	public static class LaunchProbeCheck extends AbstractGameCommandCheck
	{
		Probe probe;
		RealLocation destination;
		
		public LaunchProbeCheck(PlayerGameBoard gameBoard)
		{
			super(gameBoard);
		}
	}
	
	public static class LaunchProbe extends AbstractGameCommand<LaunchProbeParams, LaunchProbeCheck> implements Serializable
	{
		public LaunchProbe(String probeName, RealLocation destination)
		{
			super(new LaunchProbeParams(probeName, destination));
		}

		@Override
		protected PlayerGameBoard apply(LaunchProbeCheck check) throws GameCommandException
		{
			check.probe.launch(check.destination);
			return check.getGameBoard();
		}

		@Override
		protected LaunchProbeCheck check(PlayerGameBoard gameBoard)
		{
			LaunchProbeCheck check = new LaunchProbeCheck(gameBoard);
			try
			{
				check.probe = gameBoard.getUnit(gameBoard.getPlayerName(), params.probeName, Probe.class);
			}
			catch(PlayerGameBoardQueryException e)
			{
				check.setException(e);
				return check;
			}
			
			if (check.probe == null)
			{
				check.setImpossibilityReason("Probe '"+params.probeName+"' does not exist.");
				return check;
			}
			
			if (check.probe.isMoving() || check.probe.isLaunched())
			{
				check.setImpossibilityReason("Probe '"+params.probeName+"' has already been launched.");
				return check;
			}
			
			if (check.probe.isDeployed())
			{
				check.setImpossibilityReason("Probe '"+params.probeName+"' is already deployed.");
				return check;
			}
			
			if (!gameBoard.isValidLocation(params.destination))
			{
				check.setImpossibilityReason("Invalid destination '"+params.destination.toString()+"'");
				return check;
			}
			
			if (gameBoard.isTravellingTheSun(check.probe.getCurrentLocation(), params.destination))
			{
				check.setImpossibilityReason("Impossible path : "+check.probe.getCurrentLocation()+" to "+params.destination+", cannot travel the sun.");
				return check;
			}
			
			check.destination = params.destination;
			
			return check;
		}
		
	}
	
	///// EmbarkGovernment
	
	public static class EmbarkGovernmentParams implements Serializable {}
	
	public static class EmbarkGovernmentCheck extends AbstractGameCommandCheck
	{
		Planet planet;
		int carbonCost;
		int populationCost;
		
		Area area;
		RealLocation location;
		Fleet unasignedFleet;
		
		public EmbarkGovernmentCheck(PlayerGameBoard gameBoard)
		{
			super(gameBoard);
		}
	}
	
	public static class EmbarkGovernment extends AbstractGameCommand<EmbarkGovernmentParams, EmbarkGovernmentCheck> implements Serializable
	{

		public EmbarkGovernment()
		{
			super(new EmbarkGovernmentParams());
		}

		@Override
		protected PlayerGameBoard apply(EmbarkGovernmentCheck check) throws GameCommandException
		{
			check.planet.removeBuilding(GovernmentModule.class);
			Set<ISpecialUnit> specialUnitsToMake = new HashSet<ISpecialUnit>();
			specialUnitsToMake.add(new GovernmentStarship(check.getGameBoard().getPlayerName()+" government starship"));
			check.planet.setCarbon(check.planet.getCarbon() - check.carbonCost);
			check.planet.setPopulation(check.planet.getPopulation() - check.populationCost);
			
			if (check.unasignedFleet == null)
			{
				check.area.addUnit(new Fleet(true, -1, Fleet.getUnasignedFleetName(check.planet.getName()), check.getGameBoard().getPlayerName(), null, null, check.location, 0.0, 0.0, null, specialUnitsToMake, null, null, true));
			}
			else
			{
				check.unasignedFleet.merge(null, specialUnitsToMake);
			}						
			
			return check.getGameBoard();
		}

		@Override
		protected EmbarkGovernmentCheck check(PlayerGameBoard gameBoard)
		{
			EmbarkGovernmentCheck check = new EmbarkGovernmentCheck(gameBoard);								
			
			try
			{
				check.planet = gameBoard.locateGovernmentModule(gameBoard.getPlayerName());
			}
			catch(PlayerGameBoardQueryException e)
			{
				check.setException(e);
				return check;
			}
			
			if (check.planet == null)
			{
				check.setImpossibilityReason("Cannot find government module.");
				return check;
			}
			try
			{
				check.location = gameBoard.getCelestialBodyLocation(check.planet.getName());
			}
			catch(PlayerGameBoardQueryException e)
			{
				check.setException(e);
				return check;
			}
			if (check.location == null)
			{
				check.setImpossibilityReason("Cannot locate planet '"+check.planet.getName()+"'");
				return check;
			}
			
			check.area = gameBoard.getArea(check.location);
			if (check.area == null)
			{
				check.setImpossibilityReason("Cannot find fleet area.");
				return check;
			}
			
			try
			{
				check.unasignedFleet = gameBoard.getUnit(gameBoard.getPlayerName(), Fleet.getUnasignedFleetName(check.planet.getName()), Fleet.class);
			}
			catch(PlayerGameBoardQueryException e)
			{
				check.setException(e);
				return check;
			}
			// (check.unasignedFleet == null) is OK.
			
			GovernmentModule governmentModule = check.planet.getBuilding(GovernmentModule.class);
			if (governmentModule == null)
			{
				check.setImpossibilityReason("No government module on planet '"+check.planet.getName()+"'");
				return check;
			}
			
			StarshipPlant starshipPlant = check.planet.getBuilding(StarshipPlant.class);
			if (starshipPlant == null)
			{
				check.setImpossibilityReason("No starship plant on planet '"+check.planet.getName()+"'");
				return check;
			}
			
			if (starshipPlant.getLastBuildDate() >= gameBoard.getDate())
			{
				check.setImpossibilityReason("Starship plant is still in construction.");
				return check;
			}
			
			check.carbonCost = gameBoard.getConfig().getGovernmentStarshipCarbonPrice();
			check.populationCost = gameBoard.getConfig().getGovernmentStarshipPopulationPrice();
			
			if (check.carbonCost > check.planet.getCarbon())
			{
				check.setImpossibilityReason("Not enough carbon ("+check.carbonCost+"C, "+check.populationCost+"P)");
				return check;
			}
			
			if (check.populationCost > check.planet.getPopulation())
			{
				check.setImpossibilityReason("Not enough population ("+check.carbonCost+"C, "+check.populationCost+"P)");
				return check;
			}
			
			return check;
		}
		
	}
	
	///// MakeStarships
	
	public static class MakeStarshipsParams implements Serializable
	{				
		public final String planetName;
		public final Map<StarshipTemplate, Integer> starshipsToMake = new HashMap<StarshipTemplate, Integer>();

		public MakeStarshipsParams(String planetName, Map<org.axan.sep.common.StarshipTemplate, Integer> starshipsToMake)
		{
			this.planetName = planetName;
			if (starshipsToMake != null) this.starshipsToMake.putAll(starshipsToMake);
		}
	}
	
	public static class MakeStarshipsCheck extends AbstractGameCommandCheck
	{
		Planet planet;
		int carbonCost;
		int populationCost;
		Fleet unasignedFleet;
		Area area;
		RealLocation location;
		
		public MakeStarshipsCheck(PlayerGameBoard gameBoard)
		{
			super(gameBoard);
		}
	}
	
	public static class MakeStarships extends AbstractGameCommand<MakeStarshipsParams, MakeStarshipsCheck> implements Serializable
	{
		public MakeStarships(String planetName, Map<org.axan.sep.common.StarshipTemplate, Integer> starshipsToMake)
		{
			super(new MakeStarshipsParams(planetName, starshipsToMake));
		}

		@Override
		protected PlayerGameBoard apply(MakeStarshipsCheck check) throws GameCommandException
		{
			check.planet.setCarbon(check.planet.getCarbon() - check.carbonCost);
			check.planet.setPopulation(check.planet.getPopulation() - check.populationCost);
			
			if (check.unasignedFleet == null)
			{
				check.area.addUnit(new Fleet(true, -1, Fleet.getUnasignedFleetName(check.planet.getName()), check.getGameBoard().getPlayerName(), null, null, check.location, 0.0, 0.0, params.starshipsToMake, null, null, null, true));
			}
			else
			{
				check.unasignedFleet.merge(params.starshipsToMake, null);
			}			

			return check.getGameBoard();
		}

		@Override
		protected MakeStarshipsCheck check(PlayerGameBoard gameBoard)
		{
			MakeStarshipsCheck check = new MakeStarshipsCheck(gameBoard);
			
			try
			{
				check.planet = gameBoard.getCelestialBody(params.planetName, Planet.class);
			}
			catch(PlayerGameBoardQueryException e)
			{
				check.setException(e);
				return check;
			}
			
			if (check.planet == null || !gameBoard.getPlayerName().equals(check.planet.getOwnerName()))
			{
				check.setImpossibilityReason("Planet '"+params.planetName+"' does not exist or is not owned by '"+gameBoard.getPlayerName()+"'");
				return check;
			}
			
			StarshipPlant starshipPlant = check.planet.getBuilding(StarshipPlant.class);
			if (starshipPlant == null || starshipPlant.getLastBuildDate() >= gameBoard.getDate())
			{
				check.setImpossibilityReason("No starship plant (or still in construction).");
				return check;
			}
			
			try
			{
				check.location = gameBoard.getCelestialBodyLocation(check.planet.getName());
			}
			catch(PlayerGameBoardQueryException e)
			{
				check.setException(e);
				return check;
			}
			if (check.location == null)
			{
				check.setImpossibilityReason("Cannot locate planet '"+check.planet.getName()+"'");
				return check;
			}
			
			check.area = gameBoard.getArea(check.location);
			if (check.area == null)
			{
				check.setImpossibilityReason("Cannot find fleet area.");
				return check;
			}
			
			try
			{
				check.unasignedFleet = gameBoard.getUnit(gameBoard.getPlayerName(), Fleet.getUnasignedFleetName(check.planet.getName()), Fleet.class);
			}
			catch(PlayerGameBoardQueryException e)
			{
				check.setException(e);
				return check;
			}
			// (check.unasignedFleet == null) is OK.
			
			check.carbonCost = 0;
			check.populationCost = 0;
			
			for(Map.Entry<StarshipTemplate, Integer> e : params.starshipsToMake.entrySet())
			{
				if (e.getValue() <= 0) continue;
				
				int carbonPrice = e.getKey().getCarbonPrice();
				int populationPrice = e.getKey().getPopulationPrice();
				
				if ((carbonPrice <= 0 && populationPrice <= 0) || carbonPrice < 0 || populationPrice < 0)
				{
					check.setImpossibilityReason("Implementation error : Price are not correctly defined for Starship template '"+e.getKey().getName()+"'");
					return check;
				}
				
				check.carbonCost += carbonPrice * e.getValue();
				check.populationCost += carbonPrice * e.getValue();
			}
			
			if ((check.carbonCost <= 0 && check.populationCost <= 0) || check.carbonCost < 0 || check.populationCost < 0)
			{
				check.setImpossibilityReason("Seems like no starships are selected (cost is incorrect)");
				return check;
			}
			
			if (check.planet.getCarbon() < check.carbonCost)
			{
				check.setImpossibilityReason("Not enough carbon ("+check.carbonCost+"C, "+check.populationCost+"P)");
				return check;
			}
			
			if (check.planet.getPopulation() < check.populationCost)
			{
				check.setImpossibilityReason("Not enough population ("+check.carbonCost+"C, "+check.populationCost+"P)");
				return check;
			}
			
			check.setPrice(check.carbonCost, check.populationCost);
			
			return check;
		}
	}
	
	///// FormFleet
	
	public static class FormFleetParams implements Serializable
	{
		public final String productiveCelestialBodyName;
		public final String fleetName;
		public final Map<StarshipTemplate, Integer> fleetToFormStarships = new HashMap<StarshipTemplate, Integer>();
		public final Set<String> fleetToFormSpecialUnits = new HashSet<String>();
		
		public FormFleetParams(String productiveCelestialBodyName, String fleetName, Map<org.axan.sep.common.StarshipTemplate, Integer> fleetToFormStarships, Set<String> fleetToFormSpecialUnits)
		{
			this.productiveCelestialBodyName = productiveCelestialBodyName;
			this.fleetName = fleetName;
			if (fleetToFormStarships != null) this.fleetToFormStarships.putAll(fleetToFormStarships);
			if (fleetToFormSpecialUnits != null) this.fleetToFormSpecialUnits.addAll(fleetToFormSpecialUnits); 
		}
	}
	
	public static class FormFleetCheck extends AbstractGameCommandCheck
	{
		Area area;
		ProductiveCelestialBody productiveCelestialBody;
		Fleet newFleet;
		Fleet unasignedFleet;
		
		public FormFleetCheck(PlayerGameBoard gameBoard)
		{
			super(gameBoard);
		}
	}
	
	public static class FormFleet extends AbstractGameCommand<FormFleetParams, FormFleetCheck> implements Serializable
	{
		public FormFleet(String productiveCelestialBodyName, String fleetName, Map<org.axan.sep.common.StarshipTemplate, Integer> fleetToFormStarships, Set<String> fleetToFormSpecialUnits)
		{
			super(new FormFleetParams(productiveCelestialBodyName, fleetName, fleetToFormStarships, fleetToFormSpecialUnits));
		}

		@Override
		protected PlayerGameBoard apply(FormFleetCheck check) throws GameCommandException
		{
			check.unasignedFleet.remove(params.fleetToFormStarships, check.newFleet.getSpecialUnits());			
			check.area.addUnit(check.newFleet);
			return check.getGameBoard();
		}

		@Override
		protected FormFleetCheck check(PlayerGameBoard gameBoard)
		{
			FormFleetCheck check = new FormFleetCheck(gameBoard);
					
			try
			{
				check.productiveCelestialBody = gameBoard.getCelestialBody(params.productiveCelestialBodyName, ProductiveCelestialBody.class);
			}
			catch(PlayerGameBoardQueryException e)
			{
				check.setException(e);
				return check;
			}
			
			if (check.productiveCelestialBody == null || !gameBoard.getPlayerName().equals(check.productiveCelestialBody.getOwnerName()))
			{
				check.setImpossibilityReason("Productive celestial body '"+params.productiveCelestialBodyName+"' does not exist or is not owned by '"+gameBoard.getPlayerName()+"'");
				return check;
			}
			
			try
			{
				check.unasignedFleet = gameBoard.getUnit(gameBoard.getPlayerName(), Fleet.getUnasignedFleetName(check.productiveCelestialBody.getName()), Fleet.class);
			}
			catch(PlayerGameBoardQueryException e)
			{
				check.setException(e);
				return check;
			}
			if (check.unasignedFleet == null)
			{
				check.setImpossibilityReason("No unasigned fleet on '"+params.productiveCelestialBodyName+"'");
				return check;
			}
			
			Fleet fleet;
			try
			{
				fleet = gameBoard.getUnit(gameBoard.getPlayerName(), params.fleetName, Fleet.class);
			}
			catch(PlayerGameBoardQueryException e2)
			{
				check.setException(e2);
				return check;				
			}
			
			if (fleet != null)
			{
				check.setImpossibilityReason("Fleet '"+params.fleetName+"' already exist.");
				return check;
			}						
			
			Map<StarshipTemplate, Integer> unasignedStarships = check.unasignedFleet.getStarships();
			if (unasignedStarships == null)
			{
				check.setImpossibilityReason("No unasigned starships on '"+params.productiveCelestialBodyName+"'");
				return check;
			}
			
			// Starship availability check
			for(Map.Entry<StarshipTemplate, Integer> e : params.fleetToFormStarships.entrySet())
			{
				if (e.getValue() <= 0) continue;
				
				int qt = e.getValue();
				if (!unasignedStarships.containsKey(e.getKey()) || unasignedStarships.get(e.getKey()) < qt)
				{
					check.setImpossibilityReason("Not enough unasigned '"+e.getKey().getName()+"' on '"+params.productiveCelestialBodyName+"'");
					return check;
				}
			}
			
			Set<ISpecialUnit> specialUnits = new HashSet<ISpecialUnit>();
			Set<ISpecialUnit> unasignedSpecialUnits = check.unasignedFleet.getSpecialUnits();
			
			if (params.fleetToFormSpecialUnits != null) for(String specialUnitName : params.fleetToFormSpecialUnits)
			{
				boolean found = false;
				for(ISpecialUnit specialUnit : unasignedSpecialUnits)
				{
					if (specialUnitName.equals(specialUnit.getName()))
					{						
						found = true;
						specialUnits.add(specialUnit);
						break;
					}
				}
				
				if (!found)
				{
					check.setImpossibilityReason("Cannot find special unit '"+specialUnitName+"' among unasigned units on '"+params.productiveCelestialBodyName+"'");
					return check;
				}				
			}
			
			RealLocation location;
			try
			{
				location = gameBoard.getCelestialBodyLocation(params.productiveCelestialBodyName);
			}
			catch(PlayerGameBoardQueryException e1)
			{
				check.setException(e1);
				return check;
			}
			
			if (location == null)
			{
				check.setImpossibilityReason("Cannot locate celestial body '"+params.productiveCelestialBodyName+"'");
				return check;
			}
			
			check.area = gameBoard.getArea(location);
			if (check.area == null)
			{
				check.setImpossibilityReason("Cannot find location area");
				return check;
			}
			
			check.newFleet = new Fleet(true, gameBoard.getDate(), params.fleetName, gameBoard.getPlayerName(), location, null, location, 0, /*TODO*//* 0, params.fleetToFormStarships, specialUnits, null, null, false);
/*			
			return check;
		}
	}
	
	///// MakeProbes
	public static class MakeProbesParams implements Serializable
	{
		public final String planetName;
		public final String probeName;
		public final int quantity;
		
		public MakeProbesParams(String planetName, String probeName, int quantity)
		{
			this.planetName = planetName;
			this.probeName = probeName;
			this.quantity = quantity;
		}
	}
	
	public static class MakeProbesCheck extends AbstractGameCommandCheck
	{
		Planet planet;
		int carbonCost;
		int populationCost;
		Set<Probe> newProbes;
		Area area;
		
		public MakeProbesCheck(PlayerGameBoard gameBoard)
		{
			super(gameBoard);
		}
	}
	
	public static class MakeProbes extends AbstractGameCommand<MakeProbesParams, MakeProbesCheck> implements Serializable
	{
		public MakeProbes(String planetName, String probeName, int quantity)
		{
			super(new MakeProbesParams(planetName, probeName, quantity));
		}

		@Override
		protected PlayerGameBoard apply(MakeProbesCheck check) throws GameCommandException
		{
			check.planet.setCarbon(check.planet.getCarbon() - check.carbonCost);
			check.planet.setPopulation(check.planet.getPopulation() - check.populationCost);
			for(Probe p : check.newProbes)
			{
				check.area.addUnit(p);
			}
			
			return check.getGameBoard();
		}

		@Override
		protected MakeProbesCheck check(PlayerGameBoard gameBoard)
		{
			MakeProbesCheck check = new MakeProbesCheck(gameBoard);
			
			try
			{
				check.planet = gameBoard.getCelestialBody(params.planetName, Planet.class);
			}
			catch(PlayerGameBoardQueryException e)
			{
				check.setException(e);
				return check;
			}
			
			if (check.planet == null || !gameBoard.getPlayerName().equals(check.planet.getOwnerName()))
			{
				check.setImpossibilityReason("Planet '"+params.planetName+"' does not exist or is not owned by '"+gameBoard.getPlayerName()+"'");
				return check;
			}
			
			StarshipPlant starshipPlant = check.planet.getBuilding(StarshipPlant.class);
			if (starshipPlant == null || starshipPlant.getLastBuildDate() >= gameBoard.getDate())
			{
				check.setImpossibilityReason("No starship plant (or still in construction).");
				return check;
			}
			
			check.carbonCost = Probe.PRICE_CARBON * params.quantity;
			check.populationCost = Probe.PRICE_POPULATION * params.quantity;
			
			if ((check.carbonCost <= 0 && check.populationCost <= 0) || check.carbonCost < 0 || check.populationCost < 0)
			{
				check.setImpossibilityReason("Cost is incorrect (Quantity is null ?)");
				return check;
			}									
									
			if (check.planet.getCarbon() < check.carbonCost)
			{
				check.setImpossibilityReason("Not enough carbon ("+check.carbonCost+"C, "+check.populationCost+"P)");
				return check;
			}
			
			if (check.planet.getPopulation() < check.populationCost)
			{
				check.setImpossibilityReason("Not enough population ("+check.carbonCost+"C, "+check.populationCost+"P)");
				return check;
			}
						
			try
			{
				if (gameBoard.getUnit(gameBoard.getPlayerName(), params.probeName, Probe.class) != null)
				{
					check.setImpossibilityReason("Probe named '"+params.probeName+"' already exist.");
					return check;
				}
				
				for(int i=0; i < params.quantity; ++i)
				{
					if (gameBoard.getUnit(gameBoard.getPlayerName(), params.probeName + i, Probe.class) != null)
					{
						check.setImpossibilityReason("Probe named '"+params.probeName+"' already exist.");
						return check;
					}
				}
			}
			catch(PlayerGameBoardQueryException e)
			{
				check.setException(e);
				return check;
			}
			
			RealLocation location;
			try
			{
				location = gameBoard.getCelestialBodyLocation(params.planetName);
			}
			catch(PlayerGameBoardQueryException e1)
			{
				check.setException(e1);
				return check;
			}
			
			if (location == null)
			{
				check.setImpossibilityReason("Cannot locate celestial body '"+params.planetName+"'");
				return check;
			}
			
			check.area = gameBoard.getArea(location);
			if (check.area == null)
			{
				check.setImpossibilityReason("Cannot find location area");
				return check;
			}
			
			check.newProbes = new HashSet<Probe>();
			for(int i=0; i<params.quantity; ++i)
			{
				check.newProbes.add(new Probe(true, gameBoard.getDate(), params.probeName + i, gameBoard.getPlayerName(), location, null, location, 0, /*TODO*//*0, false));
/*			}
			
			return check;
		}
	}
	
	///// MakeAntiProbeMissiles
	public static class MakeAntiProbeMissilesParams implements Serializable
	{
		public final String planetName;
		public final String antiProbeMissileName;
		public final int quantity;
		
		public MakeAntiProbeMissilesParams(String planetName, String antiProbeMissileName, int quantity)
		{
			this.planetName = planetName;
			this.antiProbeMissileName = antiProbeMissileName;
			this.quantity = quantity;
		}
	}
	
	public static class MakeAntiProbeMissilesCheck extends AbstractGameCommandCheck
	{
		Planet planet;
		int carbonCost;
		int populationCost;
		Set<AntiProbeMissile> newAntiProbeMissiles;
		Area area;
		
		public MakeAntiProbeMissilesCheck(PlayerGameBoard gameBoard)
		{
			super(gameBoard);
		}
	}
	
	public static class MakeAntiProbeMissiles extends AbstractGameCommand<MakeAntiProbeMissilesParams, MakeAntiProbeMissilesCheck> implements Serializable
	{
		public MakeAntiProbeMissiles(String planetName, String antiProbeMissileName, int quantity)
		{
			super(new MakeAntiProbeMissilesParams(planetName, antiProbeMissileName, quantity));
		}

		@Override
		protected PlayerGameBoard apply(MakeAntiProbeMissilesCheck check) throws GameCommandException
		{
			check.planet.setCarbon(check.planet.getCarbon() - check.carbonCost);
			check.planet.setPopulation(check.planet.getPopulation() - check.populationCost);
			for(AntiProbeMissile apm : check.newAntiProbeMissiles)
			{
				check.area.addUnit(apm);
			}
			
			return check.getGameBoard();
		}

		@Override
		protected MakeAntiProbeMissilesCheck check(PlayerGameBoard gameBoard)
		{
			MakeAntiProbeMissilesCheck check = new MakeAntiProbeMissilesCheck(gameBoard);
			
			try
			{
				check.planet = gameBoard.getCelestialBody(params.planetName, Planet.class);
			}
			catch(PlayerGameBoardQueryException e)
			{
				check.setException(e);
				return check;
			}
			
			if (check.planet == null || !gameBoard.getPlayerName().equals(check.planet.getOwnerName()))
			{
				check.setImpossibilityReason("Planet '"+params.planetName+"' does not exist or is not owned by '"+gameBoard.getPlayerName()+"'");
				return check;
			}
			
			StarshipPlant starshipPlant = check.planet.getBuilding(StarshipPlant.class);
			if (starshipPlant == null || starshipPlant.getLastBuildDate() >= gameBoard.getDate())
			{
				check.setImpossibilityReason("No starship plant (or still in construction).");
				return check;
			}
			
			check.carbonCost = AntiProbeMissile.PRICE_CARBON * params.quantity;
			check.populationCost = AntiProbeMissile.PRICE_POPULATION * params.quantity;
			
			if ((check.carbonCost <= 0 && check.populationCost <= 0) || check.carbonCost < 0 || check.populationCost < 0)
			{
				check.setImpossibilityReason("Cost is incorrect (Quantity is null ?)");
				return check;
			}									
									
			if (check.planet.getCarbon() < check.carbonCost)
			{
				check.setImpossibilityReason("Not enough carbon ("+check.carbonCost+"C, "+check.populationCost+"P)");
				return check;
			}
			
			if (check.planet.getPopulation() < check.populationCost)
			{
				check.setImpossibilityReason("Not enough population ("+check.carbonCost+"C, "+check.populationCost+"P)");
				return check;
			}
						
			try
			{
				if (gameBoard.getUnit(gameBoard.getPlayerName(), params.antiProbeMissileName, AntiProbeMissile.class) != null)
				{
					check.setImpossibilityReason("Anti-probe missile named '"+params.antiProbeMissileName+"' already exist.");
					return check;
				}
				
				for(int i=0; i < params.quantity; ++i)
				{
					if (gameBoard.getUnit(gameBoard.getPlayerName(), params.antiProbeMissileName + i, AntiProbeMissile.class) != null)
					{
						check.setImpossibilityReason("Anti-probe missile named '"+params.antiProbeMissileName+"' already exist.");
						return check;
					}
				}
			}
			catch(PlayerGameBoardQueryException e)
			{
				check.setException(e);
				return check;
			}
			
			RealLocation location;
			try
			{
				location = gameBoard.getCelestialBodyLocation(params.planetName);
			}
			catch(PlayerGameBoardQueryException e1)
			{
				check.setException(e1);
				return check;
			}
			
			if (location == null)
			{
				check.setImpossibilityReason("Cannot locate celestial body '"+params.planetName+"'");
				return check;
			}
			
			check.area = gameBoard.getArea(location);
			if (check.area == null)
			{
				check.setImpossibilityReason("Cannot find location area");
				return check;
			}
			
			check.newAntiProbeMissiles = new HashSet<AntiProbeMissile>();
			for(int i=0; i<params.quantity; ++i)
			{
				check.newAntiProbeMissiles.add(new AntiProbeMissile(true, gameBoard.getDate(), params.antiProbeMissileName + i, gameBoard.getPlayerName(), location, null, location, 0, /*TODO*//*0, false));
/*			}
			
			return check;
		}
	}
	
	///// DemolishSpaceRoad
	public static class DemolishSpaceRoadParams implements Serializable
	{
		public final String sourceName;
		public final String destinationName;
		
		public DemolishSpaceRoadParams(String sourceName, String destinationName)
		{
			this.sourceName = sourceName;
			this.destinationName = destinationName;
		}
	}
	
	public static class DemolishSpaceRoadCheck extends AbstractGameCommandCheck
	{
		SpaceCounter source;
		
		public DemolishSpaceRoadCheck(PlayerGameBoard gameBoard)
		{
			super(gameBoard);
		}
	}
	
	public static class DemolishSpaceRoad extends AbstractGameCommand<DemolishSpaceRoadParams, DemolishSpaceRoadCheck> implements Serializable
	{
		public DemolishSpaceRoad(String sourceName, String destinationName)
		{
			super(new DemolishSpaceRoadParams(sourceName, destinationName));
		}

		@Override
		protected PlayerGameBoard apply(DemolishSpaceRoadCheck check) throws GameCommandException
		{
			check.source.cutSpaceRoadLink(params.destinationName);
			return check.getGameBoard();
		}

		@Override
		protected DemolishSpaceRoadCheck check(PlayerGameBoard gameBoard)
		{
			DemolishSpaceRoadCheck check = new DemolishSpaceRoadCheck(gameBoard);
			
			ProductiveCelestialBody source;
			try
			{
				source = gameBoard.getCelestialBody(params.sourceName, ProductiveCelestialBody.class);
			}
			catch(PlayerGameBoardQueryException e)
			{
				check.setException(e);
				return check;
			}
			
			if (source == null)
			{
				check.setImpossibilityReason("Cannot find source productive celestial body '"+params.sourceName+"'");
				return check;
			}
			
			check.source = source.getBuilding(SpaceCounter.class);
			if (check.source == null)
			{
				check.setImpossibilityReason(params.sourceName+" has no space counter built.");
			}
			
			if (!check.source.hasSpaceRoadWith(params.destinationName))
			{
				check.setImpossibilityReason(params.sourceName+" has no space road link with '"+params.destinationName+"'");
				return check;
			}
			
			return check;
		}		
		
	}
	
	///// BuildSpaceRoad
	public static class BuildSpaceRoadParams implements Serializable
	{
		public final String productiveCelestialBodyNameA;
		public final String productiveCelestialBodyNameB;
		
		public BuildSpaceRoadParams(String productiveCelestialBodyNameA, String productiveCelestialBodyNameB)
		{
			this.productiveCelestialBodyNameA = productiveCelestialBodyNameA;
			this.productiveCelestialBodyNameB = productiveCelestialBodyNameB;
		}
	}
	
	public static class BuildSpaceRoadCheck extends AbstractGameCommandCheck
	{
		Area source;
		SpaceRoadDeliverer deliverer;
		RealLocation destinationLocation;
		ProductiveCelestialBody payer;
		int price;
		
		public BuildSpaceRoadCheck(PlayerGameBoard gameBoard)
		{
			super(gameBoard);
		}
	}
	
	public static class BuildSpaceRoad extends AbstractGameCommand<BuildSpaceRoadParams, BuildSpaceRoadCheck> implements Serializable
	{
		public BuildSpaceRoad(String productiveCelestialBodyNameA, String productiveCelestialBodyNameB)
		{
			super(new BuildSpaceRoadParams(productiveCelestialBodyNameA, productiveCelestialBodyNameB));
		}

		@Override
		protected PlayerGameBoard apply(BuildSpaceRoadCheck check) throws GameCommandException
		{
			check.payer.setCarbon(check.payer.getCarbon() - check.price);
			check.source.addUnit(check.deliverer);
			check.deliverer.launch(check.destinationLocation);
			
			return check.getGameBoard();
		}

		@Override
		protected BuildSpaceRoadCheck check(PlayerGameBoard gameBoard)
		{
			BuildSpaceRoadCheck check = new BuildSpaceRoadCheck(gameBoard);
			
			ProductiveCelestialBody source;
			try
			{
				source = gameBoard.getCelestialBody(params.productiveCelestialBodyNameA, ProductiveCelestialBody.class);
			}
			catch(PlayerGameBoardQueryException e)
			{
				check.setException(e);
				return check;
			}
			if (source == null)
			{
				check.setImpossibilityReason("Cannot find productive celestial body '"+params.productiveCelestialBodyNameA+"'");
				return check;
			}
			if (!gameBoard.getPlayerName().equals(source.getOwnerName()))
			{
				check.setImpossibilityReason(gameBoard.getPlayerName()+" is not '"+params.productiveCelestialBodyNameA+"' owner.");
				return check;
			}
			
			SpaceCounter sourceSpaceCounter = source.getBuilding(SpaceCounter.class);
			if (sourceSpaceCounter == null)
			{
				check.setImpossibilityReason(params.productiveCelestialBodyNameA+" has no space counter built.");
				return check;
			}
			
			
			ProductiveCelestialBody destination;
			try
			{
				destination = gameBoard.getCelestialBody(params.productiveCelestialBodyNameB, ProductiveCelestialBody.class);
			}
			catch(PlayerGameBoardQueryException e)
			{
				check.setException(e);
				return check;
			}
			if (destination == null)
			{
				check.setImpossibilityReason("Cannot find productive celestial body '"+params.productiveCelestialBodyNameB+"'");
				return check;
			}
			
			SpaceCounter destinationSpaceCounter = destination.getBuilding(SpaceCounter.class);
			if (destinationSpaceCounter == null)
			{
				check.setImpossibilityReason(params.productiveCelestialBodyNameB+" has no space counter built.");
				return check;
			}
			
			
			if (sourceSpaceCounter.hasSpaceRoadWith(params.productiveCelestialBodyNameB))
			{
				check.setImpossibilityReason(params.productiveCelestialBodyNameA+" already has a space road link with "+params.productiveCelestialBodyNameB);
				return check;
			}
			
			RealLocation sourceLocation;
			try
			{
				sourceLocation = gameBoard.getCelestialBodyLocation(source.getName());
			}
			catch(PlayerGameBoardQueryException e)
			{
				check.setException(e);
				return check;
			}
			if (sourceLocation == null)
			{
				check.setImpossibilityReason("Cannot locate '"+params.productiveCelestialBodyNameA+"'");
				return check;
			}
			check.source = gameBoard.getArea(sourceLocation);
			if (check.source == null)
			{
				check.setImpossibilityReason("Cannot locate source area.");
				return check;
			}
			
			RealLocation destinationLocation;
			try
			{
				destinationLocation = gameBoard.getCelestialBodyLocation(destination.getName());
			}
			catch(PlayerGameBoardQueryException e)
			{
				check.setException(e);
				return check;
			}
			if (destinationLocation == null)
			{
				check.setImpossibilityReason("Cannot locate '"+params.productiveCelestialBodyNameB+"'");
				return check;
			}						
			
			if (gameBoard.isTravellingTheSun(sourceLocation, destinationLocation))
			{
				check.setImpossibilityReason("Impossible path : "+sourceLocation+" to "+destinationLocation+", cannot travel the sun.");
				return check;
			}
			
			double distance = SEPUtils.getDistance(sourceLocation, destinationLocation);
			check.price = (int) (gameBoard.getConfig().getSpaceRoadPricePerArea() * distance);
			
			if (sourceSpaceCounter.getAvailableRoadsBuilder() <= 0)
			{
				check.setImpossibilityReason("No free road builder at "+params.productiveCelestialBodyNameA+" space counter.");
				return check;
			}
			
			check.payer = source;
			if (check.payer.getCarbon() < check.price)
			{
				check.setImpossibilityReason("Not enough carbon ("+check.price+"C)");
				return check;
			}
			
			String delivererId = params.productiveCelestialBodyNameA+" to "+params.productiveCelestialBodyNameB+" space road deliverer";
			try
			{
				if (gameBoard.getUnit(gameBoard.getPlayerName(), delivererId) != null)
				{
					check.setImpossibilityReason("Space road from '"+params.productiveCelestialBodyNameA+"' to '"+params.productiveCelestialBodyNameB+"' is already in delivery.");
					return check;
				}
			}
			catch(PlayerGameBoardQueryException e)
			{
				check.setException(e);
				return check;
			}
			
			check.deliverer = new SpaceRoadDeliverer(true, gameBoard.getDate(), delivererId, gameBoard.getPlayerName(), sourceLocation, destinationLocation, sourceLocation, 0, /*TODO*//*0);
/*			check.destinationLocation = destinationLocation;
			
			check.setPrice(check.price, 0);
			
			return check;
		}
	}
	
	///// ModifyCarbonOrder
	public static class ModifyCarbonOrderParams implements Serializable
	{
		public final String originCelestialBodyName;
		public final Stack<CarbonOrder> nextCarbonOrders = new Stack<CarbonOrder>();
		
		public ModifyCarbonOrderParams(String originCelestialBodyName, Stack<CarbonOrder> nextCarbonOrders)
		{
			this.originCelestialBodyName = originCelestialBodyName;
			if (nextCarbonOrders != null) this.nextCarbonOrders.addAll(nextCarbonOrders);
		}
	}
	
	public static class ModifyCarbonOrderCheck extends AbstractGameCommandCheck
	{
		SpaceCounter spaceCounter;
		
		public ModifyCarbonOrderCheck(PlayerGameBoard gameBoard)
		{
			super(gameBoard);
		}
	}
	
	public static class ModifyCarbonOrder extends AbstractGameCommand<ModifyCarbonOrderParams, ModifyCarbonOrderCheck> implements Serializable
	{
		public ModifyCarbonOrder(String originCelestialBodyName, Stack<CarbonOrder> nextCarbonOrders)
		{
			super(new ModifyCarbonOrderParams(originCelestialBodyName, nextCarbonOrders));
		}

		@Override
		protected PlayerGameBoard apply(ModifyCarbonOrderCheck check) throws GameCommandException
		{
			check.spaceCounter.modifyCarbonOrder(params.nextCarbonOrders);
			return check.getGameBoard();
		}

		@Override
		protected ModifyCarbonOrderCheck check(PlayerGameBoard gameBoard)
		{
			ModifyCarbonOrderCheck check = new ModifyCarbonOrderCheck(gameBoard);
			
			ProductiveCelestialBody source;
			try
			{
				source = gameBoard.getCelestialBody(params.originCelestialBodyName, ProductiveCelestialBody.class);
			}
			catch(PlayerGameBoardQueryException e)
			{
				check.setException(e);
				return check;
			}
			if (source == null)
			{
				check.setImpossibilityReason("Cannot find productive celestial body '"+params.originCelestialBodyName+"'");
				return check;
			}
			if (!gameBoard.getPlayerName().equals(source.getOwnerName()))
			{
				check.setImpossibilityReason(gameBoard.getPlayerName()+" is not '"+params.originCelestialBodyName+"' owner.");
				return check;
			}
			
			SpaceCounter sourceSpaceCounter = source.getBuilding(SpaceCounter.class);
			if (sourceSpaceCounter == null)
			{
				check.setImpossibilityReason(params.originCelestialBodyName+" has no space counter built.");
				return check;
			}
			
			RealLocation sourceLocation;
			try
			{
				sourceLocation = gameBoard.getCelestialBodyLocation(source.getName());
			}
			catch(PlayerGameBoardQueryException e)
			{
				check.setException(e);
				return check;
			}
			if (sourceLocation == null)
			{
				check.setImpossibilityReason("Cannot locate '"+source.getName()+"'");
				return check;
			}
			
			for(CarbonOrder order : params.nextCarbonOrders)
			{
				String destinationCelestialBodyName = order.getDestinationName();
				int amount = order.getAmount();
				
				ProductiveCelestialBody destination;
				try
				{
					destination = gameBoard.getCelestialBody(destinationCelestialBodyName, ProductiveCelestialBody.class);
				}
				catch(PlayerGameBoardQueryException e1)
				{
					check.setException(e1);
					return check;
				}				
				
				if (destination == null)
				{
					check.setImpossibilityReason("Celestial body '"+destinationCelestialBodyName+"' is not a productive celestial body.");
					return check;
				}
				
				SpaceCounter destinationSpaceCounter = destination.getBuilding(SpaceCounter.class);
				if (destinationSpaceCounter == null)
				{
					check.setImpossibilityReason("'"+destinationCelestialBodyName+"' has no space counter build.");
					return check;
				}
			
				if (amount < gameBoard.getConfig().getCarbonMinimalFreight())
				{
					check.setImpossibilityReason("Carbon amount must be greater than "+gameBoard.getConfig().getCarbonMinimalFreight()+".");
					return check;
				}
				
				RealLocation destinationLocation;
				try
				{
					destinationLocation = gameBoard.getCelestialBodyLocation(destination.getName());
				}
				catch(PlayerGameBoardQueryException e)
				{
					check.setException(e);
					return check;
				}
				if (destinationLocation == null)
				{
					check.setImpossibilityReason("Cannot locate '"+destinationCelestialBodyName+"'");
					return check;
				}
				
				if (gameBoard.isTravellingTheSun(sourceLocation, destinationLocation))
				{
					check.setImpossibilityReason("Impossible path : " + sourceLocation + " to " + destinationLocation + ", cannot travel the sun.");
					return check;
				}
			}
			
			check.spaceCounter = sourceSpaceCounter;
			return check;
		}
		
	}
	
	///// ChangeDiplomacy
	public static class ChangeDiplomacyParams implements Serializable
	{
		public final Map<String, PlayerPolicies> newPolicies = new HashMap<String, PlayerPolicies>();
		
		public ChangeDiplomacyParams(Map<String,PlayerPolicies> newPolicies)
		{
			if (newPolicies != null) this.newPolicies.putAll(newPolicies);
		}
	}
	
	public static class ChangeDiplomacyCheck extends AbstractGameCommandCheck
	{
		public ChangeDiplomacyCheck(PlayerGameBoard gameBoard)
		{
			super(gameBoard);
		}
	}
	
	public static class ChangeDiplomacy extends AbstractGameCommand<ChangeDiplomacyParams, ChangeDiplomacyCheck> implements Serializable
	{
		public ChangeDiplomacy(Map<String,PlayerPolicies> newPolicies)
		{
			super(new ChangeDiplomacyParams(newPolicies));
		}

		@Override
		protected PlayerGameBoard apply(ChangeDiplomacyCheck check) throws GameCommandException
		{
			check.getGameBoard().getPlayersPolicies().get(check.getGameBoard().getPlayerName()).update(params.newPolicies);
			return check.getGameBoard();
		}

		@Override
		protected ChangeDiplomacyCheck check(PlayerGameBoard gameBoard)
		{
			ChangeDiplomacyCheck check = new ChangeDiplomacyCheck(gameBoard);
			
			if (params.newPolicies.get(gameBoard.getPlayerName()) != null)
			{
				check.setImpossibilityReason("Cannot have a diplomacy toward ourselves.");
				return check;
			}
			
			return check;
		}
	}
	
	///// MoveFleet
	public static class MoveFleetParams implements Serializable
	{
		public final String fleetName;
		public final Stack<Fleet.Move> checkpoints = new Stack<Fleet.Move>();
		
		public MoveFleetParams(String fleetName, Stack<org.axan.sep.common.db.sqlite.orm.Fleet.Move> checkpoints)
		{
			this.fleetName = fleetName;
			if (checkpoints != null) this.checkpoints.addAll(checkpoints);
		}
	}
	
	public static class MoveFleetCheck extends AbstractGameCommandCheck
	{
		Fleet fleet;
		Stack<Fleet.Move> locatedCheckpoints;
		
		public MoveFleetCheck(PlayerGameBoard gameBoard)
		{
			super(gameBoard);
		}
	}
	
	public static class MoveFleet extends AbstractGameCommand<MoveFleetParams, MoveFleetCheck> implements Serializable
	{
		public MoveFleet(String fleetName, Stack<org.axan.sep.common.db.sqlite.orm.Fleet.Move> checkpoints)
		{
			super(new MoveFleetParams(fleetName, checkpoints));
		}

		@Override
		protected PlayerGameBoard apply(MoveFleetCheck check) throws GameCommandException
		{
			check.fleet.updateMoveOrder(check.locatedCheckpoints);
			return check.getGameBoard();
		}

		@Override
		protected MoveFleetCheck check(PlayerGameBoard gameBoard)
		{
			MoveFleetCheck check = new MoveFleetCheck(gameBoard);
			
			try
			{
				check.fleet = gameBoard.getUnit(gameBoard.getPlayerName(), params.fleetName, Fleet.class);
			}
			catch(PlayerGameBoardQueryException e)
			{
				check.setException(e);
				return check;
			}
			if (check.fleet == null)
			{
				check.setImpossibilityReason("Fleet '"+params.fleetName+"' does not exist.");
				return check;
			}
			
			check.locatedCheckpoints = new Stack<Fleet.Move>();
			
			RealLocation currentStart = (check.fleet.isMoving() ? check.fleet.getDestinationLocation() : check.fleet.getCurrentLocation());
			for(org.axan.sep.common.db.sqlite.orm.Fleet.Move move : params.checkpoints)
			{
				RealLocation destinationLocation;
				try
				{
					destinationLocation = gameBoard.getCelestialBodyLocation(move.getDestinationName());
				}
				catch(PlayerGameBoardQueryException e)
				{
					check.setException(e);
					return check;
				}
				
				if (destinationLocation == null)
				{
					check.setImpossibilityReason("Unexpected error : checkpoint destination '" + move.getDestinationName() + "' not found.");
					return check;
				}

				if (gameBoard.isTravellingTheSun(currentStart, destinationLocation))
				{
					check.setImpossibilityReason("Impossible path : " + currentStart + " to " + destinationLocation + ", cannot travel the sun.");
					return check;
				}

				currentStart = destinationLocation;

				check.locatedCheckpoints.add(new org.axan.sep.common.db.sqlite.orm.Fleet.Move(move, destinationLocation));
			}
			
			return check;
		}				
	}
	*/
}