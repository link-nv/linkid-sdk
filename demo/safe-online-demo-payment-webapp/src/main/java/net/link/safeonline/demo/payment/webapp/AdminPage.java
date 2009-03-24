package net.link.safeonline.demo.payment.webapp;

import net.link.safeonline.demo.payment.entity.PaymentUserEntity;
import net.link.safeonline.demo.payment.keystore.DemoPaymentKeyStore;
import net.link.safeonline.demo.payment.webapp.AccountPage.AccountForm;
import net.link.safeonline.model.demo.DemoConstants;
import net.link.safeonline.sdk.exception.AttributeNotFoundException;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.exception.SubjectNotFoundException;
import net.link.safeonline.sdk.ws.OlasServiceFactory;
import net.link.safeonline.sdk.ws.data.Attribute;
import net.link.safeonline.sdk.ws.exception.WSClientTransportException;
import net.link.safeonline.wicket.web.ForceLogout;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.RedirectToUrlException;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;


/**
 * <h2>{@link AdminPage}<br>
 * <sub>Wicket backend for servicing a new transaction request from a remote application.</sub></h2>
 * 
 * <p>
 * On this page the details submitted by the remote application for a new transaction are shown.
 * </p>
 * 
 * <p>
 * The user can log in to complete the transaction.
 * </p>
 * 
 * <p>
 * <i>Jun 20, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
@ForceLogout
public class AdminPage extends LayoutPage {

    static final Log          LOG              = LogFactory.getLog(AdminPage.class);

    private static final long serialVersionUID = 1L;


    /**
     * Assign components to the wicket IDs defined in the HTML.
     * 
     * If not logged in, redirects back to the {@link LoginPage}.
     */
    public AdminPage() {

        add(new AdminForm("adminForm"));
    }


    /**
     * <h2>{@link AccountForm}<br>
     * <sub>New Transaction Form.</sub></h2>
     * 
     * <p>
     * This form is used to specify all the details for the new transaction.
     * </p>
     * 
     * <p>
     * <i>Jun 23, 2008</i>
     * </p>
     * 
     * @author mbillemo
     */
    class AdminForm extends Form<String> {

        private static final long           serialVersionUID = 1L;
        Model<String>                       name;
        private Model<Boolean>              junior;
        private boolean                     createJunior;

        private CheckBox                    juniorField;
        private Button                      submitButton;
        private TextField<String>           nameField;
        private ListView<PaymentUserEntity> namesList;


        public AdminForm(String id) {

            super(id);

            add(nameField = new TextField<String>("name", name = new Model<String>()));

            add(namesList = new ListView<PaymentUserEntity>("names", userService.getUsers()) {

                private static final long serialVersionUID = 1L;


                @Override
                public boolean isVisible() {

                    return !getList().isEmpty();
                }

                @Override
                protected void populateItem(final ListItem<PaymentUserEntity> item) {

                    item.add(new Link<String>("select") {

                        private static final long serialVersionUID = 1L;

                        {
                            add(new Label("name", item.getModelObject().getOlasName()));
                        }


                        @Override
                        public void onClick() {

                            name.setObject(item.getModelObject().getOlasName());
                            onSubmit();
                        }
                    });
                }
            });

            add(juniorField = new CheckBox("junior", junior = new Model<Boolean>()));

            add(submitButton = new Button("submit", new Model<String>("Search &gt;")));

            nameField.setRequired(true);
            submitButton.setEscapeModelStrings(false);

            juniorField.setVisible(false);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void onBeforeRender() {

            // Decide what submit text to show.
            if (nameField.isEnabled()) {
                namesList.setVisibilityAllowed(true);

                submitButton.setModelObject("Search &gt;");
                focus(nameField);
            }

            else {
                namesList.setVisibilityAllowed(false);

                if (juniorField.isEnabled()) {
                    submitButton.setModelObject("Apply &gt;");
                    focus(juniorField);
                } else {
                    submitButton.setModelObject("Return &lt;");
                    focus(submitButton);
                }
            }

            super.onBeforeRender();
        }

        @Override
        protected void onSubmit() {

            // Toggle field visibility & enabling depending on whether this is a search or not;
            // and if it is, what result it yields.

            if (nameField.isEnabled()) {
                nameField.setVisible(false);
                juniorField.setVisible(true);

                // Submit was a search query.
                try {
                    String userId = OlasServiceFactory.getIdMappingService(DemoPaymentKeyStore.getPrivateKeyEntry()).getUserId(
                            name.getObject());

                    Attribute<Boolean> attributeValue = OlasServiceFactory.getDataService(DemoPaymentKeyStore.getPrivateKeyEntry())
                                                                          .getAttributeValue(userId,
                                                                                  DemoConstants.PAYMENT_JUNIOR_ATTRIBUTE_NAME,
                                                                                  Boolean.class);
                    if (attributeValue == null) {
                        junior.setObject(false);
                        createJunior = true;
                    } else {
                        junior.setObject(attributeValue.getValue());
                        createJunior = false;
                    }
                }

                catch (WSClientTransportException e) {
                    error("Connection error. Check your SSL setup.");
                } catch (SubjectNotFoundException e) {
                    error("Subject not found");
                } catch (RequestDeniedException e) {
                    error("Request Denied");
                    LOG.error("request denied", e);
                }

                nameField.setEnabled(false);
            }

            else {
                // Submit was an apply/create/return.

                if (juniorField.isVisible()) {
                    try {
                        String userId = OlasServiceFactory.getIdMappingService(DemoPaymentKeyStore.getPrivateKeyEntry()).getUserId(
                                name.getObject());

                        if (createJunior) {
                            OlasServiceFactory.getDataService(DemoPaymentKeyStore.getPrivateKeyEntry()).createAttribute(userId,
                                    DemoConstants.PAYMENT_JUNIOR_ATTRIBUTE_NAME, junior.getObject());
                        } else {
                            OlasServiceFactory.getDataService(DemoPaymentKeyStore.getPrivateKeyEntry()).setAttributeValue(userId,
                                    DemoConstants.PAYMENT_JUNIOR_ATTRIBUTE_NAME, junior.getObject());
                        }
                    }

                    catch (AttributeNotFoundException e) {
                        error("Attribute not found.");
                    } catch (WSClientTransportException e) {
                        error("Connection error. Check your SSL setup.");
                    } catch (SubjectNotFoundException e) {
                        error("Subject not found");
                    } catch (RequestDeniedException e) {
                        error("Request Denied");
                        LOG.error("request denied", e);
                    }
                }

                // Reset the form.
                juniorField.setVisible(false);
                nameField.setEnabled(true);
                namesList.setList(userService.getUsers());
            }
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected String getHeaderTitle() {

        return "New Transaction Request";
    }

    @Override
    Link<?> getPageLink() {

        return new Link<String>("pageLink") {

            private static final long serialVersionUID = 1L;


            @Override
            public void onClick() {

                throw new RedirectToUrlException(PaymentSession.get().getService().getTarget());
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    String getPageLinkString() {

        return "Abort";
    }
}
