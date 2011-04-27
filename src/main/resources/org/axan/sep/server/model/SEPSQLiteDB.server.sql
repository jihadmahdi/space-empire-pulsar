-- Database Section
-- ________________ 


-- Tables Section
-- _____________ 

CREATE TABLE Area (
     isSun BOOL NOT NULL,
     location_x INTEGER NOT NULL,
     location_y INTEGER NOT NULL,
     location_z INTEGER NOT NULL,
     CONSTRAINT PKArea PRIMARY KEY (location_x, location_y, location_z),
     CHECK(location_x >= 0 AND location_y >= 0 AND location_z >= 0)
);
--comment on table Area is 'An area is a (x;y;z) location AND volume in the universe. It can be occupied by one celestial body x-or a part of a sun.';

CREATE TABLE Player (
     name TEXT NOT NULL,
     CONSTRAINT PKPlayer PRIMARY KEY (name)
--     CHECK(EXISTS(SELECT * FROM Government
--                  WHERE Government.owner = name)) 
 --,
--     CHECK(EXISTS(SELECT * FROM PlayerConfig
--                  WHERE PlayerConfig.name = name)) 
);

CREATE TABLE PlayerConfig (
     name TEXT NOT NULL,
     color TEXT NOT NULL,
     symbol BLOB NULL,  	-- TODO: Change to NOT NULL   
     portrait BLOB NULL,	-- TODO: Change to NOT NULL
     CONSTRAINT PKPlayerConfig PRIMARY KEY (name),
     CONSTRAINT FKPlayerConfig FOREIGN KEY (name) REFERENCES Player
);

CREATE TABLE Unit (
     owner TEXT NOT NULL,
     name TEXT NOT NULL,
     type TEXT NOT NULL,
     CHECK(type IN ('PulsarMissile', 'Probe', 'AntiProbeMissile', 'Fleet', 'CarbonCarrier', 'SpaceRoadDeliverer')),
     CONSTRAINT PKUnit PRIMARY KEY (owner, name),
     UNIQUE (owner, name, type),     
     CONSTRAINT FKUnitOwner FOREIGN KEY (owner) REFERENCES Player
);

CREATE TABLE CelestialBody (
     name TEXT NOT NULL,
     location_x INTEGER NOT NULL,
     location_y INTEGER NOT NULL,
     location_z INTEGER NOT NULL,
     type TEXT NOT NULL,
     CHECK(type IN ('Vortex', 'Planet', 'AsteroidField', 'Nebula')),
     CONSTRAINT PKCelestialBody PRIMARY KEY (name),
     UNIQUE (name, type),
     CONSTRAINT UCelestialBodyArea UNIQUE (location_x, location_y, location_z),     
     CONSTRAINT FKCelestialBodyLocation FOREIGN KEY (location_x, location_y, location_z) REFERENCES Area
);
--comment on table CelestialBody is 'A celestial body occupy one area in the universe. There are several celestial body types.';

CREATE TABLE ProductiveCelestialBody (
     name TEXT NOT NULL,
     type TEXT NOT NULL,
     initialCarbonStock INTEGER NOT NULL,
     maxSlots INTEGER NOT NULL,
     CHECK(type IN ('Nebula', 'AsteroidField', 'Planet')),
     CONSTRAINT PKProductiveCelestialBody PRIMARY KEY (name),
     UNIQUE (name, type),
     CONSTRAINT FKProductiveCelestialBodyISACelestialBody FOREIGN KEY (name, type) REFERENCES CelestialBody (name, type)
);

CREATE TABLE Nebula (
     name TEXT NOT NULL,
     type TEXT NOT NULL,
     CHECK(type = 'Nebula'),
     CONSTRAINT PKNebula PRIMARY KEY (name),
     UNIQUE (name, type),
     CONSTRAINT FKNebulaISAProductiveCelestialBody FOREIGN KEY (name, type) REFERENCES ProductiveCelestialBody (name, type)
);

