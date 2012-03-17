package org.axan.sep.server.ai;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.axan.sep.client.SEPClient;
import org.axan.sep.common.PlayerGameBoard;
import org.axan.sep.common.SEPUtils;
import org.axan.sep.common.Protocol.eBuildingType;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.SEPUtils.RealLocation;
import org.axan.sep.common.db.IBuilding;
import org.axan.sep.common.db.ICelestialBody;
import org.axan.sep.common.db.IDiplomacy;
import org.axan.sep.common.db.IFleet;
import org.axan.sep.common.db.IDiplomacyMarker.eForeignPolicy;
import org.axan.sep.common.db.IProductiveCelestialBody;
import org.axan.sep.common.db.orm.DiplomacyMarker;
import org.axan.sep.common.db.orm.SEPCommonDB;

import com.almworks.sqlite4java.SQLiteException;

public class ClientAI
{
	private final String playerName;
	private String startingPlanet;
	private final SEPClient client;
	private final boolean isClientTest;
	
	public ClientAI(String playerName, SEPClient client, boolean isClientTest)
	{
		this.playerName = playerName;
		this.client = client;
		this.isClientTest = isClientTest;
	}
	
	public SEPClient getClient()
	{
		return client;
	}
	
	public SEPCommonDB getDB()
	{
		SEPCommonDB sepDB = getGameBoard().getDB();
		if (sepDB == null) throw new IllegalStateException("Gameboard not refreshed yet.");
		return sepDB;
	}
	
	public PlayerGameBoard getGameBoard()
	{
		PlayerGameBoard gb = client == null ? null : client.getGameboard();
		if (gb == null) throw new IllegalStateException("Gameboard not refreshed yet.");
		return gb;
	}
	
	public String getStartingPlanetName()
	{
		if (startingPlanet == null)
		{			
			startingPlanet = getDB().getStartingPlanetName(playerName);
		}
		return startingPlanet;
	}
	
	public int getTurn()
	{
		SEPCommonDB sepDB = client == null ? null : client.getGameboard() == null ? null : client.getGameboard().getDB();
		if (sepDB == null) return -1;
		return sepDB.getTurn();
	}
	
	public void checkBuilding(String celestialBodyName, eBuildingType buildingType, int buildSlotsCount)
	{
		SEPCommonDB sepDB = getDB();
		
		IBuilding building = sepDB.getBuilding(celestialBodyName, buildingType);
				
		if (building == null && buildSlotsCount > 0)
		{
			fail("No building but "+buildSlotsCount+" expected.");
			return;
		}
		
		if (building != null && building.getNbSlots() != buildSlotsCount)
		{
			fail(buildSlotsCount+" building slots count expected but "+building.getNbSlots()+" found.");
			return;
		}
	}
	
	public void checkFleetNotMoved(String fleetName, int expectedQt, boolean checkPreviousGameBoard)
	{
		checkFleetMove(fleetName, expectedQt, null, null, -1, checkPreviousGameBoard);
	}
	
	public double checkFleetMove(String fleetName, int expectedQt, String sourceCelestialBody, String destinationCelestialBody, int departureDate, boolean checkPreviousGameBoard)
	{
		SEPCommonDB sepDB = getDB();
		if (sepDB.hasPrevious()) sepDB = sepDB.previous();		
		
		double expectedMoved = 0;
		
		IFleet f = sepDB.getFleet(playerName, fleetName);			
		assertNotNull("Cannot find fleet '"+playerName+"@"+fleetName+"' : Unknown unit '"+playerName+"@"+fleetName+"'", f);
		
		if (f.getStarshipsCount() != expectedQt) fail("Unexpected starships quantity.");
		
		expectedMoved = (departureDate < 0 || sourceCelestialBody == null || destinationCelestialBody == null) ? 0 : shouldMove(sourceCelestialBody, destinationCelestialBody, departureDate, f.getSpeed(), getTurn()-1);
		
		if (!checkPreviousGameBoard) return expectedMoved;
		
		IFleet pf = sepDB.hasPrevious() ? sepDB.previous().getFleet(playerName, fleetName) : null;
		
		if ((pf == null && (f.isStopped() != (expectedMoved == 0))) || (pf != null && (pf.getRealLocation().equals(f.getRealLocation()) == (expectedMoved > 0))))
		{
			//shouldMove(sourceCelestialBody, destinationCelestialBody, departureDate, f.getSpeed(), getDate()-1);
			System.err.println("checkFleetMove('"+fleetName+"', "+expectedQt+", '"+sourceCelestialBody+"', '"+destinationCelestialBody+"', "+departureDate+") ERROR");
			System.err.println("Fleet: "+f.toString());
			
			if (pf == null)
			{
				System.err.println("\tPrevious fleet == null");
			}
			else
			{
				System.err.println("\t-1 Departure: "+pf.getDeparture());
				System.err.println("\t-1 RealLocation: "+pf.getRealLocation());
				System.err.println("\t-1 Destination: "+pf.getDestination());
			}			
			
			int i = 0;
			while(sepDB != null && f != null)
			{
				f = sepDB.getFleet(playerName, fleetName);
				if (f == null) break;
				
				System.err.println("\t"+i+" Departure: "+f.getDeparture());
				System.err.println("\t"+i+" RealLocation: "+f.getRealLocation());
				System.err.println("\t"+i+" Destination: "+f.getDestination());
				
				sepDB = sepDB.next();
				++i;
			}
			
			System.err.println("ExpectedMoved : "+expectedMoved);
			
			expectedMoved = shouldMove(sourceCelestialBody, destinationCelestialBody, departureDate, f.getSpeed(), getTurn()-1);
			
			fail("Unexpected fleet move state ("+(expectedMoved<=0)+").");
		}
		
		return expectedMoved;
	}
	
