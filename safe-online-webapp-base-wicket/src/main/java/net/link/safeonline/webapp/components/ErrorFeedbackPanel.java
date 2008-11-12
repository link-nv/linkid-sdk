/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.webapp.components;

import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.markup.html.panel.FeedbackPanel;


/**
 * <h2>{@link ErrorFeedbackPanel}<br>
 * <sub>Feedback panel only displaying error messages.</sub></h2>
 * 
 * <p>
 * Feedback panel only displaying error messages.
 * </p>
 * 
 * <p>
 * <i>Nov 6, 2008</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class ErrorFeedbackPanel extends FeedbackPanel {

    private static final long serialVersionUID = 1L;


    public ErrorFeedbackPanel(String id) {

        super(id);
    }

    public ErrorFeedbackPanel(String id, IFeedbackMessageFilter filter) {

        super(id, filter);
    }

    @Override
    public boolean isVisible() {

        if (anyErrorMessage())
            return true;
        return false;
    }
}
