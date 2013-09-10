package net.link.safeonline.sdk.auth.protocol.oauth2.library.messages;

import java.util.List;


/**
 * <p/>
 * Date: 22/03/12
 * Time: 16:54
 *
 * @author sgdesmet
 */
public class AccessTokenResponse implements ResponseMessage {

    protected String accessToken;

    protected String tokenType;

    protected Long expiresIn = null;

    protected String refreshToken;

    protected List<String> scope;

    public String getAccessToken() {

        return accessToken;
    }

    public void setAccessToken(final String accessToken) {

        this.accessToken = accessToken;
    }

    public String getTokenType() {

        return tokenType;
    }

    public void setTokenType(final String tokenType) {

        this.tokenType = tokenType;
    }

    public Long getExpiresIn() {

        return expiresIn;
    }

    public void setExpiresIn(final Long expiresIn) {

        this.expiresIn = expiresIn;
    }

    public String getRefreshToken() {

        return refreshToken;
    }

    public void setRefreshToken(final String refreshToken) {

        this.refreshToken = refreshToken;
    }

    public List<String> getScope() {

        return scope;
    }

    public void setScope(final List<String> scope) {

        this.scope = scope;
    }
}
