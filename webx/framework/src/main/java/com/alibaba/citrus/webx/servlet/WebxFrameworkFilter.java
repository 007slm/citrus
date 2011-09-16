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
 *
 */
package com.alibaba.citrus.webx.servlet;

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static org.springframework.web.context.support.WebApplicationContextUtils.*;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;

import com.alibaba.citrus.webx.WebxComponents;
import com.alibaba.citrus.webx.WebxRootController;
import com.alibaba.citrus.webx.context.WebxComponentsContext;
import com.alibaba.citrus.webx.util.RequestURIFilter;

/**
 * ��ʼ��spring������filter��
 * 
 * @author Michael Zhou
 */
public class WebxFrameworkFilter extends FilterBean {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private String parentContextAttribute;
    private WebxComponents components;
    private RequestURIFilter excludeFilter;
    private RequestURIFilter passthruFilter;

    /**
     * ������servletContext�б���parent context��attribute key��
     */
    public final String getParentContextAttribute() {
        return parentContextAttribute;
    }

    /**
     * ����������servletContext�б���parent context��attribute key��
     */
    public final void setParentContextAttribute(String parentContextAttribute) {
        this.parentContextAttribute = trimToNull(parentContextAttribute);
    }

    /**
     * ����Ҫ�ų�����URL��
     */
    public void setExcludes(String excludes) {
        excludeFilter = new RequestURIFilter(excludes);
    }

    /**
     * ���ò���Ҫִ��pipeline��URL���ù��ܿɱ����ڽ�webx��Ϊ����servlet��filter��������
     * ������servlet����ʹ��webx���ṩ��request context���ܣ����磺session�ȡ�
     */
    public void setPassthru(String passthru) {
        passthruFilter = new RequestURIFilter(passthru);
    }

    /**
     * ȡ������components����Ϣ��
     */
    public WebxComponents getWebxComponents() {
        return components;
    }

    /**
     * ��ʼ��filter��
     */
    @Override
    protected final void init() throws Exception {
        WebApplicationContext parentContext = findParentContext();

        if (parentContext instanceof WebxComponentsContext) {
            components = ((WebxComponentsContext) parentContext).getWebxComponents();
        }

        WebxRootController rootController = components.getWebxRootController();

        if (passthruFilter != null) {
            if (rootController instanceof PassThruSupportable) {
                ((PassThruSupportable) rootController).setPassthruFilter(passthruFilter);
            } else {
                log.warn(
                        "You have specified Passthru Filter in /WEB-INF/web.xml.  "
                                + "It will not take effect because the implementation of WebxRootController ({}) does not support this feature.",
                        rootController.getClass().getName());
            }
        }
    }

    /**
     * ��<code>ServletContext</code>�в���parent context��
     * <ul>
     * <li>����δָ��<code>parentContextAttribute</code>�������Ĭ�ϵ�attribute key��</li>
     * <li>����ָ����init-param <code>parentContextAttribute</code>�������ָ����attribute
     * key������û�ҵ����򱨴�</li>
     * </ul>
     */
    private WebApplicationContext findParentContext() {
        WebApplicationContext parentContext = null;
        String parentContextAttribute = getParentContextAttribute();

        if (parentContextAttribute == null) {
            parentContext = getWebApplicationContext(getServletContext());
        } else {
            parentContext = getWebApplicationContext(getServletContext(), parentContextAttribute);
            assertNotNull(parentContext, "No WebApplicationContext \"%s\" found: not registered?",
                    parentContextAttribute);
        }

        return parentContext;
    }

    protected void initFrameworkFilter() {
    }

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        // ���ָ����excludes�����ҵ�ǰrequestURIƥ���κ�һ��exclude pattern��
        // �������������ƣ������ƻ���servlet engine��
        if (excludeFilter != null && excludeFilter.matches(request)) {
            chain.doFilter(request, response);
            return;
        }

        try {
            getWebxComponents().getWebxRootController().service(request, response, chain);
        } catch (IOException e) {
            throw e;
        } catch (ServletException e) {
            throw e;
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
