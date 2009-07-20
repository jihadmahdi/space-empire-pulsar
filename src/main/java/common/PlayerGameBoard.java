/**
 * @author Escallier Pierre
 * @file GameTurnInfos.java
 * @date 2 juin 2009
 */
package common;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.corba.se.impl.orbutil.ObjectWriter;
import common.SEPUtils.RealLocation;

/**
 * Represent the game board at a specific turn for a specific player. It provide
 * informations about the universe and the last turn resolution.
 */
public class PlayerGameBoard implements Serializable
{
	//public static final Logger log = Logger.getLogger(PlayerGameBoard.class.getName());

	private static final long			serialVersionUID	= 1L;

	/** 3 dimensional array of universe area. */
	private transient Area[][][]	universe;

	/** Sun location. Sun always fills 9 area. */
	private final RealLocation				sunLocation;

	private final int					date;
	
	private final Map<String, Diplomacy>	playersPolicies;

	// TODO : add last turn resolution informations.

	/**
	 * Full constructor.
	 */
	public PlayerGameBoard(Area[][][] universe, RealLocation sunLocation, int date, Map<String, Diplomacy> playersPolicies)
	{
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

	public RealLocation getUnitLocation(String ownerName, String unitName)
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

		return null;
	}

	public Unit getUnit(String ownerName, String unitName)
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

	public Set<ProductiveCelestialBody> getCelestialBodiesWithBuilding(Class<? extends IBuilding> buildingType)
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
							SpaceCounter spaceCounter = productiveCelestialBody.getBuilding(SpaceCounter.class);

							if (spaceCounter != null)
							{
								result.add(productiveCelestialBody);
							}
						}
					}
				}

		return result;
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
