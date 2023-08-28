package haidnor.log.common.model;

import lombok.Data;

@Data
public class GetLogRequest {

    /**
     * 日志父文件夹路径
     * 例如:  D:\log\vip 或 D:\log\vip
     */
    private String path;

    /**
     * 日志文件日期
     * 例如:error-log.log
     */
    private String day;

    /**
     * 文件名称
     * 例如:error-log.log
     */
    private String fileName;

    /**
     * 读取日志文件行数
     */
    private Integer rows;

}
