package org.axan.sep.common.db.sqlite;

import java.io.File;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.Set;
import java.util.Stack;

import org.axan.eplib.orm.sqlite.SQLiteDB;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteStatementJob;
import org.axan.eplib.utils.Reflect;
import org.axan.sep.common.GameConfigCopier;
import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.Player;
import org.axan.sep.common.GameConfigCopier.GameConfigCopierException;
import org.axan.sep.server.SEPServer;
import org.axan.sep.server.model.ISEPServerDataBase.SEPServerDataBaseException;

import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;

public class SEPCommonSQLiteDB implements Serializable
{
	private static final long serialVersionUID = 1L;

	private static class SQLiteDBGameConfigInvocationHandler implements InvocationHandler
	{
		private final SEPCommonSQLiteDB sepDB;
		private final SQLiteDB db;
		
		public SQLiteDBGameConfigInvocationHandler(SEPCommonSQLiteDB sepDB)
		{
			this.sepDB = sepDB;
			this.db = sepDB.db;
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
				throw new SEPServer.SEPImplementationException("SQLiteDBGameConfigInvocationHandler must be used with IGameConfig.class proxy.");
			}
			else
			{
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
					
					if (i+1 == args.length)
					{						
						db.exec("INSERT OR REPLACE INTO GameConfig (key, value) VALUES ('%s', '%s');", key, args[i] == null ? "NULL" : args[i].toString());
					}
					else
					{
						for(int j=0; i+j < args.length; ++j)
						{
							db.exec("INSERT OR REPLACE INTO GameConfig (key, value) VALUES ('%s-%d', '%s');", key, j, args[i+j] == null ? "NULL" : args[i+j].toString());
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
							throw new SEPServer.SEPImplementationException("Invalid IGameConfig: Bad argument type in method (all arguments must be Enum<?>): "+method.toGenericString());
						}
						
						key += '-'+args[i].toString();
					}
					
					Object result = db.prepare("SELECT value FROM GameConfig WHERE key GLOB '%s*' ORDER BY key;", new SQLiteStatementJob<Object>()
					{
						public Object job(SQLiteStatement stmnt) throws SQLiteException
						{
							Stack<Object> results = new Stack<Object>();
							while(stmnt.step())
							{
								results.add(stmnt.columnValue(0));
							}
							
							if (results.size() == 0) return null;
							
							if (!method.getReturnType().isArray())
							{
								if (results.size() > 1) throw new RuntimeException("Return type is not an array, but several results found in DB ("+method.toGenericString()+").");
								return valueOf(Class.class.cast(method.getReturnType()), results.firstElement().toString());
							}
							else
							{
								// TODO: Support multi-dimenstional array (get/set).
								// int nrDims = 1 + method.getReturnType().getName().lastIndexOf('[');
								Object arr = Array.newInstance(method.getReturnType().getComponentType(), results.size());
								for(int i=0; i<results.size(); ++i)
								{
									Array.set(arr, i, valueOf(Class.class.cast(method.getReturnType().getComponentType()), results.get(i).toString()));
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
					throw new SEPServer.SEPImplementationException("Invalid IGameConfig: Cannot recognize getter nor setter in method: "+method.toGenericString());
				}
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
			
			Method valueOf = obClazz.getMethod("valueOf", String.class);
			
			return obClazz.cast(valueOf.invoke(null, s));
		}
		catch(Throwable t)
		{
			// 
		}
		
		return clazz.cast(s);
	}
	
	////////////////////////////////////////////////
	
	private SQLiteDB db;
	private transient IGameConfig config;
	
	public SEPCommonSQLiteDB(Set<Player> players, IGameConfig config) throws IOException, SQLiteDBException, GameConfigCopierException
	{
		File dbFile = File.createTempFile("SEP-commonDB", ".sep");
		this.db = new SQLiteDB(dbFile);
		
		// Create Tables
		URL sqlURL = Reflect.getResource(SEPCommonSQLiteDB.class.getPackage().getName(), "SEPSQLiteDB.sql");
		if (sqlURL == null)
		{
			throw new IOException("Import resource '" + sqlURL + "' not found");
		}
		
		db.importSQLFile(new File(sqlURL.getFile()));

		// Write GameConfig
		this.config = (IGameConfig) Proxy.newProxyInstance(IGameConfig.class.getClassLoader(), new Class<?>[] {IGameConfig.class}, new SQLiteDBGameConfigInvocationHandler(this));
		
		GameConfigCopier.copy(IGameConfig.class, config, this.config);
	}
	
	public IGameConfig getConfig()
	{
		return this.config;
	}
	
	public SQLiteDB getDB()
	{
		return db;
	}
	
	//////////////// Serialization
	
	private void writeObject(final java.io.ObjectOutputStream out) throws IOException
	{
		out.defaultWriteObject();
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
		this.config = (IGameConfig) Proxy.newProxyInstance(IGameConfig.class.getClassLoader(), new Class<?>[] {IGameConfig.class}, new SQLiteDBGameConfigInvocationHandler(this));
	}
	
	private void readObjectNoData() throws ObjectStreamException
	{

	}
}
