package com.alibaba.citrus.springext.support.context;

import static com.alibaba.citrus.util.Assert.*;

import java.lang.reflect.Method;
import java.util.Collection;

import org.springframework.context.ApplicationListener;
import org.springframework.context.support.AbstractApplicationContext;

/**
 * ����spring3��spring2��api����ȫ���ݣ�������spring2�ϱ����context.getApplicationListeners()
 * ������spring3�б�NoSuchMethodError�������÷���ķ�����������������⡣������ȫǨ�Ƶ�spring3�Ժ󣬿���ɾ�����ʵ�֡�
 * 
 * @author Michael Zhou
 */
class GetApplicationListeners {
    private final AbstractApplicationContext context;
    private final Method getApplicationListenersMethod;

    public GetApplicationListeners(AbstractApplicationContext context) {
        this.context = context;

        Method method = null;

        for (Class<?> clazz = context.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            try {
                method = clazz.getDeclaredMethod("getApplicationListeners");
                break;
            } catch (Exception e) {
            }
        }

        getApplicationListenersMethod = assertNotNull(method,
                "Could not call method: context.getApplicationListeners()");
    }

    public Collection<ApplicationListener> invoke() {
        try {
            @SuppressWarnings("unchecked")
            Collection<ApplicationListener> listeners = (Collection<ApplicationListener>) getApplicationListenersMethod
                    .invoke(context);

            return listeners;
        } catch (Exception e) {
            throw new RuntimeException("Could not call method: context.getApplicationListeners()", e);
        }
    }
}
