# haidnor-log
轻量级分布式日志实时查看下载系统。提高运维效率，降低日志搜集系统部署资源成本   

![](./doc/images/img02.png)

# 特性
1. 实时查看、下载任意服务器节点项目日志文件
2. 支持多节点日志按照时间顺序交叉合并
3. 快速配置部署，无需数据库存储数据

# 工作原理
![](./doc/images/img01.png)

# 启动
# 日志中心
java -jar haidnor-log-center.jar -Xms256m Xmx256m --configPath=D:/project_haidnor/haidnor-log/server-config.json

# 日志客户端
java -jar haidnor-log-client.jar -Xms128m Xmx128m --centerAddress=192.168.21.123:8085
