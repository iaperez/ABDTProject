#!/bin/sh
. ./setEnv.sh
RUN_CMD="$JAVA_HOME/bin/java $MEM_ARGS -cp $CLASSPATH:bin ConversationsDownload"
echo $RUN_CMD ${1+"$@"}
exec $RUN_CMD ${1+"$@"}
