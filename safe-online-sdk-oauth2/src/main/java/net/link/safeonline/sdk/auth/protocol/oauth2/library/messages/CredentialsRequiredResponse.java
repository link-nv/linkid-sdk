package net.link.safeonline.sdk.auth.protocol.oauth2.library.messages;

/**
 * TODO description
 * <p/>
 * Date: 09/05/12
 * Time: 11:01
 *
 * @author: sgdesmet
 */
public class CredentialsRequiredResponse extends ErrorResponse {

    public CredentialsRequiredResponse() {

    }

    public CredentialsRequiredResponse(final ErrorType errorType, final String errorDescription, final String errorUri, final String state) {

        super( errorType, errorDescription, errorUri, state );
    }
}
