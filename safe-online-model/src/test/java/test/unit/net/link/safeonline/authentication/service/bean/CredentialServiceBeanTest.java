/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.authentication.service.bean;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.InputStream;

import junit.framework.TestCase;
import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.service.bean.CredentialServiceBean;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.model.SubjectManager;
import net.link.safeonline.test.util.EJBTestUtils;

import org.apache.commons.io.IOUtils;

public class CredentialServiceBeanTest extends TestCase {

	private CredentialServiceBean testedInstance;

	private SubjectManager mockSubjectManager;

	private AttributeDAO mockAttributeDAO;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		this.testedInstance = new CredentialServiceBean();

		this.mockSubjectManager = createMock(SubjectManager.class);
		EJBTestUtils.inject(this.testedInstance, this.mockSubjectManager);

		this.mockAttributeDAO = createMock(AttributeDAO.class);
		EJBTestUtils.inject(this.testedInstance, this.mockAttributeDAO);

		EJBTestUtils.init(this.testedInstance);
	}

	public void testUnparsableIdentityStatement() throws Exception {
		// setup
		String identityStatementStr = "foobar-identity-statemennt";
		String testCallerLogin = "test-caller-login-" + getName();

		// stubs
		expect(this.mockSubjectManager.getCallerLogin()).andStubReturn(
				testCallerLogin);

		// prepare
		replay(this.mockSubjectManager, this.mockAttributeDAO);

		// operate
		try {
			this.testedInstance.mergeIdentityStatement(identityStatementStr);
			fail();
		} catch (IllegalArgumentException e) {
			// expected
		}

		// verify
		verify(this.mockSubjectManager, this.mockAttributeDAO);
	}

	public void testMergeIdentityStatement() throws Exception {
		// setup
		InputStream inputStream = CredentialServiceBean.class
				.getResourceAsStream("/test-identity-statement.xml");
		String identityStatementStr = IOUtils.toString(inputStream);
		String testCallerLogin = "test-caller-login-" + getName();

		// stubs
		expect(this.mockSubjectManager.getCallerLogin()).andStubReturn(
				testCallerLogin);

		expect(
				this.mockAttributeDAO.findAttribute(
						SafeOnlineConstants.SURNAME_ATTRIBUTE, testCallerLogin))
				.andStubReturn(null);
		expect(
				this.mockAttributeDAO.findAttribute(
						SafeOnlineConstants.GIVENNAME_ATTRIBUTE,
						testCallerLogin)).andStubReturn(null);

		// expectations
		this.mockAttributeDAO.addAttribute(
				SafeOnlineConstants.SURNAME_ATTRIBUTE, testCallerLogin,
				"test-surname");
		this.mockAttributeDAO.addAttribute(
				SafeOnlineConstants.GIVENNAME_ATTRIBUTE, testCallerLogin,
				"test-given-name");

		// prepare
		replay(this.mockSubjectManager, this.mockAttributeDAO);

		// operate
		this.testedInstance.mergeIdentityStatement(identityStatementStr);

		// verify
		verify(this.mockSubjectManager, this.mockAttributeDAO);
	}
}
