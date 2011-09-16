package com.alibaba.citrus.turbine.pipeline.valve;

import static com.alibaba.citrus.springext.util.SpringExtUtil.*;
import static com.alibaba.citrus.turbine.util.TurbineUtil.*;
import static com.alibaba.citrus.util.ArrayUtil.*;
import static com.alibaba.citrus.util.CollectionUtil.*;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.alibaba.citrus.service.pipeline.PipelineContext;
import com.alibaba.citrus.service.pipeline.support.AbstractValve;
import com.alibaba.citrus.service.pipeline.support.AbstractValveDefinitionParser;
import com.alibaba.citrus.turbine.TurbineRunData;
import com.alibaba.citrus.turbine.auth.PageAuthorizationService;

public class PageAuthorizationValve extends AbstractValve {
    @Autowired
    private HttpServletRequest request;

    @Autowired
    private PageAuthorizationService pageAuthorizationService;

    private Callback<?> callback;

    public void setCallback(Callback<?> callback) {
        this.callback = callback;
    }

    @Override
    protected void init() throws Exception {
        if (callback == null) {
            callback = new DefaultCallback();
        }
    }

    public void invoke(PipelineContext pipelineContext) throws Exception {
        TurbineRunData rundata = getTurbineRunData(request);

        @SuppressWarnings("unchecked")
        Callback<Object> cb = (Callback<Object>) callback;

        Object status = cb.onStart(rundata);

        String userName = cb.getUserName(status);
        String[] roleNames = cb.getRoleNames(status);

        String target = rundata.getTarget();
        String action = rundata.getAction();

        // ȡ�õ�ǰ�����actions�����������֣�
        // 1. screen
        // 2. action.* - �����������action�����Ļ�
        // 3. callback���صĶ���actions
        // ֻ�е�����actionȫ������Ȩʱ������Ż����������ȥ��
        List<String> actions = createLinkedList();

        actions.add("screen");

        if (action != null) {
            actions.add("action." + action);
        }

        String[] extraActions = cb.getActions(status);

        if (!isEmptyArray(extraActions)) {
            for (String extraAction : extraActions) {
                actions.add(extraAction);
            }
        }

        // ���Ȩ�ޣ����ݣ�
        // 1. ��ǰ��target
        // 2. ��ǰ��user��roles
        // 3. ��Ҫִ�е�actions�����磺screen��action.xxx.UserAction
        if (pageAuthorizationService.isAllow(target, userName, roleNames, actions.toArray(new String[actions.size()]))) {
            cb.onAllow(status);
        } else {
            cb.onDeny(status);
        }

        pipelineContext.invokeNext();
    }

    public interface Callback<T> {
        String getUserName(T status);

        String[] getRoleNames(T status);

        String[] getActions(T status);

        T onStart(TurbineRunData rundata) throws Exception;

        void onAllow(T status) throws Exception;

        void onDeny(T status) throws Exception;
    }

    private class DefaultCallback implements Callback<TurbineRunData> {
        public String getUserName(TurbineRunData status) {
            return null;
        }

        public String[] getRoleNames(TurbineRunData status) {
            return null;
        }

        public String[] getActions(TurbineRunData status) {
            return null;
        }

        public TurbineRunData onStart(TurbineRunData rundata) {
            return rundata;
        }

        public void onAllow(TurbineRunData status) throws Exception {
        }

        public void onDeny(TurbineRunData status) {
        }
    }

    public static class DefinitionParser extends AbstractValveDefinitionParser<PageAuthorizationValve> {
        @Override
        protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
            element.setAttribute("class", element.getAttribute("callbackClass"));
            builder.addPropertyValue("callback", parseBean(element, parserContext, builder));
        }
    }
}
