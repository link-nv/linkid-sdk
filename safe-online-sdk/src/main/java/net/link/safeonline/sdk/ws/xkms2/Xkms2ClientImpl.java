/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.xkms2;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.List;
import javax.xml.ws.BindingProvider;
import net.link.safeonline.sdk.logging.exception.ValidationFailedException;
import net.link.safeonline.sdk.logging.exception.WSClientTransportException;
import net.link.safeonline.sdk.ws.AbstractWSClient;
import net.link.safeonline.xkms2.ws.ResultMajorCode;
import net.link.safeonline.xkms2.ws.Xkms2ServiceFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3._2000._09.xmldsig_.KeyInfoType;
import org.w3._2000._09.xmldsig_.X509DataType;
import org.w3._2002._03.xkms_.ObjectFactory;
import org.w3._2002._03.xkms_.QueryKeyBindingType;
import org.w3._2002._03.xkms_.ValidateRequestType;
import org.w3._2002._03.xkms_.ValidateResultType;
import org.w3._2002._03.xkms_wsdl.XKMSPortType;
import org.w3._2002._03.xkms_wsdl.XKMSService;


/**
 * Implementation of XKMS 2.0 Service Client.
 *
 * @author wvdhaute
 */
public class Xkms2ClientImpl extends AbstractWSClient implements Xkms2Client {

    private static final Log LOG = LogFactory.getLog( Xkms2ClientImpl.class );

    private final XKMSPortType port;

    private final String location;

    /**
     * Main constructor.
     *
     * @param location       the location (host:port) of the LinkID XKMS 2 web service.
     * @param sslCertificate If not <code>null</code> will verify the server SSL {@link X509Certificate}.
     */
    public Xkms2ClientImpl(String location, X509Certificate sslCertificate) {

        XKMSService service = Xkms2ServiceFactory.newInstance();
        port = service.getXKMSPort();
        this.location = location + "/xkms2";
        setEndpointAddress();

        registerMessageLoggerHandler( port );

        registerTrustManager( port, sslCertificate );
    }

    private void setEndpointAddress() {

        BindingProvider bindingProvider = (BindingProvider) port;
        bindingProvider.getRequestContext().put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY, location );
    }

    /**
     * {@inheritDoc}
     */
    public void validate(final List<X509Certificate> certificateChain)
            throws WSClientTransportException, ValidationFailedException, CertificateEncodingException {

        LOG.debug( "validate" );

        ObjectFactory objectFactory = new ObjectFactory();
        org.w3._2000._09.xmldsig_.ObjectFactory xmldsigObjectFactory = new org.w3._2000._09.xmldsig_.ObjectFactory();

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
        request.setQueryKeyBinding( queryKeyBinding );

        ValidateResultType result;
        try {
            result = port.validate( request );
        } catch (Exception e) {
            LOG.error( "Exception: " + e.getMessage(), e );
            throw new WSClientTransportException( location, e );
        } finally {
            LOG.debug( "finally" );
            retrieveHeadersFromPort( port );
            LOG.debug( "finally done" );
        }

        if (!result.getResultMajor().equals( ResultMajorCode.SUCCESS.getErrorCode() )) {
            throw new ValidationFailedException(
                    "Certificate chain validation failed: ResultMajor=" + result.getResultMajor() + " ResultMinor="
                    + result.getResultMinor() );
        }
    }
}
