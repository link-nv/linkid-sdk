/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.authentication.service.bean;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.UUID;

import net.link.safeonline.p11sc.SmartCard;
import net.link.safeonline.p11sc.SmartCardConfig;
import net.link.safeonline.p11sc.SmartCardInteraction;
import net.link.safeonline.p11sc.SmartCardPinCallback;


public class SoftwareSmartCard implements SmartCard {

    private final KeyPair         keyPair;

    private final X509Certificate certificate;

    private final String          surname;

    private final String          givenName;


    public SoftwareSmartCard(KeyPair keyPair, X509Certificate certificate) {

        this.keyPair = keyPair;
        this.certificate = certificate;
        this.surname = UUID.randomUUID().toString();
        this.givenName = UUID.randomUUID().toString();
    }

    public void close() {

    }

    public X509Certificate getAuthenticationCertificate() {

        return this.certificate;
    }

    public PrivateKey getAuthenticationPrivateKey() {

        return this.keyPair.getPrivate();
    }

    public String getCity() {

        return null;
    }

    public String getCountryCode() {

        return null;
    }

    public String getGivenName() {

        return this.givenName;
    }

    public String getPostalCode() {

        return null;
    }

    public X509Certificate getSignatureCertificate() {

        return null;
    }

    public PrivateKey getSignaturePrivateKey() {

        return null;
    }

    public String getStreet() {

        return null;
    }

    public String getSurname() {

        return this.surname;
    }

    public void init(@SuppressWarnings("unused") List<SmartCardConfig> smartCardConfigs,
            SmartCardInteraction smartCardInteraction) {

    }

    public boolean isOpen() {

        return false;
    }

    public void setSmartCardPinCallback(@SuppressWarnings("unused") SmartCardPinCallback smartCardPinCallback) {

    }

    public void open(@SuppressWarnings("unused") String smartCardAlias) {

    }

    public List<X509Certificate> getAuthenticationCertificatePath() {

        return null;
    }

    public void resetPKCS11Driver() {

    }
}
