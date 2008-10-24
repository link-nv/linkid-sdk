package net.link.safeonline.demo.bank.webapp;

import javax.ejb.EJB;

import net.link.safeonline.demo.bank.entity.BankAccountEntity;
import net.link.safeonline.demo.bank.entity.BankUserEntity;
import net.link.safeonline.demo.bank.service.AccountService;
import net.link.safeonline.demo.bank.service.TransactionService;
import net.link.safeonline.demo.bank.service.UserService;
import net.link.safeonline.demo.wicket.tools.OlasLogoutLink;
import net.link.safeonline.demo.wicket.tools.WicketUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.PageLink;
import org.apache.wicket.model.Model;


public abstract class LayoutPage extends WebPage {

    private static final long  serialVersionUID = 1L;
    Log                        LOG              = LogFactory.getLog(getClass());

    @EJB
    private UserService        userService;

    @EJB
    private AccountService     accountService;

    @EJB
    private TransactionService transactionService;


    /**
     * @return The userService of this {@link LayoutPage}.
     */
    UserService getUserService() {

        return this.userService;
    }

    /**
     * @return The accountService of this {@link LayoutPage}.
     */
    AccountService getAccountService() {

        return this.accountService;
    }

    /**
     * @return The transactionService of this {@link LayoutPage}.
     */
    TransactionService getTransactionService() {

        return this.transactionService;
    }

    /**
     * Add components to the layout that are present on every page.
     * 
     * This includes the title and the global ticket.
     */
    public LayoutPage() {

        add(new Label("pageTitle", "Bank Demo Application"));
        add(new Label("headerTitle", getHeaderTitle()));

        add(new UserInfo("user"));
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
            setVisible(BankSession.isUserSet());
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
            add(new Label("name", this.name = new Model<String>()));
            add(new Label("amount", this.amount = new Model<String>()));

            if (BankSession.isUserSet()) {
                double total = 0;
                BankUserEntity user = BankSession.get().getUser();
                for (BankAccountEntity account : getUserService().getAccounts(user)) {
                    total += account.getAmount();
                }

                this.name.setObject(user.getName());
                this.amount.setObject(WicketUtil.format(BankSession.CURRENCY, total));
            }
        }
    }


    Link<?> getPageLink() {

        return new PageLink("pageLink", getPageLinkDestination());
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
