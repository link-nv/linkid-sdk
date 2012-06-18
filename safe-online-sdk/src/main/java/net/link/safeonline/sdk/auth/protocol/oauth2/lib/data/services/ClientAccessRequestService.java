package net.link.safeonline.sdk.auth.protocol.oauth2.lib.data.services;

import java.util.Date;
import java.util.List;
import net.link.safeonline.sdk.auth.protocol.oauth2.lib.exceptions.ClientAccessRequestNotFoundException;
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


    public String create(ClientConfiguration clientConfiguration, ClientConfiguration.FlowType flowType, List<String> requestedScope, String state, String validatedRedirectionURI);

    /**
     * Get the request with the specified id
     * @param clientAccessId
     * @return
     */
    public ClientAccessRequest findClientAccessRequest(String clientAccessId);

    /**
     * Find the request with the specified request token, access token or authorization code
     * @param token
     * @return
     */
    public ClientAccessRequest findClientAccessRequestByToken(Token token);

    /**
     * Get the request with the specified id
     * @param clientAccessId
     * @return
     */
    public ClientAccessRequest getClientAccessRequest(String clientAccessId) throws ClientAccessRequestNotFoundException;


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
