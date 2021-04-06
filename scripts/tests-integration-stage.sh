#!/bin/bash
set -e

function clean_docker {
    ./scripts/docker-clean.sh
    echo "===> clean maven repository"
	docker run --rm \
		-w /opt/maven \
		-v $PWD:/opt/maven \
		-v $HOME/.m2:/root/.m2 \
		maven:3.5.0-jdk-8 \
		mvn clean
}

function clean_exit {
  ARG=$?
	echo "===> Exit stage ${STAGE} = ${ARG}"
  clean_docker
  rm -rf /tmp/persist
  exit $ARG
}
trap clean_exit EXIT

usage(){
	echo "Usage: ./tests-integration-stage.sh --stage=REST"
	exit 1
}

for i in "$@"
do
case $i in
    --stage=*)
    STAGE="${i#*=}"
    shift # past argument=value
    ;;
    *)
            # unknown option
    ;;
esac
done

mkdir /tmp/persist/
# GO TO PROJECT PATH
SCRIPT_PATH=`cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd`
cd ${SCRIPT_PATH}/..

# CHECK ALV2 DISCLAIMER
if [ $(find ./*/src -name "*.java" -exec grep -L Licensed {} \; | wc -l) -gt 0 ]; then
    echo "ALv2 disclaimer is missing in the following files :"
    find ./*/src -name "*.java" -exec grep -L Licensed {} \;
    exit -1
fi

if [ -z ${STAGE+x} ]; then usage; else echo "Tests stage : ${STAGE}"; fi

function start_stack() {
    ./scripts/docker-clean.sh
    ./scripts/docker-run.sh --build
}


function test_rest_server() {
    export ARLAS_AUTH_ENABLED=false
    start_stack
    docker run --rm \
        -w /opt/maven \
        -v $PWD:/opt/maven \
        -v $HOME/.m2:/root/.m2 \
        -e ARLAS_PERSISTENCE_HOST="arlas-persistence-server" \
        -e ARLAS_PERSISTENCE_PREFIX="arlas_persistence_server" \
        -e ARLAS_PERSISTENCE_APP_PATH=${ARLAS_PERSISTENCE_APP_PATH} \
        -e ARLAS_PERSISTENCE_ENGINE=${ARLAS_PERSISTENCE_ENGINE} \
        --network arlaspersist_default \
        maven:3.5.0-jdk-8 \
        mvn -Dit.test=PersistenceIT verify -DskipTests=false -DfailIfNoTests=false
}


function test_doc() {
    ./mkDocs.sh
}

if [ "$STAGE" == "REST_FILE" ]; then export ARLAS_PERSISTENCE_ENGINE="file"; test_rest_server; fi
if [ "$STAGE" == "REST_HIBERNATE" ]; then export ARLAS_PERSISTENCE_ENGINE="hibernate"; test_rest_server; fi
if [ "$STAGE" == "DOC" ]; then test_doc; fi

