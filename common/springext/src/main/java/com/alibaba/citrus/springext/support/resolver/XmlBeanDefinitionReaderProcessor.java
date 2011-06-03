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
package com.alibaba.citrus.springext.support.resolver;

import static com.alibaba.citrus.util.Assert.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.xml.DefaultNamespaceHandlerResolver;
import org.springframework.beans.factory.xml.NamespaceHandlerResolver;
import org.springframework.beans.factory.xml.ResourceEntityResolver;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.xml.sax.EntityResolver;

import com.alibaba.citrus.springext.impl.ConfigurationPointsImpl;

/**
 * ��������<code>XmlBeanDefinitionReader</code>�����configuration point�Ĺ��ܡ�
 *
 * @author Michael Zhou
 */
public class XmlBeanDefinitionReaderProcessor {
    private final static String PROPERTY_SKIP_VALIDATION = "skipValidation";
    private final static Logger log = LoggerFactory.getLogger(XmlBeanDefinitionReaderProcessor.class);
    private final XmlBeanDefinitionReader reader;
    private final boolean skipValidation;

    public XmlBeanDefinitionReaderProcessor(XmlBeanDefinitionReader reader) {
        this(reader, Boolean.getBoolean(PROPERTY_SKIP_VALIDATION));
    }

    public XmlBeanDefinitionReaderProcessor(XmlBeanDefinitionReader reader, boolean skipValidation) {
        this.reader = assertNotNull(reader, "XmlBeanDefinitionReader");
        this.skipValidation = skipValidation;
    }

    public void addConfigurationPointsSupport() {
        if (skipValidation) {
            reader.setValidationMode(XmlBeanDefinitionReader.VALIDATION_NONE);
            reader.setNamespaceAware(true); // Ϊ�����Configuration Point֧�֣�namespace�Ǳ���򿪵ġ�

            log.warn(
                    "XSD validation has been disabled according to the system property: -D{}.  Please be warned: NEVER skipping validation in Production Environment.",
                    PROPERTY_SKIP_VALIDATION);
        }

        ResourceLoader resourceLoader = reader.getResourceLoader();

        if (resourceLoader == null) {
            resourceLoader = new DefaultResourceLoader();
        }

        ClassLoader classLoader = resourceLoader.getClassLoader();

        // schema providers
        ConfigurationPointsImpl cps = new ConfigurationPointsImpl(classLoader);
        SpringPluggableSchemas sps = new SpringPluggableSchemas(resourceLoader);

        // default resolvers
        EntityResolver defaultEntityResolver = new ResourceEntityResolver(resourceLoader);
        NamespaceHandlerResolver defaultNamespaceHanderResolver = new DefaultNamespaceHandlerResolver(classLoader);

        // new resolvers
        EntityResolver entityResolver = new SchemaEntityResolver(defaultEntityResolver, cps, sps);
        NamespaceHandlerResolver namespaceHandlerResolver = new ConfigurationPointNamespaceHandlerResolver(cps,
                defaultNamespaceHanderResolver);

        reader.setEntityResolver(entityResolver);
        reader.setNamespaceHandlerResolver(namespaceHandlerResolver);
    }
}
