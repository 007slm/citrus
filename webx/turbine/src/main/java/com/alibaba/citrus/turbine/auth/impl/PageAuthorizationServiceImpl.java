package com.alibaba.citrus.turbine.auth.impl;

import static com.alibaba.citrus.turbine.auth.impl.PageAuthorizationServiceImpl.PageAuthorizationResult.*;
import static com.alibaba.citrus.util.ArrayUtil.*;
import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static java.lang.Boolean.*;
import static java.util.Collections.*;

import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.regex.Matcher;

import com.alibaba.citrus.service.AbstractService;
import com.alibaba.citrus.turbine.auth.PageAuthorizationService;
import com.alibaba.citrus.util.ObjectUtil;

/**
 * Ϊҳ����Ȩ��service��
 * 
 * @author Michael Zhou
 */
public class PageAuthorizationServiceImpl extends AbstractService<PageAuthorizationService> implements
        PageAuthorizationService {
    private final List<AuthMatch> matches = createLinkedList();
    private boolean allowByDefault = false;

    public void setMatches(AuthMatch[] matches) {
        this.matches.clear();

        if (matches != null) {
            for (AuthMatch match : matches) {
                this.matches.add(match);
            }
        }
    }

    public boolean isAllowByDefault() {
        return allowByDefault;
    }

    public void setAllowByDefault(boolean allowByDefault) {
        this.allowByDefault = allowByDefault;
    }

    public boolean isAllow(String target, String userName, String[] roleNames, String... actions) {
        PageAuthorizationResult result = authorize(target, userName, roleNames, actions);

        switch (result) {
            case ALLOWED:
                return true;

            case DENIED:
                return false;

            default:
                return allowByDefault;
        }
    }

    public PageAuthorizationResult authorize(String target, String userName, String[] roleNames, String... actions) {
        userName = trimToNull(userName);

        if (actions == null) {
            actions = new String[] { EMPTY_STRING };
        }

        if (roleNames == null) {
            roleNames = EMPTY_STRING_ARRAY;
        }

        // �ҳ�����ƥ���pattern����ƥ�䳤�ȵ�����
        MatchResult[] results = getMatchResults(target);
        PageAuthorizationResult result;

        if (isEmptyArray(results)) {
            result = TARGET_NOT_MATCH;
        } else {
            boolean grantNotMatch = false;

            for (int i = 0; i < actions.length; i++) {
                actions[i] = trimToEmpty(actions[i]);
                Boolean actionAllowed = isActionAllowed(results, target, userName, roleNames, actions[i]);

                if (actionAllowed == null) {
                    grantNotMatch = true;
                } else if (!actionAllowed) {
                    return DENIED;
                }
            }

            if (!grantNotMatch) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug(
                            "Access Permitted: target=\"{}\", user=\"{}\", roles={}, action={}",
                            new Object[] { target, userName, ObjectUtil.toString(roleNames),
                                    ObjectUtil.toString(actions) });
                }

                return ALLOWED;
            } else {
                result = GRANT_NOT_MATCH;
            }
        }

        if (allowByDefault) {
            if (getLogger().isDebugEnabled()) {
                getLogger()
                        .debug("Access Permitted.  No matches found for request: target=\"{}\", user=\"{}\", roles={}, action={}",
                                new Object[] { target, userName, ObjectUtil.toString(roleNames),
                                        ObjectUtil.toString(actions) });
            }
        } else {
            if (getLogger().isWarnEnabled()) {
                getLogger()
                        .warn("Access Denied.  No matches found for request: target=\"{}\", user=\"{}\", roles={}, action={}",
                                new Object[] { target, userName, ObjectUtil.toString(roleNames),
                                        ObjectUtil.toString(actions) });
            }
        }

        return result;
    }

    private Boolean isActionAllowed(MatchResult[] results, String target, String userName, String[] roleNames,
                                    String action) {
        // ��˳������Ȩ��ֱ��role��user��allow��deny
        for (MatchResult result : results) {
            AuthMatch match = result.match;

            // ������grant������ĸ���ǰ��ġ�
            for (int i = match.getGrants().length - 1; i >= 0; i--) {
                AuthGrant grant = match.getGrants()[i];

                // �ж�user��role�Ƿ�ƥ��
                boolean userMatch = grant.isUserMatched(userName);
                boolean roleMatch = grant.areRolesMatched(roleNames);

                if (userMatch || roleMatch) {
                    // �ж�action�Ƿ�ƥ��
                    boolean actionAllowed = grant.isActionAllowed(action);
                    boolean actionDenied = grant.isActionDenied(action);

                    if (actionAllowed || actionDenied) {
                        boolean allowed = !actionDenied;

                        if (allowed) {
                            if (getLogger().isTraceEnabled()) {
                                getLogger()
                                        .trace("Access Partially Permitted: target=\"{}\", user=\"{}\", roles={}, action=\"{}\"\n{}",
                                                new Object[] { target, userName, ObjectUtil.toString(roleNames),
                                                        action, match.toString(i) });
                            }

                            return TRUE;
                        } else {
                            if (getLogger().isWarnEnabled()) {
                                getLogger().warn(
                                        "Access Denied: target=\"{}\", user=\"{}\", roles={}, action=\"{}\"\n{}",
                                        new Object[] { target, userName, ObjectUtil.toString(roleNames), action,
                                                match.toString(i) });
                            }

                            return FALSE;
                        }
                    }
                }
            }
        }

        return null;
    }

    private MatchResult[] getMatchResults(String target) {
        List<MatchResult> results = createArrayList(matches.size());

        // ƥ�����У�ע�⣬���ﰴ����ƥ�䣬����������ͬ��ƥ�䣬�Ժ����Ϊ׼��
        for (ListIterator<AuthMatch> i = matches.listIterator(matches.size()); i.hasPrevious();) {
            AuthMatch match = i.previous();
            Matcher matcher = match.getPattern().matcher(target);

            if (matcher.find()) {
                MatchResult result = new MatchResult();
                result.matchLength = matcher.end() - matcher.start();
                result.match = match;
                result.target = target;

                results.add(result);
            }
        }

        // ��ƥ�䳤�ȵ�����ע�⣬�����ȶ����򣬶��ڳ�����ͬ��ƥ�䣬˳�򲻱䡣
        sort(results);

        // ��ȥ�ظ���ƥ��
        Map<AuthGrant[], MatchResult> grantsSet = createLinkedHashMap();

        for (MatchResult result : results) {
            AuthGrant[] grants = result.match.getGrants();

            if (!grantsSet.containsKey(grants)) {
                grantsSet.put(grants, result);
            }
        }

        return grantsSet.values().toArray(new MatchResult[grantsSet.size()]);
    }

    private static class MatchResult implements Comparable<MatchResult> {
        private int matchLength = -1;
        private AuthMatch match;
        private String target;

        public int compareTo(MatchResult o) {
            return o.matchLength - matchLength;
        }

        @Override
        public String toString() {
            return "Match length=" + matchLength + ", target=" + target + ", " + match;
        }
    }

    public static enum PageAuthorizationResult {
        /**
         * ����ҳ�汻��ɷ��ʡ�
         */
        ALLOWED,

        /**
         * ����ҳ�汻�ܾ����ʡ�
         */
        DENIED,

        /**
         * ����ǰ��targetδƥ�䡣
         */
        TARGET_NOT_MATCH,

        /**
         * ����ǰ��grantδƥ�䣬Ҳ����user/roles/actionsδƥ�䡣
         */
        GRANT_NOT_MATCH
    }

}
