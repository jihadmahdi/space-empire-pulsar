package org.axan.sep.common.db;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.axan.eplib.orm.sql.SQLDataBaseException;
import org.axan.sep.common.Protocol.eBuildingType;
import org.axan.sep.common.SEPUtils;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.orm.SEPCommonDB;

public class EvCreateUniverse implements IGameEvent, Serializable
{
	final private Location sunLocation;
	final private Map<IPlayer, IPlayerConfig> players;
	final private Map<Location, ICelestialBody> celestialBodies;
	final private Map<IProductiveCelestialBody, IPlayer> ownershipRelations;
	
	public EvCreateUniverse(Location sunLocation, Map<IPlayer, IPlayerConfig> players, Map<Location, ICelestialBody> celestialBodies, Map<IProductiveCelestialBody, IPlayer> ownershipRelations)
	{
		this.sunLocation = sunLocation;
		this.players = players;
		this.celestialBodies = celestialBodies;
		this.ownershipRelations = ownershipRelations;
	}
	
	public Location getSunLocation()
	{
		return sunLocation;
	}
	
	/**
	 * Check if current event need to be processed or already has been (for optimization concerns).
	 * @param db
	 * @return
	 * @throws SQLDataBaseException 
	 */
	private boolean skipCondition(SEPCommonDB db) throws SQLDataBaseException
	{
		// We cannot rely on GameConfig to check if universe has already been created, so we just check if there already is any Area instered.
		return db.isUniverseCreated();
	}
	
	@Override
	public void process(IGameEventExecutor executor, SEPCommonDB db) throws GameEventException
	{
		// TODO: Recode GameBoard#createUniverse and EvCReateUniverse to remove unnecessary parameters (celestial body locations, owners, ...) that now can be retrieved from off-db objects getters.
		
		try
		{
			IGameConfig config = db.getConfig();
			
			if (skipCondition(db)) return;
			
			// Generate players
			for(IPlayer player : players.keySet())
			{
				db.createPlayer(player);
			}
			
			// Generate Universe
			/*
			for(int x=0; x < config.getDimX(); ++x)
				for(int y=0; y < config.getDimY(); ++y)
					for(int z=0; z < config.getDimZ(); ++z)
					{
						Area.insertOrUpdate(db, new Area(new Location(x, y, z), false));
					}
			*/
			
			// Make the sun
			for(int x = -Math.min(config.getSunRadius(), sunLocation.x); x <= Math.min(config.getSunRadius(), sunLocation.x); ++x)
				for(int y = -Math.min(config.getSunRadius(), sunLocation.y); y <= Math.min(config.getSunRadius(), sunLocation.y); ++y)
					for(int z = -Math.min(config.getSunRadius(), sunLocation.z); z <= Math.min(config.getSunRadius(), sunLocation.z); ++z)						
					{
						Location parsedLoc = new Location(sunLocation.x + x, sunLocation.y + y, sunLocation.z + z);
						if (SEPUtils.getDistance(parsedLoc, sunLocation) <= config.getSunRadius())
						{
							db.createArea(parsedLoc, true);
						}						
					}
			
			Map<String, IPlanet> playerStartingPlanets = new HashMap<String, IPlanet>();
			for(Location location : celestialBodies.keySet())
			{
				ICelestialBody celestialBody = celestialBodies.get(location);
				
				if (!location.equals(celestialBody.getLocation()))
				{
					throw new RuntimeException("Implementation error, mapped location differs from celestial body location.");
				}
				
				ICelestialBody connectedCelestialBody = db.createCelestialBody(celestialBody);
				
				if (ownershipRelations.containsKey(celestialBody))
				{
					IPlayer player = ownershipRelations.get(celestialBody);
					
					((IProductiveCelestialBody) connectedCelestialBody).setOwner(player.getName());
					
					if (IPlanet.class.isInstance(celestialBody) && !playerStartingPlanets.containsKey(player.getName()))
					{
						IPlanet planet = (IPlanet) celestialBody;
						
						// Player starting planet
						playerStartingPlanets.put(player.getName(), planet);
						
						// If victory rule "Regimicide" is on, starting planet has a pre-built government module.						
						if (config.isRegimicide())
						{
							IGovernmentModule governmentModule = SEPCommonDB.makeGovernmentModule(planet.getName(), config.getTurn(), 1);
							governmentModule = db.createGovernmentModule(governmentModule);							
						}
					}
				}				
			}
			
			config.setTurn(0);
		}
		catch(Throwable t)
		{
			throw new GameEventException(this, t);
		}
	}

}
