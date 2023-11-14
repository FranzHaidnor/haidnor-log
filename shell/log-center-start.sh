#!/bin/bash
jar_name='haidnor-log-center.jar'
jar_path='/home/wangxiang/haidnor-log/'
config_path='/home/wangxiang/haidnor-log/server-config.json'

echo "start haidnor-log-center"

ID=`ps -ef | grep $jar_name | grep  "java" | awk '{print $2}'`
if [[ -n $ID ]]; then
  for id in $ID
    do
    kill -9 $id
  done
fi

nohup java -jar ${jar_path}${jar_name} -Xms2048m Xmx2048m --configPath=${config_path} > center.out  2>&1 &