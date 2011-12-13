package org.axan.sep.common.db;

import org.neo4j.graphdb.GraphDatabaseService;

public interface IDBFactory
{
	GraphDatabaseService createDB();
}