CREATE TABLE AsteroidField (
     name TEXT NOT NULL,
     type TEXT NOT NULL,
     CHECK(type = 'AsteroidField'),
     CONSTRAINT PKAsteroidField PRIMARY KEY (name),
     UNIQUE (name, type),
     CONSTRAINT FKAsteroidFieldISAProductiveCelestialBody FOREIGN KEY (name, type) REFERENCES ProductiveCelestialBody (name, type)
);

CREATE TABLE Planet (
     name TEXT NOT NULL,
     populationPerTurn INTEGER NOT NULL,
     maxPopulation INTEGER NOT NULL,
     type TEXT NOT NULL,
     CHECK(type = 'Planet'),
     CONSTRAINT PKPlanet PRIMARY KEY (name),
     UNIQUE (name, type),
     CONSTRAINT FKPlanetISAProductiveCelestialBody FOREIGN KEY (name, type) REFERENCES ProductiveCelestialBody (name, type)
);

CREATE TABLE VersionedProductiveCelestialBody (
     name TEXT NOT NULL,
     turn INTEGER NOT NULL,
     carbonStock INTEGER NOT NULL,
     currentCarbon INTEGER NOT NULL,
     owner TEXT,
     type TEXT NOT NULL, -- CHECK not mandatory as foreign key is already checked in foreign table.
     CHECK(turn >= 0),
     CONSTRAINT PKVersionedProductiveCelestialBody PRIMARY KEY (name, turn),
     UNIQUE (name, turn, type),
     CONSTRAINT FKVersionedProductiveCelestialBodyISAProductiveCelestialBody FOREIGN KEY (name, type) REFERENCES ProductiveCelestialBody (name, type),   
     CONSTRAINT FKVersionedProductiveCelestialBodyOwner FOREIGN KEY (owner) REFERENCES Player
);
     
CREATE TABLE Building (
     type TEXT NOT NULL,
     nbSlots INTEGER NOT NULL,
     celestialBodyName TEXT NOT NULL,
     turn INTEGER NOT NULL,
     CHECK(type IN ('PulsarLaunchingPad', 'SpaceCounter', 'GovernmentModule', 'DefenseModule', 'StarshipPlant', 'ExtractionModule')),
     CONSTRAINT PKBuilding PRIMARY KEY (type, celestialBodyName, turn),
     CONSTRAINT FKBuildingLocation FOREIGN KEY (celestialBodyName, turn) REFERENCES VersionedProductiveCelestialBody     
);
--comment on table Building is 'A building occupy a building slot on a productive celestial body. There are several building types.';

CREATE TABLE SpaceCounter (
     type TEXT NOT NULL,
     celestialBodyName TEXT NOT NULL,
     turn INTEGER NOT NULL,
     CHECK(type = 'SpaceCounter'),
     CONSTRAINT PKSpaceCounter PRIMARY KEY (type, celestialBodyName, turn),
     CONSTRAINT FKSpaceCounterISABuilding FOREIGN KEY (type, celestialBodyName, turn) REFERENCES Building
);

CREATE TABLE CarbonCarrier (
     owner TEXT NOT NULL,
     name TEXT NOT NULL,
     sourceType TEXT NOT NULL,
     sourceCelestialBodyName TEXT NOT NULL,
     sourceTurn INTEGER NOT NULL,
     type TEXT NOT NULL,
     CHECK(type = 'CarbonCarrier'),
     CHECK(sourceType='SpaceCounter'),
     CONSTRAINT PKCarbonCarrier PRIMARY KEY (owner, name),
     UNIQUE (owner, name, type),
     CONSTRAINT FKCarbonCarrierISAUnit FOREIGN KEY (owner, name, type) REFERENCES Unit (owner, name, type),
     CONSTRAINT FKCarbonCarrierSource FOREIGN KEY (sourceType, sourceCelestialBodyName, sourceTurn) REFERENCES SpaceCounter
);

