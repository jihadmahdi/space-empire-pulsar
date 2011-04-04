package org.axan.sep.server;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.Stack;

import org.axan.sep.client.SEPClient;
import org.axan.sep.client.gui.SpaceEmpirePulsarGUI;
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

public class TestSQLite
{
	
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
			
			SQLiteQueue q = new SQLiteQueue(new File("sqlite_test.db"));
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
			assertQueryResult(q, "SQLite error?", "DROP TABLE IF EXISTS people ;", "");			
			
			assertQueryResult(q, "SQLite error?", "CREATE TABLE people ( name TEXT, surname TEXT, age INTEGER, PRIMARY KEY(name, surname) );", "");
			assertQueryResult(q, "SQLite error?", "CREATE TABLE parents ( people_name TEXT, people_surname TEXT, parent_name TEXT, parent_surname TEXT, FOREIGN KEY(people_name, people_surname) REFERENCES people(name, surname), FOREIGN KEY(parent_name, parent_surname) REFERENCES people(name, surname) );", "");
			
			assertQueryResult(q, "SQLite error?", "INSERT INTO people VALUES ( 'A', 'a', 30) ;", "");			
			assertQueryResult(q, "SQLite error?", "INSERT INTO people VALUES ( 'B', 'b', 12) ;", "");			
			assertQueryResult(q, "SQLite error?", "INSERT INTO people VALUES ( 'C', 'c', 32) ;", "");
			assertQueryResult(q, "SQLite error?", "INSERT INTO people VALUES ( 'D', 'd', 61) ;", "");
			assertQueryResult(q, "SQLite error?", "INSERT INTO parents VALUES ( 'B', 'b', 'A', 'a') ;", "");			
			assertQueryResult(q, "SQLite error?", "INSERT INTO parents VALUES ( 'B', 'b', 'C', 'c') ;", "");
			
			// Cannot insert with non-existing foreign key value.
			assertQueryResult(q, "SQLite error expected", "INSERT INTO parents VALUES ( 'C', 'c', 'Z', 'z' ) ;", "[constraint failed]");
			
			// Test that (parent_name, parent_surname) is checked to be the same person. 
			assertQueryResult(q, "SQLite error expected", "INSERT INTO parents VALUES ( 'C', 'c', 'A', 'b' ) ;", "[constraint failed]");
			assertQueryResult(q, "SQLite error expected", "INSERT INTO parents VALUES ( 'C', 'a', 'A', 'c' ) ;", "[constraint failed]");
			
			assertQueryResult(q, "SQLite error?", "INSERT INTO parents VALUES ( 'C', 'c', 'A', 'a' ) ;", "");
			assertQueryResult(q, "SQLite error?", "INSERT INTO parents VALUES ( 'C', 'c', 'D', 'd' ) ;", "");
			
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
			
			String designSQL = "Space-Empire-Pulsar-0.2.sqlite";
			System.out.println();
			System.out.println(designSQL+" IMPORT...");
			assertFileResult(q, designSQL, "SQLite error?", "");					
			
			// INSERT Area
			assertQueryResult(q, "Error expected", "INSERT INTO Area VALUES(0, 0, 0, -1);", "[constraint failed]");
			assertQueryResult(q, "SQLite error?", "INSERT INTO Area VALUES(0, 0, 0, 0);", "");
			
			// INSERT Player
			assertQueryResult(q, "SQLite error?", "INSERT INTO Player VALUES('player1');", "");
			assertQueryResult(q, "SQLite error expected", "INSERT INTO Unit VALUES('player1', 'unit', 0, 0, 0, 0, 0, 0);", "[constraint failed]");
			assertQueryResult(q, "SQLite error expected", "INSERT INTO Unit VALUES('player1', 'unit', 0, 1, 0, 0, 1, 0);", "[constraint failed]");
			assertQueryResult(q, "SQLite error?", "INSERT INTO Unit VALUES('player1', 'unit', 0, 0, 0, 0, 1, 0);", "");
			
			// INSERT CelestialBody, ProductiveCelestialBody 
			assertQueryResult(q, "SQLite error?", "INSERT INTO CelestialBody VALUES('planeteA', 0, 0, 0, 0, 1);", "");
			assertQueryResult(q, "SQLite error?", "INSERT INTO ProductiveCelestialBody VALUES('planeteA', 10000, 4, 1, 0, 0);", "");

			// INSERT VersionedProductiveCelestialBody
			assertQueryResult(q, "SQLite error?", "INSERT INTO VersionedProductiveCelestialBody VALUES('planeteA', 0, 10000, 1000, 'player1', 1, 0, 0);", "");

			// INSERT Building
			assertQueryResult(q, "SQLite error?", "INSERT INTO Building VALUES('error', 1, 'planeteA', 0);", "[constraint failed]");
			assertQueryResult(q, "SQLite error?", "INSERT INTO Building VALUES('StarshipPlant', 1, 'planeteA', 0);", "");
			
			
		}
		catch(SQLiteException e)
		{
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}
