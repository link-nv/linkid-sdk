/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.user.webapp.pages.applications;

import java.util.Iterator;
import java.util.List;

import javax.ejb.EJB;

import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.authentication.service.SubscriptionService;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.notification.exception.MessageHandlerNotFoundException;
import net.link.safeonline.user.webapp.pages.MainPage;
import net.link.safeonline.user.webapp.template.UserTemplatePage;
import net.link.safeonline.user.webapp.template.NavigationPanel.Panel;
import net.link.safeonline.webapp.components.CustomPagingNavigator;
import net.link.safeonline.webapp.components.ErrorFeedbackPanel;
import net.link.safeonline.wicket.web.RequireLogin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;


@RequireLogin(loginPage = MainPage.class)
public class ApplicationsPage extends UserTemplatePage {

    static final Log           LOG                 = LogFactory.getLog(ApplicationsPage.class);

    private static final long  serialVersionUID    = 1L;

    public static final String SUBSCRIPTIONS_ID    = "subscriptions";
    public static final String VIEW_LINK_ID        = "view";
    public static final String NAME_LABEL_ID       = "name";
    public static final String UNSUBSCRIBE_LINK_ID = "unsubscribe";
    public static final String NAVIGATOR_ID        = "navigator";

    @EJB(mappedName = SubscriptionService.JNDI_BINDING)
    SubscriptionService        subscriptionService;


    public ApplicationsPage() {

        super(Panel.applications);

        getSidebar(localize("helpApplications"), false);

        List<SubscriptionEntity> subscriptions = subscriptionService.listSubscriptions();

        DataView<SubscriptionEntity> subscriptionView = new DataView<SubscriptionEntity>(SUBSCRIPTIONS_ID, new SubscriptionDataProvider(
                subscriptions), 10) {

            private static final long serialVersionUID = 1L;


            @Override
            protected void populateItem(Item<SubscriptionEntity> subscriptionItem) {

                final SubscriptionEntity subscription = subscriptionItem.getModelObject();

                String name = subscription.getApplication().getFriendlyName();
                if (null == name) {
                    name = subscription.getApplication().getName();
                }
                final String applicationName = name;

                Link<String> viewLink = new Link<String>(VIEW_LINK_ID) {

                    private static final long serialVersionUID = 1L;


                    @Override
                    public void onClick() {

                        setResponsePage(new ViewSubscriptionPage(subscription));

                    }

                };
                subscriptionItem.add(viewLink);
                viewLink.add(new Label(NAME_LABEL_ID, name));
                subscriptionItem.add(new Link<String>(UNSUBSCRIBE_LINK_ID) {

                    private static final long serialVersionUID = 1L;


                    @Override
                    public void onClick() {

                        LOG.debug("unsubscribe from : " + applicationName);

                        try {
                            subscriptionService.unsubscribe(subscription.getApplication().getId());
                        } catch (SubscriptionNotFoundException e) {
                            error(localize("errorSubscriptionNotFound"));
                            return;
                        } catch (ApplicationNotFoundException e) {
                            error(localize("errorApplicationNotFound"));
                            return;
                        } catch (PermissionDeniedException e) {
                            error(new StringResourceModel("errorUserMayNotUnsubscribeFrom", this, new Model<String>(),
                                    new Object[] { applicationName }).getObject());
                            return;
                        } catch (MessageHandlerNotFoundException e) {
                            error(localize("errorMessage"));
                            return;
                        }

                        setResponsePage(ApplicationsPage.class);

                    }

                });

            }

        };
        getContent().add(subscriptionView);
        getContent().add(new CustomPagingNavigator(NAVIGATOR_ID, subscriptionView));

        getContent().add(new ErrorFeedbackPanel("feedback"));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPageTitle() {

        return localize("applications");
    }


    class SubscriptionDataProvider implements IDataProvider<SubscriptionEntity> {

        private static final long        serialVersionUID = 1L;

        private List<SubscriptionEntity> subscriptions;


        public SubscriptionDataProvider(List<SubscriptionEntity> subscriptions) {

            this.subscriptions = subscriptions;
        }

        /**
         * {@inheritDoc}
         */
        public Iterator<? extends SubscriptionEntity> iterator(int first, int count) {

            return subscriptions.subList(first, count).iterator();
        }

        /**
         * {@inheritDoc}
         */
        public IModel<SubscriptionEntity> model(SubscriptionEntity object) {

            return new Model<SubscriptionEntity>(object);
        }

        /**
         * {@inheritDoc}
         */
        public int size() {

            return subscriptions.size();
        }

        /**
         * {@inheritDoc}
         */
        public void detach() {

        }

    }

}
