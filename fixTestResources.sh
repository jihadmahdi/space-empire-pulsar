#!/bin/sh
for f in src/test/resources/org/axan/sep/server/*.sql*; do
	ln -s ../../../../../../src/test/resources/org/axan/sep/server/`basename $f` target/test-classes/org/axan/sep/server/
#ln -s ../../../../../../src/test/resources/org/axan/sep/server/TestSQLite.creation.sql target/test-classes/org/axan/sep/server/
done
