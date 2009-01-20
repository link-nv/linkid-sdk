package net.link.safeonline.webapp.template;

import net.link.safeonline.webapp.common.HelpPage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.border.Border;
import org.apache.wicket.markup.html.link.Link;


public class SidebarBorder extends Border {

    private static final long serialVersionUID = 1L;

    Log                       LOG              = LogFactory.getLog(getClass());


    public SidebarBorder(String id, String helpMessage, final boolean showHelpdeskLink) {

        super(id);
        add(new Link<String>("help-link") {

            private static final long serialVersionUID = 1L;


            @Override
            public void onClick() {

                throw new RestartResponseException(new HelpPage(getPage()));
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean isVisible() {

                return showHelpdeskLink;
            }
        });
        add(new Label("help-message", helpMessage));
    }

}
