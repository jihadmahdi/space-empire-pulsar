package org.axan.sep.common;

public interface IGameCommand
{
	public static class GameCommandException extends Exception
	{
		public GameCommandException(String msg)
		{
			super(msg);
		}
		
		public GameCommandException(Throwable t)
		{
			super(t);
		}
	}
	
	PlayerGameBoard apply(PlayerGameBoard gameBoard) throws GameCommandException;
	Object getParams();
	//PlayerGameBoard undo(PlayerGameBoard gameBoard);
}
