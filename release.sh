#!/bin/bash
set -o errexit -o pipefail

SCRIPT_DIRECTORY="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null && pwd)"
PROJECT_ROOT_DIRECTORY="$SCRIPT_DIRECTORY"

#########################################
#### Variables intialisation ############
#########################################
TEST="YES"
RELEASE="NO"
SKIP_API="NO"
BASEDIR=$PWD
DOCKER_COMPOSE="${PROJECT_ROOT_DIRECTORY}/docker/docker-files/docker-compose.yml"

#########################################
#### Cleaning functions #################
#########################################
function clean_docker {
    echo "===> Stop arlas-persistence-server stack"
    docker-compose -f ${DOCKER_COMPOSE} --project-name arlaspersist down -v
}

function clean_exit {
  ARG=$?
	echo "=> Exit status = $ARG"
	rm -rf pom.xml.versionsBackup
	rm -rf target/tmp || echo "target/tmp already removed"
	clean_docker
	if [ "$RELEASE" == "YES" ]; then
        git checkout -- .
        mvn clean
    else
        echo "=> Skip discard changes";
        git checkout -- pom.xml
        git checkout -- arlas-persistence-core/pom.xml
        git checkout -- arlas-persistence-rest/pom.xml
        git checkout -- arlas-persistence-server/pom.xml
        git checkout -- arlas-persistence-tests/pom.xml
        sed -i.bak 's/\"'${FULL_API_VERSION}'\"/\"API_VERSION\"/' arlas-persistence-rest/src/main/java/io/arlas/persistence/rest/PersistenceRestService.java
    fi
    exit $ARG
}
trap clean_exit EXIT

#########################################
#### Available arguments ################
#########################################
usage(){
	echo "Usage: ./release.sh -api-major=X -api-minor=Y -api-patch=U -rel=Z -dev=Z+1 [--no-tests] [--skip-api]"
	echo " -api-major|--api-version       release arlas-persistence-server API major version"
	echo " -api-minor|--api-minor-version release arlas-persistence-server API minor version"
	echo " -api-patch|--api-patch-version release arlas-persistence-server API patch version"
	echo " -rel|--arlas-release           release arlas-server version"
	echo " -dev|--arlas-dev               development arlas-persistence-server version (-SNAPSHOT qualifier will be automatically added)"
	echo " --no-tests                     do not run integration tests"
	echo " --release                      publish artifacts and git push local branches"
	echo " --skip-api                     do not generate clients APIs"
	exit 1
}

#########################################
#### Parsing arguments ##################
#########################################
for i in "$@"
do
case $i in
    -rel=*|--arlas-release=*)
    ARLAS_REL="${i#*=}"
    shift # past argument=value
    ;;
    -dev=*|--arlas-dev=*)
    ARLAS_DEV="${i#*=}"
    shift # past argument=value
    ;;
    -api-major=*|--api-major-version=*)
    API_MAJOR_VERSION="${i#*=}"
    shift # past argument=value
    ;;
    -api-minor=*|--api-minor-version=*)
    API_MINOR_VERSION="${i#*=}"
    shift # past argument=value
    ;;
    -api-patch=*|--api-patch-version=*)
    API_PATCH_VERSION="${i#*=}"
    shift # past argument=value
    ;;
    --no-tests)
    TESTS="NO"
    shift # past argument with no value
    ;;
    --release)
    RELEASE="YES"
    shift # past argument with no value
    ;;
    --skip-api)
    SKIP_API="YES"
    shift # past argument with no value
    ;;
    *)
            # unknown option
    ;;
esac
done

#########################################
#### Recap of chosen arguments ##########
#########################################

if [ -z ${API_MAJOR_VERSION+x} ]; then usage;  else    echo "API MAJOR version           : ${API_MAJOR_VERSION}"; fi
if [ -z ${API_MINOR_VERSION+x} ]; then usage;  else    echo "API MINOR version           : ${API_MINOR_VERSION}"; fi
if [ -z ${API_PATCH_VERSION+x} ]; then usage;  else    echo "API PATCH version           : ${API_PATCH_VERSION}"; fi
if [ -z ${ARLAS_REL+x} ]; then usage;          else    echo "Release version             : ${ARLAS_REL}"; fi
if [ -z ${ARLAS_DEV+x} ]; then usage;          else    echo "Next development version    : ${ARLAS_DEV}"; fi
                                                       echo "Running tests               : ${TESTS}"
                                                       echo "Release                     : ${RELEASE}"

#########################################
#### Check if you're logged on to repos ###########
#########################################

