/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.webapp.components.toggle;

import java.util.LinkedList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.border.Border;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.Model;


/**
 * <h2>{@link ToggleHeader}<br>
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
public class ToggleHeader extends Border {

    private static final long  serialVersionUID  = 1L;

    public static final String LABEL_ID          = "label";
    public static final String MINIMIZE_LINK_ID  = "minimize";
    public static final String MINIMIZE_LABEL_ID = "minimize_label";

    public static final String MINIMIZE_LABEL    = "<<";
    public static final String MAXIMIZE_LABEL    = ">>";

    boolean                    opened            = true;
    private Model<String>      labelModel        = new Model<String>(MINIMIZE_LABEL);

    final Label                minimizeLabel;

    private List<Component>    targetComponents;


    public ToggleHeader(String id, String label, boolean openedDefault) {

        super(id);
        targetComponents = new LinkedList<Component>();
        opened = openedDefault;
        setLabelModel();

        add(new Label(LABEL_ID, label));

        Link<String> minimize = new AjaxFallbackLink<String>(MINIMIZE_LINK_ID) {

            private static final long serialVersionUID = 1L;


            @Override
            public void onClick(AjaxRequestTarget target) {

                minimize(target);

                opened = !opened;
                setLabelModel();
            }
        };
        add(minimize);

        minimizeLabel = new Label(MINIMIZE_LABEL_ID, labelModel);
        minimizeLabel.setOutputMarkupId(true);
        minimize.add(minimizeLabel);

        targetComponents.add(minimizeLabel);
    }

    void setLabelModel() {

        if (opened) {
            labelModel.setObject(MINIMIZE_LABEL);
        } else {
            labelModel.setObject(MAXIMIZE_LABEL);
        }

    }

    void minimize(AjaxRequestTarget target) {

        for (Component component : targetComponents) {
            target.addComponent(component);
        }
    }

    public boolean isOpened() {

        return opened;
    }

    public void addTargetComponent(Component component) {

        targetComponents.add(component);

        component.setOutputMarkupId(true);
        component.add(new AttributeModifier("style", true, new AbstractReadOnlyModel<Object>() {

            private static final long serialVersionUID = 1L;


            @Override
            public Object getObject() {

                return opened? "": "display:none";
            }
        }));
    }
}
