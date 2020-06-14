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

import java.io.Closeable;
import java.net.URI;
import java.util.Map;

/**
 * TODO: 注意 此接口和协议无关，所以没有getStatus(), getStatus()属于http专属
 * Response interface for the client framework.  
 *
 */
public interface IResponse extends Closeable
{
   
   /**
    * TODO: 从响应中获得实体，若是http协议，那就是body体,
    * Returns the raw entity if available from the response 
    */
   public Object getPayload() throws ClientException;
      
   /**
    * A "peek" kinda API. Use to check if your service returned a response with an Entity
    */
   public boolean hasPayload();
   
   /**
    * TODO: 如果认为响应成功，则为真，例如http协议的200
    * @return true if the response is deemed success, for example, 200 response code for http protocol.
    */
   public boolean isSuccess();
   
      
   /**
    * Return the Request URI that generated this response
    */
   public URI getRequestedURI();
   
   /**
    * TODO: 响应头们
    * @return Headers if any in the response.
    */
   public Map<String, ?> getHeaders();   
}
