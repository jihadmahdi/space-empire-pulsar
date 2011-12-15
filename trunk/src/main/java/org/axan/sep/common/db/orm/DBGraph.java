package org.axan.sep.common.db.orm;

import org.axan.sep.common.db.IDBGraph;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.Transaction;
import org.axan.eplib.orm.nosql.DBGraphException;
import org.neo4j.graphdb.RelationshipType;
import org.axan.eplib.orm.DataBaseORMGenerator;
import java.util.NoSuchElementException;
import java.util.logging.Logger;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.IVortex;
import org.axan.sep.common.db.IPlayer;
import org.axan.sep.common.db.ICelestialBody;
import org.axan.sep.common.db.IArea;
import org.axan.sep.common.db.IProductiveCelestialBody;
import org.axan.sep.common.db.IPlanet;

public class DBGraph implements IDBGraph
{
	static enum eRelationTypes implements RelationshipType
	{
		Config,
		Units,
		CelestialBodies,
		Diplomacies
	}

	private static Logger log = Logger.getLogger(DBGraph.class.getName());
	private GraphDatabaseService db;
	private Index<Node> vortexIndex;
	private Index<Node> playerIndex;
	private Index<Node> celestialBodyIndex;
	private Index<Node> productiveCelestialBodyIndex;
	private Index<Node> planetIndex;

	public DBGraph(GraphDatabaseService db)
	{
		vortexIndex = db.index().forNodes("VortexIndex");
		playerIndex = db.index().forNodes("PlayerIndex");
		celestialBodyIndex = db.index().forNodes("CelestialBodyIndex");
		productiveCelestialBodyIndex = db.index().forNodes("ProductiveCelestialBodyIndex");
		planetIndex = db.index().forNodes("PlanetIndex");
	}

	public IVortex createVortex(String name, int birth, int death)
	{
		Transaction tx = db.beginTx();
		try
		{
			if (vortexIndex.get("name", name).hasNext())
			{
				tx.failure();
				throw new DBGraphException("Constraint error: Indexed field 'name' must be unique, Vortex[name='"+name+"'] already exist.");
			}
			Node vortexNode = db.createNode();
			Vortex.initializeNode(vortexNode, name, birth, death);
			vortexIndex.add(vortexNode, "name", name);
			celestialBodyIndex.add(vortexNode, "name", name);
			tx.success();
			return new Vortex(vortexNode);
		}
		finally
		{
			tx.finish();
		}
	}

	public IPlayer createPlayer(String name, String playerConfigColor, String playerConfigSymbol, String playerConfigPortrait)
	{
		Transaction tx = db.beginTx();
		try
		{
			if (playerIndex.get("name", name).hasNext())
			{
				tx.failure();
				throw new DBGraphException("Constraint error: Indexed field 'name' must be unique, Player[name='"+name+"'] already exist.");
			}
			Node playerNode = db.createNode();
			Player.initializeNode(playerNode, name);
			playerIndex.add(playerNode, "name", name);
			Node playerConfigNode = createPlayerConfig(playerConfigColor, playerConfigSymbol, playerConfigPortrait);
			playerNode.createRelationshipTo(playerConfigNode, eRelationTypes.Config);
			tx.success();
			return new Player(playerNode);
		}
		finally
		{
			tx.finish();
		}
	}

	public IArea createArea(Location location)
	{
		Transaction tx = db.beginTx();
		try
		{
			Node areaNode = db.createNode();
			Area.initializeNode(areaNode, location);
			tx.success();
			return new Area(areaNode);
		}
		finally
		{
			tx.finish();
		}
	}

	private Node createPlayerConfig(String color, String symbol, String portrait)
	{
		Transaction tx = db.beginTx();
		try
		{
			Node playerConfigNode = db.createNode();
			PlayerConfig.initializeNode(playerConfigNode, color, symbol, portrait);
			tx.success();
			return playerConfigNode;
		}
		finally
		{
			tx.finish();
		}
	}

	public IPlanet createPlanet(String name, int initialCarbonStock, int maxSlots, int carbonStock, int currentCarbon, int populationPerTurn, int maxPopulation, int currentPopulation)
	{
		Transaction tx = db.beginTx();
		try
		{
			if (planetIndex.get("name", name).hasNext())
			{
				tx.failure();
				throw new DBGraphException("Constraint error: Indexed field 'name' must be unique, Planet[name='"+name+"'] already exist.");
			}
			Node planetNode = db.createNode();
			Planet.initializeNode(planetNode, name, initialCarbonStock, maxSlots, carbonStock, currentCarbon, populationPerTurn, maxPopulation, currentPopulation);
			planetIndex.add(planetNode, "name", name);
			celestialBodyIndex.add(planetNode, "name", name);
			productiveCelestialBodyIndex.add(planetNode, "name", name);
			tx.success();
			return new Planet(planetNode);
		}
		finally
		{
			tx.finish();
		}
	}

	public IVortex getVortexByName(String name)
	{
		return new Vortex(vortexIndex.get("name", name).getSingle());
	}

	public IPlayer getPlayerByName(String name)
	{
		return new Player(playerIndex.get("name", name).getSingle());
	}

	public ICelestialBody getCelestialBodyByName(String name)
	{
		try
		{
			return DataBaseORMGenerator.mapTo(ICelestialBody.class, celestialBodyIndex.get("name", name).getSingle());
		}
		catch(Throwable t)
		{
			if (!NoSuchElementException.class.isInstance(t))
			{
				log.log(Level.WARNING, "ORM Error, assume CelestialBody name '"+name+"' is not found.", t);
			}
			return null;
		}
	}

	public IProductiveCelestialBody getProductiveCelestialBodyByName(String name)
	{
		try
		{
			return DataBaseORMGenerator.mapTo(IProductiveCelestialBody.class, productiveCelestialBodyIndex.get("name", name).getSingle());
		}
		catch(Throwable t)
		{
			if (!NoSuchElementException.class.isInstance(t))
			{
				log.log(Level.WARNING, "ORM Error, assume ProductiveCelestialBody name '"+name+"' is not found.", t);
			}
			return null;
		}
	}

	public IPlanet getPlanetByName(String name)
	{
		return new Planet(planetIndex.get("name", name).getSingle());
	}

}
