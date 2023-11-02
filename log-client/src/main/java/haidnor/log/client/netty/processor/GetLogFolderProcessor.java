package haidnor.log.client.netty.processor;

import cn.hutool.core.io.FileUtil;
import haidnor.log.common.model.GetLogFolderRequest;
import haidnor.log.common.model.GetLogRequest;
import haidnor.log.common.util.Jackson;
import haidnor.log.common.util.LogUtil;
import haidnor.remoting.core.NettyRequestProcessor;
import haidnor.remoting.protocol.RemotingCommand;
import haidnor.remoting.protocol.RemotingSysResponseCode;
import io.netty.channel.ChannelHandlerContext;

import java.io.File;
import java.io.FileFilter;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GetLogFolderProcessor implements NettyRequestProcessor {

    @Override
    public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand remotingCommand) {
        try {
            GetLogFolderRequest request = Jackson.toBean(remotingCommand.getBody(), GetLogFolderRequest.class);
            Set<String> folderNameSet = new HashSet<>();
            List<File> files = FileUtil.loopFiles(FileUtil.file(request.getPath()), 1, File::isDirectory);
            for (File file : files) {
                folderNameSet.add(file.getName());
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
