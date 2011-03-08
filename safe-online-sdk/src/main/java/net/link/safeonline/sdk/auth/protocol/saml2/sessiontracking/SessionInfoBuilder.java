/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.sdk.auth.protocol.saml2.sessiontracking;

import org.opensaml.common.impl.AbstractSAMLObjectBuilder;
import org.opensaml.common.xml.SAMLConstants;


/**
 * <h2>{@link SessionInfoBuilder}</h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Mar 26, 2009</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class SessionInfoBuilder extends AbstractSAMLObjectBuilder<SessionInfo> {

    /**
     * {@inheritDoc}
     */
    @Override
    public SessionInfo buildObject() {

        return buildObject( SAMLConstants.SAML20P_NS, SessionInfo.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20P_PREFIX );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SessionInfo buildObject(String namespaceURI, String localName, String namespacePrefix) {

        return new SessionInfoImpl( namespaceURI, localName, namespacePrefix );
    }
}
