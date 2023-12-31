package haidnor.log.client.netty.processor;

import haidnor.log.common.model.GetLogRequest;
import haidnor.log.common.util.Jackson;
import haidnor.log.common.util.LogUtil;
import haidnor.remoting.core.NettyRequestProcessor;
import haidnor.remoting.protocol.RandomAccessFileResponseCommand;
import haidnor.remoting.protocol.RemotingCommand;
import haidnor.remoting.protocol.RemotingSysResponseCode;
import io.netty.channel.ChannelHandlerContext;

import java.io.RandomAccessFile;

public class GetLogProcessor implements NettyRequestProcessor {

    @Override
    public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand remotingCommand) {
        GetLogRequest request = Jackson.toBean(remotingCommand.getBody(), GetLogRequest.class);
        try {
//            byte[] bytes = LogUtil.readLastRows(request.getPath() + "/" + request.getDay() + "/" + request.getFileName(), request.getRows());
//            byte[] zlib = ZipUtil.zlib(bytes, 1);

            // 使用分片传输
            RandomAccessFile randomAccessFile = LogUtil.readLastRowsAndGetRandomAccessFile(request.getPath() + "/" + request.getDay() + "/" + request.getFileName(), request.getRows());
            return RandomAccessFileResponseCommand.createResponse(RemotingSysResponseCode.SUCCESS, null, randomAccessFile);
        } catch (Exception exception) {
            return RemotingCommand.createResponse(RemotingSysResponseCode.SYSTEM_ERROR, exception.getMessage());
        }
    }

    @Override
    public boolean rejectRequest() {
        return NettyRequestProcessor.super.rejectRequest();
    }

}
