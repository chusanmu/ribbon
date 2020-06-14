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

import com.netflix.client.config.IClientConfig;

/**
 * A "VipAddress" is a logical name for a Target Server farm.
 * VIP地址解析器，是目标服务器上的逻辑名称，该处理器帮助解析并获取得到最终的地址
 * @author stonse
 *
 */
public interface VipAddressResolver {
    public String resolve(String vipAddress, IClientConfig niwsClientConfig);
}
