package com.alibaba.citrus.util.templatelite;

/**
 * ���visitorʵ��������ӿڣ���ô������visitor��������ʱ���ӿڽ������ã��Դ����쳣������<code>Template</code>
 * ���׳��쳣��
 * 
 * @author Michael Zhou
 */
public interface VisitorInvocationErrorHandler {
    void handleInvocationError(String desc, Throwable e);
}
