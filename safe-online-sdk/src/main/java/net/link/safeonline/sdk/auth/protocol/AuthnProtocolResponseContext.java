package net.link.safeonline.sdk.auth.protocol;

import com.google.common.base.Preconditions;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.link.safeonline.attribute.provider.AttributeSDK;
import net.link.util.common.CertificateChain;


/**
 * <h2>{@link AuthnProtocolResponseContext}<br> <sub>[in short] (TODO).</sub></h2>
 *
 * <p> <i>08 19, 2010</i> </p>
 *
 * @author lhunath
 */
public class AuthnProtocolResponseContext extends ProtocolResponseContext {

    private final String applicationName;
    private final List<String>                       authenticatedDevices;
    private final Map<String, List<AttributeSDK<?>>> attributes;
    private final String                             userId;
    private final boolean                            success;

    /**
     * @param request              Authentication request this response is a response to.
     * @param id                   Response ID
     * @param applicationName The name of the application this authentication grants the user access to.
     * @param userId               The user that has authenticated himself.
     * @param authenticatedDevices The devices that have authenticated the user.
     * @param attributes           The user's attributes that were sent in this response.
     * @param success              Whether a user has successfully authenticated himself.
     * @param certificateChain     Optional certificate chain if protocol response was signed and contained the chain embedded in the
     *                             signature.
     */
    public AuthnProtocolResponseContext(AuthnProtocolRequestContext request, String id, String userId, String applicationName, List<String> authenticatedDevices,
                                        Map<String, List<AttributeSDK<?>>> attributes, boolean success,
                                        CertificateChain certificateChain) {

        super( request, id, certificateChain );
        this.userId = userId;
        this.applicationName = applicationName;
        this.authenticatedDevices = Collections.unmodifiableList( authenticatedDevices );
        this.attributes = Collections.unmodifiableMap( attributes );
        this.success = success;
    }

    @Override
    public AuthnProtocolRequestContext getRequest() {

        return (AuthnProtocolRequestContext) super.getRequest();
    }

    /**
     * @return The name of the application this authentication grants the user access to.
     */
    public String getApplicationName() {

        return applicationName;
    }

    /**
     * @return The devices that have authenticated the user.
     */
    public List<String> getAuthenticatedDevices() {

        return Preconditions.checkNotNull( authenticatedDevices, "Authenticated Devices not set for %s", this );
    }

    /**
     * @return The user's attributes that were sent in this response.
     */
    public Map<String, List<AttributeSDK<?>>> getAttributes() {

        return Preconditions.checkNotNull( attributes, "Attributes not set for %s", this );
    }

    /**
     * @return The user that has authenticated himself.
     */
    public String getUserId() {

        return Preconditions.checkNotNull( userId, "User Id not set for %s", this );
    }

    /**
     * @return Whether a user has successfully authenticated himself.
     */
    public boolean isSuccess() {

        return success;
    }
}
