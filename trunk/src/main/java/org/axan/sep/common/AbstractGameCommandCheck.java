package org.axan.sep.common;

/**
 * This class represent an abstract game command check result.
 * This is used to say if a game command is possible or not, and provide additional informations (command price, fail reason, ...).
 * It is also used in command implementation as the check step is responsible for query gameBoard about the objects which command will modify.
 */
@Deprecated // Not used anymore in server package.
public abstract class AbstractGameCommandCheck
{
	/** Checked gameBoard. */
	private final PlayerGameBoard gameBoard;
	
	/** Check result, is command possible ? */
	private boolean isCommandPossible;
	
	/** If command is not possible, why ? */
	private String impossibilityReason = null;
	
	/** If the command check rose an exception. */
	private Throwable exception = null;
	
	/** If the command has a carbon price. */
	private Integer carbonPrice = null;
	
	/** If the command has a population price. */
	private Integer populationPrice = null;
	
	/**
	 * "Check failed with an exception" case.
	 * @param nextGameBoard
	 * @param exception
	 */	
	public void setException(Throwable exception)
	{
		this.exception = exception;
		if (exception.getMessage() != null) this.impossibilityReason = exception.getMessage();
		this.isCommandPossible = false;
	}
	
	/**
	 * "Check failed for a specific reason" case.
	 * @param impossibilityReason Impossibility reason message.
	 */
	public void setImpossibilityReason(String impossibilityReason)
	{
		this.impossibilityReason = impossibilityReason;
		this.isCommandPossible = false;
	}
	
	/**
	 * "Check passed, free command" constructor.
	 */
	public AbstractGameCommandCheck(PlayerGameBoard nextGameBoard)
	{
		this.gameBoard = nextGameBoard;
		
		this.isCommandPossible = true;
	}

	/**
	 * Set command price.
	 * @param carbonPrice
	 * @param populationPrice
	 */
	public void setPrice(int carbonPrice, int populationPrice)
	{
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
	
	/**
	 * Implementation class must print a useful string of the command check result.
	 */
	@Override
	public String toString()
	{
		// TODO: Set this method abstract so each command check must define a proper customised msg.
		return (isPossible() ? (isPriceDefined() ? getCarbonPrice()+"C, "+getPopulationPrice()+"P" : "") : getReason());
	}
	
	PlayerGameBoard getGameBoard()
	{
		return gameBoard;
	}
}
