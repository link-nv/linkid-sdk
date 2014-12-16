/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.protocol.saml2.callback;

import javax.xml.namespace.QName;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.AttributeStatement;


public interface Callback extends AttributeStatement {

    /**
     * Element local name.
     */
    String DEFAULT_ELEMENT_LOCAL_NAME = "Callback";

    /**
     * Default element name.
     */
    QName DEFAULT_ELEMENT_NAME = new QName( SAMLConstants.SAML20_NS, DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX );

    /**
     * Local name of the XSI type.
     */
    String TYPE_LOCAL_NAME = "CallbackType";

    /**
     * QName of the XSI type.
     */
    QName TYPE_NAME = new QName( SAMLConstants.SAML20_NS, TYPE_LOCAL_NAME, SAMLConstants.SAML20_PREFIX );
}
