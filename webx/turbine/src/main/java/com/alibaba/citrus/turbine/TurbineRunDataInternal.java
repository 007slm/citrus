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
package com.alibaba.citrus.turbine;

/**
 * ��������ڲ�ʹ�á�
 * 
 * @author Michael Zhou
 */
public interface TurbineRunDataInternal extends TurbineRunData, Navigator, ControlParameters {
    void setTarget(String target);

    void setAction(String action);

    Context getContext(String componentName);

    Context getContext();

    /**
     * ȡ�õ�ǰ��context��
     */
    Context getCurrentContext();

    /**
     * �޸ĵ�ǰ��context��
     */
    void pushContext(Context context);

    /**
     * �޸ĵ�ǰ��context��
     */
    void pushContext(Context context, String template);

    /**
     * ������ǰ��context���ָ���һ��context��
     * 
     * @throws IllegalStateException ���pop��push����ԣ����״�
     */
    Context popContext() throws IllegalStateException;

    /**
     * ȡ����ȷָ����layoutģ�塣
     */
    String getLayoutTemplateOverride();
}
