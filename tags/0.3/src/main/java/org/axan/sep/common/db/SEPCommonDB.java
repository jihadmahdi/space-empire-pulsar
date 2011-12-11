package org.axan.sep.common.db;

import java.io.File;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

import org.axan.eplib.orm.ISQLDataBase;
import org.axan.eplib.orm.ISQLDataBaseStatement;
import org.axan.eplib.orm.ISQLDataBaseStatementJob;
import org.axan.eplib.orm.SQLDataBaseException;
import org.axan.eplib.orm.sqlite.SQLiteDB;
import org.axan.eplib.utils.Basic;
import org.axan.eplib.utils.Reflect;
import org.axan.sep.common.GameConfigCopier;
import org.axan.sep.common.GameConfigCopier.GameConfigCopierException;
import org.axan.sep.common.Protocol;
import org.axan.sep.server.SEPServer;

public class SEPCommonDB implements Serializable, ListIterator<SEPCommonDB>
{
	private static final long serialVersionUID = 1L;

	private static class GameConfigInvocationHandler implements InvocationHandler
	{
		private final SEPCommonDB sepDB;
		private final ISQLDataBase db;
		
		public GameConfigInvocationHandler(SEPCommonDB sepDB)
		{
			this.sepDB = sepDB;
			this.db = sepDB.getConfigDB();
		}
		
