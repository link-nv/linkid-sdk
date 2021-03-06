/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.xkms2;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import net.link.safeonline.sdk.api.exception.LinkIDValidationFailedException;
import net.link.safeonline.sdk.api.exception.LinkIDWSClientTransportException;
import net.link.safeonline.sdk.api.ws.xkms2.LinkIDResultMajorCode;
import net.link.safeonline.sdk.api.ws.xkms2.LinkIDXkms2Client;
import net.link.safeonline.sdk.ws.LinkIDAbstractWSClient;
import net.link.safeonline.ws.xkms2.LinkIDXkms2ServiceFactory;
import net.link.util.common.CertificateChain;
import net.link.util.logging.Logger;
import org.jetbrains.annotations.Nullable;
import org.w3._2000._09.xmldsig.KeyInfoType;
import org.w3._2000._09.xmldsig.X509DataType;
import org.w3._2002._03.xkms.ObjectFactory;
import org.w3._2002._03.xkms.QueryKeyBindingType;
import org.w3._2002._03.xkms.UseKeyWithType;
import org.w3._2002._03.xkms.ValidateRequestType;
import org.w3._2002._03.xkms.ValidateResultType;
import org.w3._2002._03.xkms.XKMSPortType;


/**
 * Implementation of XKMS 2.0 Service Client.
 *
 * @author wvdhaute
 */
public class LinkIDXkms2ClientImpl extends LinkIDAbstractWSClient<XKMSPortType> implements LinkIDXkms2Client {

    private static final Logger logger = Logger.get( LinkIDXkms2ClientImpl.class );

    /**
     * Main constructor.
     *
     * @param location        the location (host:port) of the XKMS 2 web service.
     * @param sslCertificates If not {@code null} will verify the server SSL {@link X509Certificate}.
     */
    public LinkIDXkms2ClientImpl(String location, X509Certificate[] sslCertificates) {

        super( location, LinkIDXkms2ServiceFactory.newInstance().getXKMSPort(), sslCertificates );
    }

    public LinkIDXkms2ClientImpl(String location, String path, X509Certificate[] sslCertificates) {

        super( location, path, LinkIDXkms2ServiceFactory.newInstance().getXKMSPort(), sslCertificates );
    }

    @Override
    protected String getLocationProperty() {

        return "linkid.ws.xkms2.path";
    }

    @Override
    public void validate(final X509Certificate... certificateChain)
            throws LinkIDWSClientTransportException, LinkIDValidationFailedException, CertificateEncodingException {

        validate( new CertificateChain( certificateChain ) );
    }

    @Override
    public void validate(final String useKeyWithApplication, final String useKeyWithIdentifier, final X509Certificate... certificateChain)
            throws LinkIDWSClientTransportException, LinkIDValidationFailedException, CertificateEncodingException {

        validate( useKeyWithApplication, useKeyWithIdentifier, new CertificateChain( certificateChain ) );
    }

    private void validate(final CertificateChain certificateChain)
            throws LinkIDWSClientTransportException, LinkIDValidationFailedException, CertificateEncodingException {

        validate( null, null, certificateChain );
    }

    @Override
    public void validate(@Nullable final String useKeyWithApplication, @Nullable final String useKeyWithIdentifier, final CertificateChain certificateChain)
            throws LinkIDWSClientTransportException, LinkIDValidationFailedException, CertificateEncodingException {

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
            x509Data.getX509IssuerSerialOrX509SKIOrX509SubjectName().add( xmldsigObjectFactory.createX509DataTypeX509Certificate( encodedCertificate ) );
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
        catch (RuntimeException e) {
            throw new LinkIDWSClientTransportException( getBindingProvider(), e );
        }

        if (!result.getResultMajor().equals( LinkIDResultMajorCode.SUCCESS.getErrorCode() )) {
            throw new LinkIDValidationFailedException(
                    String.format( "Certificate chain validation failed: ResultMajor=%s ResultMinor=%s", result.getResultMajor(), result.getResultMinor() ) );
        }
    }
}
