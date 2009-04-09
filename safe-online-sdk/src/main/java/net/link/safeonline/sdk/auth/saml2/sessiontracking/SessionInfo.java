/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.sdk.auth.saml2.sessiontracking;

import javax.xml.namespace.QName;

import org.opensaml.common.SAMLObject;
import org.opensaml.common.xml.SAMLConstants;


/**
 * <h2>{@link SessionInfo}<br>
 * <sub>[in short] (TODO).</sub></h2>
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
public interface SessionInfo extends SAMLObject {

    /** Element local name. */
    public static final String DEFAULT_ELEMENT_LOCAL_NAME = "SessionInfo";

    /** Default element name. */
    public static final QName  DEFAULT_ELEMENT_NAME       = new QName(SAMLConstants.SAML20P_NS, DEFAULT_ELEMENT_LOCAL_NAME,
                                                                  SAMLConstants.SAML20P_PREFIX);

    /** Local name of the XSI type. */
    public static final String TYPE_LOCAL_NAME            = "SessionInfoType";

    /** QName of the XSI type. */
    public static final QName  TYPE_NAME                  = new QName(SAMLConstants.SAML20_NS, TYPE_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);

    /** Device attribute name. */
    public static final String SESSION_ATTRIB_NAME        = "Session";


    /**
     * Gets the session info.
     * 
     * @return the value of the Session attribute
     */
    public String getSession();

    /**
     * Sets the session info.
     * 
     * @param session
     *            the new value of the session attribute
     */
    public void setSession(String session);

}
