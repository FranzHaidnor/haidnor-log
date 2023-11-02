package haidnor.log.common.model;

import lombok.Data;

@Data
public class GetLogFolderRequest {

    /**
     * 日志父文件夹路径
     * 例如:  D:\log\vip 或 D:\log\vip
     */
    private String path;

}
