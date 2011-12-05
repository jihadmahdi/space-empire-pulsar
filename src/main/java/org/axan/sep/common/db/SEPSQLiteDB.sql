-- Database Section
-- ________________ 


-- Tables Section
-- _____________ 

CREATE TABLE Area (
     location_x INTEGER NOT NULL,
     location_y INTEGER NOT NULL,
     location_z INTEGER NOT NULL,
     isSun BOOL NOT NULL,
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
     symbol TEXT NULL,  	-- TODO: Change to NOT NULL   
     portrait TEXT NULL,	-- TODO: Change to NOT NULL
     CONSTRAINT PKPlayerConfig PRIMARY KEY (name),
     CONSTRAINT FKPlayerConfig FOREIGN KEY (name) REFERENCES Player
);

CREATE TABLE Unit (	 
     -- constants
     owner TEXT NOT NULL,
     name TEXT NOT NULL,
     type TEXT NOT NULL,
     -- variables
     departure_x INTEGER NOT NULL,
     departure_y INTEGER NOT NULL,
     departure_z INTEGER NOT NULL,
     progress FLOAT NOT NULL DEFAULT 0.0,
     -- destination_xyz are redundant with unit-specific move (probe destination, fleet move plan, carbon carrier order) so they must be maintained consistent.
     -- unit-specific move representation should be maintained to enforce types relationship (i.e.: fleet cannot move to an empty area).
     destination_x INTEGER NULL,
     destination_y INTEGER NULL,
     destination_z INTEGER NULL,     
     CHECK(type IN ('PulsarMissile', 'Probe', 'AntiProbeMissile', 'Fleet', 'CarbonCarrier', 'SpaceRoadDeliverer')),
     CONSTRAINT PKUnit PRIMARY KEY (owner, name),
     UNIQUE (owner, name, type),     
     CONSTRAINT FKUnitOwner FOREIGN KEY (owner) REFERENCES Player,
     CHECK((destination_x is NOT NULL AND destination_y is NOT NULL AND destination_z is NOT NULL)
           OR (destination_x IS NULL AND destination_y IS NULL AND destination_z IS NULL)),
	 CHECK(progress >= 0 AND progress <= 100),
     CONSTRAINT FKVersionedUnitDeparture FOREIGN KEY (departure_x, departure_y, departure_z) REFERENCES Area,
     CONSTRAINT FKVersionedUnitDestination FOREIGN KEY (destination_x, destination_y, destination_z) REFERENCES Area     
);

CREATE TABLE CelestialBody (
     name TEXT NOT NULL,
     type TEXT NOT NULL,
     location_x INTEGER NOT NULL,
     location_y INTEGER NOT NULL,
     location_z INTEGER NOT NULL,
     CHECK(type IN ('Vortex', 'Planet', 'AsteroidField', 'Nebula')),
     CONSTRAINT PKCelestialBody PRIMARY KEY (name),
     UNIQUE (name, type),
     CONSTRAINT UCelestialBodyArea UNIQUE (location_x, location_y, location_z),     
     CONSTRAINT FKCelestialBodyLocation FOREIGN KEY (location_x, location_y, location_z) REFERENCES Area
);
--comment on table CelestialBody is 'A celestial body occupy one area in the universe. There are several celestial body types.';

CREATE TABLE ProductiveCelestialBody (
     -- constants
     name TEXT NOT NULL,
     type TEXT NOT NULL,
     initialCarbonStock INTEGER NOT NULL,
     maxSlots INTEGER NOT NULL,
     -- variables
     owner TEXT,
     carbonStock INTEGER NOT NULL,
     currentCarbon INTEGER NOT NULL,     
     CHECK(type IN ('Nebula', 'AsteroidField', 'Planet')),
     CONSTRAINT PKProductiveCelestialBody PRIMARY KEY (name),
     UNIQUE (name, type),
     CONSTRAINT FKProductiveCelestialBodyISACelestialBody FOREIGN KEY (name, type) REFERENCES CelestialBody (name, type),
     CONSTRAINT FKVersionedProductiveCelestialBodyOwner FOREIGN KEY (owner) REFERENCES Player
);

