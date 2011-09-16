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
package com.alibaba.citrus.webx.util;

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.StringUtil.*;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.citrus.webx.WebxComponent;

public class WebxUtil {
    private final static String CURRENT_WEBX_COMPONENT_KEY = "webx.component";

    public static WebxComponent getCurrentComponent(HttpServletRequest request) {
        return assertNotNull((WebxComponent) request.getAttribute(CURRENT_WEBX_COMPONENT_KEY),
                "No WebxComponent bound in request.  "
                        + "Make sure WebxFrameworkFilter run or set WebxComponent explicitly by calling WebxUtil");
    }

    public static void setCurrentComponent(HttpServletRequest request, WebxComponent component) {
        if (component == null) {
            request.removeAttribute(CURRENT_WEBX_COMPONENT_KEY);
        } else {
            request.setAttribute(CURRENT_WEBX_COMPONENT_KEY, component);
        }
    }

    /**
     * ȡ��webx version�� �汾����ͨ��META-INF/MANIFEST.MF�е���Ϣȡ�õġ�
     */
    public static String getWebxVersion() {
        Package pkg = WebxUtil.class.getPackage();
        String version = null;

        if (pkg != null) {
            version = trimToNull(pkg.getImplementationVersion());
        }

        if (version == null) {
            version = "Unknown Version";
        }

        return version;
    }
}
