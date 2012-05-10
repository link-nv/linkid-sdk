package net.link.safeonline.sdk.auth.protocol.oauth2.lib.messages;

import net.link.safeonline.sdk.auth.protocol.oauth2.lib.OAuth2Message;


/**
 * TODO description
 * <p/>
 * Date: 23/03/12
 * Time: 14:41
 *
 * @author: sgdesmet
 */
public class ErrorResponse implements ResponseMessage{

    protected OAuth2Message.ErrorType errorType; //required
    protected String errorDescription; //optional
    protected String errorUri; //optional
    protected String state; //required if included in request

    public ErrorResponse() {

    }

    public ErrorResponse(final OAuth2Message.ErrorType errorType, final String errorDescription, final String errorUri, final String state) {

        this.errorType = errorType;
        this.errorDescription = errorDescription;
        this.errorUri = errorUri;
        this.state = state;
    }

    public OAuth2Message.ErrorType getErrorType() {

        return errorType;
    }

    public void setErrorType(final OAuth2Message.ErrorType errorType) {

        this.errorType = errorType;
    }

    public String getErrorDescription() {

        return errorDescription;
    }

    public void setErrorDescription(final String errorDescription) {

        this.errorDescription = errorDescription;
    }

    public String getErrorUri() {

        return errorUri;
    }

    public void setErrorUri(final String errorUri) {

        this.errorUri = errorUri;
    }

    public String getState() {

        return state;
    }

    public void setState(final String state) {

        this.state = state;
    }

    @Override
    public String toString() {

        return "ErrorResponse{" +
               "errorType=" + errorType +
               ", errorDescription='" + errorDescription + '\'' +
               ", errorUri='" + errorUri + '\'' +
               ", state='" + state + '\'' +
               '}';
    }
}
