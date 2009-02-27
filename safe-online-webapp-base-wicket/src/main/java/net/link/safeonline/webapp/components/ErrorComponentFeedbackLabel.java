/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.webapp.components;

import java.io.Serializable;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Component;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.FeedbackMessages;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;


/**
 * <h2>{@link ErrorComponentFeedbackLabel}<br>
 * <sub>Custom feedback label component only displaying error messages.</sub></h2>
 * 
 * <p>
 * Custom feedback label component only displaying error messages. Introduced as the FeedbackPanel is a bit too heavy for most cases.
 * </p>
 * 
 * <p>
 * <i>Nov 6, 2008</i>
 * </p>
 * 
 * @author wvdhaute
 */
@SuppressWarnings("unchecked")
public class ErrorComponentFeedbackLabel extends Label {

    private static final long serialVersionUID = 1L;

    private static final Log  LOG              = LogFactory.getLog(ErrorComponentFeedbackLabel.class);

    /** Field component holds a reference to the {@link Component} this FeedbackLabel belongs to */
    private FormComponent     component;
    /** Field text holds a model of the text to be shown in the FeedbackLabel */
    private IModel            text             = null;


    /**
     * Call this constructor if you just want to display the FeedbackMessage of the component
     * 
     * @param id
     *            The non-null id of this component
     * @param component
     *            The {@link FormComponent} to show the FeedbackMessage for.
     */
    public ErrorComponentFeedbackLabel(String id, FormComponent component, IModel text) {

        this(id, component);
        this.text = text;
    }

    /**
     * Call this constructor if you just want to display the FeedbackMessage of the component
     * 
     * @param id
     *            The non-null id of this component
     * @param component
     *            The {@link FormComponent} to show the FeedbackMessage for.
     */
    public ErrorComponentFeedbackLabel(String id, FormComponent component) {

        super(id);
        this.component = component;

        LOG.debug("formcomponent: " + component.hashCode());
        LOG.debug("this.component: " + this.component.hashCode());
    }

    /**
     * Set the content of this FeedbackLabel, depending on if the component has a FeedbackMessage.
     * 
     * @see Component
     */
    @Override
    protected void onBeforeRender() {

        super.onBeforeRender();

        LOG.debug("onBeforeRender");

        setDefaultModel(null);
        if (component.getFeedbackMessage() != null) {
            LOG.debug("onBeforeRender: feedbackmsg = " + component.getFeedbackMessage());
            if (text != null) {
                setDefaultModel(text);
            } else {
                setDefaultModel(new Model<Serializable>(component.getFeedbackMessage().getMessage()));
            }
        } else {
            setDefaultModel(null);
        }
    }

    @Override
    public boolean isVisible() {

        LOG.debug("isVisible");

        FeedbackMessages msgs = org.apache.wicket.Session.get().getFeedbackMessages();
        Iterator<FeedbackMessage> it = msgs.iterator();
        while (it.hasNext()) {
            FeedbackMessage msg = it.next();
            LOG.debug("feedback message: " + msg.getMessage() + " reporter: " + msg.getReporter().getId() + " level: "
                    + msg.getLevelAsString() + " reporter: " + msg.getReporter().hashCode() + " this.component: " + component.hashCode()
                    + " this.component.id: " + component.getId());
            if (component == msg.getReporter()) {
                LOG.debug("moehoehoehoeeeeeeeeeeee");
            }
        }

        if (component.getFeedbackMessage() != null && component.getFeedbackMessage().getLevel() == FeedbackMessage.ERROR)
            return true;
        LOG.debug("isVisible: false");
        return false;
    }
}
