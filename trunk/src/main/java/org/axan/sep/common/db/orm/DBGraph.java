package org.axan.sep.common.db.orm;

import org.axan.sep.common.db.IDBGraph;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.Transaction;
import org.axan.eplib.orm.nosql.DBGraphException;
import org.neo4j.graphdb.RelationshipType;
import org.axan.sep.common.SEPUtils.Location;

public class DBGraph implements IDBGraph
{
	static enum eRelationTypes implements RelationshipType
	{
		Config,
		Units,
		CelestialBodies,
		Diplomacies
	}

	private GraphDatabaseService db;
	private Index<Node> vortexIndex;
	private Index<Node> playerIndex;
	private Index<Node> planetIndex;

	public DBGraph(GraphDatabaseService db)
	{
		vortexIndex = db.index().forNodes("VortexIndex");
		playerIndex = db.index().forNodes("PlayerIndex");
		planetIndex = db.index().forNodes("PlanetIndex");
	}

	public Node createVortex(String name, int birth, int death)
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
			tx.success();
			return vortexNode;
		}
		finally
		{
			tx.finish();
		}
	}

	public Node createPlayer(String name, String playerConfigColor, String playerConfigSymbol, String playerConfigPortrait)
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
			return playerNode;
		}
		finally
		{
			tx.finish();
		}
	}

	public Node createArea(Location location)
	{
		Transaction tx = db.beginTx();
		try
		{
			Node areaNode = db.createNode();
			Area.initializeNode(areaNode, location);
			tx.success();
			return areaNode;
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

	public Node createPlanet(String name, int initialCarbonStock, int maxSlots, int carbonStock, int currentCarbon, int populationPerTurn, int maxPopulation, int currentPopulation)
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
			tx.success();
			return planetNode;
		}
		finally
		{
			tx.finish();
		}
	}

}
