package org.axan.sep.client.gui;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.javabuilders.BuildException;
import org.javabuilders.BuildResult;
import org.javabuilders.DuplicateAliasException;
import org.javabuilders.annotations.DoInBackground;
import org.javabuilders.event.BackgroundEvent;
import org.javabuilders.event.ObjectMethod;
import org.javabuilders.swing.SwingJavaBuilder;
import org.javabuilders.swing.SwingJavaBuilderConfig;
import org.javabuilders.util.BuilderUtils;

public class SwingJavaBuilderMyUtils
{
	private static Logger log = Logger.getLogger(SpaceEmpirePulsarGUI.class.getName());
	
	private SwingJavaBuilderMyUtils()
	{		
	}
	
	/**
	 * Silent version of SwingJavaBuilderConfig#addType(Class...), no {@link DuplicateAliasException} thrown. 
	 * @see SwingJavaBuilderConfig#addType(Class...)
	 * @param classTypes
	 */
	static public void addType(Class<?> ... classTypes)
	{
		for(Class<?> classType: classTypes)
		{
			if (SwingJavaBuilder.getConfig().isTypeDefined(classType)) continue;
			try
			{
				SwingJavaBuilder.getConfig().addType(classType);
			}
			catch(DuplicateAliasException e)
			{
				log.log(Level.FINE, "DuplicateAliasException: "+e.getMessage());
			}
		}
	}
	
	/**
	 * Call given method on eventClassInstance as a background method (must marked with {@link DoInBackground} annotation).
	 * @param result BuildResult
	 * @param methodName Method to call
	 * @param eventClassInstance Instance to use
	 */
	static public void callBackgroundMethod(BuildResult result, String methodName, Object eventClassInstance)
	{
		callBackgroundMethod(result, methodName, "Anonymous", eventClassInstance);
	}
	
	/**
	 * Call given method on eventClassInstance as a background method (must marked with {@link DoInBackground} annotation).
	 * @param result BuildResult
	 * @param methodName Method to call
	 * @param mainObject Object responsible for the event (e.g. a button whose ActionPerformed event is calling current background method), this object is disabled during the method execution.
	 * @param eventClassInstance Instance to use
	 */
	static public void callBackgroundMethod(BuildResult result, String methodName, Object mainObject, Object eventClassInstance)
	{
		Method method;
		try
		{
			method = eventClassInstance.getClass().getMethod(methodName, BackgroundEvent.class);
			if (method.getAnnotation(DoInBackground.class) == null)
			{
				throw new BuildException("Method \"{0}\" in class \"{1}\" does not have \"{2}\" annotation.", methodName, eventClassInstance.getClass().getName(), DoInBackground.class.getName());
			}
		}
		catch(Exception e)
		{
			throw new BuildException(e, "Unable to find method to call for name \"{0}\" in class \"{1}\"", methodName, eventClassInstance.getClass().getName());
		}
		
		ObjectMethod objectMethod = new ObjectMethod(eventClassInstance, method);
		BuilderUtils.invokeCallerEventMethods(result, mainObject, Arrays.asList(objectMethod), eventClassInstance);
	}
	
	/**
	 * Generic version of {@link BuildResult#get(Object)}
	 * @param build
	 * @param name
	 * @param clazz
	 * @return
	 */
	static <T> T get(BuildResult build, String name, Class<T> clazz)
	{
		return clazz.cast(build.get(name));
	}
}
