package org.axan.sep.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.axan.eplib.clientserver.rpc.RpcException;
import org.axan.eplib.gameserver.common.IServerUser.ServerPrivilegeException;
import org.axan.sep.client.SEPClient;
import org.axan.sep.client.SEPClient.IUserInterface;
import org.axan.sep.common.GameConfig;
import org.axan.sep.common.Player;
import org.axan.sep.common.PlayerGameBoard;
import org.axan.sep.common.Protocol.ServerGameCreation;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;




public class TestSEP
{

	private BufferedReader	serverOut;

	private BufferedReader	clientOut;

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
		PipedInputStream serverPipeOut = new PipedInputStream();
		serverOut = new BufferedReader(new InputStreamReader(serverPipeOut));
		SEPServer.log = org.axan.eplib.utils.Test.getTestLogger(new PipedOutputStream(serverPipeOut));

		PipedInputStream clientPipeOut = new PipedInputStream();
		clientOut = new BufferedReader(new InputStreamReader(clientPipeOut));
		SEPClient.log = org.axan.eplib.utils.Test.getTestLogger(new PipedOutputStream(clientPipeOut));
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception
	{
	}
	
	static class TestClientUserInterface implements IUserInterface
	{
		private final SEPClient client;
		private final Logger log = SEPClient.log;
		
		public TestClientUserInterface(String login, String pwd, String server, int port, int timeOut)
		{
			this.client = new SEPClient(this, login, pwd, server, port, timeOut);
		}
		
		public TestClientUserInterface(String login, String server, int port, int timeOut)
		{
			this.client = new SEPClient(this, login, server, port, timeOut);
		}
		
		public SEPClient getClient()
		{
			return client;
		}

		@Override
		public void displayGameCreationPanel()
		{
			log.log(Level.INFO, client.getLogin()+ ".displayGameCreationPanel");			
		}

		@Override
		public void onGamePaused()
		{
			log.log(Level.INFO, client.getLogin()+ ".onGamePaused");
		}

		@Override
		public void onGameRan()
		{
			log.log(Level.INFO, client.getLogin()+ ".onGameRan");
		}

		@Override
		public void onGameResumed()
		{
			log.log(Level.INFO, client.getLogin()+ ".onGameResumed");
		}

		@Override
		public void receiveGameCreationMessage(Player fromPlayer, String msg)
		{
			log.log(Level.INFO, client.getLogin()+ ".receiveGameCreationMessage("+fromPlayer.getName()+", "+msg+")");
		}

		@Override
		public void receiveNewTurnGameBoard(PlayerGameBoard gameBoard)
		{
			log.log(Level.INFO, client.getLogin()+ ".receiveNewTurnGameBoard("+gameBoard.getDate()+")");
		}

		@Override
		public void receivePausedGameMessage(Player fromPlayer, String msg)
		{
			log.log(Level.INFO, client.getLogin()+ ".receivePausedGameMessage("+fromPlayer.getName()+", "+msg+")");
		}

		@Override
		public void receiveRunningGameMessage(Player fromPlayer, String msg)
		{
			log.log(Level.INFO, client.getLogin()+ ".receiveRunningGameMessage("+fromPlayer.getName()+", "+msg+")");
		}

		@Override
		public void refreshGameConfig(GameConfig gameCfg)
		{
			log.log(Level.INFO, client.getLogin()+ ".refreshGameConfig("+gameCfg.getDimZ()+")");
		}

		@Override
		public void refreshPlayerList(Set<Player> playerList)
		{
			log.log(Level.INFO, client.getLogin()+ ".refreshPlayerList("+playerList.toString()+")");
		}
		
	}
	
