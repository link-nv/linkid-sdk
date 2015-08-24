/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.protocol;

/**
 * <h2>{@link LinkIDAuthnProtocolRequestContext}<br>
 * <sub>[in short].</sub></h2>
 * <p/>
 * <p>
 * <i>08 17, 2010</i>
 * </p>
 *
 * @author lhunath
 */
public class LinkIDAuthnProtocolRequestContext extends LinkIDProtocolRequestContext {

    public LinkIDAuthnProtocolRequestContext(final String id, final String issuer, final LinkIDProtocolHandler linkIDProtocolHandler, final String target) {

        super( id, issuer, linkIDProtocolHandler, target );
    }

}
