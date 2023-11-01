package haidnor.log.common.util;

import cn.hutool.core.date.DateUtil;
import haidnor.log.common.model.LogLine;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 日志合并工具
 *
 * @author wang xiang
 */
public class LogUtil {

    /**
     * 读取文本文件的倒数多少行内容
     *
     * @param filePath 文件路径
     * @param rows     需要读取的行数. 0 则为全部内容
     * @author wang xiang
     */
    public static String readLastRows(String filePath, long rows) {
        rows = rows - 1;
        try (RandomAccessFile accessFile = new RandomAccessFile(filePath, "r")) {
            // 每次读取的字节数要和系统换行符大小一致
            byte[] charBytes = new byte[1];
            // 在获取到指定行数和读完文档之前,从文档末尾向前移动指针,遍历文档每一个字节
            for (long pointer = accessFile.length(), lineSeparatorNum = 0; pointer >= 0 && lineSeparatorNum < rows + 1; ) {
                // 移动指针
                accessFile.seek(pointer--);
                // 读取数据
                int readLength = accessFile.read(charBytes);
                if (readLength != -1 && charBytes[0] == 10) {
                    lineSeparatorNum++;
                }
                // 扫描完依然没有找到足够的行数,将指针归0
                if (pointer == -1 && lineSeparatorNum < rows + 1) {
                    accessFile.seek(0);
                }
            }
            byte[] tempBytes = new byte[(int) (accessFile.length() - accessFile.getFilePointer())];
            accessFile.readFully(tempBytes);
            return new String(tempBytes, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * 多个日志文件交织合并成一个日志文件
     */
    public static List<String> margeLog(List<LogLine> lineList, boolean showIp) {
        List<Log> logList = new ArrayList<>();
        Log log = null;
        for (LogLine line : lineList) {
            // 校验文本内容是否以指定时间格式开头 (2000-01-01 23:23:23.999)
            if (line.getContent().length() >= 24 && Pattern.matches("^((([0-9]{3}[1-9]|[0-9]{2}[1-9][0-9]{1}|[0-9]{1}[1-9][0-9]{2}|[1-9][0-9]{3})-(((0[13578]|1[02])-(0[1-9]|[12][0-9]|3[01]))|((0[469]|11)-(0[1-9]|[12][0-9]|30))|(02-(0[1-9]|[1][0-9]|2[0-8]))))|((([0-9]{2})(0[48]|[2468][048]|[13579][26])|((0[48]|[2468][048]|[3579][26])00))-02-29))\\s+([0-1]?[0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9]).([0-9][0-9][0-9]).*$", line.getContent().substring(0, 24))) {
                log = new Log(DateUtil.parse(line.getContent().substring(0, 24)).getTime());
                if (showIp) {
                    log.addLine(line.getIp() + " | " + line.getContent());
                } else {
                    log.addLine(line.getContent());
                }

                logList.add(log);
            } else {
                if (log != null) {
                    if (showIp) {
                        log.addLine(line.getIp() + " | " + line.getContent());
                    } else {
                        log.addLine(line.getContent());
                    }
                }
            }
        }

        // 日志信息对象按时间戳升序排序后归并为一个集合
        List<String> logLineList = logList.stream().sorted(Comparator.comparing(Log::getTimestamp)).map(Log::getContent)
                .reduce((l1, l2) -> {
                    l1.addAll(l2);
                    return l1;
                }).orElse(new ArrayList<>());
        return logLineList;
    }

    private static class Log {
        /**
         * 日志内容的时间戳
         */
        private final Long timestamp;
        /**
         * 日志文本内容
         */
        private final List<String> content = new LinkedList<>();

        public Log(Long timestamp) {
            this.timestamp = timestamp;
        }

        public Long getTimestamp() {
            return timestamp;
        }

        public List<String> getContent() {
            return content;
        }

        public void addLine(String line) {
            this.content.add(line);
        }
    }

}
