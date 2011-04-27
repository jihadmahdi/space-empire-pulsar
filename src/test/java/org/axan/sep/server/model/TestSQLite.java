
package org.axan.sep.server.model;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import org.axan.eplib.orm.sqlite.SQLiteDB;
import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteStatementJob;
import org.axan.eplib.utils.Reflect;
import org.axan.sep.common.GameConfig;
import org.axan.sep.common.Player;
import org.axan.sep.common.PlayerConfig;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.server.model.orm.IVersionedUnit;
import org.axan.sep.server.model.orm.VersionedAntiProbeMissile;
import org.axan.sep.server.model.orm.VersionedUnit;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.almworks.sqlite4java.SQLite;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteJob;
import com.almworks.sqlite4java.SQLiteStatement;

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
	
	private static void assertFileResult(SQLiteDB db, String sqlResourceFile, String message, String expectedResultEnd)
	{
		URL sqlURL = Reflect.getResource(TestSQLite.class.getPackage().getName(), sqlResourceFile);
		assertTrue(sqlURL != null);
		
		String result;
		try
		{
			result = db.debugSQLFile(new File(sqlURL.getFile()));
		}
		catch(SQLiteDBException e)
		{
			e.printStackTrace();
			fail(e.getMessage());
			return;
		}
		
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
	
	private static void assertQueryResult(SQLiteDB db, String message, String query, String expectedResultEnd)
	{
		String result;
		try
		{
			result = db.debug(query);
		}
		catch(SQLiteDBException e)
		{
			e.printStackTrace();
			fail(e.getMessage());
			return;
		}
		
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
	
	private static void SQLiteDebugQuery(SQLiteDB db, String query) throws SQLiteDBException
	{
		System.out.println(query);
		System.out.println(db.debug(query));
	}
	
	/**
	 * SQLite lib tests.
	 */
	@Test
	public void testSQLite()
	{		
		System.out.println("SQLite Test:");
		
		SQLiteDB db = null;
		
		try
		{
			SQLiteDB.checkSQLiteLib("target/izpack/lib/");
			
			db = new SQLiteDB(new File("sqlite_test.db"));
			
			SQLiteDebugQuery(db, "PRAGMA foreign_keys;");
			SQLiteDebugQuery(db, "PRAGMA foreign_keys=On;");
			
			assertQueryResult(db, "Foreign keys error:", "PRAGMA foreign_keys;", "|foreign_keys|\n|------------|\n|1           |");
			
			SQLiteDebugQuery(db, "PRAGMA user_version;");
			SQLiteDebugQuery(db, "PRAGMA user_version=1;");
			SQLiteDebugQuery(db, "PRAGMA user_version;");
			
			SQLiteDebugQuery(db, "PRAGMA encoding;");
			SQLiteDebugQuery(db, "PRAGMA integrity_check;");
			SQLiteDebugQuery(db, "PRAGMA quick_check;");
			
			assertQueryResult(db, "SQLite error?", "DROP TABLE IF EXISTS parents ;", "");
			assertQueryResult(db, "SQLite error?", "DROP TABLE IF EXISTS real_people ;", "");
			assertQueryResult(db, "SQLite error?", "DROP TABLE IF EXISTS people ;", "");			
			
			assertQueryResult(db, "SQLite error?", "CREATE TABLE people ( name TEXT, surname TEXT, age INTEGER, PRIMARY KEY(name, surname), UNIQUE (name, surname, age) );", "");
			assertQueryResult(db, "SQLite error?", "CREATE TABLE parents ( people_name TEXT, people_surname TEXT, parent_name TEXT, parent_surname TEXT, FOREIGN KEY(people_name, people_surname) REFERENCES people(name, surname), FOREIGN KEY(parent_name, parent_surname) REFERENCES people(name, surname) );", "");
			
			assertQueryResult(db, "SQLite error?", "INSERT INTO people VALUES ( 'A', 'a', 30) ;", "");			
			assertQueryResult(db, "SQLite error?", "INSERT INTO people VALUES ( 'B', 'b', 12) ;", "");			
			assertQueryResult(db, "SQLite error?", "INSERT INTO people VALUES ( 'C', 'c', 32) ;", "");
			assertQueryResult(db, "SQLite error?", "INSERT INTO people VALUES ( 'D', 'd', 61) ;", "");
			assertQueryResult(db, "SQLite error?", "INSERT INTO parents VALUES ( 'B', 'b', 'A', 'a') ;", "");			
			assertQueryResult(db, "SQLite error?", "INSERT INTO parents VALUES ( 'B', 'b', 'C', 'c') ;", "");
			
			assertQueryResult(db, "SQLite error?", "SELECT EXISTS (SELECT * FROM parents WHERE people_name GLOB 'B') AS NotEmpty;", "|NotEmpty|\n|--------|\n|1       |");
			
			// Cannot insert with non-existing foreign key value.
			assertQueryResult(db, "SQLite error expected", "INSERT INTO parents VALUES ( 'C', 'c', 'Z', 'z' ) ;", "[constraint failed]");
			
			// Test that (parent_name, parent_surname) is checked to be the same person. 
			assertQueryResult(db, "SQLite error expected", "INSERT INTO parents VALUES ( 'C', 'c', 'A', 'b' ) ;", "[constraint failed]");
			assertQueryResult(db, "SQLite error expected", "INSERT INTO parents VALUES ( 'C', 'a', 'A', 'c' ) ;", "[constraint failed]");
			
			assertQueryResult(db, "SQLite error?", "INSERT INTO parents VALUES ( 'C', 'c', 'A', 'a' ) ;", "");
			assertQueryResult(db, "SQLite error?", "INSERT INTO parents VALUES ( 'C', 'c', 'D', 'd' ) ;", "");
			
			// Not primary key fields used in foreign keys test			
			assertQueryResult(db, "SQLite error?", "CREATE TABLE real_people ( name TEXT, surname TEXT, age INTEGER, sex TEXT NOT NULL, CHECK(sex IN ('m', 'f')), PRIMARY KEY(name, surname), FOREIGN KEY (name, surname, age) REFERENCES people (name, surname, age) );", "");
						
			assertQueryResult(db, "SQLite error?", "SELECT * FROM people WHERE name = 'A' AND surname = 'a' AND age = 30;", "|name|surname|age|\n|----|-------|---|\n|A   |a      |30 |");
			assertQueryResult(db, "SQLite error?", "INSERT INTO real_people VALUES ( 'A', 'a', 30, 'm') ;", "");
			assertQueryResult(db, "SQLite error?", "INSERT INTO real_people VALUES ( 'A', 'a', 30, 'f') ;", "[constraint failed]");
			assertQueryResult(db, "SQLite error?", "INSERT INTO real_people VALUES ( 'B', 'b', 10, 'f') ;", "[constraint failed]");
			assertQueryResult(db, "SQLite error?", "INSERT INTO real_people VALUES ( 'B', 'b', 12, 'f') ;", "");
			
			
			// Heritance relations tests
			assertFileResult(db, "TestSQLite.creation.sql", "TestSQLite.creation.sql error", "");
			
			// New fleet creation on turn 1
			assertQueryResult(db, "SQLite error?", "INSERT INTO Entity VALUES(1, 1);", "");
			assertQueryResult(db, "SQLite error?", "INSERT INTO Fleet VALUES(1, 'pl', 'fl', 1);", "");
			// No GovSt expected
			assertFileResult(db, "TestSQLite.GovSt.sql", "SQLite error?", "|isGovSt|\n|-------|\n|0      |\n");
			
			// GovernmentStarship join on turn 2
			assertQueryResult(db, "SQLite error?", "INSERT INTO Entity VALUES(2, 2);", "");
			assertQueryResult(db, "SQLite error?", "INSERT INTO GovSt VALUES(2, 'pl', 'fl', 2);", "");
			
			// GovSt expected
			assertFileResult(db, "TestSQLite.GovSt.sql", "SQLite error?", "|isGovSt|\n|-------|\n|1      |\n");
			
			// Fleet moves on turn 3
			assertQueryResult(db, "SQLite error?", "INSERT INTO Entity VALUES(3, 3);", "");
			assertQueryResult(db, "SQLite error?", "INSERT INTO Fleet VALUES(3, 'pl', 'fl', 3);", "");
			// GovSt expected
			assertFileResult(db, "TestSQLite.GovSt.sql", "SQLite error?", "|isGovSt|\n|-------|\n|1      |\n");
			
			// Government starship split
			assertQueryResult(db, "SQLite error?", "INSERT INTO Entity VALUES(4, 4);", "");
			assertQueryResult(db, "SQLite error?", "INSERT INTO GovSt VALUES(4, 'pl', NULL, 4);", "");
			// No GovSt expected
			assertFileResult(db, "TestSQLite.GovSt.sql", "SQLite error?", "|isGovSt|\n|-------|\n|0      |\n");
			
			// Invalid INSERT query test, trigger must throw exception.
			assertQueryResult(db, "SQLite error?", "INSERT INTO Entity VALUES(5,5);","");
			assertQueryResult(db, "SQLite error expected.", "INSERT INTO GovSt VALUES(5,'pl','flou',5);","[constraint failed]");
			
			db.exec(new SQLiteJob<Void>() {
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
			});

			/*
			 * Game Server DB Design tests
			 */
			
			// Start from new DB.
			db.stop();
			try {
				db.join();
			} catch (InterruptedException e) {
				fail(e.getMessage());
			}
			
			db = new SQLiteDB();
			
			SQLiteDebugQuery(db, "PRAGMA foreign_keys=1;");			
			assertQueryResult(db, "Foreign keys error:", "PRAGMA foreign_keys;", "|foreign_keys|\n|------------|\n|1           |");						
			
			String designSQL = "SEPSQLiteDB.server.sql";
			System.out.println();
			System.out.println(designSQL+" IMPORT...");
			assertFileResult(db, designSQL, "SQLite error?", "");					
			
			// INSERT Area
			assertQueryResult(db, "Error expected", "INSERT INTO Area VALUES(0, 0, 0, -1);", "[constraint failed]");
			assertQueryResult(db, "SQLite error?", "INSERT INTO Area VALUES(0, 0, 0, 0);", "");
			
			// INSERT Player
			assertQueryResult(db, "SQLite error?", "INSERT INTO Player VALUES('player1');", "");
			assertQueryResult(db, "SQLite error expected", "INSERT INTO Unit (owner, name, type) VALUES('player1', 'unit', NULL);", "[constraint failed]");
			assertQueryResult(db, "SQLite error expected", "INSERT INTO Unit VALUES('player1', 'unit', 'Error');", "[constraint failed]");
			assertQueryResult(db, "SQLite error?", "INSERT INTO Unit VALUES('player1', 'unit', 'Fleet');", "");

			// INSERT CelestialBody, ProductiveCelestialBody 
			assertQueryResult(db, "SQLite error?", "INSERT INTO CelestialBody (name, type, location_x, location_y, location_z) VALUES('planeteA', '"+eCelestialBodyType.Planet+"', 0, 0, 0);", "");
			assertQueryResult(db, "SQLite error?", "INSERT INTO ProductiveCelestialBody (name, initialCarbonStock, maxSlots, type) VALUES('planeteA', 10000, 4, '"+eCelestialBodyType.Planet+"');", "");

			// INSERT VersionedProductiveCelestialBody
			assertQueryResult(db, "SQLite error?", "INSERT INTO VersionedProductiveCelestialBody (name, turn, carbonStock, currentCarbon, owner, type) VALUES('planeteA', 0, 10000, 1000, 'player1', '"+eCelestialBodyType.Planet+"');", "");

			// INSERT Building
			assertQueryResult(db, "SQLite error?", "INSERT INTO Building VALUES('error', 1, 'planeteA', 0);", "[constraint failed]");
			assertQueryResult(db, "SQLite error?", "INSERT INTO Building VALUES('StarshipPlant', 1, 'planeteA', 0);", "");

		}
		catch(SQLiteException e)
		{
			e.printStackTrace();
			fail(e.getMessage());
		}
		finally
		{
			if (db != null)
			{
				db.stop();
				try {
					db.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	@Test
	public void testMemoryDBSaveFail() throws FileNotFoundException, IOException, InterruptedException, ClassNotFoundException, SQLiteDBException
	{
		SQLiteDB db = new SQLiteDB();
		
		assertQueryResult(db, "SQLite error?", "PRAGMA foreign_keys=On;", "");
		assertQueryResult(db, "Foreign keys error:", "PRAGMA foreign_keys;", "|foreign_keys|\n|------------|\n|1           |");
		
		assertTrue("DB is not in memory.", db.isMemoryDatabase());
		
		assertQueryResult(db, "SQLite error?", "CREATE TABLE test ( nom TEXT NOT NULL, prenom TEXT NOT NULL, age INT NOT NULL, PRIMARY KEY (nom, prenom) );", "");
		assertQueryResult(db, "SQLite error?", "SELECT * FROM test;", "");
		assertQueryResult(db, "SQLite error?", "INSERT INTO test VALUES ('Escallier', 'Pierre', 26 );", "");
		assertQueryResult(db, "SQLite error?", "SELECT * FROM test;","|nom      |prenom|age|\n|---------|------|---|\n|Escallier|Pierre|26 |");
		
		final File saveFile = File.createTempFile("SQLiteTest", ".bdb");
		saveFile.deleteOnExit();
		
		if (db.exec(new SQLiteJob<Boolean>() {
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
		})) return;
		
		fail("Not serializable exception expected before this point.");
		
		db.stop();
		db.join();
	
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(saveFile));
		
		Object o = ois.readObject();
		
		assertTrue(SQLiteConnection.class.isInstance(o));
		SQLiteConnection connection = SQLiteConnection.class.cast(o);
		
		System.out.println(connection.debug("SELECT * FROM test;"));
	}
	
	@Test
	public void testSQliteSavePointsDoNotSurviveConnections() throws InterruptedException, IOException, SQLiteDBException
	{
		File saveFile = File.createTempFile("SQLiteTestSavePoints", ".db");
		saveFile.deleteOnExit();
		
		SQLiteDB db = new SQLiteDB(saveFile);
		
		assertQueryResult(db, "SQLite error?", "PRAGMA foreign_keys=On;", "");
		assertQueryResult(db, "Foreign keys error:", "PRAGMA foreign_keys;", "|foreign_keys|\n|------------|\n|1           |");
		
		assertQueryResult(db, "SQLite error?", "CREATE TABLE test ( nom TEXT NOT NULL, prenom TEXT NOT NULL, age INT NOT NULL, PRIMARY KEY (nom, prenom) );", "");
		assertQueryResult(db, "SQLite error?", "SELECT * FROM test;", "");
		assertQueryResult(db, "SQLite error?", "INSERT INTO test VALUES ('Escallier', 'Pierre', 25 );", "");
		assertQueryResult(db, "SQLite error?", "SELECT * FROM test;","|nom      |prenom|age|\n|---------|------|---|\n|Escallier|Pierre|25 |");
		
		assertQueryResult(db, "SQLite error?", "SAVEPOINT save20091003;", "");
		assertQueryResult(db, "SQLite error?", "UPDATE Test SET age=26 WHERE nom='Escallier' AND prenom='Pierre';", "");
		assertQueryResult(db, "SQLite error?", "SELECT age FROM Test;", "|age|\n|---|\n|26 |");
		assertQueryResult(db, "SQLite error?", "SAVEPOINT save20101003;", "");
		assertQueryResult(db, "SQLite error?", "UPDATE Test SET age=27 WHERE nom='Escallier' AND prenom='Pierre';", "");
		assertQueryResult(db, "SQLite error?", "SELECT age FROM Test;", "|age|\n|---|\n|27 |");
		
		db.stop();
		db.join();
		
		db = new SQLiteDB(saveFile);
		
		assertQueryResult(db, "SQLite error?", "SELECT age FROM Test;", "|age|\n|---|\n|25 |");
		
		/* If age is 27 then SQLite SAVEPOINT appear to survive connections.
		assertQueryResult(db, "SQLite error?", "SELECT age FROM Test;", "|age|\n|---|\n|27 |");
		assertQueryResult(db, "SQLite error?", "ROLLBACK TO save20101003;", "");
		assertQueryResult(db, "SQLite error?", "SELECT age FROM Test;", "|age|\n|---|\n|26 |");
		assertQueryResult(db, "SQLite error?", "ROLLBACK TO save20091003;", "");
		assertQueryResult(db, "SQLite error?", "SELECT age FROM Test;", "|age|\n|---|\n|25 |");
		assertQueryResult(db, "SQLite error?", "ROLLBACK TO save20101003;", "TODO");
		*/
	}
	
	private long unitIds = 1;
	
	@Test
	public void testSEPSQLiteDB()
	{		
		GameConfig config = new GameConfig();
		final SEPSQLiteDB sepDB;
		SQLiteDB db;
		Vector<Player> players = new Vector<Player>();
		
		players.add(new Player("p1", new PlayerConfig()));
		players.add(new Player("p2", new PlayerConfig()));
		players.add(new Player("p3", new PlayerConfig()));
		
		try
		{
			sepDB = new SEPSQLiteDB(new HashSet<Player>(players), config);
			db = sepDB.getDB();
		
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
					}while(sepDB.areaExists(d));
							
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

				for(int turn=1; turn<6; ++turn)
				{
					db.exec("INSERT INTO VersionedUnit (turn, owner, name, type, departure_x, departure_y, departure_z, progress, destination_x, destination_y, destination_z) VALUES (%d, '%s', '%s', 'AntiProbeMissile', %d, %d, %d, '%f', %s, %s, %s);", turn, owner.getName(), unitName, l.x, l.y, l.z, turn * progress / 5, d==null?"NULL":d.x, d==null?"NULL":d.y, d==null?"NULL":d.z);				
					db.exec("INSERT INTO VersionedAntiProbeMissile (turn, owner, name, type, targetTurn, targetOwner, targetName) VALUES ('%d', '%s', '%s', 'AntiProbeMissile', %d, '%s', '%s');", turn, owner.getName(), unitName, 1, target[1], target[0]);
				}
			}
			
			long t1 = System.currentTimeMillis();
			String q = "SELECT U.type, * FROM Unit U LEFT JOIN	VersionedUnit VU USING (name, owner, type) LEFT JOIN	PulsarMissile PM USING (name, owner, type) LEFT JOIN	Probe P USING (name, owner, type) LEFT JOIN	AntiProbeMissile APM USING (name, owner, type) LEFT JOIN	CarbonCarrier CC USING (name, owner, type) LEFT JOIN	SpaceRoadDeliverer SRD USING (name, owner, type) LEFT JOIN	Fleet F USING (name, owner, type) LEFT JOIN	VersionedPulsarMissile VPM USING (name, owner, turn) LEFT JOIN	VersionedProbe VP USING (name, owner, turn) LEFT JOIN	VersionedAntiProbeMissile VAPM USING (name, owner, turn) LEFT JOIN	VersionedCarbonCarrier VCC USING (name, owner, turn) LEFT JOIN	VersionedFleet VF USING (name, owner, turn) WHERE VU.turn = 1;";
			q = "SELECT U.type, U.name, U.owner, VU.turn, VU.progress FROM Unit U LEFT JOIN	VersionedUnit VU USING (name, owner, type) LEFT JOIN	PulsarMissile PM USING (name, owner, type) LEFT JOIN	Probe P USING (name, owner, type) LEFT JOIN	AntiProbeMissile APM USING (name, owner, type) LEFT JOIN	CarbonCarrier CC USING (name, owner, type) LEFT JOIN	SpaceRoadDeliverer SRD USING (name, owner, type) LEFT JOIN	Fleet F USING (name, owner, type) LEFT JOIN	VersionedPulsarMissile VPM USING (name, owner, turn) LEFT JOIN	VersionedProbe VP USING (name, owner, turn) LEFT JOIN	VersionedAntiProbeMissile VAPM USING (name, owner, turn) LEFT JOIN	VersionedCarbonCarrier VCC USING (name, owner, turn) LEFT JOIN	VersionedFleet VF USING (name, owner, turn) WHERE VU.turn = ( SELECT MAX(VVUU.turn) FROM VersionedUnit VVUU WHERE VVUU.name = VU.name AND VVUU.owner = VU.owner );";
			Set<IVersionedUnit> units = db.prepare(q, new SQLiteStatementJob<Set<IVersionedUnit>>()
			{
				@Override
				public Set<IVersionedUnit> job(SQLiteStatement stmnt) throws SQLiteException
				{					
					Set<Map<String, String>> results = new HashSet<Map<String, String>>();
					Set<IVersionedUnit> units = new HashSet<IVersionedUnit>();
					
					while(stmnt.step())
					{
						try
						{
							// DEAD LOCK, VersionedUnit constructor need GameConfig to make query while already in job, waiting for result produce dead lock.
							units.add(VersionedUnit.create(eUnitType.valueOf(stmnt.columnString(0)), stmnt, sepDB.getConfig()));
						}
						catch(Exception e)
						{
							throw new SQLiteDBException(e);
						}
						
						/*
						Map<String, String> row = new HashMap<String, String>(stmnt.columnCount());
						for(int i=0; i<stmnt.columnCount(); ++i)
						{
							String col = stmnt.getColumnName(i);
							if (row.containsKey(col) && ( (stmnt.columnString(i) == null && row.get(col) != null) || ((stmnt.columnString(i) != null && row.get(col) == null)) || (stmnt.columnString(i) != null && row.get(col) != null && stmnt.columnString(i).compareTo(row.get(col)) != 0)))
							{
								throw new SQLiteException(-1, "Different values for the same column '"+col+"'");
							}
							System.out.format("%s type: %s", col, stmnt.columnType(i));
							
							row.put(stmnt.getColumnName(i), stmnt.columnString(i));
						}
						
						results.add(row);
						*/
					}
					
					return units;
				}
			});
			Thread.sleep(1000);
			long t2 = System.currentTimeMillis();
			
			for(IVersionedUnit vu : units)
			{
				System.out.println(vu.toString());
			}
			System.out.println("in "+(t2-t1)+"ms");
			
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
