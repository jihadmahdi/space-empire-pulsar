package org.axan.sep.common;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.axan.sep.common.db.ICommand;
import org.axan.sep.common.db.IGameEvent;
import org.axan.sep.common.db.ICommand.GameCommandException;
import org.axan.sep.common.db.IGameEvent.GameEventException;
import org.axan.sep.common.db.IGameEvent.IGameEventExecutor;
import org.axan.sep.common.db.orm.SEPCommonDB;

public class PlayerGameboardView
{
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
		currentTurnEvents.addAll(events);
	}

	public synchronized void onLocalCommand(ICommand command) throws GameCommandException
	{
		if (hasEndedTurn()) throw new GameCommandException(command, "Cannot process command once turn is ended.");
		command.process(executor, db);
		currentTurnCommands.add(command);
	}
	
	public synchronized void onGameEvent(IGameEvent evt)
	{
		currentTurnEvents.add(evt);
	}

	public synchronized void resolveCurrentTurn() throws GameEventException
	{
		List<IGameEvent> resolvedEvents = new Vector<IGameEvent>();

		while (!currentTurnEvents.isEmpty())
		{
			IGameEvent evt = currentTurnEvents.get(0);
			currentTurnEvents.remove(0);
			evt.process(executor, db);
			resolvedEvents.add(evt);
			
			// If event change db turn, ensure to work with the most recent db.
			while(db.hasNext()) db = db.next();
		}
		
		loggedEvents.addAll(lastTurnCommands);
		lastTurnCommands.clear();
		loggedEvents.addAll(lastTurnEvents);
		lastTurnEvents.clear();
		
		lastTurnCommands.addAll(currentTurnCommands);
		currentTurnCommands.clear();
		lastTurnEvents.addAll(resolvedEvents);
		
		hasEndedTurn = false;
		
		// Increment turn
		db.getConfig().setTurn(db.getConfig().getTurn()+1);
		while(db.hasNext()) db = db.next();
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
