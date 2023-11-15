package haidnor.remoting.protocol;


import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * RandomAccessFile 数据分片响应
 */
@Slf4j
@Setter
@Getter
public class RandomAccessFileResponseCommand extends RemotingCommand {

    /**
     * 消息体
     */
    private RandomAccessFile accessFile;

    private int length;

    public static RandomAccessFileResponseCommand createResponse(int responseCode, String remark, RandomAccessFile accessFile) {
        RandomAccessFileResponseCommand cmd = new RandomAccessFileResponseCommand();
        cmd.markResponseType();
        cmd.setResponseCode(responseCode);
        cmd.setRemark(remark);
        cmd.setAccessFile(accessFile);
        cmd.setLength(getReadLength(accessFile));
        return cmd;
    }

    @Override
    public ByteBuffer encode() {
        byte[] remarkBytes = null;
        if (remark != null) {
            remarkBytes = remark.getBytes(StandardCharsets.UTF_8);
        }

        int remarkLength = remarkBytes != null ? remarkBytes.length : 0;  // 备注文本长度
        int bodyLength = accessFile != null ? length : 0;                 // 消息体长度

        ByteBuffer buffer = ByteBuffer.allocate(28 + remarkLength + 4);

        // 4 > 消息的总长度(被 LengthFieldBasedFrameDecoder 解析)
        buffer.putInt(28 + remarkLength + bodyLength);

        // 4 > 具体的指令字符串哈希码
        buffer.putInt(commandHashCode);                 // > 4

        // 4 > 响应码
        buffer.putInt(responseCode);                    // > 8

        // 4 > 类型 0:请求 1:响应
        buffer.putInt(rpcType);                         // > 12

        // 4 > 是否为单向发送的类型 0:不是 1:是
        buffer.putInt(rpcOneway);                       // > 16

        // 4 > 请求的唯一 id
        buffer.putInt(opaque);                          // > 20

        // 4 > 备注文本内容长度
        buffer.putInt(remarkLength);                    // > 24

        // 若干字节 > 备注文本内容
        if (remarkLength != 0) {
            buffer.put(remarkBytes);                    // > remarkLength
        }

        // 4 > 消息体长度
        buffer.putInt(bodyLength);                      // > 28

                                                        // > bodyLength
        // https://stackoverflow.com/questions/61267495/exception-in-thread-main-java-lang-nosuchmethoderror-java-nio-bytebuffer-flip
         ((Buffer) buffer).flip();
        return buffer;
    }

    public RemotingCommandType getType() {
        return RemotingCommandType.RESPONSE_COMMAND;
    }

    private static int getReadLength(RandomAccessFile accessFile) {
        try {
            return (int) (accessFile.length() - accessFile.getFilePointer());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}