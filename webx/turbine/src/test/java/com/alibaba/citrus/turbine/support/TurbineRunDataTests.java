package com.alibaba.citrus.turbine.support;

import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.alibaba.citrus.turbine.AbstractWebxTests;
import com.alibaba.citrus.turbine.Context;

public class TurbineRunDataTests extends AbstractWebxTests {
    @BeforeClass
    public static void initWebx() throws Exception {
        prepareServlet();
    }

    @Before
    public void init() throws Exception {
        getInvocationContext("http://localhost/app1/1.html");
        initRequestContext();
    }

    @Test
    public void getContext() {
        Context context1 = rundata.getContext();
        Context context2 = rundata.getContext("app2");

        // getContext()ȡ�õ�ǰcomponent��context��
        assertSame(rundata.getContext("app1"), rundata.getContext());
        assertSame(rundata.getContext("app1"), context1);

        assertNotSame(context2, context1);
    }

    @Test
    public void getCurrentContext() {
        assertNull(getFieldValue(rundata, "threadContexts", ThreadLocal.class).get());
        assertNull(rundata.getCurrentContext());

        try {
            rundata.popContext();
            fail();
        } catch (IllegalStateException e) {
            assertThat(e, exception("can't popContext without pushContext"));
        }

        Context context1 = rundata.getContext("app1");
        Context context2 = rundata.getContext("app2");

        try {
            rundata.pushContext(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("context"));
        }

        rundata.pushContext(context1);
        assertSame(context1, rundata.getCurrentContext());
        assertSame(context1, rundata.getCurrentContext());

        rundata.pushContext(context2);
        assertSame(context2, rundata.getCurrentContext());
        assertSame(context2, rundata.getCurrentContext());

        assertSame(context2, rundata.popContext());
        assertSame(context1, rundata.popContext());

        try {
            rundata.popContext();
            fail();
        } catch (IllegalStateException e) {
            assertThat(e, exception("can't popContext without pushContext"));
        }

        assertNull(getFieldValue(rundata, "threadContexts", ThreadLocal.class).get());
    }
}
