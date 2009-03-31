package net.link.safeonline.digipass.webapp;

import java.util.LinkedList;
import java.util.List;

import javax.ejb.EJB;

import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.data.AttributeDO;
import net.link.safeonline.keystore.SafeOnlineNodeKeyStore;
import net.link.safeonline.model.digipass.DigipassDeviceService;
import net.link.safeonline.model.digipass.DigipassException;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.ws.OlasServiceFactory;
import net.link.safeonline.sdk.ws.exception.WSClientTransportException;
import net.link.safeonline.webapp.components.CustomRequiredTextField;
import net.link.safeonline.webapp.components.ErrorComponentFeedbackLabel;
import net.link.safeonline.webapp.components.ErrorFeedbackPanel;
import net.link.safeonline.webapp.template.TemplatePage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;


public class RemovePage extends TemplatePage {

    static final Log           LOG                = LogFactory.getLog(RemovePage.class);

    private static final long  serialVersionUID   = 1L;

    public static final String GET_FORM_ID        = "get_form";

    public static final String LIST_FORM_ID       = "list_form";

    public static final String LOGIN_FIELD_ID     = "login";

    public static final String VIEW_BUTTON_ID     = "view";

    public static final String CANCEL_BUTTON_ID   = "cancel";

    public static final String DIGIPASSS_LIST_ID  = "digipassList";

    public static final String DIGIPASS_LIST_ID   = "digipassList";

    public static final String REMOVE_LINK_ID     = "remove";

    @EJB(mappedName = DigipassDeviceService.JNDI_BINDING)
    DigipassDeviceService      digipassDeviceService;

    List<AttributeDO>          digipassAttributes = new LinkedList<AttributeDO>();


    public RemovePage() {

        getHeader();
        getSidebar(localize("helpRemoveDigipass"));
        getContent().add(new GetForm("get_form"));
        getContent().add(new ListForm("list_form"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPageTitle() {

        return localize("digipassRemove");
    }


    class GetForm extends Form<String> {

        private static final long       serialVersionUID = 1L;

        Model<String>                   login;

        CustomRequiredTextField<String> loginField;


        @SuppressWarnings("unchecked")
        public GetForm(String id) {

            super(id);

            loginField = new CustomRequiredTextField<String>(LOGIN_FIELD_ID, login = new Model<String>());
            loginField.setRequired(true);
            loginField.setRequiredMessageKey("errorMissingLoginName");
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

        /**
         * {@inheritDoc}
         */
        @Override
        protected void onBeforeRender() {

            focus(loginField);

            super.onBeforeRender();
        }

        protected String getUserId()
                throws SubjectNotFoundException, PermissionDeniedException {

            try {
                return OlasServiceFactory.getIdMappingService(SafeOnlineNodeKeyStore.getPrivateKeyEntry()).getUserId(login.getObject());
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
        }
    }

    class ListForm extends Form<String> {

        private static final long serialVersionUID = 1L;
        ListView<AttributeDO>     digiPassList;


        @SuppressWarnings("unchecked")
        public ListForm(String id) {

            super(id);

            add(digiPassList = new ListView<AttributeDO>(DIGIPASS_LIST_ID, new PropertyModel<List<AttributeDO>>(RemovePage.this,
                    "digipassAttributes")) {

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
                            }

                            throw new RestartResponseException(MainPage.class);

                        }
                    });

                }
            });

            add(new ErrorFeedbackPanel("feedback_list", new ComponentFeedbackMessageFilter(this)));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void onBeforeRender() {

            focus(digiPassList);

            super.onBeforeRender();
        }

        @Override
        public boolean isVisible() {

            return !digipassAttributes.isEmpty();
        }
    }

}
