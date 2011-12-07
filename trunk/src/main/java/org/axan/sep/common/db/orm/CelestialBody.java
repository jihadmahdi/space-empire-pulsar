package org.axan.sep.common.db.orm;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.axan.eplib.orm.ISQLDataBaseStatement;
import org.axan.eplib.orm.SQLDataBaseException;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.ICelestialBody;
import org.axan.sep.common.db.IProductiveCelestialBody;
import org.axan.sep.common.db.IVortex;
import org.axan.sep.common.db.SEPCommonDB;
import org.axan.sep.common.db.orm.base.BaseCelestialBody;
import org.axan.sep.common.db.orm.base.IBaseCelestialBody;

public class CelestialBody implements ICelestialBody, Serializable
{
	private final IBaseCelestialBody baseCelestialBodyProxy;
	private final eCelestialBodyType type;
	private final Location location;

	CelestialBody(IBaseCelestialBody baseCelestialBodyProxy)
	{
		this.baseCelestialBodyProxy = baseCelestialBodyProxy;
		this.type = eCelestialBodyType.valueOf(baseCelestialBodyProxy.getType());
		this.location = (baseCelestialBodyProxy.getLocation_x() == null ? null : new Location(baseCelestialBodyProxy.getLocation_x(), baseCelestialBodyProxy.getLocation_y(), baseCelestialBodyProxy.getLocation_z()));
	}

	public CelestialBody(String name, eCelestialBodyType type, Location location)
	{
		this(new BaseCelestialBody(name, type.toString(), location == null ? null : location.x, location == null ? null : location.y, location == null ? null : location.z));
	}

	public CelestialBody(ISQLDataBaseStatement stmnt) throws Exception
	{
		this(new BaseCelestialBody(stmnt));
	}

	@Override
	public String getName()
	{
		return baseCelestialBodyProxy.getName();
	}

	@Override
	public eCelestialBodyType getType()
	{
		return type;
	}

	@Override
	public Location getLocation()
	{
		return location;
	}
	
	private static boolean loopWatchdog = false;
	
	public synchronized static <T extends ICelestialBody> T selectOne(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
	{
		if (loopWatchdog) throw new RuntimeException("CelestialBody static loop");
		loopWatchdog = true;
		try
		{
			if (IProductiveCelestialBody.class.isAssignableFrom(expectedType))
			{
				return ProductiveCelestialBody.selectOne(db, expectedType, from, where, params);
			}
			else if (IVortex.class.isAssignableFrom(expectedType))
			{
				return Vortex.selectOne(db, expectedType, from, where, params);
			}
			else
			{
				T test = (T) ProductiveCelestialBody.selectOne(db, IProductiveCelestialBody.class, from, where, params);
				if (test != null) return test;
				test = (T) Vortex.selectOne(db, IVortex.class, from, where, params);
				if (test != null) return test;
				
				return null;
				
				//throw new SQLDataBaseException("Unknown CelestialBody type "+expectedType.getName());
			}
		}
		finally
		{
			loopWatchdog = false;
		}
	}

	public synchronized static <T extends ICelestialBody> Set<T> select(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
	{
		if (loopWatchdog) throw new RuntimeException("CelestialBody static loop");
		loopWatchdog = true;
		try
		{
			if (IProductiveCelestialBody.class.isAssignableFrom(expectedType))
			{
				return ProductiveCelestialBody.select(db, expectedType, from, where, params);
			}
			else if (IVortex.class.isAssignableFrom(expectedType))
			{
				return Vortex.select(db, expectedType, from, where, params);
			}
			else
			{
				Set<T> result = new HashSet<T>();
				
				if (expectedType.isAssignableFrom(IProductiveCelestialBody.class))
				{
					Set<? extends IProductiveCelestialBody> pcbs = ProductiveCelestialBody.select(db, IProductiveCelestialBody.class, from, where, params);
					if (pcbs != null && !pcbs.isEmpty())
					{
						result.addAll((Set<? extends T>) pcbs);
					}
				}
				
				if (expectedType.isAssignableFrom(IVortex.class))
				{
					Set<? extends IVortex> vxs = Vortex.select(db, IVortex.class, from, where, params);
					if (vxs != null && !vxs.isEmpty())
					{
						result.addAll((Set<? extends T>) vxs);
					}
				}
				
				return result;
				
				//throw new SQLDataBaseException("Unknown CelestialBody type "+expectedType.getName());
			}
		}
		finally
		{
			loopWatchdog = false;
		}
	}

	public synchronized static <T extends ICelestialBody> boolean exist(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
	{
		if (loopWatchdog) throw new RuntimeException("CelestialBody static loop");
		loopWatchdog = true;
		try
		{
			if (IProductiveCelestialBody.class.isAssignableFrom(expectedType))
			{
				return ProductiveCelestialBody.exist(db, expectedType, from, where, params);
			}
			else if (IVortex.class.isAssignableFrom(expectedType))
			{
				return Vortex.exist(db, expectedType, from, where, params);
			}
			else
			{
				if (ProductiveCelestialBody.exist(db, expectedType, from, where, params)) return true;
				if (Vortex.exist(db, expectedType, from, where, params)) return true;
				return false;
				// throw new SQLDataBaseException("Unknown CelestialBody type "+expectedType.getName());
			}
		}
		finally
		{
			loopWatchdog = false;
		}
	}

	public synchronized static <T extends ICelestialBody> void insertOrUpdate(SEPCommonDB db, T celestialBody) throws SQLDataBaseException
	{
		if (loopWatchdog) throw new RuntimeException("CelestialBody static loop");
		loopWatchdog = true;
		try
		{
			if (IProductiveCelestialBody.class.isInstance(celestialBody))
			{
				ProductiveCelestialBody.insertOrUpdate(db, (IProductiveCelestialBody) celestialBody);
			}
			else if (IVortex.class.isInstance(celestialBody))
			{
				Vortex.insertOrUpdate(db, (IVortex) celestialBody);
			}
			else
			{
				throw new SQLDataBaseException("Unknown CelestialBody type "+celestialBody.getType());
			}
		}
		finally
		{
			loopWatchdog = false;
		}
	}
}
