#!/bin/sh
for f in src/test/resources/org/axan/sep/server/model/*.sql*; do
	ln -s ../../../../../../../src/test/resources/org/axan/sep/server/model/`basename $f` target/test-classes/org/axan/sep/server/model/
#ln -s ../../../../../../../src/test/resources/org/axan/sep/server/model/TestSQLite.creation.sql target/test-classes/org/axan/sep/server/model/
done
