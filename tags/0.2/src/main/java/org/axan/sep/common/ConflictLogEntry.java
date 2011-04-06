package org.axan.sep.common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.axan.eplib.utils.Basic;

public class ConflictLogEntry extends ALogEntry.AUpdatableLogEntry<ConflictLogEntry> implements Serializable
{
	// Constants
	private final String celestialBodyName;	
	
	// Variables
	private int round;
	private final Map<Integer, Map<String, Fleet>> startingForcesByRound;
	private final Map<Integer, Map<String, Map<String, Boolean>>> conflictDiplomacyByRound;
	private final Map<Integer, Map<String, Fleet>> survivingForcesByRound;
	
	public ConflictLogEntry(int date, float instantTime, int round, String celestialBodyName, Map<String, Fleet> startingForces, Map<String, Map<String, Boolean>> conflictDiplomacy, Map<String, Fleet> survivingForces)
	{
		super(date, instantTime);
		this.round = round;
		this.celestialBodyName = celestialBodyName;
		this.startingForcesByRound = new HashMap<Integer, Map<String, Fleet>>();
		this.startingForcesByRound.put(round, startingForces);
		this.conflictDiplomacyByRound = new HashMap<Integer, Map<String,Map<String,Boolean>>>();
		this.conflictDiplomacyByRound.put(round, conflictDiplomacy);
		this.survivingForcesByRound = new HashMap<Integer, Map<String,Fleet>>();
		this.survivingForcesByRound.put(round, survivingForces);
	}
	
	private static Map<String, Map<String, Boolean>> mergeDiplomacy(Map<String, Map<String, Boolean>> current, Map<String, Map<String, Boolean>> next)
	{
		Map<String, Map<String, Boolean>> merged = new HashMap<String, Map<String,Boolean>>();
		for(String p1 : current.keySet())
		{
			merged.put(p1, new HashMap<String, Boolean>());
			for(String p2 : current.get(p1).keySet())
			{
				merged.get(p1).put(p2, current.get(p1).get(p2));
				if (next.containsKey(p1) && next.get(p1).containsKey(p2))
				{
					merged.get(p1).put(p2, next.get(p1).get(p2));
				}
			}
		}
		
		return merged;
	}
	
	public Map<String, Map<String, Boolean>> getGlobalDiplomacy()
	{
		TreeSet<Integer> keys = new TreeSet<Integer>(conflictDiplomacyByRound.keySet());
		Iterator<Integer> it = keys.iterator();
		int i = it.next();		
		Map<String, Map<String, Boolean>> result = conflictDiplomacyByRound.get(i);		
		
		while(it.hasNext())
		{
			int n = it.next();
			result = mergeDiplomacy(result, conflictDiplomacyByRound.get(n));
		}
		
		return result;
	}
		
	@Override
	public String toString()
	{		
		StringBuffer sb = new StringBuffer();
		
		int firstRound = new TreeSet<Integer>(startingForcesByRound.keySet()).iterator().next();
		
		sb.append("Conflict on "+celestialBodyName+" resolved in "+round+" rounds :\n");
		Map<String, Map<String, Boolean>> globalDiplomacy = getGlobalDiplomacy();		
		
		sb.append("Global conflict diplomacy :\n");
		boolean fought = false;
		for(String player : globalDiplomacy.keySet())
		{
			if (player == null) continue;
			if (!startingForcesByRound.get(firstRound).containsKey(player) || startingForcesByRound.get(firstRound).get(player) == null) continue;
			
			Set<String> opponents = new HashSet<String>();
			
			for(String opponent : globalDiplomacy.get(player).keySet())
			{
				if (opponent == null) continue;
				if (!startingForcesByRound.get(firstRound).containsKey(opponent) || startingForcesByRound.get(firstRound).get(opponent) == null) continue;
				
				if (globalDiplomacy.get(player).get(opponent))
				{
					opponents.add(opponent);
					fought = true;
				}								
			}
			
			if (!opponents.isEmpty())
			{
				sb.append(player+" VS "+Basic.joinStringElements(opponents.toArray(new String[opponents.size()]), ", ")+"\n");
			}
		}				
		
		if (!fought)
		{
			sb.append("No fight\n");
			return sb.toString();
		}
		
		sb.append("\nStarting forces :\n");		
		
		for(String player : startingForcesByRound.get(firstRound).keySet())
		{
			sb.append(player+" :\n");
			Fleet startingFleet = startingForcesByRound.get(firstRound).get(player);
			Fleet endingFleet = (survivingForcesByRound.get(round).containsKey(player) ? survivingForcesByRound.get(round).get(player) : null);
			Map<StarshipTemplate, Integer> endStarships = (endingFleet == null ? null :endingFleet.getStarships());
			Set<ISpecialUnit> endSpecialUnits = (endingFleet == null ? null : endingFleet.getSpecialUnits());
			
			sb.append("\nFleet composition (survivors / total) :\n");
			for(Map.Entry<StarshipTemplate, Integer> e : startingFleet.getStarships().entrySet())
			{
				if (e.getValue() != null && e.getValue() > 0)
				{
					sb.append("  "+e.getKey().getName()+"\t"+(endingFleet == null || !endStarships.containsKey(e.getKey()) ? 0 : endStarships.get(e.getKey()))+"/"+e.getValue()+"\n");
				}
			}
			
			if (startingFleet.getSpecialUnits() != null && !startingFleet.getSpecialUnits().isEmpty())
			{
				sb.append("\nSpecial units :\n");
				
				for(ISpecialUnit u : startingFleet.getSpecialUnits())
				{
					sb.append("  "+u.toString()+(endingFleet == null || !endSpecialUnits.contains(u) ? " [dead]" : "")+"\n");
				}
			}
			
			sb.append("\n");
		}
		
		return sb.toString();
	}

	@Override
	protected String getALogEntryUID()
	{
		return String.format("%d-%s", getCreationDate(), celestialBodyName);
	}

	@Override
	public Class<ConflictLogEntry> getType()
	{
		return ConflictLogEntry.class;
	}
	
	@Override
	public ConflictLogEntry update(AUpdatableLogEntry<?> o)
	{
		if (!getUID().equals(o.getUID())) throw new IllegalArgumentException("Cannot update log entry with different uids.");
		if (!getType().equals(o.getType())) throw new IllegalArgumentException("Cannot update log entry with different final class.");
		
		ConflictLogEntry up = ConflictLogEntry.class.cast(o);
		
		conflictDiplomacyByRound.putAll(up.conflictDiplomacyByRound);
		startingForcesByRound.putAll(up.startingForcesByRound);
		survivingForcesByRound.putAll(up.survivingForcesByRound);
		round = Math.max(round, up.round);
		
		return this;
	}
}
