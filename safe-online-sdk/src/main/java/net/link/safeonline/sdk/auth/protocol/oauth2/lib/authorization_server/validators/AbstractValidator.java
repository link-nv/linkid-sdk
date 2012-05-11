package net.link.safeonline.sdk.auth.protocol.oauth2.lib.authorization_server.validators;

import java.util.Collection;
import net.link.safeonline.sdk.auth.protocol.oauth2.lib.OAuth2Message;
import net.link.safeonline.sdk.auth.protocol.oauth2.lib.authorization_server.Validator;
import net.link.safeonline.sdk.auth.protocol.oauth2.lib.data.objects.ClientAccess;
import net.link.safeonline.sdk.auth.protocol.oauth2.lib.data.objects.ClientApplication;
import net.link.safeonline.sdk.auth.protocol.oauth2.lib.exceptions.OauthInvalidMessageException;
import net.link.safeonline.sdk.auth.protocol.oauth2.lib.exceptions.OauthValidationException;
import net.link.safeonline.sdk.auth.protocol.oauth2.lib.messages.*;


/**
 * TODO description
 * <p/>
 * Date: 04/05/12
 * Time: 10:49
 *
 * @author: sgdesmet
 */
public abstract class AbstractValidator implements Validator {

    @Override
    public void validate(final AuthorizationRequest request, final ClientApplication application)
            throws OauthValidationException {
        // default empty implementation
    }

    @Override
    public void validate(final AccessTokenRequest request, final ClientAccess clientAccess, final ClientApplication clientApplication)
            throws OauthValidationException {

    }

    @Override
    public void validate(final ValidationRequest request, final ClientAccess clientAccess, final ClientApplication clientApplication)
            throws OauthValidationException {

    }

    protected void requiredClientApplication(final ClientApplication application, OAuth2Message.ErrorType typeToThrow) throws OauthValidationException{
        if (application == null){
            throw new OauthValidationException( typeToThrow, "missing or invalid client application configuration" );
        }
    }

    protected void requiredClientAccessRequest(final ClientAccess clientAccess, OAuth2Message.ErrorType typeToThrow) throws OauthValidationException{
        if (clientAccess == null){
            throw new OauthValidationException( typeToThrow, "invalid state, missing resource owner authorization" );
        }
    }

    protected void requiredField(Object field, String name) throws OauthValidationException{
        if (field == null
            || (field instanceof String && "".equals( field.toString() ))
            || (field instanceof Collection && ((Collection) field).size() == 0 ) )
            throw new OauthInvalidMessageException("Missing field: " + name);
    }
}
