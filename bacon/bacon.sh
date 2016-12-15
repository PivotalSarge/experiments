#!/bin/bash

BACON_CALCULATOR=heap.SerialBaconNumberCalculator
while [ 0 -lt $# ]
do
    if [ "-serial" = "$1" -o "-s" = "$1" ]
    then
        BACON_CALCULATOR=heap.SerialBaconNumberCalculator
    elif [ "-parallel" = "$1" -o "-p" = "$1" ]
    then
        BACON_CALCULATOR=heap.ParallelBaconNumberCalculator
    else
        break
    fi
    shift
done

java \
    -Dbacon.calculator=$BACON_CALCULATOR \
    -cp `dirname $0`/out/artifacts/bacon_jar/bacon.jar \
    io.pivotal.bacon.Main "$@"
