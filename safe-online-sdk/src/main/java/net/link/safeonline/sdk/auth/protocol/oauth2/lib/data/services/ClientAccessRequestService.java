package net.link.safeonline.sdk.auth.protocol.oauth2.lib.data.services;

import java.util.Date;
import java.util.List;
import net.link.safeonline.sdk.auth.protocol.oauth2.lib.exceptions.ClientAccessRequestNotFoundException;
import net.link.safeonline.sdk.auth.protocol.oauth2.lib.messages.AccessTokenRequest;
import net.link.safeonline.sdk.auth.protocol.oauth2.lib.messages.AuthorizationRequest;
import net.link.safeonline.sdk.auth.protocol.oauth2.lib.data.objects.*;


/**
 * Interface to the authorization requests made by clients ({@code ClientAccess}). Implement this (for example using JPA) to provide a store
 * for client authorization requests.
 *
 * <p/>
 * Date: 20/03/12
 * Time: 15:19
 *
 * @author: sgdesmet
 */
public interface ClientAccessRequestService {

    //TODO add/invalidate access token for user/app combo

    //TODO add/invalidate refresh token for user/app combo

    /**
     * Create and store an authorization request. This is typically done at the start of the authorization flow, so the identity of the
     * resource owner/user is probably not yet known.
     *
     * @param authRequest
     * @param validatedRedirectionURI
     * @return
     */
    public String create(AuthorizationRequest authRequest, String validatedRedirectionURI);

    /**
     * Create and store a request. Used for authorization-skipping flows such as client-credentials and resource owner credentials flows
     * @param request
     * @return
     */
    public String create(AccessTokenRequest request);

    /**
     * Get the request with the specified id
     * @param clientAccessId
     * @return
     */
    public ClientAccess findClientAccessRequest(String clientAccessId);

    /**
     * Find the request with the specified request token, access token or authorization code
     * @param token
     * @return
     */
    public ClientAccess findClientAccessRequestByToken(Token token);

    /**
     * Get the request with the specified id
     * @param clientAccessId
     * @return
     */
    public ClientAccess getClientAccessRequest(String clientAccessId) throws ClientAccessRequestNotFoundException;


    /**
     * Sets the resource owner's idenity for the client authorization request.
     * @param clientAccessId
     * @param userId
     * @return
     */
    public void setUser(String clientAccessId, String userId) throws ClientAccessRequestNotFoundException;

    /**
     * Sets the resource owner's authorization (or lack thereof) for the given authorization request.
     *
     * @param clientAccessId
     * @param authorized
     * @param approvedScope
     * @param expireTime
     * @return
     */
    public void setAuthorizationResult(String clientAccessId, boolean authorized, List<String> approvedScope,
                                               Date expireTime) throws ClientAccessRequestNotFoundException;

    /**
     * Update the authorization request with the generated authorization code (in case of the authorization grant flow)
     *
     * @param clientAccessId
     * @param authorizationCode
     * @return
     */
    public void setAuthorizationCode(String clientAccessId, CodeToken authorizationCode) throws ClientAccessRequestNotFoundException;

    public void addToken(String clientAccessId, Token token) throws ClientAccessRequestNotFoundException;

    public void invalidateToken(String clientAccessId, Token token) throws ClientAccessRequestNotFoundException;

}
