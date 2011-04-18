
package org.axan.sep.server.model;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.NotSerializableException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import org.axan.sep.client.SEPClient;
import org.axan.sep.client.gui.SpaceEmpirePulsarGUI;
import org.axan.sep.common.GameConfig;
import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.Player;
import org.axan.sep.common.PlayerConfig;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.server.model.GameBoard;
import org.axan.sep.server.model.SEPSQLiteDB;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.almworks.sqlite4java.SQLite;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteJob;
import com.almworks.sqlite4java.SQLiteQueue;
import com.almworks.sqlite4java.SQLiteStatement;

import org.axan.sep.server.model.SEPSQLiteDB.SQLiteStatementJob;

public class TestSQLite
{
	
	private static Random rnd = new Random();
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
		
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception
	{
	}
	
	private static void assertFileResult(SQLiteQueue q, String sqlResourceFile, String message, String expectedResultEnd)
	{
		String result = SQLiteDebugFile(q, sqlResourceFile);
		
		if (expectedResultEnd.isEmpty())
		{
			if (!result.isEmpty()) System.err.println(result);
			assertTrue(message+"\n"+result, result.isEmpty());
		}
		else
		{
			assertTrue(message+"\n"+result, result.endsWith(expectedResultEnd));
		}
	}
	
	private static void assertQueryResult(SQLiteQueue q, String message, String query, String expectedResultEnd)
	{
		String result = SQLiteDebug(q, query);
		
		if (expectedResultEnd.isEmpty())
		{
			if (!result.isEmpty())
			{
				System.err.println(result);
			}
			assertTrue(message+"\n"+result, result.isEmpty());
		}
		else
		{
			if (!result.endsWith(expectedResultEnd))
			{
				System.err.println(result);
			}
			assertTrue(message+"\n"+result, result.endsWith(expectedResultEnd));
		}
	}
	
	private static void SQLiteDebugQuery(SQLiteQueue q, String query)
	{
		System.out.println(query);
		System.out.println(SQLiteDebug(q, query));
	}
	
	private static String SQLiteDebug(SQLiteQueue q, final String query)
	{
		return q.execute(new SQLiteJob<String>() {
			@Override
			protected String job(SQLiteConnection conn) throws Throwable {
				return conn.debug(query);
			}
		}).complete();
	}
	
	private static String SQLiteDebugFile(SQLiteQueue q, String sqlResourceFile)
	{
		String resourcesBasePath = TestSQLite.class.getPackage().getName().replace('.', File.separatorChar) + File.separatorChar;
		final URL sqlURL = ClassLoader.getSystemResource(resourcesBasePath + sqlResourceFile);
		assertTrue(sqlURL != null);
		
		return q.execute(new SQLiteJob<String>() {
			
			private StringBuffer result = new StringBuffer();
			public URL url = sqlURL;
			
			@Override
			protected void jobError(Throwable error) throws Throwable {
				result.append("Exception: "+error.getMessage());				
				super.jobError(error);
				cancel();
			}
			
			@Override
			protected String job(SQLiteConnection conn) throws Throwable {
								
				conn.exec("BEGIN TRANSACTION;");				
				boolean cancelled = false;
				
				try
				{
					String source = url.getFile();
					InputStreamReader isr = new InputStreamReader(url.openStream());					
					StringBuffer sb = new StringBuffer("");
					char lastChar = '\0';
					StringBuffer lastWord = new StringBuffer(10);
					int inBegin = 0; boolean inComment = false; boolean isBlank = true;
					
					while(isr.ready())
					{
						lastChar = (char) isr.read();
						
						sb.append(lastChar);
						lastWord.append(lastChar);
						
						if (lastWord.toString().endsWith("--"))
						{
							inComment = true;
						}
						
						if (inComment)
						{
							if (lastChar == '\n')
							{
								inComment = false;
								isBlank = true;
							}
							
							continue;
						}
						
						if (isBlank && !Character.isWhitespace(lastChar))
						{
							isBlank = false;
						}
						
						if (lastWord.toString().endsWith("BEGIN"))
						{
							++inBegin;
							lastWord.setLength(0);
						}
						
						if (lastWord.toString().endsWith("END"))
						{
							--inBegin;
							lastWord.setLength(0);
						}
						
						if (inBegin <= 0 && lastChar == ';')
						{
							String res = conn.debug(sb.toString());
							
							if (!res.isEmpty())
							{
								result.append(res);
								result.append('\n');
							}
							
							sb.setLength(0);
							lastWord.setLength(0);
							isBlank = true;
						}
					}
					
					assertTrue("Remaining not executed lines in file '"+url+"'", isBlank || inComment || sb.length() == 0);
				}
				catch(Throwable t)
				{
					conn.exec("ROLLBACK;");
					cancelled = true;
				}
				
				if (!cancelled)
				{
					conn.exec("COMMIT;");
				}
				
				return result.toString();
			}
		}).complete();
	}
	