CREATE TABLE CarbonOrder (
     source TEXT NOT NULL,
     owner TEXT NOT NULL,
     priority INTEGER NOT NULL,
     amount INTEGER NOT NULL,
     destination TEXT NOT NULL,
     CHECK(source != destination AND amount > 0 AND priority >= 0),
     CONSTRAINT PKCarbonOrder PRIMARY KEY (owner, source, priority),
     CONSTRAINT FKCarbonOrderOwner FOREIGN KEY (owner) REFERENCES Player,
     CONSTRAINT FKCarbonOrderSource FOREIGN KEY (source) REFERENCES ProductiveCelestialBody,
     CONSTRAINT FKCarbonOrderDestination FOREIGN KEY (destination) REFERENCES ProductiveCelestialBody
);

CREATE TABLE DefenseModule (
     type TEXT NOT NULL,
     celestialBodyName TEXT NOT NULL,
     turn INTEGER NOT NULL,
     CHECK(type = 'DefenseModule'),
     CONSTRAINT PKDefenseModule PRIMARY KEY (type, celestialBodyName, turn),
     CONSTRAINT FKDefenseModuleISABuilding FOREIGN KEY (type, celestialBodyName, turn) REFERENCES Building
);

CREATE TABLE Fleet (
     owner TEXT NOT NULL,
     name TEXT NOT NULL,
     type TEXT NOT NULL,
     CHECK(type = 'Fleet'),
     CONSTRAINT PKFleet PRIMARY KEY (owner, name),
     UNIQUE (owner, name, type),
     CONSTRAINT FKFleetISAUnit FOREIGN KEY (owner, name, type) REFERENCES Unit (owner, name, type)
);

CREATE TABLE SpecialUnit (
     owner TEXT NOT NULL,
     name TEXT NOT NULL,
     type TEXT NOT NULL,
     CHECK(type IN ('Hero')), -- Waiting for other child tables.
     CONSTRAINT PKSpecialUnit PRIMARY KEY (owner, name),
     UNIQUE (owner, name, type),
     CONSTRAINT FKSpecialUnitOwner FOREIGN KEY (owner) REFERENCES Player
);

CREATE TABLE VersionedUnit (
     turn INTEGER NOT NULL,
     owner TEXT NOT NULL,
     name TEXT NOT NULL,
     type TEXT NOT NULL,
     departure_x INTEGER NOT NULL,
     departure_y INTEGER NOT NULL,
     departure_z INTEGER NOT NULL,
     progress FLOAT NOT NULL DEFAULT 0.0,
     -- destination_xyz are redundant with unit-specific move (probe destination, fleet move plan, carbon carrier order) so they must be maintained consistent.
     -- unit-specific move representation should be maintained to enforce types relationship (ie: fleet cannot move to an empty area).
     destination_x INTEGER NULL,
     destination_y INTEGER NULL,
     destination_z INTEGER NULL,
	 CHECK(turn >= 0),
	 CHECK((destination_x is NOT NULL AND destination_y is NOT NULL AND destination_z is NOT NULL)
           OR (destination_x IS NULL AND destination_y IS NULL AND destination_z IS NULL)),
	 CHECK(progress >= 0 AND progress < 100),
     CONSTRAINT PKVersionedUnit PRIMARY KEY (owner, name, turn),
     UNIQUE (owner, name, turn, type),
     CONSTRAINT FKVersionedUnitISAUnit FOREIGN KEY (owner, name, type) REFERENCES Unit (owner, name, type),
     CONSTRAINT FKVersionedUnitDeparture FOREIGN KEY (departure_x, departure_y, departure_z) REFERENCES Area,
     CONSTRAINT FKVersionedUnitDestination FOREIGN KEY (destination_x, destination_y, destination_z) REFERENCES Area
);
     
