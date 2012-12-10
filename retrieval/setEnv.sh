#!/bin/sh
if  [ -z $JAVA_HOME ] ; then
 export JAVA_HOME="/Library/Java/Home"
fi

for jar in ../lib/*.jar;do
 export CLASSPATH=$CLASSPATH:$jar
done

for jar in ../lib/jgrapht-0.8.3/*.jar;do
 export CLASSPATH=$CLASSPATH:$jar
done

for jar in ../lib/twitter4j-3.0.2/*.jar;do
 export CLASSPATH=$CLASSPATH:$jar
done

for jar in ../lib/twitter-text-1.5.0/*.jar;do
 export CLASSPATH=$CLASSPATH:$jar
done

for jar in ../lib/klout4j/*.jar;do
 export CLASSPATH=$CLASSPATH:$jar
done

export MEM_ARGS="-Xms1024m -Xmx1024m"
