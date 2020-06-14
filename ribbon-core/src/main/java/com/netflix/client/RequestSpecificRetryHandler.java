package com.netflix.client;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.netflix.client.config.CommonClientConfigKey;
import com.netflix.client.config.IClientConfig;

import javax.annotation.Nullable;
import java.net.SocketException;
import java.util.List;
import java.util.Optional;

/**
 * TODO: Specific: 特征，细节，特殊的，它是和request请求特征相关的重试处理器
 * TODO: Ribbon会为允许请求的每个请求创建的RetryHandler的实例，每个请求可以带有自己的requestConfig,比如每个client请求都可以有自己的retrySameServer和retryNextServer参数
 * Implementation of RetryHandler created for each request which allows for request
 * specific override
 */
public class RequestSpecificRetryHandler implements RetryHandler {

    /**
     * fallback默认使用的是RetryHandler.Default, 有点代理的意思
     */
    private final RetryHandler fallback;
    private int retrySameServer = -1;
    private int retryNextServer = -1;
    /**
     * TODO: 只有是连接异常，也就是SocketException或者其子类异常才执行重试
     */
    private final boolean okToRetryOnConnectErrors;
    /**
     * 若是true, 只要异常了，任何错都执行重试
     */
    private final boolean okToRetryOnAllErrors;
    
    protected List<Class<? extends Throwable>> connectionRelated = 
            Lists.<Class<? extends Throwable>>newArrayList(SocketException.class);

    public RequestSpecificRetryHandler(boolean okToRetryOnConnectErrors, boolean okToRetryOnAllErrors) {
        this(okToRetryOnConnectErrors, okToRetryOnAllErrors, RetryHandler.DEFAULT, null);    
    }
    
    public RequestSpecificRetryHandler(boolean okToRetryOnConnectErrors, boolean okToRetryOnAllErrors, RetryHandler baseRetryHandler, @Nullable IClientConfig requestConfig) {
        Preconditions.checkNotNull(baseRetryHandler);
        this.okToRetryOnConnectErrors = okToRetryOnConnectErrors;
        this.okToRetryOnAllErrors = okToRetryOnAllErrors;
        this.fallback = baseRetryHandler;
        if (requestConfig != null) {
            requestConfig.getIfSet(CommonClientConfigKey.MaxAutoRetries).ifPresent(
                    value -> retrySameServer = value
            );
            requestConfig.getIfSet(CommonClientConfigKey.MaxAutoRetriesNextServer).ifPresent(
                    value -> retryNextServer = value
            );
        }
    }
    
    public boolean isConnectionException(Throwable e) {
        return Utils.isPresentAsCause(e, connectionRelated);
    }

    @Override
    public boolean isRetriableException(Throwable e, boolean sameServer) {
        // TODO: 若强制开启所有错误都重试，那就直接重试呗，默认是false
        if (okToRetryOnAllErrors) {
            return true;
        }
        // TODO: ClientException, 属于执行过程中会抛出的异常类型，所以需要加以判断
        else if (e instanceof ClientException) {
            ClientException ce = (ClientException) e;
            // TODO: 若是服务端异常，那就同一台server上不用重试了，没要求同一台server才允许重试
            if (ce.getErrorType() == ClientException.ErrorType.SERVER_THROTTLED) {
                return !sameServer;
            } else {
                // TODO: 若不是服务端异常类型，那都不要重试了
                return false;
            }
        } 
        else  {
            // TODO: 若不是ClientException, 那就看看异常是否是Socket的连接异常喽
            return okToRetryOnConnectErrors && isConnectionException(e);
        }
    }

    @Override
    public boolean isCircuitTrippingException(Throwable e) {
        return fallback.isCircuitTrippingException(e);
    }

    @Override
    public int getMaxRetriesOnSameServer() {
        if (retrySameServer >= 0) {
            return retrySameServer;
        }
        return fallback.getMaxRetriesOnSameServer();
    }

    @Override
    public int getMaxRetriesOnNextServer() {
        if (retryNextServer >= 0) {
            return retryNextServer;
        }
        return fallback.getMaxRetriesOnNextServer();
    }    
}
