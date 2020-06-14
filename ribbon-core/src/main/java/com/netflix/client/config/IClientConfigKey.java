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
package com.netflix.client.config;

/**
 * Defines the key used in {@link IClientConfig}. See {@link CommonClientConfigKey}
 * for the commonly defined client configuration keys.
 *  TODO: 用于定义用于IClientConfig使用的key, 仅仅是key
 * @author awang
 *
 */
public interface IClientConfigKey<T> {

    @SuppressWarnings("rawtypes")
    public static final class Keys extends CommonClientConfigKey {
        private Keys(String configKey) {
            super(configKey);
        }
    }
    
	/**
	 * @return string representation of the key used for hash purpose.
	 * 用于做hash的字符串表现形式
	 */
	public String key();
	
	/**
     * @return Data type for the key. For example, Integer.class.
	 * key的类型，比如Integer.class, 若不指定会根据泛型类型自动判断出来
	 */
	public Class<T> type();
}