CREATE TABLE Nebula (
     -- constants 
     name TEXT NOT NULL,
     type TEXT NOT NULL,
     CHECK(type = 'Nebula'),
     CONSTRAINT PKNebula PRIMARY KEY (name),
     UNIQUE (name, type),
     CONSTRAINT FKNebulaISAProductiveCelestialBody FOREIGN KEY (name, type) REFERENCES ProductiveCelestialBody (name, type)
);

CREATE TABLE AsteroidField (
     -- constants
     name TEXT NOT NULL,
     type TEXT NOT NULL,
     CHECK(type = 'AsteroidField'),
     CONSTRAINT PKAsteroidField PRIMARY KEY (name),
     UNIQUE (name, type),
     CONSTRAINT FKAsteroidFieldISAProductiveCelestialBody FOREIGN KEY (name, type) REFERENCES ProductiveCelestialBody (name, type)
);

CREATE TABLE Planet (
     -- constants
     name TEXT NOT NULL,
     type TEXT NOT NULL,
     populationPerTurn INTEGER NOT NULL,
     maxPopulation INTEGER NOT NULL,
     -- variables
     currentPopulation INTEGER NOT NULL,         
     CHECK(type = 'Planet'),
     CONSTRAINT PKPlanet PRIMARY KEY (name),
     UNIQUE (name, type),
     CONSTRAINT FKPlanetISAProductiveCelestialBody FOREIGN KEY (name, type) REFERENCES ProductiveCelestialBody (name, type)
);
     
CREATE TABLE Building (
     type TEXT NOT NULL,
     celestialBodyName TEXT NOT NULL,
     turn INTEGER NOT NULL,
     nbSlots INTEGER NOT NULL,
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
     -- constants
     owner TEXT NOT NULL,
     name TEXT NOT NULL,
     type TEXT NOT NULL,
     sourceType TEXT NOT NULL,
     sourceCelestialBodyName TEXT NOT NULL,
     sourceTurn INTEGER NOT NULL,
     -- variables
     orderOwner TEXT NOT NULL,
     orderSource TEXT NOT NULL,
     orderPriority INTEGER NOT NULL,     
     CHECK(type = 'CarbonCarrier'),
     CHECK(sourceType='SpaceCounter'),
     CONSTRAINT PKCarbonCarrier PRIMARY KEY (owner, name),
     UNIQUE (owner, name, type),
     CONSTRAINT FKCarbonCarrierISAUnit FOREIGN KEY (owner, name, type) REFERENCES Unit (owner, name, type),
     CONSTRAINT FKCarbonCarrierSource FOREIGN KEY (sourceType, sourceCelestialBodyName, sourceTurn) REFERENCES SpaceCounter,
     CONSTRAINT FKCarbonCarrierCarbonOrder FOREIGN KEY (orderOwner, orderSource, orderPriority) REFERENCES CarbonOrder
);

CREATE TABLE CarbonOrder (
     owner TEXT NOT NULL,
     source TEXT NOT NULL,
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
     -- constants
     owner TEXT NOT NULL,
     name TEXT NOT NULL,
     type TEXT NOT NULL,
     CHECK(type = 'Fleet'),
     CONSTRAINT PKFleet PRIMARY KEY (owner, name),
     UNIQUE (owner, name, type),
     CONSTRAINT FKFleetISAUnit FOREIGN KEY (owner, name, type) REFERENCES Unit (owner, name, type),
     --     CHECK(EXISTS(SELECT * FROM FleetComposition
     --                  WHERE FleetComposition.fleetOwner = owner AND FleetComposition.fleetName = name AND FleetComposition.fleetTurn = turn)),
     CONSTRAINT FKFleetISAUnit FOREIGN KEY (owner, name, type) REFERENCES Unit (owner, name, type)     
);

CREATE TABLE SpecialUnit (
     -- constants
     owner TEXT NOT NULL,
     name TEXT NOT NULL,
     type TEXT NOT NULL,
     -- variables
     -- fleetOwner TEXT NOT NULL, // fleetOwner is SpecialUnit owner
     fleetName TEXT NOT NULL,     
     CHECK(type IN ('Hero')), -- Waiting for other child tables.
     CONSTRAINT PKSpecialUnit PRIMARY KEY (owner, name),
     UNIQUE (owner, name, type),
     CONSTRAINT FKSpecialUnitOwner FOREIGN KEY (owner) REFERENCES Player,
     CONSTRAINT FKSpecialUnitISAMemberOfFleet FOREIGN KEY (owner, fleetName) REFERENCES Fleet
);
     
