package net.link.safeonline.attribute.provider.profile.attributes.panel;

import net.link.safeonline.attribute.provider.AttributeCore;
import net.link.safeonline.attribute.provider.input.AttributeInputPanel;
import net.link.util.wicket.component.feedback.ErrorComponentFeedbackLabel;
import net.link.util.wicket.component.input.CustomRequiredTextField;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.apache.wicket.validation.validator.EmailAddressValidator;


/**
 * Created by IntelliJ IDEA.
 * User: sgdesmet
 * Date: 21/09/11
 * Time: 11:51
 * To change this template use File | Settings | File Templates.
 */
public class EmailAttributeInputPanel extends AttributeInputPanel {

    private static final String EMAILFIELD_ID = "email";
    private static final String VERIFICATION_ID = "verification";
    private static final String FEEDBACK_EMAIL_ID = "feedbackEmail";
    private static final String FEEDBACK_VERIFICATION_ID = "feedbackVerification";
    private static final String VERIFICATION_TEXT_ID = "confirm";

    private final CustomRequiredTextField<String> emailField;
    private final CustomRequiredTextField<String> verificationField;
    private final Label verificationLabel;

    private final AttributeCore attribute;

    public EmailAttributeInputPanel(String id, AttributeCore attribute) {

        super( id, attribute );

        this.attribute = attribute;

        //required field email
        emailField = new CustomRequiredTextField<String>( EMAILFIELD_ID, new PropertyModel<String>( attribute, "value" ) );
        emailField.setRequiredMessageKey( "errorMissingEmail" );
        emailField.add( new EmailAddressValidator() {
            @Override
            protected String resourceKey() {

                return getLocalizedString( "errorInvalidEmail" );
            }
        });
        add( emailField );
        //add Ajax behaviour to toggle verification field if content changes
        emailField.add( new AjaxEventBehavior("onchange"){

            @Override
            protected void onEvent(final AjaxRequestTarget ajaxRequestTarget) {
                verificationField.setEnabled( true );
                verificationField.setVisible( true );
                verificationLabel.setEnabled( true );
                verificationLabel.setVisible( true );
                ajaxRequestTarget.addComponent( verificationField );
                ajaxRequestTarget.addComponent( verificationLabel );
            }
        });

        //field for verification: contents must match that of emailField.
        verificationField = new CustomRequiredTextField<String>( VERIFICATION_ID );
        verificationField.setRequiredMessageKey( "errorRepeatEmail" );
        //validation: field must match content of emailField
        verificationField.add( new AbstractValidator<String>(){
            @Override
            protected void onValidate(final IValidatable<String> stringIValidatable) {
                  if(!stringIValidatable.getValue().equals( emailField.newValidatable().getValue() )){
                        verificationField.error( getLocalizedString( "errorRepeatEmail" ) );
                  }
            }
        });
        add( verificationField );

        verificationLabel = new Label( VERIFICATION_TEXT_ID, new StringResourceModel( "pleaseConfirm", this, null) );
        add( verificationLabel ) ;

        ErrorComponentFeedbackLabel feedbackEmail = new ErrorComponentFeedbackLabel( FEEDBACK_EMAIL_ID, emailField );
        ErrorComponentFeedbackLabel feedbackVerification = new ErrorComponentFeedbackLabel( FEEDBACK_VERIFICATION_ID, verificationField );
        add( feedbackEmail );
        add( feedbackVerification );
    }

    @Override
    protected void onBeforeRender() {
        if (attribute.getValue() == null || attribute.getValue().equals( "" )){
            verificationField.setEnabled( false );
            verificationField.setVisible( false );
            verificationLabel.setEnabled( false );
            verificationLabel.setVisible( false );
        }
        super.onBeforeRender();
    }

    @Override
    public void onMissingAttribute() {
       emailField.error( getLocalizedString( "errorMissingEmail" ) );
    }
}
