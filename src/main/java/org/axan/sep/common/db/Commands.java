package org.axan.sep.common.db;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.axan.sep.common.Protocol.eBuildingType;
import org.axan.sep.common.Rules.StarshipTemplate;
import org.axan.sep.common.Rules;
import org.axan.sep.common.db.orm.SEPCommonDB;

public abstract class Commands
{
	public static abstract class ACommand implements ICommand, Serializable
	{
		protected final String playerName;
		
		protected ACommand(String playerName)
		{
			this.playerName = playerName;
		}
		
		protected ICelestialBody checkCelestialBody(SEPCommonDB db, String celestialBodyName) throws GameCommandException
		{
			ICelestialBody cb = db.getCelestialBody(celestialBodyName);
			if (cb == null) throw new GameCommandException(this, "Cannot find celestial body '"+celestialBodyName+"'");
			return cb;
		}
		
		protected IProductiveCelestialBody checkProductiveCelestialBody(SEPCommonDB db, String productiveCelestialBodyName) throws GameCommandException
		{
			ICelestialBody cb = checkCelestialBody(db, productiveCelestialBodyName);
			if (!IProductiveCelestialBody.class.isInstance(cb)) throw new GameCommandException(this, "Celestial body '"+productiveCelestialBodyName+"' is not a productive celestial body");
			return (IProductiveCelestialBody) cb;
		}
		
		protected void checkOwnership(String expectedOwner, String actualOwner, String errorMessage) throws GameCommandException
		{
			if (!actualOwner.equals(expectedOwner)) throw new GameCommandException(this, errorMessage);
		}
		
		protected void checkCanAfford(IProductiveCelestialBody productiveCelestialBody, int carbonCost, int populationCost) throws GameCommandException
		{
			boolean canAfford = true;
			
			if (populationCost > 0)
			{
				if (!IPlanet.class.isInstance(productiveCelestialBody)) throw new GameCommandException(this, "'"+productiveCelestialBody.getName()+"' is not a planet and cannot afford population cost.");
				IPlanet planet = (IPlanet) productiveCelestialBody;
				if (planet.getCurrentPopulation() < populationCost) canAfford = false;
			}
			
			if (productiveCelestialBody.getCurrentCarbon() < carbonCost) canAfford = false;
			
			if (!canAfford)
			{
				throw new GameCommandException(this, String.format("'%s' cannot afford building :%s%s", productiveCelestialBody.getName(), (carbonCost > 0) ? " "+carbonCost+"C" : "", (populationCost > 0) ? " "+populationCost+"P" : ""));
			}
		}
	}
	
	public static class AssignStarships extends ACommand implements Serializable
	{
		private final String productiveCelestialBodyName;
		private final Map<String, Integer> starships = new HashMap<String, Integer>();
		private final String fleetName;
		
		private transient IProductiveCelestialBody productiveCelestialBody;
		private transient IFleet assignedFleet;
		private transient IFleet destinationFleet;
		private transient Map<StarshipTemplate, Integer> newcomers;
		
		public AssignStarships(String playerName, String productiveCelestialBody, Map<String, Integer> starships, String fleetName)
		{
			super(playerName);
			this.productiveCelestialBodyName = productiveCelestialBody;
			this.starships.putAll(starships);
			this.fleetName = fleetName;
		}
		
