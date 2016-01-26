#!/bin/bash
if [ $# -lt 2 ]; then
    echo "2 parameters are required. the first one is node id, the second one is node type."
    exit 1
fi

cd `dirname $0`
BIN_DIR=`pwd`
cd ..
DEPLOY_DIR=`pwd`
CONF_DIR=$DEPLOY_DIR/conf

SERVER_NAME="kafka-conn-test"

PIDS=`ps -ef | grep java | grep "$CONF_DIR" |awk '{print $2}'`
if [ -n "$PIDS" ]; then
    echo "ERROR: The $SERVER_NAME already started!"
    echo "PID: $PIDS"
    exit 1
fi

if [ -z ${JAVA_HOME} ]; then
   JAVA=`grep 'JAVA_HOME=' /etc/profile | awk -F '=' '{print $2}'`/bin/java
else
   JAVA=$JAVA_HOME/bin/java
fi

LIB_DIR=$DEPLOY_DIR/lib
CLASS_DIR=$DEPLOY_DIR/classes
LIB_JARS=`ls $LIB_DIR|grep .jar|awk '{print "'$LIB_DIR'/"$0}'|tr "\n" ":"`
#delete the last ':'
LIB_JARS=${LIB_JARS%?}
LIB_JARS=$LIB_JARS:$CLASS_DIR

JAVA_OPTS=" -Djava.awt.headless=true -Djava.net.preferIPv4Stack=true "
JAVA_DEBUG_OPTS=""
if [ "$3" = "debug" ]; then
    JAVA_DEBUG_OPTS=" -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=9755,server=y,suspend=n "
fi

GCLOGFILE=$DEPLOY_DIR/logs/gc.log

JAVA_MEM_OPTS=""
BITS=`java -version 2>&1 | grep -i 64-bit`
if [ -n "$BITS" ]; then
    JAVA_MEM_OPTS=" -server -Xmx2g -Xms2g -Xmn1g -XX:PermSize=128m -Xss256k -XX:+DisableExplicitGC -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:+UseCMSCompactAtFullCollection -XX:LargePageSizeInBytes=128m -XX:+UseFastAccessorMethods -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=70 -XX:+PrintGCDetails -XX:+PrintGCDateStamps -Xloggc:$GCLOGFILE"
else
    JAVA_MEM_OPTS=" -server -Xms1g -Xmx1g -XX:PermSize=128m -XX:SurvivorRatio=2 -XX:+UseParallelGC "
fi

echo -e "Starting the $SERVER_NAME ...\c"
#可以在除了com.kuaidadi.polaris.kafka.conn中任何子目录下的任何地方运行class文件，但一定要给出classpath，包括jar包的和自己的classes、conf目录
$JAVA $JAVA_OPTS $JAVA_MEM_OPTS $JAVA_DEBUG_OPTS -classpath $CONF_DIR:$LIB_JARS com.kuaidadi.polaris.kafka.conn.KafkaConnLauncher $1 $2 &

sleep 1
PIDS=`ps -ef | grep java | grep "$CONF_DIR" |awk '{print $2}'`
if [ -n "$PIDS" ]; then
    echo "OK!"
else
    exit 1
fi
