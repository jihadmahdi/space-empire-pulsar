-- *********************************************
-- * SQL SQLite generation                     
-- *--------------------------------------------
-- * DB-MAIN version: 9.1.3              
-- * Generator date: Nov 23 2010              
-- * Generation date: Mon Apr  4 20:18:41 2011 
-- * LUN file: /media/data/code/Java_Workspace/Space-Empire-Pulsar/conception/Space-Empire-Pulsar.lun 
-- * Schema: SERVER/Relational 0.2 
-- ********************************************* 


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
     symbol TEXT NOT NULL,
     color TEXT NOT NULL,
     portrait TEXT NOT NULL,
     CONSTRAINT PKPlayerConfig PRIMARY KEY (name),
     CONSTRAINT FKPlayerConfigPlayer FOREIGN KEY (name) REFERENCES Player
);

CREATE TABLE Unit (
     owner TEXT NOT NULL,
     name TEXT NOT NULL,
     PulsarMissile BOOL,
     Probe BOOL,
     AntiProbeMissile BOOL,
     Fleet BOOL,
     CarbonCarrier BOOL,
     SpaceRoadDeliverer BOOL,
     CONSTRAINT PKUnit PRIMARY KEY (owner, name),
     CHECK((SpaceRoadDeliverer + AntiProbeMissile + Probe + Fleet + PulsarMissile + CarbonCarrier ) = 1)
     CONSTRAINT FKUnitPlayer FOREIGN KEY (owner) REFERENCES Player
);

CREATE TABLE CelestialBody (
     name TEXT NOT NULL,
     location_x INTEGER NOT NULL,
     location_y INTEGER NOT NULL,
     location_z INTEGER NOT NULL,
     Vortex BOOL,
     ProductiveCelestialBody BOOL,
     CONSTRAINT PKCelestialBody PRIMARY KEY (name),
     CONSTRAINT UCelestialBodyArea UNIQUE (location_x, location_y, location_z),
     CHECK((Vortex + ProductiveCelestialBody) = 1),
     CONSTRAINT FKCelestialBodyArea FOREIGN KEY (location_x, location_y, location_z) REFERENCES Area
);
--comment on table CelestialBody is 'A celestial body occupy one area in the universe. There are several celestial body types.';

CREATE TABLE ProductiveCelestialBody (
     name TEXT NOT NULL,
     initialCarbonStock INTEGER NOT NULL,
     maxSlots INTEGER NOT NULL,
     Planet BOOL,
     AsteroidField BOOL,
     Nebula BOOL,
     CONSTRAINT PKProductiveCelestialBody PRIMARY KEY (name),
     CONSTRAINT FKProductiveCelestialBodyCelestialBody FOREIGN KEY (name) REFERENCES CelestialBody,
     CHECK((Planet + AsteroidField + Nebula) = 1)
);

CREATE TABLE AsteroidField (
     name TEXT NOT NULL,
     CONSTRAINT PKAsteroidField PRIMARY KEY (name),
     CONSTRAINT FKAsteroidFieldProductiveCelestialBody FOREIGN KEY (name) REFERENCES ProductiveCelestialBody
);

CREATE TABLE VersionedProductiveCelestialBody (
     name TEXT NOT NULL,
     turn INTEGER NOT NULL,
     carbonStock INTEGER NOT NULL,
     currentCarbon INTEGER NOT NULL,
     owner TEXT,
     VersionedPlanet BOOL,
     VersionedAsteroidField BOOL,
     VersionedNebula BOOL,
     CHECK(turn >= 0),
     CONSTRAINT PKVersionedProductiveCelestialBody PRIMARY KEY (name, turn),
     CONSTRAINT FKVersionedProductiveCelestialBodyProductiveCelestialBody FOREIGN KEY (name) REFERENCES ProductiveCelestialBody,     
     CHECK((VersionedNebula + VersionedAsteroidField + VersionedPlanet) = 1),
     CONSTRAINT FKVersionedProductiveCelestialBodyPlayer FOREIGN KEY (owner) REFERENCES Player
);
     
CREATE TABLE Building (
     type TEXT NOT NULL,
     nbSlots INTEGER NOT NULL,
     celestialBodyName TEXT NOT NULL,
     turn INTEGER NOT NULL,
     CHECK(type IN ('PulsarLaunchingPad', 'SpaceCounter', 'GovernmentModule', 'DefenseModule', 'StarshipPlant', 'ExtractionModule')),
     CONSTRAINT PKBuilding PRIMARY KEY (type, celestialBodyName, turn),
     CONSTRAINT FKBuildingVersionedProductiveCelestialBody FOREIGN KEY (celestialBodyName, turn) REFERENCES VersionedProductiveCelestialBody     
);
--comment on table Building is 'A building occupy a building slot on a productive celestial body. There are several building types.';

