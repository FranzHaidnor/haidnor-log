package haidnor.log.center.model.param;

import lombok.Data;

import java.util.HashSet;

@Data
public class GetLogRequestParam {
    /**
     * 需要查询的 ip 节点
     */
    private HashSet<String> ips;
    /**
     * 服务名
     */
    private String server;
    /**
     * 查看日志的日期
     */
    private String day;
    /**
     * 日志文件名称
     */
    private String fileName;
    /**
     * 每一个服务节点查看的日志行数
     */
    private Integer rows;
    /**
     * 是否输出 ip 节点前缀信息
     */
    private boolean showIp;
}
