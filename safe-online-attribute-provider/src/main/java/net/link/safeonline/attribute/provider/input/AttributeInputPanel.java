/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.attribute.provider.input;

import net.link.safeonline.attribute.provider.AttributeCore;
import net.link.util.wicket.component.WicketPanel;
import org.apache.wicket.markup.html.form.FormComponent;


/**
 * Abstract base Attribute panel.
 *
 * Extends this class to provide a custom panel for your attribute types.
 *
 * NOTE: do NOT make the {@link FormComponent}'s in your panel required as the "requredness" is controlled by LinkID. Reason is that for
 * Identity Confirmation the  requiredness can become more complex and can only be determined by LinkID itself.
 */
public abstract class AttributeInputPanel extends WicketPanel {

    protected AttributeCore attribute;

    protected AttributeInputPanel(String id, AttributeCore attribute) {
        super( id );
        this.attribute = attribute;
    }

    /**
     * Callback for optionally visualizing something custom case a value was missing and was required.
     */
    public abstract void onMissingAttribute();
}