CREATE TABLE SpaceCounter (
     type TEXT NOT NULL,
     celestialBodyName TEXT NOT NULL,
     turn INTEGER NOT NULL,
     CONSTRAINT PKSpaceCounter PRIMARY KEY (type, celestialBodyName, turn),
     CONSTRAINT FKSpaceCounterBuilding FOREIGN KEY (type, celestialBodyName, turn) REFERENCES Building
);

CREATE TABLE CarbonCarrier (
     owner TEXT NOT NULL,
     name TEXT NOT NULL,
     sourceType TEXT NOT NULL,
     sourceCelestialBodyName TEXT NOT NULL,
     sourceTurn INTEGER NOT NULL,
     CONSTRAINT PKCarbonCarrier PRIMARY KEY (owner, name),
     CHECK(sourceType='SpaceCounter'),
     CONSTRAINT FKCarbonCarrierUnit FOREIGN KEY (owner, name) REFERENCES Unit,
     CONSTRAINT FKCarbonCarrierSpaceCounter FOREIGN KEY (sourceType, sourceCelestialBodyName, sourceTurn) REFERENCES SpaceCounter
);

CREATE TABLE CarbonOrder (
     source TEXT NOT NULL,
     owner TEXT NOT NULL,
     priority INTEGER NOT NULL,
     amount INTEGER NOT NULL,
     destination TEXT NOT NULL,
     CHECK(source != destination AND amount > 0 AND priority >= 0),
     CONSTRAINT PKCarbonOrder PRIMARY KEY (owner, source, priority),
     CONSTRAINT FKCarbonOrderPlayer FOREIGN KEY (owner) REFERENCES Player,
     CONSTRAINT FKCarbonOrderProductiveCelestialBodySource FOREIGN KEY (source) REFERENCES ProductiveCelestialBody,
     CONSTRAINT FKCarbonOrderProductiveCelestialBodyDestination FOREIGN KEY (destination) REFERENCES ProductiveCelestialBody
);

CREATE TABLE DefenseModule (
     type TEXT NOT NULL,
     celestialBodyName TEXT NOT NULL,
     turn INTEGER NOT NULL,
     CONSTRAINT PKDefenseModule PRIMARY KEY (type, celestialBodyName, turn),
     CONSTRAINT FKDefenseModuleBuilding FOREIGN KEY (type, celestialBodyName, turn) REFERENCES Building
);

CREATE TABLE Fleet (
     owner TEXT NOT NULL,
     name TEXT NOT NULL,
     CONSTRAINT PKFleet PRIMARY KEY (owner, name),
     CONSTRAINT FKFleetUnit FOREIGN KEY (owner, name) REFERENCES Unit
);

CREATE TABLE SpecialUnit (
     owner TEXT NOT NULL,
     name TEXT NOT NULL,
     Hero BOOL,
     CHECK((Hero) = 1), -- Waiting for other child tables.
     CONSTRAINT PKSpecialUnit PRIMARY KEY (owner, name),
     CONSTRAINT FKSpecialUnitPlayer FOREIGN KEY (owner) REFERENCES Player
);

CREATE TABLE VersionedUnit (
     turn INTEGER NOT NULL,
     owner TEXT NOT NULL,
     name TEXT NOT NULL,
     PulsarMissile BOOL,
     Probe BOOL,
     AntiProbeMissile BOOL,
     Fleet BOOL,
     CarbonCarrier BOOL,
     SpaceRoadDeliverer BOOL,
     location_x INTEGER NOT NULL,
     location_y INTEGER NOT NULL,
     location_z INTEGER NOT NULL,
	 CHECK(turn >= 0),
     CHECK((PulsarMissile + Probe + AntiProbeMissile + Fleet + CarbonCarrier + SpaceRoadDeliverer) = 1),
     CONSTRAINT PKVersionedUnit PRIMARY KEY (owner, name, turn),
     CONSTRAINT FKVersionedUnitUnit FOREIGN KEY (owner, name) REFERENCES Unit,
     CONSTRAINT FKVersionedUnitArea FOREIGN KEY (location_x, location_y, location_z) REFERENCES Area
);
     
CREATE TABLE VersionedFleet (
     owner TEXT NOT NULL,
     name TEXT NOT NULL,
     turn INTEGER NOT NULL,
     CONSTRAINT PKVersionedFleet PRIMARY KEY (owner, name, turn),
--     CHECK(EXISTS(SELECT * FROM FleetComposition
--                  WHERE FleetComposition.fleetOwner = owner AND FleetComposition.fleetName = name AND FleetComposition.fleetTurn = turn)),
     CONSTRAINT FKVersionedFleetVersionedUnit FOREIGN KEY (owner, name, turn) REFERENCES VersionedUnit,
     CONSTRAINT FKVersionedFleetFleet FOREIGN KEY (owner, name) REFERENCES Fleet
);

