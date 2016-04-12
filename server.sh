#!/usr/bin/env bash
DATAFILE=2015-12-17.h2.db

if [ ! -f $DATAFILE ]; then
  echo "Downloading database file"
  curl -L -o $DATAFILE https://www.dropbox.com/s/obd37ixvxaocyjt/2015-12-17.h2.db?dl=1
else
  echo "File exists"
fi

java -jar local.jar
