#!/bin/sh

java -cp `dirname $0`/out/artifacts/bacon_jar/bacon.jar pivotal.io.bacon.Main "$@"

