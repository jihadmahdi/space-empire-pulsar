package org.axan.sep.common.db;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.axan.sep.common.Protocol.eBuildingType;
import org.axan.sep.common.SEPUtils;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.orm.Area;
import org.axan.sep.common.db.orm.Building;
import org.axan.sep.common.db.orm.CelestialBody;
import org.axan.sep.common.db.orm.Government;
import org.axan.sep.common.db.orm.GovernmentModule;

public class EvCreateUniverse implements IGameEvent
{
	final private Location sunLocation;
	final private Map<IPlayer, IPlayerConfig> players;
	final private Set<ICelestialBody> celestialBodies;
	
	public EvCreateUniverse(Location sunLocation, Map<IPlayer, IPlayerConfig> players, Set<ICelestialBody> celestialBodies)
	{
		this.sunLocation = sunLocation;
		this.players = players;
		this.celestialBodies = celestialBodies;
	}
	
	public Location getSunLocation()
	{
		return sunLocation;
	}
	
	@Override
	public void process(IGameEventExecutor executor, SEPCommonDB db) throws GameEventException
	{
		try
		{
			// Generate Universe	
			// Make the sun
			IGameConfig config = db.getConfig();
	
			for(int x = -Math.min(config.getSunRadius(), sunLocation.x); x <= Math.min(config.getSunRadius(), sunLocation.x); ++x)
				for(int y = -Math.min(config.getSunRadius(), sunLocation.y); y <= Math.min(config.getSunRadius(), sunLocation.y); ++y)
					for(int z = -Math.min(config.getSunRadius(), sunLocation.z); z <= Math.min(config.getSunRadius(), sunLocation.z); ++z)
					{
						Location parsedLoc = new Location(sunLocation.x + x, sunLocation.y + y, sunLocation.z + z);
						if (SEPUtils.getDistance(parsedLoc, sunLocation) <= config.getSunRadius())
						{
							Area.insertOrUpdate(db, new Area(parsedLoc, true));
						}
					}
			
			Map<String, IPlanet> playerStartingPlanets = new HashMap<String, IPlanet>();
			for(ICelestialBody celestialBody : celestialBodies)
			{
				Area.insertOrUpdate(db, new Area(celestialBody.getLocation(), false));
				CelestialBody.insertOrUpdate(db, celestialBody);
				
				if (IPlanet.class.isInstance(celestialBody))
				{
					IPlanet planet = (IPlanet) celestialBody;
					if (planet.getOwner() != null)
					{
						for(IPlayer player : players.keySet())
						{						
							if (player.getName().equals(planet.getOwner()) && !playerStartingPlanets.containsKey(player.getName()))
							{
								// Player starting planet
								playerStartingPlanets.put(player.getName(), planet);
								
								// If victory rule "Regimicide" is on, starting planet has a pre-built government module.
								if (config.isRegimicide())
								{
									IGovernmentModule governmentModule = new GovernmentModule(eBuildingType.GovernmentModule, planet.getName(), config.getTurn(), 1);
									IGovernment government = new Government(player.getName(), null, planet.getName());
									
									Building.insertOrUpdate(db, governmentModule);
									Government.insertOrUpdate(db, government);
								}
							}
						}
					}
				}
			}
			
			config.setTurn(1);
		}
		catch(Throwable t)
		{
			throw new GameEventException(this, t);
		}
	}

}
