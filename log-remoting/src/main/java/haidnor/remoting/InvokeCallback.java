package haidnor.remoting;


import haidnor.remoting.core.ResponseFuture;

public interface InvokeCallback {
    void operationComplete(final ResponseFuture responseFuture);
}
