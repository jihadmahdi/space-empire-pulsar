package org.axan.sep.common.db;

import org.neo4j.graphdb.RelationshipType;

public enum eRelationsTypes implements RelationshipType
{
	GameConfig,
	Player,
	PlayerConfig,
	Area,
	CelestialBody,
	Unit,
	Government,
	AssignedFleet
}