	/**
	 * SQLite lib tests.
	 */
	@Test
	public void testSQLite()
	{		
		System.out.println("SQLite Test:");
		
		SQLiteQueue q = null;
		
		try
		{
			System.out.println("LibraryPathProperty: "+SQLite.LIBRARY_PATH_PROPERTY);
			System.out.println("Value: "+System.getProperty(SQLite.LIBRARY_PATH_PROPERTY));
			String libPath = "target/izpack/lib/";
			System.out.println("Setting to "+libPath);
			SQLite.setLibraryPath(libPath);
			System.out.println("Value: "+System.getProperty(SQLite.LIBRARY_PATH_PROPERTY));
			
			SQLite.loadLibrary();
			
			System.out.println("LibraryVersion: "+SQLite.getLibraryVersion());
			System.out.println("CompileOptions: "+SQLite.getSQLiteCompileOptions());
			System.out.println("SQLiteVersion: "+SQLite.getSQLiteVersion());
			System.out.println("SQLiteVersionNumber: "+SQLite.getSQLiteVersionNumber());
			System.out.println("isDebugBinaryPreferred: "+SQLite.isDebugBinaryPreferred());
			System.out.println("isThreadSafe: "+SQLite.isThreadSafe());
			SQLite.main(new String[0]);
			
			q = new SQLiteQueue(new File("sqlite_test.db"));
			q.start();
			
			SQLiteDebugQuery(q, "PRAGMA foreign_keys;");
			SQLiteDebugQuery(q, "PRAGMA foreign_keys=On;");
			
			assertQueryResult(q, "Foreign keys error:", "PRAGMA foreign_keys;", "|foreign_keys|\n|------------|\n|1           |");
			
			SQLiteDebugQuery(q, "PRAGMA user_version;");
			SQLiteDebugQuery(q, "PRAGMA user_version=1;");
			SQLiteDebugQuery(q, "PRAGMA user_version;");
			
			SQLiteDebugQuery(q, "PRAGMA encoding;");
			SQLiteDebugQuery(q, "PRAGMA integrity_check;");
			SQLiteDebugQuery(q, "PRAGMA quick_check;");
			
			assertQueryResult(q, "SQLite error?", "DROP TABLE IF EXISTS parents ;", "");
			assertQueryResult(q, "SQLite error?", "DROP TABLE IF EXISTS real_people ;", "");
			assertQueryResult(q, "SQLite error?", "DROP TABLE IF EXISTS people ;", "");			
			
			assertQueryResult(q, "SQLite error?", "CREATE TABLE people ( name TEXT, surname TEXT, age INTEGER, PRIMARY KEY(name, surname), UNIQUE (name, surname, age) );", "");
			assertQueryResult(q, "SQLite error?", "CREATE TABLE parents ( people_name TEXT, people_surname TEXT, parent_name TEXT, parent_surname TEXT, FOREIGN KEY(people_name, people_surname) REFERENCES people(name, surname), FOREIGN KEY(parent_name, parent_surname) REFERENCES people(name, surname) );", "");
			
			assertQueryResult(q, "SQLite error?", "INSERT INTO people VALUES ( 'A', 'a', 30) ;", "");			
			assertQueryResult(q, "SQLite error?", "INSERT INTO people VALUES ( 'B', 'b', 12) ;", "");			
			assertQueryResult(q, "SQLite error?", "INSERT INTO people VALUES ( 'C', 'c', 32) ;", "");
			assertQueryResult(q, "SQLite error?", "INSERT INTO people VALUES ( 'D', 'd', 61) ;", "");
			assertQueryResult(q, "SQLite error?", "INSERT INTO parents VALUES ( 'B', 'b', 'A', 'a') ;", "");			
			assertQueryResult(q, "SQLite error?", "INSERT INTO parents VALUES ( 'B', 'b', 'C', 'c') ;", "");
			
			assertQueryResult(q, "SQLite error?", "SELECT EXISTS (SELECT * FROM parents WHERE people_name GLOB 'B') AS NotEmpty;", "|NotEmpty|\n|--------|\n|1       |");
			
			// Cannot insert with non-existing foreign key value.
			assertQueryResult(q, "SQLite error expected", "INSERT INTO parents VALUES ( 'C', 'c', 'Z', 'z' ) ;", "[constraint failed]");
			
			// Test that (parent_name, parent_surname) is checked to be the same person. 
			assertQueryResult(q, "SQLite error expected", "INSERT INTO parents VALUES ( 'C', 'c', 'A', 'b' ) ;", "[constraint failed]");
			assertQueryResult(q, "SQLite error expected", "INSERT INTO parents VALUES ( 'C', 'a', 'A', 'c' ) ;", "[constraint failed]");
			
			assertQueryResult(q, "SQLite error?", "INSERT INTO parents VALUES ( 'C', 'c', 'A', 'a' ) ;", "");
			assertQueryResult(q, "SQLite error?", "INSERT INTO parents VALUES ( 'C', 'c', 'D', 'd' ) ;", "");
			
			// Not primary key fields used in foreign keys test			
			assertQueryResult(q, "SQLite error?", "CREATE TABLE real_people ( name TEXT, surname TEXT, age INTEGER, sex TEXT NOT NULL, CHECK(sex IN ('m', 'f')), PRIMARY KEY(name, surname), FOREIGN KEY (name, surname, age) REFERENCES people (name, surname, age) );", "");
						
			assertQueryResult(q, "SQLite error?", "SELECT * FROM people WHERE name = 'A' AND surname = 'a' AND age = 30;", "|name|surname|age|\n|----|-------|---|\n|A   |a      |30 |");
			assertQueryResult(q, "SQLite error?", "INSERT INTO real_people VALUES ( 'A', 'a', 30, 'm') ;", "");
			assertQueryResult(q, "SQLite error?", "INSERT INTO real_people VALUES ( 'A', 'a', 30, 'f') ;", "[constraint failed]");
			assertQueryResult(q, "SQLite error?", "INSERT INTO real_people VALUES ( 'B', 'b', 10, 'f') ;", "[constraint failed]");
			assertQueryResult(q, "SQLite error?", "INSERT INTO real_people VALUES ( 'B', 'b', 12, 'f') ;", "");
			
			
			// Heritance relations tests
			assertFileResult(q, "TestSQLite.creation.sql", "TestSQLite.creation.sql error", "");
			
			// New fleet creation on turn 1
			assertQueryResult(q, "SQLite error?", "INSERT INTO Entity VALUES(1, 1);", "");
			assertQueryResult(q, "SQLite error?", "INSERT INTO Fleet VALUES(1, 'pl', 'fl', 1);", "");
			// No GovSt expected
			assertFileResult(q, "TestSQLite.GovSt.sql", "SQLite error?", "|isGovSt|\n|-------|\n|0      |\n");
			
			// GovernmentStarship join on turn 2
			assertQueryResult(q, "SQLite error?", "INSERT INTO Entity VALUES(2, 2);", "");
			assertQueryResult(q, "SQLite error?", "INSERT INTO GovSt VALUES(2, 'pl', 'fl', 2);", "");
			
			// GovSt expected
			assertFileResult(q, "TestSQLite.GovSt.sql", "SQLite error?", "|isGovSt|\n|-------|\n|1      |\n");
			
			// Fleet moves on turn 3
			assertQueryResult(q, "SQLite error?", "INSERT INTO Entity VALUES(3, 3);", "");
			assertQueryResult(q, "SQLite error?", "INSERT INTO Fleet VALUES(3, 'pl', 'fl', 3);", "");
			// GovSt expected
			assertFileResult(q, "TestSQLite.GovSt.sql", "SQLite error?", "|isGovSt|\n|-------|\n|1      |\n");
			
			// Government starship split
			assertQueryResult(q, "SQLite error?", "INSERT INTO Entity VALUES(4, 4);", "");
			assertQueryResult(q, "SQLite error?", "INSERT INTO GovSt VALUES(4, 'pl', NULL, 4);", "");
			// No GovSt expected
			assertFileResult(q, "TestSQLite.GovSt.sql", "SQLite error?", "|isGovSt|\n|-------|\n|0      |\n");
			
			// Invalid INSERT query test, trigger must throw exception.
			assertQueryResult(q, "SQLite error?", "INSERT INTO Entity VALUES(5,5);","");
			assertQueryResult(q, "SQLite error expected.", "INSERT INTO GovSt VALUES(5,'pl','flou',5);","[constraint failed]");
			
			q.execute(new SQLiteJob<Void>() {
				@Override
				protected Void job(SQLiteConnection connection)	throws Throwable 
				{
					boolean exceptionThrown = false;
					try
					{
						connection.exec("INSERT INTO GovSt VALUES(5, 'pl', 'flotte', 5);");
					}
					catch(SQLiteException e)
					{
						exceptionThrown = true;
					}
					
					assertTrue("SQLiteException expected.", exceptionThrown);
					
					return Void.class.cast(null);
				}
			}).complete();

			/*
			 * Game Server DB Design tests
			 */
			
			// Start from new DB.
			q.stop(true);
			try {
				q.join();
			} catch (InterruptedException e) {
				fail(e.getMessage());
			}

			q = new SQLiteQueue(/*new File("DBDesign")*/);
			q.start();
			
			SQLiteDebugQuery(q, "PRAGMA foreign_keys=1;");			
			assertQueryResult(q, "Foreign keys error:", "PRAGMA foreign_keys;", "|foreign_keys|\n|------------|\n|1           |");						
			
			String designSQL = "SEPSQLiteDB.server.sql";
			System.out.println();
			System.out.println(designSQL+" IMPORT...");
			assertFileResult(q, designSQL, "SQLite error?", "");					
			
			// INSERT Area
			assertQueryResult(q, "Error expected", "INSERT INTO Area VALUES(0, 0, 0, -1);", "[constraint failed]");
			assertQueryResult(q, "SQLite error?", "INSERT INTO Area VALUES(0, 0, 0, 0);", "");
			
			// INSERT Player
			assertQueryResult(q, "SQLite error?", "INSERT INTO Player VALUES('player1');", "");
			assertQueryResult(q, "SQLite error expected", "INSERT INTO Unit (owner, name, type) VALUES('player1', 'unit', NULL);", "[constraint failed]");
			assertQueryResult(q, "SQLite error expected", "INSERT INTO Unit VALUES('player1', 'unit', 'Error');", "[constraint failed]");
			assertQueryResult(q, "SQLite error?", "INSERT INTO Unit VALUES('player1', 'unit', 'Fleet');", "");

			// INSERT CelestialBody, ProductiveCelestialBody 
			assertQueryResult(q, "SQLite error?", "INSERT INTO CelestialBody (name, type, location_x, location_y, location_z) VALUES('planeteA', '"+eCelestialBodyType.Planet+"', 0, 0, 0);", "");
			assertQueryResult(q, "SQLite error?", "INSERT INTO ProductiveCelestialBody (name, initialCarbonStock, maxSlots, type) VALUES('planeteA', 10000, 4, '"+eCelestialBodyType.Planet+"');", "");

			// INSERT VersionedProductiveCelestialBody
			assertQueryResult(q, "SQLite error?", "INSERT INTO VersionedProductiveCelestialBody (name, turn, carbonStock, currentCarbon, owner, type) VALUES('planeteA', 0, 10000, 1000, 'player1', '"+eCelestialBodyType.Planet+"');", "");

			// INSERT Building
			assertQueryResult(q, "SQLite error?", "INSERT INTO Building VALUES('error', 1, 'planeteA', 0);", "[constraint failed]");
			assertQueryResult(q, "SQLite error?", "INSERT INTO Building VALUES('StarshipPlant', 1, 'planeteA', 0);", "");

		}
		catch(SQLiteException e)
		{
			e.printStackTrace();
			fail(e.getMessage());
		}
		finally
		{
			if (q != null)
			{
				q.stop(true);
				try {
					q.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	@Test
	public void testMemoryDBSaveFail() throws FileNotFoundException, IOException, InterruptedException, ClassNotFoundException
	{
		SQLiteQueue q = new SQLiteQueue();
		q.start();
		
		assertQueryResult(q, "SQLite error?", "PRAGMA foreign_keys=On;", "");
		assertQueryResult(q, "Foreign keys error:", "PRAGMA foreign_keys;", "|foreign_keys|\n|------------|\n|1           |");
		
		assertTrue("DB is not in memory.", q.execute(new SQLiteJob<Boolean>() {
			@Override
			protected Boolean job(SQLiteConnection connection) throws Throwable {
				return connection.isMemoryDatabase();
			}
		}).complete());
		
		assertQueryResult(q, "SQLite error?", "CREATE TABLE test ( nom TEXT NOT NULL, prenom TEXT NOT NULL, age INT NOT NULL, PRIMARY KEY (nom, prenom) );", "");
		assertQueryResult(q, "SQLite error?", "SELECT * FROM test;", "");
		assertQueryResult(q, "SQLite error?", "INSERT INTO test VALUES ('Escallier', 'Pierre', 26 );", "");
		assertQueryResult(q, "SQLite error?", "SELECT * FROM test;","|nom      |prenom|age|\n|---------|------|---|\n|Escallier|Pierre|26 |");
		
		final File saveFile = File.createTempFile("SQLiteTest", ".bdb");
		saveFile.deleteOnExit();
		
		if (q.execute(new SQLiteJob<Boolean>() {
			@Override
			protected Boolean job(SQLiteConnection connection) throws Throwable {
				ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(saveFile));
				
				assertTrue("Unexpected result", connection.isMemoryDatabase());
				assertNull("Unexpected result", connection.getDatabaseFile());
				
				boolean exceptionThrown = false;
				
				try
				{
					oos.writeObject(connection);
				}
				catch(NotSerializableException e)
				{
					exceptionThrown = true;
				}
				
				assertTrue("Exception expected.", exceptionThrown);
				
				oos.close();
				return exceptionThrown;
			}
		}).complete()) return;
		
		fail("Not serializable exception expected before this point.");
		
		q.stop(true);
		q.join();
	
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(saveFile));
		
		Object o = ois.readObject();
		
		assertTrue(SQLiteConnection.class.isInstance(o));
		SQLiteConnection connection = SQLiteConnection.class.cast(o);
		
		System.out.println(connection.debug("SELECT * FROM test;"));
	}
	
	@Test
	public void testSQliteSavePointsDoNotSurviveConnections() throws InterruptedException, IOException
	{
		File saveFile = File.createTempFile("SQLiteTestSavePoints", ".db");
		saveFile.deleteOnExit();
		
		SQLiteQueue q = new SQLiteQueue(saveFile);
		q.start();
		
		assertQueryResult(q, "SQLite error?", "PRAGMA foreign_keys=On;", "");
		assertQueryResult(q, "Foreign keys error:", "PRAGMA foreign_keys;", "|foreign_keys|\n|------------|\n|1           |");
		
		assertQueryResult(q, "SQLite error?", "CREATE TABLE test ( nom TEXT NOT NULL, prenom TEXT NOT NULL, age INT NOT NULL, PRIMARY KEY (nom, prenom) );", "");
		assertQueryResult(q, "SQLite error?", "SELECT * FROM test;", "");
		assertQueryResult(q, "SQLite error?", "INSERT INTO test VALUES ('Escallier', 'Pierre', 25 );", "");
		assertQueryResult(q, "SQLite error?", "SELECT * FROM test;","|nom      |prenom|age|\n|---------|------|---|\n|Escallier|Pierre|25 |");
		
		assertQueryResult(q, "SQLite error?", "SAVEPOINT save20091003;", "");
		assertQueryResult(q, "SQLite error?", "UPDATE Test SET age=26 WHERE nom='Escallier' AND prenom='Pierre';", "");
		assertQueryResult(q, "SQLite error?", "SELECT age FROM Test;", "|age|\n|---|\n|26 |");
		assertQueryResult(q, "SQLite error?", "SAVEPOINT save20101003;", "");
		assertQueryResult(q, "SQLite error?", "UPDATE Test SET age=27 WHERE nom='Escallier' AND prenom='Pierre';", "");
		assertQueryResult(q, "SQLite error?", "SELECT age FROM Test;", "|age|\n|---|\n|27 |");
		
		q.stop(true);
		q.join();
		
		q = new SQLiteQueue(saveFile);
		q.start();
		
		assertQueryResult(q, "SQLite error?", "SELECT age FROM Test;", "|age|\n|---|\n|25 |");
		
		/* If age is 27 then SQLite SAVEPOINT appear to survive connections.
		assertQueryResult(q, "SQLite error?", "SELECT age FROM Test;", "|age|\n|---|\n|27 |");
		assertQueryResult(q, "SQLite error?", "ROLLBACK TO save20101003;", "");
		assertQueryResult(q, "SQLite error?", "SELECT age FROM Test;", "|age|\n|---|\n|26 |");
		assertQueryResult(q, "SQLite error?", "ROLLBACK TO save20091003;", "");
		assertQueryResult(q, "SQLite error?", "SELECT age FROM Test;", "|age|\n|---|\n|25 |");
		assertQueryResult(q, "SQLite error?", "ROLLBACK TO save20101003;", "TODO");
		*/
	}
	
	private long unitIds = 1;
	
	@Test
	public void testSEPSQLiteDB()
	{		
		GameConfig config = new GameConfig();
		SEPSQLiteDB db;
		Vector<Player> players = new Vector<Player>();
		
		players.add(new Player("p1", new PlayerConfig()));
		players.add(new Player("p2", new PlayerConfig()));
		players.add(new Player("p3", new PlayerConfig()));
		
		try
		{
			db = new SEPSQLiteDB(new HashSet<Player>(players), config);
		
			Vector<Location> celestialBodiesLocation = db.prepare("SELECT location_x, location_y, location_z FROM CelestialBody;", new SQLiteStatementJob<Vector<Location>>()
			{
				@Override
				public Vector<Location> job(SQLiteStatement stmnt) throws SQLiteException
				{
					Vector<Location> result = new Vector<Location>();
					
					while(stmnt.step())
					{
						result.add(new Location(stmnt.columnInt(0), stmnt.columnInt(1), stmnt.columnInt(2)));
					}
					
					return result;
				}
			});
			
			boolean exceptionThrown = false;
			
			Vector<String[]> probes = new Vector<String[]>();
			
			for(int i=0; i<rnd.nextInt(5)+1; ++i)
			{
				Location l = celestialBodiesLocation.get(rnd.nextInt(celestialBodiesLocation.size()));
				Player owner = players.get(rnd.nextInt(players.size()));
				
				String unitName = String.format("U%d", ++unitIds);
				float progress = rnd.nextBoolean() ? rnd.nextFloat() : 0;
				
				Location d = (progress == 0) ? null : celestialBodiesLocation.get(rnd.nextInt(celestialBodiesLocation.size()));				
				
				db.exec("INSERT INTO Unit (owner, name, type) VALUES ('%s', '%s', 'Probe');", owner.getName(), unitName);
				db.exec("INSERT INTO Probe (owner, name, type) VALUES ('%s', '%s', 'Probe');", owner.getName(), unitName);
				
				if (i == 0)
				{
					do
					{
						d = new Location(rnd.nextInt(20), rnd.nextInt(20), rnd.nextInt(20));
					}while(db.areaExists(d));
							
					exceptionThrown = false;
					
					try
					{
						db.exec("INSERT INTO VersionedUnit (turn, owner, name, type, departure_x, departure_y, departure_z, progress, destination_x, destination_y, destination_z) VALUES (%d, '%s', '%s', 'Probe', %d, %d, %d, '%f', %s, %s, %s);", 1, owner.getName(), unitName, l.x, l.y, l.z, progress, d.x, d.y, d.z);
					}
					catch(SQLiteException e)
					{
						exceptionThrown = true;
						assertTrue("Unexpected reason.", e.getMessage().endsWith("onstraint failed"));
					}
					assertTrue("Exception expected.", exceptionThrown);
					
					d = null;
				}
				
				db.exec("INSERT INTO VersionedUnit (turn, owner, name, type, departure_x, departure_y, departure_z, progress, destination_x, destination_y, destination_z) VALUES (%d, '%s', '%s', 'Probe', %d, %d, %d, '%f', %s, %s, %s);", 1, owner.getName(), unitName, l.x, l.y, l.z, progress, d==null?"NULL":d.x, d==null?"NULL":d.y, d==null?"NULL":d.z);				
				db.exec("INSERT INTO VersionedProbe (turn, owner, name, type, destination_x, destination_y, destination_z) VALUES ('%d', '%s', '%s', 'Probe', %s, %s, %s);", 1, owner.getName(), unitName, d==null?"NULL":d.x, d==null?"NULL":d.y, d==null?"NULL":d.z);
				
				probes.add(new String[]{unitName, owner.getName()});
			}
			
			for(int i=0; i<10; ++i)
			{
				Location l = celestialBodiesLocation.get(rnd.nextInt(celestialBodiesLocation.size()));
				Player owner = players.get(rnd.nextInt(players.size()));
				
				String unitName = String.format("U%d", ++unitIds);
				float progress = rnd.nextBoolean() ? rnd.nextFloat() : 0;
				
				Location d = (progress == 0) ? null : celestialBodiesLocation.get(rnd.nextInt(celestialBodiesLocation.size()));				
				
				String[] target = probes.get(rnd.nextInt(probes.size()));
				
				db.exec("INSERT INTO Unit (owner, name, type) VALUES ('%s', '%s', 'AntiProbeMissile');", owner.getName(), unitName);
				db.exec("INSERT INTO AntiProbeMissile (owner, name, type) VALUES ('%s', '%s', 'AntiProbeMissile');", owner.getName(), unitName);				
				db.exec("INSERT INTO VersionedUnit (turn, owner, name, type, departure_x, departure_y, departure_z, progress, destination_x, destination_y, destination_z) VALUES (%d, '%s', '%s', 'AntiProbeMissile', %d, %d, %d, '%f', %s, %s, %s);", 1, owner.getName(), unitName, l.x, l.y, l.z, progress, d==null?"NULL":d.x, d==null?"NULL":d.y, d==null?"NULL":d.z);				
				db.exec("INSERT INTO VersionedAntiProbeMissile (turn, owner, name, type, targetTurn, targetOwner, targetName) VALUES ('%d', '%s', '%s', 'AntiProbeMissile', %d, '%s', '%s');", 1, owner.getName(), unitName, 1, target[1], target[0]);
			}
			
			Set<Map<String, String>> dbProbes = db.prepare("SELECT * FROM Unit U JOIN	Probe P	ON U.name = P.name AND U.owner = P.owner JOIN	VersionedUnit VU ON VU.name = U.name AND VU.owner = U.owner AND VU.turn = %d JOIN	VersionedProbe VP ON VP.name = U.name AND VP.owner = U.owner AND VP.turn = VU.turn ;", new SQLiteStatementJob<Set<Map<String, String>>>()
			{
				@Override
				public Set<Map<String, String>> job(SQLiteStatement stmnt) throws SQLiteException
				{
					Set<Map<String, String>> results = new HashSet<Map<String, String>>();					 
					while(stmnt.step())
					{
						Map<String, String> row = new HashMap<String, String>(stmnt.columnCount());
						for(int i=0; i<stmnt.columnCount(); ++i)
						{
							String col = stmnt.getColumnName(i);
							if (row.containsKey(col) && ( (stmnt.columnString(i) == null && row.get(col) != null) || ((stmnt.columnString(i) != null && row.get(col) == null)) || (stmnt.columnString(i) != null && row.get(col) != null && stmnt.columnString(i).compareTo(row.get(col)) != 0)))
							{
								throw new SQLiteException(-1, "Different values for the same column '"+col+"'");
							}
							row.put(stmnt.getColumnName(i), stmnt.columnString(i));
						}
						
						results.add(row);
					}
					
					return results;
				}
			}, 1, 1);
			
			System.out.println(dbProbes.toString());
			
			db.exportDBFile(new File("/tmp/sep_export.db"));
		}
		catch(Exception e)
		{
			e.printStackTrace();
			fail(e.getMessage());
			return;
		}
	}
}
