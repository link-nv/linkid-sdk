package net.link.safeonline.demo.payment.webapp;

import net.link.safeonline.demo.payment.keystore.DemoPaymentKeyStoreUtils;
import net.link.safeonline.demo.payment.webapp.AccountPage.AccountForm;
import net.link.safeonline.model.demo.DemoConstants;
import net.link.safeonline.sdk.exception.AttributeNotFoundException;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.exception.SubjectNotFoundException;
import net.link.safeonline.sdk.ws.data.Attribute;
import net.link.safeonline.sdk.ws.data.DataClient;
import net.link.safeonline.sdk.ws.exception.WSClientTransportException;
import net.link.safeonline.sdk.ws.idmapping.NameIdentifierMappingClient;


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

        private static final long serialVersionUID = 1L;
        private Model<String>     name;
        private Model<Boolean>    junior;
        private boolean           createJunior;

        private CheckBox          juniorField;
        private Button            submitButton;
        private FeedbackPanel     feedbackPanel;
        private TextField<String> nameField;


        public AdminForm(String id) {

            super(id);

            add(nameField = new TextField<String>("name", name = new Model<String>()));
            add(juniorField = new CheckBox("junior", junior = new Model<Boolean>()));
            add(submitButton = new Button("submit", new Model<String>("Search &gt;")));
            add(feedbackPanel = new FeedbackPanel("feedback", IFeedbackMessageFilter.ALL));

            nameField.setRequired(true);
            submitButton.setEscapeModelStrings(false);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void onBeforeRender() {

            super.onBeforeRender();

            juniorField.setVisible(name.getObject() != null);
            if (name.getObject() == null) {
                submitButton.setModelObject("Search &gt;");
                nameField.setEnabled(true);
            } else {
                submitButton.setModelObject("Apply &gt;");
                nameField.setEnabled(false);
            }
            feedbackPanel.setVisible(feedbackPanel.anyMessage());
        }

        @Override
        protected void onSubmit() {

            if (juniorField.isVisible()) {
                try {
                    DataClient dataClient = WicketUtil.getOLASDataService(WicketUtil.toServletRequest(getRequest()),
                            DemoPaymentKeyStoreUtils.getPrivateKeyEntry());
                    NameIdentifierMappingClient nameIdentifierMappingClient = WicketUtil.getOLASIdMappingService(
                            WicketUtil.toServletRequest(getRequest()), DemoPaymentKeyStoreUtils.getPrivateKeyEntry());
                    String userId = nameIdentifierMappingClient.getUserId(name.getObject());

                    if (createJunior) {
                        dataClient.createAttribute(userId, DemoConstants.PAYMENT_JUNIOR_ATTRIBUTE_NAME, junior.getObject());
                    } else {
                        dataClient.setAttributeValue(userId, DemoConstants.PAYMENT_JUNIOR_ATTRIBUTE_NAME, junior.getObject());
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

                // Reset the form.
                name.setObject(null);
                junior.setObject(null);
            }

            else if (name.getObject() != null) {
                try {
                    DataClient dataClient = WicketUtil.getOLASDataService(WicketUtil.toServletRequest(getRequest()),
                            DemoPaymentKeyStoreUtils.getPrivateKeyEntry());
                    NameIdentifierMappingClient nameIdentifierMappingClient = WicketUtil.getOLASIdMappingService(
                            WicketUtil.toServletRequest(getRequest()), DemoPaymentKeyStoreUtils.getPrivateKeyEntry());
                    String userId = nameIdentifierMappingClient.getUserId(name.getObject());

                    Attribute<Boolean> attributeValue = dataClient.getAttributeValue(userId, DemoConstants.PAYMENT_JUNIOR_ATTRIBUTE_NAME,
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