CREATE TABLE VersionedFleet (
     owner TEXT NOT NULL,
     name TEXT NOT NULL,
     turn INTEGER NOT NULL,
     type TEXT NOT NULL,
     CONSTRAINT PKVersionedFleet PRIMARY KEY (owner, name, turn),
     UNIQUE (owner, name, turn, type),
--     CHECK(EXISTS(SELECT * FROM FleetComposition
--                  WHERE FleetComposition.fleetOwner = owner AND FleetComposition.fleetName = name AND FleetComposition.fleetTurn = turn)),
     CONSTRAINT FKVersionedFleetISAVersionedUnit FOREIGN KEY (owner, name, turn, type) REFERENCES VersionedUnit (owner, name, turn, type),
     CONSTRAINT FKVersionedFleetISAFleet FOREIGN KEY (owner, name, type) REFERENCES Fleet (owner, name, type)
);

CREATE TABLE VersionedSpecialUnit (
     owner TEXT NOT NULL,
     name TEXT NOT NULL,
     turn INTEGER NOT NULL,
     fleetOwner TEXT NOT NULL,
     fleetName TEXT NOT NULL,
     fleetTurn INTEGER NOT NULL,
     type TEXT NOT NULL,
     CONSTRAINT PKVersionedSpecialUnit PRIMARY KEY (owner, name, turn),
     UNIQUE (owner, name, turn, type),
     CONSTRAINT FKVersionedSpecialUnitVersionedUnit FOREIGN KEY (owner, name, type) REFERENCES SpecialUnit (owner, name, type),
     CONSTRAINT FKVersionedSpecialUnitVersionedFleet FOREIGN KEY (fleetOwner, fleetName, fleetTurn) REFERENCES VersionedFleet
);

CREATE TABLE Probe (
     owner TEXT NOT NULL,
     name TEXT NOT NULL,
     type TEXT NOT NULL,
     CHECK(type = 'Probe'),
--     deployed BOOL NOT NULL,
     CONSTRAINT PKProbe PRIMARY KEY (owner, name),
     UNIQUE (owner, name, type),
     CONSTRAINT FKProbeISAUnit FOREIGN KEY (owner, name, type) REFERENCES Unit (owner, name, type)
);

CREATE TABLE PulsarLaunchingPad (
     type TEXT NOT NULL,
     celestialBodyName TEXT NOT NULL,
     turn INTEGER NOT NULL,
     firedDate INTEGER NOT NULL,
     CHECK(type = 'PulsarLaunchingPad'),
     CONSTRAINT PKPulsarLaunchingPad PRIMARY KEY (type, celestialBodyName, turn),
     CONSTRAINT FKPulsarLaunchingPadISABuilding FOREIGN KEY (type, celestialBodyName, turn) REFERENCES Building
);

CREATE TABLE PulsarMissile (
     owner TEXT NOT NULL,
     name TEXT NOT NULL,
     time INTEGER NOT NULL,
     volume INTEGER NOT NULL,
     type TEXT NOT NULL,
     CHECK(type = 'PulsarMissile'),
     CONSTRAINT PKPulsarMissile PRIMARY KEY (owner, name),
     UNIQUE (owner, name, type),
     CONSTRAINT FKPulsarMissileISAUnit FOREIGN KEY (owner, name, type) REFERENCES Unit (owner, name, type)
);

CREATE TABLE VersionedProbe (
     owner TEXT NOT NULL,
     name TEXT NOT NULL,
     turn INTEGER NOT NULL,
     destination_x INTEGER,
     destination_y INTEGER,
     destination_z INTEGER,
     type TEXT NOT NULL,
     CHECK((destination_x is NOT NULL AND destination_y is NOT NULL AND destination_z is NOT NULL)
           OR (destination_x IS NULL AND destination_y IS NULL AND destination_z IS NULL)),
     CONSTRAINT PKVersionedProbe PRIMARY KEY (owner, name, turn),
     UNIQUE (owner, name, turn, type),
     CONSTRAINT FKVersionedProbeISAVersionedUnit FOREIGN KEY (owner, name, turn, type) REFERENCES VersionedUnit (owner, name, turn, type),
     CONSTRAINT FKVersionedProbeISAProbe FOREIGN KEY (owner, name, type) REFERENCES Probe (owner, name, type),
     CONSTRAINT FKVersionedProbeDestination FOREIGN KEY (destination_x, destination_y, destination_z) REFERENCES Area     
);

