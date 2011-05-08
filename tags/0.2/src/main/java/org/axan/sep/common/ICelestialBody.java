/**
 * @author Escallier Pierre
 * @file CelestialBody.java
 * @date 3 juin 2009
 */
package org.axan.sep.common;


/**
 * Represent a celestial body in the universe, from a specific player point of view.
 */
public interface ICelestialBody extends IObservable
{
	String getName();
}