	/**
	 * Test for {@link SEPServer} and {@link SEPClient}
	 */
	@Test
	public void allCases()
	{
		final int PORT = 3131;
		final int TIMEOUT = 10000;
		final int CONNECTION_TIMEOUT = 10000;
		
		SEPServer server = new SEPServer(PORT, TIMEOUT);
		assertNotNull("Unexpected result", server);
		
		assertEquals("No connected client expected.", 0, server.getClientsNumber());
		
		assertTrue("Player list expected to be empty.", server.getPlayerList().isEmpty());
		
		final String SERVER_ADMIN_KEY = server.getServerAdminKey();
		
		try
		{
			assertEquals("Server address expected to be localhost.", InetAddress.getLocalHost(), server.getAddress());
		}
		catch(UnknownHostException e)
		{
			fail("Test code exception: "+e.getMessage());
			return;
		}
		
		server.start();
		
		checkNextTrace(serverOut, SEPServer.class, "start", "Server started");
		
		// Client 1
		
		SEPClient client1 = new TestClientUserInterface("client1", SERVER_ADMIN_KEY, "localhost", PORT, TIMEOUT).getClient();
		
		assertEquals("Unexpected client login", "client1", client1.getLogin());
		assertFalse("No connection expected", client1.isConnected());	
		
		client1.connect();
		
		for(long start = System.currentTimeMillis(); !client1.isConnected() && System.currentTimeMillis() - start < CONNECTION_TIMEOUT; );		
		assertTrue("Expected to be connected", client1.isConnected());
		
		checkNextTrace(clientOut, SEPClient.class, "connect", "Client 'client1' connected");
		checkNextTrace(clientOut, TestSEP.TestClientUserInterface.class, "refreshPlayerList", "client1.refreshPlayerList([client1])");
		checkNextTrace(clientOut, TestSEP.TestClientUserInterface.class, "displayGameCreationPanel", "client1.displayGameCreationPanel");
		
		ServerGameCreation client1GameCreation;
		try
		{
			client1GameCreation = client1.getGameCreationInterface();
		}
		catch(RpcException e)
		{
			fail("Unexpected exception thrown : " + e.getMessage());
			return;
		}
		
		// Client 2
		
		SEPClient client2 = new TestClientUserInterface("client2", "localhost", PORT, TIMEOUT).getClient();
		
		assertEquals("Unexpected client login", "client2", client2.getLogin());
		assertFalse("No connection expected", client2.isConnected());	
		
		client2.connect();
		for(long start = System.currentTimeMillis(); !client2.isConnected() && System.currentTimeMillis() - start < CONNECTION_TIMEOUT; );
		assertTrue("Expected to be connected", client2.isConnected());
		
		checkNextTrace(clientOut, SEPClient.class, "connect", "Client 'client2' connected");
		checkNextTraceIsOneOf(clientOut, TestSEP.TestClientUserInterface.class, "refreshPlayerList", new String[] {"client1.refreshPlayerList([client1, client2])", "client2.refreshPlayerList([client1, client2])"});
		checkNextTrace(clientOut, TestSEP.TestClientUserInterface.class, "displayGameCreationPanel", "client2.displayGameCreationPanel");
		
		ServerGameCreation client2GameCreation;
		GameConfig gameCfg;
		try
		{
			client2GameCreation = client2.getGameCreationInterface();
			
			//Thread.sleep(5000);
			gameCfg = client1GameCreation.getGameConfig();
		}
		catch(Exception e)
		{
			fail("Unexpected exception thrown : " + e.getMessage());
			e.printStackTrace();
			return;
		}
		
		assertTrue("Unexpected default value.", gameCfg.getDimZ() > 1);
		
		boolean expectedExceptionThrown = false; 
		
		gameCfg.setDimZ(3);
		
		try
		{
			client2GameCreation.updateGameConfig(gameCfg);
		}
		catch(ServerPrivilegeException e)
		{
			expectedExceptionThrown = true;
		}
		catch(Throwable t)
		{
			fail("Unexpected exception thrown : " + t.getMessage());
			t.printStackTrace();
			return;
		}
		assertTrue("Expected exception not thrown.", expectedExceptionThrown);
		
		try
		{
			gameCfg.setDimZ(2);
			client1GameCreation.updateGameConfig(gameCfg);
			gameCfg.setDimZ(1);
			client1GameCreation.updateGameConfig(gameCfg);
		}
		catch(Throwable t)
		{
			fail("Unexpected exception thrown : " + t.getMessage());
			t.printStackTrace();
			return;
		}
		
		checkNextTraceIsOneOf(clientOut, TestSEP.TestClientUserInterface.class, "refreshGameConfig", new String[] {"client1.refreshGameConfig(2)", "client2.refreshGameConfig(2)"});
		checkNextTraceIsOneOf(clientOut, TestSEP.TestClientUserInterface.class, "refreshGameConfig", new String[] {"client1.refreshGameConfig(1)", "client2.refreshGameConfig(1)"});
		
		checkNextTrace(serverOut, SEPServer.SEPGameCreation.class, "updateGameConfig", "client2 tried to update game config but is not admin.");
						
		//server.getAddress();
		//server.getClientsNumber();
		//server.getPlayerList();
		//server.getPlayerStateList();
		//server.getServerAdminKey();
		//server.start();
		//server.stop();
		//server.terminate();
		
		//client1.connect();
		//client1.disconnect();
		//client1.getGameCreationInterface();
		//client1.getPausedGameInterface();
		//client1.getRunningGameInterface();
		//client1.isConnected();
		//client1.pauseGame();
		//client1.resumeGame();
		//client1.runGame();
		
		//client1GameCreation.getGameConfig();
		//client1GameCreation.getPlayerList();
		//client1GameCreation.sendMessage(msg);
		//client1GameCreation.updateGameConfig(gameCfg);
		//client1GameCreation.updatePlayerConfig(playerCfg);

		server.stop();
		checkNextTrace(serverOut, SEPServer.class, "stop", "Stopping server");
		
		assertTrue("Unexpected remaining log", flush());
	}