CREATE TABLE ExtractionModule (
     type TEXT NOT NULL,
     celestialBodyName TEXT NOT NULL,
     turn INTEGER NOT NULL,
     CHECK(type = 'ExtractionModule'),
     CONSTRAINT PKExtractionModule PRIMARY KEY (type, celestialBodyName, turn),
     CONSTRAINT FKExtractionModuleISABuilding FOREIGN KEY (type, celestialBodyName, turn) REFERENCES Building
);

CREATE TABLE GameConfig (
     key TEXT NOT NULL,
     value TEXT NOT NULL,
     CONSTRAINT PKGameConfig PRIMARY KEY (key)
);

CREATE TABLE GovernmentModule (
     type TEXT NOT NULL,
     celestialBodyName TEXT NOT NULL,
     turn INTEGER NOT NULL,
     CHECK(type = 'GovernmentModule'),
     CONSTRAINT PKGovernmentModule PRIMARY KEY (type, celestialBodyName, turn),
     CONSTRAINT FKGovernmentModuleISABuilding FOREIGN KEY (type, celestialBodyName, turn) REFERENCES Building
);

CREATE TABLE Hero (
     owner TEXT NOT NULL,
     name TEXT NOT NULL,
     experience INTEGER NOT NULL,
     type TEXT NOT NULL,
     CHECK(type = 'Hero'),
     CHECK(experience >= 0),
     CONSTRAINT PKHero PRIMARY KEY (owner, name),
     UNIQUE (owner, name, type),
     CONSTRAINT FKHeroISASpecialUnit FOREIGN KEY (owner, name, type) REFERENCES SpecialUnit (owner, name, type)
);

CREATE TABLE MovePlan (
     owner TEXT NOT NULL,
     name TEXT NOT NULL,
     turn INTEGER NOT NULL,
     priority INTEGER NOT NULL,
     delay INTEGER NOT NULL,
     attack BOOL NOT NULL,
     destination TEXT NOT NULL,
     CHECK(priority >= 0 AND delay >= 0),
     CONSTRAINT PKMovePlan PRIMARY KEY (owner, name, turn, priority),
     CONSTRAINT FKMovePlanVersionedFleet FOREIGN KEY (owner, name, turn) REFERENCES VersionedFleet,
     CONSTRAINT FKMovePlanCelestialBody FOREIGN KEY (destination) REFERENCES CelestialBody
);

CREATE TABLE VersionedCarbonCarrier (
     owner TEXT NOT NULL,
     name TEXT NOT NULL,
     turn INTEGER NOT NULL,
     orderOwner TEXT NOT NULL,
     orderSource TEXT NOT NULL,
     orderPriority INTEGER NOT NULL,
     type TEXT NOT NULL,
     CONSTRAINT PKVersionedCarbonCarrier PRIMARY KEY (owner, name, turn),
     UNIQUE (owner, name, turn, type),
     CONSTRAINT FKVersionedCarbonCarrierISAVersionedUnit FOREIGN KEY (owner, name, turn, type) REFERENCES VersionedUnit (owner, name, turn, type),
     CONSTRAINT FKVersionedCarbonCarrierISACarbonCarrier FOREIGN KEY (owner, name, type) REFERENCES CarbonCarrier (owner, name, type),
     CONSTRAINT FKVersionedCarbonCarrierCarbonOrder FOREIGN KEY (orderOwner, orderSource, orderPriority) REFERENCES CarbonOrder
);

