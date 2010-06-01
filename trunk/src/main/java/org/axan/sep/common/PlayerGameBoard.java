/**
 * @author Escallier Pierre
 * @file GameTurnInfos.java
 * @date 2 juin 2009
 */
package org.axan.sep.common;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.SEPUtils.RealLocation;



/**
 * Represent the game board at a specific turn for a specific player. It provide
 * informations about the universe and the last turn resolution.
 */
public class PlayerGameBoard implements Serializable
{
	//public static final Logger log = Logger.getLogger(PlayerGameBoard.class.getName());
	
	public static class PlayerGameBoardQueryException extends Exception
	{
		public PlayerGameBoardQueryException(String msg)
		{
			super(msg);
		}
	}		

	private static final long			serialVersionUID	= 1L;

	/** Game config. */
	private final GameConfig config;
	
	/** 3 dimensional array of universe area. */
	private transient Area[][][]	universe;

	/** Sun location. Sun always fills 9 area. */
	private final RealLocation				sunLocation;

	private final int					date;
	private int localDate = 0;
	
	private final Map<String, Diplomacy>	playersPolicies;
	
	private final String	playerName;

	// TODO : add last turn resolution informations.

	/**
	 * Full constructor.
	 */
	public PlayerGameBoard(GameConfig config, String playerName, Area[][][] universe, RealLocation sunLocation, int date, Map<String, Diplomacy> playersPolicies)
	{
		this.config = config;
		this.playerName = playerName;
		this.universe = universe;
		this.sunLocation = sunLocation;
		this.date = date;
		this.playersPolicies = playersPolicies;
	}

	public Map<String, Diplomacy> getPlayersPolicies()
	{
		return playersPolicies;
	}
	
	public int getDimX()
	{
		return universe.length;
	}

	public int getDimY()
	{
		return universe[0].length;
	}

	public int getDimZ()
	{
		return universe[0][0].length;
	}

	public int getLocalDate()
	{
		return localDate;
	}
	
	public void incLocalDate()
	{
		++localDate;
	}
	
	public Area getArea(int x, int y, int z)
	{
		return getArea(new RealLocation(x + 0.5, y + 0.5, z + 0.5));
	}
	
	public Area getArea(RealLocation location)
	{
		if (universe[(int) location.x][(int) location.y][(int) location.z] == null)
		{
			universe[(int) location.x][(int) location.y][(int) location.z] = new Area();
		}
		return universe[(int) location.x][(int) location.y][(int) location.z];
	}	

	public int getDate()
	{
		return date;
	}
	
	public GameConfig getConfig()
	{
		return config;
	}

	public RealLocation getUnitLocation(String ownerName, String unitName) throws PlayerGameBoardQueryException
	{
		for(int x = 0; x < getDimX(); ++x)
			for(int y = 0; y < getDimY(); ++y)
				for(int z = 0; z < getDimZ(); ++z)
				{
					Area area = universe[x][y][z];
					if (area == null) continue;

					Unit unit = area.getUnit(ownerName, unitName);
					if (unit != null) return new RealLocation(x + 0.5, y + 0.5, z + 0.5);
				}

		throw new PlayerGameBoardQueryException("Unknown unit '"+ownerName+"@"+unitName+"'");
	}

	public <U extends Unit> void removeUnit(String ownerName, String unitName, Class<U> unitType) throws PlayerGameBoardQueryException
	{
		U unit = getUnit(ownerName, unitName, unitType);
		if (unit == null) throw new PlayerGameBoardQueryException("Unit '"+ownerName+"@"+unitName+"' does not exist.");
		Area a = getArea(unit.getCurrentLocation());
		if (a == null) throw new PlayerGameBoardQueryException("Cannot find unit '"+ownerName+"@"+unitName+"' area");
		a.removeUnit(ownerName, unitName, unitType);
	}
	
	public <U extends Unit> U getUnit(String ownerName, String unitName, Class<U> unitType) throws PlayerGameBoardQueryException
	{
		Unit u = getUnit(ownerName, unitName);
		if (u == null) return null;		
		if (!unitType.isInstance(u)) throw new PlayerGameBoardQueryException("Unit '"+ownerName+"@"+unitName+"' is not of unitType '"+unitType.getSimpleName()+"'");
		return unitType.cast(u);
	}
	
