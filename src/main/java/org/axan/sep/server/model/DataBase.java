package org.axan.sep.server.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.axan.sep.common.ALogEntry;
import org.axan.sep.common.GameConfig;
import org.axan.sep.common.Player;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.SEPUtils.RealLocation;
import org.axan.sep.server.model.Area.AreaIllegalDefinitionException;
import org.axan.sep.server.model.SpaceCounter.SpaceRoad;

class DataBase implements Serializable
{
	//SUIS LA, recoder DataBase avec SQLite.
	
	public static class DataBaseError extends Error
	{
		private static final long	serialVersionUID	= 1L;
		
		public DataBaseError(String msg)
		{
			super(msg);
		}
		
		public DataBaseError(Throwable t)
		{
			super(t);
		}
	}
	
	private static final long			serialVersionUID	= 1L;
	
	private final Hashtable<String, org.axan.sep.common.Player> players;
	
	private final Map<String, Diplomacy>	playersPolicies;
	
	private final Map<String, SortedSet<ALogEntry>>	playersLogs;

	private final Hashtable<Location, Area>	areas;
	
	private final Hashtable<ICelestialBody.Key, ICelestialBody> celestialBodies;
	
	private final Hashtable<Unit.Key, Unit> units;
	
	private final Hashtable<String, Hashtable<IMarker.Key, IMarker>> playersMarkers;
	
	private final RealLocation			sunLocation;							// Sun center location : [0] x; [1] y; [2] z. Sun is always fill 9 area.

	private final org.axan.sep.common.GameConfig		config;

	private int							date;
	
	public DataBase(Hashtable<String, org.axan.sep.common.Player> players, org.axan.sep.common.GameConfig config, int date, Hashtable<Location, Area> areas, Hashtable<ICelestialBody.Key, ICelestialBody> celestialBodies, Hashtable<String, Hashtable<IMarker.Key, IMarker>> playersMarkers, RealLocation sunLocation, Hashtable<Unit.Key, Unit> units, Map<String, Diplomacy> playersPolicies, Map<String, SortedSet<ALogEntry>> playersLogs)
	{
		this.players = players;
		this.config = config;
		this.date = date;
		this.areas = areas;
		this.celestialBodies = celestialBodies;
		this.units = units;
		this.playersMarkers = playersMarkers;
		this.sunLocation = sunLocation;
		this.playersPolicies = playersPolicies;
		this.playersLogs = playersLogs;
	}
	
	/// INSERT

	public void insertPlayer(Player player)
	{
		players.put(player.getName(), player);
		if (!playersMarkers.containsKey(player.getName()))
		{
			playersMarkers.put(player.getName(), new Hashtable<IMarker.Key, IMarker>());			
			playersPolicies.put(player.getName(), new Diplomacy(this, player.getName()));
		}		
	}	

	public void writeLog(String playerName, ALogEntry logEntry)
	{
		if (!playersLogs.containsKey(playerName)) playersLogs.put(playerName, new TreeSet<ALogEntry>());
		ALogEntry.addUpdateLogEntry(playersLogs.get(playerName), logEntry);		
	}
	
	public SortedSet<ALogEntry> getPlayerLogs(String playerName)
	{
		if (!playersLogs.containsKey(playerName)) playersLogs.put(playerName, new TreeSet<ALogEntry>());
		return playersLogs.get(playerName);
	}
	
	public void insertCelestialBody(ICelestialBody celestialBody)
	{
		if (celestialBodies.containsKey(celestialBody.getKey())) throw new DataBaseError("Already contains celestial body '"+celestialBody.getKey()+"'");
		
		celestialBodies.put(celestialBody.getKey(), celestialBody);
		try
		{
			getCreateArea(celestialBody.getLocation()).setCelestialBody(celestialBody.getName());
		}
		catch(AreaIllegalDefinitionException e)
		{
			throw new DataBaseError(e);
		}
	}
	
	public void insertUnit(Unit unit)
	{
		if (units.containsKey(unit.getKey())) throw new DataBaseError("Already contains unit '"+unit.getKey()+"'");
		
		units.put(unit.getKey(), unit);
	}
	
	public Area getCreateArea(Location location)
	{
		if (!areas.containsKey(location))
		{
			areas.put(location, new Area(this, location));
		}
		return areas.get(location);
	}
	
