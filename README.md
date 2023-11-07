# haidnor-log
轻量级分布式日志实时查看下载工具。提高运维效率，降低日志搜集系统部署资源成本   

1. 支持实时查看、下载多个服务器节点的应用服务日志文件
2. 多服务节点的日志按时间顺序交叉合并
3. 快速配置安装部署，不依赖任何数据库与中间件

![](./doc/images/img02.png)

# 架构
![](./doc/images/img01.png)

haidnor-log 分布式日志系统分为 **日志中心** 和 **日志客户端** 两个模块   
日志中心负责向日志客户端发送请求收集日志文本数据，并提供 Web 控制台页面给用户使用。   
客户端启动后会自动连接日志中心，等待日志中心的请求。

# 技术选型
**服务端**   
[Netty](https://github.com/netty/netty)   日志中心与日志客户端双向通信   
[Spring Boot](https://github.com/spring-projects/spring-boot)   构建日志中心 WEB 应用服务   
[Java 8](https://www.oracle.com/java/technologies/java8.html)   

**H5 前端**   
[Vue](https://github.com/vuejs/vue)   
[Element UI](https://github.com/ElemeFE/element)   
[Axios](https://github.com/axios/axios)   

# 使用方式

## maven 编译打包
执行 `mvn clean package` 命令对项目进行编译打包，得到 `haidnor-log-client.jar` 和 `haidnor-log-center.jar` 文件

---
## 日志中心 haidnor-center
### 1. 配置客户端节点信息
`server-config.json` 文件是记录多个客户端节点应用服务与日志目录关系的配置文件，其中是以 JSON 结构进行存储的
```json
[
  {
    "ip": "192.168.21.123",
    "server": "order_service",
    "path": "/home/log/order_service"
  }
]
```
**ip :** 日志客户端所在的服务器 IP 地址 (目前仅支持 IPV4)   
**server :** 应用服务的名称。若同一个应用服务在多个服务器上进行集群部署，应把多个 server 名称写成一致   
**path :** 应用服务日志文件夹存储的绝对路径

### 2. 启动与关闭
`log-center-start.sh` 文件是日志中心的启动脚本。修改变量值 `jar_path` 为 `haidnor-log-center.jar` 文件的绝对路径。修改 `config_path` 变量值为存放 `server-config.json` 配置文件的绝对路径。

执行命令 `sh log-center-start.sh` 启动日志中心   
执行命令 `sh log-center-stop.sh` 关闭日志中心   

---
## 日志客户端 haidnor-client

### 1. 启动与关闭
`log-client-start.sh` 文件是日志客户端的启动脚本。修改变量值 `jar_path` 为 `haidnor-log-client.jar` 文件的绝对路径。修改 `centerAddress` 变量值为日志中心 Netty 服端的(IP + 端口号)地址。

执行命令 `sh log-client-start.sh` 启动日志客户端   
执行命令 `sh log-client-stop.sh` 关闭日志客户端     

# 特性与机制
## 1.通信数据压缩传递
由于 haidnor-log 为分布式架构，日志中心与日志客户端之间存在大量的网络通信，为了减少网络资源消耗，日志文本信息传递做了压缩处理。实测 100M 的业务日志文件经 ZIP 压缩算法处理后大小约为 10M

## 2.客户端断线重连
日志客户端每间隔 3 秒会向日志中心发送一次心跳，用于注册客户端信息和检测连接是否有效。如果心跳发送失败，客户端将会3秒后再次自动尝试连接。因此，不用日志中心时可以将其关闭，以节省服务器资源，日志中心启动后客户端将会自动连接

## 3.配置文件更新热加载
日志中心启动后会自动读取 `server-config.json` 配置文件，并开启了一个守护线程对配置文件进行监听，每间隔 10 秒扫描一次配置文件的修改时间，如果发生变化将会自动重新加载配置

## 4.服务端消息主动推送
浏览器日志控制台与服务端之间的通信使用了 websocket 技术，用户在控制台上看到的日志内容均为服务端主动推送。日志中心会检测本次推送的日志内容是否与上一次推送的一致，如果没有发生变化将不会重复推送，以减少网络资源消耗

