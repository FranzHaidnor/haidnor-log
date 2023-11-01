package haidnor.log.center.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import haidnor.log.center.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@RestControllerAdvice
@Slf4j
public class ControllerExceptionAdvice {

    /**
     * 获取异常将要的堆栈信息
     */
    private static String getStackInfo(Exception exception) {
        StackTraceElement[] stackTrace = exception.getStackTrace();
        if (stackTrace.length < 1) {
            return exception.getMessage();
        }
        StackTraceElement traceElement = exception.getStackTrace()[0];
        return String.format("Exception: %s Class: %s LineNumber: %s", exception, traceElement.getClassName(), traceElement.getLineNumber());
    }

    /**
     * 程序异常
     * 该异常表示程序运行时异常. 开发者不应该在代码中编写抛出此异常, 例如 NullPointerException, IndexOutOfBoundsException
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result exception(Exception exception) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            HttpServletRequest request = requestAttributes.getRequest();
            try {
                log.error("Global Exception Message: URI:{} Method:{} Parameter:{}", request.getRequestURI(), request.getMethod(), new ObjectMapper().writeValueAsString(request.getParameterMap()), exception);
            } catch (JsonProcessingException jsonProcessingException) {
                log.error("ControllerExceptionAdvice jsonProcessingException Message:", jsonProcessingException);
                log.error("Global Exception Message:", exception);
            }
        } else {
            log.error("Global Exception Message:", exception);
        }
        return Result.fail(exception.getMessage(), getStackInfo(exception));
    }

}