CREATE TABLE Probe (
     -- constants
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
     -- constants
     owner TEXT NOT NULL,
     name TEXT NOT NULL,
     type TEXT NOT NULL,
     time INTEGER NOT NULL,
     volume INTEGER NOT NULL,
     -- variables
     direction_x INTEGER,
     direction_y INTEGER,
     direction_z INTEGER,
     CHECK((direction_x IS NOT NULL AND direction_y IS NOT NULL AND direction_z IS NOT NULL)
           OR (direction_x IS NULL AND direction_y IS NULL AND direction_z IS NULL)),
     FOREIGN KEY (direction_x, direction_y, direction_z) REFERENCES Area,
     CHECK(type = 'PulsarMissile'),
     CONSTRAINT PKPulsarMissile PRIMARY KEY (owner, name),
     UNIQUE (owner, name, type),
     CONSTRAINT FKPulsarMissileISAUnit FOREIGN KEY (owner, name, type) REFERENCES Unit (owner, name, type)
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
     type TEXT NOT NULL,
     experience INTEGER NOT NULL,
     CHECK(type = 'Hero'),
     CHECK(experience >= 0),
     CONSTRAINT PKHero PRIMARY KEY (owner, name),
     UNIQUE (owner, name, type),
     CONSTRAINT FKHeroISASpecialUnit FOREIGN KEY (owner, name, type) REFERENCES SpecialUnit (owner, name, type)
);

CREATE TABLE MovePlan (
     owner TEXT NOT NULL,
     name TEXT NOT NULL,
     priority INTEGER NOT NULL,
     delay INTEGER NOT NULL,
     attack BOOL NOT NULL,
     destination TEXT NOT NULL,
     CHECK(priority >= 0 AND delay >= 0),
     CONSTRAINT PKMovePlan PRIMARY KEY (owner, name, priority),
     CONSTRAINT FKMovePlanOfFleet FOREIGN KEY (owner, name) REFERENCES Fleet,
     CONSTRAINT FKMovePlanCelestialBody FOREIGN KEY (destination) REFERENCES CelestialBody
);

CREATE TABLE SpaceRoad (
     name TEXT NOT NULL,
     builder TEXT NOT NULL,
     spaceCounterAType TEXT NOT NULL,
     spaceCounterACelestialBodyName TEXT NOT NULL,
     spaceCounterATurn INTEGER NOT NULL,
     spaceCounterBType TEXT NOT NULL,
     spaceCounterBCelestialBodyName TEXT NOT NULL,
     spaceCounterBTurn INTEGER NOT NULL,
     CHECK(spaceCounterAType = 'SpaceCounter' AND spaceCounterBType = 'SpaceCounter'),
     CHECK(spaceCounterACelestialBodyName != spaceCounterBCelestialBodyName),
     PRIMARY KEY (name, builder),
     FOREIGN KEY (builder) REFERENCES Player,
     FOREIGN KEY (spaceCounterAType, spaceCounterACelestialBodyName, spaceCounterATurn) REFERENCES SpaceCounter,
     FOREIGN KEY (spaceCounterBType, spaceCounterBCelestialBodyName, spaceCounterBTurn) REFERENCES SpaceCounter
);

CREATE TABLE SpaceRoadDeliverer (
     --constants
     owner TEXT NOT NULL,
     name TEXT NOT NULL,
     type TEXT NOT NULL,
     sourceType TEXT NOT NULL,
     sourceCelestialBodyName TEXT NOT NULL,
     sourceTurn INTEGER NOT NULL,
     destinationType TEXT NOT NULL,
     destinationCelestialBodyName TEXT NOT NULL,
     destinationTurn INTEGER NOT NULL,
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
     fleetName text,
     planetName text,
     PRIMARY KEY (owner),
     CHECK((fleetName IS NOT NULL AND planetName IS NULL)
     	   OR (planetName IS NOT NULL AND fleetName IS NULL)),     
     FOREIGN KEY (owner) REFERENCES Player,	   	
     FOREIGN KEY (owner, fleetName) REFERENCES Fleet,     
     FOREIGN KEY (planetName) REFERENCES Planet
);

