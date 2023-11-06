#!/bin/bash
jar_name='haidnor-log-client.jar'

echo "stop haidnor-log-client"

ID=`ps -ef | grep $jar_name | grep  "java" | awk '{print $2}'`
#echo $ID
if [[ -n $ID ]]; then
    for id in $ID
    do
        kill -9 $id
    done
fi