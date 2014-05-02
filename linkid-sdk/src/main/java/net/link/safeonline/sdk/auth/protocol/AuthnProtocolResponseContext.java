/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.protocol;

import com.google.common.base.Preconditions;
import java.io.Serializable;
import java.util.*;
import net.link.safeonline.sdk.api.attribute.AttributeSDK;
import net.link.safeonline.sdk.api.payment.PaymentResponseDO;
import net.link.util.common.CertificateChain;
import org.jetbrains.annotations.Nullable;


/**
 * <h2>{@link AuthnProtocolResponseContext}<br> <sub>[in short].</sub></h2>
 * <p/>
 * <p> <i>08 19, 2010</i> </p>
 *
 * @author lhunath
 */
@SuppressWarnings("UnusedDeclaration")
public class AuthnProtocolResponseContext extends ProtocolResponseContext {

    private final String                                        applicationName;
    private final Map<String, List<AttributeSDK<Serializable>>> attributes;
    private final String                                        userId;
    private final boolean                                       success;
    private final PaymentResponseDO                             paymentResponse;

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
    public AuthnProtocolResponseContext(AuthnProtocolRequestContext request, String id, String userId, String applicationName,
                                        Map<String, List<AttributeSDK<Serializable>>> attributes, boolean success, CertificateChain certificateChain,
                                        final PaymentResponseDO paymentResponse) {

        super( request, id, certificateChain );
        this.userId = userId;
        this.applicationName = applicationName;
        this.attributes = Collections.unmodifiableMap( attributes );
        this.success = success;
        this.paymentResponse = paymentResponse;
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
     * @return The user's attributes that were sent in this response.
     */
    public Map<String, List<AttributeSDK<Serializable>>> getAttributes() {

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
    public PaymentResponseDO getPaymentResponse() {

        return paymentResponse;
    }
}
