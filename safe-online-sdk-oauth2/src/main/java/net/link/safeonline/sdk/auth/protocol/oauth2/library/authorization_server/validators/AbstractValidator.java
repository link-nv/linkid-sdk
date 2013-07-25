package net.link.safeonline.sdk.auth.protocol.oauth2.library.authorization_server.validators;

import java.util.Collection;
import net.link.safeonline.sdk.auth.protocol.oauth2.library.OAuth2Message;
import net.link.safeonline.sdk.auth.protocol.oauth2.library.authorization_server.Validator;
import net.link.safeonline.sdk.auth.protocol.oauth2.library.data.objects.ClientAccessRequest;
import net.link.safeonline.sdk.auth.protocol.oauth2.library.data.objects.ClientConfiguration;
import net.link.safeonline.sdk.auth.protocol.oauth2.library.exceptions.OAuthException;
import net.link.safeonline.sdk.auth.protocol.oauth2.library.exceptions.OAuthInvalidMessageException;
import net.link.safeonline.sdk.auth.protocol.oauth2.library.messages.*;


/**
 * <p/>
 * Date: 04/05/12
 * Time: 10:49
 *
 * @author sgdesmet
 */
@SuppressWarnings("UnusedDeclaration")
public abstract class AbstractValidator implements Validator {

    @Override
    public void validate(final AuthorizationRequest request, final ClientConfiguration configuration)
            throws OAuthException {
        // default empty implementation
    }

    @Override
    public void validate(final AccessTokenRequest request, final ClientAccessRequest clientAccessRequest, final ClientConfiguration clientConfiguration)
            throws OAuthException {

    }

    @Override
    public void validate(final ValidationRequest request, final ClientAccessRequest clientAccessRequest, final ClientConfiguration clientConfiguration)
            throws OAuthException {

    }

    protected void requiredClientApplication(final ClientConfiguration configuration, OAuth2Message.ErrorType typeToThrow)
            throws OAuthException {

        if (configuration == null) {
            throw new OAuthException( typeToThrow, "missing or invalid client application configuration" );
        }
    }

    protected void requiredClientAccessRequest(final ClientAccessRequest clientAccessRequest, OAuth2Message.ErrorType typeToThrow)
            throws OAuthException {

        if (clientAccessRequest == null) {
            throw new OAuthException( typeToThrow, "invalid state, missing resource owner authorization" );
        }
    }

    protected void requiredField(Object field, String name)
            throws OAuthException {

        if (field == null || (field instanceof String && "".equals( field.toString() )) || (field instanceof Collection && ((Collection) field).size() == 0))
            throw new OAuthInvalidMessageException( "Missing field: " + name );
    }
}