CREATE TABLE VersionedNebula (
     name TEXT NOT NULL,
     turn INTEGER NOT NULL,
     type TEXT NOT NULL,
     CONSTRAINT PKVersionedNebula PRIMARY KEY (name, turn),
     UNIQUE (name, turn, type),
     CONSTRAINT FKVersionedNebulaISAVersionedProductiveCelestialBody FOREIGN KEY (name, turn, type) REFERENCES VersionedProductiveCelestialBody (name, turn, type),
     CONSTRAINT FKVersionedNebulaISANebula FOREIGN KEY (name, type) REFERENCES Nebula (name, type)
);

CREATE TABLE VersionedAsteroidField (
     name TEXT NOT NULL,
     turn INTEGER NOT NULL,
     type TEXT NOT NULL,
     CONSTRAINT PKVersionedAsteroidField PRIMARY KEY (name, turn),
     UNIQUE (name, turn, type),
     CONSTRAINT FKVersionedAsteroidFieldISAVersionedProductiveCelestialBody FOREIGN KEY (name, turn, type) REFERENCES VersionedProductiveCelestialBody (name, turn, type),
     CONSTRAINT FKVersionedAsteroidFieldISAAsteroidField FOREIGN KEY (name, type) REFERENCES AsteroidField (name, type)
);

CREATE TABLE VersionedPlanet (
     name TEXT NOT NULL,
     turn INTEGER NOT NULL,
     currentPopulation INTEGER NOT NULL,
     type TEXT NOT NULL,
     PRIMARY KEY (name, turn),
     UNIQUE (name, turn, type),
     CONSTRAINT FKVersionedPlanetISAVersionedProductiveCelestialBody FOREIGN KEY (name, turn, type) REFERENCES VersionedProductiveCelestialBody (name, turn, type),
     CONSTRAINT FKVersionedPlanetISAPlanet FOREIGN KEY (name, type) REFERENCES Planet (name, type)
);

CREATE TABLE SpaceRoad (
     spaceCounterAType TEXT NOT NULL,
     spaceCounterACelestialBodyName TEXT NOT NULL,
     spaceCounterATurn INTEGER NOT NULL,
     spaceCounterBType TEXT NOT NULL,
     spaceCounterBCelestialBodyName TEXT NOT NULL,
     spaceCounterBTurn INTEGER NOT NULL,
     CHECK(spaceCounterAType = 'SpaceCounter' AND spaceCounterBType = 'SpaceCounter'),
     CHECK(spaceCounterACelestialBodyName != spaceCounterBCelestialBodyName),
     FOREIGN KEY (spaceCounterAType, spaceCounterACelestialBodyName, spaceCounterATurn) REFERENCES SpaceCounter,
     FOREIGN KEY (spaceCounterBType, spaceCounterBCelestialBodyName, spaceCounterBTurn) REFERENCES SpaceCounter
);

CREATE TABLE SpaceRoadDeliverer (
     owner TEXT NOT NULL,
     name TEXT NOT NULL,
     sourceType TEXT NOT NULL,
     sourceCelestialBodyName TEXT NOT NULL,
     sourceTurn INTEGER NOT NULL,
     destinationType TEXT NOT NULL,
     destinationCelestialBodyName TEXT NOT NULL,
     destinationTurn INTEGER NOT NULL,
     type TEXT NOT NULL,
     CHECK(type = 'SpaceRoadDeliverer'),
     CHECK(sourceType='SpaceRoad' AND destinationType='SpaceRoad'),
     CHECK(sourceCelestialBodyName != destinationCelestialBodyName),
     PRIMARY KEY (owner, name),
     UNIQUE (owner, name, type),
     FOREIGN KEY (owner, name, type) REFERENCES Unit (owner, name, type),
     FOREIGN KEY (sourceType, sourceCelestialBodyName, sourceTurn) REFERENCES SpaceCounter,
     FOREIGN KEY (destinationType, destinationCelestialBodyName, destinationTurn) REFERENCES SpaceCounter
);

CREATE TABLE StarshipPlant (
     type TEXT NOT NULL,
     celestialBodyName TEXT NOT NULL,
     turn INTEGER NOT NULL,
     CHECK(type = 'StarshipPlant'),
     PRIMARY KEY (type, celestialBodyName, turn),
     FOREIGN KEY (type, celestialBodyName, turn) REFERENCES Building
);