		@Override
		public Object invoke(Object proxy, final Method method, Object[] args) throws Throwable
		{
			if (method.getDeclaringClass().equals(Object.class))
			{
				return method.invoke(this, args);
			}
			else if (!method.getDeclaringClass().equals(IGameConfig.class))
			{
				throw new Protocol.SEPImplementationError("GameConfigInvocationHandler must be used with IGameConfig.class proxy.");
			}
			else
			{
				// Special case: setTurn
				if(method.getName().equals("setTurn"))
				{										
					if (args == null || args.length != 1 || !Integer.class.isInstance(args[0]))
					{
						throw new RuntimeException(method.getName()+" invalid call.");
					}
					
					int value = (Integer) args[0];
					
					if (value < 0)
					{
						throw new RuntimeException(method.getName()+" invalid call, turn cannot be negative.");
					}
					
					if (sepDB.nextVersion != null)
					{
						throw new RuntimeException(method.getName()+" invalid call, next version already exists.");
					}
					
					if (value > 0)
					{
						int currentTurn = sepDB.getConfig().getTurn();
						
						if (currentTurn == value) // No change
						{
							return null;
						}
						
						if (value != currentTurn+1)
						{
							throw new RuntimeException(method.getName()+" invalid call, must increment turn by 1 every time you call this method.");
						}
						
						synchronized(sepDB)
						{						
							sepDB.nextVersion = Basic.clone(sepDB);
							sepDB.nextVersion.previousVersion = sepDB;
							sepDB.nextVersion.getConfigDB().exec("UPDATE GameConfig SET value = '%s' WHERE key = '%s';", value, "Turn");
						}
						
						return null;
					}
					else // value == 0						
					{
						if (sepDB.previousVersion != null)
						{
							throw new RuntimeException("Cannot set game turn to 0 because SEPCommonDB already has previous version");
						}
						
						// setTurn as classic setter, do not generates next version
					}										
				}
				
				/*
				 * Setters must start with "set", have arguments and return void.
				 */
				if (method.getName().startsWith("set") && method.getReturnType().equals(void.class) && args != null && args.length > 0)
				{				
					// Setter
					String key = method.getName().substring(3);
					
					int i;
					for(i=0; i < args.length-1; ++i)
					{
						if (Enum.class.isInstance(args[i]))
						{
							key += '-'+args[i].toString();
						}
						else
						{
							break;
						}
					}
					
					String value = null;
					
					if (i+1 == args.length)
					{
						value = args[i] == null ? "NULL" : args[i].toString();
						set(key, value);
					}
					else
					{
						for(int j=0; i+j < args.length; ++j)
						{
							value = args[i+j] == null ? "NULL" : args[i+j].toString();
							set(String.format("%s-%s", key, j), value);
						}
					}
					
					return null;
				}
				/*
				 * Getters must do not return void and have only Enum<?> arguments.
				 */
				else if (!void.class.equals(method.getReturnType()))
				{
					// Getter
					String key = method.getName();
					if (method.getName().startsWith("get") || method.getName().startsWith("has"))
					{
						key = method.getName().substring(3);
					}
					else if (method.getName().startsWith("is"))
					{
						key = method.getName().substring(2);
					}
					
					if (args != null) for(int i=0; i<args.length; ++i)
					{
						if (!Enum.class.isInstance(args[i]))
						{
							throw new Protocol.SEPImplementationError("Invalid IGameConfig: Bad argument type in method (all arguments must be Enum<?>): "+method.toGenericString());
						}
						
						key += '-'+args[i].toString();
					}
					
					Object result = db.prepare("SELECT value FROM GameConfig WHERE key GLOB '%s*' ORDER BY key;", new ISQLDataBaseStatementJob<Object>()
					{
						@Override
						public Object job(ISQLDataBaseStatement stmnt) throws SQLDataBaseException
						{
							Stack<Object> results = new Stack<Object>();
							while(stmnt.step())
							{
								Object o = stmnt.columnValue(0);
								if (!method.getReturnType().isPrimitive() && o == null)
								{
									o = stmnt.columnValue(0);
									throw new Error();
								}
								results.add(o);
							}
							
							if (results.size() == 0 || results.firstElement() == null)
							{
								return null;
							}
							
							if (!method.getReturnType().isArray())
							{
								if (results.size() > 1) throw new RuntimeException("Return type is not an array, but several results found in DB ("+method.toGenericString()+").");
								try
								{
									return valueOf(Class.class.cast(method.getReturnType()), results.firstElement().toString());
								}
								catch(Throwable t)
								{
									Object o = valueOf(Class.class.cast(method.getReturnType()), results.firstElement().toString());
									throw new Error(t);
								}
							}
							else
							{
								// TODO: Support multi-dimenstional array (get/set).
								// int nrDims = 1 + method.getReturnType().getName().lastIndexOf('[');
								Object arr = Array.newInstance(method.getReturnType().getComponentType(), results.size());
								for(int i=0; i<results.size(); ++i)
								{
									try
									{
										Array.set(arr, i, valueOf(Class.class.cast(method.getReturnType().getComponentType()), results.get(i) == null ? null : results.get(i).toString()));
									}
									catch(Throwable t)
									{
										throw new Error(t);
									}
								}
								return arr;
							}
						};
					}, key);
					
					
					return result;
					//return method.getReturnType().cast(result);
				}
				else
				{
					throw new Protocol.SEPImplementationError("Invalid IGameConfig: Cannot recognize getter nor setter in method: "+method.toGenericString());
				}
			}
		}
		
		private void set(String key, String value) throws SQLDataBaseException, InterruptedException
		{
			boolean exist = db.prepare("SELECT key FROM GameConfig WHERE key = '%s'", new ISQLDataBaseStatementJob<Boolean>()
			{
				@Override
				public Boolean job(ISQLDataBaseStatement stmnt) throws SQLDataBaseException
				{
					return stmnt.step() && stmnt.columnString(0) != null;
				}
			}, key);
			
			if (!exist)
			{
				db.exec("INSERT INTO GameConfig (key, value) VALUES ('%s', '%s');", key, value);
			}
			else
			{
				db.exec("UPDATE GameConfig SET value = '%s' WHERE key = '%s';", value, key);
			}
		}
	}
	
	private static Class<?> getWrapper(Class<?> primitive)
	{
		if (!primitive.isPrimitive()) return primitive;
		
		if (primitive == byte.class) return Byte.class;
		if (primitive == short.class) return Short.class;
		if (primitive == int.class) return Integer.class;
		if (primitive == long.class) return Long.class;
		if (primitive == float.class) return Float.class;
		if (primitive == double.class) return Double.class;
		if (primitive == boolean.class) return Boolean.class;
		if (primitive == char.class) return Character.class;
		
		return primitive;
	}
	
