/**
 * @author Escallier Pierre
 * @file Marker.java
 * @date 3 juin 2009
 */
package org.axan.sep.common;


/**
 * Represent a marker from a specific player point of view.
 * A marker is something used to notice the player about a specific event, but player is free to ignore and delete it.
 * ie: A fleet in travel has seen an enemy probe, when it arrive a marker is added in the player view to notice him about the probe location.
 * 
 * Note: To mark several area with the same marker just use the same instance.
 */
public interface IMarker
{
	int getCreationDate();
	boolean isVisible();
}
