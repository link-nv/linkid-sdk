/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.model.beid.bean;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.security.KeyPair;
import java.security.cert.X509Certificate;

import junit.framework.TestCase;
import net.link.safeonline.entity.pkix.TrustDomainEntity;
import net.link.safeonline.model.beid.BeIdPkiProvider;
import net.link.safeonline.model.beid.bean.BeIdStartableBean;
import net.link.safeonline.pkix.dao.TrustDomainDAO;
import net.link.safeonline.pkix.dao.TrustPointDAO;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.JmxTestUtils;
import net.link.safeonline.test.util.MBeanActionHandler;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.util.ee.AuthIdentityServiceClient;
import net.link.safeonline.util.ee.IdentityServiceClient;

import org.easymock.EasyMock;


public class BeIdStartableBeanTest extends TestCase {

    private BeIdStartableBean testedInstance;

    private TrustDomainDAO    mockTrustDomainDAO;

    private TrustPointDAO     mockTrustPointDAO;

    private Object[]          mockObjects;


    @Override
    protected void setUp() throws Exception {

        super.setUp();

        JmxTestUtils jmxTestUtils = new JmxTestUtils();
        jmxTestUtils.setUp(AuthIdentityServiceClient.AUTH_IDENTITY_SERVICE);

        final KeyPair authKeyPair = PkiTestUtils.generateKeyPair();
        final X509Certificate authCertificate = PkiTestUtils.generateSelfSignedCertificate(authKeyPair, "CN=Test");
        jmxTestUtils.registerActionHandler(AuthIdentityServiceClient.AUTH_IDENTITY_SERVICE, "getCertificate",
                new MBeanActionHandler() {

                    public Object invoke(@SuppressWarnings("unused") Object[] arguments) {

                        return authCertificate;
                    }
                });

        jmxTestUtils.setUp(IdentityServiceClient.IDENTITY_SERVICE);

        final KeyPair keyPair = PkiTestUtils.generateKeyPair();
        final X509Certificate certificate = PkiTestUtils.generateSelfSignedCertificate(keyPair, "CN=Test");
        jmxTestUtils.registerActionHandler(IdentityServiceClient.IDENTITY_SERVICE, "getCertificate",
                new MBeanActionHandler() {

                    public Object invoke(@SuppressWarnings("unused") Object[] arguments) {

                        return certificate;
                    }
                });

        this.testedInstance = new BeIdStartableBean();

        this.mockTrustDomainDAO = createMock(TrustDomainDAO.class);
        EJBTestUtils.inject(this.testedInstance, this.mockTrustDomainDAO);

        this.mockTrustPointDAO = createMock(TrustPointDAO.class);
        EJBTestUtils.inject(this.testedInstance, this.mockTrustPointDAO);

        EJBTestUtils.init(this.testedInstance);

        this.mockObjects = new Object[] { this.mockTrustDomainDAO, this.mockTrustPointDAO };
    }

    public void testInitTrustDomain() throws Exception {

        // setup
        TrustDomainEntity trustDomain = new TrustDomainEntity(BeIdPkiProvider.TRUST_DOMAIN_NAME, true);

        // stubs
        expect(this.mockTrustDomainDAO.findTrustDomain(BeIdPkiProvider.TRUST_DOMAIN_NAME)).andStubReturn(null);

        // expectations
        expect(this.mockTrustDomainDAO.addTrustDomain(BeIdPkiProvider.TRUST_DOMAIN_NAME, true)).andReturn(trustDomain);
        this.mockTrustPointDAO.addTrustPoint(EasyMock.eq(trustDomain), (X509Certificate) EasyMock.anyObject());
        expectLastCall().times(1 + 2 + 1 + 15 + 20 + 1 + 1 + 1 + 1 + 1);

        // prepare
        replay(this.mockObjects);

        // operate
        this.testedInstance.initTrustDomain();

        // verify
        verify(this.mockObjects);
    }
}
