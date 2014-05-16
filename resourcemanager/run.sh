#!/bin/bash
FILES=settingfiles/*
for f in $FILES
do
  echo "Processing $f file..."
  java -jar DASS.jar $f
  # take action on each file. $f store current file name
  # cat $f #old
done
