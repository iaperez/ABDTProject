#!/bin/sh

. ./setEnv.sh
RUN_CMD="$JAVA_HOME/bin/javac  -d bin -cp $CLASSPATH src/ConversationsDownload.java"
echo $RUN_CMD ${1+"$@"}
exec $RUN_CMD ${1+"$@"}
