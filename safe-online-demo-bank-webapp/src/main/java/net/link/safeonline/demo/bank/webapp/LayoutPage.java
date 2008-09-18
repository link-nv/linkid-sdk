package net.link.safeonline.demo.bank.webapp;

import javax.ejb.EJB;

import net.link.safeonline.demo.bank.entity.AccountEntity;
import net.link.safeonline.demo.bank.entity.UserEntity;
import net.link.safeonline.demo.bank.service.AccountService;
import net.link.safeonline.demo.bank.service.TransactionService;
import net.link.safeonline.demo.bank.service.UserService;
import net.link.safeonline.demo.wicket.tools.WicketUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;


public class LayoutPage extends WebPage<Object> {

    private static final long    serialVersionUID = 1L;
    Log                          LOG              = LogFactory.getLog(getClass());

    @EJB
    transient UserService        userService;

    @EJB
    transient AccountService     accountService;

    @EJB
    transient TransactionService transactionService;


    /**
     * Add components to the layout that are present on every page.
     * 
     * This includes the title and the global ticket.
     */
    public LayoutPage() {

        add(new Label<String>("pageTitle", "Cinema Demo Application"));

        add(new UserInfo("user"));
    }


    class UserInfo extends WebMarkupContainer<String> {

        private static final long serialVersionUID = 1L;

        {
            setVisible(BankSession.isUserSet());
        }

        public UserInfo(String id) {

            super(id);

            add(new Link<String>("logout") {

                private static final long serialVersionUID = 1L;


                @Override
                public void onClick() {

                    getSession().invalidateNow();

                    setRedirect(true);
                    setResponsePage(LoginPage.class);
                }
            });
            Label<String> name = new Label<String>("name");
            add(name);
            Label<String> amount = new Label<String>("amount");
            add(amount);

            if (BankSession.isUserSet()) {
                double total = 0;
                UserEntity user = BankSession.get().getUser();
                for (AccountEntity account : LayoutPage.this.userService.getAccounts(user)) {
                    total += account.getAmount();
                }

                name.setModel(new Model<String>(user.getName()));
                amount.setModel(new Model<String>(WicketUtil.formatCurrency(getSession(), total)));
            }
        }
    }
}
