package org.axan.sep.common.db;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.axan.sep.common.Protocol.eBuildingType;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.Rules.StarshipTemplate;
import org.axan.sep.common.Rules;
import org.axan.sep.common.SEPUtils;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.Events.AGameEvent;
import org.axan.sep.common.db.Events.LogMessage;
import org.axan.sep.common.db.Events.UnitDismantled;
import org.axan.sep.common.db.Events.UnitMade;
import org.axan.sep.common.db.Events.UnitMoveUpdate;
import org.axan.sep.common.db.Events.UpdateDiplomacyMarker;
import org.axan.sep.common.db.IDiplomacyMarker.eForeignPolicy;
import org.axan.sep.common.db.orm.SEPCommonDB;

public abstract class Commands
{
	public static abstract class ACommand extends AGameEvent implements ICommand, Serializable
	{
		protected final String playerName;
		
		protected ACommand(String playerName)
		{
			this.playerName = playerName;
		}
		
		public String getPlayerName()
		{
			return playerName;
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
			if ((actualOwner == null && expectedOwner != null) || !actualOwner.equals(expectedOwner)) throw new GameCommandException(this, errorMessage);
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
		
		protected void checkLocation(Location location, IGameConfig config) throws GameCommandException
		{
			if (location.x < 0 || location.x > config.getDimX() || location.y < 0 || location.y > config.getDimY() || location.z < 0 || location.z > config.getDimZ())
			{
				throw new GameCommandException(this, "Invalid location "+location.toString());
			}
		}
		
		/**
		 * Fire events to players (excluding player who issued the current command) for who the given area is visible.
		 */
		@Override
		protected void fireEventForObservers(IGameEventExecutor executor, IArea area, Set<String> players, IGameEvent event)
		{
			players.remove(playerName);
			super.fireEventForObservers(executor, area, players, event);
		}
	}
	
	/**
	 * Update player diplomacy toward another player. Provided target is not the player himself.
	 */
	public static class UpdateDiplomacy extends ACommand implements Serializable
	{
		private final String targetName;
		private final boolean isAllowedToLand;
		private final eForeignPolicy foreignPolicy;
		
		transient private IDiplomacy diplomacy;
		
		public UpdateDiplomacy(String playerName, String targetName, boolean isAllowedToLand, eForeignPolicy foreignPolicy)
		{
			super(playerName);
			this.targetName = targetName;
			this.isAllowedToLand = isAllowedToLand;
			this.foreignPolicy = foreignPolicy;
		}
		
		@Override
		public void check(SEPCommonDB db) throws GameCommandException
		{
			if (playerName.equals(targetName)) throw new GameCommandException(this, "Player cannot set diplomacy toward himself");
			IPlayer player = db.getPlayer(playerName);
			if (player == null) throw new GameCommandException(this, "Unknown player '"+playerName+"'");
			diplomacy = player.getDiplomacy(targetName);
			if (diplomacy == null) throw new GameCommandException(this, "Cannot access '"+playerName+"' diplomacy toward '"+targetName+"'");			
		}
		
		public String getTargetName()
		{
			return targetName;
		}
		
		@Override
		public void process(IGameEventExecutor executor, SEPCommonDB db) throws GameCommandException
		{
			check(db);
			
			diplomacy.setAllowedToLand(isAllowedToLand);
			diplomacy.setForeignPolicy(foreignPolicy);
			
			if (isGlobalView(executor))
			{
				// Fire diplomacy update event for players who currently observe owner government.
				IGovernmentModule governmentalModule = db.getPlayer(playerName).getGovernmentModule();
				IArea area = db.getArea(db.getCelestialBody(governmentalModule.getProductiveCelestialBodyName()).getLocation());
				UpdateDiplomacyMarker diplomacyMarkerUpdate = new UpdateDiplomacyMarker(playerName, targetName, isAllowedToLand, foreignPolicy);				
				fireEventForObservers(executor, area, db.getPlayersNames(), diplomacyMarkerUpdate);
			}
			
			db.fireLog(String.format("%s changed diplomacy toward %s : %s, $diplomacy.foreignPolicy.%s$", playerName, targetName, isAllowedToLand ? "allowed to land" : "not allowed to land", foreignPolicy.toString()));
			db.firePlayerChangeEvent(playerName);			
		}
	}
	
