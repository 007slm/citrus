package com.alibaba.citrus.turbine.dataresolver;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ������ʶһ��context�е�ֵ��
 * <p>
 * �÷����£�
 * </p>
 * <ol>
 * <li>ָ��ֵ�����ƣ�<code>@ContextValue("name")</code>��</li> </li>
 * </ol>
 * 
 * @author Michael Zhou
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER })
public @interface ContextValue {
    /**
     * ���ڱ�ʶcontextֵ�����ơ�
     * <p>
     * �˲������ڼ򻯵���ʽ��<code>@ContextValue("name")</code>��
     * </p>
     */
    String value();
}
