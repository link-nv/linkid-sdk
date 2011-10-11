/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.attribute.provider.confirmation;

import net.link.safeonline.attribute.provider.AttributeCore;
import net.link.util.wicket.component.WicketPanel;
import org.apache.wicket.markup.html.form.FormComponent;


/**
 * Abstract base Attribute panel.
 *
 * Extends this class to provide a custom panel for your attribute types.
 *
 */
public abstract class AttributeConfirmationPanel extends WicketPanel {

    protected AttributeCore attribute;

    protected String confirmationId;

    protected AttributeConfirmationPanel(String id, String confirmationId, AttributeCore attribute) {
        super( id );
        this.attribute = attribute;
        this.confirmationId = confirmationId;
    }

    public AttributeCore getAttribute() {

        return attribute;
    }

    public void setAttribute(final AttributeCore attribute) {

        this.attribute = attribute;
    }
}
