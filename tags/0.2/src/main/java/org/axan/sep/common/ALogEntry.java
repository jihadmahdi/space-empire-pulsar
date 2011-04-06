package org.axan.sep.common;

import java.io.Serializable;
import java.util.Set;

/**
 * Describes a player log entry.
 */
public abstract class ALogEntry implements Comparable<ALogEntry>, Serializable
{
	private static final int M_ENC = 100000;
	private final double creationDate;			
	
	public ALogEntry(int creationDate, double instantTime)
	{
		if (instantTime < 0 || instantTime >= 1) throw new IllegalArgumentException("instantTime must be between [0 and 1[");
		this.creationDate = creationDate + instantTime;
	}
	
	/**
	 * @return The game date of the creation of this log.
	 */
	final public int getCreationDate()
	{
		return (int) Math.floor(creationDate);
	}
	
	/**
	 * @return The game instant date of the creation of this log.
	 */
	final public double getCreationInstantDate()
	{
		return creationDate;
	}
	
	/**
	 * Must return a unique Id for this log.
	 * The UID must be computed from initial parameters that remain constant so that a future new log will claim the same UID and will be used to merge.
	 * @return Unique id of this log. 
	 */
	final public String getUID()
	{
		return String.format("%s-%s", getClass().getSimpleName(), getALogEntryUID());
	}
	
	/**
	 * Must return a unique Id for this log.
	 * The UID must be computed from initial parameters that remain constant so that a future new log will claim the same UID and will be used to merge.
	 * @return Unique id of this log. 
	 */
	protected abstract String getALogEntryUID();
	
	
	/**
	 * @return Human readable log, as complete as possible.
	 */
	public abstract String toString();
	
	@Override
	final public boolean equals(Object obj)
	{
		if (obj == null) return false;
		if (!ALogEntry.class.isInstance(obj)) return false;
		
		return getUID().equals(ALogEntry.class.cast(obj).getUID());
	}

	/**
	 * Used to compare different final instances of ALogEntry.
	 * Subclass must override this method if the order should not be indexed by creation date.
	 */
	protected double getOrder()
	{
		return creationDate;
	}
	
	@Override
	public int compareTo(ALogEntry o)
	{
		int r = Double.compare(getOrder(), o.getOrder());
		if (r != 0) return r;
		return getClass().getSimpleName().compareTo(o.getClass().getSimpleName());
	}

	public static abstract class AUpdatableLogEntry<T extends ALogEntry> extends ALogEntry
	{		
		private static final long	serialVersionUID	= 1829417193080914290L;
		
		public AUpdatableLogEntry(int creationDate, double instantTime)
		{
			super(creationDate, instantTime);
		}
		
		public abstract T update(AUpdatableLogEntry<?> o);
		public abstract Class<T> getType();		
	}
	
	public static void addUpdateLogEntry(Set<ALogEntry> logs, ALogEntry logEntry)
	{
		if (AUpdatableLogEntry.class.isInstance(logEntry))
		{
			AUpdatableLogEntry<?> ne = AUpdatableLogEntry.class.cast(logEntry);
			for(ALogEntry e : logs)
			{
				if (logEntry.getUID().equals(e.getUID()) && AUpdatableLogEntry.class.isInstance(e))
				{
					AUpdatableLogEntry<?> oe = AUpdatableLogEntry.class.cast(e);
					if (oe.getType().equals(ne.getType()))
					{
						logs.remove(oe);
						ne = (AUpdatableLogEntry<?>) ne.update(oe);
						logs.add(ne);
						return;
					}
				}
			}
		}
		
		logs.add(logEntry);
	}
}