CREATE TABLE VersionedSpecialUnit (
     owner TEXT NOT NULL,
     name TEXT NOT NULL,
     turn INTEGER NOT NULL,
     fleetOwner TEXT NOT NULL,
     fleetName TEXT NOT NULL,
     fleetTurn INTEGER NOT NULL,
     CONSTRAINT PKVersionedSpecialUnit PRIMARY KEY (owner, name, turn),
     CONSTRAINT FKVersionedSpecialUnitVersionedUnit FOREIGN KEY (owner, name) REFERENCES SpecialUnit,
     CONSTRAINT FKVersionedSpecialUnitVersionedFleet FOREIGN KEY (fleetOwner, fleetName, fleetTurn) REFERENCES VersionedFleet
);

CREATE TABLE Probe (
     owner TEXT NOT NULL,
     name TEXT NOT NULL,
     deployed BOOL NOT NULL,
     CONSTRAINT PKProbe PRIMARY KEY (owner, name),
     CONSTRAINT FKProbeUnit FOREIGN KEY (owner, name) REFERENCES Unit
);

CREATE TABLE PulsarLaunchingPad (
     type TEXT NOT NULL,
     celestialBodyName TEXT NOT NULL,
     turn INTEGER NOT NULL,
     firedDate INTEGER NOT NULL,
     CONSTRAINT PKPulsarLaunchingPad PRIMARY KEY (type, celestialBodyName, turn),
     CONSTRAINT FKPulsarLaunchingPadBuilding FOREIGN KEY (type, celestialBodyName, turn) REFERENCES Building
);

CREATE TABLE PulsarMissile (
     owner TEXT NOT NULL,
     name TEXT NOT NULL,
     time INTEGER NOT NULL,
     volume INTEGER NOT NULL,
     CONSTRAINT PKPulsarMissile PRIMARY KEY (owner, name),
     CONSTRAINT FKPulsarMissileUnit FOREIGN KEY (owner, name) REFERENCES Unit
);

CREATE TABLE VersionedProbe (
     owner TEXT NOT NULL,
     name TEXT NOT NULL,
     turn INTEGER NOT NULL,
     destination_x INTEGER,
     destination_y INTEGER,
     destination_z INTEGER,
     CHECK((destination_x is NOT NULL AND destination_y is NOT NULL AND destination_z is NOT NULL)
           OR (destination_x IS NULL AND destination_y IS NULL AND destination_z IS NULL)),
     CONSTRAINT PKVersionedProbe PRIMARY KEY (owner, name, turn),
     CONSTRAINT FKVersionedProbeVersionedUnit FOREIGN KEY (owner, name, turn) REFERENCES VersionedUnit,
     CONSTRAINT FKVersionedProbeProbe FOREIGN KEY (owner, name) REFERENCES Probe,
     CONSTRAINT FKVersionedProbeArea FOREIGN KEY (destination_x, destination_y, destination_z) REFERENCES Area     
);

CREATE TABLE ExtractionModule (
     type TEXT NOT NULL,
     celestialBodyName TEXT NOT NULL,
     turn INTEGER NOT NULL,
     CONSTRAINT PKExtractionModule PRIMARY KEY (type, celestialBodyName, turn),
     CONSTRAINT FKExtractionModuleBuilding FOREIGN KEY (type, celestialBodyName, turn) REFERENCES Building
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
     CONSTRAINT PKGovernmentModule PRIMARY KEY (type, celestialBodyName, turn),
     CONSTRAINT FKGovernmentModuleBuilding FOREIGN KEY (type, celestialBodyName, turn) REFERENCES Building
);

CREATE TABLE Hero (
     owner TEXT NOT NULL,
     name TEXT NOT NULL,
     experience INTEGER NOT NULL,
     CHECK(experience >= 0),
     CONSTRAINT PKHero PRIMARY KEY (owner, name),
     CONSTRAINT FKHeroSpecialUnit FOREIGN KEY (owner, name) REFERENCES SpecialUnit
);
     
CREATE TABLE Nebula (
     name TEXT NOT NULL,
     CONSTRAINT PKNebula PRIMARY KEY (name),
     CONSTRAINT FKNebulaProductiveCelestialBody FOREIGN KEY (name) REFERENCES ProductiveCelestialBody
);

CREATE TABLE Planet (
     name TEXT NOT NULL,
     populationPerTurn INTEGER NOT NULL,
     maxPopulation INTEGER NOT NULL,
     CONSTRAINT PKPlanet PRIMARY KEY (name),
     CONSTRAINT FKPlanetProductiveCelestialBody FOREIGN KEY (name) REFERENCES ProductiveCelestialBody
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
     CONSTRAINT PKVersionedCarbonCarrier PRIMARY KEY (owner, name, turn),
     CONSTRAINT FKVersionedCarbonCarrierVersionedUnit FOREIGN KEY (owner, name, turn) REFERENCES VersionedUnit,
     CONSTRAINT FKVersionedCarbonCarrierCarbonCarrier FOREIGN KEY (owner, name) REFERENCES CarbonCarrier,
     CONSTRAINT FKVersionedCarbonCarrierCarbonOrder FOREIGN KEY (orderOwner, orderSource, orderPriority) REFERENCES CarbonOrder
);

