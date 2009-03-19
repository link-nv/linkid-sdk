/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.webapp.components.toggle;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.AbstractReadOnlyModel;


/**
 * <h2>{@link ToggleBody}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Mar 18, 2009</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class ToggleBody extends WebMarkupContainer {

    private static final long serialVersionUID = 1L;


    public ToggleBody(String id, final ToggleHeader toggleHeader) {

        super(id);

        add(new AttributeModifier("style", true, new AbstractReadOnlyModel<Object>() {

            private static final long serialVersionUID = 1L;


            @Override
            public Object getObject() {

                return toggleHeader.isOpened()? "": "display:none";
            }
        }));

        setOutputMarkupId(true);

    }
}
