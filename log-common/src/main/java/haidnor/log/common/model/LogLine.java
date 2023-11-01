package haidnor.log.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 日志行信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogLine {

    private String content;

    private String ip;

}