if [ "$RELEASE" == "YES" -a "$SKIP_API" == "NO" ]; then
    export npmlogin=`npm whoami`
    if  [ -z "$npmlogin"  ] ; then echo "Your are not logged on to npm"; exit -1; else  echo "logged as "$npmlogin ; fi
    if  [ -z "$GITHUB_CHANGELOG_TOKEN"  ] ; then echo "Please set GITHUB_CHANGELOG_TOKEN environment variable"; exit -1; fi
    if  [ -z "$PIP_LOGIN"  ] ; then echo "Please set PIP_LOGIN environment variable"; exit -1; fi
    if  [ -z "$PIP_PASSWORD"  ] ; then echo "Please set PIP_PASSWORD environment variable"; exit -1; fi
fi


#########################################
#### Setting versions ###################
#########################################
export ARLAS_persistence_VERSION="${API_MAJOR_VERSION}.0.${ARLAS_REL}"
ARLAS_DEV_VERSION="${API_MAJOR_VERSION}.0.${ARLAS_DEV}"
FULL_API_VERSION=${API_MAJOR_VERSION}"."${API_MINOR_VERSION}"."${API_PATCH_VERSION}
API_DEV_VERSION=${API_MAJOR_VERSION}"."${API_MINOR_VERSION}"."${ARLAS_DEV}

echo "Release : ${ARLAS_persistence_VERSION}"
echo "API     : ${FULL_API_VERSION}"
echo "Dev     : ${ARLAS_DEV_VERSION}"


#########################################
#### Ongoing release process ############
#########################################

echo "=> Get develop branch"
if [ "$RELEASE" == "YES" ]; then
    git checkout develop
    git pull origin develop
else echo "=> Skip develop checkout"; fi

echo "=> Update project version"
mvn clean
mvn versions:set -DnewVersion=${ARLAS_persistence_VERSION}
sed -i.bak 's/\"API_VERSION\"/\"'${FULL_API_VERSION}'\"/' arlas-persistence-rest/src/main/java/io/arlas/persistence/rest/PersistenceRestService.java

if [ "$RELEASE" == "YES" ]; then
    export DOCKERFILE="${PROJECT_ROOT_DIRECTORY}/docker/docker-files/Dockerfile"
else
    echo "=> Build arlas-persistence-server"
    docker run \
        -e GROUP_ID="$(id -g)" \
        -e USER_ID="$(id -u)" \
        --mount dst=/mnt/.m2,src="$HOME/.m2/",type=bind \
        --mount dst=/opt/maven,src="$PWD",type=bind \
        --rm \
        maven:3.8.5-openjdk-17 \
            clean install
fi

#########################################
#### Generate swagger definiton of the API #######
#########################################

echo "=> Start arlas-persistence-server stack"
docker-compose -f ${DOCKER_COMPOSE} --project-name arlaspersist up -d --build
DOCKER_IP=$(docker-machine ip || echo "localhost")

echo "=> Wait for arlas-persistence-server up and running"
i=1; until nc -w 2 ${DOCKER_IP} 19997; do if [ $i -lt 30 ]; then sleep 1; else break; fi; i=$(($i + 1)); done

echo "=> Get swagger documentation"
mkdir -p target/tmp || echo "target/tmp exists"
i=1; until curl -XGET http://${DOCKER_IP}:19997/arlas_persistence_server/openapi.json -o target/tmp/openapi.json; do if [ $i -lt 60 ]; then sleep 1; else break; fi; i=$(($i + 1)); done
i=1; until curl -XGET http://${DOCKER_IP}:19997/arlas_persistence_server/openapi.yaml -o target/tmp/openapi.yaml; do if [ $i -lt 60 ]; then sleep 1; else break; fi; i=$(($i + 1)); done

mkdir -p openapi
cp target/tmp/openapi.yaml openapi
cp target/tmp/openapi.json openapi

echo "=> Stop arlas-persistence-server stack"
docker-compose -f ${DOCKER_COMPOSE} --project-name arlaspersist down -v

echo "=> Generate API documentation"
mvn "-Dswagger.output=docs/api" swagger2markup:convertSwagger2markup

itests() {
	echo "=> Run integration tests"
    ./scripts/tests-integration.sh
}
if [ "$TESTS" == "YES" ]; then itests; else echo "=> Skip integration tests"; fi


#########################################
#### Generate API clients ###############
#########################################

if [ "$SKIP_API" == "YES" ]; then
  echo "=> Skipping generation of API clients"
