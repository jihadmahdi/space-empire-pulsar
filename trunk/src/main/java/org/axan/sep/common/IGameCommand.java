package org.axan.sep.common;

public interface IGameCommand<T extends PlayerGameBoard>
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
	
	T apply(T gameBoard) throws GameCommandException;
	Object getParams();
	//PlayerGameBoard undo(PlayerGameBoard gameBoard);
}
