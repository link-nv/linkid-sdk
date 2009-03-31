/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.sdk.auth.saml2.sessiontracking;

import org.opensaml.common.impl.AbstractSAMLObjectMarshaller;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.MarshallingException;
import org.w3c.dom.Element;


/**
 * <h2>{@link SessionInfoMarshaller}<br>
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
public class SessionInfoMarshaller extends AbstractSAMLObjectMarshaller {

    @Override
    protected void marshallAttributes(XMLObject samlObject, Element domElement)
            throws MarshallingException {

        SessionInfo sessionInfo = (SessionInfo) samlObject;

        if (sessionInfo.getSession() != null) {
            domElement.setAttributeNS(null, SessionInfo.SESSION_ATTRIB_NAME, sessionInfo.getSession());
        }

        super.marshallAttributes(samlObject, domElement);
    }

}
