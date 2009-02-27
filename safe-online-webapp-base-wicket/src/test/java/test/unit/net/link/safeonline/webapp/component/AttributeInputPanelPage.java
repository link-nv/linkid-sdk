/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.webapp.component;

import net.link.safeonline.data.AttributeDO;
import net.link.safeonline.webapp.components.AttributeInputPanel;
import net.link.safeonline.webapp.components.ErrorFeedbackPanel;

import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;


public class AttributeInputPanelPage extends WebPage {

    private static final long  serialVersionUID  = 1L;

    public static final String ATTRIBUTE_FORM_ID = "attribute_form";
    public static final String ATTRIBUTE_ID      = "attribute";
    public static final String SUBMIT_BUTTON_ID  = "submit";

    AttributeDO                attribute;


    public AttributeInputPanelPage(AttributeDO attribute) {

        this.attribute = attribute;

        add(new AttributeForm(ATTRIBUTE_FORM_ID));

    }


    class AttributeForm extends Form<String> {

        private static final long serialVersionUID = 1L;


        @SuppressWarnings("unchecked")
        public AttributeForm(String id) {

            super(id);
            setMarkupId(id);

            add(new AttributeInputPanel(ATTRIBUTE_ID, attribute, true));

            add(new Button(SUBMIT_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                }
            });

            add(new ErrorFeedbackPanel("feedback", new ComponentFeedbackMessageFilter(this)));
        }
    }
}
