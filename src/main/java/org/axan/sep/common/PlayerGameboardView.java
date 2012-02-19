package org.axan.sep.common;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.axan.eplib.utils.Profiling.ExecTimeMeasures;
import org.axan.sep.common.db.Commands.UpdateDiplomacy;
import org.axan.sep.common.db.Commands.UpdateFleetMovesPlan;
import org.axan.sep.common.db.Events.IPerTurnEvent;
import org.axan.sep.common.db.Events.IncrementTurn;
import org.axan.sep.common.db.ICommand;
import org.axan.sep.common.db.IGameEvent;
import org.axan.sep.common.db.Events.IConditionalEvent;
import org.axan.sep.common.db.ICommand.GameCommandException;
import org.axan.sep.common.db.IGameEvent.GameEventException;
import org.axan.sep.common.db.IGameEvent.IGameEventExecutor;
import org.axan.sep.common.db.orm.SEPCommonDB;

public class PlayerGameboardView
{
	private final IGameEventExecutor nullExecutor = new IGameEventExecutor()
	{		
		@Override
		public String getCurrentViewPlayerName()
		{
			return playerName;
		}
		
		@Override
		public void onGameEvent(IGameEvent event, Set<String> observers)
		{
		}
	};
	
	/** Executor used to generate sub events. */
	private final IGameEventExecutor executor;
	
	/** Player name. */
	private final String playerName;

	/** Already processed event archive. */
	private final List<IGameEvent> loggedEvents = new Vector<IGameEvent>();

	/**
	 * Commands resolved during previous turn. Commands are separated from events because clients does not need them to be send from server on new turn (clients already processed them).
	 */
	private final List<ICommand> lastTurnCommands = new Vector<ICommand>();
	
	/**
	 * Events resolved on previous turn resolution. Events to be sent with
	 * {@link Client#receiveNewTurnGameBoard(List)}.
	 */
	private final List<IGameEvent> lastTurnEvents = new Vector<IGameEvent>();

	/**
	 * Commands generated on current turn. Unlike other events, commands are processed as soon as they are fired.
	 */
	private final List<ICommand> currentTurnCommands = new Vector<ICommand>();
	
	/**
	 * Events generated on current turn, before turn resolution.
	 */
	private final List<IGameEvent> currentTurnEvents = new Vector<IGameEvent>();
	
	/**
	 * While this flag is on, no new Command event are accepted.
	 * Flag is reset on resolveTurn call.
	 */
	private boolean hasEndedTurn = false;

	private SEPCommonDB db;

	public PlayerGameboardView(SEPCommonDB newDB, IGameEventExecutor executor)
	{
		this(null, newDB, executor);
	}
	
	public PlayerGameboardView(String playerName, SEPCommonDB newDB, IGameEventExecutor executor)
	{
		this.executor = executor;
		this.playerName = playerName;
		this.db = newDB;
	}
	
	public String getName()
	{
		return playerName;
	}
		
	/**
	 * Should be used read-only, all update queries must be done within event processing.
	 * @return
	 */
	public SEPCommonDB getDB()
	{
		return db;
	}

	public List<IGameEvent> getLoggedEvents()
	{
		return Collections.unmodifiableList(loggedEvents);
	}
	
	public List<IGameEvent> getLastTurnEvents()
	{
		return Collections.unmodifiableList(lastTurnEvents);
	}
	
	public List<ICommand> getCurrentTurnCommands()
	{
		return Collections.unmodifiableList(currentTurnCommands);
	}

	public synchronized void onGameEvents(Collection<? extends IGameEvent> events)
	{
		for(IGameEvent event : events)
		{
			onGameEvent(event);
		}
	}

