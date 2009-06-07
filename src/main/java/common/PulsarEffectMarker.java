/**
 * @author Escallier Pierre
 * @file PulsarEffectMarker.java
 * @date 3 juin 2009
 */
package common;

import java.io.Serializable;

/**
 * 
 */
public class PulsarEffectMarker implements IMarker, Serializable
{
	private static final long	serialVersionUID	= 1L;
	
	private final int creationDate;
	private final boolean isVisible;
	
	/**
	 * Full constructor.
	 */
	public PulsarEffectMarker(int creationDate, boolean isVisible)
	{
		this.creationDate = creationDate;
		this.isVisible = isVisible;
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