CREATE TABLE StarshipTemplate (
     name TEXT NOT NULL,
     specializedClass TEXT NOT NULL,
     CHECK(specializedClass IN ('ARTILLERY', 'DESTROYER', 'FIGHTER')),
     PRIMARY KEY (name)
);

CREATE TABLE Government (
     owner TEXT NOT NULL,
     turn INTEGER NOT NULL,
     fleetName text,
     fleetTurn INTEGER,
     planetName text,
     planetTurn INTEGER,
     PRIMARY KEY (owner, turn),
     CHECK(turn >= 0),
     CHECK((fleetTurn IS NOT NULL AND fleetTurn = turn AND fleetName IS NOT NULL AND planetTurn IS NULL AND planetName IS NULL)
     	   OR (planetTurn IS NOT NULL AND planetTurn = turn AND planetName IS NOT NULL AND fleetTurn IS NULL AND fleetName IS NULL)),     
     FOREIGN KEY (owner) REFERENCES Player,	   	
     FOREIGN KEY (owner, fleetName, fleetTurn) REFERENCES VersionedFleet,     
     FOREIGN KEY (planetName, planetTurn) REFERENCES VersionedPlanet
);

CREATE TABLE AntiProbeMissile (
     owner TEXT NOT NULL,
     name TEXT NOT NULL,
     type TEXT NOT NULL,
     CHECK(type = 'AntiProbeMissile'),
     PRIMARY KEY (owner, name),
     UNIQUE (owner, name, type),
     FOREIGN KEY (owner, name, type) REFERENCES Unit (owner, name, type)
);

CREATE TABLE VersionedSpaceRoadDeliverer (
     owner TEXT NOT NULL,
     name TEXT NOT NULL,
     turn INTEGER NOT NULL,
     type TEXT NOT NULL,
     PRIMARY KEY (owner, name, turn),
     UNIQUE (owner, name, turn, type),
     FOREIGN KEY (owner, name, turn, type) REFERENCES VersionedUnit (owner, name, turn, type),
     FOREIGN KEY (owner, name, type) REFERENCES SpaceRoadDeliverer (owner, name, type)
);     

CREATE TABLE VersionedDiplomacy (
     cible TEXT NOT NULL,
     name TEXT NOT NULL,
     turn INTEGER NOT NULL,
     allowToLand BOOL NOT NULL,
     foreignPolicy TEXT NOT NULL,
     CHECK(foreignPolicy IN ('NEUTRAL', 'HOSTILE', 'HOSTILE_IF_OWNER')),
     CHECK(turn >= 0),
     PRIMARY KEY (name, cible, turn),
     FOREIGN KEY (name) REFERENCES Player,
     FOREIGN KEY (cible) REFERENCES Player
);

CREATE TABLE Vortex (
     name TEXT NOT NULL,
     onsetDate INTEGER NOT NULL,
     endDate INTEGER NOT NULL,
     destination TEXT NOT NULL,
     type TEXT NOT NULL,
     CHECK(type = 'Vortex'),
     CHECK(onsetDate >= 0 AND endDate > onsetDate),
     PRIMARY KEY (name),
     UNIQUE (name, type),
     FOREIGN KEY (name, type) REFERENCES CelestialBody (name, type),
     FOREIGN KEY (destination) REFERENCES CelestialBody
);        
  
