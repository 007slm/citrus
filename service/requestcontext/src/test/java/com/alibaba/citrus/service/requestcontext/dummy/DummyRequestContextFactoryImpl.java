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

package com.alibaba.citrus.service.requestcontext.dummy;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.citrus.service.requestcontext.RequestContext;
import com.alibaba.citrus.service.requestcontext.RequestContextException;
import com.alibaba.citrus.service.requestcontext.support.AbstractRequestContextFactory;

/**
 * ����һ��û���κι��ܵ�factory����ֱ�Ӵ���RequestContext�ӿڣ�������ĳ���ӽӿڡ�
 * ������RequestContextPostProcessor������Ϊ������singleton proxy��
 * 
 * @author Michael Zhou
 */
public class DummyRequestContextFactoryImpl extends AbstractRequestContextFactory<RequestContext> {
    public RequestContext getRequestContextWrapper(final RequestContext wrappedContext) {
        return new RequestContext() {
            public RequestContext getWrappedRequestContext() {
                return wrappedContext;
            }

            public HttpServletRequest getRequest() {
                return wrappedContext.getRequest();
            }

            public HttpServletResponse getResponse() {
                return wrappedContext.getResponse();
            }

            public ServletContext getServletContext() {
                return wrappedContext.getServletContext();
            }

            public void commit() throws RequestContextException {
            }

            public void prepare() {
            }
        };
    }

    public String[] getFeatures() {
        return null;
    }

    public com.alibaba.citrus.service.requestcontext.RequestContextInfo.FeatureOrder[] featureOrders() {
        return null;
    }
}
