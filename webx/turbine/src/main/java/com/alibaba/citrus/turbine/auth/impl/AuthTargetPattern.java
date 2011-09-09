package com.alibaba.citrus.turbine.auth.impl;

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static com.alibaba.citrus.util.regex.PathNameWildcardCompiler.*;

import java.util.regex.Pattern;

public class AuthTargetPattern extends AuthPattern {
    public AuthTargetPattern(String patternName) {
        super(patternName);
    }

    @Override
    protected String normalizePatternName(String patternName) {
        patternName = assertNotNull(trimToNull(patternName), "patternName");

        // �������·�����Զ���ǰ�����/����ɾ���·����
        if (!patternName.startsWith("/")) {
            patternName = "/" + patternName;
        }

        return patternName;
    }

    @Override
    protected Pattern compilePattern(String patternName) {
        return compilePathName(patternName, FORCE_MATCH_PREFIX | FORCE_ABSOLUTE_PATH);
    }
}
