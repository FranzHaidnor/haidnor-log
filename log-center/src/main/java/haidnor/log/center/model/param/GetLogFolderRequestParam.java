package haidnor.log.center.model.param;

import lombok.Data;

import java.util.HashSet;

@Data
public class GetLogFolderRequestParam {
    /**
     * 需要查询的 ip 节点
     */
    private HashSet<String> ip;
    /**
     * 服务名
     */
    private String server;
}
