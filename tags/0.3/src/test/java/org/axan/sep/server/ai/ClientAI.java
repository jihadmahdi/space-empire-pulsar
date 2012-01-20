package org.axan.sep.server.ai;

import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.axan.eplib.orm.SQLDataBaseException;
import org.axan.eplib.orm.sqlite.SQLiteDB;
import org.axan.sep.client.SEPClient;
import org.axan.sep.common.PlayerGameBoard;
import org.axan.sep.common.db.orm.ProductiveCelestialBody;

import com.almworks.sqlite4java.SQLiteException;

public class ClientAI implements IGame.Client
{
	private static Boolean sqlite_lib_init = false;
	
	private final Stack<PlayerGameBoard> previousGameBoards = new Stack<PlayerGameBoard>();
	private final String playerName;
	private String startingPlanet;
	private IGame currentLocalGame;
	private final SEPClient client;
	private final boolean isClientTest;
	
	public ClientAI(String playerName, SEPClient client, boolean isClientTest)
	{
		synchronized(sqlite_lib_init)
		{
			if (!sqlite_lib_init)
			{
				try
				{
					SQLiteDB.checkSQLiteLib("target/izpack/lib/");
				}
				catch(SQLiteException e)
				{
					throw new Error(e);
				}
				
				sqlite_lib_init = true;
			}
		}
		
		this.playerName = playerName;
		this.client = client;
		this.isClientTest = isClientTest;
	}
	
	public IGame getLocalGame()
	{
		return currentLocalGame;
	}
	
	public SEPClient getClient()
	{
		return client;
	}
	
	@Override
	public void endTurn(List<GameCommand<?>> commands) throws Throwable
	{
		client.getRunningGameInterface().endTurn(commands);
	}
	
	public void refreshLocalGameBoard(PlayerGameBoard gameBoard)
	{
		//getStartingPlanetName();
	}
	
	public void refreshGameBoard(PlayerGameBoard gameBoard)
	{
		if (currentLocalGame != null) previousGameBoards.add(currentLocalGame.getGameBoard());
		if (isClientTest)
		{
			currentLocalGame = new LocalGame(this, gameBoard);
		}
		else
		{
			currentLocalGame = new UncheckedLocalGame(this, gameBoard);
		}
		refreshLocalGameBoard(gameBoard);
	}
	
	public String getStartingPlanetName()
	{
		if (startingPlanet == null)
		{
			if (previousGameBoards.isEmpty() && currentLocalGame == null) throw new IllegalStateException("Gameboard not refreshed yet.");
			
			final PlayerGameBoard initial = (previousGameBoards.isEmpty()?currentLocalGame.getGameBoard():previousGameBoards.firstElement());
			
			try
			{				 
				Set<IVersionedPlanet> planets = ProductiveCelestialBody.selectVersion(initial.getDB(), IVersionedPlanet.class, 0, null, "owner = '%s'", playerName);
				if (planets.isEmpty()) throw new Error("No starting planet found for player '"+playerName+"'.");
				startingPlanet = planets.iterator().next().getName();
			}
			catch(SQLDataBaseException e)
			{
				throw new Error(e);
			}
		}
		return startingPlanet;
	}
	
	public int getTurn()
	{
		if (currentLocalGame == null) return -1;
		return currentLocalGame.getGameBoard().getTurn();
	}
	
