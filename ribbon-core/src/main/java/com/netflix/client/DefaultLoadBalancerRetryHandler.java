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

import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.List;

import com.google.common.collect.Lists;
import com.netflix.client.config.CommonClientConfigKey;
import com.netflix.client.config.DefaultClientConfigImpl;
import com.netflix.client.config.IClientConfig;

/**
 * A default {@link RetryHandler}. The implementation is limited to
 * known exceptions in java.net. Specific client implementation should provide its own
 * {@link RetryHandler}
 *  TODO: 默认的重试实现，它只能识别java.net里的异常做出判断，若有其他异常，继承子类复写相关的方法
 * @author awang
 *
 * @param <T> Type of request
 * @param <S> Type of response
 */
public class DefaultLoadBalancerRetryHandler implements RetryHandler {

    /**
     * TODO: 这两个异常会进行重试，代表连接不上，这时候重试是可以的
     */
    @SuppressWarnings("unchecked")
    private List<Class<? extends Throwable>> retriable =
            Lists.<Class<? extends Throwable>>newArrayList(ConnectException.class, SocketTimeoutException.class);

    /**
     * Ciruit的异常类型
     */
    @SuppressWarnings("unchecked")
    private List<Class<? extends Throwable>> circuitRelated = 
            Lists.<Class<? extends Throwable>>newArrayList(SocketException.class, SocketTimeoutException.class);
    /* ---------------- 都可以通过IClientConfig配置 -------------- */

    /**
     * 默认为0，表示同一台机器上不重试(只执行一次，失败了就失败了)
     */
    protected final int retrySameServer;
    /**
     * 默认值是1，也就是只会再试下面一台机器，不行就不行了
     */
    protected final int retryNextServer;
    /**
     * 重试开关，true开启重试，false 不开启重试
     */
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
        this.retrySameServer = clientConfig.get(CommonClientConfigKey.MaxAutoRetries, DefaultClientConfigImpl.DEFAULT_MAX_AUTO_RETRIES);
        this.retryNextServer = clientConfig.get(CommonClientConfigKey.MaxAutoRetriesNextServer, DefaultClientConfigImpl.DEFAULT_MAX_AUTO_RETRIES_NEXT_SERVER);
        this.retryEnabled = clientConfig.get(CommonClientConfigKey.OkToRetryOnAllOperations, false);
    }
    
    @Override
    public boolean isRetriableException(Throwable e, boolean sameServer) {
        if (retryEnabled) {
            return Utils.isPresentAsCause(e, getRetriableExceptions());
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
