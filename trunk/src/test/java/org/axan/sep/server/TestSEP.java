package org.axan.sep.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.FormatFlagsConversionMismatchException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.axan.eplib.clientserver.rpc.RpcException;
import org.axan.eplib.gameserver.common.IServerUser.ServerPrivilegeException;
import org.axan.sep.client.SEPClient;
import org.axan.sep.client.SEPClient.IUserInterface;
import org.axan.sep.common.db.Commands.Build;
import org.axan.sep.common.db.Commands.MakeStarships;
import org.axan.sep.common.db.Commands.AssignStarships;
import org.axan.sep.common.db.ICommand.GameCommandException;
import org.axan.sep.common.GameConfig;
import org.axan.sep.common.GameConfigCopier;
import org.axan.sep.common.Rules;
import org.axan.sep.common.SEPUtils;
import org.axan.sep.common.IGameBoard.GameBoardException;
import org.axan.sep.common.PlayerGameBoard;
import org.axan.sep.common.Protocol.ServerGameCreation;
import org.axan.sep.common.Protocol.ServerRunningGame;
import org.axan.sep.common.Protocol.eBuildingType;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.Rules.StarshipTemplate;
import org.axan.sep.common.db.ICommand;
import org.axan.sep.common.db.IGameConfig;
import org.axan.sep.common.db.IGameEvent;
import org.axan.sep.common.db.IPlayerConfig;
import org.axan.sep.server.ai.ClientAI;
import org.axan.sep.server.ai.UncheckedLocalGame;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import scala.xml.dtd.MakeValidationException;

public class TestSEP
{

	static private BufferedReader			serverOut;

	static private BufferedReader			clientOut;

	private static ExecutorService	threadPool	= Executors.newCachedThreadPool();

	private static enum eTest {Server, Clients};
	static private org.axan.eplib.utils.Test<eTest> tester;
	
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
		tester = org.axan.eplib.utils.Test.getTester(5000, 1048576, eTest.Server, eTest.Clients);
		SEPServer.log = tester.getLogger(eTest.Server);
		SEPClient.log = tester.getLogger(eTest.Clients);
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
		private final boolean isAdmin;
		private final ClientAI	ai;
		//private final SEPClient	client;
		private final Logger	log	= SEPClient.log;

		/**
		 * Create client with server admin password. Client is admin.
		 * @param isClientTest
		 * @param login
		 * @param pwd
		 * @param server
		 * @param port
		 * @param timeOut
		 */
		public TestClientUserInterface(boolean isClientTest, String login, String pwd, String server, int port, int timeOut)
		{
			SEPClient client = new SEPClient(this, login, pwd, server, port, timeOut);
			this.ai = new ClientAI(login, client, isClientTest);
			this.isAdmin = true;
		}

		public TestClientUserInterface(boolean isClientTest, String login, String server, int port, int timeOut)
		{
			SEPClient client = new SEPClient(this, login, server, port, timeOut);
			this.ai = new ClientAI(login, client, isClientTest);
			this.isAdmin = false;
		}

		public SEPClient getClient()
		{
			return ai.getClient();
		}

		public ClientAI getAITest()
		{
			return ai;
		}

		@Override
		public boolean isAdmin()
		{
			return isAdmin;
		}
		
		@Override
		public void displayGameCreationPanel()
		{
			log.log(Level.INFO, getClient().getLogin() + ".displayGameCreationPanel");
		}

		@Override
		public void onGamePaused()
		{
			log.log(Level.INFO, getClient().getLogin() + ".onGamePaused");
		}

		@Override
		public void onGameRan()
		{
			log.log(Level.INFO, getClient().getLogin() + ".onGameRan");
			/*
			threadPool.execute(new Runnable()
			{

				@Override
				public void run()
				{
					try
					{
						ai.refreshGameBoard(getClient().getRunningGameInterface().getPlayerGameBoard());
					}
					catch(Throwable t)
					{
						t.printStackTrace();
						fail("Unexpected throwable thrown : " + t.getMessage());
						return;
					}
				}
			});
			*/
		}

		@Override
		public void onGameResumed()
		{
			log.log(Level.INFO, getClient().getLogin() + ".onGameResumed");
			/*
			threadPool.execute(new Runnable()
			{

				@Override
				public void run()
				{
					try
					{
						ai.refreshGameBoard(getClient().getRunningGameInterface().getPlayerGameBoard());
					}
					catch(Throwable t)
					{
						t.printStackTrace();
						fail("Unexpected throwable thrown : " + t.getMessage());
						return;
					}
				}
			});
			*/
		}

		@Override
		public void receiveGameCreationMessage(String fromPlayer, String msg)
		{
			log.log(Level.INFO, getClient().getLogin() + ".receiveGameCreationMessage(" + fromPlayer + ", " + msg + ")");
		}

		@Override
		public void receiveNewTurnGameBoard(List<IGameEvent> newTurnEvents)
		{			
			log.log(Level.INFO, getClient().getLogin() + ".receiveNewTurnGameBoard(" + getClient().getGameboard().getConfig().getTurn() + ")");
			//ai.refreshGameBoard(gameBoard);
		}

		@Override
		public void receivePausedGameMessage(String fromPlayer, String msg)
		{
			log.log(Level.INFO, getClient().getLogin() + ".receivePausedGameMessage(" + fromPlayer + ", " + msg + ")");
		}

		@Override
		public void receiveRunningGameMessage(String fromPlayer, String msg)
		{
			log.log(Level.INFO, getClient().getLogin() + ".receiveRunningGameMessage(" + fromPlayer + ", " + msg + ")");
		}

		@Override
		public void refreshGameConfig(GameConfig gameCfg)
		{
			log.log(Level.INFO, getClient().getLogin() + ".refreshGameConfig(" + gameCfg.getDimZ() + ")");
		}

