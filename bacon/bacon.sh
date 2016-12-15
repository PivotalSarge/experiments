#!/bin/bash

if [ -z "$BACON_CALCULATOR" ]
then
    BACON_CALCULATOR=heap.SerialBaconNumberCalculator
fi

java -Dbacon.calculator=$BACON_CALCULATOR -cp `dirname $0`/out/artifacts/bacon_jar/bacon.jar pivotal.io.bacon.Main "$@"