CREATE TABLE VersionedAntiProbeMissile (
     owner TEXT NOT NULL,
     name TEXT NOT NULL,
     turn INTEGER NOT NULL,
     targetOwner text,
     targetName text,
     targetTurn INTEGER,
     type TEXT NOT NULL,
     CHECK((targetOwner IS NOT NULL AND targetName IS NOT NULL AND targetTurn IS NOT NULL)
           OR (targetOwner IS NULL AND targetName IS NULL AND targetTurn IS NULL)),
     PRIMARY KEY (owner, name, turn),
     UNIQUE (owner, name, turn, type),
     FOREIGN KEY (owner, name, turn, type) REFERENCES VersionedUnit (owner, name, turn, type),
     FOREIGN KEY (owner, name, type) REFERENCES AntiProbeMissile (owner, name, type),
     FOREIGN KEY (targetOwner, targetName, targetTurn) REFERENCES VersionedProbe
);

CREATE TABLE VersionedPulsarMissile (
     owner TEXT NOT NULL,
     name TEXT NOT NULL,
     turn INTEGER NOT NULL,
     direction_x INTEGER,
     direction_y INTEGER,
     direction_z INTEGER,
     type TEXT NOT NULL,
     CHECK((direction_x IS NOT NULL AND direction_y IS NOT NULL AND direction_z IS NOT NULL)
           OR (direction_x IS NULL AND direction_y IS NULL AND direction_z IS NULL)),
     PRIMARY KEY (owner, name, turn),
     UNIQUE (owner, name, turn, type),
     FOREIGN KEY (owner, name, turn, type) REFERENCES VersionedUnit (owner, name, turn, type),
     FOREIGN KEY (owner, name, type) REFERENCES PulsarMissile (owner, name, type),
     FOREIGN KEY (direction_x, direction_y, direction_z) REFERENCES Area
);

CREATE TABLE AssignedFleet (
     celestialBody TEXT NOT NULL,
     owner TEXT NOT NULL,
     fleetName TEXT NOT NULL, -- If fleetName were NULL there actually would be no row at all.  
     PRIMARY KEY (celestialBody, owner),
     FOREIGN KEY (celestialBody) REFERENCES CelestialBody,
     FOREIGN KEY (owner, fleetName) REFERENCES Fleet,
     FOREIGN KEY (owner) REFERENCES Player
);

CREATE TABLE FleetComposition (
     fleetOwner TEXT NOT NULL,
     fleetName TEXT NOT NULL,
     fleetTurn INTEGER NOT NULL,
     starshipTemplate TEXT NOT NULL,
     quantity INTEGER NOT NULL,
     CHECK(quantity >= 0)
     PRIMARY KEY (fleetOwner, fleetName, fleetTurn, starshipTemplate),
     FOREIGN KEY (starshipTemplate) REFERENCES StarshipTemplate,
     FOREIGN KEY (fleetOwner, fleetName, fleetTurn) REFERENCES VersionedFleet
);

CREATE TABLE UnitEncounterLog (
	unitOwner TEXT NOT NULL,
	unitName TEXT NOT NULL,
	unitTurn INTEGER NOT NULL,
	unitType TEXT NOT NULL,
	seenOwner TEXT NOT NULL,
	seenName TEXT NOT NULL,
	seenTurn INTEGER NOT NULL,
	seenType TEXT NOT NULL,
	instantTime INTEGER NOT NULL,
	PRIMARY KEY (unitOwner, unitName, unitTurn, unitType, instantTime),
	FOREIGN KEY (unitOwner, unitName, unitTurn, unitType) REFERENCES VersionedUnit(owner, name, turn, type),
	FOREIGN KEY (seenOwner, seenName, seenTurn, seenType) REFERENCES VersionedUnit(owner, name, turn, type)
);

CREATE TABLE UnitArrivalLog (
	unitOwner TEXT NOT NULL,
	unitName TEXT NOT NULL,
	unitTurn INTEGER NOT NULL,
	unitType TEXT NOT NULL,
	instantTime INTEGER NOT NULL,
	destination TEXT NOT NULL,
	vortex TEXT DEFAULT NULL,	
	PRIMARY KEY (unitOwner, unitName, unitTurn, unitType, instantTime),
	FOREIGN KEY (destination) REFERENCES ProductiveCelestialBody(name),
	FOREIGN KEY (vortex) REFERENCES Vortex(name)
);