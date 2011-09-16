package com.alibaba.citrus.turbine.auth;

import static com.alibaba.citrus.turbine.auth.impl.PageAuthorizationServiceImpl.PageAuthorizationResult.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.alibaba.citrus.turbine.auth.impl.AuthGrant;
import com.alibaba.citrus.turbine.auth.impl.AuthMatch;
import com.alibaba.citrus.turbine.auth.impl.PageAuthorizationServiceImpl;
import com.alibaba.citrus.turbine.auth.impl.PageAuthorizationServiceImpl.PageAuthorizationResult;

public class PageAuthorizationServiceTests {
    protected static final String[] ADMIN_ROLE = new String[] { "admin" };

    protected PageAuthorizationServiceImpl auth;

    @Before
    public void init() throws Exception {
        auth = new PageAuthorizationServiceImpl();

        auth.setMatches(new AuthMatch[] {
                // matches
                match("/user", grant(null, "*", null, "*")), //
                match("/user", grant("baobao", null, "read,write", null)), //
                match("/admin", grant("baobao", null, "read,write", null)), //
                match("/user/profile", grant(null, "admin", "*", null)), //
                match("/user/public", //
                        // grants
                        grant(null, "*", "action", null), // 
                        grant("*", null, "read", null), // 
                        grant("anonymous", null, "write", null)), //
                match("/**/*.vm", grant(null, "*", "*", null)) //
        });
    }

    private AuthMatch match(String target, AuthGrant... grants) {
        return new AuthMatch(target, grants);
    }

    private AuthGrant grant(String user, String role, String allow, String deny) {
        AuthGrant grant = new AuthGrant();

        grant.setUsers(new String[] { user });
        grant.setRoles(new String[] { role });
        grant.setAllow(split(allow, ", "));
        grant.setDeny(split(deny, ", "));

        return grant;
    }

    @Test
    public void noAction() {
        // allow=*, actions=null
        assertAuth(ALLOWED, "/test.vm", null, ADMIN_ROLE, (String[]) null);

        // deny=*, actions=null
        assertAuth(DENIED, "/user", null, ADMIN_ROLE, (String[]) null);
    }

    @Test
    public void multiActions() {
        // allow=read,write, actions=read,write
        assertAuth(ALLOWED, "/user", "baobao", null, "read", "write");

        // allow=read,write, action=read,write,other
        assertAuth(GRANT_NOT_MATCH, "/user", "baobao", null, "read", "write", "other");
    }

    /**
     * target��ƥ�䡣
     */
    @Test
    public void targetNotMatch() {
        assertAuth(TARGET_NOT_MATCH, "/", "baobao", null, (String[]) null);
        assertAuth(TARGET_NOT_MATCH, "/notMatch", "baobao", null, (String[]) null);
    }

    /**
     * ���ƥ��������Ȩ����ͬ��ƥ���Ժ����Ϊ׼��
     */
    @Test
    public void priority() {
        // allow=read,write, actions=read
        assertAuth(ALLOWED, "/user", "baobao", null, "read");

        // allow=read,write, actions=write
        assertAuth(ALLOWED, "/user", "baobao", null, "write");

        // deny=*, actions=write
        assertAuth(DENIED, "/user", null, ADMIN_ROLE, "write");
    }

    /**
     * targetƥ�䣬���û�δƥ�䡣
     */
    @Test
    public void userNotMatch() {
        assertAuth(GRANT_NOT_MATCH, "/user", "other", null, "read");
        assertAuth(GRANT_NOT_MATCH, "/user", "other", null, "write");
    }

    /**
     * targetƥ�䡢�û�ƥ�䣬��action��ƥ�䡣
     */
    @Test
    public void actionNotMatch() {
        // allow=read,write, action=otherAction
        assertAuth(GRANT_NOT_MATCH, "/user", "baobao", null, "otherAction");
    }

    /**
     * ƥ��role��
     */
    @Test
    public void role() {
        // allow=*, action=read
        assertAuth(ALLOWED, "/user/profile", "other", ADMIN_ROLE, "read");

        // allow=*, action=write
        assertAuth(ALLOWED, "/user/profile/abc", "other", ADMIN_ROLE, "write");

        // role=admin��ƥ��null
        assertAuth(GRANT_NOT_MATCH, "/user/profile/abc", "other", null, "write");
    }

    /**
     * ���·����
     */
    @Test
    public void relativeTarget() {
        // allow=*
        assertAuth(ALLOWED, "/user/hello.vm", "other", ADMIN_ROLE, "read");

        // role=admin��ƥ��null
        assertAuth(GRANT_NOT_MATCH, "/user/world.vm", "other", null, "write");
    }

    /**
     * �������ʡ�
     */
    @Test
    public void anonymous() {
        // role=*��������role
        assertAuth(GRANT_NOT_MATCH, "/user/public/hello", null, null, "action");

        // user=* ������anonymous
        assertAuth(GRANT_NOT_MATCH, "/user/public/hello", null, null, "read");

        // user=anonymous
        assertAuth(ALLOWED, "/user/public/hello", null, null, "write");
    }

    private void assertAuth(PageAuthorizationResult result, String target, String userName, String[] roleNames,
                            String... actions) {
        assertSame(result, auth.authorize(target, userName, roleNames, actions));

        if (result == ALLOWED) {
            assertTrue(auth.isAllow(target, userName, roleNames, actions));
        } else {
            assertFalse(auth.isAllow(target, userName, roleNames, actions));
        }
    }
}
