/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.sdk.ws.auth;

import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import net.lin_k.safe_online.auth.*;
import net.link.safeonline.auth.ws.soap.Confirmation;
import oasis.names.tc.saml._2_0.assertion.AttributeType;
import oasis.names.tc.saml._2_0.assertion.NameIDType;
import oasis.names.tc.saml._2_0.protocol.RequestAbstractType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.impl.SecureRandomIdentifierGenerator;
import org.w3._2000._09.xmldsig_.DSAKeyValueType;
import org.w3._2000._09.xmldsig_.KeyInfoType;
import org.w3._2000._09.xmldsig_.ObjectFactory;
import org.w3._2000._09.xmldsig_.RSAKeyValueType;


/**
 * <h2>{@link AuthenticationUtil}</h2>
 *
 * <p> Utility class for constructing the linkID Authentication WS messages. </p>
 *
 * <p> <i>Jan 15, 2009</i> </p>
 *
 * @author wvdhaute
 */
public class AuthenticationUtil {

    private static final Log LOG = LogFactory.getLog( AuthenticationUtil.class );

    public static WSAuthenticationRequestType getAuthenticationRequest(String applicationName, String deviceName, String language,
                                                                       Object deviceCredentials, PublicKey publicKey) {

        WSAuthenticationRequestType authenticationRequest = new WSAuthenticationRequestType();
        setRequest( authenticationRequest );

        // Issuer
        NameIDType issuerName = new NameIDType();
        issuerName.setValue( applicationName );
        authenticationRequest.setIssuer( issuerName );

        authenticationRequest.setApplicationName( applicationName );
        authenticationRequest.setDeviceName( deviceName );
        authenticationRequest.setLanguage( language );

        if (null != deviceCredentials) {
            DeviceCredentialsType deviceCredentialsType = new DeviceCredentialsType();
            if (deviceCredentials instanceof Map<?, ?>) {
                @SuppressWarnings("unchecked")
                Map<String, String> genericDeviceCredentials = (Map<String, String>) deviceCredentials;

                for (Map.Entry<String, String> entry : genericDeviceCredentials.entrySet()) {
                    NameValuePairType nameValuePair = new NameValuePairType();
                    nameValuePair.setName( entry.getKey() );
                    nameValuePair.setValue( entry.getValue() );
                    deviceCredentialsType.getNameValuePair().add( nameValuePair );
                }
            } else if (deviceCredentials instanceof JAXBElement<?>)
                deviceCredentialsType.getAny().add( deviceCredentials );
            authenticationRequest.setDeviceCredentials( deviceCredentialsType );
        }

        if (null != publicKey) {
            KeyInfoType keyInfo = getKeyInfo( publicKey );
            authenticationRequest.setKeyInfo( keyInfo );
        }

        return authenticationRequest;
    }

    public static WSAuthenticationGlobalUsageAgreementRequestType getGlobalUsageAgreementRequest() {

        WSAuthenticationGlobalUsageAgreementRequestType request = new WSAuthenticationGlobalUsageAgreementRequestType();
        setRequest( request );
        return request;
    }

    public static WSAuthenticationGlobalUsageAgreementConfirmationType getGlobalUsageAgreementConfirmationRequest(
            Confirmation confirmation) {

        WSAuthenticationGlobalUsageAgreementConfirmationType request = new WSAuthenticationGlobalUsageAgreementConfirmationType();
        setRequest( request );
        request.setConfirmation( confirmation.getValue() );
        return request;
    }

    public static WSAuthenticationUsageAgreementRequestType getUsageAgreementRequest() {

        WSAuthenticationUsageAgreementRequestType request = new WSAuthenticationUsageAgreementRequestType();
        setRequest( request );
        return request;
    }

    public static WSAuthenticationUsageAgreementConfirmationType getUsageAgreementConfirmationRequest(Confirmation confirmation) {

        WSAuthenticationUsageAgreementConfirmationType request = new WSAuthenticationUsageAgreementConfirmationType();
        setRequest( request );
        request.setConfirmation( confirmation.getValue() );
        return request;
    }

    public static WSAuthenticationIdentityRequestType getIdentityRequest() {

        WSAuthenticationIdentityRequestType request = new WSAuthenticationIdentityRequestType();
        setRequest( request );
        return request;
    }

    public static WSAuthenticationIdentityConfirmationType getIdentityConfirmationRequest(List<AttributeIdentitySDK> attributes) {

        WSAuthenticationIdentityConfirmationType request = new WSAuthenticationIdentityConfirmationType();
        setRequest( request );

        if (null != attributes)
            for (AttributeIdentitySDK attribute : attributes) {
                AttributeType attributeType = attribute.toSDK();
                request.getAttribute().add( attributeType );
            }

        return request;
    }

    private static void setRequest(RequestAbstractType request) {

        SecureRandomIdentifierGenerator idGenerator;
        try {
            idGenerator = new SecureRandomIdentifierGenerator();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException( "secure random init error: " + e.getMessage(), e );
        }
        String id = idGenerator.generateIdentifier();
        XMLGregorianCalendar now = getCurrentXmlGregorianCalendar();

        request.setID( id );
        request.setVersion( SAMLVersion.VERSION_20.toString() );
        request.setIssueInstant( now );
    }

    private static XMLGregorianCalendar getCurrentXmlGregorianCalendar() {

        DatatypeFactory datatypeFactory;
        try {
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            LOG.error( "datatype configuration exception", e );
            throw new RuntimeException( "datatype configuration exception: " + e.getMessage() );
        }

        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        Date now = new Date();
        gregorianCalendar.setTime( now );
        return datatypeFactory.newXMLGregorianCalendar( gregorianCalendar );
    }

    /**
     * Converts public key to XML DSig KeyInfoType
     *
     * @param publicKey public key
     *
     * @return converted KeyInfo
     */
    private static KeyInfoType getKeyInfo(PublicKey publicKey) {

        KeyInfoType keyInfo = new KeyInfoType();
        ObjectFactory dsigObjectFactory = new ObjectFactory();

        if (publicKey instanceof RSAPublicKey) {
            RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;
            RSAKeyValueType rsaKeyValue = new RSAKeyValueType();
            rsaKeyValue.setModulus( rsaPublicKey.getModulus().toByteArray() );
            rsaKeyValue.setExponent( rsaPublicKey.getPublicExponent().toByteArray() );
            keyInfo.getContent().add( dsigObjectFactory.createRSAKeyValue( rsaKeyValue ) );
        } else if (publicKey instanceof DSAPublicKey) {
            DSAPublicKey dsaPublicKey = (DSAPublicKey) publicKey;
            DSAKeyValueType dsaKeyValue = new DSAKeyValueType();
            dsaKeyValue.setY( dsaPublicKey.getY().toByteArray() );
            dsaKeyValue.setG( dsaPublicKey.getParams().getG().toByteArray() );
            dsaKeyValue.setP( dsaPublicKey.getParams().getP().toByteArray() );
            dsaKeyValue.setQ( dsaPublicKey.getParams().getQ().toByteArray() );
            keyInfo.getContent().add( dsaKeyValue );
        } else
            throw new IllegalArgumentException( "Only RSAPublicKey and DSAPublicKey are supported" );

        return keyInfo;
    }
}
