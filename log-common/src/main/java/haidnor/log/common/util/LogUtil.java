package haidnor.log.common.util;

import haidnor.log.common.model.LogLine;
import lombok.Getter;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

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
    public static String margeLog(List<LogLine> lineList, boolean showIp) {
        List<Stack> logList = new ArrayList<>();
        Stack stack = null;
        long time = -1L;
        for (LogLine line : lineList) {
            // 解析文本开头的时间
            String content = line.content;
            if (content.length() >= 23) {
                time = TimeUtil.parseTime(content.substring(0, 23));
            }
            if (time != -1L) {
                stack = new Stack(time, line.ip);
                stack.addLine(content);
                logList.add(stack);
            } else {
                if (stack != null) {
                    stack.addLine(content);
                }
            }
            time = -1L;
        }

        if (showIp) {
            List<Stack> collect = logList.stream()
                    .sorted(Comparator.comparing(Stack::getTimestamp))
                    .collect(Collectors.toList());
            StringBuilder sb = new StringBuilder();
            for (Stack s : collect) {
                for (String line : s.content) {
                    sb.append(s.ipStr).append(" | ").append(line).append('\n');
                }
            }
            return sb.toString();
        }

        // 日志信息对象按时间戳升序排序后归并为一个集合
        List<String> strings = logList.stream()
                .sorted(Comparator.comparing(Stack::getTimestamp))
                .map(Stack::getContent)
                .reduce((l1, l2) -> {
                    l1.addAll(l2);
                    return l1;
                }).get();

        return String.join("\n", strings);
    }

    @Getter
    private static class Stack {
        /**
         * 日志内容的时间戳
         */
        public final Long timestamp;

        public final String ipStr;
        /**
         * 日志文本内容
         */
        public final List<String> content = new LinkedList<>();

        public Stack(Long timestamp, String ipStr) {
            this.timestamp = timestamp;
            this.ipStr = ipStr;
        }

        public void addLine(String line) {
            this.content.add(line);
        }
    }

    private static class TimeUtil {

        private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

        public static long parseTime(String dateCharSequence) {
            try {
                return simpleDateFormat.parse(dateCharSequence).getTime();
            } catch (ParseException e) {
                return -1;
            }
        }
    }

}