	public void insertMarker(String playerName, IMarker marker)
	{
		Hashtable<IMarker.Key, IMarker> playerMarkers = getPlayerMarkers(playerName);
		if (playerMarkers.containsKey(marker.getKey())) throw new DataBaseError("Already contains player '"+playerName+"''s marker '"+marker.getKey()+"'");
		
		playerMarkers.put(marker.getKey(), marker);
	}
	
	/// UPDATE
	
	public void incDate()
	{
		++date;
	}
	
	/// DELETE
	
	public void removeMarker(String playerName, IMarker.Key key)
	{
		Hashtable<IMarker.Key, IMarker> playerMarkers = getPlayerMarkers(playerName);		
		playerMarkers.remove(key);
	}
	
	public void removeUnit(Unit.Key key)
	{
		units.remove(key);
	}
	
	/// SELECT
	
	public RealLocation getSunLocation()
	{
		return sunLocation;
	}
	
	public <C extends ICelestialBody> Set<C> getCelestialBodies(Class<C> celestialBodyType)
	{
		Set<C> result = new HashSet<C>();
		for(ICelestialBody celestialBody : celestialBodies.values())
		{
			if (celestialBodyType.isInstance(celestialBody))
			{
				result.add(celestialBodyType.cast(celestialBody));
			}
		}
		
		return result;
	}

	public Hashtable<IMarker.Key, IMarker> getPlayerMarkers(String playerName)
	{
		if (!playersMarkers.containsKey(playerName))
		{
			playersMarkers.put(playerName, new Hashtable<IMarker.Key, IMarker>());
		}
		
		return playersMarkers.get(playerName);
	}
	
	public Area getArea(Location location)
	{
		return areas.get(location);
	}
	
	public GameConfig getGameConfig()
	{
		return config;
	}
	
	public int getDate()
	{
		return date;
	}
	
	public Set<String> getPlayersKeySet()
	{
		return players.keySet();
	}
	
	public Set<ICelestialBody.Key> getCelestialBodiesSet()
	{
		return  celestialBodies.keySet();
	}
	
	public Diplomacy getPlayerPolicies(String playerName)
	{
		if (!playersPolicies.containsKey(playerName) && playerExists(playerName))
		{
			playersPolicies.put(playerName, new Diplomacy(this, playerName));
		}
		
		return playersPolicies.get(playerName);
	}
	
	UnitMarker getUnitMarker(String observerName, String ownerName, String unitName)
	{
		for(UnitMarker um : getMarkers(observerName, UnitMarker.class))
		{
			org.axan.sep.common.Unit u = um.getUnit();
			
			// Owner filter
			if (!ownerName.equals(u.getOwnerName())) continue;
			
			// Name filter
			if (unitName.equals(u.getName())) return um;
		}
		
		return null;
	}
	
	Set<UnitMarker> getUnitMarkers(String observerName, Location location)
	{
		Set<UnitMarker> result = new HashSet<UnitMarker>();
		
		for(UnitMarker um : getMarkers(observerName, UnitMarker.class))
		{
			if (um.getUnit().getCurrentLocation().asLocation().equals(location))
			{
				result.add(um);
			}
		}
		
		return result;
	}
	
	private <M extends IMarker> Set<M> getMarkers(String observerName, Class<M> markerType)
	{
		Set<M> result = new HashSet<M>();
		if (playersMarkers.containsKey(observerName)) for(IMarker m : playersMarkers.get(observerName).values())
		{
			if (markerType.isInstance(m)) result.add(markerType.cast(m));
		}
		
		return result;
	}

	protected <U extends Unit> U getUnit(Location location, Class<U> unitType, String ownerName, String unitName)
	{
		U unit = getUnit(unitType, ownerName, unitName);
		if (unit == null || !unit.getRealLocation().asLocation().equals(location)) return null;
		return unit;
	}
	
	/**
	 * Return the unit by its name, type, and owner.
	 * 
	 * @param <U>
	 * @param unitType
	 * @param playerLogin
	 * @param unitName
	 * @return
	 */
	protected <U extends Unit> U getUnit(Class<U> unitType, String ownerName, String unitName)
	{
		Unit u = units.get(new Unit.Key(unitName, ownerName));
		if (u != null && unitType.isInstance(u))
		{
			return unitType.cast(u);
		}
		
		return null;
	}

