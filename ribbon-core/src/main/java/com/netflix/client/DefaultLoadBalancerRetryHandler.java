/*
 *
 * Copyright 2013 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.netflix.client;

import com.google.common.collect.Lists;
import com.netflix.client.config.CommonClientConfigKey;
import com.netflix.client.config.IClientConfig;

import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.List;

/**
 * A default {@link RetryHandler}. The implementation is limited to
 * known exceptions in java.net. Specific client implementation should provide its own
 * {@link RetryHandler}
 * 
 * @author awang
 */
public class DefaultLoadBalancerRetryHandler implements RetryHandler {

    @SuppressWarnings("unchecked")
    private List<Class<? extends Throwable>> retriable = 
            Lists.<Class<? extends Throwable>>newArrayList(ConnectException.class, SocketTimeoutException.class);
    
    @SuppressWarnings("unchecked")
    private List<Class<? extends Throwable>> circuitRelated = 
            Lists.<Class<? extends Throwable>>newArrayList(SocketException.class, SocketTimeoutException.class);

    protected final int retrySameServer;
    protected final int retryNextServer;
    protected final boolean retryEnabled;

    public DefaultLoadBalancerRetryHandler() {
        this.retrySameServer = 0;
        this.retryNextServer = 0;
        this.retryEnabled = false;
    }
    
    public DefaultLoadBalancerRetryHandler(int retrySameServer, int retryNextServer, boolean retryEnabled) {
        this.retrySameServer = retrySameServer;
        this.retryNextServer = retryNextServer;
        this.retryEnabled = retryEnabled;
    }
    
    public DefaultLoadBalancerRetryHandler(IClientConfig clientConfig) {
        this.retrySameServer = clientConfig.getOrDefault(CommonClientConfigKey.MaxAutoRetries);
        this.retryNextServer = clientConfig.getOrDefault(CommonClientConfigKey.MaxAutoRetriesNextServer);
        this.retryEnabled = clientConfig.getOrDefault(CommonClientConfigKey.OkToRetryOnAllOperations);
    }

    /**
     * 是否可以进行重试的异常
     * TODO: 开启重试的情况下，若是同一台Server, 因为它失败过了，所以需要判断这次的异常类型是啥 是否需要进行重试，若是不同的server, 那就直接重试呗，因为你不知道其是否ok
     * @param e the original exception
     * @param sameServer if true, the method is trying to determine if retry can be
     *        done on the same server. Otherwise, it is testing whether retry can be
     * @return
     */
    @Override
    public boolean isRetriableException(Throwable e, boolean sameServer) {
        /**
         * 1. 若retryEnabled=false全局关闭了禁止重试，那就不管了
         * 2. 若retryEnabled=true，那就继续看
         */
        if (retryEnabled) {
            // TODO: 3.若是在同一太机器上，所以需要看此次异常类型是啥
            if (sameServer) {
                return Utils.isPresentAsCause(e, getRetriableExceptions());
            } else {
                // TODO: 若是不同的server, 那就直接重试呗
                return true;
            }
        }
        return false;
    }

    /**
     * @return true if {@link SocketException} or {@link SocketTimeoutException} is a cause in the Throwable.
     */
    @Override
    public boolean isCircuitTrippingException(Throwable e) {
        return Utils.isPresentAsCause(e, getCircuitRelatedExceptions());        
    }

    @Override
    public int getMaxRetriesOnSameServer() {
        return retrySameServer;
    }

    @Override
    public int getMaxRetriesOnNextServer() {
        return retryNextServer;
    }
    
    protected List<Class<? extends Throwable>> getRetriableExceptions() {
        return retriable;
    }
    
    protected List<Class<? extends Throwable>>  getCircuitRelatedExceptions() {
        return circuitRelated;
    }
}