	/**
	 * Update given fleet moves plan. Providing moves are valids (no sun area between to points) and fleet belongs to the player.
	 */
	public static class UpdateFleetMovesPlan extends ACommand implements Serializable
	{
		private final String fleetName;
		private final List<FleetMove> moves = new LinkedList<FleetMove>();
		
		private transient IFleet fleet;
		
		public UpdateFleetMovesPlan(String playerName, String fleetName, List<FleetMove> moves)
		{
			super(playerName);
			this.fleetName = fleetName;
			this.moves.addAll(moves);
		}
		
		public String getFleetName()
		{
			return fleetName;
		}
		
		@Override
		public void check(SEPCommonDB db) throws GameCommandException
		{
			fleet = (IFleet) db.getUnit(playerName, fleetName, eUnitType.Fleet);
			if (fleet == null) throw new GameCommandException(this, "Unknown fleet "+playerName+"@"+fleetName);
				
			String departureName = fleet.isStopped() ? fleet.getDepartureName() : fleet.getDestinationName();
			Location departure = fleet.isStopped() ? fleet.getDeparture() : fleet.getDestination();
			for(FleetMove move : moves)
			{
				ICelestialBody cb = db.getCelestialBody(move.getDestinationName());
				String destinationName = cb.getName();
				Location destination = cb.getLocation();
				
				if (SEPUtils.isTravelingTheSun(db.getConfig(), departure, destination))
				{
					throw new GameCommandException(this, String.format("Incorrect move from %s to %s : there is sun on the way", departureName != null ? departureName : departure.toString(), destinationName != null ? destinationName : destination.toString()));
				}
				
				departureName = destinationName;
				departure = destination;
			}
		}
		
		@Override
		public void process(IGameEventExecutor executor, SEPCommonDB db) throws GameCommandException
		{
			check(db);
			
			fleet.updateMovesPlan(moves);
			
			db.fireLog(String.format("Fleet %s@%s moves plan updated", playerName, fleetName));
			db.fireAreaChangedEvent(fleet.getRealLocation().asLocation());
		}
	}
	
	/**
	 * Fire given anti probe missile to the current location of the given target. Providing the anti probe missile belongs to the player and path is free (no sun area on the way).
	 */
	public static class FireAntiProbeMissile extends ACommand implements Serializable
	{
		private final String antiProbeMissileName;
		private final String targetOwnerName;
		private final String targetName;
		
		private transient IAntiProbeMissile antiProbeMissile;
		private transient IProbeMarker target;
		private transient Location destination;
		
		public FireAntiProbeMissile(String playerName, String antiProbeMissileName, String targetOwnerName, String targetName)
		{
			super(playerName);
			this.antiProbeMissileName = antiProbeMissileName;
			this.targetOwnerName = targetOwnerName;
			this.targetName = targetName;
		}
		
		@Override
		public void check(SEPCommonDB db) throws GameCommandException
		{
			antiProbeMissile = (IAntiProbeMissile) db.getUnit(playerName, antiProbeMissileName, eUnitType.AntiProbeMissile);
			if (antiProbeMissile == null) throw new GameCommandException(this, "Unknown anti probe missile "+playerName+"@"+antiProbeMissileName);
			if (antiProbeMissile.isFired()) throw new GameCommandException(this, "Anti probe missile already fired");
			if (!antiProbeMissile.isStopped()) throw new GameCommandException(this, "Implementation error: Anti probe missile is not stopped nor fired");
			
			IPlayer targetOwner = db.getPlayer(targetOwnerName);
			if (targetOwner == null) throw new GameCommandException(this, "Unknown player '"+targetOwnerName+"'");
			IUnitMarker unitMarker = db.getPlayer(targetOwnerName).getUnitMarker(targetName);
			if (unitMarker == null) throw new GameCommandException(this, "Unknown target "+targetOwnerName+"@"+targetName);
			if (!IProbeMarker.class.isInstance(unitMarker)) throw new GameCommandException(this, "Target must be a Probe");
			
			target = (IProbeMarker) unitMarker;
			if (!target.isDeployed()) throw new GameCommandException(this, "Can only target already deployed probes");			
			
			destination = target.getRealLocation().asLocation();
			checkLocation(destination, db.getConfig());
			
			if (SEPUtils.isTravelingTheSun(db.getConfig(), antiProbeMissile.getDeparture(), destination))
			{
				throw new GameCommandException(this, "Cannot fire antiProbeMissile to "+destination+" because there is sun on the way");
			}			
		}
		
