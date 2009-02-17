package net.link.safeonline.demo.payment.webapp;

import javax.ejb.EJB;

import net.link.safeonline.demo.payment.entity.PaymentUserEntity;
import net.link.safeonline.demo.payment.service.TransactionService;
import net.link.safeonline.demo.payment.service.UserService;
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

    private static final long    serialVersionUID = 1L;

    @EJB(mappedName = UserService.JNDI_BINDING)
    transient UserService        userService;

    @EJB(mappedName = TransactionService.JNDI_BINDING)
    transient TransactionService transactionService;

    private UserInfo             userForm;

    private FeedbackPanel        globalFeedback;


    /**
     * Add components to the layout that are present on every page.
     * 
     * This includes the title and the global ticket.
     */
    public LayoutPage() {

        add(new Label("pageTitle", "Payment Demo Application"));
        add(new Label("headerTitle", getHeaderTitle()));
        add(globalFeedback = new FeedbackPanel("globalFeedback"));

        add(userForm = new UserInfo("user"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onBeforeRender() {

        userForm.setVisible(PaymentSession.get().isUserSet());

        globalFeedback.setVisible(globalFeedback.anyErrorMessage());

        super.onBeforeRender();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onOlasAuthenticated() {

        String olasId = WicketUtil.findOlasId(getRequest());
        PaymentUserEntity user = userService.getUser(olasId);

        PaymentSession.get().setUser(userService.updateUser(user, WicketUtil.toServletRequest(getRequest())));
    }

    /**
     * @return The string to use as the title for this page.
     */
    protected abstract String getHeaderTitle();


    class UserInfo extends WebMarkupContainer {

        private static final long serialVersionUID = 1L;
        private Model<String>     name;


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
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void onBeforeRender() {

            if (PaymentSession.get().isUserSet()) {
                PaymentUserEntity user = PaymentSession.get().getUser();
                name.setObject(user.getOlasName());
            }

            super.onBeforeRender();
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