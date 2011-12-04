package org.axan.sep.common.db.orm;

import java.lang.Exception;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.axan.sep.common.db.orm.base.IBaseCelestialBody;
import org.axan.sep.common.db.orm.base.BaseCelestialBody;
import org.axan.sep.common.db.IAsteroidField;
import org.axan.sep.common.db.ICelestialBody;
import org.axan.sep.common.db.INebula;
import org.axan.sep.common.db.IPlanet;
import org.axan.sep.common.db.IProductiveCelestialBody;
import org.axan.sep.common.db.IVersionedAsteroidField;
import org.axan.sep.common.db.IVersionedNebula;
import org.axan.sep.common.db.IVersionedPlanet;
import org.axan.sep.common.db.IVersionedProductiveCelestialBody;
import org.axan.sep.common.db.IVortex;
import org.axan.sep.common.db.SEPCommonDB;
import org.axan.eplib.orm.DataBaseORMGenerator;
import org.axan.eplib.orm.ISQLDataBaseStatement;
import org.axan.eplib.orm.ISQLDataBaseStatementJob;
import org.axan.eplib.orm.SQLDataBaseException;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.IGameConfig;

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

	public String getName()
	{
		return baseCelestialBodyProxy.getName();
	}

	public eCelestialBodyType getType()
	{
		return type;
	}

	public Location getLocation()
	{
		return location;
	}

	/** Set maxVersion to null to select last version. */
	public static <T extends ICelestialBody> Set<T> selectMaxVersion(SEPCommonDB db, Class<T> expectedType, Integer maxVersion, String from, String where, Object ... params) throws SQLDataBaseException
	{
		if (IProductiveCelestialBody.class.isAssignableFrom(expectedType))
		{
			return ProductiveCelestialBody.selectMaxVersion(db, expectedType, maxVersion, from, where, params);
		}
		else if (IVortex.class.isAssignableFrom(expectedType))
		{
			return Vortex.selectMaxVersion(db, expectedType, maxVersion, from, where, params);
		}
		else
		{
			throw new SQLDataBaseException("Unknown CelestialBody type "+expectedType.getName());
		}
	}

	public static <T extends IProductiveCelestialBody> Set<T> selectVersion(SEPCommonDB db, Class<T> expectedType, int version, String from, String where, Object ... params) throws SQLDataBaseException
	{
		if (IProductiveCelestialBody.class.isAssignableFrom(expectedType))
		{
			return ProductiveCelestialBody.selectVersion(db, expectedType, version, from, where, params);
		}
		else if (IVortex.class.isAssignableFrom(expectedType))
		{
			return Vortex.selectVersion(db, expectedType, version, from, where, params);
		}
		else
		{
			throw new SQLDataBaseException("Unknown CelestialBody type "+expectedType.getName());
		}
	}

	public static <T extends IProductiveCelestialBody> Set<T> selectUnversioned(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
	{
		if (IProductiveCelestialBody.class.isAssignableFrom(expectedType))
		{
			return ProductiveCelestialBody.selectUnversioned(db, expectedType, from, where, params);
		}
		else if (IVortex.class.isAssignableFrom(expectedType))
		{
			return Vortex.selectUnversioned(db, expectedType, from, where, params);
		}
		else
		{
			throw new SQLDataBaseException("Unknown CelestialBody type "+expectedType.getName());
		}
	}

	/** Set maxVersion to null to select last version. */
	public static <T extends ICelestialBody> boolean existMaxVersion(SEPCommonDB db, Class<T> expectedType, Integer maxVersion, String from, String where, Object ... params) throws SQLDataBaseException
	{
		if (IProductiveCelestialBody.class.isAssignableFrom(expectedType))
		{
			return ProductiveCelestialBody.existMaxVersion(db, expectedType, maxVersion, from, where, params);
		}
		else if (IVortex.class.isAssignableFrom(expectedType))
		{
			return Vortex.existMaxVersion(db, expectedType, maxVersion, from, where, params);
		}
		else
		{
			throw new SQLDataBaseException("Unknown CelestialBody type "+expectedType.getName());
		}				
	}

	public static <T extends ICelestialBody> boolean existVersion(SEPCommonDB db,Class<T> expectedType, int version, String from, String where, Object ... params) throws SQLDataBaseException
	{
		if (IProductiveCelestialBody.class.isAssignableFrom(expectedType))
		{
			return ProductiveCelestialBody.existVersion(db, expectedType, version, from, where, params);
		}
		else if (IVortex.class.isAssignableFrom(expectedType))
		{
			return Vortex.existVersion(db, expectedType, version, from, where, params);
		}
		else
		{
			throw new SQLDataBaseException("Unknown CelestialBody type "+expectedType.getName());
		}
	}

	public static <T extends ICelestialBody> boolean existUnversioned(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
	{
		if (IProductiveCelestialBody.class.isAssignableFrom(expectedType))
		{
			return ProductiveCelestialBody.existUnversioned(db, expectedType, from, where, params);
		}
		else if (IVortex.class.isAssignableFrom(expectedType))
		{
			return Vortex.existUnversioned(db, expectedType, from, where, params);
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
			ProductiveCelestialBody.insertOrUpdate(db, celestialBody);
		}
		else if (IVortex.class.isInstance(celestialBody))
		{
			Vortex.insertOrUpdate(db, celestialBody);
		}
		else
		{
			throw new SQLDataBaseException("Unknown CelestialBody type "+celestialBody.getType());
		}
	}
}
