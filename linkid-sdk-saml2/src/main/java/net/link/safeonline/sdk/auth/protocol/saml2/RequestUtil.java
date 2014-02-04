/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.protocol.saml2;

import com.lyndir.lhunath.opal.system.logging.Logger;
import java.io.IOException;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.link.safeonline.sdk.api.auth.LoginMode;
import net.link.safeonline.sdk.api.auth.StartPage;
import net.link.safeonline.sdk.configuration.SAMLBinding;
import net.link.util.common.CertificateChain;
import net.link.util.exception.ValidationFailedException;
import net.link.util.saml.Saml2Utils;
import org.jetbrains.annotations.Nullable;
import org.opensaml.saml2.core.*;


/**
 * Utility class for SAML2 requests.
 *
 * @author lhunath
 */
public abstract class RequestUtil {

    private static final Logger logger = Logger.get( RequestUtil.class );

    /**
     * Sends a SAML2 Request.
     *
     * @param consumerUrl          The URL of the SAML response message consumer.
     * @param requestBinding       The SAML Binding to use for communicating the request to the consumer.
     * @param samlRequest          The SAML request token.
     * @param signingKeyPair       The {@link KeyPair} to use for generating the message signature.
     * @param certificateChain     The optional certificate chain for the signing keypair, if specified it is included in the message's
     *                             signature KeyInfo and can be used for offline signature validation
     * @param response             The {@link HttpServletResponse} to write the response to.
     * @param relayState           optional relay state
     * @param postTemplateResource The resource that contains the template of the SAML HTTP POST Binding message.
     * @param language             A language hint to make the linkID authentication application use the same locale as the requesting
     *                             application.
     * @param themeName            The name of the theme to apply in linkID.
     * @param loginMode            indiciate to LinkID how the client renders the login process. POPUP and FRAMED indicate a different
     *                             theme should be used, if the application hasn't specified one itself. Furthermore, if FRAMED, send
     *                             the response with target="_top" for jumping out of an iframe.
     *
     * @throws IOException IO Exception
     */
    public static void sendRequest(String consumerUrl, SAMLBinding requestBinding, RequestAbstractType samlRequest, KeyPair signingKeyPair,
                                   CertificateChain certificateChain, HttpServletResponse response, @Nullable String relayState, String postTemplateResource,
                                   @Nullable Locale language, @Nullable String themeName, @Nullable LoginMode loginMode, @Nullable StartPage startPage)
            throws IOException {

        switch (requestBinding) {
            case HTTP_POST:
                PostBindingUtil.sendRequest( samlRequest, signingKeyPair, certificateChain, relayState, postTemplateResource, consumerUrl, response, language,
                        themeName, loginMode, startPage );
                break;

            case HTTP_REDIRECT:
                RedirectBindingUtil.sendRequest( samlRequest, signingKeyPair, relayState, consumerUrl, response, language, themeName, loginMode, startPage );
                break;
        }
    }

    /**
     * The SAML {@link LogoutRequest} that is in the HTTP request<br> {@code null} if there is no SAML message in the HTTP request.
     *
     * @param request       HTTP Servlet Request
     * @param logoutRequest SAML v2.0 Request
     *
     * @return optional embedded certificate chain in the LogoutRequest's signature.
     *
     * @throws ValidationFailedException validation failed for some reason
     */
    @Nullable
    public static CertificateChain validateRequest(HttpServletRequest request, LogoutRequest logoutRequest, Collection<X509Certificate> trustedCertificates)
            throws ValidationFailedException {

        // validate signature
        CertificateChain certificateChain = Saml2Utils.validateSignature( logoutRequest.getSignature(), request, trustedCertificates );

        // validate logout request
        if (null == logoutRequest.getIssuer() || null == logoutRequest.getIssuer().getValue()) {
            throw new ValidationFailedException( "Missing Issuer for the SAML v2.0 Logout Request" );
        }
        if (null == logoutRequest.getNameID() || null == logoutRequest.getNameID().getValue()) {
            throw new ValidationFailedException( "Missing NameID field for the SAML v2.0 Logout Request" );
        }

        return certificateChain;
    }

    /**
     * @param request HTTP Servlet Request
     *
     * @return The SAML {@link AuthnRequest} that is in the HTTP request<br> {@code null} if there is no SAML message in the HTTP
     * request.
     */
    public static AuthnRequest findAuthnRequest(HttpServletRequest request) {

        return BindingUtil.findSAMLObject( request, AuthnRequest.class );
    }

    /**
     * @param request HTTP Servlet Request
     *
     * @return The {@link LogoutRequest} in the HTTP request.<br> {@code null} if there is no SAML message in the HTTP request.
     */
    public static LogoutRequest findLogoutRequest(HttpServletRequest request) {

        return BindingUtil.findSAMLObject( request, LogoutRequest.class );
    }
}
