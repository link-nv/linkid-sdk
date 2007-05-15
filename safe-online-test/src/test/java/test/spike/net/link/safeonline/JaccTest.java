/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.spike.net.link.safeonline;

import java.util.Set;

import javax.security.auth.Subject;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;
import javax.security.jacc.PolicyContextHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import junit.framework.TestCase;

public class JaccTest extends TestCase {

	private static final Log LOG = LogFactory.getLog(JaccTest.class);

	@SuppressWarnings("unchecked")
	public void testPolicyContext() throws Exception {
		String contextId = PolicyContext.getContextID();
		LOG.debug("context Id: " + contextId);
		assertNull(contextId);

		Set<String> handlerKeys = PolicyContext.getHandlerKeys();
		LOG.debug("# handler keys: " + handlerKeys.size());
		assertEquals(0, handlerKeys.size());

		TestSubjectPolicyContextHandler policyContextHandler = new TestSubjectPolicyContextHandler();
		Subject subject = new Subject();
		policyContextHandler.setSubject(subject);
		PolicyContext.registerHandler(
				TestSubjectPolicyContextHandler.SUBJECT_CONTEXT_KEY,
				policyContextHandler, false);

		handlerKeys = PolicyContext.getHandlerKeys();
		LOG.debug("# handler keys: " + handlerKeys.size());
		assertEquals(1, handlerKeys.size());

		Object result = PolicyContext
				.getContext(TestSubjectPolicyContextHandler.SUBJECT_CONTEXT_KEY);
		assertTrue(result instanceof Subject);
		assertEquals(subject, result);
	}

	private static class TestSubjectPolicyContextHandler implements
			PolicyContextHandler {

		private static final String SUBJECT_CONTEXT_KEY = "javax.security.auth.Subject.container";

		private Subject subject;

		public void setSubject(Subject subject) {
			this.subject = subject;
		}

		public Object getContext(String key, Object data)
				throws PolicyContextException {
			return this.subject;
		}

		public String[] getKeys() throws PolicyContextException {
			return new String[] { SUBJECT_CONTEXT_KEY };
		}

		public boolean supports(String key) throws PolicyContextException {
			return SUBJECT_CONTEXT_KEY.equals(key);
		}
	}
}
