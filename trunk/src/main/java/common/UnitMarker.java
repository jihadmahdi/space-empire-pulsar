package common;

import java.awt.Component;
import java.io.Serializable;

public class UnitMarker implements IMarker, Serializable
{
	private static final long	serialVersionUID	= 1L;
	
	private final int creationDate;
	private final boolean isVisible;
	private final Unit markedUnit;

	public UnitMarker(int creationDate, boolean isVisible, Unit markedUnit)
	{
		this.creationDate = creationDate;
		this.isVisible = isVisible;
		this.markedUnit = markedUnit;
	}
	
	@Override
	public int getCreationDate()
	{
		return creationDate;
	}

	@Override
	public boolean isVisible()
	{
		return isVisible;
	}

	public Unit getUnit()
	{
		return markedUnit;
	}
}
