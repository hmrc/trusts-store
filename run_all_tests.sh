#!/usr/bin/env bash

sbt clean scalafmtAll scalastyleAll compile coverage test it/Test/test coverageOff coverageReport dependencyUpdates
