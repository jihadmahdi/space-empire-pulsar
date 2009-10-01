package common;

import java.io.Serializable;

import org.omg.CORBA.BooleanHolder;

/**
 * This class represent a command possibility result (is the command possible or not), it provide optional impossibility reason message.
 */
public class CommandCheckResult implements Serializable
{
	private static final long	serialVersionUID	= 1L;
	
	private final boolean isCommandPossible;
	private final String impossibilityReason;
	private final Throwable exception;
	
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
		this.exception = null;
	}
	
	/**
	 * Possibility case.
	 */
	public CommandCheckResult()
	{
		this.isCommandPossible = true;
		this.impossibilityReason = null;
		this.exception = null;
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
