/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.client.sdk.device.beid;

public interface BeIdDevice {

    // attributes
    String ATTRIBUTE_NRN               = "device.beid.nrn";
    String ATTRIBUTE_MASKED_NRN        = "device.beid.maskedNrn";
    String ATTRIBUTE_FAMILYNAME        = "device.beid.surname";
    String ATTRIBUTE_GIVENNAME         = "device.beid.givenName";
    String ATTRIBUTE_NATIONALITY       = "device.beid.nationality";
    String ATTRIBUTE_PLACE_OF_BIRTH    = "device.beid.placeOfBirth";
    String ATTRIBUTE_DATE_OF_BIRTH     = "device.beid.dateOfBirth";
    String ATTRIBUTE_GENDER            = "device.beid.gender";
    String ATTRIBUTE_STREET_AND_NUMBER = "device.beid.streetAndNumber";
    String ATTRIBUTE_POSTAL_CODE       = "device.beid.zip";
    String ATTRIBUTE_MUNICIPALITY      = "device.beid.municipality";

    // WS-Authentication
    String WS_AUTH_SALT_ATTRIBUTE                    = "urn:net:lin-k:safe-online:beid:ws:auth:salt";
    String WS_AUTH_AUTHN_SIGNATURE_VALUE_ATTRIBUTE   = "urn:net:lin-k:safe-online:beid:ws:auth:authnSignatureValue";
    String WS_AUTH_AUTHN_CERTIFICATE_CHAIN_ATTRIBUTE = "urn:net:lin-k:safe-online:beid:ws:auth:authnCertificateChain";
    String WS_AUTH_CHALLENGE_ATTRIBUTE               = "urn:net:lin-k:safe-online:beid:ws:auth:challenge";

    String WS_AUTH_ID_FILE_ATTRIBUTE                 = "urn:net:lin-k:safe-online:beid:ws:auth:idFile";
    String WS_AUTH_ADDRESS_FILE_ATTRIBUTE            = "urn:net:lin-k:safe-online:beid:ws:auth:addressFile";
    String WS_AUTH_IDENTITY_SIGNATURE_FILE_ATTRIBUTE = "urn:net:lin-k:safe-online:beid:ws:auth:identitySignatureFile";
    String WS_AUTH_ADDRESS_SIGNATURE_FILE_ATTRIBUTE  = "urn:net:lin-k:safe-online:beid:ws:auth:addressSignatureFile";
    String WS_AUTH_RRN_CERT_FILE_ATTRIBUTE           = "urn:net:lin-k:safe-online:beid:ws:auth:rrnCertFile";
    String WS_AUTH_ROOT_CERT_FILE_ATTRIBUTE          = "urn:net:lin-k:safe-online:beid:ws:auth:rootCertFile";
}
