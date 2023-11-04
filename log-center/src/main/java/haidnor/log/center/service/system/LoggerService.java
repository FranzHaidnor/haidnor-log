package haidnor.log.center.service.system;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LoggerService {

    public void updateLogLevel(String packageName, String level) {
        level = level.toUpperCase();
        if (!(level.equals("INFO") || level.equals("DEBUG"))) {
            throw new RuntimeException("修改日志级别仅支持 INFO, DEBUG");
        }
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerContext.getLogger(packageName).setLevel(Level.valueOf(level));
        log.info("修改 Logger 日志输出级别. PackageName:{} Level:{}", packageName, level);
    }

    public String getLogLevel(String packageName) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger logger = loggerContext.getLogger(packageName);
        return logger.getLevel().levelStr;
    }

}
