/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.webapp.pages;

import java.util.List;

import javax.ejb.EJB;

import net.link.safeonline.auth.LoginManager;
import net.link.safeonline.auth.protocol.AuthenticationServiceManager;
import net.link.safeonline.auth.servlet.LoginServlet;
import net.link.safeonline.auth.webapp.template.AuthenticationTemplatePage;
import net.link.safeonline.authentication.ProtocolContext;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.AuthenticationAssertion;
import net.link.safeonline.authentication.service.AuthenticationService;
import net.link.safeonline.helpdesk.HelpdeskLogger;
import net.link.safeonline.service.SubjectService;
import net.link.safeonline.shared.helpdesk.LogLevelType;
import net.link.safeonline.webapp.components.ErrorComponentFeedbackLabel;
import net.link.safeonline.webapp.components.ErrorFeedbackPanel;
import net.link.safeonline.webapp.template.ProgressAuthenticationPanel;
import net.link.safeonline.wicket.tools.WicketUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;


public class SelectUserPage extends AuthenticationTemplatePage {

    static final Log           LOG              = LogFactory.getLog(SelectUserPage.class);

    private static final long  serialVersionUID = 1L;

    public static final String PATH             = "select-user";

    public static final String SELECT_FORM_ID   = "select_form";
    public static final String USER_GROUP_ID    = "userGroup";
    public static final String USERS_ID         = "users";
    public static final String USER_NAME_ID     = "userName";
    public static final String USER_RADIO_ID    = "userRadio";

    public static final String NEXT_BUTTON_ID   = "next";
    public static final String CANCEL_BUTTON_ID = "cancel";

    @EJB(mappedName = SubjectService.JNDI_BINDING)
    SubjectService             subjectService;


    public SelectUserPage() {

        List<AuthenticationAssertion> authenticationAssertions = LoginManager.getAuthenticationAssertions(WicketUtil.getHttpSession());

        getSidebar(localize("helpSelectUser"));

        getHeader();

        getContent().add(new ProgressAuthenticationPanel("progress", ProgressAuthenticationPanel.stage.select));

        getContent().add(new SelectForm(SELECT_FORM_ID, authenticationAssertions));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onBeforeRender() {

        super.onBeforeRender();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPageTitle() {

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(WicketUtil.getHttpSession());
        String title = localize("%l: %s", "authenticatingFor", protocolContext.getApplicationFriendlyName());
        return title;
    }


    class SelectForm extends Form<String> {

        private static final long      serialVersionUID = 1L;

        Model<AuthenticationAssertion> authenticationAssertion;


        @SuppressWarnings("unchecked")
        public SelectForm(String id, List<AuthenticationAssertion> authenticationAssertions) {

            super(id);
            setMarkupId(id);

            final RadioGroup<AuthenticationAssertion> userGroup = new RadioGroup(USER_GROUP_ID,
                    authenticationAssertion = new Model<AuthenticationAssertion>());
            userGroup.setRequired(true);
            add(userGroup);
            add(new ErrorComponentFeedbackLabel("user_feedback", userGroup, new Model<String>(localize("errorUserSelection"))));

            ListView<AuthenticationAssertion> userView = new ListView<AuthenticationAssertion>(USERS_ID, authenticationAssertions) {

                private static final long serialVersionUID = 1L;


                @Override
                protected void populateItem(final ListItem<AuthenticationAssertion> item) {

                    Radio userRadio = new Radio(USER_RADIO_ID, item.getModel());

                    AuthenticationAssertion assertion = item.getModelObject();
                    String username = subjectService.getSubjectLogin(assertion.getSubject().getUserId());
                    userRadio.setLabel(new Model<String>(username));
                    item.add(new SimpleFormComponentLabel(USER_NAME_ID, userRadio));
                    item.add(userRadio);
                }

            };
            userGroup.add(userView);

            add(new Button(NEXT_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    String username = subjectService.getSubjectLogin(authenticationAssertion.getObject().getSubject().getUserId());
                    LOG.debug("next: " + username);

                    HelpdeskLogger.add("selected user: " + username, LogLevelType.INFO);

                    AuthenticationService authenticationService = AuthenticationServiceManager
                                                                                              .getAuthenticationService(WicketUtil
                                                                                                                                  .getHttpSession());
                    try {
                        authenticationService.selectUser(authenticationAssertion.getObject().getSubject());
                    } catch (SubjectNotFoundException e) {
                        LOG.error("Subject not found", e);
                        SelectForm.this.error("errorSubjectNotFound");
                        return;
                    }

                    LoginManager.login(WicketUtil.getHttpSession(), authenticationAssertion.getObject());

                    getResponse().redirect(LoginServlet.SERVLET_PATH);
                    setRedirect(false);

                }
            });

            add(new Button(CANCEL_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    LOG.debug("cancel");

                    setResponsePage(MainPage.class);
                }

            });

            add(new ErrorFeedbackPanel("feedback", new ComponentFeedbackMessageFilter(this)));
        }
    }

}
