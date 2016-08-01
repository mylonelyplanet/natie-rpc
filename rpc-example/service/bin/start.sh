#!/bin/sh
rm -f tpid

nohup java -server -Xms512m -Xmx512m -Xss256K -XX:MaxDirectMemorySize=1024m -XX:NewRatio=1 -XX:MaxPermSize=128m -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:+UnlockCommercialFeatures -XX:+FlightRecorder   -jar ../target/service-0.1.jar > /dev/null 2>&1  &

echo $! > tpid

