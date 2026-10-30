#!/usr/bin/env bash

#sbt clean scalafmtAll compile coverage test it/Test/test coverageOff coverageReport dependencyUpdates
sbt clean scalafmtAll compile coverage test it/test coverageOff coverageReport dependencyUpdates