		@Override
		public void refreshPlayerList(Map<String, IPlayerConfig> playerList)
		{
			log.log(Level.INFO, getClient().getLogin() + ".refreshPlayerList(" + playerList.keySet().toString() + ")");
		}

	}
	
	private static void checkErronedCommand(ICommand erronedCommand, ClientAI ai)
	{
		boolean exceptionThrown = false;
		try
		{
			ai.getGameBoard().onLocalCommand(erronedCommand);
		}
		catch(GameBoardException e)
		{
			Throwable t = e;
			while(t.getCause() != null && t.getCause() != t) t = t.getCause();
			
			if (!GameCommandException.class.isInstance(t))
			{
				t.printStackTrace();
				fail("GameCommandException expected, but '"+t.getClass().getSimpleName()+"' thrown.");
			}
			
			exceptionThrown = true;
		}
		
		assertTrue("Expected exception not thrown.", exceptionThrown);		
	}
	
	private static void waitForNextTurn(long timeOut, int turn, ClientAI ... ias) throws TimeoutException, InterruptedException
	{	
		
		boolean finished;
		long start = System.currentTimeMillis();
		do
		{
			if (System.currentTimeMillis() - start > timeOut)
			{
				//throw new TimeoutException();
			}
			
			finished = true;
			
			for(ClientAI ia : ias)
			{
				if (ia.getTurn() != turn)
				{
					finished = false;
					break;
				}
			}
			
			Thread.sleep(200);
			
		}while(!finished);		
	}
	
	// Server/Client ports
	final int PORT = 3131;
	// JUnit Timeout
	final int TIMEOUT_JUNIT = 1000*9999;
	// Network Timeout
	final int TIMEOUT_NET = 1000*30;
	// Client is connected test timeout
	final int TIMEOUT_CLIENT_CONNECTION = 1000*10;
	
	/**
	 * Test for client side.
	 * @throws InterruptedException 
	 */
	@Test(timeout=TIMEOUT_JUNIT)
	public void testClient() throws InterruptedException
	{
		for(int i=0; i < 10; ++i)
		{
			System.err.println("i == "+i);
			long start = System.currentTimeMillis();
			while(System.currentTimeMillis() - start < TIMEOUT_NET)
			{
				Thread.sleep(1000);
				System.err.print(".");
			}
			System.err.println();
			
			tester.flush();
			test(true);
		}
	}
	
	/**
	 * Test for server side.
	 */
	//@Test
	public void testServer() {test(false);}
	
	private void test(boolean isClientTest)
	{	
		Logger.getLogger("com.almworks.sqlite4java").setLevel(Level.ALL);
		
		int turn = 0;
		
		assertTrue("Unexpected remaining log", tester.flush());
		System.out.println("Step: Server creation");

		SEPServer server = new SEPServer(PORT, TIMEOUT_NET);
		assertNotNull("Unexpected result", server);

		// server.getClientsNumber()
		assertEquals("No connected client expected.", 0, server.getClientsNumber());

		// server.getPlayerList()
		boolean noPlayers = false;
		try
		{
			noPlayers = server.getPlayerList().isEmpty();
		}
		catch(Throwable t)
		{
			fail(t.getMessage());
		}
		
		assertTrue("Player list expected to be empty.", noPlayers);

		// server.getServerAdminKey()
		final String SERVER_ADMIN_KEY = server.getServerAdminKey();

		try
		{
			// server.getAddress()
			assertEquals("Server address expected to be localhost.", InetAddress.getLocalHost(), server.getAddress());
		}
		catch(UnknownHostException e)
		{
			fail("Test code exception: " + e.getMessage());
			return;
		}
		
		assertTrue("Unexpected remaining log", tester.flush());
		System.out.println("Step: Starting server");
		
		// server.start
		server.start();
		try
		{
			Thread.sleep(500);
		}
		catch(InterruptedException e)
		{
			fail(e.getMessage());
		}

		tester.checkNextTrace(eTest.Server, SEPServer.class, "start", "Server started");

		assertTrue("Unexpected remaining log", tester.flush());
		System.out.println("Step: Client1 connection");
		
		// Client 1

		TestClientUserInterface ui1 = new TestClientUserInterface(isClientTest, "client1", SERVER_ADMIN_KEY, "localhost", PORT, TIMEOUT_NET);
		SEPClient client1 = ui1.getClient();

		assertEquals("Unexpected client login", "client1", client1.getLogin());
		assertFalse("No connection expected", client1.isConnected());

		// client.connect()
		client1.connect();

		// client.isConnected()
		for(long start = System.currentTimeMillis(); !client1.isConnected() && System.currentTimeMillis() - start < TIMEOUT_CLIENT_CONNECTION;)
			;
		assertTrue("Expected to be connected", client1.isConnected());

		tester.checkNextTrace(eTest.Clients, SEPClient.class, "connect", "Client 'client1' connected");
		tester.checkNextTrace(eTest.Clients, TestSEP.TestClientUserInterface.class, "refreshPlayerList", "client1.refreshPlayerList([client1])");
		tester.checkNextTrace(eTest.Clients, TestSEP.TestClientUserInterface.class, "displayGameCreationPanel", "client1.displayGameCreationPanel");

		assertTrue("Unexpected remaining log", tester.flush());
		System.out.println("Step: Client1 getting gameCreation interface");
		
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

		TestClientUserInterface ui2 = new TestClientUserInterface(isClientTest, "client2", "localhost", PORT, TIMEOUT_NET);
		SEPClient client2 = ui2.getClient();

		assertEquals("Unexpected client login", "client2", client2.getLogin());
		assertFalse("No connection expected", client2.isConnected());

		assertTrue("Unexpected remaining log", tester.flush());
		System.out.println("Step: Client2 connection");
		
		client2.connect();
		for(long start = System.currentTimeMillis(); !client2.isConnected() && System.currentTimeMillis() - start < TIMEOUT_CLIENT_CONNECTION;)
			;
		assertTrue("Expected to be connected", client2.isConnected());

		tester.checkNextTrace(eTest.Clients, SEPClient.class, "connect", "Client 'client2' connected");
		tester.checkAllUnorderedTraces(eTest.Clients, TestSEP.TestClientUserInterface.class, "refreshPlayerList", new String[] { "client1.refreshPlayerList([client1, client2])", "client2.refreshPlayerList([client1, client2])" });
		tester.checkNextTrace(eTest.Clients, TestSEP.TestClientUserInterface.class, "displayGameCreationPanel", "client2.displayGameCreationPanel");
		
		assertTrue("Unexpected remaining log", tester.flush());
		System.out.println("Step: Client2 getting gameCreation interface");
		
		// client1GameCreation.getGameConfig()
		ServerGameCreation client2GameCreation;
		IGameConfig gameConfig;
		GameConfig gameCfg = new GameConfig();
		try
		{
			client2GameCreation = client2.getGameCreationInterface();
			gameConfig = client1GameCreation.getGameConfig();
			GameConfigCopier.copy(IGameConfig.class, gameConfig, gameCfg);
		}
		catch(Exception e)
		{
			fail("Unexpected exception thrown : " + e.getMessage());
			e.printStackTrace();
			return;
		}

		assertTrue("Unexpected default value.", gameCfg.getDimZ() > 1);

		assertTrue("Unexpected remaining log", tester.flush());
		System.out.println("Step: GameConfig update NOK/OK cases");
		
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
			
			while(client1GameCreation.getGameConfig().getDimZ() != gameCfg.getDimZ() || client2GameCreation.getGameConfig().getDimZ() != gameCfg.getDimZ()) Thread.sleep(1000);
			
			gameCfg.setDimZ(1);
			gameCfg.setPlayersPlanetsStartingCarbonResources(gameCfg.getCelestialBodiesStartingCarbonAmount(eCelestialBodyType.Planet)[0]-1);
			client1GameCreation.updateGameConfig(gameCfg);
			
			while(client1GameCreation.getGameConfig().getDimZ() != gameCfg.getDimZ() || client2GameCreation.getGameConfig().getDimZ() != gameCfg.getDimZ()) Thread.sleep(1000);
		}
		catch(Throwable t)
		{
			fail("Unexpected exception thrown : " + t.getMessage());
			t.printStackTrace();
			return;
		}

		tester.checkAllUnorderedTraces(eTest.Clients, TestSEP.TestClientUserInterface.class, "refreshGameConfig", new String[] { "client1.refreshGameConfig(2)", "client2.refreshGameConfig(2)" });
		tester.checkAllUnorderedTraces(eTest.Clients, TestSEP.TestClientUserInterface.class, "refreshGameConfig", new String[] { "client1.refreshGameConfig(1)", "client2.refreshGameConfig(1)" });
		
		tester.checkNextTrace(eTest.Server, SEPServer.SEPGameCreation.class, "updateGameConfig", "client2 tried to update game config but is not admin.");

		// Client 3

		assertTrue("Unexpected remaining log", tester.flush());
		System.out.println("Step: Client3 connection");
		
		TestClientUserInterface ui3 = new TestClientUserInterface(isClientTest, "client3", "localhost", PORT, TIMEOUT_NET);
		SEPClient client3 = ui3.getClient();

		assertEquals("Unexpected client login", "client3", client3.getLogin());
		assertFalse("No connection expected", client3.isConnected());

		client3.connect();
		for(long start = System.currentTimeMillis(); !client3.isConnected() && System.currentTimeMillis() - start < TIMEOUT_CLIENT_CONNECTION;)
			;
		assertTrue("Expected to be connected", client3.isConnected());

		tester.checkNextTrace(eTest.Clients, SEPClient.class, "connect", "Client 'client3' connected");
		tester.checkAllUnorderedTraces(eTest.Clients, TestSEP.TestClientUserInterface.class, "refreshPlayerList", new String[] { "client1.refreshPlayerList([client1, client2, client3])", "client2.refreshPlayerList([client1, client2, client3])", "client3.refreshPlayerList([client1, client2, client3])" });
		tester.checkNextTrace(eTest.Clients, TestSEP.TestClientUserInterface.class, "displayGameCreationPanel", "client3.displayGameCreationPanel");

		assertTrue("Unexpected remaining log", tester.flush());
		System.out.println("Step: Client3 getting gameCreation interface");
		
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

		assertTrue("Unexpected remaining log", tester.flush());
		System.out.println("Step: running game");
		
		ClientAI client1AITest = ui1.getAITest();
		ClientAI client2AITest = ui2.getAITest();
		ClientAI client3AITest = ui3.getAITest();

		turn = -1;
		assertEquals("Unexpected result", turn, client1AITest.getTurn());

		// client.runGame()
		try
		{
			client1.runGame();
			turn = 1;
			waitForNextTurn(120*1000, turn, client1AITest, client2AITest, client3AITest);
		}
		catch(Throwable t)
		{
			tester.flush();
			t.printStackTrace();			
			fail("Unexpected exception thrown : " + t.getMessage());
			return;
		}

		assertEquals("Unexpected result", turn, client1AITest.getTurn());

		tester.checkAllUnorderedTraces(eTest.Clients, TestSEP.TestClientUserInterface.class, "onGameRan", new String[] { "client1.onGameRan", "client2.onGameRan", "client3.onGameRan" });		
		tester.checkNextTrace(eTest.Server, SEPServer.SEPGameServerListener.class, "gameRan", "gameRan");						
		
		//tester.checkAllUnorderedTraces(eTest.Server, GameBoard.class, "getPlayerGameBoard", new String[] { "getGameBoard(client1)", "getGameBoard(client2)", "getGameBoard(client3)" });
		tester.checkAllUnorderedTraces(eTest.Clients, TestSEP.TestClientUserInterface.class, "refreshGameConfig", new String[] { "client1.refreshGameConfig(1)", "client2.refreshGameConfig(1)", "client3.refreshGameConfig(1)" });
		tester.checkAllUnorderedTraces(eTest.Clients, TestSEP.TestClientUserInterface.class, "receiveNewTurnGameBoard", new String[] { "client1.receiveNewTurnGameBoard(1)", "client2.receiveNewTurnGameBoard(1)", "client3.receiveNewTurnGameBoard(1)" });
		
		assertTrue("Unexpected remaining log", tester.flush());
		System.out.println("Step: getting clients runningGame interfaces");
		
		// client.getRunningGameInterface()
		//ServerRunningGame client1RunningGame;
		ServerRunningGame client2RunningGame;
		ServerRunningGame client3RunningGame;
		try
		{
			//client1RunningGame = client1.getRunningGameInterface();
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

		assertTrue("Unexpected remaining log", tester.flush());
		System.out.println("Step: Build");

		// T0, client1 build a starship plant on its home planet, client2 too.

		assertTrue("Unexpected remaining log", tester.flush());
		
		System.out.println("Step: T0, build");
		
		client1AITest.checkBuilding(startingPlanet1, eBuildingType.StarshipPlant, 0);
		client2AITest.checkBuilding(startingPlanet2, eBuildingType.StarshipPlant, 0);
		client3AITest.checkBuilding(startingPlanet3, eBuildingType.ExtractionModule, 0);

		try
		{
			client1AITest.getGameBoard().onLocalCommand(new Build(client1.getLogin(), startingPlanet1, eBuildingType.StarshipPlant));			
			client2AITest.getGameBoard().onLocalCommand(new Build(client2.getLogin(), startingPlanet2, eBuildingType.StarshipPlant));			
			client3AITest.getGameBoard().onLocalCommand(new Build(client3.getLogin(), startingPlanet3, eBuildingType.ExtractionModule));

			expectedExceptionThrown = false;

			Build erronedCommand = new Build(client1.getLogin(), startingPlanet1, eBuildingType.ExtractionModule);
			checkErronedCommand(erronedCommand, client1AITest);						

			client1AITest.getClient().endTurn();
			client2AITest.getClient().endTurn();
			client3AITest.getClient().endTurn();
			turn = 1;

			waitForNextTurn(120*1000, turn, client1AITest, client2AITest, client3AITest);
		}
		catch(Throwable t)
		{
			t.printStackTrace();
			fail("Unexpected exception thrown : " + t.getMessage());
			return;
		}

		assertEquals("Unexpected result", turn, client1AITest.getTurn());
		
		tester.checkNextTrace(eTest.Server, SEPServer.class, "checkForNextTurn", "Resolving new turn");
		tester.checkAllUnorderedTraces(eTest.Clients, TestSEP.TestClientUserInterface.class, "receiveNewTurnGameBoard", new String[] { "client1.receiveNewTurnGameBoard("+turn+")", "client2.receiveNewTurnGameBoard("+turn+")", "client3.receiveNewTurnGameBoard("+turn+")" });
		tester.checkAllUnorderedTraces(eTest.Server, GameBoard.class, "getPlayerGameBoard", new String[] { "getGameBoard(client1)", "getGameBoard(client2)", "getGameBoard(client3)" });

		client1AITest.checkBuilding(startingPlanet1, eBuildingType.StarshipPlant, 1);
		client2AITest.checkBuilding(startingPlanet2, eBuildingType.StarshipPlant, 1);
		client3AITest.checkBuilding(startingPlanet3, eBuildingType.ExtractionModule, 1);

		// T1, client1 & client2 make starships, form fleets and change diplomacy, client3 demolish ExtractionModule, build a StarshipPlant and change diplomacy.

		assertTrue("Unexpected remaining log", tester.flush());
		System.out.println("Step: T1, make starships, form fleets, change diplomacies, move fleets");
		String t1Fleets = "fleet1";

		Map<String, PlayerPolicies> client1diplomacy = new HashMap<String, PlayerPolicies>();
		Map<String, PlayerPolicies> client2diplomacy = new HashMap<String, PlayerPolicies>();
		Map<String, PlayerPolicies> client3diplomacy = new HashMap<String, PlayerPolicies>();

		try
		{
			Map<String, Integer> starshipsToMake = new HashMap<String, Integer>();			
			StarshipTemplate[] starshipTemplates = Rules.getStarshipTemplates().toArray(new StarshipTemplate[Rules.getStarshipTemplates().size()]);

			starshipsToMake.put(starshipTemplates[0].getName(), 10);

			client1AITest.getGameBoard().onLocalCommand(new MakeStarships(client1.getLogin(), startingPlanet1, starshipsToMake));

			starshipsToMake.put(starshipTemplates[0].getName(), 999999);
			ICommand erronedCommand = new MakeStarships(client1.getLogin(), startingPlanet1, starshipsToMake);
			checkErronedCommand(erronedCommand, client1AITest);						

			starshipsToMake.put(starshipTemplates[0].getName(), 5);
			client1AITest.getGameBoard().onLocalCommand(new AssignStarships(client1.getLogin(), startingPlanet1, starshipsToMake, t1Fleets));

			starshipsToMake.put(starshipTemplates[0].getName(), 3);
			client1AITest.getGameBoard().onLocalCommand(new AssignStarships(client1.getLogin(), startingPlanet1, starshipsToMake, t1Fleets + "bis"));

			starshipsToMake.put(starshipTemplates[0].getName(), 3);
			erronedCommand = new AssignStarships(client1.getLogin(), startingPlanet1, starshipsToMake, "incorrectFleet");
			checkErronedCommand(erronedCommand, client1AITest);						

			starshipsToMake.remove(starshipTemplates[0].getName());
			starshipsToMake.put(starshipTemplates[3].getName(), 11);
			client2AITest.getGameBoard().onLocalCommand(new MakeStarships(client2.getLogin(), startingPlanet2, starshipsToMake));
			starshipsToMake.put(starshipTemplates[3].getName(), 4);
			client2AITest.getGameBoard().onLocalCommand(new AssignStarships(client2.getLogin(), startingPlanet2, starshipsToMake, t1Fleets));

			client3AITest.getGameBoard().onLocalCommand(new Demolish(startingPlanet3, ExtractionModule.class));
			client3AITest.getGameBoard().onLocalCommand(new Build(startingPlanet3, StarshipPlant.class));

			PlayerPolicies client31policies = new PlayerPolicies(client1.getLogin(), true, eForeignPolicy.HOSTILE);
			PlayerPolicies client32policies = new PlayerPolicies(client2.getLogin(), true, eForeignPolicy.HOSTILE);
			client3diplomacy.put(client1.getLogin(), client31policies);
			client3diplomacy.put(client2.getLogin(), client32policies);
			client3AITest.getGameBoard().onLocalCommand(new ChangeDiplomacy(client3diplomacy));

			PlayerPolicies client33policies = new PlayerPolicies(client3.getLogin(), true, eForeignPolicy.HOSTILE);
			client3diplomacy.put(client3.getLogin(), client33policies);
			erronedCommand = new ChangeDiplomacy(client3diplomacy);
			checkErronedCommand(erronedCommand, client3AITest.getGameBoard(), isClientTest);

			PlayerPolicies client12policies = new PlayerPolicies(client2.getLogin(), false, eForeignPolicy.HOSTILE_IF_OWNER);
			PlayerPolicies client13policies = new PlayerPolicies(client3.getLogin(), false, eForeignPolicy.HOSTILE_IF_OWNER);
			client1diplomacy.put(client2.getLogin(), client12policies);
			client1diplomacy.put(client3.getLogin(), client13policies);
			client1AITest.getGameBoard().onLocalCommand(new ChangeDiplomacy(client1diplomacy));

			PlayerPolicies client21policies = new PlayerPolicies(client1.getLogin(), false, eForeignPolicy.NEUTRAL);
			PlayerPolicies client23policies = new PlayerPolicies(client3.getLogin(), false, eForeignPolicy.HOSTILE);
			client2diplomacy.put(client1.getLogin(), client21policies);
			client2diplomacy.put(client3.getLogin(), client23policies);
			client2AITest.getGameBoard().onLocalCommand(new ChangeDiplomacy(client2diplomacy));

			Move client1move = new Move(startingPlanet3, 0, false);
			Move client2move = new Move(startingPlanet3, 1, false);
			Stack<Move> checkpoints = new Stack<Move>();
			checkpoints.add(client1move);
			client1AITest.getGameBoard().onLocalCommand(new MoveFleet(t1Fleets, checkpoints));

			checkpoints.clear();
			checkpoints.add(client2move);
			client2AITest.getGameBoard().onLocalCommand(new MoveFleet(t1Fleets, checkpoints));

			client1AITest.getGameBoard().endTurn();
			client2AITest.getGameBoard().endTurn();
			client3AITest.getGameBoard().endTurn();
			turn = 2;

			waitForNextTurn(turn, client1AITest, client2AITest, client3AITest);
		}
		catch(Throwable t)
		{
			t.printStackTrace();
			fail("Unexpected exception thrown : " + t.getMessage());
			return;
		}

		assertEquals("Unexpected result", turn, client1AITest.getTurn());

		tester.checkNextTrace(serverOut, SEPServer.class, "checkForNextTurn", "Resolving new turn");
		tester.checkAllUnorderedTraces(clientOut, TestSEP.TestClientUserInterface.class, "receiveNewTurnGameBoard", new String[] { "client1.receiveNewTurnGameBoard("+turn+")", "client2.receiveNewTurnGameBoard("+turn+")", "client3.receiveNewTurnGameBoard("+turn+")" });
		tester.checkAllUnorderedTraces(serverOut, GameBoard.class, "getPlayerGameBoard", new String[] { "getGameBoard(client1)", "getGameBoard(client2)", "getGameBoard(client3)" });

		client1AITest.checkBuilding(startingPlanet1, StarshipPlant.class, 1);
		client2AITest.checkBuilding(startingPlanet2, StarshipPlant.class, 1);
		client3AITest.checkBuilding(startingPlanet3, ExtractionModule.class, 0);
		client3AITest.checkBuilding(startingPlanet3, StarshipPlant.class, 1);

		client1AITest.checkFleetMove(t1Fleets, 5, startingPlanet1, startingPlanet3, 1, isClientTest);		
		client1AITest.checkFleetNotMoved(t1Fleets + "bis", 3, isClientTest);
		client2AITest.checkFleetNotMoved(t1Fleets, 4, isClientTest);

		client1AITest.checkDiplomacy(client1diplomacy);
		client2AITest.checkDiplomacy(client2diplomacy);
		client3diplomacy.remove(client3.getLogin());
		client3AITest.checkDiplomacy(client3diplomacy);

		// T2, client1 dismantle formed fleet on starting planet, client3 build starships and form a fleet. 

		assertTrue("Unexpected remaining log", tester.flush());
		System.out.println("Step: dismantle/build/form fleets");
		
		String t2Fleets = "fleet2";

		try
		{
			client1AITest.getGameBoard().onLocalCommand(new DismantleFleet(t1Fleets + "bis"));

			Map<StarshipTemplate, Integer> starshipsToMake = new HashMap<StarshipTemplate, Integer>();
			StarshipTemplate[] starshipTemplates = SEPUtils.starshipTypes.toArray(new StarshipTemplate[SEPUtils.starshipTypes.size()]);

			starshipsToMake.put(starshipTemplates[6], 5);
			client3AITest.getGameBoard().onLocalCommand(new MakeStarships(startingPlanet3, starshipsToMake));

			starshipsToMake.put(starshipTemplates[6], 5);
			client3AITest.getGameBoard().onLocalCommand(new FormFleet(startingPlanet3, t2Fleets, starshipsToMake, null));

			client1AITest.getGameBoard().endTurn();
			client2AITest.getGameBoard().endTurn();
			client3AITest.getGameBoard().endTurn();
			turn = 3;

			waitForNextTurn(turn, client1AITest, client2AITest, client3AITest);
		}
		catch(Throwable t)
		{
			t.printStackTrace();
			fail("Unexpected exception thrown : " + t.getMessage());
			return;
		}

		assertEquals("Unexpected result", turn, client1AITest.getTurn());

		tester.checkNextTrace(serverOut, SEPServer.class, "checkForNextTurn", "Resolving new turn");
		tester.checkAllUnorderedTraces(clientOut, TestSEP.TestClientUserInterface.class, "receiveNewTurnGameBoard", new String[] { "client1.receiveNewTurnGameBoard("+turn+")", "client2.receiveNewTurnGameBoard("+turn+")", "client3.receiveNewTurnGameBoard("+turn+")" });
		tester.checkAllUnorderedTraces(serverOut, GameBoard.class, "getPlayerGameBoard", new String[] { "getGameBoard(client1)", "getGameBoard(client2)", "getGameBoard(client3)" });

		client1AITest.checkBuilding(startingPlanet1, StarshipPlant.class, 1);
		client2AITest.checkBuilding(startingPlanet2, StarshipPlant.class, 1);
		client3AITest.checkBuilding(startingPlanet3, ExtractionModule.class, 0);
		client3AITest.checkBuilding(startingPlanet3, StarshipPlant.class, 1);

		client1AITest.checkFleetMove(t1Fleets, 5, startingPlanet1, startingPlanet3, 1, true);		
		
		expectedExceptionThrown = false;
		try
		{
			client1AITest.checkFleetNotMoved(t1Fleets + "bis", 3, true);
		}
		catch(AssertionError e)
		{
			assertEquals(e.getMessage(), "Cannot find fleet 'client1@"+t1Fleets+"bis' : Unknown unit 'client1@"+t1Fleets+"bis'");
			expectedExceptionThrown = true;
		}
		assertTrue("Expected exception not thrown", expectedExceptionThrown);

		client2AITest.checkFleetMove(t1Fleets, 4, startingPlanet2, startingPlanet3, 2, true);
		client3AITest.checkFleetNotMoved(t2Fleets, 5, isClientTest);

		assertTrue("Unexpected remaining log", tester.flush());

		// T3..n loop untill client1 and client2 fleets arrive to client3 starting planet. 

		assertTrue("Unexpected remaining log", tester.flush());
		System.out.println("Step: waiting for fleets to arrive on destination");
		
		boolean arrived = false;

		do
		{
			try
			{
				client1AITest.getGameBoard().endTurn();
				client2AITest.getGameBoard().endTurn();
				client3AITest.getGameBoard().endTurn();
				++turn;
				
				waitForNextTurn(turn, client1AITest, client2AITest, client3AITest);
			}
			catch(Throwable t)
			{
				t.printStackTrace();
				fail("Unexpected exception thrown : " + t.getMessage());
				return;
			}

			assertEquals("Unexpected result", turn, client1AITest.getTurn());

			tester.checkNextTrace(serverOut, SEPServer.class, "checkForNextTurn", "Resolving new turn");
			tester.checkAllUnorderedTraces(clientOut, TestSEP.TestClientUserInterface.class, "receiveNewTurnGameBoard", new String[] { "client1.receiveNewTurnGameBoard(" + turn + ")", "client2.receiveNewTurnGameBoard(" + turn + ")", "client3.receiveNewTurnGameBoard(" + turn + ")" });
			tester.checkAllUnorderedTraces(serverOut, GameBoard.class, "getPlayerGameBoard", new String[] { "getGameBoard(client1)", "getGameBoard(client2)", "getGameBoard(client3)" });

			boolean fleet1moved = client1AITest.checkFleetMove(t1Fleets, 5, startingPlanet1, startingPlanet3, 1, true) > 0;
			boolean fleet2moved = client2AITest.checkFleetMove(t1Fleets, 4, startingPlanet2, startingPlanet3, 2, true) > 0;
			client3AITest.checkFleetNotMoved(t2Fleets, 5, true);

			if (!fleet1moved && !fleet2moved)
			{
				arrived = true;
			}

			assertTrue("Unexpected remaining log", tester.flush());			
			
		} while(!arrived);
		
		client1AITest.checkFleetLocation(t1Fleets, 5, startingPlanet3);
		client2AITest.checkFleetLocation(t1Fleets, 4, startingPlanet3);
		client3AITest.checkFleetLocation(t2Fleets, 5, startingPlanet3);
		
		
		assertTrue("Unexpected remaining log", tester.flush());
		System.out.println("Step: save game");
		
		String saveName = "junitTest";
		try
		{
			client1.saveGame(saveName);
			
			while(!client1.isGameSaved(saveName)) {Thread.sleep(500);}
		}
		catch(Throwable t)
		{
			t.printStackTrace();
			fail("Unexpected exception thrown : " + t.getMessage());
			return;
		}

		try
		{
			client1AITest.getGameBoard().endTurn();
			client2AITest.getGameBoard().endTurn();
			client3AITest.getGameBoard().endTurn();
			++turn;
			
			waitForNextTurn(turn, client1AITest, client2AITest, client3AITest);
		}
		catch(Throwable t)
		{
			t.printStackTrace();
			fail("Unexpected exception thrown : " + t.getMessage());
			return;
		}

		assertEquals("Unexpected result", turn, client1AITest.getTurn());

		tester.checkNextTrace(serverOut, SEPServer.class, "checkForNextTurn", "Resolving new turn");
		tester.checkAllUnorderedTraces(clientOut, TestSEP.TestClientUserInterface.class, "receiveNewTurnGameBoard", new String[] { "client1.receiveNewTurnGameBoard("+turn+")", "client2.receiveNewTurnGameBoard("+turn+")", "client3.receiveNewTurnGameBoard("+turn+")" });
		tester.checkAllUnorderedTraces(serverOut, GameBoard.class, "getPlayerGameBoard", new String[] { "getGameBoard(client1)", "getGameBoard(client2)", "getGameBoard(client3)" });

		assertTrue("Unexpected remaining log", tester.flush());
		System.out.println("Step: looping load game & battle test");
		
		int client1Win=0, client2Win=0, client3Win=0, totalRuns=0;
		long startTime, sumTime=0;
		
		do
		{
			++totalRuns;
			
			try
			{
				client1.loadGame(saveName);
				--turn;
				
				waitForNextTurn(turn, client1AITest, client2AITest, client3AITest);
			}
			catch(Throwable t)
			{
				t.printStackTrace();
				fail("Unexpected exception thrown : " + t.getMessage());
				return;
			}
			
			assertEquals("Unexpected result", turn, client1AITest.getTurn());
	
			tester.checkNextTrace(serverOut, SEPServer.SEPGameServerListener.class, "gamePaused", "gamePaused");
			tester.checkNextTrace(serverOut, SEPServer.SEPGameServerListener.class, "gameResumed", "gameResumed");
			tester.checkAllUnorderedTraces(serverOut, GameBoard.class, "getPlayerGameBoard", "getGameBoard(client1)", "getGameBoard(client2)", "getGameBoard(client3)");
	
			tester.checkAllUnorderedTraces(clientOut, TestClientUserInterface.class, "onGamePaused", "client1.onGamePaused", "client2.onGamePaused", "client3.onGamePaused");
			tester.checkAllUnorderedTraces(clientOut, TestClientUserInterface.class, "onGameResumed", "client1.onGameResumed", "client2.onGameResumed", "client3.onGameResumed");		
	
			assertTrue("Unexpected remaining log", tester.flush());
			
			try
			{
				Stack<Move> checkpoints = new Stack<Move>();
				checkpoints.add(new Move(startingPlanet3, 0, true));
				client2AITest.getGameBoard().onLocalCommand(new MoveFleet(t1Fleets, checkpoints));
				
				client1AITest.getGameBoard().endTurn();
				client2AITest.getGameBoard().endTurn();
				client3AITest.getGameBoard().endTurn();
				++turn;
				
				startTime = System.currentTimeMillis();
				waitForNextTurn(turn, client1AITest, client2AITest, client3AITest);
				sumTime += System.currentTimeMillis() - startTime;
			}
			catch(Throwable t)
			{
				t.printStackTrace();
				fail("Unexpected exception thrown : " + t.getMessage());
				return;
			}
	
			assertEquals("Unexpected result", turn, client1AITest.getTurn());
			
			tester.checkNextTrace(serverOut, SEPServer.class, "checkForNextTurn", "Resolving new turn");
			tester.checkAllUnorderedTraces(clientOut, TestSEP.TestClientUserInterface.class, "receiveNewTurnGameBoard", new String[] { "client1.receiveNewTurnGameBoard("+turn+")", "client2.receiveNewTurnGameBoard("+turn+")", "client3.receiveNewTurnGameBoard("+turn+")" });
			tester.checkAllUnorderedTraces(serverOut, GameBoard.class, "getPlayerGameBoard", new String[] { "getGameBoard(client1)", "getGameBoard(client2)", "getGameBoard(client3)" });
			
			Fleet f;
			
			if (client1AITest.isCelestialBodyOwner(startingPlanet3))
			{
				System.out.println("Run "+totalRuns+", winner: "+client1.getLogin());
				++client1Win;
				f = client1AITest.checkFleetLocation(t1Fleets, startingPlanet3);
				assertTrue("Unexpected result ("+f.getTotalQt()+")", f.getTotalQt() <= 5);
				
				client3AITest.checkFleetDestroyed(t2Fleets);
				client3AITest.checkInvisibleLocation(startingPlanet3);
			}
			else if (client2AITest.isCelestialBodyOwner(startingPlanet3))
			{
				System.out.println("Run "+totalRuns+", winner: "+client2.getLogin());
				++client2Win;
				f = client2AITest.checkFleetLocation(t1Fleets, startingPlanet3);
				assertTrue("Unexpected result ("+f.getTotalQt()+")", f.getTotalQt() <= 4);
				
				client1AITest.checkFleetDestroyed(t1Fleets);
				client1AITest.checkInvisibleLocation(startingPlanet3);
				client3AITest.checkFleetDestroyed(t2Fleets);
				client3AITest.checkInvisibleLocation(startingPlanet3);
			}
			else if (client3AITest.isCelestialBodyOwner(startingPlanet3))
			{
				System.out.println("Run "+totalRuns+", winner: "+client3.getLogin());
				++client3Win;
				f = client3AITest.checkFleetLocation(t2Fleets, startingPlanet3);
				assertTrue("Unexpected result ("+f.getTotalQt()+")", f.getTotalQt() <= 5);
				
				client1AITest.checkFleetDestroyed(t1Fleets);
				client1AITest.checkInvisibleLocation(startingPlanet3);
				client2AITest.checkFleetDestroyed(t1Fleets);
				client2AITest.checkInvisibleLocation(startingPlanet3);
			}
			
		}while(totalRuns < 10 && (client1Win<=0 || client2Win<=0 || client3Win<=0));
		
		System.out.format("Average time (ms) : %.2fms\n", (double) sumTime / totalRuns);
		System.out.format(client1.getLogin()+" wins "+client1Win+" times (%.2f).\n"+client2.getLogin()+" wins "+client2Win+" times (%.2f).\n"+client3.getLogin()+" wins "+client3Win+" times (%.2f).\n", (double) client1Win/totalRuns, (double) client2Win/totalRuns, (double) client3Win/totalRuns);
		
		assertEquals("Unexpected result", totalRuns, client1Win);
		
		// DefenseModule test
		assertTrue("Unexpected remaining log", tester.flush());
		System.out.println("Step: load game / max defenseModule battle test");
		try
		{
			client1.loadGame(saveName);
			--turn;
			
			waitForNextTurn(turn, client1AITest, client2AITest, client3AITest);
		}
		catch(Throwable t)
		{
			t.printStackTrace();
			fail("Unexpected exception thrown : " + t.getMessage());
			return;
		}
		
		assertEquals("Unexpected result", turn, client1AITest.getTurn());

		tester.checkNextTrace(serverOut, SEPServer.SEPGameServerListener.class, "gamePaused", "gamePaused");
		tester.checkNextTrace(serverOut, SEPServer.SEPGameServerListener.class, "gameResumed", "gameResumed");
		tester.checkAllUnorderedTraces(serverOut, GameBoard.class, "getPlayerGameBoard", "getGameBoard(client1)", "getGameBoard(client2)", "getGameBoard(client3)");

		tester.checkAllUnorderedTraces(clientOut, TestClientUserInterface.class, "onGamePaused", "client1.onGamePaused", "client2.onGamePaused", "client3.onGamePaused");
		tester.checkAllUnorderedTraces(clientOut, TestClientUserInterface.class, "onGameResumed", "client1.onGameResumed", "client2.onGameResumed", "client3.onGameResumed");		

		assertTrue("Unexpected remaining log", tester.flush());

		client1AITest.checkBuilding(startingPlanet3, DefenseModule.class, 0);
		
		int nbDefenseModule = 0;
		BuildCheck chr;
		
		do
		{
			try
			{	
				chr = new Build(startingPlanet3, DefenseModule.class).can(client3AITest.getGameBoard().getGameBoard());
				if (!chr.isPossible()) continue;
				
				++nbDefenseModule;
				client3AITest.getGameBoard().onLocalCommand(new Build(startingPlanet3, DefenseModule.class));				
				
				client1AITest.getGameBoard().endTurn();
				client2AITest.getGameBoard().endTurn();
				client3AITest.getGameBoard().endTurn();
				++turn;
				
				waitForNextTurn(turn, client1AITest, client2AITest, client3AITest);			
			}
			catch(Throwable t)
			{
				t.printStackTrace();
				fail("Unexpected exception thrown : " + t.getMessage());
				return;
			}
	
			assertEquals("Unexpected result", turn, client1AITest.getTurn());
			
			tester.checkNextTrace(serverOut, SEPServer.class, "checkForNextTurn", "Resolving new turn");
			tester.checkAllUnorderedTraces(clientOut, TestSEP.TestClientUserInterface.class, "receiveNewTurnGameBoard", new String[] { "client1.receiveNewTurnGameBoard("+turn+")", "client2.receiveNewTurnGameBoard("+turn+")", "client3.receiveNewTurnGameBoard("+turn+")" });
			tester.checkAllUnorderedTraces(serverOut, GameBoard.class, "getPlayerGameBoard", new String[] { "getGameBoard(client1)", "getGameBoard(client2)", "getGameBoard(client3)" });
			
			client1AITest.checkBuilding(startingPlanet3, DefenseModule.class, nbDefenseModule);
			
		}while(chr.isPossible());
		
		assertTrue("Unexpected numbers of built ("+nbDefenseModule+").", nbDefenseModule > 3);
		
		try
		{
			Stack<Move> checkpoints = new Stack<Move>();
			checkpoints.add(new Move(startingPlanet3, 0, true));
			client2AITest.getGameBoard().onLocalCommand(new MoveFleet(t1Fleets, checkpoints));
			
			client1AITest.getGameBoard().endTurn();
			client2AITest.getGameBoard().endTurn();
			client3AITest.getGameBoard().endTurn();
			++turn;
			
			waitForNextTurn(turn, client1AITest, client2AITest, client3AITest);			
		}
		catch(Throwable t)
		{
			t.printStackTrace();
			fail("Unexpected exception thrown : " + t.getMessage());
			return;
		}

		assertEquals("Unexpected result", turn, client1AITest.getTurn());
		
		tester.checkNextTrace(serverOut, SEPServer.class, "checkForNextTurn", "Resolving new turn");
		tester.checkAllUnorderedTraces(clientOut, TestSEP.TestClientUserInterface.class, "receiveNewTurnGameBoard", new String[] { "client1.receiveNewTurnGameBoard("+turn+")", "client2.receiveNewTurnGameBoard("+turn+")", "client3.receiveNewTurnGameBoard("+turn+")" });
		tester.checkAllUnorderedTraces(serverOut, GameBoard.class, "getPlayerGameBoard", new String[] { "getGameBoard(client1)", "getGameBoard(client2)", "getGameBoard(client3)" });
		
		if (client1AITest.isCelestialBodyOwner(startingPlanet3) || client2AITest.isCelestialBodyOwner(startingPlanet3))
		{
			fail("Unexpected winner.");
		}
		assertTrue(client3AITest.isCelestialBodyOwner(startingPlanet3));
		client1AITest.checkBuilding(startingPlanet3, DefenseModule.class, nbDefenseModule);
		
		// Test case: DefenseModule owner is defeated, DefenseModule is supposed to be destroyed.
		assertTrue("Unexpected remaining log", tester.flush());
		System.out.println("Step: load game / defenseModule defeat case");
		try
		{
			client1.loadGame(saveName);
			turn-=(nbDefenseModule+1);
			
			waitForNextTurn(turn, client1AITest, client2AITest, client3AITest);
		}
		catch(Throwable t)
		{
			t.printStackTrace();
			fail("Unexpected exception thrown : " + t.getMessage());
			return;
		}
		
		assertEquals("Unexpected result", turn, client1AITest.getTurn());

		tester.checkNextTrace(serverOut, SEPServer.SEPGameServerListener.class, "gamePaused", "gamePaused");
		tester.checkNextTrace(serverOut, SEPServer.SEPGameServerListener.class, "gameResumed", "gameResumed");
		tester.checkAllUnorderedTraces(serverOut, GameBoard.class, "getPlayerGameBoard", "getGameBoard(client1)", "getGameBoard(client2)", "getGameBoard(client3)");

		tester.checkAllUnorderedTraces(clientOut, TestClientUserInterface.class, "onGamePaused", "client1.onGamePaused", "client2.onGamePaused", "client3.onGamePaused");
		tester.checkAllUnorderedTraces(clientOut, TestClientUserInterface.class, "onGameResumed", "client1.onGameResumed", "client2.onGameResumed", "client3.onGameResumed");		

		assertTrue("Unexpected remaining log", tester.flush());

		client1AITest.checkBuilding(startingPlanet3, DefenseModule.class, 0);
		
		nbDefenseModule = 0;
			
		try
		{	
			++nbDefenseModule;
			client3AITest.getGameBoard().onLocalCommand(new Build(startingPlanet3, DefenseModule.class));				
			
			client1AITest.getGameBoard().endTurn();
			client2AITest.getGameBoard().endTurn();
			client3AITest.getGameBoard().endTurn();
			++turn;
			
			waitForNextTurn(turn, client1AITest, client2AITest, client3AITest);			
		}
		catch(Throwable t)
		{
			t.printStackTrace();
			fail("Unexpected exception thrown : " + t.getMessage());
			return;
		}

		assertEquals("Unexpected result", turn, client1AITest.getTurn());
		
		tester.checkNextTrace(serverOut, SEPServer.class, "checkForNextTurn", "Resolving new turn");
		tester.checkAllUnorderedTraces(clientOut, TestSEP.TestClientUserInterface.class, "receiveNewTurnGameBoard", new String[] { "client1.receiveNewTurnGameBoard("+turn+")", "client2.receiveNewTurnGameBoard("+turn+")", "client3.receiveNewTurnGameBoard("+turn+")" });
		tester.checkAllUnorderedTraces(serverOut, GameBoard.class, "getPlayerGameBoard", new String[] { "getGameBoard(client1)", "getGameBoard(client2)", "getGameBoard(client3)" });
		
		assertEquals("Unexpected numbers of built ("+nbDefenseModule+").", 1, nbDefenseModule);
		client1AITest.checkBuilding(startingPlanet3, DefenseModule.class, nbDefenseModule);
		
		try
		{
			Stack<Move> checkpoints = new Stack<Move>();
			checkpoints.add(new Move(startingPlanet3, 0, true));
			client2AITest.getGameBoard().onLocalCommand(new MoveFleet(t1Fleets, checkpoints));
			
			client1AITest.getGameBoard().endTurn();
			client2AITest.getGameBoard().endTurn();
			client3AITest.getGameBoard().endTurn();
			++turn;
			
			waitForNextTurn(turn, client1AITest, client2AITest, client3AITest);			
		}
		catch(Throwable t)
		{
			t.printStackTrace();
			fail("Unexpected exception thrown : " + t.getMessage());
			return;
		}

		assertEquals("Unexpected result", turn, client1AITest.getTurn());
		
		tester.checkNextTrace(serverOut, SEPServer.class, "checkForNextTurn", "Resolving new turn");
		tester.checkAllUnorderedTraces(clientOut, TestSEP.TestClientUserInterface.class, "receiveNewTurnGameBoard", new String[] { "client1.receiveNewTurnGameBoard("+turn+")", "client2.receiveNewTurnGameBoard("+turn+")", "client3.receiveNewTurnGameBoard("+turn+")" });
		tester.checkAllUnorderedTraces(serverOut, GameBoard.class, "getPlayerGameBoard", new String[] { "getGameBoard(client1)", "getGameBoard(client2)", "getGameBoard(client3)" });
		
		if (client3AITest.isCelestialBodyOwner(startingPlanet3))
		{
			fail("Unexpected winner.");
		}
		else if (client1AITest.isCelestialBodyOwner(startingPlanet3))
		{
			client1AITest.checkBuilding(startingPlanet3, DefenseModule.class, 0);
		}
		else if (client2AITest.isCelestialBodyOwner(startingPlanet3))
		{
			client2AITest.checkBuilding(startingPlanet3, DefenseModule.class, 0);
		}		

		*/
		
		/*=======================================================
		
		X	server.getAddress();
		X	server.getClientsNumber();
		V	server.getPlayerList();
		X		empty
				full
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
						
			client1RunningGame.saveGame(saveName);
			client1RunningGame.getPlayerGameBoard();			
			client1RunningGame.sendMessage(msg);
			client1RunningGame.resetTurn();
		V	client1RunningGame.endTurn();
		X		ok
				nok?
			conflicts
				new fleet arrival, no fight.
				new fleet arriver, fight.
		
		=======================================================*/

		// server.stop()
		server.stop();
		tester.checkNextTrace(eTest.Server, SEPServer.class, "stop", "Stopping server");

		assertTrue("Unexpected remaining log", tester.flush());
		
		server.stop();
		server.terminate();
		
		client1.disconnect();
		client2.disconnect();
		client3.disconnect();
	}
}
