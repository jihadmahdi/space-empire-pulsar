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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.axan.eplib.clientserver.rpc.RpcException;
import org.axan.eplib.gameserver.common.IServerUser.ServerPrivilegeException;
import org.axan.sep.client.SEPClient;
import org.axan.sep.client.SEPClient.IUserInterface;
import org.axan.sep.common.ExtractionModule;
import org.axan.sep.common.GameConfig;
import org.axan.sep.common.Player;
import org.axan.sep.common.PlayerGameBoard;
import org.axan.sep.common.SEPUtils;
import org.axan.sep.common.StarshipPlant;
import org.axan.sep.common.StarshipTemplate;
import org.axan.sep.common.Diplomacy.PlayerPolicies;
import org.axan.sep.common.Diplomacy.PlayerPolicies.eForeignPolicy;
import org.axan.sep.common.Fleet.Move;
import org.axan.sep.common.Protocol.ServerGameCreation;
import org.axan.sep.common.Protocol.ServerRunningGame;
import org.axan.sep.common.Protocol.ServerRunningGame.RunningGameCommandException;
import org.axan.sep.server.ai.AITest;
import org.axan.sep.server.model.GameBoard;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;




public class TestSEP
{

	private BufferedReader	serverOut;

	private BufferedReader	clientOut;
	
	private static ExecutorService threadPool = Executors.newCachedThreadPool();

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
		if (threadPool != null) threadPool.shutdown();
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
		private final AITest ai;
		private final SEPClient client;
		private final Logger log = SEPClient.log;
		
		public TestClientUserInterface(String login, String pwd, String server, int port, int timeOut)
		{
			this.client = new SEPClient(this, login, pwd, server, port, timeOut);
			this.ai = new AITest(login);
		}
		
		public TestClientUserInterface(String login, String server, int port, int timeOut)
		{
			this.client = new SEPClient(this, login, server, port, timeOut);
			this.ai = new AITest(login);
		}
		
		public SEPClient getClient()
		{
			return client;
		}
		
