package net.link.safeonline.sdk.auth.protocol.oauth2;

import static net.link.safeonline.sdk.configuration.SDKConfigHolder.config;

import com.lyndir.lhunath.opal.system.logging.exception.InternalInconsistencyException;
import java.io.IOException;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import net.link.safeonline.sdk.auth.protocol.oauth2.lib.messages.*;
import net.link.safeonline.sdk.configuration.ConfigUtils;
import net.link.safeonline.sdk.configuration.SafeOnlineConfigHolder;
import net.link.safeonline.sdk.ws.LinkIDServiceFactory;
import net.link.util.config.KeyProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import net.link.util.error.ValidationFailedException;

/**
 * TODO description
 * <p/>
 * Date: 16/05/12
 * Time: 11:46
 *
 * @author: sgdesmet
 */
public class OAuth2TokenValidationHandler {

    public static final int EXPIRATION_SLACK_SECONDS = 60; //in seconds

    private static final Log LOG = LogFactory.getLog( OAuth2TokenValidationHandler.class );

    private static Map<String, OAuth2TokenValidationHandler> handlers;

    protected ConcurrentHashMap<String,CacheEntry> cache;

    protected X509Certificate sslCertificate;

    protected boolean alwaysRefresh;

    public static synchronized OAuth2TokenValidationHandler getInstance(String name){

        if (handlers == null)
            handlers = Collections.<String, OAuth2TokenValidationHandler>synchronizedMap(new HashMap<String, OAuth2TokenValidationHandler>(  ));
        if (!handlers.containsKey( name )){
            OAuth2TokenValidationHandler handler = new OAuth2TokenValidationHandler();
            KeyProvider keyProvider = SafeOnlineConfigHolder.config().linkID().app().keyProvider();
            handler.sslCertificate = keyProvider.getTrustedCertificate( LinkIDServiceFactory.SSL_ALIAS );
            handler.alwaysRefresh = false;
            handlers.put( name, handler );
        }

        return handlers.get( name );
    }

    protected OAuth2TokenValidationHandler(){
        cache = new ConcurrentHashMap<String, CacheEntry>(  );
    }

    /**
     * Validate an access token for given user id and application
     *
     * @param accessToken
     * @param expectedUserId
     * @param expectedAudience expected application the token is for
     * @param forceRefresh don't look at the cache, but contact linkid for validation
     * @return
     */
    public boolean validateAccessToken(String accessToken, String expectedUserId, String expectedAudience, boolean forceRefresh){

        if (alwaysRefresh || forceRefresh || !cache.containsKey( accessToken )){
            boolean validToken = true;
            Date expirationDate = new Date(  );

            ResponseMessage validationResponse = null;
            try {
                validationResponse = getValidationMessage( accessToken );
            }
            catch (IOException e) {
                throw new InternalInconsistencyException("While trying to get token validation", e);
            }
            catch (NoSuchAlgorithmException e) {
                throw new InternalInconsistencyException("While trying to get token validation", e);
            }
            catch (KeyManagementException e) {
                throw new InternalInconsistencyException("While trying to get token validation", e);
            }
            catch (KeyStoreException e) {
                throw new InternalInconsistencyException("While trying to get token validation", e);
            }

            if (validationResponse instanceof ErrorResponse) {
                LOG.error( "Received error response for OAuth authorization request: " + validationResponse.toString() );
                validToken = false;
            } else {
                Long expiresIn = ((ValidationResponse)validationResponse).getExpiresIn();
                if ( null == expiresIn || expiresIn <= EXPIRATION_SLACK_SECONDS){
                    LOG.error( "Token expired: " + validationResponse.toString() );
                    validToken = false;
                } else {
                    // add some expiration slack to account for transmitting the token and processing it
                    expirationDate = new Date( System.currentTimeMillis() + expiresIn * 1000 - EXPIRATION_SLACK_SECONDS * 1000 );
                }
            }
            CacheEntry cacheEntry = new CacheEntry();
            cacheEntry.valid = validToken;
            cacheEntry.expirationDate = expirationDate;
            cacheEntry.audience = validToken ? ((ValidationResponse) validationResponse).getAudience() : "";
            cacheEntry.userId = validToken ? ((ValidationResponse) validationResponse).getUserId() : "";
            cacheEntry.accessToken = accessToken;
            cache.put( accessToken, cacheEntry );
        }

        CacheEntry entry = cache.get( accessToken );
        return entry.valid && entry.expirationDate.after( new Date() )
               && (expectedUserId == null || expectedUserId.equals( entry.userId ))
               && (expectedAudience == null || expectedAudience.equals( entry.audience ));
    }

    /**
     * Invalidate an access token in the cache
     * @param accessToken
     */
    public void invalidateAccessToken(String accessToken){
        CacheEntry cacheEntry = new CacheEntry();
        cacheEntry.valid = false;
        cacheEntry.expirationDate = new Date(  );
        cacheEntry.accessToken = accessToken;
        cache.put( accessToken, cacheEntry );
    }

    public void clear(){
        cache.clear();
    }

    public String getUserId(String accessToken) throws ValidationFailedException{
        if (validateAccessToken( accessToken, null, null, false ) ){
            return cache.get( accessToken ).userId;
        }
        throw new ValidationFailedException( "invalid access token" );
    }

    public String getAudience(String accessToken) throws ValidationFailedException{
        if (validateAccessToken( accessToken, null, null, false ) ){
            return cache.get( accessToken ).audience;
        }
        throw new ValidationFailedException( "invalid access token" );
    }

    protected ResponseMessage getValidationMessage(String accessToken)
            throws IOException, NoSuchAlgorithmException, KeyManagementException, KeyStoreException {

        ValidationRequest validationRequest = new ValidationRequest();
        validationRequest.setAccessToken( accessToken );
        String endpoint = ConfigUtils.getLinkIDAuthURLFromPath( config().proto().oauth2().validationPath() );

        ResponseMessage validationResponse = MessageUtils.sendRequestMessage( endpoint, validationRequest,
                sslCertificate, null, null );

        return validationResponse;
    }

    public X509Certificate getSslCertificate() {

        return sslCertificate;
    }

    /**
     * The SSL certificate to validate when connecting to LinkID
     * @param sslCertificate
     */
    public void setSslCertificate(final X509Certificate sslCertificate) {

        this.sslCertificate = sslCertificate;
    }

    /**
     * If true, handler will always contact linkid to verify the access token
     * @return
     */
    public boolean isAlwaysRefresh() {

        return alwaysRefresh;
    }

    /**
     * Disable cache, always perform requests to LinkID to verify the token
     * @param alwaysRefresh
     */
    public void setAlwaysRefresh(final boolean alwaysRefresh) {

        this.alwaysRefresh = alwaysRefresh;
    }

    protected static class CacheEntry{
        String accessToken;
        String audience;
        String userId;
        Date expirationDate;
        boolean valid;
    }

}
