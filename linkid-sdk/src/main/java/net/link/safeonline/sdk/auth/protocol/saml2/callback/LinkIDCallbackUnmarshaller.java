/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.protocol.saml2.callback;

import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.EncryptedAttribute;
import org.opensaml.saml2.core.impl.AttributeStatementUnmarshaller;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.UnmarshallingException;


public class LinkIDCallbackUnmarshaller extends AttributeStatementUnmarshaller {

    @Override
    protected void processChildElement(XMLObject parentObject, XMLObject childObject)
            throws UnmarshallingException {

        LinkIDCallback linkIDCallback = (LinkIDCallback) parentObject;

        if (childObject instanceof Attribute) {
            linkIDCallback.getAttributes().add( (Attribute) childObject );
        } else if (childObject instanceof EncryptedAttribute) {
            linkIDCallback.getEncryptedAttributes().add( (EncryptedAttribute) childObject );
        } else {
            super.processChildElement( parentObject, childObject );
        }
    }
}
