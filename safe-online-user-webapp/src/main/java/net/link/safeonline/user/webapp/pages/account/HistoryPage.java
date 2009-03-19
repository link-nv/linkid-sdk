/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.user.webapp.pages.account;

import java.text.DateFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.ejb.EJB;

import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.entity.HistoryEntity;
import net.link.safeonline.history.HistoryMessage;
import net.link.safeonline.history.HistoryMessageManager;
import net.link.safeonline.user.webapp.pages.MainPage;
import net.link.safeonline.user.webapp.template.UserTemplatePage;
import net.link.safeonline.user.webapp.template.NavigationPanel.Panel;
import net.link.safeonline.wicket.tools.WicketUtil;
import net.link.safeonline.wicket.web.RequireLogin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.PageLink;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;


@RequireLogin(loginPage = MainPage.class)
public class HistoryPage extends UserTemplatePage {

    static final Log           LOG                  = LogFactory.getLog(HistoryPage.class);

    private static final long  serialVersionUID     = 1L;

    public static final String PATH                 = "overview";

    public static final String ACCOUNT_SIDE_LINK_ID = "account_side";
    public static final String USAGE_SIDE_LINK_ID   = "usage_side";
    public static final String REMOVE_SIDE_LINK_ID  = "remove_side";

    public static final String HISTORY_ID           = "history";
    public static final String WHEN_ID              = "when";
    public static final String MESSAGE_ID           = "message";
    public static final String NAVIGATOR_ID         = "navigator";

    @EJB(mappedName = IdentityService.JNDI_BINDING)
    IdentityService            identityService;


    public HistoryPage() {

        super(Panel.account);

        getSidebar(localize("helpHistory"), false);

        getSidebar().add(new PageLink<String>(ACCOUNT_SIDE_LINK_ID, AccountPage.class));
        getSidebar().add(new PageLink<String>(USAGE_SIDE_LINK_ID, UsagePage.class));
        getSidebar().add(new PageLink<String>(REMOVE_SIDE_LINK_ID, RemovePage.class));

        /*
         * Add history messages
         */
        DataView<HistoryMessage> historyView = new DataView<HistoryMessage>(HISTORY_ID, new HistoryDataProvider(getHistory()), 10) {

            private static final long serialVersionUID = 1L;


            @Override
            protected void populateItem(Item<HistoryMessage> item) {

                final HistoryMessage message = item.getModelObject();
                item.add(new Label(WHEN_ID, DateFormat.getDateInstance(DateFormat.MEDIUM).format(message.getWhen())));
                item.add(new Label(MESSAGE_ID, message.getMessage()));

            }
        };
        getContent().add(historyView);
        getContent().add(new PagingNavigator(NAVIGATOR_ID, historyView));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPageTitle() {

        return localize("history");
    }

    public List<HistoryMessage> getHistory() {

        List<HistoryEntity> result = identityService.listHistory();
        List<HistoryMessage> messageList = new LinkedList<HistoryMessage>();

        for (HistoryEntity historyEntity : result) {
            String historyMessage = HistoryMessageManager.getMessage(getLocale(), WicketUtil.toServletRequest(getRequest()), historyEntity);
            messageList.add(new HistoryMessage(historyEntity.getWhen(), historyMessage));
        }

        return messageList;
    }


    class HistoryDataProvider implements IDataProvider<HistoryMessage> {

        private static final long    serialVersionUID = 1L;

        private List<HistoryMessage> history;


        public HistoryDataProvider(List<HistoryMessage> history) {

            this.history = history;
        }

        /**
         * {@inheritDoc}
         */
        public Iterator<? extends HistoryMessage> iterator(int first, int count) {

            return history.subList(first, count).iterator();
        }

        /**
         * {@inheritDoc}
         */
        public IModel<HistoryMessage> model(HistoryMessage object) {

            return new Model<HistoryMessage>(object);
        }

        /**
         * {@inheritDoc}
         */
        public int size() {

            return history.size();
        }

        /**
         * {@inheritDoc}
         */
        public void detach() {

        }

    }

}
