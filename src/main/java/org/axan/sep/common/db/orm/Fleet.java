package org.axan.sep.common.db.orm;

import java.sql.PreparedStatement;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.OverridingMethodsMustInvokeSuper;

import org.axan.eplib.orm.nosql.AVersionedGraphNode;
import org.axan.eplib.orm.nosql.DBGraphException;
import org.axan.eplib.utils.Basic;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.Rules;
import org.axan.sep.common.Rules.StarshipTemplate;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.FleetMove;
import org.axan.sep.common.db.ICelestialBody;
import org.axan.sep.common.db.IFleet;
import org.axan.sep.common.db.IFleetMarker;
import org.axan.sep.common.db.IProductiveCelestialBody;
import org.axan.sep.common.db.IUnitMarker;
import org.axan.sep.common.db.IGameEvent.IGameEventExecutor;
import org.axan.sep.common.db.orm.SEPCommonDB.eRelationTypes;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;

public class Fleet extends Unit implements IFleet
{
	/*
	 * PK: inherited
	 */
	
	/*
	 * Off-DB fields.
	 */
	private final Map<StarshipTemplate, Integer> starships = new HashMap<StarshipTemplate, Integer>();
	private final LinkedList<FleetMove> moves = new LinkedList<FleetMove>();
	
	/*
	 * DB connection
	 */
	
	/**
	 * Off-DB constructor.
	 * @param ownerName
	 * @param name
	 */
	public Fleet(String ownerName, String name, String productiveCelestialBodyName, Map<StarshipTemplate, Integer> starships)
	{
		super(ownerName, name, productiveCelestialBodyName);
		this.starships.putAll(starships);
	}
	
	/**
	 * On-DB constructor.
	 * @param sepDB
	 * @param ownerName
	 * @param name
	 */
	public Fleet(SEPCommonDB sepDB, String ownerName, String name)
	{
		super(sepDB, ownerName, name);
	}
	
	@Override
	final protected void checkForDBUpdate()
	{				
		super.checkForDBUpdate();
	}
	
	@Override
	protected void initializeProperties()
	{
		super.initializeProperties();
		initializeStarships(this, starships);
	}
	
	@Override
	final protected void register(Node properties)
	{
		super.register(properties);
	}
	
	@Override
	@OverridingMethodsMustInvokeSuper
	public void onArrival(IGameEventExecutor executor)
	{
		super.onArrival(executor);
		
		// TODO: nothing yet
	}
	
	@Override
	public boolean isAssignedFleet()
	{
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		return hasRelationship(eRelationTypes.AssignedFleets, Direction.INCOMING);
	}
	
	@Override
	public IFleetMarker getMarker(double step)
	{
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		return new FleetMarker(getTurn(), step, ownerName, name, isStopped(), getRealLocation(), getSpeed(), getStarships(), isAssignedFleet());
	}
	
	@Override
	public List<FleetMove> getMoves()
	{
		if (isDBOnline())
		{
			checkForDBUpdate();
			
			moves.clear();
			
			for(int i=0; properties.hasProperty("move"+i); ++i)
			{
				String destinationName = (String) properties.getProperty("move"+i);
				int delay = (Integer) properties.getProperty("move"+i+"_delay");
				boolean isAnAttack = (Boolean) properties.getProperty("move"+i+"_isAnAttack");
				FleetMove move = new FleetMove(destinationName, delay, isAnAttack);
				moves.add(move);
			}
		}
		
		return Collections.unmodifiableList(moves);
	}
	