CREATE TABLE VersionedNebula (
     name TEXT NOT NULL,
     turn INTEGER NOT NULL,
     CONSTRAINT PKVersionedNebula PRIMARY KEY (name, turn),
     CONSTRAINT FKVersionedNebulaVersionedProductiveCelestialBody FOREIGN KEY (name, turn) REFERENCES VersionedProductiveCelestialBody,
     CONSTRAINT FKVersionedNebulaNebula FOREIGN KEY (name) REFERENCES Nebula
);

CREATE TABLE VersionedAsteroidField (
     name TEXT NOT NULL,
     turn INTEGER NOT NULL,
     CONSTRAINT PKVersionedAsteroidField PRIMARY KEY (name, turn),
     CONSTRAINT FKVersionedAsteroidFieldVersionedProductiveCelestialBody FOREIGN KEY (name, turn) REFERENCES VersionedProductiveCelestialBody,
     CONSTRAINT FKVersionedAsteroidFieldAsteroidField FOREIGN KEY (name) REFERENCES AsteroidField
);

CREATE TABLE VersionedPlanet (
     name TEXT NOT NULL,
     turn INTEGER NOT NULL,
     currentPopulation INTEGER NOT NULL,
     PRIMARY KEY (name, turn),
     FOREIGN KEY (name, turn) REFERENCES VersionedProductiveCelestialBody,
     FOREIGN KEY (name) REFERENCES Planet
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
     CHECK(sourceType='SpaceRoad' AND destinationType='SpaceRoad'),
     CHECK(sourceCelestialBodyName != destinationCelestialBodyName),
     PRIMARY KEY (owner, name),
     FOREIGN KEY (owner, name) REFERENCES Unit,
     FOREIGN KEY (sourceType, sourceCelestialBodyName, sourceTurn) REFERENCES SpaceCounter,
     FOREIGN KEY (destinationType, destinationCelestialBodyName, destinationTurn) REFERENCES SpaceCounter
);

CREATE TABLE StarshipPlant (
     type TEXT NOT NULL,
     celestialBodyName TEXT NOT NULL,
     turn INTEGER NOT NULL,
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
     PRIMARY KEY (owner, name),
     FOREIGN KEY (owner, name) REFERENCES Unit
);

CREATE TABLE VersionedSpaceRoadDeliverer (
     owner TEXT NOT NULL,
     name TEXT NOT NULL,
     turn INTEGER NOT NULL,
     PRIMARY KEY (owner, name, turn),
     FOREIGN KEY (owner, name, turn) REFERENCES VersionedUnit,
     FOREIGN KEY (owner, name) REFERENCES SpaceRoadDeliverer
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
     CHECK(onsetDate >= 0 AND endDate > onsetDate),
     PRIMARY KEY (name),
     FOREIGN KEY (name) REFERENCES CelestialBody,
     FOREIGN KEY (destination) REFERENCES CelestialBody
);        
  
CREATE TABLE VersionedAntiProbeMissile (
     owner TEXT NOT NULL,
     name TEXT NOT NULL,
     turn INTEGER NOT NULL,
     targetOwner text,
     targetName text,
     targetTurn INTEGER,
     CHECK((targetOwner IS NOT NULL AND targetName IS NOT NULL AND targetTurn IS NOT NULL)
           OR (targetOwner IS NULL AND targetName IS NULL AND targetTurn IS NULL)),
     PRIMARY KEY (owner, name, turn),
     FOREIGN KEY (owner, name, turn) REFERENCES VersionedUnit,
     FOREIGN KEY (owner, name) REFERENCES AntiProbeMissile,
     FOREIGN KEY (targetOwner, targetName, targetTurn) REFERENCES VersionedProbe
);

CREATE TABLE VersionedPulsarMissile (
     owner TEXT NOT NULL,
     name TEXT NOT NULL,
     turn INTEGER NOT NULL,
     direction_x INTEGER,
     direction_y INTEGER,
     direction_z INTEGER,
     CHECK((direction_x IS NOT NULL AND direction_y IS NOT NULL AND direction_z IS NOT NULL)
           OR (direction_x IS NULL AND direction_y IS NULL AND direction_z IS NULL)),
     PRIMARY KEY (owner, name, turn),
     FOREIGN KEY (owner, name, turn) REFERENCES VersionedUnit,
     FOREIGN KEY (owner, name) REFERENCES PulsarMissile,
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
