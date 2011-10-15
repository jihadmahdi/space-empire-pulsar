
package org.axan.sep.server;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import org.axan.eplib.orm.DataBaseORMGenerator;
import org.axan.eplib.orm.ISQLDataBase;
import org.axan.eplib.orm.ISQLDataBaseStatement;
import org.axan.eplib.orm.ISQLDataBaseStatementJob;
import org.axan.eplib.orm.SQLDataBaseException;
import org.axan.eplib.orm.hsqldb.HSQLDB;
import org.axan.eplib.orm.sqlite.SQLiteDB;
import org.axan.eplib.utils.Reflect;
import org.axan.sep.common.GameConfig;
import org.axan.sep.common.PlayerConfig;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.IVersionedUnit;
import org.axan.sep.common.db.orm.Unit;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

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
	
	private static void assertFileResult(ISQLDataBase db, String sqlResourceFile, String message, String expectedResultEnd)
	{
		URL sqlURL = Reflect.getResource(TestSQLite.class.getPackage().getName(), sqlResourceFile);
		assertTrue(sqlURL != null);
		
		String result;
		try
		{
			result = db.debugSQLFile(new File(sqlURL.getFile()));
		}
		catch(SQLDataBaseException e)
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
	
	private static void assertQueryResult(ISQLDataBase db, String message, String query, String expectedResultEnd)
	{
		String result;
		try
		{
			result = db.debug(query);
		}
		catch(SQLDataBaseException e)
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
	
	private static void SQLDebugQuery(ISQLDataBase db, String query) throws SQLDataBaseException
	{
		System.out.println(query);
		System.out.println(db.debug(query));
	}
	
	private static ISQLDataBase createNewDB(File dbFile) throws Exception
	{
		return new SQLiteDB(dbFile);
		//return new HSQLDB(dbFile, "sa", "");
	}
	
	private static ISQLDataBase createNewDB() throws Exception
	{
		return createNewDB(File.createTempFile("TestSQLDataBase_", ".sep"));
	}
	
	/**
	 * SQL lib tests.
	 */
	@Test
	public void testSQLDB()
	{		
		System.out.println("SQL DB Test:");
		
		ISQLDataBase db = null;
		
		try
		{
			SQLiteDB.checkSQLiteLib("target/izpack/lib/");
			
			db = createNewDB();
			
			SQLDebugQuery(db, "PRAGMA foreign_keys;");
			SQLDebugQuery(db, "PRAGMA foreign_keys=On;");
			
			assertQueryResult(db, "Foreign keys error:", "PRAGMA foreign_keys;", "|foreign_keys|\n|------------|\n|1           |");
			
			SQLDebugQuery(db, "PRAGMA user_version;");
			SQLDebugQuery(db, "PRAGMA user_version=1;");
			SQLDebugQuery(db, "PRAGMA user_version;");
			
			SQLDebugQuery(db, "PRAGMA encoding;");
			SQLDebugQuery(db, "PRAGMA integrity_check;");
			SQLDebugQuery(db, "PRAGMA quick_check;");
			
			assertQueryResult(db, "SQL error?", "DROP TABLE IF EXISTS parents ;", "");
			assertQueryResult(db, "SQL error?", "DROP TABLE IF EXISTS real_people ;", "");
			assertQueryResult(db, "SQL error?", "DROP TABLE IF EXISTS people ;", "");			
			
			assertQueryResult(db, "SQL error?", "CREATE TABLE people ( name TEXT, surname TEXT, age INTEGER, PRIMARY KEY(name, surname), UNIQUE (name, surname, age) );", "");
			assertQueryResult(db, "SQL error?", "CREATE TABLE parents ( people_name TEXT, people_surname TEXT, parent_name TEXT, parent_surname TEXT, FOREIGN KEY(people_name, people_surname) REFERENCES people(name, surname), FOREIGN KEY(parent_name, parent_surname) REFERENCES people(name, surname) );", "");
			
			assertQueryResult(db, "SQL error?", "INSERT INTO people VALUES ( 'A', 'a', 30) ;", "");			
			assertQueryResult(db, "SQL error?", "INSERT INTO people VALUES ( 'B', 'b', 12) ;", "");			
			assertQueryResult(db, "SQL error?", "INSERT INTO people VALUES ( 'C', 'c', 32) ;", "");
			assertQueryResult(db, "SQL error?", "INSERT INTO people VALUES ( 'D', 'd', 61) ;", "");
			assertQueryResult(db, "SQL error?", "INSERT INTO parents VALUES ( 'B', 'b', 'A', 'a') ;", "");			
			assertQueryResult(db, "SQL error?", "INSERT INTO parents VALUES ( 'B', 'b', 'C', 'c') ;", "");
			
			assertQueryResult(db, "SQL error?", "SELECT EXISTS (SELECT * FROM parents WHERE people_name GLOB 'B') AS NotEmpty;", "|NotEmpty|\n|--------|\n|1       |");
			
			// Cannot insert with non-existing foreign key value.
			assertQueryResult(db, "SQL error expected", "INSERT INTO parents VALUES ( 'C', 'c', 'Z', 'z' ) ;", "[constraint failed]");
			
			// Test that (parent_name, parent_surname) is checked to be the same person. 
			assertQueryResult(db, "SQL error expected", "INSERT INTO parents VALUES ( 'C', 'c', 'A', 'b' ) ;", "[constraint failed]");
			assertQueryResult(db, "SQL error expected", "INSERT INTO parents VALUES ( 'C', 'a', 'A', 'c' ) ;", "[constraint failed]");
			
			assertQueryResult(db, "SQL error?", "INSERT INTO parents VALUES ( 'C', 'c', 'A', 'a' ) ;", "");
			assertQueryResult(db, "SQL error?", "INSERT INTO parents VALUES ( 'C', 'c', 'D', 'd' ) ;", "");
			
			// Not primary key fields used in foreign keys test			
			assertQueryResult(db, "SQL error?", "CREATE TABLE real_people ( name TEXT, surname TEXT, age INTEGER, sex TEXT NOT NULL, CHECK(sex IN ('m', 'f')), PRIMARY KEY(name, surname), FOREIGN KEY (name, surname, age) REFERENCES people (name, surname, age) );", "");
						
			assertQueryResult(db, "SQL error?", "SELECT * FROM people WHERE name = 'A' AND surname = 'a' AND age = 30;", "|name|surname|age|\n|----|-------|---|\n|A   |a      |30 |");
			assertQueryResult(db, "SQL error?", "INSERT INTO real_people VALUES ( 'A', 'a', 30, 'm') ;", "");
			assertQueryResult(db, "SQL error?", "INSERT INTO real_people VALUES ( 'A', 'a', 30, 'f') ;", "[constraint failed]");
			assertQueryResult(db, "SQL error?", "INSERT INTO real_people VALUES ( 'B', 'b', 10, 'f') ;", "[constraint failed]");
			assertQueryResult(db, "SQL error?", "INSERT INTO real_people VALUES ( 'B', 'b', 12, 'f') ;", "");
			
			/* Impossible because of the foreignn key constraint
			assertQueryResult(db, "SQL error?", "SELECT age FROM people WHERE name = 'B' AND surname = 'b';", "|age|\n|---|\n|12 |");
			assertQueryResult(db, "SQL error?", "INSERT OR REPLACE INTO people VALUES ( 'B', 'b', 14) ;", "");
			assertQueryResult(db, "SQL error?", "SELECT age FROM people WHERE name = 'B' AND surname = 'b';", "|age|\n|---|\n|14 |");
			assertQueryResult(db, "SQL error?", "SELECT age FROM people WHERE name = 'C' AND surname = 'c';", "");
			assertQueryResult(db, "SQL error?", "INSERT OR REPLACE INTO people VALUES ( 'C', 'c', 20) ;", "");
			assertQueryResult(db, "SQL error?", "SELECT age FROM people WHERE name = 'C' AND surname = 'c';", "|age|\n|---|\n|20 |");
			*/
			
			
			// Heritance relations tests
			assertFileResult(db, "TestSQLite.creation.sql", "TestSQLite.creation.sql error", "");
			
			// New fleet creation on turn 1
			assertQueryResult(db, "SQL error?", "INSERT INTO Entity VALUES(1, 1);", "");
			assertQueryResult(db, "SQL error?", "INSERT INTO Fleet VALUES(1, 'pl', 'fl', 1);", "");
			// No GovSt expected
			assertFileResult(db, "TestSQLite.GovSt.sql", "SQL error?", "|isGovSt|\n|-------|\n|0      |\n");
			
			// GovernmentStarship join on turn 2
			assertQueryResult(db, "SQL error?", "INSERT INTO Entity VALUES(2, 2);", "");
			assertQueryResult(db, "SQL error?", "INSERT INTO GovSt VALUES(2, 'pl', 'fl', 2);", "");
			
			// GovSt expected
			assertFileResult(db, "TestSQLite.GovSt.sql", "SQL error?", "|isGovSt|\n|-------|\n|1      |\n");
			
			// Fleet moves on turn 3
			assertQueryResult(db, "SQL error?", "INSERT INTO Entity VALUES(3, 3);", "");
			assertQueryResult(db, "SQL error?", "INSERT INTO Fleet VALUES(3, 'pl', 'fl', 3);", "");
			// GovSt expected
			assertFileResult(db, "TestSQLite.GovSt.sql", "SQL error?", "|isGovSt|\n|-------|\n|1      |\n");
			
			// Government starship split
			assertQueryResult(db, "SQL error?", "INSERT INTO Entity VALUES(4, 4);", "");
			assertQueryResult(db, "SQL error?", "INSERT INTO GovSt VALUES(4, 'pl', NULL, 4);", "");
			// No GovSt expected
			assertFileResult(db, "TestSQLite.GovSt.sql", "SQL error?", "|isGovSt|\n|-------|\n|0      |\n");
			
			// Invalid INSERT query test, trigger must throw exception.
			assertQueryResult(db, "SQL error?", "INSERT INTO Entity VALUES(5,5);","");
			assertQueryResult(db, "SQL error expected.", "INSERT INTO GovSt VALUES(5,'pl','flou',5);","[constraint failed]");
			
			boolean exceptionThrown = false;
			try
			{
				db.exec("INSERT INTO GovSt VALUES(5, 'pl', 'flotte', 5);");
			}
			catch(SQLDataBaseException e)
			{
				exceptionThrown = true;
			}
					
			assertTrue("SQLDataBaseException expected.", exceptionThrown);
		}
		catch(Throwable t)
		{
			t.printStackTrace();
			fail(t.getMessage());
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
	
	/*
	@Test
	public void testMemoryDBSaveFail() throws FileNotFoundException, IOException, InterruptedException, ClassNotFoundException, SQLiteDBException
	{
		SQLiteDB db = new SQLiteDB();
		
		assertQueryResult(db, "SQL error?", "PRAGMA foreign_keys=On;", "");
		assertQueryResult(db, "Foreign keys error:", "PRAGMA foreign_keys;", "|foreign_keys|\n|------------|\n|1           |");
		
		assertTrue("DB is not in memory.", db.isMemoryDatabase());
		
		assertQueryResult(db, "SQL error?", "CREATE TABLE test ( nom TEXT NOT NULL, prenom TEXT NOT NULL, age INT NOT NULL, PRIMARY KEY (nom, prenom) );", "");
		assertQueryResult(db, "SQL error?", "SELECT * FROM test;", "");
		assertQueryResult(db, "SQL error?", "INSERT INTO test VALUES ('Escallier', 'Pierre', 26 );", "");
		assertQueryResult(db, "SQL error?", "SELECT * FROM test;","|nom      |prenom|age|\n|---------|------|---|\n|Escallier|Pierre|26 |");
		
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
	*/
	
	@Test
	public void testSQliteSavePointsDoNotSurviveConnections() throws Exception
	{
		File saveFile = File.createTempFile("SQLiteTestSavePoints", ".db");
		saveFile.deleteOnExit();
		
		ISQLDataBase db = createNewDB(saveFile);
		
		assertQueryResult(db, "SQL error?", "PRAGMA foreign_keys=On;", "");
		assertQueryResult(db, "Foreign keys error:", "PRAGMA foreign_keys;", "|foreign_keys|\n|------------|\n|1           |");
		
		assertQueryResult(db, "SQL error?", "CREATE TABLE test ( nom TEXT NOT NULL, prenom TEXT NOT NULL, age INT NOT NULL, PRIMARY KEY (nom, prenom) );", "");
		assertQueryResult(db, "SQL error?", "SELECT * FROM test;", "");
		assertQueryResult(db, "SQL error?", "INSERT INTO test VALUES ('Escallier', 'Pierre', 25 );", "");
		assertQueryResult(db, "SQL error?", "SELECT * FROM test;","|nom      |prenom|age|\n|---------|------|---|\n|Escallier|Pierre|25 |");
		
		assertQueryResult(db, "SQL error?", "SAVEPOINT save20091003;", "");
		assertQueryResult(db, "SQL error?", "UPDATE Test SET age=26 WHERE nom='Escallier' AND prenom='Pierre';", "");
		assertQueryResult(db, "SQL error?", "SELECT age FROM Test;", "|age|\n|---|\n|26 |");
		assertQueryResult(db, "SQL error?", "SAVEPOINT save20101003;", "");
		assertQueryResult(db, "SQL error?", "UPDATE Test SET age=27 WHERE nom='Escallier' AND prenom='Pierre';", "");
		assertQueryResult(db, "SQL error?", "SELECT age FROM Test;", "|age|\n|---|\n|27 |");
		
		db.stop();
		db.join();
		
		db = createNewDB(saveFile);
		
		assertQueryResult(db, "SQL error?", "SELECT age FROM Test;", "|age|\n|---|\n|25 |");
		
		/* If age is 27 then SQLite SAVEPOINT appear to survive connections.
		assertQueryResult(db, "SQL error?", "SELECT age FROM Test;", "|age|\n|---|\n|27 |");
		assertQueryResult(db, "SQL error?", "ROLLBACK TO save20101003;", "");
		assertQueryResult(db, "SQL error?", "SELECT age FROM Test;", "|age|\n|---|\n|26 |");
		assertQueryResult(db, "SQL error?", "ROLLBACK TO save20091003;", "");
		assertQueryResult(db, "SQL error?", "SELECT age FROM Test;", "|age|\n|---|\n|25 |");
		assertQueryResult(db, "SQL error?", "ROLLBACK TO save20101003;", "TODO");
		*/
	}
	
	private long unitIds = 1;
	
	@Test
	public void testSEPSQLiteDB()
	{		
		GameConfig config = new GameConfig();
		final GameBoard sepDB;
		ISQLDataBase db;
		Vector<org.axan.sep.common.Player> players = new Vector<org.axan.sep.common.Player>();
		
		players.add(new org.axan.sep.common.Player("p1", new PlayerConfig()));
		players.add(new org.axan.sep.common.Player("p2", new PlayerConfig()));
		players.add(new org.axan.sep.common.Player("p3", new PlayerConfig()));
		
		try
		{
			sepDB = new GameBoard(createNewDB(), new HashSet<org.axan.sep.common.Player>(players), config);
			db = sepDB.getDB();
		
			final Vector<Location> celestialBodiesLocation = db.prepare("SELECT location_x, location_y, location_z FROM CelestialBody;", new ISQLDataBaseStatementJob<Vector<Location>>()
			{
				@Override
				public Vector<Location> job(ISQLDataBaseStatement stmnt) throws SQLDataBaseException
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
			Set<String> locatedUnitsName = new HashSet<String>();
			
			for(int i=0; i<rnd.nextInt(5)+1; ++i)
			{
				int cb = rnd.nextInt(celestialBodiesLocation.size());
				Location l = celestialBodiesLocation.get(cb);
				org.axan.sep.common.Player owner = players.get(rnd.nextInt(players.size()));
				
				String unitName = String.format("U%d", ++unitIds);
				float progress = rnd.nextBoolean() ? rnd.nextFloat() : 0;
				
				Location d = (progress == 0) ? null : celestialBodiesLocation.get(rnd.nextInt(celestialBodiesLocation.size()));				
				
				db.exec("INSERT INTO Unit (owner, name, type) VALUES (?, ?, 'Probe');", owner.getName(), unitName);
				db.exec("INSERT INTO Probe (owner, name, type) VALUES (?, ?, 'Probe');", owner.getName(), unitName);								
				
				if (i == 0)
				{
					do
					{
						d = new Location(rnd.nextInt(20), rnd.nextInt(20), rnd.nextInt(20));
					}while(sepDB.areaExists(d));
							
					exceptionThrown = false;
					
					try
					{
						//db.exec("INSERT INTO VersionedUnit (turn, owner, name, type, departure_x, departure_y, departure_z, progress, destination_x, destination_y, destination_z) VALUES (%d, '%s', '%s', 'Probe', %d, %d, %d, '%f', %s, %s, %s);", 1, owner.getName(), unitName, l.x, l.y, l.z, progress, d.x, d.y, d.z);
						db.exec("INSERT INTO VersionedUnit (turn, owner, name, type, departure_x, departure_y, departure_z, progress, destination_x, destination_y, destination_z) VALUES (?, ?, ?, 'Probe', ?, ?, ?, ?, ?, ?, ?);", 1, owner.getName(), unitName, l.x, l.y, l.z, progress, d.x, d.y, d.z);
					}
					catch(SQLDataBaseException e)
					{
						exceptionThrown = true;
						assertTrue("Unexpected reason : "+e.getMessage(), e.getMessage().endsWith("[constraint failed]"));
					}
					assertTrue("Exception expected.", exceptionThrown);
					
					d = null;
				}
				
				db.exec("INSERT INTO VersionedUnit (turn, owner, name, type, departure_x, departure_y, departure_z, progress, destination_x, destination_y, destination_z) VALUES (?, ?, ?, 'Probe', ?, ?, ?, ?, ?, ?, ?);", 1, owner.getName(), unitName, l.x, l.y, l.z, progress, d==null?null:d.x, d==null?null:d.y, d==null?null:d.z);				
				db.exec("INSERT INTO VersionedProbe (turn, owner, name, type) VALUES (?, ?, ?, 'Probe');", 1, owner.getName(), unitName);
				
				if (cb == 0) locatedUnitsName.add(unitName);
				
				probes.add(new String[]{unitName, owner.getName()});
			}
			
			for(int i=0; i<10; ++i)
			{
				int cb = rnd.nextInt(celestialBodiesLocation.size());
				Location l = celestialBodiesLocation.get(cb);
				org.axan.sep.common.Player owner = players.get(rnd.nextInt(players.size()));
				
				String unitName = String.format("U%d", ++unitIds);
				float progress = rnd.nextBoolean() ? rnd.nextFloat() : 0;
				
				Location d = (progress == 0) ? null : celestialBodiesLocation.get(rnd.nextInt(celestialBodiesLocation.size()));				
				
				String[] target = probes.get(rnd.nextInt(probes.size()));
				
				db.exec("INSERT INTO Unit (owner, name, type) VALUES (?, ?, 'AntiProbeMissile');", owner.getName(), unitName);
				db.exec("INSERT INTO AntiProbeMissile (owner, name, type) VALUES (?, ?, 'AntiProbeMissile');", owner.getName(), unitName);

				for(int turn=1; turn<6; ++turn)
				{
					db.exec("INSERT INTO VersionedUnit (turn, owner, name, type, departure_x, departure_y, departure_z, progress, destination_x, destination_y, destination_z) VALUES (?, ?, ?, 'AntiProbeMissile', ?, ?, ?, ?, ?, ?, ?);", turn, owner.getName(), unitName, l.x, l.y, l.z, turn * progress / 5, d==null?null:d.x, d==null?null:d.y, d==null?null:d.z);				
					db.exec("INSERT INTO VersionedAntiProbeMissile (turn, owner, name, type, targetTurn, targetOwner, targetName) VALUES (?, ?, ?, 'AntiProbeMissile', ?, ?, ?);", turn, owner.getName(), unitName, 1, target[1], target[0]);
				}
				
				if (cb == 0) locatedUnitsName.add(unitName);
			}
			
			long t1 = System.currentTimeMillis();
			String q = "SELECT U.type, * FROM Unit U LEFT JOIN	VersionedUnit VU USING (name, owner, type) LEFT JOIN	PulsarMissile PM USING (name, owner, type) LEFT JOIN	Probe P USING (name, owner, type) LEFT JOIN	AntiProbeMissile APM USING (name, owner, type) LEFT JOIN	CarbonCarrier CC USING (name, owner, type) LEFT JOIN	SpaceRoadDeliverer SRD USING (name, owner, type) LEFT JOIN	Fleet F USING (name, owner, type) LEFT JOIN	VersionedPulsarMissile VPM USING (name, owner, turn) LEFT JOIN	VersionedProbe VP USING (name, owner, turn) LEFT JOIN	VersionedAntiProbeMissile VAPM USING (name, owner, turn) LEFT JOIN	VersionedCarbonCarrier VCC USING (name, owner, turn) LEFT JOIN	VersionedFleet VF USING (name, owner, turn) WHERE VU.turn = 1;";
			q = "SELECT U.type, U.name, U.owner, VU.turn, VU.progress FROM Unit U LEFT JOIN	VersionedUnit VU USING (name, owner, type) LEFT JOIN	PulsarMissile PM USING (name, owner, type) LEFT JOIN	Probe P USING (name, owner, type) LEFT JOIN	AntiProbeMissile APM USING (name, owner, type) LEFT JOIN	CarbonCarrier CC USING (name, owner, type) LEFT JOIN	SpaceRoadDeliverer SRD USING (name, owner, type) LEFT JOIN	Fleet F USING (name, owner, type) LEFT JOIN	VersionedPulsarMissile VPM USING (name, owner, turn) LEFT JOIN	VersionedProbe VP USING (name, owner, turn) LEFT JOIN	VersionedAntiProbeMissile VAPM USING (name, owner, turn) LEFT JOIN	VersionedCarbonCarrier VCC USING (name, owner, turn) LEFT JOIN	VersionedFleet VF USING (name, owner, turn) WHERE VU.turn = ( SELECT MAX(VVUU.turn) FROM VersionedUnit VVUU WHERE VVUU.name = VU.name AND VVUU.owner = VU.owner );";
			Set<IVersionedUnit> units = db.prepare(q, new ISQLDataBaseStatementJob<Set<IVersionedUnit>>()
			{
				@Override
				public Set<IVersionedUnit> job(ISQLDataBaseStatement stmnt) throws SQLDataBaseException
				{					
					Set<Map<String, String>> results = new HashSet<Map<String, String>>();
					Set<IVersionedUnit> units = new HashSet<IVersionedUnit>();
					
					while(stmnt.step())
					{
						try
						{
							// DEAD LOCK, VersionedUnit constructor need GameConfig to make query while already in job, waiting for result produce dead lock.
							eUnitType type = eUnitType.valueOf(stmnt.columnString(0));
							Class<? extends IVersionedUnit> clazz = (Class<? extends IVersionedUnit>)  Class.forName(String.format("%s.Versioned%s", Unit.class.getPackage().getName(), type.toString()));
							units.add(DataBaseORMGenerator.mapTo(clazz, stmnt, sepDB.getConfig()));
						}
						catch(Exception e)
						{
							throw new SQLDataBaseException(e);
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
			long t2 = System.currentTimeMillis();
			
			Set<? extends IVersionedUnit> units2 = Unit.selectMaxVersion(sepDB.getSEPDB(), IVersionedUnit.class, null, null, null); 
			
			long t3 = System.currentTimeMillis();
			
			for(IVersionedUnit vu : units)
			{
				System.out.format("%s - %s (%s) v%d%n", vu.getType(), vu.getName(), vu.getOwner(), vu.getTurn());
			}
			System.out.println("in "+(t2-t1)+"ms");
			
			for(IVersionedUnit vu : units2)
			{
				System.out.format("%s - %s (%s) v%d%n", vu.getType(), vu.getName(), vu.getOwner(), vu.getTurn());
				if (vu.getOwner().matches("p2") && vu.getName().matches("F1"))
				{
					fail("Unexpected unit exist.");
				}
			}
			System.out.println("in "+(t3-t2)+"ms");
			
			/*
			Set<? extends IVersionedUnit> units3 = db.exec(new SQLiteJob<Set<? extends IVersionedUnit>>()
			{
				@Override
				protected Set<? extends IVersionedUnit> job(SQLiteConnection conn) throws Throwable
				{
					VersionedFleet vf = new VersionedFleet("p2", "F1", eUnitType.Fleet, sepDB.getConfig().getTurn(), celestialBodiesLocation.lastElement(), 0.0, null, sepDB.getConfig().getUnitTypeSight(eUnitType.Fleet));
					Unit.insertOrUpdate(conn, vf);
					return Unit.selectMaxVersion(conn, sepDB.getConfig(), IVersionedUnit.class, null, null, "Unit.name = 'F1'");
				}
			});
			long t4 = System.currentTimeMillis();
			
			for(IVersionedUnit vu : units3)
			{
				System.out.format("%s - %s (%s) v%d%n", vu.getType(), vu.getName(), vu.getOwner(), vu.getTurn());
			}
			System.out.println("in "+(t4-t3)+"ms");
			
			assertTrue("Only one unit expected to match.", units3.size() == 1);
			IVersionedUnit f = units3.iterator().next();
			assertTrue("Unexpected unit", f.getOwner().matches("p2") && f.getName().matches("F1") && f.getType().equals(eUnitType.Fleet) && IVersionedFleet.class.isInstance(f));
			assertTrue("Unexpected value", f.getProgress() == 0.0);
			
			Set<? extends IVersionedUnit> units4 = db.exec(new SQLiteJob<Set<? extends IVersionedUnit>>()
			{
				@Override
				protected Set<? extends IVersionedUnit> job(SQLiteConnection conn) throws Throwable
				{
					VersionedFleet vf = new VersionedFleet("p2", "F1", eUnitType.Fleet, sepDB.getConfig().getTurn(), celestialBodiesLocation.lastElement(), 100.0, null, sepDB.getConfig().getUnitTypeSight(eUnitType.Fleet));
					Unit.insertOrUpdate(conn, vf);
					return Unit.selectMaxVersion(conn, sepDB.getConfig(), IVersionedUnit.class, null, null, "Unit.name = 'F1'");
				}
			});
			long t5 = System.currentTimeMillis();
			
			for(IVersionedUnit vu : units4)
			{
				System.out.format("%s - %s (%s) v%d%n", vu.getType(), vu.getName(), vu.getOwner(), vu.getTurn());
			}
			System.out.println("in "+(t5-t4)+"ms");
			
			assertTrue("Only one unit expected to match.", units4.size() == 1);
			f = units4.iterator().next();
			assertTrue("Unexpected unit", f.getOwner().matches("p2") && f.getName().matches("F1") && f.getType().equals(eUnitType.Fleet) && IVersionedFleet.class.isInstance(f));
			assertTrue("Unexpected value", f.getProgress() == 100.0);
			
			///
			
			Set<IVersionedUnit> vus = db.exec(new SQLiteJob<Set<IVersionedUnit>>()
			{
				@Override
				protected Set<IVersionedUnit> job(SQLiteConnection conn) throws Throwable
				{
					return Unit.selectMaxVersion(conn, sepDB.getConfig(), IVersionedUnit.class, null, "CelestialBody CB", "CB.name = 'A' AND VersionedUnit.departure_x = CB.location_x AND VersionedUnit.departure_y = CB.location_y AND VersionedUnit.departure_z = CB.location_z");
				}
			});
			
			Set<String> unlocatedUnitsName = new HashSet<String>(locatedUnitsName);
			
			for(IVersionedUnit vu : vus)
			{
				assertTrue("Unexpected unit on first celestial body.", locatedUnitsName.contains(vu.getName()));
				unlocatedUnitsName.remove(vu.getName());
			}
			
			assertTrue("Unexpected unit locations", unlocatedUnitsName.isEmpty());
			
			Set<IPlayer> ps = db.exec(new SQLiteJob<Set<IPlayer>>()
			{
				
				@Override
				protected Set<IPlayer> job(SQLiteConnection conn) throws Throwable
				{
					return Player.select(conn, sepDB.getConfig(), IPlayer.class, null, null);
				}
			});
			
			assertTrue("Unexpected result", ps.size() == 3);
			*/
			
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
