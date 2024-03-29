#!/bin/bash
set -o errexit -o pipefail

function clean_exit {
  ARG=$?
  echo "===> Exit status = ${ARG}"
  exit $ARG
}
trap clean_exit EXIT

usage(){
	echo "Usage: ./tests-integration.sh"
	exit 1
}

# GO TO PROJECT PATH
SCRIPT_PATH=`cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd`
cd ${SCRIPT_PATH}/../..

# TESTS SUITE
./scripts/ci/tests-integration-stage.sh --stage="REST_FILE"
./scripts/ci/tests-integration-stage.sh --stage="REST_HIBERNATE"
