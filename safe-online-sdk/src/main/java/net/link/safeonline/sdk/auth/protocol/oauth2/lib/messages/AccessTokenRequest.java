package net.link.safeonline.sdk.auth.protocol.oauth2.lib.messages;

import java.util.List;


/**
 * TODO description
 * <p/>
 * Date: 22/03/12
 * Time: 16:16
 *
 * @author: sgdesmet
 */
public class AccessTokenRequest implements RequestMessage {

    // universal
    protected GrantType grantType; //required

    // for authorization code based request
    protected String code; //required
    protected String redirectUri; //required if included earlier

    // resource owner credentials flow
    protected String username; //required
    protected String password; //required

    // refresh token request
    protected String refreshToken; //required

    // resource owner credentials flow, client credentials flow and
    // refresh token request
    protected List<String> scope; //optional

    protected String clientId; // required if confidential client
    protected String clientSecret;// required if confidential client

    public GrantType getGrantType() {

        return grantType;
    }

    public void setGrantType(final GrantType grantType) {

        this.grantType = grantType;
    }

    public String getCode() {

        return code;
    }

    public void setCode(final String code) {

        this.code = code;
    }

    public String getRedirectUri() {

        return redirectUri;
    }

    public void setRedirectUri(final String redirectUri) {

        this.redirectUri = redirectUri;
    }

    public String getUsername() {

        return username;
    }

    public void setUsername(final String username) {

        this.username = username;
    }

    public String getPassword() {

        return password;
    }

    public void setPassword(final String password) {

        this.password = password;
    }

    public String getRefreshToken() {

        return refreshToken;
    }

    public void setRefreshToken(final String refreshToken) {

        this.refreshToken = refreshToken;
    }

    public String getClientId() {

        return clientId;
    }

    public void setClientId(final String clientId) {

        this.clientId = clientId;
    }

    public String getClientSecret() {

        return clientSecret;
    }

    public void setClientSecret(final String clientSecret) {

        this.clientSecret = clientSecret;
    }

    public List<String> getScope() {

        return scope;
    }

    public void setScope(final List<String> scope) {

        this.scope = scope;
    }

    @Override
    public String toString() {

        return "AccessTokenRequest{" +
               "grantType=" + grantType +
               ", code='" + code + '\'' +
               ", redirectUri='" + redirectUri + '\'' +
               ", username='" + username + '\'' +
               ", password='" + password + '\'' +
               ", refreshToken='" + refreshToken + '\'' +
               ", scope=" + scope +
               ", clientId='" + clientId + '\'' +
               ", clientSecret='" + clientSecret + '\'' +
               '}';
    }
}
