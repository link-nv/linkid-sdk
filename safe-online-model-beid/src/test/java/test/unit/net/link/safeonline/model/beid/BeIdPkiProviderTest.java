/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.model.beid;

import java.security.KeyPair;
import java.security.cert.X509Certificate;

import junit.framework.TestCase;
import net.link.safeonline.model.beid.bean.BeIdPkiProviderBean;
import net.link.safeonline.test.util.PkiTestUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class BeIdPkiProviderTest extends TestCase {

    private static final Log LOG = LogFactory.getLog(BeIdPkiProviderTest.class);

    private BeIdPkiProviderBean  testedInstance;


    @Override
    protected void setUp() throws Exception {

        super.setUp();

        this.testedInstance = new BeIdPkiProviderBean();
    }

    public void testAcceptBeIDCertificate() throws Exception {

        // setup
        X509Certificate certificate = PkiTestUtils.loadCertificateFromResource("/fcorneli-auth.crt");

        // operate
        boolean result = this.testedInstance.accept(certificate);

        // verify
        LOG.debug("certificate: " + certificate);
        LOG.debug("certificate size: " + certificate.getEncoded().length);
        assertTrue(result);
    }

    public void testDoNotAcceptAnotherCertificate() throws Exception {

        // setup
        KeyPair keyPair = PkiTestUtils.generateKeyPair();
        X509Certificate certificate = PkiTestUtils.generateSelfSignedCertificate(keyPair, "CN=Test");

        // operate
        boolean result = this.testedInstance.accept(certificate);

        // verify
        assertFalse(result);
    }

    public void testSubjectIdentifier() throws Exception {

        // setup
        KeyPair keyPair = PkiTestUtils.generateKeyPair();
        X509Certificate certificate = PkiTestUtils.generateSelfSignedCertificate(keyPair, "CN=Test");

        // operate
        String result = this.testedInstance.getSubjectIdentifier(certificate);

        // verify
        assertNotNull(result);
        LOG.debug("subject identifier: " + result);
    }
}