		@Override
		public void check(SEPCommonDB db) throws GameCommandException
		{
			productiveCelestialBody = checkProductiveCelestialBody(db, productiveCelestialBodyName);
			checkOwnership(playerName, productiveCelestialBody.getOwner(), playerName+" does not own "+productiveCelestialBodyName);
			
			assignedFleet = productiveCelestialBody.getAssignedFleet(playerName);
			if (assignedFleet == null) throw new GameCommandException(this, "No starships available on "+productiveCelestialBodyName);
			Map<StarshipTemplate, Integer> assignedStarships = assignedFleet.getStarships();
			
			newcomers = new HashMap<StarshipTemplate, Integer>();
			for(String templateName : starships.keySet())
			{
				StarshipTemplate template = Rules.getStarshipTemplate(templateName);
				int quantity = starships.get(templateName);
				
				if (quantity < 0) throw new GameCommandException(this, "Cannot assign negative quantity of starships ("+templateName+")");
								
				int availableQuantity = (assignedStarships.containsKey(template) ? assignedStarships.get(template) : 0);
				if (quantity > availableQuantity) throw new GameCommandException(this, "Not enough starships ("+templateName+")");
				
				if (quantity > 0) newcomers.put(template, quantity);
			}
			
			if (fleetName == null || fleetName.isEmpty()) throw new GameCommandException(this, "Incorrect fleet name");
			destinationFleet = db.getFleet(playerName, fleetName);			
			if (destinationFleet != null)
			{
				if (!destinationFleet.isStopped()) throw new GameCommandException(this, "Cannot assign starships to fleet '"+fleetName+"' because fleet is currently moving ("+destinationFleet.getRealLocation().toString()+")");
				if (!productiveCelestialBody.getLocation().equals(destinationFleet.getDeparture())) throw new GameCommandException(this, "Cannot assign starships to fleet '"+fleetName+"' because fleet is not stopped on '"+productiveCelestialBodyName+"' ("+destinationFleet.getRealLocation().toString()+")");				
			}
		}
		
		public IFleet getDestinationFleet()
		{
			return destinationFleet;
		}
		
		@Override
		public void process(IGameEventExecutor executor, SEPCommonDB db) throws GameCommandException
		{
			check(db);
			
			if (destinationFleet == null)
			{
				db.createFleet(db.makeFleet(playerName, fleetName, productiveCelestialBodyName, newcomers));
			}
			else
			{
				destinationFleet.addStarships(newcomers);
				assignedFleet.removeStarships(newcomers);
			}
		}
	}
	
	/**
	 * Make starships on given productive celestial body, providing it has a StarshipPlant and belongs to the given player.
	 */
	public static class MakeStarships extends ACommand implements Serializable
	{
		private final String productiveCelestialBodyName;
		private final Map<String, Integer> starships = new HashMap<String, Integer>();
		
		private transient IProductiveCelestialBody productiveCelestialBody;
		private transient IPlanet planet;
		private transient IStarshipPlant starshipPlant;
		private transient int carbonCost;
		private transient int populationCost;
		private transient Map<StarshipTemplate, Integer> newcomers;
		
		public MakeStarships(String playerName, String productiveCelestialBodyName, Map<String, Integer> starships)
		{
			super(playerName);
			this.productiveCelestialBodyName = productiveCelestialBodyName;
			this.starships.putAll(starships);
		}
		
		@Override
		public void check(SEPCommonDB db) throws GameCommandException
		{
			productiveCelestialBody = checkProductiveCelestialBody(db, productiveCelestialBodyName);			
			checkOwnership(playerName, productiveCelestialBody.getOwner(), playerName+" does not own "+productiveCelestialBodyName);
			
			starshipPlant = (IStarshipPlant) productiveCelestialBody.getBuilding(eBuildingType.StarshipPlant);
			if (starshipPlant == null) throw new GameCommandException(this, "Cannot find "+eBuildingType.StarshipPlant+" on '"+productiveCelestialBodyName+"'");
			
			carbonCost = 0;
			populationCost = 0;
			
			newcomers = new HashMap<StarshipTemplate, Integer>();
			boolean empty = true;
			
			for(String templateName : starships.keySet())
			{
				StarshipTemplate template = Rules.getStarshipTemplate(templateName);
				int quantity = starships.get(templateName);
				if (quantity < 0) throw new GameCommandException(this, "Cannot make negative quantity of starships ("+templateName+")");
								
				if (quantity > 0)
				{
					carbonCost += template.getCarbonPrice() * quantity;
					populationCost += template.getPopulationPrice() * quantity;
					newcomers.put(template, quantity);
					if (empty) empty = false;
				}				
			}
			
			if (empty) throw new GameCommandException(this, "No starship selected");
			
			checkCanAfford(productiveCelestialBody, carbonCost, populationCost);
			planet = (IPlanet.class.isInstance(productiveCelestialBody)) ? (IPlanet) productiveCelestialBody : null;
		}
		
