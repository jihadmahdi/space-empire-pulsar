package org.axan.sep.common.db.orm;

import java.lang.Exception;

import org.axan.eplib.orm.DataBaseORMGenerator;
import org.axan.sep.common.db.orm.base.IBaseArea;
import org.axan.sep.common.db.orm.base.BaseArea;
import org.axan.sep.common.db.IArea;
import org.axan.sep.common.db.IAssignedFleet;
import org.axan.sep.common.db.ICelestialBody;
import org.axan.sep.common.db.IProbe;
import org.axan.sep.common.db.IProductiveCelestialBody;
import org.axan.sep.common.db.IUnit;
import org.axan.sep.common.db.SEPCommonDB;
import org.axan.sep.common.db.eRelationsTypes;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.IGameConfig;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ReturnableEvaluator;
import org.neo4j.graphdb.StopEvaluator;
import org.neo4j.graphdb.TraversalPosition;
import org.neo4j.graphdb.Traverser.Order;

public class Area implements IArea
{
	private final IBaseArea baseAreaProxy;
	private final Location location;

	Area(IBaseArea baseAreaProxy)
	{
		this.baseAreaProxy = baseAreaProxy;
		this.location = (baseAreaProxy.getLocation_x() == null ? null : new Location(baseAreaProxy.getLocation_x(), baseAreaProxy.getLocation_y(), baseAreaProxy.getLocation_z()));
	}

	public Area(Location location, Boolean isSun)
	{
		this(new BaseArea(location == null ? null : location.x, location == null ? null : location.y, location == null ? null : location.z, isSun));
	}

	public Area(Node stmnt)
	{
		this(new BaseArea(stmnt));
	}

	@Override
	public Location getLocation()
	{
		return location;
	}

	@Override
	public boolean isSun()
	{
		return baseAreaProxy.getIsSun();
	}
	
	@Override
	public boolean isVisible(SEPCommonDB db, final String playerName)
	{
		// TODO: Pulsar effect is not implemented yet
		
		// If player own a (productive) celestial body in this area
		ICelestialBody cb = db.getCelestialBody(getLocation());
		if (cb != null && IProductiveCelestialBody.class.isInstance(cb) && playerName.equals(IProductiveCelestialBody.class.cast(cb).getOwner())) return true;		
		
		// If player has assigned fleet in this area
		if (db.getAreaNode(getLocation()).traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, new ReturnableEvaluator()
		{
			
			@Override
			public boolean isReturnableNode(TraversalPosition currentPos)
			{
				Node n = currentPos.currentNode();
				return (n.hasRelationship(eRelationsTypes.AssignedFleet, Direction.OUTGOING) && playerName.equals(n.getProperty("owner")));
			}
		}, eRelationsTypes.AssignedFleet, Direction.INCOMING).iterator().hasNext()) return true;
		
		// If player has stopped fleet in this area
		if (db.getAreaNode(getLocation()).traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, new ReturnableEvaluator()
		{			
			@Override
			public boolean isReturnableNode(TraversalPosition currentPos)
			{
				Node n = currentPos.currentNode();
				return (n.hasRelationship(eUnitType.Fleet, Direction.OUTGOING) && playerName.equals(n.getProperty("owner")) && (false == ((((Integer) n.getProperty("progress")) > 0) && (((Integer) n.getProperty("progress")) != 1) && (n.getProperty("destination_x") != null) && (n.getProperty("departure_x") != null))) );
			}
		}, eUnitType.Fleet, Direction.INCOMING).iterator().hasNext()) return true;
		
		// If player has deployed probe to observe this area
		final double probeSight = db.getConfig().getUnitTypeSight(eUnitType.Probe);
		if (db.getAreaNode(getLocation()).traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, new ReturnableEvaluator()
		{
			
			@Override
			public boolean isReturnableNode(TraversalPosition currentPos)
			{
				if (currentPos.isStartNode()) return false;
				
				Node n = currentPos.currentNode();
				double distance = Math.pow(getLocation().x - (Integer) n.getProperty("departure_x"), 2) + Math.pow(getLocation().y - (Integer) n.getProperty("departure_y"), 2) + Math.pow(getLocation().z - (Integer) n.getProperty("departure_z"), 2);
				return (n.hasRelationship(eUnitType.Probe, Direction.OUTGOING) && playerName.equals(n.getProperty("owner")) && ((Integer) n.getProperty("progress") == 1) && distance <= probeSight);
			}
		}, eUnitType.Probe, Direction.INCOMING).iterator().hasNext()) return true;
		
		return false;
	}
	
	@Override
	public ICelestialBody getCelestialBody(SEPCommonDB db)
	{
		return db.getCelestialBody(getLocation());
	}
	
	@Override
	public <T extends IUnit> Set<T> getUnits(SEPCommonDB db, final Class<T> expectedType)
	{
		try
		{
			Set<T> result = new HashSet<T>();
			eUnitType type;
			try
			{
				type = eUnitType.valueOf(expectedType.getSimpleName().charAt(0) == 'I' ? expectedType.getSimpleName().substring(1) : expectedType.getSimpleName());
			}
			catch(IllegalArgumentException e)
			{
				type = null;
			}
			
			for(Node n : db.getAreaNode(getLocation()).traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, ReturnableEvaluator.ALL_BUT_START_NODE, type == null ? eRelationsTypes.Unit : type, Direction.INCOMING))
			{
				result.add(DataBaseORMGenerator.mapTo(expectedType, n));
			}
			return result;
		}
		catch(Throwable t)
		{
			throw new RuntimeException(t);
		}
	}

	public String toString(SEPCommonDB db, String playerName)
	{
		StringBuffer sb = new StringBuffer();
		
		if (isVisible(db, playerName))
		{
			sb.append("currently observed");
		}
		else
		{
			int lastObservation = -1;
			SEPCommonDB pDB = db;
			while(pDB.hasPrevious())
			{
				pDB = pDB.previous();
				if (isVisible(pDB, playerName))
				{
					lastObservation = pDB.getConfig().getTurn();
					break;
				}
			}
			
			sb.append((lastObservation < 0)?"never been observed":"last observation on turn "+lastObservation);
		}
		sb.append("\n");
		
		if (isSun())
		{
			sb.append("Sun\n");
		}
		else
		{
			ICelestialBody cb = getCelestialBody(db);
			if (cb != null)
			{
				// SUIS LA
				//sb.append(cb.toString(db, playerName)+"\n");
			}
		}
		
		Set<IUnit> units = getUnits(db, IUnit.class);
		if (units != null && !units.isEmpty())
		{
			sb.append("Units :\n");
			for(IUnit u : units)
			{
				sb.append("   ["+u.getOwner()+"] "+u.getName()+"\n");
			}
		}
		
		/*
		if (markers != null && !markers.isEmpty())
		{
			sb.append("Markers :\n");
			for(IMarker m : markers)
			{
				sb.append(m);
			}
		}
		*/
		
		return sb.toString();
	}
	
	public Map<String, Object> getNode()
	{
		return baseAreaProxy.getNode();
	}

}
