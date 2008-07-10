/**
 * @author Escallier Pierre
 * @file SEPUtils.java
 * @date 23 juin 08
 */
package utils;

import java.io.Serializable;

import com.sun.sgs.app.Task;

/**
 * 
 */
public class SEPUtils
{
	/**
	 * Vérifie le nombre (minimum) et le type des paramètres.
	 * Note: Le nombre de types passés devrait toujours être égal au suppérieur à minNb.
	 * @param minNb
	 * @param parameters
	 * @param types
	 */
	public static void checkParametersTypes(int minNb, Object[] parameters, String errorMsgBase, Class<?> ... types)
	{
		if (parameters.length < minNb)
		{
			throw new IllegalArgumentException(errorMsgBase+" : "+minNb+" parameter(s) expected, "+parameters.length+" given.");
		}

		for(int i=0; i < parameters.length; ++i)
		{
			Class<?> c = (i < types.length)?types[i]:types[types.length-1];
			
			if (!c.isInstance(parameters[i]))
			{
				throw new IllegalArgumentException(errorMsgBase+" : parameters["+i+"] expected to be a \""+c.getName()+"\" instance, but is a \""+parameters[i].getClass().getName()+"\" one.");
			}
		}
	}
	
	public static interface SerializableTask extends Serializable, Task {}
	public static interface SerializableRunnable extends Serializable, Runnable {}
	
	public static class SerializableThread extends Thread implements Serializable
	{
		private static final long	serialVersionUID	= 1L;

		/**
		 * @param target
		 * @param name
		 */
		public SerializableThread(SerializableRunnable target, String name)
		{
			super(target, name);
			// TODO Auto-generated constructor stub
		}
		
	}
}
