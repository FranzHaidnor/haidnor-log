#!/bin/bash
jar_name='haidnor-log-client.jar'
jar_path='/home/wangxiang/haidnor-log/'
centerAddress='192.168.12.197:8084'

echo "start haidnor-log-client"

ID=`ps -ef | grep $jar_name | grep  "java" | awk '{print $2}'`
if [[ -n $ID ]]; then
  for id in $ID
    do
    kill -9 $id
  done
fi

nohup java -jar ${jar_path}${jar_name} -Xms600m Xmx600m --centerAddress=${centerAddress} > client.out  2>&1 &
