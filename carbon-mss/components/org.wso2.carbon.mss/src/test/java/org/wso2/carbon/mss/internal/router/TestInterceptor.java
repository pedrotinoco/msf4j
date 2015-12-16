/*
 *  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.mss.internal.router;

import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.wso2.carbon.mss.HandlerInfo;
import org.wso2.carbon.mss.HttpResponder;
import org.wso2.carbon.mss.Interceptor;

/**
 * TODO : add class level comment.
 */
public class TestInterceptor implements Interceptor {
    private volatile int numPreCalls = 0;
    private volatile int numPostCalls = 0;

    public int getNumPreCalls() {
        return numPreCalls;
    }

    public int getNumPostCalls() {
        return numPostCalls;
    }

    public void reset() {
        numPreCalls = 0;
        numPostCalls = 0;
    }

    @Override
    public boolean preCall(HttpRequest request, HttpResponder responder, HandlerInfo handlerInfo) {
        ++numPreCalls;

        String header = request.headers().get("X-Request-Type");
        if (header != null && header.equals("Reject")) {
            responder.sendStatus(HttpResponseStatus.NOT_ACCEPTABLE);
            return false;
        }

        if (header != null && header.equals("PreException")) {
            throw new IllegalArgumentException("PreException");
        }

        return true;
    }

    @Override
    public void postCall(HttpRequest request, HttpResponseStatus status, HandlerInfo handlerInfo) {
        ++numPostCalls;
        String header = request.headers().get("X-Request-Type");
        if (header != null && header.equals("PostException")) {
            throw new IllegalArgumentException("PostException");
        }
    }
}
