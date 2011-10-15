/*
 * Copyright 2010 Alibaba Group Holding Limited.
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.citrus.service.moduleloader.impl.adapter;

import static com.alibaba.citrus.springext.util.SpringExtUtil.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.internal.ActionEventUtil.*;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.citrus.service.moduleloader.ActionEventException;
import com.alibaba.citrus.service.moduleloader.ActionEventNotFoundException;
import com.alibaba.citrus.util.ToStringBuilder;
import com.alibaba.citrus.util.ToStringBuilder.MapBuilder;

public class ActionEventAdapter extends AbstractDataBindingAdapter implements InitializingBean {
    private final Map<String, MethodInvoker> handlers;
    private final MethodInvoker preHandler;
    private final MethodInvoker postHandler;

    @Autowired
    private HttpServletRequest request;

    ActionEventAdapter(Object moduleObject, Map<String, MethodInvoker> handlers, MethodInvoker preHandler,
                       MethodInvoker postHandler) {
        super(moduleObject);
        this.handlers = assertNotNull(handlers, "handlers");
        this.preHandler = preHandler;
        this.postHandler = postHandler;

        assertTrue(!handlers.isEmpty(), "handlers");
    }

    public void afterPropertiesSet() {
        assertProxy(assertNotNull(request, "missing HttpServletRequest object"));
    }

    /**
     * ִ��һ��module��
     */
    public void execute() throws ActionEventException, ActionEventNotFoundException {
        String event = getEventName(request);
        MethodInvoker handler = null;

        // ���Ҿ�ȷƥ��ķ���
        if (event != null) {
            handler = handlers.get(event);
        }

        // ����fallback method
        if (handler == null) {
            handler = handlers.get(null);
        }

        // δ�ҵ����ʵ�handler method������
        if (handler == null) {
            throw new ActionEventNotFoundException("Could not find handler method for action event: " + event);
        }

        // ִ��preHandler
        if (preHandler != null) {
            log.debug("Invoking pre-action event handler for event {}: {}", event, preHandler);

            try {
                preHandler.invoke(moduleObject, log);
            } catch (Exception e) {
                throw new ActionEventException("Failed to execute pre-action event handler: " + preHandler, e);
            }
        }

        ActionEventException exception = null;

        try {
            // ִ��event handler
            log.debug("Invoking action event handler for event {}: {}", event, handler);

            try {
                handler.invoke(moduleObject, log);
            } catch (Exception e) {
                exception = new ActionEventException("Failed to execute action event handler: " + handler, e);
            }
        } finally {
            // ִ��postHandler
            if (postHandler != null) {
                log.debug("Invoking post-action event handler for event {}: {}", event, postHandler);

                try {
                    postHandler.invoke(moduleObject, log);
                } catch (Exception e) {
                    if (exception == null) {
                        exception = new ActionEventException("Failed to execute post-action event handler: "
                                + postHandler, e);
                    }
                }
            }
        }

        if (exception != null) {
            throw exception;
        }
    }

    @Override
    public String toString() {
        MapBuilder mb = new MapBuilder();

        mb.append("moduleClass", moduleObject.getClass().getName());
        mb.append("handlers", new MapBuilder().appendAll(handlers).setPrintCount(true).setSortKeys(true));
        mb.append("preHandler", preHandler);
        mb.append("postHandler", postHandler);

        return new ToStringBuilder().append(getClass().getSimpleName()).append(mb).toString();
    }
}
