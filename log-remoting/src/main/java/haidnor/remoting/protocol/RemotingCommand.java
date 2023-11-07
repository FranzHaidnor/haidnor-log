package haidnor.remoting.protocol;


import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Setter
@Getter
public class RemotingCommand {

    private static final AtomicInteger requestId = new AtomicInteger(0);
    /**
     * 指令字符串哈希码
     */
    private int commandHashCode;

    /**
     * 请求的唯一 id
     */
    private int opaque = requestId.getAndIncrement();

    /**
     * 响应码 {@link RemotingSysResponseCode}
     */
    private int responseCode = -1;

    /**
     * 类型 0:请求 1:响应 {@link RemotingCommandType}
     */
    private int rpcType = 0;

    /**
     * 是否为单向发送的类型 0:不是 1:是
     */
    private int rpcOneway = 1;

    /**
     * 备注文本内容
     */
    private String remark;

    /**
     * 消息体
     */
    private byte[] body;

    private RemotingCommand() {
    }

    public static <T extends Enum<T>> RemotingCommand creatRequest(T commandEnum) {
        return creatRequest(commandEnum, null, null);
    }

    public static <T extends Enum<T>> RemotingCommand creatRequest(T commandEnum, byte[] body) {
        return creatRequest(commandEnum, null, body);
    }

    public static <T extends Enum<T>> RemotingCommand creatRequest(T commandEnum, String remark) {
        return creatRequest(commandEnum, remark, null);
    }

    public static <T extends Enum<T>> RemotingCommand creatRequest(T commandEnum, String remark, byte[] body) {
        RemotingCommand cmd = new RemotingCommand();
        cmd.markReqeustType();
        cmd.setCommandHashCode(commandEnum.name().hashCode());
        cmd.setRemark(remark);
        cmd.setBody(body);
        return cmd;
    }

    public static RemotingCommand createResponse(int responseCode) {
        return createResponse(responseCode, null, null);
    }

    public static RemotingCommand createResponse(int responseCode, byte[] body) {
        return createResponse(responseCode, null, body);
    }

    public static RemotingCommand createResponse(int responseCode, String remark) {
        return createResponse(responseCode, remark, null);
    }

    public static RemotingCommand createResponse(int responseCode, String remark, byte[] body) {
        RemotingCommand cmd = new RemotingCommand();
        cmd.markResponseType();
        cmd.setResponseCode(responseCode);
        cmd.setRemark(remark);
        cmd.setBody(body);
        return cmd;
    }

    public ByteBuffer encode() {
        byte[] remarkBytes = null;
        if (remark != null) {
            remarkBytes = remark.getBytes(StandardCharsets.UTF_8);
        }
        int remarkLength = remarkBytes != null ? remarkBytes.length : 0;
        int bodyLength = body != null ? body.length : 0;
        int length = 28 + remarkLength + bodyLength;

        ByteBuffer buffer = ByteBuffer.allocate(length + 4);

        // 4 > 消息的总长度(被 LengthFieldBasedFrameDecoder 解析)
        buffer.putInt(length);

        // 4 > 具体的指令字符串哈希码
        buffer.putInt(commandHashCode);

        // 4 > 响应码
        buffer.putInt(responseCode);

        // 4 > 类型 0:请求 1:响应
        buffer.putInt(rpcType);

        // 4 > 是否为单向发送的类型 0:不是 1:是
        buffer.putInt(rpcOneway);

        // 4 > 请求的唯一 id
        buffer.putInt(opaque);

        // 4 > 备注文本内容长度
        buffer.putInt(remarkLength);

        // 若干字节 > 备注文本内容
        if (remarkLength != 0) {
            buffer.put(remarkBytes);
        }

        // 4 > 消息体长度
        buffer.putInt(bodyLength);

        // 若干字节 > 消息体内容
        if (bodyLength != 0) {
            buffer.put(body);
        }

        // https://stackoverflow.com/questions/61267495/exception-in-thread-main-java-lang-nosuchmethoderror-java-nio-bytebuffer-flip
        ((Buffer)buffer).flip();
        return buffer;
    }

    public static RemotingCommand decode(final ByteBuffer buffer) {
        RemotingCommand command = new RemotingCommand();

        // 4 > 具体的指令字符串哈希码
        int commandHashCode = buffer.getInt();
        command.setCommandHashCode(commandHashCode);

        // 4 > 响应码
        int code = buffer.getInt();
        command.setResponseCode(code);

        // 4 > 类型 0:请求 1:响应
        int rpcType = buffer.getInt();
        command.setRpcType(rpcType);

        // 4 > 是否为单向发送的类型 0:不是 1:是
        int rpcOneway = buffer.getInt();
        command.setRpcOneway(rpcOneway);

        // 4 > 请求的唯一 id
        int opaque = buffer.getInt();
        command.setOpaque(opaque);

        // 4 > 备注文本内容长度
        int remarkLength = buffer.getInt();

        // 若干字节 > 备注文本内容
        if (remarkLength != 0) {
            byte[] remark = new byte[remarkLength];
            buffer.get(remark);
            command.setRemark(new String(remark, StandardCharsets.UTF_8));
        }

        // 4 > 消息体长度
        int bodyLength = buffer.getInt();

        // 若干字节 > 消息体内容
        if (bodyLength != 0) {
            byte[] body = new byte[bodyLength];
            buffer.get(body);
            command.setBody(body);
        }

        return command;
    }

    public void markResponseType() {
        this.rpcType = 1;
    }

    public void markReqeustType() {
        this.rpcType = 0;
    }

    public void markOnewayRPC() {
        this.rpcOneway = 1;
    }

    public boolean isOnewayRPC() {
        return rpcOneway == 1;
    }

    public RemotingCommandType getType() {
        if (rpcType == 0) {
            return RemotingCommandType.REQUEST_COMMAND;
        } else {
            return RemotingCommandType.RESPONSE_COMMAND;
        }
    }

}