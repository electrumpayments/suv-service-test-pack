#!/bin/bash

testdir=$1
echo "Running newman tests:"

newman run ${testdir}/Suv.postman_collection.json -e ${testdir}/localhost.postman_environment.json

if [ "${?}" != 0 ]; then
	echo "Tests failed"
	exit 1
else
	echo "Tests passed"
	exit 0
fi
