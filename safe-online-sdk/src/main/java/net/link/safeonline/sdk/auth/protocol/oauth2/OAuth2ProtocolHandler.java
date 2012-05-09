package net.link.safeonline.sdk.auth.protocol.oauth2;

import com.google.common.base.Function;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.link.safeonline.sdk.auth.protocol.*;
import net.link.safeonline.sdk.configuration.AuthenticationContext;
import net.link.safeonline.sdk.configuration.LogoutContext;
import net.link.util.error.ValidationFailedException;


/**
 * TODO description
 * <p/>
 * Date: 09/05/12
 * Time: 14:46
 *
 * @author: sgdesmet
 */
public class OAuth2ProtocolHandler implements ProtocolHandler {

    @Override
    public AuthnProtocolRequestContext sendAuthnRequest(final HttpServletResponse response, final AuthenticationContext context)
            throws IOException {

        //TODO

        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public AuthnProtocolResponseContext findAndValidateAuthnResponse(final HttpServletRequest request)
            throws ValidationFailedException {

        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public AuthnProtocolResponseContext findAndValidateAuthnAssertion(final HttpServletRequest request,
                                                                      final Function<AuthnProtocolResponseContext, AuthenticationContext> responseToContext)
            throws ValidationFailedException {

        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public LogoutProtocolRequestContext sendLogoutRequest(final HttpServletResponse response, final String userId,
                                                          final LogoutContext context)
            throws IOException {

        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public LogoutProtocolResponseContext findAndValidateLogoutResponse(final HttpServletRequest request)
            throws ValidationFailedException {

        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public LogoutProtocolRequestContext findAndValidateLogoutRequest(final HttpServletRequest request,
                                                                     final Function<LogoutProtocolRequestContext, LogoutContext> requestToContext)
            throws ValidationFailedException {

        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public LogoutProtocolResponseContext sendLogoutResponse(final HttpServletResponse response,
                                                            final LogoutProtocolRequestContext logoutRequestContext,
                                                            final boolean partialLogout)
            throws IOException {

        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
