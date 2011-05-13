package org.axan.sep.common;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Vector;

public class GameConfigCopier
{	
	public static class GameConfigCopierException extends Exception
	{
		private static final long serialVersionUID = 1L;

		public GameConfigCopierException(String message)
		{
			super(message);
		}		
		
		public GameConfigCopierException(String message, Throwable cause)
		{
			super(message, cause);
		}
	}
	
	public static <T> void copy(Class<T> clazz, T source, T destination) throws GameConfigCopierException
	{	
		for(Method getter : clazz.getMethods())
		{
			
			System.err.println(getter.toGenericString());
			
			if (isGetter(getter))
			{
				Method setter = getSetter(clazz, getter);
				Class<?> getterParams[] = getter.getParameterTypes();
				Class<?> setterParams[] = setter.getParameterTypes();
				//int nbParams = setter.getParameterTypes().length;
				int nbSetterArrayParams = (setter.getParameterTypes().length - getterParams.length);
				if (nbSetterArrayParams < 2) nbSetterArrayParams = 0;
				
				//int arrayParams = (nbParams - getterParams.length - 1);
				
				if (nbSetterArrayParams > 0 && !getter.getReturnType().isArray())
				{
					throw new GameConfigCopierException("As Getter return value is not an Array, Getter/Setter number of parameters is invalid for "+clazz.getName()+" / "+getter.toGenericString());
				}
				
				Object params[] = new Object[setterParams.length];
				
				if (getterParams.length == 0)
				{
					try
					{
						setter.invoke(destination, returnParams(params, getter.invoke(source), 0, nbSetterArrayParams));
					}
					catch(Throwable t)
					{
						throw new GameConfigCopierException("Invocation exception", t);
					}
				}
				else
				{
					int seen[] = new int[getterParams.length];
					for(int i=0; i < seen.length; ++i) seen[i]=0;
					
					for(int i=0; i >= 0; ++i) // We play with i in the loop so it actually decrease.
					{
						Class<?> paramType = getterParams[i];
						if (!paramType.isEnum()) throw new GameConfigCopierException("All getter parameters must be enum types: "+clazz.getName()+" / "+getter.toGenericString());
						Object[] enumConstants = paramType.getEnumConstants();
						int currentEnumCursor = seen[i];
						
						++seen[i];
						
						if (currentEnumCursor < enumConstants.length)
						{														       
							// Set current param value to current seen cursor, and ++cursor.
							params[i] = enumConstants[currentEnumCursor];
							
							// If last parameters, write.
							if (i == (getterParams.length-1))
							{
								Object getterParamsValues[] = Arrays.copyOf(params, getterParams.length);
								try
								{
									params = returnParams(params, getter.invoke(source, getterParamsValues), getterParams.length, nbSetterArrayParams);
									
									boolean skip = false;
									for(int j=0; j<params.length; ++j)
									{
										if (params[j] == null && setterParams[j].isPrimitive())
										{
											// If we compute null for a primitive type parameter, then it is a undefined combination of enum parameters. Should be ok to skip.
											skip = true;
											break;
										}
									}
									
									if (!skip)
									{
										setter.invoke(destination, params);
									}
								}
								catch(Throwable t)
								{
									throw new GameConfigCopierException("Invocation exception", t);
								}
								--i;
							}
						}
						else
						{							
							i -= 2;
						}						
					}
				}
			}
		}
	}
	
	/**
	 * Check if returnValue is an array (if array is expected) and add values to currentParams.
	 * returnValue can be null, if it is expected to be an array, every value is set to null.
	 * 
	 * @param currentParams
	 * @param returnValue
	 * @param offset
	 * @param returnValuesLength
	 * @return
	 * @throws GameConfigCopierException
	 */
	private static Object[] returnParams(Object[] currentParams, Object returnValue, int offset, int returnValuesLength) throws GameConfigCopierException
	{
		// offset = getterParams.length
		// IF returnValuesLength > 0, returnValue must be an array, and expected to be this length.
		// ELSE returnValue might be an array, but is processed as a single parameter.
		
		if (returnValuesLength > 0)
		{
			if (returnValue != null && !returnValue.getClass().isArray()) throw new GameConfigCopierException("Getter return value should be an array.");
				
			for(int j=0; j<returnValuesLength; ++j)
			{				
				currentParams[offset+j] = (returnValue == null)? null : Array.get(returnValue, j);
			}										
		}
		else
		{
			currentParams[offset] = returnValue;
		}
		
		return currentParams;
	}
	
	private static boolean isGetter(Method method)
	{
		// Method must have a return value.
		if (method.getReturnType().equals(void.class)) return false;
		
		// Method name must start with "get", "is", or "has".
		if (method.getName().startsWith("get") || method.getName().startsWith("is") || method.getName().startsWith("has")) return true;
		
		return false;
	}
	
	private static Method getSetter(Class<?> clazz, Method getter) throws GameConfigCopierException
	{
		if (!isGetter(getter)) throw new GameConfigCopierException(getter.toGenericString()+" is not a valid getter.");
		String key = getter.getName();
		if (key.startsWith("get") || key.startsWith("has"))
		{
			key = key.substring(3);
		}
		else if (key.startsWith("is"))
		{
			key = key.substring(2);
		}
		
		// int getInt() -> void setInt(int)
		// int[] getInts() -> void setInts(int ...)
		// Color getColorOf(eCategory) -> void setColorOf(eCategory, Color)
		// Color[] getColorsOf(eCategory) -> void setColorsOf(eCategory, Color ...)
		
		Vector<Class<?>> setterParams = new Vector<Class<?>>();
		Collections.addAll(setterParams, getter.getParameterTypes());
		
		for(Class<?> p : setterParams)
		{
			if (!p.isEnum())
			{
				throw new GameConfigCopierException("All getter parameters must be enum types: "+clazz.getName()+" / "+getter.toGenericString());
			}
		}
		
		setterParams.add(getter.getReturnType());
		
		try
		{
			return clazz.getMethod("set"+key, setterParams.toArray(new Class<?>[setterParams.size()]));			
		}
		catch(Throwable t)
		{
			if (getter.getReturnType().isArray())
			{
				setterParams.remove(setterParams.size()-1);
				int nbBaseParams = setterParams.size();
				
				for(int i=1; i<=10; ++i)
				{
					// Reset
					for(int j=nbBaseParams; j < setterParams.size(); ++j) setterParams.remove(setterParams.size()-1);
					// Set
					for(int j=0; j < i; ++j) setterParams.add(getter.getReturnType().getComponentType());
					
					try
					{
						return clazz.getMethod("set"+key, setterParams.toArray(new Class<?>[setterParams.size()]));
					}
					catch(Throwable tt)
					{
						// NOP
					}
				}
			}
			
			throw new GameConfigCopierException("Cannot found setter for "+getter.toGenericString(), t);
		}
	}
}
