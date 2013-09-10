/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.sdk.auth.protocol.saml2.sessiontracking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.opensaml.common.impl.AbstractSAMLObject;
import org.opensaml.xml.XMLObject;


/**
 * <h2>{@link SessionInfoImpl}</h2>
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
public class SessionInfoImpl extends AbstractSAMLObject implements SessionInfo {

    private String session;


    protected SessionInfoImpl(String namespaceURI, String elementLocalName, String namespacePrefix) {

        super( namespaceURI, elementLocalName, namespacePrefix );
    }

    /**
     * {@inheritDoc}
     */
    public String getSession() {

        return session;
    }

    /**
     * {@inheritDoc}
     */
    public void setSession(String session) {

        this.session = session;
    }

    /**
     * {@inheritDoc}
     */
    public List<XMLObject> getOrderedChildren() {

        ArrayList<XMLObject> children = new ArrayList<XMLObject>();
        return Collections.unmodifiableList( children );
    }
}