	/**
	 * Return all units that implement a given unit sub class.
	 * 
	 * @param unitTypeFilter
	 *            if not null, return only units that implement this unit sub
	 *            class.
	 * @param playerLoginFilter
	 *            if not null, return only units owned by this player.
	 * @return Map<int[], Set<U extends Unit>> filtered unit from the entire
	 *         universe.
	 * @see #getUnits(int[], String, Class)
	 */
	public <U extends Unit> Set<U> getUnits(Class<U> unitType, String playerLogin)
	{
		Set<U> filteredUnits = new HashSet<U>();

		for(Unit unit : units.values())
		{
			if (playerLogin.equals(unit.getOwnerName()) && unitType.isInstance(unit))
			{
				filteredUnits.add(unitType.cast(unit));
			}
		}

		return filteredUnits;
	}

	/**
	 * Return units that implement a given unit sub class and are located in a
	 * specific location .
	 * 
	 * @param location
	 *            Location to look units for.
	 * @param unitTypeFilter
	 *            (cannot not be null) return only units that implement this
	 *            unit sub class.
	 * @param playerLoginFilter
	 *            return only units owned by this player.
	 * @return Set<Unit> filtered unit for the given location.
	 * @see #getUnits(String, Class)
	 */
	public <U extends Unit> Set<U> getUnits(Location location, Class<U> unitTypeFilter, String playerLoginFilter)
	{
		Set<U> filteredUnits = new HashSet<U>();
		
		for(U u : getUnits(unitTypeFilter, playerLoginFilter))
		{
			if (u.getRealLocation().asLocation().equals(location))
			{
				filteredUnits.add(u);
			}
		}
		
		return filteredUnits;
	}

	/**
	 * Return units from a specific location. Optional filters can apply.
	 * 
	 * @param location
	 *            Location to look units for.
	 * @param playerLoginFilter
	 *            return only units owned by this player.
	 * @return
	 */
	public Set<Unit> getUnits(Location location, String ownerName)
	{
		Set<Unit> filteredUnits = new HashSet<Unit>();

		for(Unit u : getUnits(location))
		{
			if (ownerName.equals(u.getOwnerName()))
			{
				filteredUnits.add(u);
			}
		}
		
		return filteredUnits;
	}
	
	public Set<Unit> getUnits()
	{
		return new HashSet<Unit>(units.values());
	}
	
	public <U extends Unit> Set<U> getUnits(Location location, Class<U> unitTypeFilter)
	{
		Set<U> filteredUnits = new HashSet<U>();
		
		for(Unit u : getUnits(location))
		{
			if (unitTypeFilter.isInstance(u)) filteredUnits.add(unitTypeFilter.cast(u));
		}
		
		return filteredUnits;
	}
	
	protected Set<Unit> getUnits(Location location)
	{
		Set<Unit> filteredUnits = new HashSet<Unit>();
		
		for(Unit u : units.values())
		{
			if (u.getRealLocation().asLocation().equals(location))
			{
				filteredUnits.add(u);
			}
		}
		
		return filteredUnits;
	}
	
	protected <C extends ICelestialBody> C getCelestialBody(String celestialBodyName, Class<C> celestialBodyType, String ownerName)
	{
		C celestialBody = getCelestialBody(celestialBodyName, celestialBodyType);
		if (!ownerName.equals(celestialBody.getOwnerName())) return null;
		return celestialBody;
	}
	
	public <C extends ICelestialBody> C getCelestialBody(Location location, Class<C> celestialBodyType)
	{
		return getCelestialBody(getArea(location).getCelestialBodyName(), celestialBodyType);
	}
	
	protected <C extends ICelestialBody> C getCelestialBody(String celestialBodyName, Class<C> celestialBodyType)
	{
		ICelestialBody celestialBody = getCelestialBody(celestialBodyName);
		if (!celestialBodyType.isInstance(celestialBody)) return null;		
		return celestialBodyType.cast(celestialBody);
	}
	
	protected ICelestialBody getCelestialBody(String celestialBodyName)
	{
		if (celestialBodyName == null) return null;
		return celestialBodies.get(new ICelestialBody.Key(celestialBodyName));
	}
	
	protected boolean celestialBodyExists(ICelestialBody.Key celestialBodyKey)
	{
		return celestialBodies.containsKey(celestialBodyKey);
	}
	
