package org.axan.sep.server.ai;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.util.Map;
import java.util.Stack;

import org.axan.sep.common.Diplomacy;
import org.axan.sep.common.Fleet;
import org.axan.sep.common.IBuilding;
import org.axan.sep.common.ICelestialBody;
import org.axan.sep.common.Planet;
import org.axan.sep.common.PlayerGameBoard;
import org.axan.sep.common.ProductiveCelestialBody;
import org.axan.sep.common.SEPUtils;
import org.axan.sep.common.Diplomacy.PlayerPolicies;
import org.axan.sep.common.PlayerGameBoard.PlayerGameBoardQueryException;
import org.axan.sep.common.SEPUtils.RealLocation;

public class ClientAI
{
	private final Stack<PlayerGameBoard> previousGameBoards = new Stack<PlayerGameBoard>();
	private PlayerGameBoard gameBoard;
	private final String playerName;
	private String startingPlanet;
	
	public ClientAI(String playerName)
	{
		this.playerName = playerName;
	}
	
	public void refreshGameBoard(PlayerGameBoard gameBoard)
	{
		if (this.gameBoard != null) previousGameBoards.add(this.gameBoard);
		this.gameBoard = gameBoard;
		getStartingPlanetName();
	}
	
	public String getStartingPlanetName()
	{
		if (startingPlanet == null)
		{
			if (previousGameBoards.isEmpty() && gameBoard == null) throw new IllegalStateException("Gameboard not refreshed yet.");
			
			PlayerGameBoard initial = (previousGameBoards.isEmpty()?gameBoard:previousGameBoards.firstElement());
			
			for(ICelestialBody c : initial.getCelestialBodies())
			{
				if (Planet.class.isInstance(c))
				{
					Planet p = Planet.class.cast(c);
					if (p.getOwnerName() != null && playerName.equals(p.getOwnerName()))
					{
						return p.getName();
					}
				}
			}
			
			throw new Error("No starting planet found for player '"+playerName+"'.");
		}
		return startingPlanet;
	}
	
	public int getDate()
	{
		if (gameBoard == null) return -1;		
		return gameBoard.getDate();
	}
	
	public void checkBuilding(String celestialBodyName, Class<? extends IBuilding> buildingType, int buildSlotsCount)
	{
		if (gameBoard == null) fail("Test code error: Gameboard not refreshed yet.");
		
		IBuilding building;
		try
		{
			building = gameBoard.getBuilding(celestialBodyName, buildingType);
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
	
	public void checkFleetNotMoved(String fleetName, int expectedQt)
	{
		checkFleetMove(fleetName, expectedQt, null, null, -1);
	}
	public double checkFleetMove(String fleetName, int expectedQt, String sourceCelestialBody, String destinationCelestialBody, int departureDate)
	{
		if (gameBoard == null) fail("Test code error: Gameboard not refreshed yet.");
		
		double expectedMoved = 0;
		
		try
		{
			Fleet f = gameBoard.getUnit(playerName, fleetName, Fleet.class);
			if (f.getTotalQt() != expectedQt) fail("Unexpected starships quantity.");
			
			expectedMoved = (departureDate < 0 || sourceCelestialBody == null || destinationCelestialBody == null) ? 0 : shouldMove(sourceCelestialBody, destinationCelestialBody, departureDate, f.getSpeed(), getDate()-1);
			
			if (!previousGameBoards.isEmpty())
			{
				try
				{
					Fleet pf = previousGameBoards.lastElement().getUnit(playerName, fleetName, Fleet.class);
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
		if (gameBoard == null) fail("Test code error: Gameboard not refreshed yet.");
		
		Diplomacy expectedDiplomacy = new Diplomacy(true, gameBoard.getDate(), playerName, diplomacy);
		assertEquals(expectedDiplomacy, gameBoard.getPlayersPolicies().get(playerName));
	}
	
	public double shouldMove(String sourceCelestialBody, String destinationCelestialBody, int departureDate, double speed, int date)
	{
		if (gameBoard == null) fail("Test code error: Gameboard not refreshed yet.");
		
		try
		{
			RealLocation source = gameBoard.getCelestialBodyLocation(sourceCelestialBody);
			RealLocation destination = gameBoard.getCelestialBodyLocation(destinationCelestialBody);			
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
		if (gameBoard == null) fail("Test code error: Gameboard not refreshed yet.");
		
		Fleet f = gameBoard.getUnit(playerName, fleetName, Fleet.class);
		
		RealLocation fleetLoc = gameBoard.getUnitLocation(playerName, fleetName);
		RealLocation expectedLoc = gameBoard.getCelestialBodyLocation(expectedLocationName);
		
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
		if (gameBoard == null) fail("Test code error: Gameboard not refreshed yet.");
		
		try
		{
			Fleet f = gameBoard.getUnit(playerName, fleetName, Fleet.class);			
			
			RealLocation fleetLoc = gameBoard.getUnitLocation(playerName, fleetName);
			RealLocation expectedLoc = gameBoard.getCelestialBodyLocation(expectedLocationName);
			
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
		if (gameBoard == null) fail("Test code error: Gameboard not refreshed yet.");
		
		try
		{
			ProductiveCelestialBody productiveCelestialBody = gameBoard.getCelestialBody(celestialBodyName, ProductiveCelestialBody.class);			
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
		if (gameBoard == null) fail("Test code error: Gameboard not refreshed yet.");
		
		Fleet currentFleet = null;
		try
		{
			currentFleet = gameBoard.getUnit(playerName, fleetName, Fleet.class);
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
		if (gameBoard == null) fail("Test code error: Gameboard not refreshed yet.");
		
		try
		{
			assertFalse("Unexpected result", gameBoard.getCelestialBody(celestialBodyName).isVisible());
		}
		catch(PlayerGameBoardQueryException e)
		{
			fail("Cannot locate productive celestial body '"+celestialBodyName+"'");
		}
	}
}
