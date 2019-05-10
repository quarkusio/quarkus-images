#! /bin/bash

set -e

#############################################
# Configure maven - for a complete list of
# supported values, please refer the module.yaml
# file.
CONFIGURE_SCRIPTS=(
  ${QUARKUS_HOME}/.m2/configure-maven.sh
)
source ${QUARKUS_HOME}/.m2/configure.sh
#############################################

exec "$@"