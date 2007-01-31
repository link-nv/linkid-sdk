/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.model.beid;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.easymock.EasyMock.expectLastCall;

import java.security.cert.X509Certificate;

import org.easymock.EasyMock;

import junit.framework.TestCase;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.dao.TrustDomainDAO;
import net.link.safeonline.dao.TrustPointDAO;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.TrustDomainEntity;
import net.link.safeonline.model.beid.BeIdPkiProvider;
import net.link.safeonline.model.beid.BeIdStartableBean;
import net.link.safeonline.test.util.EJBTestUtils;

public class BeIdStartableBeanTest extends TestCase {

	private BeIdStartableBean testedInstance;

	private TrustDomainDAO mockTrustDomainDAO;

	private TrustPointDAO mockTrustPointDAO;

	private AttributeTypeDAO mockAttributeTypeDAO;

	private Object[] mockObjects;

	protected void setUp() throws Exception {
		super.setUp();

		this.testedInstance = new BeIdStartableBean();

		this.mockTrustDomainDAO = createMock(TrustDomainDAO.class);
		EJBTestUtils.inject(this.testedInstance, this.mockTrustDomainDAO);

		this.mockTrustPointDAO = createMock(TrustPointDAO.class);
		EJBTestUtils.inject(this.testedInstance, this.mockTrustPointDAO);

		this.mockAttributeTypeDAO = createMock(AttributeTypeDAO.class);
		EJBTestUtils.inject(this.testedInstance, this.mockAttributeTypeDAO);

		EJBTestUtils.init(this.testedInstance);

		this.mockObjects = new Object[] { this.mockTrustDomainDAO,
				this.mockTrustPointDAO, this.mockAttributeTypeDAO };
	}

	public void testStart() throws Exception {
		// setup
		TrustDomainEntity trustDomain = new TrustDomainEntity(
				BeIdPkiProvider.TRUST_DOMAIN_NAME, true);

		// stubs
		expect(
				this.mockTrustDomainDAO
						.findTrustDomain(BeIdPkiProvider.TRUST_DOMAIN_NAME))
				.andStubReturn(null);
		expect(
				this.mockAttributeTypeDAO.findAttributeType((String) EasyMock
						.anyObject())).andStubReturn(null);

		// expectations
		expect(
				this.mockTrustDomainDAO.addTrustDomain(
						BeIdPkiProvider.TRUST_DOMAIN_NAME, true)).andReturn(
				trustDomain);
		this.mockTrustPointDAO.addTrustPoint(EasyMock.eq(trustDomain),
				(X509Certificate) EasyMock.anyObject());
		expectLastCall().times(21);

		this.mockAttributeTypeDAO
				.addAttributeType((AttributeTypeEntity) EasyMock.anyObject());
		expectLastCall().times(3);

		// prepare
		replay(this.mockObjects);

		// operate
		this.testedInstance.postStart();

		// verify
		verify(this.mockObjects);
	}
}