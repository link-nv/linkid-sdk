/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.model.beid.bean;

import static org.easymock.EasyMock.checkOrder;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.security.KeyPair;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import junit.framework.TestCase;
import net.link.safeonline.entity.pkix.TrustDomainEntity;
import net.link.safeonline.keystore.SafeOnlineNodeKeyStore;
import net.link.safeonline.keystore.service.KeyService;
import net.link.safeonline.model.beid.bean.BeIdPkiProviderBean;
import net.link.safeonline.model.beid.bean.BeIdStartableBean;
import net.link.safeonline.pkix.dao.TrustDomainDAO;
import net.link.safeonline.pkix.dao.TrustPointDAO;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;

import org.easymock.EasyMock;


public class BeIdStartableBeanTest extends TestCase {

    private BeIdStartableBean testedInstance;

    private TrustDomainDAO    mockTrustDomainDAO;

    private TrustPointDAO     mockTrustPointDAO;

    private Object[]          mockObjects;

    private KeyService        mockKeyService;

    private JndiTestUtils     jndiTestUtils;


    @Override
    protected void setUp()
            throws Exception {

        super.setUp();

        testedInstance = new BeIdStartableBean();

        mockTrustDomainDAO = createMock(TrustDomainDAO.class);
        EJBTestUtils.inject(testedInstance, mockTrustDomainDAO);

        mockTrustPointDAO = createMock(TrustPointDAO.class);
        EJBTestUtils.inject(testedInstance, mockTrustPointDAO);

        mockKeyService = createMock(KeyService.class);
        checkOrder(mockKeyService, false);

        final KeyPair nodeKeyPair = PkiTestUtils.generateKeyPair();
        final X509Certificate nodeCertificate = PkiTestUtils.generateSelfSignedCertificate(nodeKeyPair, "CN=Test");
        expect(mockKeyService.getPrivateKeyEntry(SafeOnlineNodeKeyStore.class)).andReturn(
                new PrivateKeyEntry(nodeKeyPair.getPrivate(), new Certificate[] { nodeCertificate }));

        jndiTestUtils = new JndiTestUtils();
        jndiTestUtils.setUp();
        jndiTestUtils.bindComponent(KeyService.JNDI_BINDING, mockKeyService);

        EJBTestUtils.init(testedInstance);

        mockObjects = new Object[] { mockTrustDomainDAO, mockTrustPointDAO, mockKeyService };
    }

    public void testInitTrustDomain()
            throws Exception {

        // setup
        TrustDomainEntity trustDomain = new TrustDomainEntity(BeIdPkiProviderBean.TRUST_DOMAIN_NAME, true);

        // stubs
        expect(mockTrustDomainDAO.findTrustDomain(BeIdPkiProviderBean.TRUST_DOMAIN_NAME)).andStubReturn(null);

        // expectations
        expect(mockTrustDomainDAO.addTrustDomain(BeIdPkiProviderBean.TRUST_DOMAIN_NAME, true)).andReturn(trustDomain);
        mockTrustPointDAO.addTrustPoint(EasyMock.eq(trustDomain), (X509Certificate) EasyMock.anyObject());
        expectLastCall().times(1 + 2 + 1 + 15 + 20 + 1 + 1 + 1 + 1 + 1 + 11);

        // prepare
        replay(mockObjects);

        // operate
        testedInstance.initTrustDomain();

        // verify
        verify(mockObjects);
    }
}