	public Unit getUnit(String ownerName, String unitName) throws PlayerGameBoardQueryException
	{
		for(int x = 0; x < getDimX(); ++x)
			for(int y = 0; y < getDimY(); ++y)
				for(int z = 0; z < getDimZ(); ++z)
				{
					Area area = universe[x][y][z];
					if (area == null) continue;

					Unit unit = area.getUnit(ownerName, unitName);
					if (unit != null) return unit;
				}

		return null;
		//throw new PlayerGameBoardQueryException("Unknown unit '"+ownerName+"@"+unitName+"'");
	}

	public <U extends Unit> Set<U> getUnits(Class<U> unitType)
	{
		Set<U> result = new HashSet<U>();

		for(int x = 0; x < getDimX(); ++x)
			for(int y = 0; y < getDimY(); ++y)
				for(int z = 0; z < getDimZ(); ++z)
				{
					Area area = universe[x][y][z];
					if (area == null) continue;

					Set<U> units = area.getUnits(unitType);
					if (units != null) result.addAll(units);
				}

		return result;
	}

	public Set<ICelestialBody> getCelestialBodies()
	{
		Set<ICelestialBody> result = new HashSet<ICelestialBody>();
		for(int x = 0; x < getDimX(); ++x)
			for(int y = 0; y < getDimY(); ++y)
				for(int z = 0; z < getDimZ(); ++z)
				{
					Area area = universe[x][y][z];
					if (area == null) continue;

					if (area.getCelestialBody() != null)
					{
						result.add(area.getCelestialBody());
					}
				}

		return result;
	}

	public RealLocation getCelestialBodyLocation(String celestialBodyName) throws PlayerGameBoardQueryException
	{
		for(int x = 0; x < getDimX(); ++x)
			for(int y = 0; y < getDimY(); ++y)
				for(int z = 0; z < getDimZ(); ++z)
				{
					Area area = universe[x][y][z];
					if (area == null) continue;

					if (area.getCelestialBody() != null)
					{
						if (area.getCelestialBody().getName().equals(celestialBodyName)) return new Location(x, y, z).asRealLocation();
					}
				}

		throw new PlayerGameBoardQueryException("Unknown celestial body '"+celestialBodyName+"'");
	}
	
	public <C extends ICelestialBody> C getCelestialBody(RealLocation location, Class<C> celestialBodyType) throws PlayerGameBoardQueryException
	{
		Area a = getArea(location);
		if (a == null) throw new PlayerGameBoardQueryException("Cannot find area in location '"+location.asLocation().toString()+"'");
		ICelestialBody c = a.getCelestialBody();
		if (c == null) return null;
		if (!celestialBodyType.isInstance(c)) throw new PlayerGameBoardQueryException("Celestial body in area '"+location.asLocation().toString()+"' is not a '"+celestialBodyType.getSimpleName()+"' one.");
		return celestialBodyType.cast(c);
	}
	
	public <C extends ICelestialBody> C getCelestialBody(String celestialBodyName, Class<C> celestialBodyType) throws PlayerGameBoardQueryException
	{
		ICelestialBody celestialBody = getCelestialBody(celestialBodyName);
		if (!celestialBodyType.isInstance(celestialBody)) throw new PlayerGameBoardQueryException("Celestial body '"+celestialBodyName+"' is not of type '"+celestialBodyType.getSimpleName()+"'");
		return celestialBodyType.cast(celestialBody);
	}
	
	public ICelestialBody getCelestialBody(String celestialBodyName) throws PlayerGameBoardQueryException
	{
		for(int x = 0; x < getDimX(); ++x)
			for(int y = 0; y < getDimY(); ++y)
				for(int z = 0; z < getDimZ(); ++z)
				{
					Area area = universe[x][y][z];
					if (area == null) continue;

					if (area.getCelestialBody() != null)
					{
						if (area.getCelestialBody().getName().equals(celestialBodyName)) return area.getCelestialBody();
					}
				}

		throw new PlayerGameBoardQueryException("Unknown celestial body '"+celestialBodyName+"'");
	}
	
