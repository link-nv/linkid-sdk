/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.spike.net.link.safeonline;

import java.util.Set;
import java.util.UUID;

import javax.security.auth.Subject;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextHandler;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class JaccTest extends TestCase {

    private static final Log LOG = LogFactory.getLog(JaccTest.class);


    @SuppressWarnings("unchecked")
    public void testPolicyContext()
            throws Exception {

        String contextId = PolicyContext.getContextID();
        LOG.debug("context Id: " + contextId);
        assertNull(contextId);

        Set<String> handlerKeys = PolicyContext.getHandlerKeys();
        LOG.debug("# handler keys: " + handlerKeys.size());
        assertEquals(0, handlerKeys.size());

        TestSubjectPolicyContextHandler policyContextHandler = new TestSubjectPolicyContextHandler();
        Subject subject = new Subject();
        policyContextHandler.setSubject(subject);
        PolicyContext.registerHandler(TestSubjectPolicyContextHandler.SUBJECT_CONTEXT_KEY, policyContextHandler, false);

        handlerKeys = PolicyContext.getHandlerKeys();
        LOG.debug("# handler keys: " + handlerKeys.size());
        assertEquals(1, handlerKeys.size());

        Object result = PolicyContext.getContext(TestSubjectPolicyContextHandler.SUBJECT_CONTEXT_KEY);
        assertTrue(result instanceof Subject);
        assertEquals(subject, result);
    }

    public void testRetrieveNonExistingPolicyContext()
            throws Exception {

        String contextId = "context-id-" + UUID.randomUUID().toString();
        try {
            PolicyContext.getContext(contextId);
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }
    }


    static class TestSubjectPolicyContextHandler implements PolicyContextHandler {

        private static final String SUBJECT_CONTEXT_KEY = "javax.security.auth.Subject.container";

        private Subject             subject;


        public void setSubject(Subject subject) {

            this.subject = subject;
        }

        @SuppressWarnings("unused")
        public Object getContext(String key, Object data) {

            return subject;
        }

        public String[] getKeys() {

            return new String[] { SUBJECT_CONTEXT_KEY };
        }

        public boolean supports(String key) {

            return SUBJECT_CONTEXT_KEY.equals(key);
        }
    }
}
