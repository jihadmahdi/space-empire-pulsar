package org.axan.sep.common.db.orm;

import org.axan.sep.common.db.orm.ProductiveCelestialBody;

import java.io.Serializable;
import java.lang.Exception;
import org.axan.sep.common.db.orm.base.IBasePlanet;
import org.axan.sep.common.db.orm.base.BasePlanet;
import org.axan.sep.common.db.IPlanet;
import org.axan.sep.common.db.SEPCommonDB;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.IGameConfig;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ReturnableEvaluator;
import org.neo4j.graphdb.StopEvaluator;
import org.neo4j.graphdb.Traverser.Order;

public class Planet extends ProductiveCelestialBody implements IPlanet, Serializable
{
	private final IBasePlanet basePlanetProxy;

	Planet(IBasePlanet basePlanetProxy)
	{
		super(basePlanetProxy);
		this.basePlanetProxy = basePlanetProxy;
	}

	public Planet(String name, eCelestialBodyType type, Location location, Integer initialCarbonStock, Integer maxSlots, String owner, Integer carbonStock, Integer currentCarbon, Integer populationPerTurn, Integer maxPopulation, Integer currentPopulation)
	{
		this(new BasePlanet(name, type.toString(), location == null ? null : location.x, location == null ? null : location.y, location == null ? null : location.z, initialCarbonStock, maxSlots, owner, carbonStock, currentCarbon, populationPerTurn, maxPopulation, currentPopulation));
	}

	public Planet(Node stmnt)
	{
		this(new BasePlanet(stmnt));
	}

	@Override
	public Integer getPopulationPerTurn()
	{
		return basePlanetProxy.getPopulationPerTurn();
	}

	@Override
	public Integer getMaxPopulation()
	{
		return basePlanetProxy.getMaxPopulation();
	}

	@Override
	public Integer getCurrentPopulation()
	{
		return basePlanetProxy.getCurrentPopulation();
	}

	@Override
	public Map<String, Object> getNode()
	{
		return basePlanetProxy.getNode();
	}

	/**
	 * Return the starting planet on first turn.
	 * @param playerName
	 * @return
	 * @throws SQLDataBaseException 
	 */
	public static IPlanet getStartingPlanet(SEPCommonDB db, String playerName)
	{
		while(db.getConfig().getTurn() > 1) db = db.previous();
		Iterator<Node> it = db.getPlayerNode(playerName).traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, ReturnableEvaluator.ALL_BUT_START_NODE, eCelestialBodyType.Planet, Direction.OUTGOING).iterator();
		if (!it.hasNext()) return null;
		return new Planet(it.next());
	}
}
