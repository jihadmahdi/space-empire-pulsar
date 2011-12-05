package org.axan.sep.common.db.orm;

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

public class CelestialBody implements ICelestialBody
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
	
	public static <T extends ICelestialBody> T selectOne(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
	{
		if (IProductiveCelestialBody.class.isAssignableFrom(expectedType))
		{
			return CelestialBody.selectOne(db, expectedType, from, where, params);
		}
		else if (IVortex.class.isAssignableFrom(expectedType))
		{
			return CelestialBody.selectOne(db, expectedType, from, where, params);
		}
		else
		{
			throw new SQLDataBaseException("Unknown CelestialBody type "+expectedType.getName());
		}
	}

	public static <T extends ICelestialBody> Set<T> select(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
	{
		if (IProductiveCelestialBody.class.isAssignableFrom(expectedType))
		{
			return CelestialBody.select(db, expectedType, from, where, params);
		}
		else if (IVortex.class.isAssignableFrom(expectedType))
		{
			return CelestialBody.select(db, expectedType, from, where, params);
		}
		else
		{
			throw new SQLDataBaseException("Unknown CelestialBody type "+expectedType.getName());
		}
	}

	public static <T extends ICelestialBody> boolean exist(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
	{
		if (IProductiveCelestialBody.class.isAssignableFrom(expectedType))
		{
			return CelestialBody.exist(db, expectedType, from, where, params);
		}
		else if (IVortex.class.isAssignableFrom(expectedType))
		{
			return CelestialBody.exist(db, expectedType, from, where, params);
		}
		else
		{
			throw new SQLDataBaseException("Unknown CelestialBody type "+expectedType.getName());
		}
	}

	public static <T extends ICelestialBody> void insertOrUpdate(SEPCommonDB db, T celestialBody) throws SQLDataBaseException
	{
		if (IProductiveCelestialBody.class.isInstance(celestialBody))
		{
			CelestialBody.insertOrUpdate(db, celestialBody);
		}
		else if (IVortex.class.isInstance(celestialBody))
		{
			CelestialBody.insertOrUpdate(db, celestialBody);
		}
		else
		{
			throw new SQLDataBaseException("Unknown CelestialBody type "+celestialBody.getType());
		}
	}
}
