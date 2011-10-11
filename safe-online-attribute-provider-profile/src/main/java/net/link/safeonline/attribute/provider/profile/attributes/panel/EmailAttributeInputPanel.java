package net.link.safeonline.attribute.provider.profile.attributes.panel;

import net.link.safeonline.attribute.provider.AttributeCore;
import net.link.safeonline.attribute.provider.input.AttributeInputPanel;
import net.link.safeonline.attribute.provider.profile.attributes.EmailAttribute;
import net.link.safeonline.attribute.provider.service.LinkIDService;
import net.link.util.wicket.component.feedback.ErrorComponentFeedbackLabel;
import net.link.util.wicket.component.input.CustomRequiredTextField;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
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

    static final Log LOG = LogFactory.getLog( EmailAttributeInputPanel.class );

    private static final String EMAILFIELD_ID = "email";
    private static final String VERIFICATION_ID = "verification";
    private static final String FEEDBACK_EMAIL_ID = "feedbackEmail";
    private static final String FEEDBACK_VERIFICATION_ID = "feedbackVerification";
    private static final String VERIFICATION_TEXT_ID = "confirm";

    private final CustomRequiredTextField<String> emailField;
    private final CustomRequiredTextField<String> verificationField;
    private final Label verificationLabel;



    public EmailAttributeInputPanel(String id, final LinkIDService linkIDService, final AttributeCore valueAttribute, final String userId) {

        super( id, valueAttribute );

        //final AttributeCore valueAttribute = (AttributeCore) ((Compound)attribute.getValue()).findMember( EmailAddressAttribute.NAME );
        //final AttributeCore dateAttribute = (AttributeCore) ((Compound)attribute.getValue()).findMember( new EmailExpireDateAttribute( null ).getName() );
        //final AttributeCore confirmedAttribute = (AttributeCore) ((Compound)attribute.getValue()).findMember( new EmailConfirmedAttribute(  null ).getName() );

        //required field email
        emailField = new CustomRequiredTextField<String>( EMAILFIELD_ID, new PropertyModel<String>( valueAttribute, "value" ) );
        emailField.setRequiredMessageKey( "profile.email.errorMissingEmail" );
        emailField.setOutputMarkupPlaceholderTag( true );
        emailField.add( new EmailAddressValidator() {
            @Override
            protected String resourceKey() {

                return "profile.email.errorInvalidEmail";
            }
        });
        emailField.add( new AbstractValidator<String>(){
            @Override
            protected void onValidate(final IValidatable<String> stringIValidatable) {
                if (EmailAttribute.isEmailInUse( linkIDService, stringIValidatable.getValue() ) && !EmailAttribute.isEmailOwner( linkIDService,
                        userId, stringIValidatable.getValue() )){
                    emailField.error( getLocalizedString( "profile.email.errorEmailAlreadyInUse"  ) );
                }

            }
        });
        add( emailField );
        //add Ajax behaviour to toggle verification field if content changes
        emailField.add( new AjaxEventBehavior("onfocus"){

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
        verificationField = new CustomRequiredTextField<String>( VERIFICATION_ID , new Model<String>( (valueAttribute.getValue() == null?"":(String)attribute.getValue())));
        verificationField.setRequiredMessageKey( "profile.email.errorRepeatEmail" );
        verificationField.setOutputMarkupPlaceholderTag( true );
        verificationField.setOutputMarkupId( true );
        //validation: field must match content of emailField
        verificationField.add( new AbstractValidator<String>(){
            @Override
            protected void onValidate(final IValidatable<String> stringIValidatable) {
                  if(!stringIValidatable.getValue().equals( emailField.newValidatable().getValue() )){
                        verificationField.error( getLocalizedString( "profile.email.errorRepeatEmail" ) );
                  }
            }
        });
        add( verificationField );

        verificationLabel = new Label( VERIFICATION_TEXT_ID, getLocalizedParameterModel( "profile.email.pleaseConfirm" ) );
        verificationLabel.setOutputMarkupPlaceholderTag( true );
        add( verificationLabel ) ;

        ErrorComponentFeedbackLabel feedbackEmail = new ErrorComponentFeedbackLabel( FEEDBACK_EMAIL_ID, emailField );
        ErrorComponentFeedbackLabel feedbackVerification = new ErrorComponentFeedbackLabel( FEEDBACK_VERIFICATION_ID, verificationField );
        add( feedbackEmail );
        add( feedbackVerification );

        verificationField.setEnabled( false );
        verificationField.setVisible( false );
        verificationLabel.setEnabled( false );
        verificationLabel.setVisible( false );

    }

    @Override
    public void onMissingAttribute() {
       emailField.error( getLocalizedString( "profile.email.errorMissingEmail" ) );
    }




}
