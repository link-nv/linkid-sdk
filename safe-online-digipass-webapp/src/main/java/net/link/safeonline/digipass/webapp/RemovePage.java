package net.link.safeonline.digipass.webapp;

import java.util.LinkedList;
import java.util.List;

import javax.ejb.EJB;

import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.data.AttributeDO;
import net.link.safeonline.model.digipass.DigipassDeviceService;
import net.link.safeonline.model.digipass.DigipassException;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.ws.exception.WSClientTransportException;
import net.link.safeonline.sdk.ws.idmapping.NameIdentifierMappingClient;
import net.link.safeonline.util.ee.AuthIdentityServiceClient;
import net.link.safeonline.webapp.components.ErrorComponentFeedbackLabel;
import net.link.safeonline.webapp.components.ErrorFeedbackPanel;
import net.link.safeonline.webapp.template.TemplatePage;
import net.link.safeonline.wicket.tools.WicketUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;


public class RemovePage extends TemplatePage {

    private static final long       serialVersionUID   = 1L;

    static final Log                LOG                = LogFactory.getLog(RemovePage.class);

    public static final String      GET_FORM_ID        = "get_form";

    public static final String      LIST_FORM_ID       = "list_form";

    public static final String      LOGIN_FIELD_ID     = "login";

    public static final String      VIEW_BUTTON_ID     = "view";

    public static final String      CANCEL_BUTTON_ID   = "cancel";

    public static final String      DIGIPASSS_LIST_ID  = "digipassList";

    public static final String      REMOVE_LINK_ID     = "remove";

    @EJB(mappedName = DigipassDeviceService.JNDI_BINDING)
    transient DigipassDeviceService digipassDeviceService;

    List<AttributeDO>               digipassAttributes = new LinkedList<AttributeDO>();


    public RemovePage() {

        getHeader();
        getSidebar();
        getContent().add(new GetForm("get_form"));
        getContent().add(new ListForm("list_form"));
    }


    class GetForm extends Form<String> {

        private static final long serialVersionUID = 1L;

        Model<String>             login;


        @SuppressWarnings("unchecked")
        public GetForm(String id) {

            super(id);

            final TextField<String> loginField = new TextField<String>(LOGIN_FIELD_ID, login = new Model<String>());
            loginField.setRequired(true);
            loginField.setEnabled(digipassAttributes.isEmpty());

            add(loginField);
            add(new ErrorComponentFeedbackLabel("login_feedback", loginField));

            add(new Button(VIEW_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public boolean isVisible() {

                    return digipassAttributes.isEmpty();
                }

                @Override
                public void onSubmit() {

                    try {
                        digipassAttributes = digipassDeviceService.getDigipasses(getUserId(), getLocale());
                    } catch (SubjectNotFoundException e) {
                        LOG.debug("subject not found");
                        loginField.error(getLocalizer().getString("errorSubjectNotFound", this));
                        return;
                    } catch (PermissionDeniedException e) {
                        LOG.debug("permission denied: " + e.getMessage());
                        GetForm.this.error(getLocalizer().getString("errorPermissionDenied", this));
                        return;
                    } catch (DeviceNotFoundException e) {
                        LOG.debug("device not found: " + e.getMessage());
                        GetForm.this.error(getLocalizer().getString("errorDeviceNotFound", this));
                        return;
                    }

                    if (digipassAttributes.isEmpty()) {
                        LOG.debug("no digipasses found");
                        GetForm.this.error(getLocalizer().getString("errorNoDeviceRegistrationsFound", this));
                        return;
                    }
                }

            });

            Button cancel = new Button(CANCEL_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public boolean isVisible() {

                    return digipassAttributes.isEmpty();
                }

                @Override
                public void onSubmit() {

                    throw new RestartResponseException(MainPage.class);
                }

            };
            cancel.setDefaultFormProcessing(false);
            add(cancel);

            add(new ErrorFeedbackPanel("feedback_get", new ComponentFeedbackMessageFilter(this)));
        }

        protected String getUserId()
                throws SubjectNotFoundException, PermissionDeniedException {

            AuthIdentityServiceClient authIdentityServiceClient = new AuthIdentityServiceClient();

            NameIdentifierMappingClient idMappingClient = WicketUtil.getOLASIdMappingService(WicketUtil.toServletRequest(getRequest()),
                    authIdentityServiceClient.getPrivateKey(), authIdentityServiceClient.getCertificate());

            String userId;
            try {
                userId = idMappingClient.getUserId(login.getObject());
            } catch (net.link.safeonline.sdk.exception.SubjectNotFoundException e) {
                LOG.error("subject not found: " + login);
                throw new SubjectNotFoundException();
            } catch (RequestDeniedException e) {
                LOG.error("request denied: " + e.getMessage());
                throw new PermissionDeniedException(e.getMessage());
            } catch (WSClientTransportException e) {
                LOG.error("failed to contact web service: " + e.getMessage());
                throw new PermissionDeniedException(e.getMessage());
            }
            return userId;
        }
    }

    class ListForm extends Form<String> {

        private static final long serialVersionUID = 1L;


        @SuppressWarnings("unchecked")
        public ListForm(String id) {

            super(id);

            add(new ListView<AttributeDO>(DIGIPASSS_LIST_ID, new PropertyModel<List<AttributeDO>>(RemovePage.this, "digipassAttributes")) {

                private static final long serialVersionUID = 1L;


                @Override
                protected void populateItem(ListItem<AttributeDO> attributeItem) {

                    final AttributeDO attribute = attributeItem.getModelObject();

                    attributeItem.add(new Label("digipass", attribute.getStringValue()));
                    attributeItem.add(new Link<String>(REMOVE_LINK_ID) {

                        private static final long serialVersionUID = 1L;


                        @Override
                        public void onClick() {

                            LOG.debug("remove digipass: " + attribute.getStringValue());
                            try {
                                digipassDeviceService.remove(attribute.getStringValue());
                            } catch (DigipassException e) {
                                LOG.debug("device not found: " + e.getMessage());
                                ListForm.this.error(getLocalizer().getString("errorDeviceRegistrationNotFound", this));
                                return;
                            } catch (AttributeTypeNotFoundException e) {
                                LOG.debug("device not found: " + e.getMessage());
                                ListForm.this.error(getLocalizer().getString("errorAttributeTypeNotFound", this));
                                return;
                            } catch (AttributeNotFoundException e) {
                                LOG.debug("attribute not found: " + e.getMessage());
                                ListForm.this.error(getLocalizer().getString("errorAttributeNotFound", this));
                                return;
                            }

                            throw new RestartResponseException(MainPage.class);

                        }
                    });

                }
            });

            add(new ErrorFeedbackPanel("feedback_list", new ComponentFeedbackMessageFilter(this)));
        }

        @Override
        public boolean isVisible() {

            return !digipassAttributes.isEmpty();
        }
    }

}
