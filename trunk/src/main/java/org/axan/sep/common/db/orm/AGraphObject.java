package org.axan.sep.common.db.orm;

import java.io.Serializable;

import javax.annotation.OverridingMethodsMustInvokeSuper;

import org.junit.matchers.IsCollectionContaining;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;

public abstract class AGraphObject<T extends PropertyContainer> implements Serializable
{
	/*
	 * PK: first pk field.
	 */
	protected final Object pk;
	
	/*
	 * DB connection: DB connection and useful objects (e.g. indexes and nodes).
	 */
	protected SEPCommonDB sepDB;
	protected GraphDatabaseService db;
	protected T properties;
	
	/**
	 * Off-DB constructor.
	 * Off-DB constructor is full params off-db.
	 * If pk is a string, somes tags are processed:
	 * <ul>
	 *   <li>$class is replaced by getClass().getSimpleName()</li>
	 * </ul>
	 * @param pk
	 */
	public AGraphObject(Object pk)
	{
		if (String.class.isInstance(pk))
		{
			String sPk = (String) pk;
			sPk = String.format(sPk.replaceAll("\\$class", "%0\\$s"), getClass().getSimpleName());
			pk = sPk;
		}
		this.pk = pk;
	}
	
	/**
	 * On-DB constructor.
	 * On-DB constructor require only DB object and first pk field value.
	 * @param sepDB
	 * @param pk
	 */
	public AGraphObject(SEPCommonDB sepDB, Object pk)
	{
		this(pk);
		this.sepDB = sepDB;
	}
	
	protected void assertOnlineStatus(boolean isOnline)
	{
		assertOnlineStatus(isOnline, "DB must be "+(isOnline?"on":"off")+"line.");
	}
	protected void assertOnlineStatus(boolean isOnline, String msg)
	{
		if (isDBOnline() != isOnline)
		{
			throw new RuntimeException(msg);
		}
	}
	
	/**
	 * Check if current object is DB connected or not.
	 * @return
	 */
	protected boolean isDBOnline()
	{
		return sepDB != null;
	}
	
	/**
	 * Check if current DB connection objects are outdated.
	 * Must not be called is {@link #isDBOnline()} return false.
	 * @return
	 */
	protected boolean isDBOutdated()
	{
		return db == null || properties == null || !db.equals(sepDB.getDB());
	}
	
	/**
	 * Check if current object exists in DB.
	 * Always return false while object is DB-off.
	 * @return
	 */
	public boolean exists()
	{
		checkForDBUpdate();
		return isDBOnline() && properties != null;
	}
	
	/**
	 * If object is DB connected, check for DB update.
	 * Top level class implementation must initialize node field from index.
	 * Each sub class should override to initialize additional indexes and nodes.
	 */
	@OverridingMethodsMustInvokeSuper
	abstract protected void checkForDBUpdate();
	
	/**
	 * Current object must be Off-DB to call create method.
	 * Create method connect the object to the given DB and create the object node.
	 * After this call, object is DB connected.
	 * Bottom concrete class must override this method as final and actually create and initialize node, then call super implementation.
	 * All top class in hierarchy should override this method to add the new node to their indexes.
	 * @param sepDB
	 */
	@OverridingMethodsMustInvokeSuper
	abstract protected void create(SEPCommonDB sepDB);
	
	/**
	 * AGraphObject are equals if class are equal, pk are equal, and sepDB are equal or both null.
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (obj == null) return false;
		if (!getClass().equals(obj.getClass())) return false;
		AGraphObject o = (AGraphObject) obj;
		if (!pk.equals(o.pk)) return false;
		if ((sepDB == null && o.sepDB != null) || (!sepDB.equals(o.sepDB))) return false;
		return true;
	}
}
