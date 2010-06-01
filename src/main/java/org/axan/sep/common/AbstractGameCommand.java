package org.axan.sep.common;

import java.io.Serializable;
import java.lang.reflect.Field;

import org.axan.eplib.utils.Basic;

public abstract class AbstractGameCommand<P, O extends AbstractGameCommandCheck> implements ILocalGameCommand, Serializable
{
	P params;
	
	public AbstractGameCommand(P params)
	{
		this.params = params;
	}
	
	public O can(PlayerGameBoard gameBoard)
	{
		return check(gameBoard);		
	}
	
	abstract protected O check(PlayerGameBoard gameBoard);
	
	@Override
	public PlayerGameBoard apply(PlayerGameBoard gameBoard) throws LocalGameCommandException
	{
		PlayerGameBoard nextGameBoard = Basic.clone(gameBoard);
		O check = check(nextGameBoard);
		if (!check.isPossible())
		{
			//if (check.getReason() != null) throw new LocalGameCommandException(check.getReason());
			throw new LocalGameCommandException(check.toString());
		}
		
		return apply(check);
	}
	
	@Override
	public P getParams()
	{
		return params;
	}
	
	abstract protected PlayerGameBoard apply(O check) throws LocalGameCommandException;
	
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append(getClass().getSimpleName()+"(");
		for(Field f : params.getClass().getFields())
		{
			try
			{
				sb.append(f.get(params).toString());
			}
			catch(Throwable t)
			{
				sb.append("?");
			}
			sb.append(", ");
		}
		if (params.getClass().getFields().length > 0) sb.delete(sb.length()-2, sb.length());
		sb.append(")");
		
		return sb.toString();
	}
}
