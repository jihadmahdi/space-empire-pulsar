package org.axan.sep.common;

import java.io.Serializable;

import org.omg.CORBA.BooleanHolder;

/**
 * This class represent a command possibility result (is the command possible or not), it provide optional impossibility reason message.
 */
public class CommandCheckResult implements Serializable
{
	private static final long	serialVersionUID	= 1L;
	
	private final boolean isCommandPossible;
	private String impossibilityReason = null;
	private Throwable exception = null;
	private Integer carbonPrice = null;
	private Integer populationPrice = null;
	
	public CommandCheckResult(Throwable exception)
	{
		this.exception = exception;
		this.impossibilityReason = exception.getMessage();
		this.isCommandPossible = false;
	}
	
	/**
	 * Impossibility case.
	 * @param impossibilityReason Impossibility reason message.
	 */
	public CommandCheckResult(String impossibilityReason)
	{
		this.isCommandPossible = false;
		this.impossibilityReason = impossibilityReason;
	}
	
	/**
	 * Possibility case.
	 */
	public CommandCheckResult()
	{
		this.isCommandPossible = true;
	}
	
	public CommandCheckResult(int carbonPrice, int populationPrice)
	{
		this.isCommandPossible = true;
		this.carbonPrice = carbonPrice;
		this.populationPrice = populationPrice;
	}
	
	public boolean isPriceDefined()
	{
		return ((carbonPrice != null && carbonPrice > 0) || (populationPrice != null && populationPrice > 0));
	}
	
	public int getCarbonPrice()
	{
		return carbonPrice == null ? 0 : carbonPrice;
	}
	
	public int getPopulationPrice()
	{
		return populationPrice == null ? 0 : populationPrice;
	}
	
	public boolean isPossible()
	{
		return isCommandPossible;
	}
	
	public String getReason()
	{
		return impossibilityReason;
	}
	
	public Throwable getException()
	{
		return exception;
	}
}