	public Set<Probe> getDeployedProbes()
	{
		Set<Probe> result = new HashSet<Probe>();

		for(Unit u : units.values())
		{
			if (!Probe.class.isInstance(u)) continue;
			Probe p = Probe.class.cast(u);
			if (p.isDeployed())
			{
				result.add(p);
			}			
		}

		return result;
	}
	
	public Player getPlayer(String playerName)
	{
		return players.get(playerName);
	}
	
	/// SELECT+

	public boolean playerExists(String playerName)
	{
		return players.containsKey(playerName);
	}
	
	/**
	 * Return the area where the given player government module is located.
	 * 
	 * @param playerLogin
	 * @return
	 */
	Planet locateGovernmentModule(String playerLogin)
	{
		for(ICelestialBody celestialBody : celestialBodies.values())
		{
			if (!Planet.class.isInstance(celestialBody)) continue;
			Planet planet = Planet.class.cast(celestialBody);

			if (!playerLogin.equals(planet.getOwnerName())) continue;

			if (planet.isGovernmentSettled()) return planet;
		}

		return null;
	}

	public static class genericResultSet<C extends ICelestialBody, U extends Unit, B extends ABuilding>
	{
		public final Area area;
		public final C celestialBody;
		public final Player player;
		public final U unit;
		public final B building;
		
		public genericResultSet(Area area, C celestialBody, Player player, U unit, B building)
		{
			this.area = area;
			this.celestialBody = celestialBody;
			this.player = player;
			this.unit = unit;
			this.building = building;
		}
	}
	
	public genericResultSet<ProductiveCelestialBody, Unit, ABuilding> getBuilding(String celestialBodyName, String playerName, Class<? extends org.axan.sep.common.ABuilding> clientBuildingType)
	{
		ICelestialBody celestialBody = getCelestialBody(celestialBodyName);		
		if (celestialBody == null) throw new DataBaseError("Celestial body '" + celestialBodyName + "' does not exist.");

		// If celestial body is not a productive one.
		if (!ProductiveCelestialBody.class.isInstance(celestialBody)) throw new DataBaseError("Celestial body '" + celestialBodyName + "' is not a productive one.");
		ProductiveCelestialBody productiveCelestialBody = ProductiveCelestialBody.class.cast(celestialBody);

		// If player is not the celestial body owner.
		if (!playerName.equals(productiveCelestialBody.getOwnerName())) throw new DataBaseError("Player '" + playerName + "' is not the '" + celestialBodyName + "' celestial body owner.");

		Class<? extends ABuilding> serverBuildingType = ABuilding.getServerBuildingClass(clientBuildingType);
		ABuilding building = productiveCelestialBody.getBuilding(serverBuildingType);

		// If no building of this type exist.
		if (building == null) throw new DataBaseError("No building type '" + clientBuildingType.getSimpleName() + "' built yet.");

		return new genericResultSet<ProductiveCelestialBody, Unit, ABuilding>(getArea(celestialBody.getLocation()), productiveCelestialBody, getPlayer(playerName), null, building);
	}

	public SpaceRoad getSpaceRoad(RealLocation sourceLocation, RealLocation destinationLocation)
	{
		ProductiveCelestialBody source = getCelestialBody(sourceLocation.asLocation(), ProductiveCelestialBody.class);
		if (source == null) return null;
		
		SpaceCounter sourceSpaceCounter = source.getBuilding(SpaceCounter.class);
		if (sourceSpaceCounter == null) return null;
		
		ProductiveCelestialBody destination = getCelestialBody(destinationLocation.asLocation(), ProductiveCelestialBody.class);
		if (destination == null) return null;
		
		SpaceCounter destinationSpaceCounter = destination.getBuilding(SpaceCounter.class);
		if (destinationSpaceCounter == null) return null;
		
		return sourceSpaceCounter.getSpaceRoad(destination.getName());					
	}

	/*
	private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException
	{
		ois.defaultReadObject();
		//if (playersLogs == null) playersLogs = new HashMap<String, SortedSet<ILogEntry>>();
		if (playersLogs != null && !playersLogs.isEmpty())
		{
			for(String k : playersLogs.keySet())
			{
				if (playersLogs.get(k) == null) continue;
				if (HashSet.class.isInstance(playersLogs.get(k)))
				{
					Set<ILogEntry> s = playersLogs.get(k);
					playersLogs.put(k, new TreeSet<ILogEntry>(s));
				}
			}
		}
	}
	*/
}