	public synchronized void onLocalCommand(ICommand command) throws GameCommandException
	{
		if (hasEndedTurn()) throw new GameCommandException(command, "Cannot process command once turn is ended.");
		command.process(executor, db);

		if (IPerTurnEvent.class.isInstance(command))
		{
			IPerTurnEvent perTurnEvent = (IPerTurnEvent) command;
			
			for(ICommand c : currentTurnCommands)
			{
				if (!perTurnEvent.getClass().isInstance(c)) continue;
				IPerTurnEvent p = (IPerTurnEvent) c;
				if (perTurnEvent.replace(p))
				{
					currentTurnCommands.remove(p);
					break;
				}
			}
		}
		
		// Ensure we keep only the last UpdateFleetMovesPlan command foreach fleet.
		if (UpdateFleetMovesPlan.class.isInstance(command))
		{
			UpdateFleetMovesPlan updateFleetMovesPlan = (UpdateFleetMovesPlan) command;
			//Set<UpdateFleetMovesPlan> previousFleetMovesPlanUpdates = new HashSet<UpdateFleetMovesPlan>();
			for(ICommand c : currentTurnCommands)
			{
				if (!UpdateFleetMovesPlan.class.isInstance(c)) continue;
				UpdateFleetMovesPlan u = (UpdateFleetMovesPlan) c;
				if (updateFleetMovesPlan.getPlayerName().equals(u.getPlayerName()) && updateFleetMovesPlan.getFleetName().equals(u.getFleetName()))
				{
					currentTurnCommands.remove(u);
					break;
				}
			}
		}
		
		// Ensure we keep only the last UpdateDiplomacy command foreach ownerVtarget.
		if (UpdateDiplomacy.class.isInstance(command))
		{
			UpdateDiplomacy updateDiplomacy = (UpdateDiplomacy) command;
			//Set<UpdateDiplomacy> previousDiplomacyUpdates = new HashSet<UpdateDiplomacy>();
			for(ICommand c : currentTurnCommands)
			{
				if (!UpdateDiplomacy.class.isInstance(c)) continue;
				UpdateDiplomacy u = (UpdateDiplomacy) c;
				if (updateDiplomacy.getPlayerName().equals(u.getPlayerName()) && updateDiplomacy.getTargetName().equals(u.getTargetName()))
				{
					currentTurnCommands.remove(u);
					break;
				}
			}
		}
		
		currentTurnCommands.add(command);
	}
	
	public synchronized void onGameEvent(IGameEvent evt)
	{
		if (IConditionalEvent.class.isInstance(evt))
		{
			IConditionalEvent conditionalEvent = (IConditionalEvent) evt;
			if (!conditionalEvent.test(db, playerName)) return;
		}
		
		// Skip if current and previous event are both IncrementTurn
		if (IncrementTurn.class.isInstance(evt) && !currentTurnEvents.isEmpty() && IncrementTurn.class.isInstance(currentTurnEvents.get(currentTurnEvents.size()-1))) return;
		
		if (IPerTurnEvent.class.isInstance(evt))
		{
			IPerTurnEvent perTurnEvent = (IPerTurnEvent) evt;
			
			for(IGameEvent e : currentTurnEvents)
			{
				if (!perTurnEvent.getClass().isInstance(e)) continue;
				IPerTurnEvent p = (IPerTurnEvent) e;
				if (perTurnEvent.replace(p))
				{
					currentTurnEvents.remove(p);
					break;
				}
			}
		}
		
		currentTurnEvents.add(evt);
	}
	
	public synchronized List<IGameEvent> exportEvents()
	{
		List<IGameEvent> events = new LinkedList<IGameEvent>(loggedEvents);
		events.addAll(lastTurnCommands);
		events.addAll(lastTurnEvents);		
		return events;
	}
	
	public synchronized void importEvents(List<IGameEvent> events) throws GameEventException
	{
		resolveEvents(nullExecutor, events);
	}
	
	public synchronized void resolveCurrentTurn() throws GameEventException
	{		
		onGameEvent(new IncrementTurn());
		resolveEvents(executor, currentTurnEvents);
	}
	
	public synchronized void resolveEvents(IGameEventExecutor executor, List<IGameEvent> events) throws GameEventException
	{
		List<IGameEvent> resolvedEvents = new Vector<IGameEvent>();
		
		ExecTimeMeasures etm = new ExecTimeMeasures();
		
		while(!events.isEmpty())
		{
			IGameEvent evt = events.get(0);
			events.remove(0);
			
			etm.measures("process "+evt.getClass().getSimpleName());
			evt.process(executor, db);			
			
			if (ICommand.class.isInstance(evt))
			{
				ICommand cmd = (ICommand) evt;
				currentTurnCommands.add(cmd);
			}
			else
			{
				resolvedEvents.add(evt);
			}
			
			while(db.hasNext())
			{
				etm.measures("next db");
				
				loggedEvents.addAll(lastTurnCommands);
				lastTurnCommands.clear();
				loggedEvents.addAll(lastTurnEvents);
				lastTurnEvents.clear();
				
				lastTurnCommands.addAll(currentTurnCommands);
				currentTurnCommands.clear();
				lastTurnEvents.addAll(resolvedEvents);
				resolvedEvents.clear();
				
				hasEndedTurn = false;
				
				db = db.next();
			}
		}
		
		etm.measures("resolved");
		System.err.println(etm.toString());
	}

	public synchronized void endTurn()
	{
		hasEndedTurn = true;
	}
	
	public synchronized boolean hasEndedTurn()
	{
		return hasEndedTurn;
		//return currentTurnEvents != null && !currentTurnEvents.isEmpty();
	}
}