		public IProbeMarker getTarget()
		{
			return target;
		}
		
		@Override
		public void process(IGameEventExecutor executor, SEPCommonDB db) throws GameCommandException
		{
			check(db);
			antiProbeMissile.setDestination(destination);
			antiProbeMissile.setTarget(target);
			
			if (isGlobalView(executor))
			{
				IAntiProbeMissileMarker unitMarker = antiProbeMissile.getMarker(0);
				String markerMessage = String.format("AntiProbeMissile %s@%s fired", antiProbeMissile.getOwnerName(), antiProbeMissile.getName());
				IArea area = db.getArea(antiProbeMissile.getRealLocation().asLocation());
				UnitMoveUpdate umu = new UnitMoveUpdate(unitMarker, markerMessage, null, area);
				fireEventForObservers(executor, area, db.getPlayersNames(), umu);
			}
			
			db.fireLog(String.format("AntiProbeMissile %s fired to %s@%s (%s)", antiProbeMissile.getName(), target.getOwnerName(), target.getName(), destination));
			db.fireAreaChangedEvent(antiProbeMissile.getRealLocation().asLocation());
		}
	}
	
	/**
	 * Launch given probe to given location, providing the probe belongs to the player and the path is free (no sun area on the way).
	 */
	public static class LaunchProbe extends ACommand implements Serializable
	{
		private final String probeName;
		private final Location destination;
		
		private transient IProbe probe;
		
		public LaunchProbe(String playerName, String probeName, Location destination)
		{
			super(playerName);
			this.probeName = probeName;
			this.destination = destination;
		}
		
		@Override
		public void check(SEPCommonDB db) throws GameCommandException
		{
			probe = (IProbe) db.getUnit(playerName, probeName, eUnitType.Probe);
			if (probe == null) throw new GameCommandException(this, "Unknown probe "+playerName+"@"+probeName);
			if (probe.isDeployed()) throw new GameCommandException(this, "Probe is already deployed");
			if (!probe.isStopped()) throw new GameCommandException(this, "Probe has already been launched");
			
			checkLocation(destination, db.getConfig());
			
			if (SEPUtils.isTravelingTheSun(db.getConfig(), probe.getDeparture(), destination))
			{
				throw new GameCommandException(this, "Cannot launch probe to "+destination+" because there is the sun on the way");
			}			
		}
		
		@Override
		public void process(IGameEventExecutor executor, SEPCommonDB db) throws GameCommandException
		{
			check(db);
			probe.setDestination(destination);
			
			if (isGlobalView(executor))
			{
				IProbeMarker unitMarker = probe.getMarker(0);
				String markerMessage = String.format("Probe %s@%s launched", probe.getOwnerName(), probe.getName());
				IArea area = db.getArea(probe.getRealLocation().asLocation());
				UnitMoveUpdate umu = new UnitMoveUpdate(unitMarker, markerMessage, null, area);
				fireEventForObservers(executor, area, db.getPlayersNames(), umu);
			}
			
			db.fireLog(String.format("Probe %s launched to %s", probe.getName(), destination));
			db.fireAreaChangedEvent(probe.getRealLocation().asLocation());
		}
	}
	
	/**
	 * Makes a anti probe missile serie on given productive celestial body, which must belongs to the player.
	 * Made anti probe missiles are numbered from the last serial number of the given serie (1 if it's a new one).
	 */
	public static class MakeAntiProbeMissile extends ACommand implements Serializable
	{
		private final String productiveCelestialBodyName;
		private final String serieName;
		private final int quantity;
		
