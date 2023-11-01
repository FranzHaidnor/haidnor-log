package haidnor.log.center.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Result {

    /**
     * 成功状态码
     */
    public static final Integer SUCCEED_CODE = 0;

    /**
     * 失败状态码
     */
    public static final Integer FAIL_CODE = 1;

    /**
     * 响应码. 0 成功, 1 失败
     */
    private Integer code;

    /**
     * 异常堆栈信息
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String stack;

    /**
     * 接口信息
     */
    private String message;

    /**
     * 响应数据
     */
    private Object data;

    private Result(Integer code, String message, Object data, String stack) {
        this.message = message;
        this.code = code;
        this.data = data;
        this.stack = stack;
    }

    public static Result success() {
        return new Result(SUCCEED_CODE, "success", null, null);
    }

    public static Result success(Object data) {
        return new Result(SUCCEED_CODE, "success", data, null);
    }

    public static Result fail() {
        return new Result(FAIL_CODE, "fail", null, null);
    }

    public static Result fail(String message) {
        return new Result(FAIL_CODE, message, null, null);
    }

    public static Result fail(String message, String stack) {
        return new Result(FAIL_CODE, message, null, stack);
    }

}
