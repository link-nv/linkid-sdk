package net.link.safeonline.demo.bank.webapp;

import javax.ejb.EJB;
import javax.ejb.EJBException;

import net.link.safeonline.demo.bank.entity.BankAccountEntity;
import net.link.safeonline.demo.bank.entity.BankUserEntity;
import net.link.safeonline.demo.bank.service.AccountService;
import net.link.safeonline.demo.bank.service.TransactionService;
import net.link.safeonline.demo.bank.service.UserService;
import net.link.safeonline.wicket.tools.WicketUtil;
import net.link.safeonline.wicket.web.OlasApplicationPage;
import net.link.safeonline.wicket.web.OlasLogoutLink;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.PageLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;


public abstract class LayoutPage extends OlasApplicationPage {

    private static final long serialVersionUID = 1L;

    @EJB(mappedName = UserService.JNDI_BINDING)
    UserService               userService;

    @EJB(mappedName = AccountService.JNDI_BINDING)
    AccountService            accountService;

    @EJB(mappedName = TransactionService.JNDI_BINDING)
    TransactionService        transactionService;

    private FeedbackPanel     globalFeedback;


    /**
     * Add components to the layout that are present on every page.
     * 
     * This includes the title and the global ticket.
     */
    public LayoutPage() {

        add(new Label("pageTitle", "Bank Demo Application"));
        add(new Label("headerTitle", getHeaderTitle()));
        add(globalFeedback = new FeedbackPanel("globalFeedback"));

        add(new UserInfo("user"));

        // Support linking bank user to olas user.
        if (BankSession.isLinking() && WicketUtil.isOlasAuthenticated()) {
            try {
                BankUserEntity user = BankSession.get().getUser();
                String olasId = WicketUtil.findOlasId();

                BankSession.get().setUser(userService.linkOLASUser(user, olasId));
            }

            catch (EJBException e) {
                error(e.getCause().getMessage());
            }

            finally {
                BankSession.get().setLinkingUser(null);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onBeforeRender() {

        globalFeedback.setVisible(globalFeedback.anyErrorMessage());

        super.onBeforeRender();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onOlasAuthenticated() {

        String olasId = WicketUtil.findOlasId();
        BankUserEntity user = userService.getOLASUser(olasId);

        BankSession.get().setUser(userService.updateUser(user));
    }

    /**
     * @return The string to use as the title for this page.
     */
    protected abstract String getHeaderTitle();


    class UserInfo extends WebMarkupContainer {

        private static final long serialVersionUID = 1L;
        private Model<String>     name;
        private Model<String>     amount;

        {
            setVisible(BankSession.get().isUserSet());
        }


        public UserInfo(String id) {

            super(id);

            // Page link.
            Link<?> pageLink = getPageLink();
            Label pageLinkString = new Label("pageLinkString", getPageLinkString());
            if (pageLinkString.getDefaultModelObject() == null) {
                pageLink.setVisible(false);
            }
            add(pageLink);
            pageLink.add(pageLinkString);

            // User information.
            add(new OlasLogoutLink("logout"));
            add(new Label("name", name = new Model<String>()));
            add(new Label("amount", amount = new Model<String>()));

            if (BankSession.get().isUserSet()) {
                double total = 0;
                BankUserEntity user = BankSession.get().getUser();
                for (BankAccountEntity account : userService.getAccounts(user)) {
                    total += account.getAmount();
                }

                name.setObject(user.getName());
                amount.setObject(WicketUtil.format(BankSession.CURRENCY, total));
            }
        }
    }


    Link<?> getPageLink() {

        return new PageLink<Page>("pageLink", getPageLinkDestination());
    }

    /**
     * @return The string to display on the page link.
     */
    abstract String getPageLinkString();

    /**
     * @return The page that the page-link refers to.
     */
    abstract Class<? extends Page> getPageLinkDestination();
}
