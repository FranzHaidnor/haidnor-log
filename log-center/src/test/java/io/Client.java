package io;

import haidnor.remoting.core.NettyClientConfig;
import haidnor.remoting.core.NettyRemotingClient;
import haidnor.remoting.protocol.RemotingCommand;
import lombok.SneakyThrows;
import server.Command;

public class Client {

    @SneakyThrows
    public static void main(String[] args) {
        NettyClientConfig config = new NettyClientConfig();
        config.setFrameMaxLength(Integer.MAX_VALUE);
        config.setClientSocketSndBufSize(Integer.MAX_VALUE);
        config.setClientSocketRcvBufSize(Integer.MAX_VALUE);
        NettyRemotingClient client = new NettyRemotingClient(config);

        // 同步
        {
            RemotingCommand request = RemotingCommand.creatRequest(Command.HEARTBEAT);
            RemotingCommand response = client.invokeSync("127.0.0.1:8085", request);
            System.out.println(response);
        }

//        // 异步
//        {
//            RemotingCommand request = RemotingCommand.creatRequest(Command.HEARTBEAT, null, null);
//            client.invokeAsync("127.0.0.1:8085", request, responseFuture -> {
//                RemotingCommand response = responseFuture.getResponseCommand();
//                System.out.println(new String(response.getBody(), StandardCharsets.UTF_8));
//            });
//        }
//
//        // 单向发送
//        {
//            RemotingCommand request = RemotingCommand.creatRequest(Command.HEARTBEAT, null, null);
//            client.invokeOneway("127.0.0.1:8085", request);
//        }
    }


}

