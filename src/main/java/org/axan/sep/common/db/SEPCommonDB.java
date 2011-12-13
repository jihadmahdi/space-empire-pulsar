package org.axan.sep.common.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import javax.management.RuntimeErrorException;

import org.apache.commons.lang.ArrayUtils;
import org.axan.eplib.orm.DataBaseORMGenerator;
import org.axan.eplib.utils.Basic;
import org.axan.eplib.utils.Compression;
import org.axan.sep.common.GameConfigCopier;
import org.axan.sep.common.GameConfigCopier.GameConfigCopierException;
import org.axan.sep.common.Protocol;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.orm.Area;
import org.axan.sep.common.db.orm.CelestialBody;
import org.axan.sep.common.db.orm.Player;
import org.axan.sep.common.db.orm.PlayerConfig;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ReturnableEvaluator;
import org.neo4j.graphdb.StopEvaluator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.TraversalPosition;
import org.neo4j.graphdb.Traverser.Order;
import org.neo4j.graphdb.index.Index;
import org.neo4j.kernel.EmbeddedGraphDatabase;

import scala.util.control.Exception.Finally;

public class SEPCommonDB implements Serializable, ListIterator<SEPCommonDB>
{
	private static final long serialVersionUID = 1L;

	private static class GameConfigInvocationHandler implements InvocationHandler
	{
		private final SEPCommonDB sepDB;
		private GraphDatabaseService db;
		private Node gameConfigNode;
		
		public GameConfigInvocationHandler(SEPCommonDB sepDB)
		{
			this.sepDB = sepDB;
			checkForUpdate();						
		}
		
		private void checkForUpdate()
		{
			if (db == null || !db.equals(sepDB.getConfigDB()))
			{
				db = sepDB.getConfigDB();
				
				Relationship r = db.getReferenceNode().getSingleRelationship(eRelationsTypes.GameConfig, Direction.OUTGOING);
				if (r == null)
				{
					Transaction tx = db.beginTx();
					try
					{
						gameConfigNode = db.createNode();
						db.getReferenceNode().createRelationshipTo(gameConfigNode, eRelationsTypes.GameConfig);
						tx.success();
					}
					finally
					{
						tx.finish();
					}
				}
				else
				{
					gameConfigNode = r.getEndNode();
				}
			}
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
				checkForUpdate();
				
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

							// Increment next version turn.
							Transaction tx = sepDB.nextVersion.getConfigDB().beginTx();
							try
							{
								sepDB.nextVersion.getConfigDB().getReferenceNode().getSingleRelationship(eRelationsTypes.GameConfig, Direction.OUTGOING).getEndNode().setProperty("Turn", value);
								tx.success();
							}
							finally
							{
								tx.finish();
							}
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
					
					Object value = null;
					
					if (i+1 == args.length)
					{
						//value = args[i] == null ? "NULL" : args[i].toString();
						value = args[i];
						set(key, value);
					}
					else
					{
						/*
						for(int j=0; i+j < args.length; ++j)
						{
							value = args[i+j] == null ? "NULL" : args[i+j].toString();
							set(String.format("%s-%s", key, j), value);
						}
						*/
						
						Object arr = Array.newInstance(getPrimitive(args[i].getClass()), args.length - i);						
						for(int j=0; i+j < args.length; ++j)
						{
							Array.set(arr, j, args[i+j]);
						}
						set(key, arr);						
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
					
					Object result = (gameConfigNode.hasProperty(key)) ? gameConfigNode.getProperty(key) : null;
					/*
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
					*/
					
					return result;
					//return method.getReturnType().cast(result);
				}
				else
				{
					throw new Protocol.SEPImplementationError("Invalid IGameConfig: Cannot recognize getter nor setter in method: "+method.toGenericString());
				}
			}
		}
		
