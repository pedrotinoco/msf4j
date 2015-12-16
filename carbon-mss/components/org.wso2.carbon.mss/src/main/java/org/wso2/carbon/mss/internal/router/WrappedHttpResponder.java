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

import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.mss.ChunkResponder;
import org.wso2.carbon.mss.HandlerInfo;
import org.wso2.carbon.mss.HttpResponder;
import org.wso2.carbon.mss.Interceptor;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import javax.annotation.Nullable;

/**
 * Wrap HttpResponder to call post handler hook.
 */
final class WrappedHttpResponder implements HttpResponder {
    private static final Logger LOG = LoggerFactory.getLogger(WrappedHttpResponder.class);

    private final HttpResponder delegate;
    private final Iterable<? extends Interceptor> interceptors;
    private final HttpRequest httpRequest;
    private final HandlerInfo handlerInfo;

    public WrappedHttpResponder(HttpResponder delegate, Iterable<? extends Interceptor> interceptors,
                                HttpRequest httpRequest, HandlerInfo handlerInfo) {
        this.delegate = delegate;
        this.interceptors = interceptors;
        this.httpRequest = httpRequest;
        this.handlerInfo = handlerInfo;
    }


    @Override
    public void sendJson(HttpResponseStatus status, Object object) {
        delegate.sendJson(status, object);
        runInterceptor(status);
    }

    @Override
    public void sendJson(HttpResponseStatus status, Object object, Type type) {
        delegate.sendJson(status, object, type);
        runInterceptor(status);
    }

    @Override
    public void sendJson(HttpResponseStatus status, Object object, Type type, Gson gson) {
        delegate.sendJson(status, object, type, gson);
        runInterceptor(status);
    }

    @Override
    public void sendString(HttpResponseStatus status, String data) {
        delegate.sendString(status, data);
        runInterceptor(status);
    }

    @Override
    public void sendString(HttpResponseStatus status, String data, @Nullable Multimap<String, String> headers) {
        delegate.sendString(status, data, headers);
        runInterceptor(status);
    }

    @Override
    public void sendStatus(HttpResponseStatus status) {
        delegate.sendStatus(status);
        runInterceptor(status);
    }

    @Override
    public void sendStatus(HttpResponseStatus status, Multimap<String, String> headers) {
        delegate.sendStatus(status, headers);
        runInterceptor(status);
    }

    @Override
    public void sendByteArray(HttpResponseStatus status, byte[] bytes, Multimap<String, String> headers) {
        delegate.sendByteArray(status, bytes, headers);
        runInterceptor(status);
    }

    @Override
    public void sendBytes(HttpResponseStatus status, ByteBuffer buffer, Multimap<String, String> headers) {
        delegate.sendBytes(status, buffer, headers);
        runInterceptor(status);
    }

    @Override
    public ChunkResponder sendChunkStart(final HttpResponseStatus status, Multimap<String, String> headers) {
        final ChunkResponder chunkResponder = delegate.sendChunkStart(status, headers);
        return new ChunkResponder() {
            @Override
            public void sendChunk(ByteBuffer chunk) throws IOException {
                chunkResponder.sendChunk(chunk);
            }

            @Override
            public void sendChunk(ByteBuf chunk) throws IOException {
                chunkResponder.sendChunk(chunk);
            }

            @Override
            public void close() throws IOException {
                chunkResponder.close();
                runInterceptor(status);
            }
        };
    }

    @Override
    public void sendContent(HttpResponseStatus status, ByteBuf content, String contentType,
                            Multimap<String, String> headers) {
        delegate.sendContent(status, content, contentType, headers);
        runInterceptor(status);
    }

    @Override
    public void sendFile(File file, String contentType, Multimap<String, String> headers) throws IOException {
        delegate.sendFile(file, contentType, headers);
        runInterceptor(HttpResponseStatus.OK);
    }

    private void runInterceptor(HttpResponseStatus status) {
        for (Interceptor interceptor : interceptors) {  //TODO: Fixme Azeez
            try {
                interceptor.postCall(httpRequest, status, handlerInfo);
            } catch (Throwable t) {
                LOG.error("Post handler hook threw exception: ", t);
            }
        }
    }
}