		private transient IProductiveCelestialBody productiveCelestialBody;
		private transient int lastSerialNumber;
		private transient int carbonCost;
		private transient int populationCost;
		private transient IPlanet planet;
		
		public MakeAntiProbeMissile(String playerName, String productiveCelestialBodyName, String serieName, int quantity)
		{
			super(playerName);
			this.productiveCelestialBodyName = productiveCelestialBodyName;
			this.serieName = serieName;
			this.quantity = quantity;
		}
		
		@Override
		public void check(SEPCommonDB db) throws GameCommandException
		{
			if (quantity <= 0) throw new GameCommandException(this, "Incorrect quantity ("+quantity+"), must be greater than 0");
			
			if (serieName == null || serieName.isEmpty()) throw new GameCommandException(this, "Incorrect serie name");
			
			productiveCelestialBody = checkProductiveCelestialBody(db, productiveCelestialBodyName);			
			checkOwnership(playerName, productiveCelestialBody.getOwner(), playerName+" does not own "+productiveCelestialBodyName);
			
			IStarshipPlant starshipPlant = (IStarshipPlant) productiveCelestialBody.getBuilding(eBuildingType.StarshipPlant);
			if (starshipPlant == null) throw new GameCommandException(this, "Cannot find "+eBuildingType.StarshipPlant+" on '"+productiveCelestialBodyName+"'");			
			
			lastSerialNumber = 0;
			for(SEPCommonDB pDB = db ; pDB.hasPrevious(); pDB = pDB.previous())
			{
				for(IAntiProbeMissile apm : pDB.getAntiProbeMissileSerie(playerName, serieName))
				{
					lastSerialNumber = Math.max(lastSerialNumber, apm.getSerialNumber());
				}
				if (lastSerialNumber != 0) break;
			}			
			
			carbonCost = Rules.antiProbeMissileCarbonPrice * quantity;
			populationCost = Rules.antiProbeMissilePopulationPrice * quantity;						
			
			checkCanAfford(productiveCelestialBody, carbonCost, populationCost);
			planet = (IPlanet.class.isInstance(productiveCelestialBody)) ? (IPlanet) productiveCelestialBody : null;
		}
		
		public int getLastSerialNumber()
		{
			return lastSerialNumber;
		}
		
		@Override
		public void process(IGameEventExecutor executor, SEPCommonDB db) throws GameCommandException
		{
			check(db);
			
			if (carbonCost > 0) productiveCelestialBody.payCarbon(carbonCost);
			if (populationCost > 0) planet.payPopulation(populationCost);
			
			for(int i = 0; i < quantity; ++i)
			{
				int serialNumber = lastSerialNumber+i+1;
				IAntiProbeMissile antiProbeMissile = db.createAntiProbeMissile(db.makeAntiProbeMissile(playerName, serieName, serialNumber, productiveCelestialBodyName));
				
				if (isGlobalView(executor))
				{
					IArea area = db.getArea(productiveCelestialBody.getLocation());
					UnitMade unitMade = new UnitMade(antiProbeMissile.getMarker(0), area);
					fireEventForObservers(executor, area, db.getPlayersNames(), unitMade);
				}
			}			
			
			db.fireLog(String.format("Produced anti probe missile serie %s [%d - %d]", serieName, lastSerialNumber+1, lastSerialNumber+quantity));
			db.fireAreaChangedEvent(productiveCelestialBody.getLocation());
		}
	}
	
	/**
	 * Makes a probe serie on given productive celestial body, which must belongs to the player.
	 * Made probes are numbered from the last serial number of the given serie (1 if it's a new one).
	 */
	public static class MakeProbes extends ACommand implements Serializable
	{
		private final String productiveCelestialBodyName;
		private final String serieName;
		private final int quantity;
		
		private transient IProductiveCelestialBody productiveCelestialBody;
		private transient int lastSerialNumber;
		private transient int carbonCost;
		private transient int populationCost;
		private transient IPlanet planet;
		
		public MakeProbes(String playerName, String productiveCelestialBodyName, String serieName, int quantity)
		{
			super(playerName);
			this.productiveCelestialBodyName = productiveCelestialBodyName;
			this.serieName = serieName;
			this.quantity = quantity;
		}
		
