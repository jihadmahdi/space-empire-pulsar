package server.model;

import java.io.Serializable;

public final class UnitMarker implements IMarker, Serializable
{
	private static final long	serialVersionUID	= 1L;
	
	private final int creationDate;
	private final IMarker.Key key;
	private final common.Unit markedUnit;
	
	public UnitMarker(int creationDate, String name, String observerName, common.Unit markedUnit)
	{
		this(creationDate, new IMarker.Key(name, UnitMarker.class, observerName), markedUnit);
	}
	
	public UnitMarker(int creationDate, IMarker.Key key, common.Unit markedUnit)
	{
		this.creationDate = creationDate;
		this.key = key;
		this.markedUnit = markedUnit;
	}

	@Override
	public int getCreationDate()
	{
		return creationDate;
	}

	@Override
	public Key getKey()
	{
		return key;
	}
	
	@Override
	public String getName()
	{
		return key.getName();
	}

	@Override
	public String getObserverName()
	{
		return key.getObserverName();
	}

	@Override
	public boolean isValid()
	{
		// TODO Auto-generated method stub
		return true;
	}

	public common.Unit getUnit()
	{
		return markedUnit;
	}
}
