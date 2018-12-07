#!/bin/bash
basedir=$1
testdir=$2
echo "Starting docker container"
docker build -t="suv-test-server" ${basedir}/target
docker run -d -p 8080:8080 --name suv-test-server_container suv-test-server
/git/circlecitools/bin/waitForServer.sh localhost:8080 5000
${testdir}/run_newman.sh ${testdir}
rc=$?
echo "Cleaning up Docker"
docker stop suv-test-server_container
docker rm suv-test-server_container
docker rmi suv-test-server
exit $rc