package haidnor.log.client.netty.processor;

import haidnor.log.common.model.GetLogRequest;
import haidnor.log.common.util.Jackson;
import haidnor.log.common.util.LogUtil;
import haidnor.remoting.core.NettyRequestProcessor;
import haidnor.remoting.protocol.RemotingCommand;
import haidnor.remoting.protocol.RemotingSysResponseCode;
import io.netty.channel.ChannelHandlerContext;

import java.nio.charset.StandardCharsets;

public class GetLogProcessor implements NettyRequestProcessor {

    @Override
    public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand remotingCommand) {
        GetLogRequest request = Jackson.toBean(remotingCommand.getBody(), GetLogRequest.class);
        try {
            String log = LogUtil.readLastRows(request.getPath() + "/" + request.getDay() + "/" + request.getFileName(), request.getRows());
            return RemotingCommand.createResponse(RemotingSysResponseCode.SUCCESS, log.getBytes(StandardCharsets.UTF_8));
        } catch (Exception exception) {
            return RemotingCommand.createResponse(RemotingSysResponseCode.SYSTEM_ERROR, exception.getMessage());
        }
    }

    @Override
    public boolean rejectRequest() {
        return NettyRequestProcessor.super.rejectRequest();
    }

}
