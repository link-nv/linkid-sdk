package net.link.safeonline.sdk.auth.protocol.oauth2.lib.data.objects;

import java.io.Serializable;
import java.util.Date;
import java.util.List;


/**
 * TODO description
 * <p/>
 * Date: 20/03/12
 * Time: 15:30
 *
 * @author: sgdesmet
 */
public class ClientAccessRequest implements Serializable {

    protected String id;
    protected String userId;
    protected ClientConfiguration          client;
    protected CodeToken authorizationCode;
    protected String state;
    protected boolean granted;
    protected List<String> approvedScope;
    protected String validatedRedirectionURI;
    protected Date userDefinedExpirationDate;
    protected ClientConfiguration.FlowType flowType;
    
    protected List<AccessToken> accessTokens;
    protected List<RefreshToken> refreshTokens;

    public String getId() {

        return id;
    }

    public void setId(final String id) {

        this.id = id;
    }

    public String getUserId() {

        return userId;
    }

    public void setUserId(final String userId) {

        this.userId = userId;
    }

    public ClientConfiguration getClient() {

        return client;
    }

    public void setClient(final ClientConfiguration client) {

        this.client = client;
    }

    public String getState() {

        return state;
    }

    public void setState(final String state) {

        this.state = state;
    }

    public boolean isGranted() {

        return granted;
    }

    public void setGranted(final boolean granted) {

        this.granted = granted;
    }

    public List<String> getApprovedScope() {

        return approvedScope;
    }

    public void setApprovedScope(final List<String> approvedScope) {

        this.approvedScope = approvedScope;
    }

    public String getValidatedRedirectionURI() {

        return validatedRedirectionURI;
    }

    public void setValidatedRedirectionURI(final String validatedRedirectionURI) {

        this.validatedRedirectionURI = validatedRedirectionURI;
    }

    public Date getUserDefinedExpirationDate() {

        return userDefinedExpirationDate;
    }

    public void setUserDefinedExpirationDate(final Date userDefinedExpirationDate) {

        this.userDefinedExpirationDate = userDefinedExpirationDate;
    }

    public ClientConfiguration.FlowType getFlowType() {

        return flowType;
    }

    public void setFlowType(final ClientConfiguration.FlowType flowType) {

        this.flowType = flowType;
    }

    public List<AccessToken> getAccessTokens() {

        return accessTokens;
    }

    public void setAccessTokens(final List<AccessToken> accessTokens) {

        this.accessTokens = accessTokens;
    }

    public List<RefreshToken> getRefreshTokens() {

        return refreshTokens;
    }

    public void setRefreshTokens(final List<RefreshToken> refreshTokens) {

        this.refreshTokens = refreshTokens;
    }

    public CodeToken getAuthorizationCode() {

        return authorizationCode;
    }

    public void setAuthorizationCode(final CodeToken authorizationCode) {

        this.authorizationCode = authorizationCode;
    }

    @Override
    public String toString() {

        return "ClientAccess{" +
               "flowType=" + flowType +
               ", id=" + id +
               ", userId='" + userId + '\'' +
               ", client=" + client +
               ", state='" + state + '\'' +
               ", granted=" + granted +
               ", approvedScope=" + approvedScope +
               ", validatedRedirectionURI='" + validatedRedirectionURI + '\'' +
               ", userDefinedExpirationDate=" + userDefinedExpirationDate +
               '}';
    }
}