		@Override
		public void check(SEPCommonDB db) throws GameCommandException
		{
			if (quantity <= 0) throw new GameCommandException(this, "Incorrect quantity ("+quantity+"), must be greater than 0");
			
			if (serieName == null || serieName.isEmpty()) throw new GameCommandException(this, "Incorrect serie name");
			
			productiveCelestialBody = checkProductiveCelestialBody(db, productiveCelestialBodyName);			
			checkOwnership(playerName, productiveCelestialBody.getOwner(), playerName+" does not own "+productiveCelestialBodyName);
			
			IStarshipPlant starshipPlant = (IStarshipPlant) productiveCelestialBody.getBuilding(eBuildingType.StarshipPlant);
			if (starshipPlant == null) throw new GameCommandException(this, "Cannot find "+eBuildingType.StarshipPlant+" on '"+productiveCelestialBodyName+"'");			
			
			lastSerialNumber = 0;
			for(SEPCommonDB pDB = db ; pDB.hasPrevious(); pDB = pDB.previous())
			{
				for(IProbe probe : pDB.getProbeSerie(playerName, serieName))
				{
					lastSerialNumber = Math.max(lastSerialNumber, probe.getSerialNumber());
				}
				if (lastSerialNumber != 0) break;
			}			
			
			carbonCost = Rules.probeCarbonPrice * quantity;
			populationCost = Rules.probePopulationPrice * quantity;						
			
			checkCanAfford(productiveCelestialBody, carbonCost, populationCost);
			planet = (IPlanet.class.isInstance(productiveCelestialBody)) ? (IPlanet) productiveCelestialBody : null;
		}
		
		public int getLastSerialNumber()
		{
			return lastSerialNumber;
		}
		
		@Override
		public void process(IGameEventExecutor executor, SEPCommonDB db) throws GameCommandException
		{
			check(db);
			
			if (carbonCost > 0) productiveCelestialBody.payCarbon(carbonCost);
			if (populationCost > 0) planet.payPopulation(populationCost);
			
			for(int i = 0; i < quantity; ++i)
			{
				int serialNumber = lastSerialNumber+i+1;
				IProbe probe = db.createProbe(db.makeProbe(playerName, serieName, serialNumber, productiveCelestialBodyName));
				
				if (isGlobalView(executor))
				{
					IArea area = db.getArea(productiveCelestialBody.getLocation());
					UnitMade unitMade = new UnitMade(probe.getMarker(0), area);
					fireEventForObservers(executor, area, db.getPlayersNames(), unitMade);
				}
			}
			
			db.fireLog(String.format("Produced probe serie %s [%d - %d]", serieName, lastSerialNumber+1, lastSerialNumber+quantity));
			db.fireAreaChangedEvent(productiveCelestialBody.getLocation());
		}
	}
	
	/**
	 * Dismantle given fleet. i.e. the given fleet merge the current location player's unassigned fleet.
	 * TODO: DismantleFleet should cancel previous AssignStarships command for same fleet same turn.
	 */
	public static class DismantleFleet extends ACommand implements Serializable
	{
		private final String productiveCelestialBodyName;
		private final String fleetName;
		
		private transient IProductiveCelestialBody productiveCelestialBody;
		private transient IFleet dismantledFleet;
		
		public DismantleFleet(String playerName, String productiveCelestialBodyName, String fleetName)
		{
			super(playerName);
			this.productiveCelestialBodyName = productiveCelestialBodyName;
			this.fleetName = fleetName;			
		}
		
		@Override
		public void check(SEPCommonDB db) throws GameCommandException
		{
			productiveCelestialBody = checkProductiveCelestialBody(db, productiveCelestialBodyName);
			dismantledFleet = db.getFleet(playerName, fleetName);
			if (dismantledFleet == null) throw new GameCommandException(this, "Unknown fleet "+playerName+"@"+fleetName);
			if (!dismantledFleet.isStopped()) throw new GameCommandException(this, "Fleet is not stopped");
			if (!productiveCelestialBodyName.equals(dismantledFleet.getDepartureName())) throw new GameCommandException(this, "Unexpected fleet location");						
		}
		
