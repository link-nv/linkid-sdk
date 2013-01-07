/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.xkms2;

import com.lyndir.lhunath.opal.system.logging.Logger;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import javax.xml.ws.BindingProvider;
import net.link.safeonline.sdk.api.exception.ValidationFailedException;
import net.link.safeonline.sdk.api.exception.WSClientTransportException;
import net.link.safeonline.sdk.api.ws.xkms2.ResultMajorCode;
import net.link.safeonline.sdk.api.ws.xkms2.client.Xkms2Client;
import net.link.safeonline.sdk.ws.SDKUtils;
import net.link.safeonline.ws.xkms2.Xkms2ServiceFactory;
import net.link.util.common.CertificateChain;
import net.link.util.ws.AbstractWSClient;
import org.jetbrains.annotations.Nullable;
import org.w3._2000._09.xmldsig.KeyInfoType;
import org.w3._2000._09.xmldsig.X509DataType;
import org.w3._2002._03.xkms.*;


/**
 * Implementation of XKMS 2.0 Service Client.
 *
 * @author wvdhaute
 */
public class Xkms2ClientImpl extends AbstractWSClient<XKMSPortType> implements Xkms2Client {

    private static final Logger logger = Logger.get( Xkms2ClientImpl.class );

    /**
     * Main constructor.
     *
     * @param location       the location (host:port) of the LinkID XKMS 2 web service.
     * @param sslCertificate If not {@code null} will verify the server SSL {@link X509Certificate}.
     */
    public Xkms2ClientImpl(String location, X509Certificate sslCertificate) {

        this( location, SDKUtils.getSDKProperty( "linkid.ws.xkms2.path" ), sslCertificate );
    }

    /**
     * Main constructor.
     *
     * @param location       the location (host:port) of the XKMS 2 web service.
     * @param path           the path where the XKMS2 WS runs
     * @param sslCertificate If not {@code null} will verify the server SSL {@link X509Certificate}.
     */
    public Xkms2ClientImpl(String location, String path, X509Certificate sslCertificate) {

        super( Xkms2ServiceFactory.newInstance().getXKMSPort(), sslCertificate );
        getBindingProvider().getRequestContext().put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY, String.format( "%s/%s", location, path ) );
    }

    @Override
    public void validate(final X509Certificate... certificateChain)
            throws WSClientTransportException, ValidationFailedException, CertificateEncodingException {

        validate( new CertificateChain( certificateChain ) );
    }

    @Override
    public void validate(final String useKeyWithApplication, final String useKeyWithIdentifier, final X509Certificate... certificateChain)
            throws WSClientTransportException, ValidationFailedException, CertificateEncodingException {

        validate( useKeyWithApplication, useKeyWithIdentifier, new CertificateChain( certificateChain ) );
    }

    public void validate(final CertificateChain certificateChain)
            throws WSClientTransportException, ValidationFailedException, CertificateEncodingException {

        validate( null, null, certificateChain );
    }

    public void validate(@Nullable final String useKeyWithApplication, @Nullable final String useKeyWithIdentifier,
                         final CertificateChain certificateChain)
            throws WSClientTransportException, ValidationFailedException, CertificateEncodingException {

        logger.dbg( "validate (useKeyWith: %s=%s)", useKeyWithApplication, useKeyWithIdentifier );

        ObjectFactory objectFactory = new ObjectFactory();
        org.w3._2000._09.xmldsig.ObjectFactory xmldsigObjectFactory = new org.w3._2000._09.xmldsig.ObjectFactory();

        ValidateRequestType request = objectFactory.createValidateRequestType();
        QueryKeyBindingType queryKeyBinding = objectFactory.createQueryKeyBindingType();
        KeyInfoType keyInfo = xmldsigObjectFactory.createKeyInfoType();
        queryKeyBinding.setKeyInfo( keyInfo );
        X509DataType x509Data = xmldsigObjectFactory.createX509DataType();
        for (X509Certificate certificate : certificateChain) {
            byte[] encodedCertificate = certificate.getEncoded();
            x509Data.getX509IssuerSerialOrX509SKIOrX509SubjectName()
                    .add( xmldsigObjectFactory.createX509DataTypeX509Certificate( encodedCertificate ) );
        }
        keyInfo.getContent().add( xmldsigObjectFactory.createX509Data( x509Data ) );

        if (null != useKeyWithApplication && null != useKeyWithIdentifier) {
            UseKeyWithType useKeyWith = objectFactory.createUseKeyWithType();
            useKeyWith.setApplication( useKeyWithApplication );
            useKeyWith.setIdentifier( useKeyWithIdentifier );
            queryKeyBinding.getUseKeyWith().add( useKeyWith );
        }

        request.setQueryKeyBinding( queryKeyBinding );

        ValidateResultType result;
        try {
            result = getPort().validate( request );
        }
        catch (Exception e) {
            throw new WSClientTransportException( getBindingProvider(), e );
        }

        if (!result.getResultMajor().equals( ResultMajorCode.SUCCESS.getErrorCode() )) {
            throw new ValidationFailedException(
                    String.format( "Certificate chain validation failed: ResultMajor=%s ResultMinor=%s", result.getResultMajor(),
                            result.getResultMinor() ) );
        }
    }
}
