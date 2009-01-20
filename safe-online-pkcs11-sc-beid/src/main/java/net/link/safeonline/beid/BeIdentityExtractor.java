/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.beid;

import java.security.cert.X509Certificate;

import javax.security.auth.x500.X500Principal;

import net.link.safeonline.p11sc.spi.IdentityDataCollector;
import net.link.safeonline.p11sc.spi.IdentityDataExtractor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class BeIdentityExtractor implements IdentityDataExtractor {

    private static final Log      LOG = LogFactory.getLog(BeIdentityExtractor.class);

    private IdentityDataCollector identityDataCollector;


    public void init(IdentityDataCollector inIdentityDataCollector) {

        LOG.debug("init");
        identityDataCollector = inIdentityDataCollector;
    }

    public void postPkcs11(X509Certificate authenticationCertificate) {

        LOG.debug("postPkcs11");
        String subjectName = getSubjectName(authenticationCertificate);
        LOG.debug("subject: " + subjectName);

        String givenName = getAttributeFromSubjectName(subjectName, "GIVENNAME");
        identityDataCollector.setGivenName(givenName);

        String surname = getAttributeFromSubjectName(subjectName, "SURNAME");
        identityDataCollector.setSurname(surname);

        String countryCode = getAttributeFromSubjectName(subjectName, "C");
        identityDataCollector.setCountryCode(countryCode);
    }

    private String getSubjectName(X509Certificate certificate) {

        X500Principal subjectPrincipal = certificate.getSubjectX500Principal();
        String subjectName = subjectPrincipal.toString();
        return subjectName;
    }

    private String getAttributeFromSubjectName(String subjectName, String attributeName) {

        int attributeBegin = subjectName.indexOf(attributeName + "=");
        if (-1 == attributeBegin)
            throw new IllegalArgumentException("attribute name does not occur in subject: " + attributeName);
        attributeBegin += attributeName.length() + 1; // "attributeName="
        int attributeEnd = subjectName.indexOf(",", attributeBegin);
        if (-1 == attributeEnd) {
            // last field has no trailing ","
            attributeEnd = subjectName.length();
        }
        String attributeValue = subjectName.substring(attributeBegin, attributeEnd);
        return attributeValue;
    }

    public void prePkcs11() {

        LOG.debug("prePkcs11");
    }
}