	public <B extends ABuilding> B getBuilding(String celestialBodyName, Class<B> buildingType) throws PlayerGameBoardQueryException
	{
		ICelestialBody celestialBody = getCelestialBody(celestialBodyName);
		if (celestialBody == null || !ProductiveCelestialBody.class.isInstance(celestialBody)) throw new PlayerGameBoardQueryException("Unknown productive celestial body '"+celestialBodyName+"'");
		ProductiveCelestialBody productiveCelestialBody = ProductiveCelestialBody.class.cast(celestialBody);
		
		return productiveCelestialBody.getBuilding(buildingType);		
	}
	
	public Set<ProductiveCelestialBody> getCelestialBodiesWithBuilding(Class<? extends ABuilding> buildingType)
	{
		Set<ProductiveCelestialBody> result = new HashSet<ProductiveCelestialBody>();
		for(int x = 0; x < getDimX(); ++x)
			for(int y = 0; y < getDimY(); ++y)
				for(int z = 0; z < getDimZ(); ++z)
				{
					Area area = universe[x][y][z];
					if (area == null) continue;

					if (area.getCelestialBody() != null)
					{
						if (ProductiveCelestialBody.class.isInstance(area.getCelestialBody()))
						{
							ProductiveCelestialBody productiveCelestialBody = ProductiveCelestialBody.class.cast(area.getCelestialBody());
							ABuilding building = productiveCelestialBody.getBuilding(buildingType);

							if (building != null)
							{
								result.add(productiveCelestialBody);
							}
						}
					}
				}

		return result;
	}
	
	public String getPlayerName()
	{
		return playerName;
	}
	
	public boolean isValidLocation(RealLocation location)
	{
		if (location.x < 0 || location.x >= getDimX()) return false;
		if (location.y < 0 || location.y >= getDimY()) return false;
		if (location.z < 0 || location.z >= getDimZ()) return false;
		return true;
	}
	
	public boolean isTravellingTheSun(RealLocation a, RealLocation b)
	{
		for(RealLocation pathStep : SEPUtils.getAllPathLoc(a, b))
		{
			Area area = getArea(pathStep);
			if (area != null && area.isSun()) return true;
		}
		
		return false;
	}
	
	public Fleet getGovernmentalFleet(String playerName)
	{
		for(Fleet f : getUnits(Fleet.class))
		{
			if (!f.isGovernmentFleet()) continue;
			
			if (!playerName.equals(f.getOwnerName())) continue;
			
			return f;
		}
		
		return null;
	}
	
	public Planet locateGovernmentModule(String playerName) throws PlayerGameBoardQueryException
	{
		for(ProductiveCelestialBody p : getCelestialBodiesWithBuilding(GovernmentModule.class))
		{
			if (playerName.equals(p.getOwnerName())) return Planet.class.cast(p);
		}
		
		throw new PlayerGameBoardQueryException("Cannot find government module for player '"+playerName+"'");
	}

	private void writeObject(java.io.ObjectOutputStream out) throws IOException
	{
		out.defaultWriteObject();
		
		out.writeInt(getDimX());
		out.writeInt(getDimY());
		out.writeInt(getDimZ());
		
		for(int x = 0; x < getDimX(); ++x)
		for(int y = 0; y < getDimY(); ++y)
		for(int z = 0; z < getDimZ(); ++z)
		{
			Area area = universe[x][y][z];
			if (area == null) continue;
			
			out.writeInt(x);
			out.writeInt(y);
			out.writeInt(z);
			out.writeObject(area);
		}
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
		
		int dimX = in.readInt();
		int dimY = in.readInt();
		int dimZ = in.readInt();
		
		int x, y, z;
		Area area;
		
		this.universe = new Area[dimX][dimY][dimZ];
		
		while(in.available() > 0)
		{
			x = in.readInt();
			y = in.readInt();
			z = in.readInt();
			
			area = Area.class.cast(in.readObject());
			
			this.universe[x][y][z] = area;
		}
	}

	private void readObjectNoData() throws ObjectStreamException
	{

	}
}