	private boolean flush()
	{
		System.out.println();
		System.out.println("Clients OUT");
		String clientsLog = flushLog(clientOut);
		System.out.println(clientsLog);
		System.out.println("Server OUT");
		String serverLog = flushLog(serverOut);
		System.out.println(serverLog);
		return clientsLog.isEmpty() && serverLog.isEmpty();
	}
	
	private String flushLog(BufferedReader out)
	{
		StringBuffer sb = new StringBuffer();
		String line;
		try
		{
			while (out.ready())
			{
				line = out.readLine();
				if ( !line.isEmpty()) sb.append(line + "\n");
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
			fail("Test code unexpected exception thrown : "+e.getMessage());			
		}

		return sb.toString();
	}
	
	private boolean checkNextTraceIsOneOf(BufferedReader out, Class<?> clazz, String methodName, String[] possibleTraces)
	{				
		Method m = getMethod(clazz, methodName);
		
		Vector<String> possibles = new Vector<String>();
		possibles.addAll(Arrays.asList(possibleTraces));
		
		try
		{
			String line = null;

			do
			{
				long start = System.currentTimeMillis();
				while(!out.ready() && System.currentTimeMillis() - start < 5000)
				{
					Thread.sleep(200);
				}
				if ( !out.ready()) fail("No output");
				line = out.readLine();
			} while (line == null || line.isEmpty());

			String pattern = "^.* " + clazz.getCanonicalName() + " " + m.getName() + "$";	
			assertTrue("Unexpected trace header \"" + line + "\", expected \"... " + clazz.getCanonicalName() + " " + m.getName() + "\"", line.matches(pattern));
			System.out.println("[CHECKED] " + line);
			line = out.readLine();
			
			for(String p : possibles)
			{							
				String errMsg = "Unexpected output \"" + line + "\", expected \"... " + p + "\"";

				if (line.length() > p.length() && line.substring(line.length() - p.length()).compareTo(p) == 0)
				{
					System.out.println("[CHECKED] "+line);
					possibles.remove(p);
					if (possibles.size() == 0) return true;
					if (checkNextTraceIsOneOf(out, clazz, methodName, possibles.toArray(new String[possibles.size()]))) return true;
					break;
				}				
			}					
			
			fail("Unexpected output \""+line+"\", expected ["+possibles+"]");
			return false;
		}
		catch (IOException e)
		{
			fail("Test code unexpected exception");
			return false;
		}
		catch (InterruptedException e)
		{
			fail("No output");
			return false;
		}
	}
	
	private void checkNextTrace(BufferedReader out, Class<?> clazz, String methodName, String expectedTrace)
	{
		Method m = getMethod(clazz, methodName);

		try
		{
			String line = null;

			do
			{
				while(!out.ready()) {Thread.sleep(200);}
				//if ( !out.ready()) fail("No output");
				line = out.readLine();
			} while (line == null || line.isEmpty());

			String pattern = "^.* " + clazz.getCanonicalName() + " " + m.getName() + "$";
			assertTrue("Unexpected trace header \"" + line + "\", expected \"... " + clazz.getCanonicalName() + " " + m.getName() + "\"", line.matches(pattern));
			System.out.println("[CHECKED] " + line);

			line = out.readLine();
			String errMsg = "Unexpected output \"" + line + "\", expected \"... " + expectedTrace + "\"";

			assertTrue(errMsg, line.length() > expectedTrace.length());
			assertTrue(errMsg, line.substring(line.length() - expectedTrace.length()).compareTo(expectedTrace) == 0);
			System.out.println("[CHECKED] " + line);
		}
		catch (IOException e)
		{
			fail("Test code unexpected exception");
			return;
		}
		catch (InterruptedException e)
		{
			fail("No output");
			return;
		}
	}

	private Method getMethod(Class<?> clazz, String name)
	{
		Method[] methods = clazz.getMethods();
		for (Method e : methods)
		{
			if (e.getName().compareTo(name) == 0)
			{
				return e;
			}
		}

		fail("Test code error");
		return null;
	}
}
