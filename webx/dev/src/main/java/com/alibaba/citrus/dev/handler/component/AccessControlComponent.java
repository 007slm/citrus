package com.alibaba.citrus.dev.handler.component;

import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.ObjectUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;

import com.alibaba.citrus.util.internal.regex.ClassNameWildcardCompiler;
import com.alibaba.citrus.util.internal.webpagelite.PageComponent;
import com.alibaba.citrus.util.internal.webpagelite.PageComponentRegistry;
import com.alibaba.citrus.webx.handler.RequestHandlerContext;
import com.alibaba.citrus.webx.handler.support.AbstractVisitor;

/**
 * ��������ӷ���Ȩ�Ļ����Ϸ��ʿ�����ҳ��������
 * 
 * @author Michael Zhou
 */
public class AccessControlComponent extends PageComponent {
    private static final String PROPERTY_ALLOWD_HOSTS = "developmentMode.allowedHosts";
    private final Pattern[] allowdHostPatterns;

    public AccessControlComponent(PageComponentRegistry registry, String componentPath) {
        super(registry, componentPath);

        String[] allowedHosts = split(defaultIfNull(System.getProperty(PROPERTY_ALLOWD_HOSTS), EMPTY_STRING), ", ");
        List<Pattern> patterns = createLinkedList();

        for (String allowedHost : allowedHosts) {
            patterns.add(ClassNameWildcardCompiler.compileClassName(allowedHost));
        }

        this.allowdHostPatterns = patterns.toArray(new Pattern[patterns.size()]);
    }

    public boolean accessAllowed(RequestHandlerContext context) {
        if (checkPermission(context)) {
            return true;
        } else {
            getTemplate().accept(new AccessDeniedVisitor(context));
            return false;
        }
    }

    private boolean checkPermission(RequestHandlerContext context) {
        String remoteAddr = getRemoteAddr(context);

        try {
            InetAddress addr = InetAddress.getByName(remoteAddr);

            for (Pattern allowedHostPattern : allowdHostPatterns) {
                if (allowedHostPattern.matcher(remoteAddr).matches()) {
                    return true;
                }
            }

            // ���ǽ���localhost
            if (addr.isLoopbackAddress()) {
                return true;
            } else {
                // ���ǽ��ܵ�ǰ����������һ������������ip
                for (Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces(); e.hasMoreElements();) {
                    for (Enumeration<InetAddress> f = e.nextElement().getInetAddresses(); f.hasMoreElements();) {
                        if (addr.equals(f.nextElement())) {
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
        }

        return false;
    }

    private String getRemoteAddr(RequestHandlerContext context) {
        return trimToNull(context.getRequest().getRemoteAddr());
    }

    @SuppressWarnings("unused")
    private class AccessDeniedVisitor extends AbstractVisitor {
        public AccessDeniedVisitor(RequestHandlerContext context) {
            super(context, AccessControlComponent.this);
        }

        public void visitPropertyName() {
            out().print(PROPERTY_ALLOWD_HOSTS);
        }

        public void visitPropertyValue() {
            String remoteAddr = getRemoteAddr(context);

            if (remoteAddr != null) {
                out().print(remoteAddr);
            }
        }

        public void visitPropertyValueWildcard() {
            String remoteAddr = getRemoteAddr(context);

            if (remoteAddr != null) {
                out().print(remoteAddr.substring(0, remoteAddr.lastIndexOf(".") + 1) + "*");
            }
        }
    }
}
