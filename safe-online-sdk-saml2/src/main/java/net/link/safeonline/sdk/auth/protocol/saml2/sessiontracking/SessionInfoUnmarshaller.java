/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.sdk.auth.protocol.saml2.sessiontracking;

import org.opensaml.common.impl.AbstractSAMLObjectUnmarshaller;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.UnmarshallingException;
import org.w3c.dom.Attr;


/**
 * <h2>{@link SessionInfoUnmarshaller}</h2>
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
public class SessionInfoUnmarshaller extends AbstractSAMLObjectUnmarshaller {

    @Override
    protected void processAttribute(XMLObject samlObject, Attr attribute)
            throws UnmarshallingException {

        SessionInfo sessionInfo = (SessionInfo) samlObject;

        if (attribute.getLocalName().equals( SessionInfo.SESSION_ATTRIB_NAME ))
            sessionInfo.setSession( attribute.getValue() );
        else
            super.processAttribute( samlObject, attribute );
    }
}
