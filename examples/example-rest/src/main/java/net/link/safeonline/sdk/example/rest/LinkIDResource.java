package net.link.safeonline.sdk.example.rest;

import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Locale;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import net.link.safeonline.sdk.api.auth.AuthnResponseDO;
import net.link.safeonline.sdk.api.ws.auth.AuthenticationState;
import net.link.safeonline.sdk.api.ws.auth.AuthnEncodedSession;
import net.link.safeonline.sdk.api.ws.auth.AuthnException;
import net.link.safeonline.sdk.api.ws.auth.AuthnSession;
import net.link.safeonline.sdk.api.ws.auth.AuthnSessionState;
import net.link.safeonline.sdk.api.ws.auth.PollException;
import net.link.safeonline.sdk.api.ws.auth.PollResponse;
import net.link.safeonline.sdk.auth.protocol.ws.AuthWSUtils;
import net.link.safeonline.sdk.ws.LinkIDWSUsernameConfiguration;
import net.link.util.logging.Logger;


@Path("linkid")
public class LinkIDResource {

    private static final Logger logger = Logger.get( LinkIDResource.class );

    @GET
    @Path("start")
    @Produces(MediaType.APPLICATION_JSON)
    public Response start(@QueryParam("language") String language) {

        try {
            AuthnSession authnSession = AuthWSUtils.startAuthentication( getLinkIDConfiguration(), null, null, null, null, null, null, getLocale( language ),
                    null, false );
            return Response.ok( new AuthnEncodedSession( authnSession ) ).cacheControl( CacheControlDefaults.NO_STORE ).build();
        }
        catch (AuthnException e) {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).build();
        }
    }

    private Locale getLocale(final String language) {

        return null != language? new Locale( language ): Locale.ENGLISH;
    }

    @GET
    @Path("poll")
    @Produces(MediaType.APPLICATION_JSON)
    public Response poll(@QueryParam("sessionId") String sessionId, @QueryParam("language") String language) {

        try {
            PollResponse<org.opensaml.saml2.core.Response> pollResponse = AuthWSUtils.pollAuthentication( getLinkIDConfiguration(), sessionId, Locale.ENGLISH );

            AuthnSessionState authnSessionState = new AuthnSessionState( pollResponse.getAuthenticationState(), pollResponse.getPaymentState(),
                    pollResponse.getPaymentMenuURL() );
            if (authnSessionState.getAuthenticationState() == AuthenticationState.AUTHENTICATED) {

                if (null != pollResponse.getResponse()) {

                    AuthnResponseDO authnResponse = AuthWSUtils.parse( pollResponse.getResponse() );

                    // the userId, user's data and optional payment response will be available at this point in the AuthnResponseDO object
                    logger.dbg( "linkID authnResponse: " + authnResponse );

                    return Response.ok().entity( authnSessionState ).build();
                }
            }

            return Response.ok( authnSessionState ).cacheControl( CacheControlDefaults.NO_STORE ).build();
        }
        catch (PollException e) {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).build();
        }
    }

    private static X509Certificate sslCertificate;

    static {

        // load SSL certificate
        try {

            CertificateFactory certificateFactory = CertificateFactory.getInstance( "X.509" );
            sslCertificate = (X509Certificate) certificateFactory.generateCertificate(
                    Thread.currentThread().getContextClassLoader().getResourceAsStream( "ssl.pem" ) );
        }
        catch (CertificateException e) {
            logger.err( e, "Failed to load SSL certificate" );
        }
    }

    private LinkIDWSUsernameConfiguration getLinkIDConfiguration() {

        return new LinkIDWSUsernameConfiguration() {
            @Override
            public String getApplicationName() {

                return "example-mobile";
            }

            @Override
            public String getUsername() {

                return "example-mobile";
            }

            @Override
            public String getPassword() {

                return "6E6C1CB7-965C-48A0-B2B0-6B65674BE19F";
            }

            @Override
            public X509Certificate getSSLCertificate() {

                return sslCertificate;
            }

            @Override
            public String getLinkIDBase() {

                return "https://demo.linkid.be";
            }
        };
    }
}
