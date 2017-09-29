#!/bin/bash

ROBOCODE_PATH=/home/rsalesc/robocode

ROBOT_FULL_NAME=$1
ROBOT_VERSION=$2
ROBOT_PROGUARD=$3
ROBOT_NAME=${ROBOT_FULL_NAME##*.}
ROBOT_PACKAGE=${ROBOT_FULL_NAME%.*}
ROBOT_PATH=${ROBOT_PACKAGE//\./\/}
ROBOT_FILE=${ROBOT_PATH}/${ROBOT_NAME}.java

rm -rf out/
mkdir -p out
mkdir -p releases/$ROBOT_NAME
mkdir -p obfuscated/$ROBOT_NAME

javac -cp $ROBOCODE_PATH/libs/robocode.jar:./src -d out src/$ROBOT_FILE \
    && ruby build.rb $ROBOT_FULL_NAME $ROBOT_VERSION && jar cmf MANIFEST.MF releases/$ROBOT_NAME/${ROBOT_FULL_NAME}_$ROBOT_VERSION.jar -C out . \
    && cp releases/$ROBOT_NAME/${ROBOT_FULL_NAME}_$ROBOT_VERSION.jar $ROBOCODE_PATH/robots \
    && ./codesize.sh releases/$ROBOT_NAME/${ROBOT_FULL_NAME}_$ROBOT_VERSION.jar \
    && [ "$ROBOT_PROGUARD" != "raw" ] \
    && proguard -injars releases/$ROBOT_NAME/${ROBOT_FULL_NAME}_$ROBOT_VERSION.jar -outjars obfuscated/$ROBOT_NAME/${ROBOT_FULL_NAME}_$ROBOT_VERSION.jar \
        -libraryjars $ROBOCODE_PATH/libs/robocode.jar -libraryjars ${JAVA_HOME}/jre/lib/rt.jar \
        -keep "public class $ROBOT_FULL_NAME" -dontoptimize
