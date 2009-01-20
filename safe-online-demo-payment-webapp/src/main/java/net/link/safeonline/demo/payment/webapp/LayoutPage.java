package net.link.safeonline.demo.payment.webapp;

import javax.ejb.EJB;
import javax.servlet.ServletException;

import net.link.safeonline.demo.payment.entity.PaymentUserEntity;
import net.link.safeonline.demo.payment.service.TransactionService;
import net.link.safeonline.demo.payment.service.UserService;


public abstract class LayoutPage extends OlasApplicationPage {

    private static final long            serialVersionUID = 1L;

    @EJB(mappedName = UserService.JNDI_BINDING)
    transient private UserService        userService;

    @EJB(mappedName = TransactionService.JNDI_BINDING)
    transient private TransactionService transactionService;


    /**
     * @return The userService of this {@link LayoutPage}.
     */
    UserService getUserService() {

        return userService;
    }

    /**
     * @return The transactionService of this {@link LayoutPage}.
     */
    TransactionService getTransactionService() {

        return transactionService;
    }

    /**
     * Add components to the layout that are present on every page.
     * 
     * This includes the title and the global ticket.
     */
    public LayoutPage() {

        add(new Label("pageTitle", "Payment Demo Application"));
        add(new Label("headerTitle", getHeaderTitle()));

        add(new UserInfo("user"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onOlasAuthenticated() {

        try {
            String olasId = WicketUtil.getOlasId(getRequest());
            PaymentUserEntity user = getUserService().getUser(olasId);

            PaymentSession.get().setUser(getUserService().updateUser(user, WicketUtil.toServletRequest(getRequest())));
        }

        catch (ServletException e) {
            LOG.error("[BUG]", e);
        }
    }

    /**
     * @return The string to use as the title for this page.
     */
    protected abstract String getHeaderTitle();


    class UserInfo extends WebMarkupContainer {

        private static final long serialVersionUID = 1L;
        private Model<String>     name;

        {
            setVisible(PaymentSession.get().isUserSet());
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

            if (PaymentSession.get().isUserSet()) {
                PaymentUserEntity user = PaymentSession.get().getUser();
                name.setObject(user.getOlasName());
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
    Class<? extends Page> getPageLinkDestination() {

        return getClass();
    }
}
