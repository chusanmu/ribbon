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

/**
 * A handler that determines if an exception is retriable for load balancer,
 * and if an exception or error response should be treated as circuit related failures
 * so that the load balancer can avoid such server.
 *   TODO: 重试处理器
 * @author awang
 */
public interface RetryHandler {

    public static final RetryHandler DEFAULT = new DefaultLoadBalancerRetryHandler();
    
    /**
     * Test if an exception is retriable for the load balancer
     *  TODO: 该异常是否可处理，可重试 true表示在同一台机器上重试，否则去其他机器上重试
     * @param request Request that causes such exception
     * @param sameServer if true, the method is trying to determine if retry can be 
     *        done on the same server. Otherwise, it is testing whether retry can be
     *        done on a different server
     */
    public boolean isRetriableException(Throwable e, boolean sameServer);

    /**
     * TODO: 是否是Ciruit熔断类型异常，比如,java.net.ConnectException就属于这种故障，这种异常比较严重，一旦发生的次数多了，就会把它熔断
     * Test if an exception should be treated as circuit failure. For example, 
     * a {@link ConnectException} is a circuit failure. This is used to determine
     * whether successive exceptions of such should trip the circuit breaker to a particular
     * host by the load balancer. If false but a server response is absent, 
     * load balancer will also close the circuit upon getting such exception.
     */
    public boolean isCircuitTrippingException(Throwable e);
        
    /**
     * Number of maximal retries to be done on one server
     * TODO: 要在一台服务器上执行的最大重试次数
     */
    public int getMaxRetriesOnSameServer();

    /**
     * TODO: 要重试的最大不同服务器数，2表示最多去2台不同的服务器身上重试
     * Number of maximal different servers to retry
     */
    public int getMaxRetriesOnNextServer();
}
