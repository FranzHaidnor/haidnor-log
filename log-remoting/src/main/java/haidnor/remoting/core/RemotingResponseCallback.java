package haidnor.remoting.core;

import haidnor.remoting.protocol.RemotingCommand;

public interface RemotingResponseCallback {
    void callback(RemotingCommand response);
}
