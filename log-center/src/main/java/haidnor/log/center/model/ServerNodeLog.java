package haidnor.log.center.model;

import lombok.Data;

@Data
public class ServerNodeLog {

    /**
     * 节点 ip
     */
    private String ip;

    /**
     * 服务名称
     */
    private String server;

    /**
     * 日志文件文件夹
     */
    private String path;

}
