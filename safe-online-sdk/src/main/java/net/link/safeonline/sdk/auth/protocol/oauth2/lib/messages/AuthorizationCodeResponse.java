package net.link.safeonline.sdk.auth.protocol.oauth2.lib.messages;

/**
 * Authorization code response
 * <p/>
 * Date: 20/03/12
 * Time: 14:25
 *
 * @author: sgdesmet
 */
public class AuthorizationCodeResponse implements ResponseMessage {
    
    protected String code;
    protected String state;

    public String getCode() {

        return code;
    }

    public void setCode(final String code) {

        this.code = code;
    }

    public String getState() {

        return state;
    }

    public void setState(final String state) {

        this.state = state;
    }
}
