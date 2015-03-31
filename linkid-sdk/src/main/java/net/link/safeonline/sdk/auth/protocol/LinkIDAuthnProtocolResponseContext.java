/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.protocol;

import com.google.common.base.Preconditions;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.link.safeonline.sdk.api.attribute.LinkIDAttribute;
import net.link.safeonline.sdk.api.externalcode.LinkIDExternalCodeResponse;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentResponse;
import net.link.util.common.CertificateChain;
import org.jetbrains.annotations.Nullable;


/**
 * <h2>{@link LinkIDAuthnProtocolResponseContext}<br> <sub>[in short].</sub></h2>
 * <p/>
 * <p> <i>08 19, 2010</i> </p>
 *
 * @author lhunath
 */
@SuppressWarnings("UnusedDeclaration")
public class LinkIDAuthnProtocolResponseContext extends LinkIDProtocolResponseContext {

    private final String                                           applicationName;
    private final Map<String, List<LinkIDAttribute<Serializable>>> attributes;
    private final String                                           userId;
    private final boolean                                          success;
    private final LinkIDPaymentResponse                            paymentResponse;
    private final LinkIDExternalCodeResponse                       externalCodeResponse;

    /**
     * @param request          Authentication request this response is a response to.
     * @param id               Response ID
     * @param applicationName  The name of the application this authentication grants the user access to.
     * @param userId           The user that has authenticated himself.
     * @param attributes       The user's attributes that were sent in this response.
     * @param success          Whether a user has successfully authenticated himself.
     * @param certificateChain Optional certificate chain if protocol response was signed and contained the chain embedded in the
     *                         signature.
     */
    public LinkIDAuthnProtocolResponseContext(LinkIDAuthnProtocolRequestContext request, String id, String userId, String applicationName,
                                              Map<String, List<LinkIDAttribute<Serializable>>> attributes, boolean success, CertificateChain certificateChain,
                                              final @Nullable LinkIDPaymentResponse paymentResponse,
                                              final @Nullable LinkIDExternalCodeResponse externalCodeResponse) {

        super( request, id, certificateChain );
        this.userId = userId;
        this.applicationName = applicationName;
        this.attributes = Collections.unmodifiableMap( attributes );
        this.success = success;
        this.paymentResponse = paymentResponse;
        this.externalCodeResponse = externalCodeResponse;
    }

    @Override
    public LinkIDAuthnProtocolRequestContext getRequest() {

        return (LinkIDAuthnProtocolRequestContext) super.getRequest();
    }

    /**
     * @return The name of the application this authentication grants the user access to.
     */
    public String getApplicationName() {

        return applicationName;
    }

    /**
     * @return The user's attributes that were sent in this response.
     */
    public Map<String, List<LinkIDAttribute<Serializable>>> getAttributes() {

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

    /**
     * @return optional payment response in case payment context was part of the authentication request.
     */
    @Nullable
    public LinkIDPaymentResponse getPaymentResponse() {

        return paymentResponse;
    }

    /**
     * @return optional external code response ( LTQR, ... )
     */
    @Nullable
    public LinkIDExternalCodeResponse getExternalCodeResponse() {

        return externalCodeResponse;
    }
}
