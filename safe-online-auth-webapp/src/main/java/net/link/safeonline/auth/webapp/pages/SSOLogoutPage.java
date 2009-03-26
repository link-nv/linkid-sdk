/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.webapp.pages;

import java.util.List;

import net.link.safeonline.auth.webapp.template.AuthenticationTemplatePage;
import net.link.safeonline.wicket.tools.WicketUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.behavior.AbstractHeaderContributor;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;


public class SSOLogoutPage extends AuthenticationTemplatePage {

    static final Log           LOG              = LogFactory.getLog(SSOLogoutPage.class);

    private static final long  serialVersionUID = 1L;

    public static final String PATH             = "ssologout";

    public static final String LOGOUT_FORM      = "logoutform";


    public SSOLogoutPage() {

        getSidebar(localize("helpSSOLogout"));

        getHeader();

        getContent().add(new LogoutForm(LOGOUT_FORM));

        add(JavascriptPackageResource.getHeaderContribution("ssoLogout.js"));
        add(new AbstractHeaderContributor() {

            private static final long serialVersionUID = 1L;


            @Override
            public IHeaderContributor[] getHeaderContributors() {

                return new IHeaderContributor[] { new IHeaderContributor() {

                    private static final long serialVersionUID = 1L;


                    public void renderHead(IHeaderResponse response) {

                        response.renderOnDomReadyJavascript(String.format("beginLogout"));
                    }
                } };
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPageTitle() {

        return localize("ssoLogout");
    }


    class LogoutForm extends Form<String> {

        private static final long serialVersionUID = 1L;


        public LogoutForm(String id) {

            super(id);
            setMarkupId(id);

            List<String> applicationIds = WicketUtil.getHttpSession().getAttribute("foo");
            add(new ListView<String>("applications", applicationIds) {

                private static final long serialVersionUID = 1L;


                @Override
                protected void populateItem(ListItem<String> item) {

                }
            });
        }
    }
}
