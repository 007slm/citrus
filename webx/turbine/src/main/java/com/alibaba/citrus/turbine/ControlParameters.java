package com.alibaba.citrus.turbine;

/**
 * ������control module���޸�control�����Ľӿڣ���ע�뵽control module�Ĳ����С�
 * 
 * @author Michael Zhou
 */
public interface ControlParameters {
    /**
     * ȡ��controlģ�塣
     */
    String getControlTemplate();

    /**
     * ����controlģ�塣����֮ǰ�Ѿ�ָ����controlģ�壬�򸲸�֮��
     */
    void setControlTemplate(String template);
}
