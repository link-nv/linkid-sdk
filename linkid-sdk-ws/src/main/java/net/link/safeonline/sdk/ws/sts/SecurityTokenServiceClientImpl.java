/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.sts;

import com.lyndir.lhunath.opal.system.logging.Logger;
import com.sun.xml.internal.ws.client.ClientTransportException;
import java.security.cert.X509Certificate;
import java.util.*;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import net.link.safeonline.sdk.api.exception.ValidationFailedException;
import net.link.safeonline.sdk.api.exception.WSClientTransportException;
import net.link.safeonline.sdk.api.ws.WebServiceConstants;
import net.link.safeonline.sdk.api.ws.sts.SecurityTokenServiceConstants;
import net.link.safeonline.sdk.api.ws.sts.TrustDomainType;
import net.link.safeonline.sdk.api.ws.sts.client.SecurityTokenServiceClient;
import net.link.safeonline.sdk.ws.SDKUtils;
import net.link.safeonline.ws.sts.SecurityTokenServiceFactory;
import net.link.util.ws.AbstractWSClient;
import net.link.util.ws.security.WSSecurityConfiguration;
import net.link.util.ws.security.WSSecurityHandler;
import org.jetbrains.annotations.Nullable;
import org.oasis_open.docs.ws_sx.ws_trust._200512.*;
import org.opensaml.saml2.core.*;
import org.w3c.dom.Element;


/**
 * Implementation of Security Token Service Client.
 *
 * @author fcorneli
 */
public class SecurityTokenServiceClientImpl extends AbstractWSClient<SecurityTokenServicePort> implements
        SecurityTokenServiceClient<Response, LogoutResponse, LogoutRequest> {

    private static final Logger logger = Logger.get( SecurityTokenServiceClientImpl.class );

    /**
     * Main constructor.
     *
     * @param location       the location (host:port) of the LinkID STS web service.
     * @param sslCertificate If not {@code null} will verify the server SSL {@link X509Certificate}.
     * @param configuration  WS Security Configuration
     */
    public SecurityTokenServiceClientImpl(String location, X509Certificate sslCertificate, final WSSecurityConfiguration configuration) {

        super( SecurityTokenServiceFactory.newInstance().getSecurityTokenServicePort(), sslCertificate );
        getBindingProvider().getRequestContext()
                .put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                        String.format( "%s/%s", location, SDKUtils.getSDKProperty( "linkid.ws.sts.path" ) ) );

        WSSecurityHandler.install( getBindingProvider(), configuration );
    }

    private void validate(Element token, TrustDomainType trustDomain, @Nullable Map<QName, String> otherAttributes, String queryString,
                          StringBuffer requestUrl)
            throws WSClientTransportException, ValidationFailedException {

        RequestSecurityTokenType request = new RequestSecurityTokenType();
        ObjectFactory objectFactory = new ObjectFactory();
        JAXBElement<String> requestType = objectFactory.createRequestType(
                String.format( "%sValidate#%s", WebServiceConstants.WS_TRUST_REQUEST_TYPE, trustDomain ) );
        request.getAny().add( requestType );

        JAXBElement<String> tokenType = objectFactory.createTokenType( SecurityTokenServiceConstants.TOKEN_TYPE_STATUS );
        request.getAny().add( tokenType );

        ValidateTargetType validateTarget = new ValidateTargetType();
        validateTarget.setAny( token );
        request.getAny().add( objectFactory.createValidateTarget( validateTarget ) );

        request.getOtherAttributes().put( WebServiceConstants.SAML_QUERY_STRING_ATTRIBUTE, queryString );
        request.getOtherAttributes().put( WebServiceConstants.SAML_REQUEST_URL_ATTRIBUTE, requestUrl.toString() );
        if (null != otherAttributes)
            request.getOtherAttributes().putAll( otherAttributes );

        RequestSecurityTokenResponseType response;
        try {
            response = getPort().requestSecurityToken( request );
        }
        catch (ClientTransportException e) {
            throw new WSClientTransportException( getBindingProvider(), e );
        }

        StatusType status = null;
        List<Object> results = response.getAny();
        for (Object result : results)
            if (result instanceof JAXBElement<?>) {
                JAXBElement<?> resultElement = (JAXBElement<?>) result;
                Object value = resultElement.getValue();
                if (value instanceof StatusType)
                    status = (StatusType) value;
            }
        if (null == status)
            throw new ValidationFailedException( "no Status found in response" );
        String statusCode = status.getCode();
        if (SecurityTokenServiceConstants.STATUS_VALID.equals( statusCode ))
            return;
        String reason = status.getReason();
        logger.dbg( "reason: %s", reason );
        throw new ValidationFailedException( String.format( "token found to be invalid: %s", reason ) );
    }

    @Override
    public void validateResponse(final Response response, final String requestIssuer, final String requestQueryString,
                                 final StringBuffer requestURL)
            throws WSClientTransportException, ValidationFailedException {

        Map<QName, String> otherAttributes = new HashMap<QName, String>();
        otherAttributes.put( WebServiceConstants.SAML_AUDIENCE_ATTRIBUTE, requestIssuer );

        validate( response.getDOM(), TrustDomainType.LINK_ID, otherAttributes, requestQueryString, requestURL );
    }

    @Override
    public void validateLogoutResponse(final LogoutResponse logoutResponse, final String requestQueryString, final StringBuffer requestURL)
            throws WSClientTransportException, ValidationFailedException {

        validate( logoutResponse.getDOM(), TrustDomainType.LINK_ID, null, requestQueryString, requestURL );
    }

    @Override
    public void validateLogoutRequest(final LogoutRequest logoutRequest, final String requestQueryString, final StringBuffer requestURL)
            throws WSClientTransportException, ValidationFailedException {

        validate( logoutRequest.getDOM(), TrustDomainType.LINK_ID, null, requestQueryString, requestURL );
    }
}
