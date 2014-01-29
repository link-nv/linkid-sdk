package net.link.safeonline.sdk.ws.haws;

import com.lyndir.lhunath.opal.system.logging.exception.InternalInconsistencyException;
import java.security.cert.X509Certificate;
import javax.xml.ws.BindingProvider;
import net.lin_k.safe_online.haws.*;
import net.link.safeonline.sdk.api.auth.LoginMode;
import net.link.safeonline.sdk.api.auth.StartPage;
import net.link.safeonline.sdk.api.haws.ErrorCode;
import net.link.safeonline.sdk.api.haws.PushException;
import net.link.safeonline.sdk.api.ws.haws.HawsServiceClient;
import net.link.safeonline.sdk.ws.SDKUtils;
import net.link.safeonline.ws.haws.HawsServiceFactory;
import net.link.util.ws.AbstractWSClient;
import net.link.util.ws.security.username.WSSecurityUsernameTokenCallback;
import net.link.util.ws.security.username.WSSecurityUsernameTokenHandler;
import net.link.util.ws.security.x509.WSSecurityConfiguration;
import net.link.util.ws.security.x509.WSSecurityX509TokenHandler;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Response;


/**
 * Created by wvdhaute
 * Date: 29/01/14
 * Time: 15:47
 */
public class HawsServiceClientImpl extends AbstractWSClient<HawsServicePort> implements HawsServiceClient<AuthnRequest, Response> {

    /**
     * Main constructor.
     *
     * @param location       the location (host:port) of the attribute web service.
     * @param sslCertificate If not {@code null} will verify the server SSL {@link X509Certificate}.
     * @param configuration  WS Security configuration
     */
    public HawsServiceClientImpl(String location, X509Certificate sslCertificate, final WSSecurityConfiguration configuration) {

        this( location, sslCertificate );

        WSSecurityX509TokenHandler.install( getBindingProvider(), configuration );
    }

    /**
     * Main constructor.
     *
     * @param location       the location (host:port) of the ltqr web service.
     * @param sslCertificate If not {@code null} will verify the server SSL {@link X509Certificate}.
     */
    public HawsServiceClientImpl(final String location, final X509Certificate sslCertificate, final WSSecurityUsernameTokenCallback usernameTokenCallback) {

        this( location, sslCertificate );

        WSSecurityUsernameTokenHandler.install( getBindingProvider(), usernameTokenCallback );
    }

    private HawsServiceClientImpl(final String location, final X509Certificate sslCertificate) {

        super( HawsServiceFactory.newInstance().getHawsServicePort(), sslCertificate );
        getBindingProvider().getRequestContext()
                            .put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                                    String.format( "%s/%s", location, SDKUtils.getSDKProperty( "linkid.ws.haws.path" ) ) );
    }

    @Override
    public String push(final AuthnRequest authnRequest, final String language, final String theme, final LoginMode loginMode, final StartPage startPage)
            throws PushException {

        PushRequest request = new PushRequest();

        request.setAny( authnRequest.getDOM() );

        request.setLanguage( language );
        request.setTheme( theme );
        if (null != loginMode)
            request.setLoginMode( loginMode.name() );
        if (null != startPage)
            request.setStartPage( startPage.name() );

        // operate
        PushResponse response = getPort().push( request );

        // convert response
        if (null != response.getError()) {
            throw new PushException( convert( response.getError().getError() ), response.getError().getInfo() );
        }

        if (null != response.getSessionId()) {

            return response.getSessionId();
        }

        throw new InternalInconsistencyException( "No sessionId nor error element in the response ?!" );
    }

    @Override
    public Response pull(final String sessionId) {

        return null;
    }

    private ErrorCode convert(final PushErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_REQUEST_INVALID:
                return ErrorCode.ERROR_REQUEST_INVALID;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }
}
