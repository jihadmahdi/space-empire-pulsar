package org.axan.sep.common.db.orm;

import org.axan.sep.common.db.orm.Unit;
import java.lang.Exception;
import org.axan.sep.common.db.orm.base.IBaseProbe;
import org.axan.sep.common.db.orm.base.BaseProbe;
import org.axan.sep.common.db.IProbe;
import java.util.HashMap;
import java.util.Map;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.IGameConfig;
import org.neo4j.graphdb.Node;

public class Probe extends Unit implements IProbe
{
	private final IBaseProbe baseProbeProxy;

	Probe(IBaseProbe baseProbeProxy, IGameConfig config)
	{
		super(baseProbeProxy, config);
		this.baseProbeProxy = baseProbeProxy;
	}

	public Probe(String owner, String name, eUnitType type, Location departure, Double progress, Location destination, IGameConfig config)
	{
		this(new BaseProbe(owner, name, type.toString(), departure == null ? null : departure.x, departure == null ? null : departure.y, departure == null ? null : departure.z, progress, destination == null ? null : destination.x, destination == null ? null : destination.y, destination == null ? null : destination.z), config);
	}

	public Probe(Node stmnt, IGameConfig config) throws Exception
	{
		this(new BaseProbe(stmnt), config);
	}

	@Override
	public Map<String, Object> getNode()
	{
		return baseProbeProxy.getNode();
	}
	
	@Override
	public boolean isDeployed()
	{
		return getProgress() == 1;
	}

	/**
	 * 1$: table.
	 */
	private static final String SQL_DEPLOYED_CONDITIONS = "(%1$sprogress = 0)";
	
	/**
	 * @see org.axan.sep.common.db.orm.Probe#getSQLDeployedConditions(String)
	 */
	public static String getSQLDeployedConditions()
	{
		return getSQLDeployedConditions(null);
	}
	
	/**
	 * Return Probe is deployed SQL conditions. No parameters added.
	 * @param table Table alias to test
	 * @return
	 */
	public static String getSQLDeployedConditions(String table)
	{
		return String.format(SQL_DEPLOYED_CONDITIONS, table == null || table.isEmpty() ? "" : table+".");
	}
}
