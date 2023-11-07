package haidnor.log.client.netty.processor;

import cn.hutool.core.io.FileUtil;
import haidnor.log.common.model.GetLogFolderRequest;
import haidnor.log.common.util.Jackson;
import haidnor.remoting.core.NettyRequestProcessor;
import haidnor.remoting.protocol.RemotingCommand;
import haidnor.remoting.protocol.RemotingSysResponseCode;
import io.netty.channel.ChannelHandlerContext;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GetLogFolderProcessor implements NettyRequestProcessor {

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand remotingCommand) {
        try {
            GetLogFolderRequest request = Jackson.toBean(remotingCommand.getBody(), GetLogFolderRequest.class);
            Set<String> folderNameSet = new HashSet<>();
            List<File> files = FileUtil.loopFiles(FileUtil.file(request.getPath()), 1, File::isDirectory);
            for (File file : files) {
                try {
                    simpleDateFormat.parse(file.getName());
                    folderNameSet.add(file.getName());
                } catch (Exception exception) {
                    // nop
                }
            }
            return RemotingCommand.createResponse(RemotingSysResponseCode.SUCCESS, Jackson.toJsonBytes(folderNameSet));
        } catch (Exception exception) {
            return RemotingCommand.createResponse(RemotingSysResponseCode.SYSTEM_ERROR, exception.getMessage());
        }
    }

    @Override
    public boolean rejectRequest() {
        return NettyRequestProcessor.super.rejectRequest();
    }

}