		@Override
		public void process(IGameEventExecutor executor, SEPCommonDB db) throws GameCommandException
		{
			check(db);
			
			IFleet assignedFleet = db.getCreateAssignedFleet(productiveCelestialBodyName, playerName);
			Map<StarshipTemplate, Integer> starships = dismantledFleet.getStarships();
			
			assignedFleet.addStarships(starships);
			IFleetMarker dismantledFleetMarker = dismantledFleet.getMarker(0);
			dismantledFleet.destroy();
			
			if (isGlobalView(executor))
			{
				IArea area = db.getArea(productiveCelestialBody.getLocation());
				UnitDismantled unitDismantled = new UnitDismantled(dismantledFleetMarker, area);
				fireEventForObservers(executor, area, db.getPlayersNames(), unitDismantled);
			}
			
			db.fireLog(String.format("Fleet %s dismantled on %s", fleetName, productiveCelestialBodyName));
			db.fireAreaChangedEvent(productiveCelestialBody.getLocation());
		}
	}
	
	/**
	 * Assign starships from the given productive celestial body to the named fleet, providing quantity is sufficient and productive celestial body belongs to the player.
	 */
	public static class AssignStarships extends ACommand implements Serializable
	{
		private final String productiveCelestialBodyName;
		private final Map<String, Integer> starships = new HashMap<String, Integer>();
		private final String fleetName;
		
		private transient IProductiveCelestialBody productiveCelestialBody;
		private transient IFleet assignedFleet;
		private transient IFleet destinationFleet;
		private transient Map<StarshipTemplate, Integer> newcomers;
		
		public AssignStarships(String playerName, String productiveCelestialBodyName, Map<String, Integer> starships, String fleetName)
		{
			super(playerName);
			this.productiveCelestialBodyName = productiveCelestialBodyName;
			this.starships.putAll(starships);
			this.fleetName = fleetName;
		}
		
		@Override
		public void check(SEPCommonDB db) throws GameCommandException
		{
			productiveCelestialBody = checkProductiveCelestialBody(db, productiveCelestialBodyName);
			//checkOwnership(playerName, productiveCelestialBody.getOwner(), playerName+" does not own "+productiveCelestialBodyName);
			
			assignedFleet = productiveCelestialBody.getAssignedFleet(playerName);
			if (assignedFleet == null) throw new GameCommandException(this, "No starships available on "+productiveCelestialBodyName);
			Map<StarshipTemplate, Integer> assignedStarships = assignedFleet.getStarships();
			
			newcomers = new HashMap<StarshipTemplate, Integer>();
			boolean empty = true;
			for(String templateName : starships.keySet())
			{
				StarshipTemplate template = Rules.getStarshipTemplate(templateName);
				int quantity = starships.get(templateName);
				
				if (quantity < 0) throw new GameCommandException(this, "Cannot assign negative quantity of starships ("+templateName+")");
								
				int availableQuantity = (assignedStarships.containsKey(template) ? assignedStarships.get(template) : 0);
				if (quantity > availableQuantity) throw new GameCommandException(this, "Not enough starships ("+templateName+")");
				
				if (quantity > 0)
				{
					newcomers.put(template, quantity);
					empty = false;
				}
			}
			
			if (empty) throw new GameCommandException(this, "No newcomer");
			if (fleetName == null || fleetName.isEmpty()) throw new GameCommandException(this, "Incorrect fleet name");			
			
			destinationFleet = db.getFleet(playerName, fleetName);
			
			if (destinationFleet == null)
			{
				// Check that fleetName has never been used previously
				
				for(SEPCommonDB pDB = db ; pDB.hasPrevious(); pDB = pDB.previous())
				{
					destinationFleet = pDB.getFleet(playerName, fleetName);
					if (destinationFleet != null) throw new GameCommandException(this, "Fleet name reserved, '"+fleetName+"' has been destroyed on turn "+destinationFleet.getTurn()+". Its name cannot be used again.");
				}
			}
			else
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
				destinationFleet = db.createFleet(db.makeFleet(playerName, fleetName, productiveCelestialBodyName, newcomers));
			}
			else
			{
				destinationFleet.addStarships(newcomers);				
			}
			