else
  echo "=> Generate API clients"
  ls target/tmp/

  mkdir -p target/tmp/typescript-fetch
  docker run --rm \
      -e GROUP_ID="$(id -g)" \
      -e USER_ID="$(id -u)" \
      --mount dst=/input/api.json,src="$PWD/target/tmp/openapi.json",type=bind,ro \
      --mount dst=/output,src="$PWD/target/tmp/typescript-fetch",type=bind \
    gisaia/swagger-codegen-3.0.42 \
          -l typescript-fetch --additional-properties modelPropertyNaming=snake_case

  echo "=> Build Typescript API "${FULL_API_VERSION}
  cd ${BASEDIR}/target/tmp/typescript-fetch/
  cp ${BASEDIR}/conf/npm/package-build.json package.json
  cp ${BASEDIR}/conf/npm/tsconfig-build.json .
  npm version --no-git-tag-version ${FULL_API_VERSION}
  npm install
  npm run build-release
  npm run postbuild
  cd ${BASEDIR}

  echo "=> Publish Typescript API "
  cp ${BASEDIR}/conf/npm/package-publish.json ${BASEDIR}/target/tmp/typescript-fetch/dist/package.json
  cd ${BASEDIR}/target/tmp/typescript-fetch/dist
  npm version --no-git-tag-version ${FULL_API_VERSION}


  if [ "$RELEASE" == "YES" ]; then
      npm publish || echo "Publishing on npm failed ... continue ..."
  else echo "=> Skip npm api publish"; fi
fi

cd ${BASEDIR}

if [ "$RELEASE" == "YES" ]; then
    echo "=> Tag arlas-persistence-server docker image"
    docker tag gisaia/arlas-persistence-server:latest gisaia/arlas-persistence-server:${ARLAS_persistence_VERSION}
    echo "=> Push arlas-persistence-server docker image"
    docker push gisaia/arlas-persistence-server:${ARLAS_persistence_VERSION}
    docker push gisaia/arlas-persistence-server:latest
else echo "=> Skip docker push image"; fi

if [ "$RELEASE" == "YES" ]; then
    echo "=> Generate CHANGELOG.md"
    git tag v${ARLAS_persistence_VERSION}
    git push origin v${ARLAS_persistence_VERSION}
    #@see scripts/build-github-changelog-generator.sh in ARLAS-server project if you need a fresher version of this tool
    docker run -it --rm -v "$(pwd)":/usr/local/src/your-app gisaia/github-changelog-generator:latest github_changelog_generator \
        -u gisaia -p arlas-persistence --token ${GITHUB_CHANGELOG_TOKEN} \
        --no-pr-wo-labels --no-issues-wo-labels --no-unreleased --issue-line-labels API,OGC,conf,security,documentation \
        --exclude-labels type:duplicate,type:question,type:wontfix,type:invalid \
        --bug-labels type:bug \
        --enhancement-labels  type:enhancement \
        --breaking-labels type:breaking \
        --enhancement-label "**New stuff:**" --issues-label "**Miscellaneous:**" --since-tag v0.0.1
    git tag -d v${ARLAS_persistence_VERSION}
    git push origin :v${ARLAS_persistence_VERSION}
    echo "=> Commit release version"
    git add docs/api
    git add openapi/openapi.json
    git add openapi/openapi.yaml
    git commit -a -m "release version ${ARLAS_persistence_VERSION}"
    git tag v${ARLAS_persistence_VERSION}
    git push origin v${ARLAS_persistence_VERSION}
    git push origin develop

    echo "=> Merge develop into master"
    git checkout master
    git pull origin master
    git merge origin/develop
    git push origin master

    echo "=> Rebase develop"
    git checkout develop
    git pull origin develop
    git rebase origin/master
else echo "=> Skip git push master"; fi

echo "=> Update project version for develop"
mvn versions:set -DnewVersion=${ARLAS_DEV_VERSION}-SNAPSHOT

echo "=> Update REST API version in JAVA source code"
sed -i.bak 's/\"'${FULL_API_VERSION}'\"/\"API_VERSION\"/' arlas-persistence-rest/src/main/java/io/arlas/persistence/rest/PersistenceRestService.java

if [ "$RELEASE" == "YES" ]; then
    sed -i.bak 's/\"'${FULL_API_VERSION}'\"/\"'${API_DEV_VERSION}-SNAPSHOT'\"/' openapi/openapi.yaml
    sed -i.bak 's/\"'${FULL_API_VERSION}'\"/\"'${API_DEV_VERSION}-SNAPSHOT'\"/' openapi/openapi.json
    git add openapi/openapi.json
    git add openapi/openapi.yaml
    git commit -a -m "development version ${ARLAS_DEV_VERSION}-SNAPSHOT"
    git push origin develop
else echo "=> Skip git push develop"; fi