	public double shouldMove(String sourceCelestialBody, String destinationCelestialBody, int departureDate, double speed, int date)
	{
		SEPCommonDB sepDB = getDB();
		
		RealLocation departure = sepDB.getCelestialBody(sourceCelestialBody).getLocation().asRealLocation();
		RealLocation destination = sepDB.getCelestialBody(destinationCelestialBody).getLocation().asRealLocation();
		double distance = SEPUtils.getDistance(departure, destination);
		double totalTime = distance / speed;
		
		// return ((date - departureDate) < totalTime); // No, because when delta is very low the unit may be already count in destination area.		
		double progress = (date - departureDate) / totalTime;
		boolean shouldMove = !SEPUtils.getMobileLocation(departure, destination, progress, true).asLocation().equals(destination);		
		return shouldMove ? totalTime : 0;
	}
	
	public void checkDiplomacy(String targetName, boolean isAllowedToLand, eForeignPolicy foreignPolicy)
	{
		SEPCommonDB sepDB = getDB();
		
		IDiplomacy d = sepDB.getPlayer(playerName).getDiplomacy(targetName);
		
		assertNotNull("Diplomacy is null", d);
		assertEquals(isAllowedToLand, d.isAllowedToLand());
		assertEquals(foreignPolicy, d.getForeignPolicy());
	}
	
	/*
	public boolean testFleetLocation(String fleetName, String expectedLocationName) throws PlayerGameBoardQueryException
	{
		SEPCommonDB sepDB = getDB();
		
		IFleet f = sepDB.getFleet(playerName, fleetName);
		
		RealLocation fleetLoc = f.getRealLocation();
		Location expectedLoc = sepDB.getCelestialBody(expectedLocationName).getLocation();
		
		return expectedLoc.equals(fleetLoc.asLocation());		
	}
	*/
	
	public IFleet checkFleetLocation(String fleetName, int expectedQt, String expectedLocationName)
	{
		IFleet f = checkFleetLocation(fleetName, expectedLocationName);
		if (f.getStarshipsCount() != expectedQt) fail("Unexpected starships quantity.");
		return f;
	}
	
	public IFleet checkFleetLocation(String fleetName, String expectedLocationName)
	{
		SEPCommonDB sepDB = getDB();
		
		IFleet f = sepDB.getFleet(playerName, fleetName);
		
		if (f == null) fail("Cannot find fleet : '"+playerName+"@"+fleetName+"'");
		RealLocation fleetLoc = f.getRealLocation();
		Location expectedLoc = sepDB.getCelestialBody(expectedLocationName).getLocation();
		
		assertEquals("Unexpected fleet location", expectedLoc, fleetLoc.asLocation());
		
		return f;
	}
	
	public boolean isCelestialBodyOwner(String celestialBodyName)
	{
		SEPCommonDB sepDB = getDB();
		IProductiveCelestialBody productiveCelestialBody = (IProductiveCelestialBody) sepDB.getCelestialBody(celestialBodyName);
		return sepDB.getArea(productiveCelestialBody.getLocation()).isVisible(playerName) && playerName.equals(productiveCelestialBody.getOwner());
	}
	
	public void checkFleetDestroyed(String fleetName)
	{
		SEPCommonDB sepDB = getDB();
		
		IFleet currentFleet = sepDB.getFleet(playerName, fleetName);
		if (currentFleet != null) fail("Fleet is not destroyed : "+currentFleet);
		
		while(sepDB.hasPrevious())
		{
			sepDB = sepDB.previous();
			currentFleet = sepDB.getFleet(playerName, fleetName);
			if (currentFleet != null) return;
		}
		
		fail("Fleet '"+fleetName+"' has never existed.");
	}
	
	public void checkInvisibleLocation(String celestialBodyName)
	{
		SEPCommonDB sepDB = getDB();
		
		assertFalse("Unexpected result", sepDB.getArea(sepDB.getCelestialBody(celestialBodyName).getLocation()).isVisible(playerName));
	}
}
