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