	/*
	public void checkBuilding(String celestialBodyName, Class<? extends ABuilding> buildingType, int buildSlotsCount)
	{
		if (currentLocalGame == null) fail("Test code error: Gameboard not refreshed yet.");
		
		ABuilding building;
		try
		{
			building = currentLocalGame.getGameBoard().getBuilding(celestialBodyName, buildingType);
		}
		catch(PlayerGameBoardQueryException e)
		{
			e.printStackTrace();
			fail(e.getMessage());
			return;
		}
		
		if (building == null && buildSlotsCount > 0)
		{
			fail("No building but "+buildSlotsCount+" expected.");
			return;
		}
		if (building != null && building.getBuildSlotsCount() != buildSlotsCount)
		{
			fail(buildSlotsCount+" building slots count expected but "+building.getBuildSlotsCount()+" found.");
			return;
		}
	}		
	
	public void checkFleetNotMoved(String fleetName, int expectedQt, boolean checkPreviousGameBoard)
	{
		checkFleetMove(fleetName, expectedQt, null, null, -1, checkPreviousGameBoard);
	}
	public double checkFleetMove(String fleetName, int expectedQt, String sourceCelestialBody, String destinationCelestialBody, int departureDate, boolean checkPreviousGameBoard)
	{
		if (currentLocalGame == null) fail("Test code error: Gameboard not refreshed yet.");
		
		double expectedMoved = 0;
		
		try
		{
			Fleet f = currentLocalGame.getGameBoard().getUnit(playerName, fleetName, Fleet.class);
			assertNotNull("Cannot find fleet '"+playerName+"@"+fleetName+"' : Unknown unit '"+playerName+"@"+fleetName+"'", f);
			
			if (f.getTotalQt() != expectedQt) fail("Unexpected starships quantity.");
			
			expectedMoved = (departureDate < 0 || sourceCelestialBody == null || destinationCelestialBody == null) ? 0 : shouldMove(sourceCelestialBody, destinationCelestialBody, departureDate, f.getSpeed(), getDate()-1);
			
			if (!checkPreviousGameBoard) return expectedMoved;
			
			if (!previousGameBoards.isEmpty())
			{
				try
				{
					Fleet pf = previousGameBoards.lastElement().getUnit(playerName, fleetName, Fleet.class);
					assertNotNull("Cannot find fleet on previous game board '"+playerName+"@"+fleetName+"'", pf);
					if (pf.getCurrentLocation().equals(f.getCurrentLocation()) == (expectedMoved > 0))
					{
						//shouldMove(sourceCelestialBody, destinationCelestialBody, departureDate, f.getSpeed(), getDate()-1);
						System.err.println("checkFleetMove('"+fleetName+"', "+expectedQt+", '"+sourceCelestialBody+"', '"+destinationCelestialBody+"', "+departureDate+") ERROR");
						System.err.println("Fleet: "+f.toString());
						System.err.println("\tpreviousSourceLocation: "+pf.getSourceLocation());
						System.err.println("\tpreviousCurrentLocation: "+pf.getCurrentLocation());
						System.err.println("\tpreviousDestinationLocation: "+pf.getDestinationLocation());
						
						System.err.println("\tsourceLocation: "+f.getSourceLocation());
						System.err.println("\tcurrentLocation: "+f.getCurrentLocation());
						System.err.println("\tdestinationLocation: "+f.getDestinationLocation());
						
						System.err.println("ExpectedMoved : "+expectedMoved);
						
						fail("Unexpected fleet move state ("+(expectedMoved<=0)+").");
					}
				}
				catch(PlayerGameBoardQueryException e)
				{
					if ((f.getDestinationLocation() == null || f.getSourceLocation().equals(f.getCurrentLocation())) == (expectedMoved>1))
					{
						//shouldMove(sourceCelestialBody, destinationCelestialBody, departureDate, f.getSpeed(), getDate()-1);
						System.err.println("checkFleetMove('"+fleetName+"', "+expectedQt+", '"+sourceCelestialBody+"', '"+destinationCelestialBody+"', "+departureDate+") ERROR");
						System.err.println("Fleet: "+f.toString());
						System.err.println("\tsourceLocation: "+f.getSourceLocation());
						System.err.println("\tcurrentLocation: "+f.getCurrentLocation());
						System.err.println("\tdestinationLocation: "+f.getDestinationLocation());
						
						System.err.println("ExpectedMoved : "+expectedMoved);
						
						fail("Unexpected fleet move state ("+(expectedMoved<=0)+").");					
					}
				}
			}
			else if (expectedMoved>0) fail("Fleet has just been created and not moved yet.");
		}
		catch(PlayerGameBoardQueryException e)
		{
			fail("Cannot find fleet '"+playerName+"@"+fleetName+"' : "+e.getMessage());
		}
		
		return expectedMoved;
	}

	public void checkDiplomacy(Map<String, PlayerPolicies> diplomacy)
	{
		if (currentLocalGame == null) fail("Test code error: Gameboard not refreshed yet.");
		
		Diplomacy expectedDiplomacy = new Diplomacy(true, currentLocalGame.getGameBoard().getDate(), playerName, diplomacy);
		assertEquals(expectedDiplomacy, currentLocalGame.getGameBoard().getPlayersPolicies().get(playerName));
	}
	
	public double shouldMove(String sourceCelestialBody, String destinationCelestialBody, int departureDate, double speed, int date)
	{
		if (currentLocalGame == null) fail("Test code error: Gameboard not refreshed yet.");
		
		try
		{
			RealLocation source = currentLocalGame.getGameBoard().getCelestialBodyLocation(sourceCelestialBody);
			RealLocation destination = currentLocalGame.getGameBoard().getCelestialBodyLocation(destinationCelestialBody);			
			double distance = SEPUtils.getDistance(source, destination);
			double totalTime = distance / speed;
			
			// return ((date - departureDate) < totalTime); // No, because when delta is very low the unit may be already count in destination area.
			
			double progress = (date - departureDate) / totalTime;
			
			boolean shouldMove = !SEPUtils.getMobileLocation(source, destination, progress, true).asLocation().equals(destination.asLocation());						
			
			return shouldMove ? totalTime : 0;			
		}
		catch(PlayerGameBoardQueryException e)
		{
			e.printStackTrace();
			fail(e.getMessage());
			return 0;
		}
	}

	public boolean testFleetLocation(String fleetName, String expectedLocationName) throws PlayerGameBoardQueryException
	{
		if (currentLocalGame == null) fail("Test code error: Gameboard not refreshed yet.");
		
		Fleet f = currentLocalGame.getGameBoard().getUnit(playerName, fleetName, Fleet.class);
		
		RealLocation fleetLoc = currentLocalGame.getGameBoard().getUnitLocation(playerName, fleetName);
		RealLocation expectedLoc = currentLocalGame.getGameBoard().getCelestialBodyLocation(expectedLocationName);
		
		if (expectedLoc == null) fail("Cannot locate celestial body '"+expectedLocationName+"'");
		return (expectedLoc.equals(fleetLoc));
	}
	
	public Fleet checkFleetLocation(String fleetName, int expectedQt, String expectedLocationName)
	{
		Fleet f = checkFleetLocation(fleetName, expectedLocationName);
		if (f.getTotalQt() != expectedQt) fail("Unexpected starships quantity.");
		return f;
	}
	
	public Fleet checkFleetLocation(String fleetName, String expectedLocationName)
	{
		if (currentLocalGame == null) fail("Test code error: Gameboard not refreshed yet.");
		
		try
		{
			Fleet f = currentLocalGame.getGameBoard().getUnit(playerName, fleetName, Fleet.class);			
			
			RealLocation fleetLoc = currentLocalGame.getGameBoard().getUnitLocation(playerName, fleetName);
			RealLocation expectedLoc = currentLocalGame.getGameBoard().getCelestialBodyLocation(expectedLocationName);
			
			if (fleetLoc == null) fail("Cannot locate fleet : '"+playerName+"@"+fleetName+"'");
			if (expectedLoc == null) fail("Cannot locate celestial body '"+expectedLocationName+"'");
			
			assertEquals("Unexpected fleet location", expectedLoc, fleetLoc);
			
			return f;
		}
		catch(PlayerGameBoardQueryException e)
		{
			fail("Cannot find fleet '"+playerName+"@"+fleetName+"' : "+e.getMessage());
			return null;
		}
	}
	
	public boolean isCelestialBodyOwner(String celestialBodyName)
	{
		if (currentLocalGame == null) fail("Test code error: Gameboard not refreshed yet.");
		
		try
		{
			ProductiveCelestialBody productiveCelestialBody = currentLocalGame.getGameBoard().getCelestialBody(celestialBodyName, ProductiveCelestialBody.class);			
			return productiveCelestialBody.isVisible() && playerName.equals(productiveCelestialBody.getOwnerName());
		}
		catch(PlayerGameBoardQueryException e)
		{
			fail("Cannot locate productive celestial body '"+celestialBodyName+"'");
			return false;
		}
	}
	
	public void checkFleetDestroyed(String fleetName)
	{
		if (currentLocalGame == null) fail("Test code error: Gameboard not refreshed yet.");
		
		Fleet currentFleet = null;
		try
		{
			currentFleet = currentLocalGame.getGameBoard().getUnit(playerName, fleetName, Fleet.class);
			if (currentFleet != null) fail("Fleet is not destroyed : "+currentFleet);
		}
		catch(PlayerGameBoardQueryException e)
		{
			assertEquals(e.getMessage(), "Unknown unit '"+playerName+"@"+fleetName+"'", e.getMessage());
		}
		
		for(PlayerGameBoard previousGameBoard : previousGameBoards)
		{
			try
			{
				currentFleet = previousGameBoard.getUnit(playerName, fleetName, Fleet.class);
				if (currentFleet != null) return;
			}
			catch (PlayerGameBoardQueryException e)
			{				
				// nothing, this might be normal. A previous unit might has the same name.
			}
		}
		
		fail("Fleet '"+fleetName+"' has never existed.");
	}
	
	public void checkInvisibleLocation(String celestialBodyName)
	{
		if (currentLocalGame == null) fail("Test code error: Gameboard not refreshed yet.");
		
		try
		{
			assertFalse("Unexpected result", currentLocalGame.getGameBoard().getCelestialBody(celestialBodyName).isVisible());
		}
		catch(PlayerGameBoardQueryException e)
		{
			fail("Cannot locate productive celestial body '"+celestialBodyName+"'");
		}
	}
	*/
}