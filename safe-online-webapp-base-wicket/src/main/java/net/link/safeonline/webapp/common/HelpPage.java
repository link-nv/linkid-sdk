/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.webapp.common;

import javax.ejb.EJB;

import net.link.safeonline.demo.wicket.tools.WicketUtil;
import net.link.safeonline.helpdesk.HelpdeskLogger;
import net.link.safeonline.model.HelpdeskContact;
import net.link.safeonline.webapp.template.TemplatePage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.PropertyModel;


/**
 * <h2>{@link HelpPage}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Nov 6, 2008</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class HelpPage extends TemplatePage {

    private static final long  serialVersionUID      = 1L;

    static final Log           LOG                   = LogFactory.getLog(HelpPage.class);

    public static final Long   EMPTY_HELPDESK_ID     = new Long(-1);

    public static final String CREATE_FORM_ID        = "create";

    public static final String TICKET_INFO_FORM_ID   = "ticket_info";

    public static final String CREATE_TICKET_LINK_ID = "createTicket";

    public static final String IDENTIFIER_LABEL_ID   = "identifier";

    public static final String PHONE_LABEL_ID        = "phone";

    public static final String EMAIL_LABEL_ID        = "email";

    Page                       returnPage;

    Long                       helpdeskId            = EMPTY_HELPDESK_ID;

    @EJB(mappedName = HelpdeskContact.JNDI_BINDING)
    transient HelpdeskContact  contact;


    public HelpPage(final Page returnPage) {

        super();

        this.returnPage = returnPage;

        addHeader(this);

        getSidebar().add(new Link<String>("back") {

            private static final long serialVersionUID = 1L;


            @Override
            public void onClick() {

                setResponsePage(returnPage);

            }

        });

        getContent().add(new CreateContainer(CREATE_FORM_ID));

        getContent().add(new TicketInfoContainer(TICKET_INFO_FORM_ID));

    }


    class CreateContainer extends WebMarkupContainer {

        private static final long serialVersionUID = 1L;


        public CreateContainer(String id) {

            super(id);

            add(new Link<String>(CREATE_TICKET_LINK_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onClick() {

                    HelpPage.this.helpdeskId = HelpdeskLogger.persistContext(HelpPage.this.returnPage.getClassRelativePath(),
                            WicketUtil.getHttpSession(getRequest()));

                }

            });
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isVisible() {

            return HelpPage.this.helpdeskId.equals(EMPTY_HELPDESK_ID);
        }
    }

    class TicketInfoContainer extends WebMarkupContainer {

        private static final long serialVersionUID = 1L;


        public TicketInfoContainer(String id) {

            super(id);

            add(new Label(IDENTIFIER_LABEL_ID, new PropertyModel<String>(HelpPage.this, "helpdeskId")));

            add(new Label(PHONE_LABEL_ID, HelpPage.this.contact.getPhone()));

            add(new Label(EMAIL_LABEL_ID, HelpPage.this.contact.getEmail()));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isVisible() {

            return !HelpPage.this.helpdeskId.equals(EMPTY_HELPDESK_ID);
        }
    }
}