		public int getCarbonCost()
		{
			return carbonCost;
		}
		
		public int getPopulationCost()
		{
			return populationCost;
		}
		
		@Override
		public void process(IGameEventExecutor executor, SEPCommonDB db) throws GameCommandException
		{
			check(db);
			
			if (carbonCost > 0) productiveCelestialBody.payCarbon(carbonCost);
			if (populationCost > 0) planet.payPopulation(populationCost);
			
			IFleet assignedFleet = db.getCreateAssignedFleet(productiveCelestialBodyName, playerName);
			assignedFleet.addStarships(newcomers);
		}
	}
	
	/**
	 * Build/upgrade given building type on given productive celestial body which must be owned by given player.
	 */
	public static class Build extends ACommand implements Serializable
	{
		private final String productiveCelestialBodyName;
		private final eBuildingType buildingType;
		
		private transient IProductiveCelestialBody productiveCelestialBody;
		private transient IPlanet planet;
		private transient IBuilding existingBuilding;
		private transient int turn;
		private transient int carbonCost=0;
		private transient int populationCost=0;
		
		public Build(String playerName, String productiveCelestialBodyName, eBuildingType buildingType)
		{
			super(playerName);
			this.productiveCelestialBodyName = productiveCelestialBodyName;
			this.buildingType = buildingType;
		}
		
		@Override
		public void check(SEPCommonDB db) throws GameCommandException
		{
			productiveCelestialBody = checkProductiveCelestialBody(db, productiveCelestialBodyName);
			
			checkOwnership(playerName, productiveCelestialBody.getOwner(), playerName+" does not own '"+productiveCelestialBodyName+"'");
			
			turn = db.getConfig().getTurn();
			existingBuilding = null;
			for(IBuilding building : productiveCelestialBody.getBuildings())
			{				
				if (building.getBuiltDate() >= turn) throw new GameCommandException(this, "'"+productiveCelestialBodyName+"' is already building a '"+building.getType()+"' for this turn.");
				if (buildingType.equals(building.getType())) existingBuilding = building;
			}
			
			if (productiveCelestialBody.getMaxSlots() - productiveCelestialBody.getBuiltSlotsCount() <= 0)
			{
				throw new GameCommandException(this, "'"+productiveCelestialBodyName+"' has no more free slots");
			}
			
			if (existingBuilding != null && !Rules.getBuildingCanBeUpgraded(buildingType))
			{
				throw new GameCommandException(this, buildingType+" cannot be upgraded.");
			}
			
			int nbBuilt = existingBuilding == null ? 0 : existingBuilding.getNbSlots();
			carbonCost = Rules.getBuildingUpgradeCarbonCost(buildingType, nbBuilt+1);
			populationCost = Rules.getBuildingUpgradePopulationCost(buildingType, nbBuilt+1);
			
			checkCanAfford(productiveCelestialBody, carbonCost, populationCost);
			planet = (IPlanet.class.isInstance(productiveCelestialBody)) ? (IPlanet) productiveCelestialBody : null;
		}
		
		public int getCarbonCost()
		{
			return carbonCost;
		}
		
		public int getPopulationCost()
		{
			return populationCost;
		}
		
		@Override
		public void process(IGameEventExecutor executor, SEPCommonDB db) throws GameCommandException
		{
			check(db);
			
			if (carbonCost > 0) productiveCelestialBody.payCarbon(carbonCost);
			if (populationCost > 0) planet.payPopulation(populationCost);
			
			if (existingBuilding == null)
			{
				db.createBuilding(productiveCelestialBodyName, turn, buildingType);
			}
			else
			{
				existingBuilding.upgrade();
			}
		}
	}
}