		public AITest getAITest()
		{
			return ai;
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
			threadPool.execute(new Runnable()
			{
				
				@Override
				public void run()
				{
					try
					{						
						ai.refreshGameBoard(client.getRunningGameInterface().getPlayerGameBoard());
					}
					catch(Throwable t)
					{
						t.printStackTrace();
						fail("Unexpected throwable thrown : "+t.getMessage());
						return;
					}
				}
			});
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
			ai.refreshGameBoard(gameBoard);
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
		
		// server.getClientsNumber()
		assertEquals("No connected client expected.", 0, server.getClientsNumber());
		
		// server.getPlayerList()
		assertTrue("Player list expected to be empty.", server.getPlayerList().isEmpty());
		
		// server.getServerAdminKey()
		final String SERVER_ADMIN_KEY = server.getServerAdminKey();
		
		try
		{
			// server.getAddress()
			assertEquals("Server address expected to be localhost.", InetAddress.getLocalHost(), server.getAddress());
		}
		catch(UnknownHostException e)
		{
			fail("Test code exception: "+e.getMessage());
			return;
		}
		
		// server.start
		server.start();
		
		checkNextTrace(serverOut, SEPServer.class, "start", "Server started");
		
		// Client 1
		
		TestClientUserInterface ui1 = new TestClientUserInterface("client1", SERVER_ADMIN_KEY, "localhost", PORT, TIMEOUT);
		SEPClient client1 = ui1.getClient();
		
		assertEquals("Unexpected client login", "client1", client1.getLogin());
		assertFalse("No connection expected", client1.isConnected());	
		
		// client.connect()
		client1.connect();
		
		// client.isConnected()
		for(long start = System.currentTimeMillis(); !client1.isConnected() && System.currentTimeMillis() - start < CONNECTION_TIMEOUT; );		
		assertTrue("Expected to be connected", client1.isConnected());
		
		checkNextTrace(clientOut, SEPClient.class, "connect", "Client 'client1' connected");
		checkNextTrace(clientOut, TestSEP.TestClientUserInterface.class, "refreshPlayerList", "client1.refreshPlayerList([client1])");
		checkNextTrace(clientOut, TestSEP.TestClientUserInterface.class, "displayGameCreationPanel", "client1.displayGameCreationPanel");
		
		// client.getGameCreationInterface()
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
		
		TestClientUserInterface ui2 = new TestClientUserInterface("client2", "localhost", PORT, TIMEOUT);
		SEPClient client2 = ui2.getClient();
		
		assertEquals("Unexpected client login", "client2", client2.getLogin());
		assertFalse("No connection expected", client2.isConnected());	
		
		client2.connect();
		for(long start = System.currentTimeMillis(); !client2.isConnected() && System.currentTimeMillis() - start < CONNECTION_TIMEOUT; );
		assertTrue("Expected to be connected", client2.isConnected());
		
		checkNextTrace(clientOut, SEPClient.class, "connect", "Client 'client2' connected");
		checkNextTraceIsOneOf(clientOut, TestSEP.TestClientUserInterface.class, "refreshPlayerList", new String[] {"client1.refreshPlayerList([client1, client2])", "client2.refreshPlayerList([client1, client2])"});
		checkNextTrace(clientOut, TestSEP.TestClientUserInterface.class, "displayGameCreationPanel", "client2.displayGameCreationPanel");
		
		// client1GameCreation.getGameConfig()
		ServerGameCreation client2GameCreation;
		GameConfig gameCfg;
		try
		{
			client2GameCreation = client2.getGameCreationInterface();
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
		
		// client1GameCreation.updateGameConfig(gameCfg)
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

		// Client 3
		
		TestClientUserInterface ui3 = new TestClientUserInterface("client3", "localhost", PORT, TIMEOUT);
		SEPClient client3 = ui3.getClient();
		
		assertEquals("Unexpected client login", "client3", client3.getLogin());
		assertFalse("No connection expected", client3.isConnected());	
		
		client3.connect();
		for(long start = System.currentTimeMillis(); !client3.isConnected() && System.currentTimeMillis() - start < CONNECTION_TIMEOUT; );
		assertTrue("Expected to be connected", client3.isConnected());
		
		checkNextTrace(clientOut, SEPClient.class, "connect", "Client 'client3' connected");
		checkNextTraceIsOneOf(clientOut, TestSEP.TestClientUserInterface.class, "refreshPlayerList", new String[] {"client1.refreshPlayerList([client1, client2, client3])", "client2.refreshPlayerList([client1, client2, client3])", "client3.refreshPlayerList([client1, client2, client3])"});
		checkNextTrace(clientOut, TestSEP.TestClientUserInterface.class, "displayGameCreationPanel", "client3.displayGameCreationPanel");
		
		// client1GameCreation.getGameConfig()
		ServerGameCreation client3GameCreation;
		try
		{
			client3GameCreation = client3.getGameCreationInterface();
		}
		catch(Exception e)
		{
			fail("Unexpected exception thrown : " + e.getMessage());
			e.printStackTrace();
			return;
		}
		
		AITest client1AITest = ui1.getAITest();
		AITest client2AITest = ui2.getAITest();
		AITest client3AITest = ui3.getAITest();
		
		assertEquals("Unexpected result", -1, client1AITest.getTurn());
		
		// client.runGame()
		try
		{
			client1.runGame();
			while(client1AITest.getTurn() < 0) {Thread.sleep(200);}
		}
		catch(Throwable t)
		{
			fail("Unexpected exception thrown : " + t.getMessage());
			t.printStackTrace();
			return;
		}
		
		assertEquals("Unexpected result", 0, client1AITest.getTurn());
		
		checkNextTraceIsOneOf(clientOut, TestSEP.TestClientUserInterface.class, "onGameRan", new String[] {"client1.onGameRan", "client2.onGameRan", "client3.onGameRan"});
		checkNextTrace(serverOut, SEPServer.SEPGameServerListener.class, "gameRan", "gameRan");		
		
		// client.getRunningGameInterface()
		ServerRunningGame client1RunningGame;
		ServerRunningGame client2RunningGame;
		ServerRunningGame client3RunningGame;
		try
		{
			client1RunningGame = client1.getRunningGameInterface();
			client2RunningGame = client2.getRunningGameInterface();
			client3RunningGame = client3.getRunningGameInterface();
		}
		catch(Throwable t)
		{
			fail("Unexpected exception thrown : " + t.getMessage());
			return;
		}
		
		String startingPlanet1 = client1AITest.getStartingPlanetName();
		assertEquals("A", startingPlanet1);
		
		String startingPlanet2 = client2AITest.getStartingPlanetName();
		assertEquals("B", startingPlanet2);
		
		String startingPlanet3 = client3AITest.getStartingPlanetName();
		assertEquals("C", startingPlanet3);
		
		checkNextTraceIsOneOf(serverOut, GameBoard.class, "getPlayerGameBoard", new String[] {"getGameBoard(client1)", "getGameBoard(client2)", "getGameBoard(client3)"});
		
		assertTrue("Unexpected remaining log", flush());
		
		// T0, client1 build a starship plant on its home planet, client2 too.
		
		client1AITest.checkBuilding(startingPlanet1, StarshipPlant.class, 0);
		client2AITest.checkBuilding(startingPlanet2, StarshipPlant.class, 0);
		client3AITest.checkBuilding(startingPlanet3, ExtractionModule.class, 0);
		
		try
		{
			client1RunningGame.build(startingPlanet1, StarshipPlant.class);
			client2RunningGame.build(startingPlanet2, StarshipPlant.class);
			client3RunningGame.build(startingPlanet3, ExtractionModule.class);
			
			expectedExceptionThrown = false;
			
			try
			{
				client1RunningGame.build(startingPlanet1, ExtractionModule.class);
			}
			catch(RunningGameCommandException e)
			{
				expectedExceptionThrown = true;
			}
			if (!expectedExceptionThrown) fail("Expected exception not thrown.");
			
			client1RunningGame.endTurn();
			client2RunningGame.endTurn();
			client3RunningGame.endTurn();
			
			while(client1AITest.getTurn() < 1 || client2AITest.getTurn() < 1 || client3AITest.getTurn() < 1) {Thread.sleep(200);}
		}
		catch(Throwable t)
		{
			fail("Unexpected exception thrown : " + t.getMessage());
			return;
		}

		assertEquals("Unexpected result", 1, client1AITest.getTurn());
		
		checkNextTrace(serverOut, SEPServer.class, "checkForNextTurn", "Resolving new turn");
		checkNextTraceIsOneOf(clientOut, TestSEP.TestClientUserInterface.class, "receiveNewTurnGameBoard", new String[] {"client1.receiveNewTurnGameBoard(1)", "client2.receiveNewTurnGameBoard(1)", "client3.receiveNewTurnGameBoard(1)"});
		checkNextTraceIsOneOf(serverOut, GameBoard.class, "getPlayerGameBoard", new String[] {"getGameBoard(client1)", "getGameBoard(client2)", "getGameBoard(client3)"});
		
		client1AITest.checkBuilding(startingPlanet1, StarshipPlant.class, 1);
		client2AITest.checkBuilding(startingPlanet2, StarshipPlant.class, 1);
		client3AITest.checkBuilding(startingPlanet3, ExtractionModule.class, 1);
		
		assertTrue("Unexpected remaining log", flush());
		
		// T1, client1 & client2 make starships, form fleets and change diplomacy, client3 demolish ExtractionModule, build a StarshipPlant and change diplomacy.
		
		String t1Fleets = "fleet1";		
		
		Map<String, PlayerPolicies> client1diplomacy = new HashMap<String, PlayerPolicies>();
		Map<String, PlayerPolicies> client2diplomacy = new HashMap<String, PlayerPolicies>();
		Map<String, PlayerPolicies> client3diplomacy = new HashMap<String, PlayerPolicies>();
		
		try
		{
			Map<StarshipTemplate, Integer> starshipsToMake = new HashMap<StarshipTemplate, Integer>();
			StarshipTemplate[] starshipTemplates = SEPUtils.starshipTypes.toArray(new StarshipTemplate[SEPUtils.starshipTypes.size()]);
			
			starshipsToMake.put(starshipTemplates[0], 10);			
			client1RunningGame.makeStarships(startingPlanet1, starshipsToMake);
			

			expectedExceptionThrown = false;			
			try
			{
				starshipsToMake.put(starshipTemplates[0], 999999);
				client1RunningGame.makeStarships(startingPlanet1, starshipsToMake);
			}
			catch(RunningGameCommandException e)
			{
				expectedExceptionThrown = true;
			}
			if (!expectedExceptionThrown) fail("Expected exception not thrown.");
			
			starshipsToMake.put(starshipTemplates[0], 5);
			client1RunningGame.formFleet(startingPlanet1, t1Fleets, starshipsToMake, null);
			
			starshipsToMake.put(starshipTemplates[0], 3);
			client1RunningGame.formFleet(startingPlanet1, t1Fleets+"bis", starshipsToMake, null);
			
			expectedExceptionThrown = false;			
			try
			{
				starshipsToMake.put(starshipTemplates[0], 3);
				client1RunningGame.formFleet(startingPlanet1, "incorrectFleet", starshipsToMake, null);
			}
			catch(RunningGameCommandException e)
			{
				expectedExceptionThrown = true;
			}
			if (!expectedExceptionThrown) fail("Expected exception not thrown.");
			
			starshipsToMake.remove(starshipTemplates[0]);
			starshipsToMake.put(starshipTemplates[3], 11);
			client2RunningGame.makeStarships(startingPlanet2, starshipsToMake);
			starshipsToMake.put(starshipTemplates[3], 4);
			client2RunningGame.formFleet(startingPlanet2, t1Fleets, starshipsToMake, null);
			
			client3RunningGame.demolish(startingPlanet3, ExtractionModule.class);
			client3RunningGame.build(startingPlanet3, StarshipPlant.class);
			
			PlayerPolicies client31policies = new PlayerPolicies(client1.getLogin(), true, eForeignPolicy.HOSTILE);
			PlayerPolicies client32policies = new PlayerPolicies(client2.getLogin(), true, eForeignPolicy.HOSTILE);			
			client3diplomacy.put(client1.getLogin(), client31policies);
			client3diplomacy.put(client2.getLogin(), client32policies);			
			client3RunningGame.changeDiplomacy(client3diplomacy);
			
			expectedExceptionThrown = false;			
			try
			{
				PlayerPolicies client33policies = new PlayerPolicies(client3.getLogin(), true, eForeignPolicy.HOSTILE);
				client3diplomacy.put(client3.getLogin(), client33policies);				
				client3RunningGame.changeDiplomacy(client3diplomacy);
			}
			catch(RunningGameCommandException e)
			{
				expectedExceptionThrown = true;
			}
			if (!expectedExceptionThrown) fail("Expected exception not thrown.");
			
			PlayerPolicies client12policies = new PlayerPolicies(client2.getLogin(), false, eForeignPolicy.HOSTILE_IF_OWNER);
			PlayerPolicies client13policies = new PlayerPolicies(client3.getLogin(), false, eForeignPolicy.HOSTILE_IF_OWNER);			
			client1diplomacy.put(client2.getLogin(), client12policies);
			client1diplomacy.put(client3.getLogin(), client13policies);			
			client1RunningGame.changeDiplomacy(client1diplomacy);
			
			PlayerPolicies client21policies = new PlayerPolicies(client1.getLogin(), false, eForeignPolicy.NEUTRAL);
			PlayerPolicies client23policies = new PlayerPolicies(client3.getLogin(), false, eForeignPolicy.HOSTILE);
			client2diplomacy.put(client1.getLogin(), client21policies);
			client2diplomacy.put(client3.getLogin(), client23policies);			
			client2RunningGame.changeDiplomacy(client2diplomacy);
			
			Move client1move = new Move(startingPlanet3, 0, false);
			Move client2move = new Move(startingPlanet3, 1, false);
			Stack<Move> checkpoints = new Stack<Move>();
			checkpoints.add(client1move);
			client1RunningGame.moveFleet(t1Fleets, checkpoints);
			
			checkpoints.clear();
			checkpoints.add(client2move);
			client2RunningGame.moveFleet(t1Fleets, checkpoints);
			
			client1RunningGame.endTurn();
			client2RunningGame.endTurn();
			client3RunningGame.endTurn();
			
			while(client1AITest.getTurn() < 1 || client2AITest.getTurn() < 1 || client3AITest.getTurn() < 1) {Thread.sleep(200);}
		}
		catch(Throwable t)
		{
			t.printStackTrace();
			fail("Unexpected exception thrown : " + t.getMessage());
			return;
		}

		assertEquals("Unexpected result", 1, client1AITest.getTurn());
		
		checkNextTrace(serverOut, SEPServer.class, "checkForNextTurn", "Resolving new turn");
		checkNextTraceIsOneOf(clientOut, TestSEP.TestClientUserInterface.class, "receiveNewTurnGameBoard", new String[] {"client1.receiveNewTurnGameBoard(2)", "client2.receiveNewTurnGameBoard(2)", "client3.receiveNewTurnGameBoard(2)"});
		checkNextTraceIsOneOf(serverOut, GameBoard.class, "getPlayerGameBoard", new String[] {"getGameBoard(client1)", "getGameBoard(client2)", "getGameBoard(client3)"});
		
		client1AITest.checkBuilding(startingPlanet1, StarshipPlant.class, 1);
		client2AITest.checkBuilding(startingPlanet2, StarshipPlant.class, 1);
		client3AITest.checkBuilding(startingPlanet3, ExtractionModule.class, 0);
		client3AITest.checkBuilding(startingPlanet3, StarshipPlant.class, 1);
		
		client1AITest.checkFleet(t1Fleets, 5, client1AITest.getDistance(startingPlanet1, startingPlanet3) > client1AITest.getTurn() - 1);
		client1AITest.checkFleet(t1Fleets+"bis", 3, false);
		client2AITest.checkFleet(t1Fleets, 4, false);
		
		client1AITest.checkDiplomacy(client1diplomacy);
		client2AITest.checkDiplomacy(client2diplomacy);
		client3diplomacy.remove(client3.getLogin());
		client3AITest.checkDiplomacy(client3diplomacy);
		
		assertTrue("Unexpected remaining log", flush());
		
		// T2, client1 dismantle formed fleet on starting planet, client3 build starships and form a fleet. 
		
		String t2Fleets = "fleet2";		
				
		try
		{
			client1RunningGame.dismantleFleet(t1Fleets+"bis");
			
			Map<StarshipTemplate, Integer> starshipsToMake = new HashMap<StarshipTemplate, Integer>();
			StarshipTemplate[] starshipTemplates = SEPUtils.starshipTypes.toArray(new StarshipTemplate[SEPUtils.starshipTypes.size()]);
			
			starshipsToMake.put(starshipTemplates[6], 11);			
			client3RunningGame.makeStarships(startingPlanet3, starshipsToMake);
						
			starshipsToMake.put(starshipTemplates[6], 5);
			client3RunningGame.formFleet(startingPlanet3, t2Fleets, starshipsToMake, null);
			
			client1RunningGame.endTurn();
			client2RunningGame.endTurn();
			client3RunningGame.endTurn();
									
			while(client1AITest.getTurn() < 2 || client2AITest.getTurn() < 2 || client3AITest.getTurn() < 2) {Thread.sleep(200);}
		}
		catch(Throwable t)
		{
			t.printStackTrace();
			fail("Unexpected exception thrown : " + t.getMessage());
			return;
		}

		assertEquals("Unexpected result", 2, client1AITest.getTurn());
		
		checkNextTrace(serverOut, SEPServer.class, "checkForNextTurn", "Resolving new turn");
		checkNextTraceIsOneOf(clientOut, TestSEP.TestClientUserInterface.class, "receiveNewTurnGameBoard", new String[] {"client1.receiveNewTurnGameBoard(3)", "client2.receiveNewTurnGameBoard(3)", "client3.receiveNewTurnGameBoard(3)"});
		checkNextTraceIsOneOf(serverOut, GameBoard.class, "getPlayerGameBoard", new String[] {"getGameBoard(client1)", "getGameBoard(client2)", "getGameBoard(client3)"});
		
		client1AITest.checkBuilding(startingPlanet1, StarshipPlant.class, 1);
		client2AITest.checkBuilding(startingPlanet2, StarshipPlant.class, 1);
		client3AITest.checkBuilding(startingPlanet3, ExtractionModule.class, 0);
		client3AITest.checkBuilding(startingPlanet3, StarshipPlant.class, 1);
		
		client1AITest.checkFleet(t1Fleets, 5, client1AITest.getDistance(startingPlanet1, startingPlanet3) > client1AITest.getTurn() - 1);
		
		expectedExceptionThrown = false;
		try
		{
			client1AITest.checkFleet(t1Fleets+"bis", 3, false);
		}
		catch(AssertionError e)
		{
			assertEquals(e.getMessage(), "Cannot find fleet 'client1@"+t1Fleets+"bis'.");
			expectedExceptionThrown = true;
		}
		assertTrue("Expected exception not thrown", expectedExceptionThrown);
		
		client2AITest.checkFleet(t1Fleets, 4, client2AITest.getDistance(startingPlanet2, startingPlanet3) > client1AITest.getTurn());
		client3AITest.checkFleet(t2Fleets, 5, false);		
		
		assertTrue("Unexpected remaining log", flush());
		
		/*=======================================================
		
		X	server.getAddress();
		X	server.getClientsNumber();
		X	server.getPlayerList();
			server.getPlayerStateList();
		X	server.getServerAdminKey();
		X	server.start();
		X	server.stop();
			server.terminate();
		X	client1.connect();
			client1.disconnect();
		X	client1.getGameCreationInterface();
			client1.getPausedGameInterface();
		X	client1.getRunningGameInterface();
		X	client1.isConnected();
		X		true
		X		false
			client1.pauseGame();
			client1.resumeGame();
		X	client1.runGame();
		
		X	client1GameCreation.getGameConfig(); client1RunningGame.getGameConfig();
		X		updated
			client1GameCreation.getPlayerList(); client1RunningGame.getPlayerList();
			client1GameCreation.sendMessage(msg);
		X	client1GameCreation.updateGameConfig(gameCfg);
			client1GameCreation.updatePlayerConfig(playerCfg);
		
		
		V	client1RunningGame.build(ceslestialBodyName, buildingType);
		X		ok
				nok
		V	client1RunningGame.demolish(ceslestialBodyName, buildingType);
		X		ok
				nok
		V	client1RunningGame.changeDiplomacy(newPolicies);
		X		ok
		X		nok (invalid newPolicies) 
				nok (government lost control)
		V	client1RunningGame.dismantleFleet(fleetName);
		X		ok
			
			client1RunningGame.attackEnemiesFleet(celestialBodyName);
			client1RunningGame.buildSpaceRoad(sourceName, destinationName);
			client1RunningGame.demolishSpaceRoad(celestialBodyNameA, celestialBodyNameB);
			client1RunningGame.embarkGovernment();
			client1RunningGame.fireAntiProbeMissile(antiProbeMissileName, targetOwnerName, targetProbeName);
			client1RunningGame.firePulsarMissile(celestialBodyName, bonusModifier);
		V	client1RunningGame.formFleet(planetName, fleetName, fleetToFormStarships, fleetToFormSpeciaUnits);
		X		ok
		X		nok (not enough starships)		
			client1RunningGame.launchProbe(probeName, destination);
			client1RunningGame.makeAntiProbeMissiles(planetName, antiProbeMissileName, quantity);
			client1RunningGame.makeProbes(planetName, probeName, quantity);
		V	client1RunningGame.makeStarships(planetName, starshipsToMake);
		X		ok
		X		nok (not enough carbon to pay)
			client1RunningGame.modifyCarbonOrder(originCelestialBodyName, nextCarbonOrders);
		V	client1RunningGame.moveFleet(fleetName, checkpoints);
		X		go
				attack
			client1RunningGame.settleGovernment(planetName);
						
			client1RunningGame.getPlayerGameBoard();			
			client1RunningGame.sendMessage(msg);
			client1RunningGame.resetTurn();
		V	client1RunningGame.endTurn();
		X		ok
				nok?
		
		=======================================================*/	
		
		// server.stop()
		server.stop();
		checkNextTrace(serverOut, SEPServer.class, "stop", "Stopping server");
		
		assertTrue("Unexpected remaining log", flush());
	}

	private boolean flush()
	{		
		String clientsLog = flushLog(clientOut);
		if (!clientsLog.isEmpty())
		{
			System.out.println();
			System.out.println("Clients OUT");
			System.out.println(clientsLog);
		}
		
		String serverLog = flushLog(serverOut);
		if (!serverLog.isEmpty())
		{
			System.out.println("Server OUT");			
			System.out.println(serverLog);
		}
		
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

			String pattern = "^.* " + clazz.getCanonicalName() + "[$[^ ]+]* " + m.getName() + "$";
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
		Method[] methods = clazz.getDeclaredMethods();
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