		private void set(String key, Object value) throws InterruptedException
		{
			Transaction tx = db.beginTx();
			try
			{
				if (value == null)
				{
					gameConfigNode.removeProperty(key);
				}
				else
				{
					gameConfigNode.setProperty(key, value);
				}
				tx.success();
			}
			finally
			{
				tx.finish();
			}
			/*
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
			*/
		}
	}
	
	private static Class<?> getPrimitive(Class<?> wrapper)
	{
		if (wrapper.isPrimitive()) return wrapper;
		
		if (wrapper == Byte.class) return byte.class;
		if (wrapper == Short.class) return short.class;
		if (wrapper == Integer.class) return int.class;
		if (wrapper == Long.class) return long.class;
		if (wrapper == Float.class) return float.class;
		if (wrapper == Double.class) return double.class;
		if (wrapper == Boolean.class) return boolean.class;
		if (wrapper == Character.class) return char.class;
		if (wrapper == String.class) return String.class;
		
		return wrapper;
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
	
	private transient GraphDatabaseService db;
	private transient IGameConfig config;
	
	private transient Node playerFactory;
	private transient Node areaFactory;
	private transient Node celestialBodyFactory;
	private transient Node unitFactory;
	
	private transient Index<Node> playerIndex;
	private transient Index<Node> areaIndex;
	private transient Index<Node> celestialBodyIndex;
	private transient Index<Node> unitIndex;
	
	private SEPCommonDB previousVersion = null;
	private SEPCommonDB nextVersion = null;
	
	public SEPCommonDB(GraphDatabaseService db, IGameConfig config) throws IOException, GameConfigCopierException, InterruptedException
	{
		init(db);				
		GameConfigCopier.copy(IGameConfig.class, config, this.config);		
	}
	
	public IGameConfig getConfig()
	{
		return this.config;
	}
	
	public synchronized GraphDatabaseService getConfigDB()
	{
		return db;
	}
	
	public synchronized GraphDatabaseService getDB()
	{
		return db;
	}
	
	//////////////// Querying interface
	
	/**
	 * Return all players.
	 * @return
	 */
	public Set<IPlayer> getPlayers()
	{
		Set<IPlayer> players = new HashSet<IPlayer>();
		for(Node n: playerFactory.traverse(Order.BREADTH_FIRST, StopEvaluator.DEPTH_ONE, ReturnableEvaluator.ALL_BUT_START_NODE, eRelationsTypes.Player, Direction.OUTGOING))
		{
			players.add(new Player(n));
		}
		return players;
	}
	
	/**
	 * Return player by name.
	 * @param playerName
	 * @return
	 */
	public IPlayer getPlayer(String playerName)
	{
		return new Player(getPlayerNode(playerName));
	}		
	
	/**
	 * Return player config by player name.
	 * @param playerName
	 * @return
	 */
	public IPlayerConfig getPlayerConfig(String playerName)
	{
		Node playerNode = getPlayerNode(playerName);
		return new PlayerConfig(playerNode.getSingleRelationship(eRelationsTypes.PlayerConfig, Direction.OUTGOING).getEndNode());
	}
	
	/**
	 * Return true if universe already created (actually if at least one Area has been created).
	 * @return
	 */
	public boolean isUniverseCreated()
	{
		return areaFactory.hasRelationship(eRelationsTypes.Area, Direction.OUTGOING);
	}
	
	/**
	 * Insert player/config in DB from given player and config.
	 * Throw runtime exception if player already exists.
	 * @param player
	 * @param playerConfig
	 */
	public void createPlayer(IPlayer player, IPlayerConfig playerConfig)
	{	
		Transaction tx = db.beginTx();
		try
		{
			if (playerIndex.get("name", player.getName()).hasNext())
			{
				tx.failure();
				throw new RuntimeException("Player '"+player.getName()+"' already exists.");
			}
					
			Node nPlayer = db.createNode();
			initializeNode(nPlayer, player.getNode());
			playerIndex.add(nPlayer, "name", player.getName());		
			playerFactory.createRelationshipTo(nPlayer, eRelationsTypes.Player);
			
			Node nPlayerConfig = db.createNode();
			initializeNode(nPlayerConfig, playerConfig.getNode());
			nPlayer.createRelationshipTo(nPlayerConfig, eRelationsTypes.PlayerConfig);
			
			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}
	
	/**
	 * Insert area in db.
	 * Does nothing if area already exists.
	 * @param area
	 */
	public void createArea(IArea area)
	{
		Transaction tx = db.beginTx();
		try
		{
			Node nArea = getAreaNode(area.getLocation());
			if (nArea != null) return;
			nArea = db.createNode();
			initializeNode(nArea, area.getNode());
			areaIndex.add(nArea, "location", area.getLocation().toString());
			areaFactory.createRelationshipTo(nArea, eRelationsTypes.Area);
			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}
	
	public void createCelestialBody(ICelestialBody celestialBody)
	{
		Transaction tx = db.beginTx();
		try
		{
			if (celestialBodyIndex.get("name", celestialBody.getName()).hasNext())
			{
				tx.failure();
				throw new RuntimeException("Celestial body '"+celestialBody.getName()+"' already exist.");
			}
			if (celestialBodyIndex.get("location", celestialBody.getLocation().toString()).hasNext())
			{
				tx.failure();
				throw new RuntimeException("Celestial body already exist on location "+celestialBody.getLocation().toString());
			}
			
			createArea(new Area(celestialBody.getLocation(), false));
			
			Node nCelestialBody = db.createNode();
			initializeNode(nCelestialBody, celestialBody.getNode());
			celestialBodyIndex.add(nCelestialBody, "name", celestialBody.getName());
			celestialBodyIndex.add(nCelestialBody, "location", celestialBody.getLocation().toString());
			celestialBodyFactory.createRelationshipTo(nCelestialBody, eRelationsTypes.CelestialBody);
			celestialBodyFactory.createRelationshipTo(nCelestialBody, celestialBody.getType());
			
			if (IProductiveCelestialBody.class.isInstance(celestialBody))
			{
				IProductiveCelestialBody pcb = IProductiveCelestialBody.class.cast(celestialBody);
				if (pcb.getOwner() != null)
				{
					Node nPlayer = getPlayerNode(pcb.getOwner());
					if (nPlayer == null)
					{
						tx.failure();
						throw new RuntimeException("Cannot find celestial body owner '"+pcb.getOwner()+"'.");
					}
					
					nPlayer.createRelationshipTo(nCelestialBody, celestialBody.getType());
				}
			}
			
			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}
	
	public void createBuilding(IBuilding building)
	{
		Transaction tx = db.beginTx();
		try
		{
			Node nCelestialBody = celestialBodyIndex.get("name", building.getCelestialBodyName()).getSingle();
			if (nCelestialBody == null)
			{
				tx.failure();
				throw new RuntimeException("Unknown celestial body '"+building.getCelestialBodyName()+"'.");
			}
			
			if (nCelestialBody.traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, ReturnableEvaluator.ALL_BUT_START_NODE, building.getType(), Direction.OUTGOING).iterator().hasNext())
			{
				tx.failure();
				throw new RuntimeException("Building '"+building.getType()+"' already exists on '"+building.getCelestialBodyName()+"'.");
			}
			
			Node nBuidling = db.createNode();
			initializeNode(nBuidling, building.getNode());
			nCelestialBody.createRelationshipTo(nBuidling, building.getType());
			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}
	
	public void updateGovernment(final IGovernment government)
	{
		Transaction tx = db.beginTx();
		try
		{
			Node nPlayer = playerIndex.get("name", government.getOwner()).getSingle();
			if (nPlayer == null)
			{
				tx.failure();
				throw new RuntimeException("Player '"+government.getOwner()+"' unknwon.");
			}
			
			Node targetNode = null;
			if (government.getFleetName() != null)
			{
				Node nFleet = nPlayer.traverse(Order.DEPTH_FIRST, new StopEvaluator()
				{
					
					@Override
					public boolean isStopNode(TraversalPosition currentPos)
					{
						return (currentPos.depth() > 1 || currentPos.returnedNodesCount() > 0);
					}
				}, new ReturnableEvaluator()
				{
					
					@Override
					public boolean isReturnableNode(TraversalPosition currentPos)
					{
						Node n = currentPos.currentNode();
						return (n.getProperty("name").equals(government.getFleetName()));
					}
				}, eUnitType.Fleet, Direction.OUTGOING).iterator().next();
				
				if (nFleet == null)
				{
					tx.failure();
					throw new RuntimeException("Fleet '"+government.getFleetName()+"' unknown for player '"+government.getOwner()+"'.");
				}
				
				targetNode = nFleet;
			}
			
			if (targetNode == null && government.getPlanetName() != null)
			{
				Node nPlanet = celestialBodyIndex.get("name", government.getPlanetName()).getSingle();
				if (nPlanet == null || !government.getOwner().equals(nPlanet.getProperty("owner")) || !eCelestialBodyType.Planet.equals(eCelestialBodyType.valueOf((String) nPlanet.getProperty("type"))))
				{
					tx.failure();
					throw new RuntimeException("Celestial body '"+government.getPlanetName()+"' is not a planet, or is not owned by player '"+government.getOwner()+"'.");
				}
				
				targetNode = nPlanet;
			}
			
			if (targetNode == null)
			{
				tx.failure();
				throw new RuntimeException("Could not found target government.");
			}
			
			Relationship currentGovernment = nPlayer.getSingleRelationship(eRelationsTypes.Government, Direction.OUTGOING);
			if (currentGovernment != null)
			{
				currentGovernment.delete();
			}
			
			nPlayer.createRelationshipTo(targetNode, eRelationsTypes.Government);
			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}
	
	public ICelestialBody getCelestialBody(Location location)
	{
		Node nCelestialBody = celestialBodyIndex.get("location", location.toString()).getSingle();
		if (nCelestialBody == null) return null;
		
		try
		{
			Class<? extends ICelestialBody> clazz = (Class<? extends ICelestialBody>) Class.forName(String.format("%s.%s", CelestialBody.class.getPackage().getName(), nCelestialBody.getProperty("type")));
			return DataBaseORMGenerator.mapTo(clazz, nCelestialBody);
		}
		catch(Throwable t)
		{
			throw new RuntimeException(t);
		}
	}
	
	
	////////// Private Querying methods
	
	/**
	 * Must be called within a transaction.
	 * @param n
	 * @param nodeProperties
	 */
	private void initializeNode(Node n, Map<String, Object> nodeProperties)
	{
		for(String key : nodeProperties.keySet())
		{
			Object value = nodeProperties.get(key);
			if (value == null)
			{
				n.removeProperty(key);
			}
			else
			{
				n.setProperty(key, value);
			}
		}
	}
	
	public Set<IArea> getAreasByZ(int z)
	{
		Set<IArea> areas = new HashSet<IArea>();
		// Lucene reserved characters: + - && || ! ( ) { } [ ] ^ " ~ * ? : \
		for(Node n: areaIndex.query("location", String.format("\\[*;%d\\]", z)))
		{
			areas.add(new Area(n));
		}
		return areas;
	}
	
	public Node getAreaNode(Location location)
	{
		 return areaIndex.get("location", location.toString()).getSingle();
	}
	
	public Node getPlayerNode(String playerName)
	{
		return playerIndex.get("name", playerName).getSingle();
		/*
		return playerFactory.traverse(Order.BREADTH_FIRST, StopEvaluator.DEPTH_ONE, new ReturnableEvaluator()
		{			
			@Override
			public boolean isReturnableNode(TraversalPosition currentPos)
			{
				Node n = currentPos.currentNode();
				return (n.hasRelationship(eRelationsTypes.Player, Direction.INCOMING) && n.hasProperty("name") && playerName.equals(n.getProperty("name")));
			}
		}, eRelationsTypes.Player, Direction.OUTGOING).iterator().next();
		*/
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
	
	private synchronized void init(final GraphDatabaseService db)
	{
		this.db = db;
		
		Runtime.getRuntime().addShutdownHook(new Thread()
		{
			@Override
			public void run()
			{
				db.shutdown();
			}
		});
		
		// Already refresh itself if SEPCommonDB#db change.
		if (config == null)
		{
			config = (IGameConfig) Proxy.newProxyInstance(IGameConfig.class.getClassLoader(), new Class<?>[] {IGameConfig.class}, new GameConfigInvocationHandler(this));
		}
		
		Transaction tx = db.beginTx();
		try
		{
			playerFactory = getFactory(db, eRelationsTypes.Player);
			areaFactory = getFactory(db, eRelationsTypes.Area);
			celestialBodyFactory = getFactory(db, eRelationsTypes.CelestialBody);
			unitFactory = getFactory(db, eRelationsTypes.Unit);
			
			playerIndex = db.index().forNodes("players");
			areaIndex = db.index().forNodes("areas");
			celestialBodyIndex = db.index().forNodes("celestialBodies");
			unitIndex = db.index().forNodes("units");
			
			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}
	
	private synchronized void writeObject(final java.io.ObjectOutputStream out) throws IOException
	{
		out.defaultWriteObject();
		EmbeddedGraphDatabase edb = (EmbeddedGraphDatabase) db;
		//File backupDirectory = File.createTempFile("dbCopy", "");		
		File zipFile = File.createTempFile("dbCopy", ".zip");
		
		//TODO: Use org.neo4j.backup.OnlineBackup abilities (enterprise version required)
		
		File dbDirectory = new File(edb.getStoreDir());
		edb.shutdown();
		Compression.zipDirectory(dbDirectory, zipFile);
		
		// Complete reload after shutdown
		init(new EmbeddedGraphDatabase(dbDirectory.getAbsolutePath()));		
		
		FileInputStream fis = new FileInputStream(zipFile);				
		out.writeLong(fis.getChannel().size());		
		byte[] bb = new byte[512];
		int red = 0;
		do
		{
			red = fis.read(bb);
			if (red > 0)
			{
				out.write(bb, 0, red);
			}
		}while(red > 0);
		
		fis.close();
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
		
		File dbDirectory = File.createTempFile("dbRestore", "");
		dbDirectory.delete();
		dbDirectory.mkdir();
		File zipFile = File.createTempFile("dbRestore", ".zip");
		FileOutputStream fos = new FileOutputStream(zipFile);
		
		long size = in.readLong();
		byte[] bb = new byte[512];
		long red = 0;
		do
		{
			int toRead = Math.min(512, (int) (size - red));
			toRead = in.read(bb, 0, toRead);
			fos.write(bb, 0, toRead);
			red += toRead;			
		}while(red < size);
		
		fos.close();
		
		Compression.unzip(zipFile, dbDirectory);
		
		init(new EmbeddedGraphDatabase(dbDirectory.getAbsolutePath()));
	}
	
	/**
	 * Must be called withing a transaction
	 * @param db
	 * @param relationType
	 * @return
	 */
	private static Node getFactory(GraphDatabaseService db, eRelationsTypes relationType)
	{
		Node result;
		Relationship r = db.getReferenceNode().getSingleRelationship(relationType, Direction.OUTGOING);
		if (r == null)
		{
			result = db.createNode();
			db.getReferenceNode().createRelationshipTo(result, relationType);
		}
		else
		{
			result = r.getEndNode();
		}
		return result;
	}
	
	private void readObjectNoData() throws ObjectStreamException
	{

	}
}