	private static Object valueOf(Class clazz, String s)
	{
		if (s == null || s.compareToIgnoreCase("NULL") == 0) return null;
		
		try
		{
			Class<?> obClazz = clazz;
			if (clazz.isPrimitive())
			{
				obClazz = getWrapper(clazz);
			}
			
			if (obClazz.equals(Integer.class))
			{
				s = s.replaceAll("\\.[0-9]*", "");
			}
			
			Method valueOf = obClazz.getMethod("valueOf", String.class);
			
			
			Object r = valueOf.invoke(null, s);
			return obClazz.cast(r);
		}
		catch(Throwable t)
		{
			t = t;
		}
		
		try
		{
			return clazz.cast(s);
		}
		catch(Throwable t)
		{
			throw new Error(t);
		}
	}
	
	////////////////////////////////////////////////
	
	private ISQLDataBase db;
	private transient ISQLDataBase configDB;
	private transient IGameConfig config;
	
	private SEPCommonDB previousVersion = null;
	private SEPCommonDB nextVersion = null;
	
	public SEPCommonDB(ISQLDataBase db, IGameConfig config) throws IOException, SQLDataBaseException, GameConfigCopierException, InterruptedException
	{
		this.db = db;
		this.configDB = this.db;
		
		// Create Tables
		String sqlFile = SQLiteDB.class.isInstance(db) ? "SEPSQLiteDB.sql" : "SEPHSQLDB.sql";
		URL sqlURL = Reflect.getResource(SEPCommonDB.class.getPackage().getName(), sqlFile);
		if (sqlURL == null)
		{
			throw new IOException("Import resource '" + sqlURL + "' not found");
		}
		
		db.importSQLFile(new File(sqlURL.getFile()));

		// Write GameConfig
		this.config = (IGameConfig) Proxy.newProxyInstance(IGameConfig.class.getClassLoader(), new Class<?>[] {IGameConfig.class}, new GameConfigInvocationHandler(this));
		
		GameConfigCopier.copy(IGameConfig.class, config, this.config);
	}
	
	public IGameConfig getConfig()
	{
		return this.config;
	}
	
	public ISQLDataBase getConfigDB()
	{
		return configDB;
	}
	
	public ISQLDataBase getDB()
	{
		return db;
	}
	
	//////////////// ListIterator implementation
	
	@Override
	public boolean hasNext()
	{
		return nextVersion != null;
	}
	
	@Override
	public boolean hasPrevious()
	{
		return previousVersion != null;
	}						
	
	@Override
	public SEPCommonDB next()
	{
		return nextVersion;
	}
	
	@Override
	public SEPCommonDB previous()
	{
		return previousVersion;
	}
	
	// Assume that DB are saved for each turn, and no saves are made in between.
	@Override
	public int nextIndex()
	{
		return getConfig().getTurn() + 1;
	}
	
	@Override
	public int previousIndex()
	{
		return getConfig().getTurn() - 1;
	}
	
	@Override
	public void add(SEPCommonDB e)
	{
		throw new UnsupportedOperationException(SEPCommonDB.class.getName()+" iterator cannot be used to add elements.");
	}
	
	@Override
	public void remove()
	{
		throw new UnsupportedOperationException(SEPCommonDB.class.getName()+" iterator cannot be used to remove elements.");
	}
	
	@Override
	public void set(SEPCommonDB e)
	{
		throw new UnsupportedOperationException(SEPCommonDB.class.getName()+" iterator cannot be used to set elements.");
	}
	
	//////////////// Serialization
	
	private void writeObject(final java.io.ObjectOutputStream out) throws IOException
	{
		out.defaultWriteObject();
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
		this.configDB = db;
		this.config = (IGameConfig) Proxy.newProxyInstance(IGameConfig.class.getClassLoader(), new Class<?>[] {IGameConfig.class}, new GameConfigInvocationHandler(this));
	}
	
	private void readObjectNoData() throws ObjectStreamException
	{

	}
}
