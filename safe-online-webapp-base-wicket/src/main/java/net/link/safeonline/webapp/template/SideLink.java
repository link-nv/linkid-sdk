/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.webapp.template;

import java.io.Serializable;

import org.apache.wicket.markup.html.link.Link;


/**
 * <h2>{@link SideLink}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Mar 20, 2009</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class SideLink implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Link<?>     link;

    private final String      linkMessage;


    public SideLink(Link<?> link, String linkMessage) {

        this.link = link;
        this.linkMessage = linkMessage;
    }

    public Link<?> getLink() {

        return link;
    }

    public String getLinkMessage() {

        return linkMessage;
    }

}
