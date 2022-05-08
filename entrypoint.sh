#!/bin/sh

cd /ldapbench
java LdapBench2 $SERVER $AUTHDN $AUTHPASS $TESTBASE $THREADS $OBJECTS

