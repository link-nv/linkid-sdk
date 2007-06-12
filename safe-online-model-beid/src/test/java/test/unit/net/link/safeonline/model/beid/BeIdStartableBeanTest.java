/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.model.beid;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.security.cert.X509Certificate;

import junit.framework.TestCase;
import net.link.safeonline.entity.pkix.TrustDomainEntity;
import net.link.safeonline.model.beid.BeIdPkiProvider;
import net.link.safeonline.model.beid.BeIdStartableBean;
import net.link.safeonline.pkix.dao.TrustDomainDAO;
import net.link.safeonline.pkix.dao.TrustPointDAO;
import net.link.safeonline.test.util.EJBTestUtils;

import org.easymock.EasyMock;

public class BeIdStartableBeanTest extends TestCase {

	private BeIdStartableBean testedInstance;

	private TrustDomainDAO mockTrustDomainDAO;

	private TrustPointDAO mockTrustPointDAO;

	private Object[] mockObjects;

	protected void setUp() throws Exception {
		super.setUp();

		this.testedInstance = new BeIdStartableBean();

		this.mockTrustDomainDAO = createMock(TrustDomainDAO.class);
		EJBTestUtils.inject(this.testedInstance, this.mockTrustDomainDAO);

		this.mockTrustPointDAO = createMock(TrustPointDAO.class);
		EJBTestUtils.inject(this.testedInstance, this.mockTrustPointDAO);

		EJBTestUtils.init(this.testedInstance);

		this.mockObjects = new Object[] { this.mockTrustDomainDAO,
				this.mockTrustPointDAO };
	}

	public void testInitTrustDomain() throws Exception {
		// setup
		TrustDomainEntity trustDomain = new TrustDomainEntity(
				BeIdPkiProvider.TRUST_DOMAIN_NAME, true);

		// stubs
		expect(
				this.mockTrustDomainDAO
						.findTrustDomain(BeIdPkiProvider.TRUST_DOMAIN_NAME))
				.andStubReturn(null);

		// expectations
		expect(
				this.mockTrustDomainDAO.addTrustDomain(
						BeIdPkiProvider.TRUST_DOMAIN_NAME, true)).andReturn(
				trustDomain);
		this.mockTrustPointDAO.addTrustPoint(EasyMock.eq(trustDomain),
				(X509Certificate) EasyMock.anyObject());
		expectLastCall().times(1 + 2 + 1 + 15 + 20 + 1 + 1 + 1 + 1 + 1);

		// prepare
		replay(this.mockObjects);

		// operate
		this.testedInstance.initTrustDomain();

		// verify
		verify(this.mockObjects);
	}
}