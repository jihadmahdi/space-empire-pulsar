/**
 * @author Escallier Pierre
 * @file MobileUnitMarker.java
 * @date 3 juin 2009
 */
package common;

import java.io.Serializable;

/**
 * 
 */
public class MobileUnitMarker implements IMarker, Serializable
{
	private static final long	serialVersionUID	= 1L;
	
	private final int creationDate;
	private final boolean isVisible;
	
	private final Unit unit;
	
	/**
	 * Full constructor.
	 */
	public MobileUnitMarker(int creationDate, boolean isVisible, Unit unit)
	{
		this.creationDate = creationDate;
		this.isVisible = isVisible;
		this.unit = unit;
	}

	/* (non-Javadoc)
	 * @see common.IMarker#getCreationDate()
	 */
	@Override
	public int getCreationDate()
	{
		return creationDate;
	}

	/* (non-Javadoc)
	 * @see common.IMarker#isVisible()
	 */
	@Override
	public boolean isVisible()
	{
		return isVisible;
	}

}