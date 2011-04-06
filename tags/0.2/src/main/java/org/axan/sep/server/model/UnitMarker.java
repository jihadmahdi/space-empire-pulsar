package org.axan.sep.server.model;

import java.io.Serializable;

public final class UnitMarker implements IMarker, Serializable
{
	private static final long	serialVersionUID	= 1L;
	
	private final int creationDate;
	private final IMarker.Key key;
	private final org.axan.sep.common.Unit markedUnit;
	
	public UnitMarker(int creationDate, String name, String observerName, org.axan.sep.common.Unit markedUnit)
	{
		this(creationDate, new IMarker.Key(name, UnitMarker.class, observerName), markedUnit);
	}
	
	public UnitMarker(int creationDate, IMarker.Key key, org.axan.sep.common.Unit markedUnit)
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

	public org.axan.sep.common.Unit getUnit()
	{
		return markedUnit;
	}
	
	public org.axan.sep.common.UnitMarker getView(boolean isVisible)
	{
		return new org.axan.sep.common.UnitMarker(creationDate, isVisible, markedUnit); 
	}
}
