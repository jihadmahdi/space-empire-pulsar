package org.axan.sep.common;

public interface ILocalGameCommand
{
	public static class LocalGameCommandException extends Exception
	{
		public LocalGameCommandException(String msg)
		{
			super(msg);
		}
		
		public LocalGameCommandException(Throwable t)
		{
			super(t);
		}
	}
	
	PlayerGameBoard apply(PlayerGameBoard gameBoard) throws LocalGameCommandException;
	Object getParams();
	//PlayerGameBoard undo(PlayerGameBoard gameBoard);
}
