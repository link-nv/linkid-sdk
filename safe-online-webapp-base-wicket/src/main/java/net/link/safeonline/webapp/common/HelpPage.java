/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.webapp.common;

import javax.ejb.EJB;

import net.link.safeonline.helpdesk.HelpdeskLogger;
import net.link.safeonline.model.HelpdeskContact;
import net.link.safeonline.webapp.template.TemplatePage;
import net.link.safeonline.wicket.tools.WicketUtil;

import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseException;
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

        this.returnPage = returnPage;

        getHeader();
        getSidebar("", false).add(new Link<String>("back") {

            private static final long serialVersionUID = 1L;


            @Override
            public void onClick() {

                throw new RestartResponseException(returnPage);

            }

        });

        getContent().add(new CreateContainer(CREATE_FORM_ID));
        getContent().add(new TicketInfoContainer(TICKET_INFO_FORM_ID));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPageTitle() {

        return localize("help");
    }


    class CreateContainer extends WebMarkupContainer {

        private static final long serialVersionUID = 1L;


        public CreateContainer(String id) {

            super(id);

            add(new Link<String>(CREATE_TICKET_LINK_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onClick() {

                    helpdeskId = HelpdeskLogger.persistContext(returnPage.getClassRelativePath(), WicketUtil.getHttpSession());

                }

            });
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isVisible() {

            return helpdeskId.equals(EMPTY_HELPDESK_ID);
        }
    }

    class TicketInfoContainer extends WebMarkupContainer {

        private static final long serialVersionUID = 1L;


        public TicketInfoContainer(String id) {

            super(id);

            add(new Label(IDENTIFIER_LABEL_ID, new PropertyModel<String>(HelpPage.this, "helpdeskId")));
            add(new Label(PHONE_LABEL_ID, contact.getPhone()));
            add(new Label(EMAIL_LABEL_ID, contact.getEmail()));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isVisible() {

            return !helpdeskId.equals(EMPTY_HELPDESK_ID);
        }
    }
}