CREATE TABLE AntiProbeMissile (	 
     -- constants
     owner TEXT NOT NULL,
     name TEXT NOT NULL,
     type TEXT NOT NULL,
     -- variables
     targetOwner text,
     targetName text,
     targetTurn INTEGER, -- usefull ? set on fire date.
     CHECK(targetTurn IS NULL OR targetTurn >= 0),
     CHECK((targetOwner IS NOT NULL AND targetName IS NOT NULL AND targetTurn IS NOT NULL)
           OR (targetOwner IS NULL AND targetName IS NULL AND targetTurn IS NULL)),
     CHECK(type = 'AntiProbeMissile'),
     PRIMARY KEY (owner, name),
     UNIQUE (owner, name, type),
     FOREIGN KEY (owner, name, type) REFERENCES Unit (owner, name, type),
     FOREIGN KEY (targetOwner, targetName) REFERENCES Probe
);     

CREATE TABLE Diplomacy (
     owner TEXT NOT NULL,
     target TEXT NOT NULL,
     allowToLand BOOL NOT NULL,
     foreignPolicy TEXT NOT NULL,
     CHECK(foreignPolicy IN ('NEUTRAL', 'HOSTILE', 'HOSTILE_IF_OWNER')),
     CHECK(owner != target),
     PRIMARY KEY (owner, target),
     FOREIGN KEY (owner) REFERENCES Player,
     FOREIGN KEY (target) REFERENCES Player
);

CREATE TABLE Vortex (
     name TEXT NOT NULL,
     type TEXT NOT NULL,
     onsetDate INTEGER NOT NULL,
     endDate INTEGER NOT NULL,
     destination TEXT NOT NULL,
     CHECK(type = 'Vortex'),
     CHECK(onsetDate >= 0 AND endDate > onsetDate),
     PRIMARY KEY (name),
     UNIQUE (name, type),
     FOREIGN KEY (name, type) REFERENCES CelestialBody (name, type),
     FOREIGN KEY (destination) REFERENCES CelestialBody
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
     starshipTemplate TEXT NOT NULL,
     quantity INTEGER NOT NULL,
     CHECK(quantity >= 0)
     PRIMARY KEY (fleetOwner, fleetName, starshipTemplate),
     FOREIGN KEY (starshipTemplate) REFERENCES StarshipTemplate,
     FOREIGN KEY (fleetOwner, fleetName) REFERENCES Fleet
);

CREATE TABLE UnitEncounterLog (
	owner TEXT NOT NULL,
	unitName TEXT NOT NULL,
	unitType TEXT NOT NULL,
	logTurn INTEGER NOT NULL,
	instantTime INTEGER NOT NULL,
	seenOwner TEXT NOT NULL,
	seenName TEXT NOT NULL,
	seenTurn INTEGER NOT NULL,
	seenType TEXT NOT NULL,
	PRIMARY KEY (owner, unitName, unitType, logTurn, instantTime),
	FOREIGN KEY (owner, unitName, unitType) REFERENCES Unit(owner, name, type),
	FOREIGN KEY (seenOwner, seenName, seenType) REFERENCES Unit(owner, name, type)
);

CREATE TABLE UnitArrivalLog (
	owner TEXT NOT NULL,
	unitName TEXT NOT NULL,
	unitType TEXT NOT NULL,
	logTurn INTEGER NOT NULL,
	instantTime INTEGER NOT NULL,
	destination TEXT NOT NULL,
	vortex TEXT DEFAULT NULL,	
	PRIMARY KEY (owner, unitName, unitType, logTurn, instantTime),
	FOREIGN KEY (owner, unitName, unitType) REFERENCES Unit(owner, name, type),
	FOREIGN KEY (destination) REFERENCES ProductiveCelestialBody(name),
	FOREIGN KEY (vortex) REFERENCES Vortex(name)
);