	@Override
	public void updateMovesPlan(List<FleetMove> newPlan)
	{
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		assertLastVersion();
		
		Transaction tx = graphDB.getDB().beginTx();
		
		try
		{
			boolean modified = false;
			
			for(int i=0; i < newPlan.size() || properties.hasProperty("move"+i); ++i)
			{
				if (i < newPlan.size())
				{
					FleetMove move = newPlan.get(i);
					if (!modified)
					{
						modified = true;
						prepareUpdate();
					}
					properties.setProperty("move"+i, move.getDestinationName());
					properties.setProperty("move"+i+"_delay", move.getDelay());
					properties.setProperty("move"+i+"_isAnAttack", move.isAnAttack());
				}
				else
				{
					if (!modified)
					{
						modified = true;
						prepareUpdate();
					}
					properties.removeProperty("move"+i);
					properties.removeProperty("move"+i+"_delay");
					properties.removeProperty("move"+i+"_isAnAttack");
				}
			}
			
			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}
	
	@Override
	public boolean nextDestination()
	{
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		if (!isStopped()) return true;
		
		List<FleetMove> moves = new LinkedList<FleetMove>(getMoves());
		if (moves.isEmpty()) return false;
		
		FleetMove move = moves.get(0);
		if (move.getDelay() > 0)
		{
			Transaction tx = graphDB.getDB().beginTx();
			
			try
			{
				prepareUpdate();
				properties.setProperty("move0_delay", move.getDelay()-1);
				tx.success();
				return false;
			}
			finally
			{
				tx.finish();
			}
		}
		
		Transaction tx = graphDB.getDB().beginTx();		
		try
		{
			ICelestialBody cb = graphDB.getCelestialBody(move.getDestinationName());
			setDestination(cb.getLocation());
			
			moves.remove(move);
			updateMovesPlan(moves);
			
			tx.success();
			return true;
		}
		finally
		{
			tx.finish();
		}
	}
	
	@Override
	public Map<StarshipTemplate, Integer> getStarships()
	{
		if (isDBOnline())
		{
			checkForDBUpdate();
			
			for(StarshipTemplate template : Rules.getStarshipTemplates())
			{
				if (properties.hasProperty("starships"+template.getName()))
				{
					starships.put(template, (Integer) properties.getProperty("starships"+template.getName()));
				}
				else
				{
					starships.put(template, 0);
				}
			}
		}
		
		return Collections.unmodifiableMap(starships);
	}
	
	@Override
	public int getStarshipsCount()
	{		
		int qt = 0;
		for(int tqt : getStarships().values())
		{
			qt += tqt;
		}
		
		return qt;
	}
	
	@Override
	public synchronized void addStarships(Map<StarshipTemplate, Integer> newcomers)
	{
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		Transaction tx = graphDB.getDB().beginTx();		
		try
		{						
			getStarships();
			
			boolean modified = false;
			for(StarshipTemplate template : newcomers.keySet())
			{
				int newComersQuantity = newcomers.get(template);
				if (newComersQuantity < 0) throw new RuntimeException("Newcomers quantity cannot be negative.");
				int quantity = (starships.containsKey(template) ? starships.get(template) : 0) + newComersQuantity;
				starships.put(template, quantity);				
			}
			
			initializeStarships(this, starships);
			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}
	
	@Override
	public synchronized void removeStarships(Map<StarshipTemplate, Integer> leavers)
	{
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		Transaction tx = graphDB.getDB().beginTx();
		
		try
		{
			getStarships();
			
			for(StarshipTemplate template : leavers.keySet())
			{
				int leaversQuantity = leavers.get(template);
				if (leaversQuantity < 0) throw new RuntimeException("Leavers quantity cannot be negative.");
				int quantity = (starships.containsKey(template) ? starships.get(template) : 0) - leaversQuantity;
				if (quantity < 0) throw new RuntimeException("Leavers quantity cannot be greater than current quantity ("+template.getName()+")");
				starships.put(template, quantity);
			}
			
			initializeStarships(this, starships);			
			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}	

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder(super.toString());		
		if (!isDBOnline()) return sb.toString(); // TODO: implement Fleet offline version ?		
		
		checkForDBUpdate();
		
		sb.append("Fleet composition :\n");
		for(Map.Entry<StarshipTemplate, Integer> e : getStarships().entrySet())
		{
			if (e.getValue() != null && e.getValue() > 0)
			{
				sb.append("  "+Basic.padRight(e.getKey().getName(), 15, ' ')+"\t"+e.getValue()+"\n");
			}
		}
		
		return sb.toString();
	}
	
	Node getProperties()
	{
		return properties;
	}
	
	@Override
	protected void prepareUpdate()
	{
		super.prepareUpdate();
	}
	
	static void initializeStarships(AVersionedGraphNode versionedNode, Map<StarshipTemplate, Integer> starships)
	{
		FleetMarker fleetMarker = FleetMarker.class.isInstance(versionedNode) ? (FleetMarker) versionedNode : null;
		Fleet fleet = Fleet.class.isInstance(versionedNode) ? (Fleet) versionedNode : null;
		
		if (fleetMarker == null && fleet == null)
		{
			throw new RuntimeException("versionedNode must be instance of FleetMarker or instance of Fleet.");
		}
		
		Node properties = fleetMarker != null ? fleetMarker.getProperties() : fleet.getProperties();		
		
		int quantity = 0;
		boolean modified = false;
		for(StarshipTemplate template : Rules.getStarshipTemplates())		
		{
			quantity = (starships.containsKey(template)) ? starships.get(template) : 0;
			
			String starship = "starships"+template.getName();
			if (!modified && (!properties.hasProperty(starship) || ((Integer) properties.getProperty(starship)) != quantity))
			{
				modified = true;
				if (fleetMarker != null)
				{
					fleetMarker.prepareUpdate();
				}
				else
				{
					fleet.prepareUpdate();
				}
			}
			properties.setProperty("starships"+template.getName(), quantity);
		}
	}
}
