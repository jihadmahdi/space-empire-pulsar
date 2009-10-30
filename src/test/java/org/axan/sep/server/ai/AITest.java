package org.axan.sep.server.ai;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Map;
import java.util.Stack;

import org.axan.sep.common.Diplomacy;
import org.axan.sep.common.Fleet;
import org.axan.sep.common.IBuilding;
import org.axan.sep.common.ICelestialBody;
import org.axan.sep.common.Planet;
import org.axan.sep.common.PlayerGameBoard;
import org.axan.sep.common.SEPUtils;
import org.axan.sep.common.Diplomacy.PlayerPolicies;
import org.axan.sep.common.PlayerGameBoard.PlayerGameBoardQueryException;
import org.axan.sep.common.SEPUtils.RealLocation;

public class AITest
{
	private final Stack<PlayerGameBoard> previousGameBoards = new Stack<PlayerGameBoard>();
	private PlayerGameBoard gameBoard;
	private final String playerName;
	private String startingPlanet;
	
	public AITest(String playerName)
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
	
	public int getTurn()
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
		
		if (building == null && buildSlotsCount > 0) fail("No building but "+buildSlotsCount+" expected.");
		if (building != null && building.getBuildSlotsCount() != buildSlotsCount) fail(buildSlotsCount+" building slots count expected but "+building.getBuildSlotsCount()+" found.");
	}
	
	public void checkFleet(String fleetName, int expectedQt, boolean expectedMoved)
	{
		if (gameBoard == null) fail("Test code error: Gameboard not refreshed yet.");
		
		try
		{
			Fleet f = gameBoard.getUnit(playerName, fleetName, Fleet.class);
			if (f.getTotalQt() != expectedQt) fail("Unexpected starships quantity.");
			
			if (!previousGameBoards.isEmpty())
			{
				try
				{
					Fleet pf = previousGameBoards.lastElement().getUnit(playerName, fleetName, Fleet.class);
					if (pf.getCurrentLocation().equals(f.getCurrentLocation()) == expectedMoved) fail("Unexpected fleet move state ("+!expectedMoved+").");
				}
				catch(PlayerGameBoardQueryException e)
				{
					if (f.getSourceLocation().equals(f.getCurrentLocation()) == expectedMoved) fail("Unexpected fleet move state ("+!expectedMoved+").");
				}
			}
			else if (expectedMoved) fail("Fleet has just been created and not moved yet.");
		}
		catch(PlayerGameBoardQueryException e)
		{
			fail("Cannot find fleet '"+playerName+"@"+fleetName+"'.");
		}
	}

	public void checkDiplomacy(Map<String, PlayerPolicies> diplomacy)
	{
		if (gameBoard == null) fail("Test code error: Gameboard not refreshed yet.");
		
		Diplomacy expectedDiplomacy = new Diplomacy(true, gameBoard.getDate(), playerName, diplomacy);
		assertEquals(expectedDiplomacy, gameBoard.getPlayersPolicies().get(playerName));
	}
	
	public double getDistance(String sourceCelestialBody, String destinationCelestialBody)
	{
		if (gameBoard == null) fail("Test code error: Gameboard not refreshed yet.");
		
		try
		{
			RealLocation source = gameBoard.getCelestialBodyLocation(sourceCelestialBody);
			RealLocation destination = gameBoard.getCelestialBodyLocation(destinationCelestialBody);
			
			return SEPUtils.getDistance(source, destination);
		}
		catch(PlayerGameBoardQueryException e)
		{
			e.printStackTrace();
			fail(e.getMessage());
			return -1;
		}
	}
}
