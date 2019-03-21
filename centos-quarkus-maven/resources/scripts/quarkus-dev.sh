#!/bin/bash 

# WORK_DIR  the directory where the application binaries are built
# MVN_CMD_ARGS - the maven command arguments e.g. clean install

set -eu

WORK_DIR=${WORK_DIR:-/project}

cd $WORK_DIR

# if no pom.xml is found in the work dir try to sleep
# TODO work on the timeout
while [ ! -f $WORK_DIR/pom.xml  ]
do
    echo 'Waiting to synchronize project sources'
    sleep 5
done;

echo 'Starting the Quarkus in dev mode'
mvn ${MVN_CMD_ARGS:- \-Dquarkus.http.host=0.0.0.0 compile quarkus:dev}
