#!/bin/bash

GOAL=install
if [ x != x$2 ]; then
	GOAL=$2
fi

git clean -fdx
(cd fixedpoint-base && mvn $GOAL)