			assignedFleet.removeStarships(newcomers);
			
			if (isGlobalView(executor))
			{
				IArea area = db.getArea(productiveCelestialBody.getLocation());
				UnitMade unitMade = new UnitMade(assignedFleet.getMarker(0), area, newcomers);
				fireEventForObservers(executor, area, db.getPlayersNames(), unitMade);
			}
			
			db.fireLog(String.format("Starships assigned to %s", destinationFleet.getName()));
			db.fireAreaChangedEvent(destinationFleet.getRealLocation().asLocation());
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
		private transient int totalQuantity;
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
			totalQuantity = 0;
			
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
				
				totalQuantity += quantity;
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
			
			if (isGlobalView(executor))
			{
				IArea area = db.getArea(productiveCelestialBody.getLocation());
				UnitMade unitMade = new UnitMade(assignedFleet.getMarker(0), area, newcomers);
				fireEventForObservers(executor, area, db.getPlayersNames(), unitMade);
			}
			
			db.fireLog(String.format("Made %d new starships on %s", totalQuantity, productiveCelestialBodyName));
			db.fireAreaChangedEvent(productiveCelestialBody.getLocation());
		}
	}
	
	/**
	 * Demolish/downgrade slot of the given building type on given productive celestial body which must be owned by given player.
	 */
	public static class Demolish extends ACommand implements Serializable
	{
		private final String productiveCelestialBodyName;
		private final eBuildingType buildingType;
		
		private transient IProductiveCelestialBody productiveCelestialBody;
		private transient IPlanet planet;
		private transient IBuilding existingBuilding;
		private transient int turn;
		
		public Demolish(String playerName, String productiveCelestialBodyName, eBuildingType buildingType)
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
			
			turn = db.getTurn();
			existingBuilding = productiveCelestialBody.getBuilding(buildingType);
			
			
			if (existingBuilding == null || existingBuilding.getNbSlots() == 0) throw new GameCommandException(this, "Cannot find '"+buildingType+"' on '"+productiveCelestialBodyName);
			if (eBuildingType.GovernmentModule == existingBuilding.getType())
			{
				throw new GameCommandException(this, "Cannot demolish '"+buildingType+"'. Try to embark government on governmental starship.");
			}
			if (existingBuilding.getBuiltDate() >= turn) throw new GameCommandException(this, "Cannot demolish '"+existingBuilding.getType()+"' because it was build on current turn.");						
			
			if (existingBuilding != null && !Rules.getBuildingCanBeDowngraded(existingBuilding))
			{
				throw new GameCommandException(this, buildingType+" cannot be downgraded.");
			}			
			
			planet = (IPlanet.class.isInstance(productiveCelestialBody)) ? (IPlanet) productiveCelestialBody : null;
		}
		
		@Override
		public void process(IGameEventExecutor executor, SEPCommonDB db) throws GameCommandException
		{
			check(db);			
			
			final String message;
			
			if (existingBuilding.getNbSlots() == 1)
			{
				existingBuilding.demolish();
				message = String.format("%s demolished on %s", buildingType, productiveCelestialBodyName);
				
			}
			else
			{
				existingBuilding.downgrade();
				message = String.format("%s downgraded on %s", buildingType, productiveCelestialBodyName);
			}
			
			if (isGlobalView(executor))
			{
				IArea area = db.getArea(productiveCelestialBody.getLocation());
				fireEventForObservers(executor, area, db.getPlayersNames(), new LogMessage(message, area));
			}
				
			db.fireLog(message);
			db.fireAreaChangedEvent(productiveCelestialBody.getLocation());
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
			
			turn = db.getTurn();
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
			
			String message = String.format("%s built on %s", buildingType, productiveCelestialBodyName);
			
			if (isGlobalView(executor))
			{
				IArea area = db.getArea(productiveCelestialBody.getLocation());
				fireEventForObservers(executor, area, db.getPlayersNames(), new LogMessage(message, area));
			}
			
			db.fireLog(message);
			db.fireAreaChangedEvent(productiveCelestialBody.getLocation());
		}
	}
}
