/*
 * SafeOnline project.
 * 
 * Copyright 2005-2006 Frank Cornelis.
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.p11sc;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;


public interface SmartCard {

    /**
     * Initializes the smart card Java component. The argument is a list of smart card configs since this component is
     * designed to support multiple smart cards.
     * 
     * @param smartCardConfigs
     * @param smartCardInteraction
     *            the optional smart card interaction object.
     */
    void init(List<SmartCardConfig> smartCardConfigs, SmartCardInteraction smartCardInteraction);

    /**
     * Sets the smart card PIN callback.
     * 
     * @param smartCardPinCallback
     */
    void setSmartCardPinCallback(SmartCardPinCallback smartCardPinCallback);

    /**
     * Open a connection towards the smart card device.
     * 
     * @param smartCardAlias
     *            the alias of the smart card config that should be used.
     * 
     * @throws SmartCardNotFoundException
     * @throws NoPkcs11LibraryException
     * @throws MissingSmartCardReaderException
     * @throws UnsupportedSmartCardException
     */
    void open(String smartCardAlias) throws SmartCardNotFoundException, NoPkcs11LibraryException,
            MissingSmartCardReaderException, UnsupportedSmartCardException;

    /**
     * Close the connection towards the smart card device.
     */
    void close();

    /**
     * Checks whether there is an existing connection towards the smart card device.
     * 
     */
    boolean isOpen();

    /**
     * Gives back the private signature key.
     * 
     * @return the private signature key.
     */
    PrivateKey getSignaturePrivateKey();

    /**
     * Gives back the private authentication key.
     * 
     * @return the private authentication key.
     */
    PrivateKey getAuthenticationPrivateKey();

    /**
     * Gives back the signature X509 certificate.
     * 
     * @return the X509 signature certificate.
     */
    X509Certificate getSignatureCertificate();

    /**
     * Gives back the X509 authentication certificate.
     * 
     * @return the X509 authentication certificate.
     */
    X509Certificate getAuthenticationCertificate();

    List<X509Certificate> getAuthenticationCertificatePath();

    String getGivenName();

    String getSurname();

    /**
     * Gives back the country code. This is the uppercase ISO 3166 2-letter code.
     * 
     */
    String getCountryCode();

    String getStreet();

    String getPostalCode();

    String getCity();

    /**
     * Resets the PKCS11 drivers cached by the SunPKCS11 security provider.
     * 
     * This fixes the issue we have when the smart card gets removed and reinserted.
     */
    void resetPKCS11Driver();
}
