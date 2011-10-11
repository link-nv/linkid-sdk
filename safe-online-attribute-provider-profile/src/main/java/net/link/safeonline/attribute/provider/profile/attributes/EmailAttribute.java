package net.link.safeonline.attribute.provider.profile.attributes;

import static net.link.safeonline.sdk.configuration.SafeOnlineConfigHolder.*;

import com.lyndir.lhunath.opal.system.logging.exception.InternalInconsistencyException;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import net.link.safeonline.attribute.provider.*;
import net.link.safeonline.attribute.provider.confirmation.AttributeConfirmationPanel;
import net.link.safeonline.attribute.provider.exception.AttributeNotFoundException;
import net.link.safeonline.attribute.provider.exception.AttributePermissionDeniedException;
import net.link.safeonline.attribute.provider.input.AttributeInputPanel;
import net.link.safeonline.attribute.provider.input.DefaultAttributeInputPanel;
import net.link.safeonline.attribute.provider.profile.attributes.panel.EmailAttributeConfirmationPanel;
import net.link.safeonline.attribute.provider.profile.attributes.panel.EmailAttributeInputPanel;
import net.link.safeonline.attribute.provider.service.LinkIDService;
import net.link.util.j2ee.Configurable;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.log.Log4JLogChute;

public class EmailAttribute extends AbstractProfileAttribute {

    public static String NAME = "profile.email";

    private static final String RESOURCE_EMAIL_TEMPLATE = "PasswordConfirmationEmailTemplate.vm";

    @Configurable(name = ProfileAttributeConstants.EMAIL_CONFIRMATION_TIMEOUT_CONFIG, group = ProfileAttributeConstants.EMAIL_CONFIRMATION_TIMEOUT_CONFIG_GROUP)
    private Integer emailTimeout = 60;

    public EmailAttribute(String providerJndi) {

        super( providerJndi, null, DataType.COMPOUNDED );
    }

    public String getName() {

        return NAME;
    }

    @Override
    public AttributeType getAttributeType() {
        //allow multiple email addresses per user, make type multivalued
        AttributeType compoundAttributeType = new AttributeType( getName(), DataType.COMPOUNDED, getProviderJndi(), true, true, true, true, false,
                false );

        compoundAttributeType.getMembers().add( new EmailAddressAttribute( getProviderJndi() ).getAttributeType() );
        compoundAttributeType.getMembers().add( new EmailExpireDateAttribute( getProviderJndi() ).getAttributeType() );
        compoundAttributeType.getMembers().add( new EmailConfirmedAttribute( getProviderJndi() ).getAttributeType() );

        return compoundAttributeType;
    }

     @Override
    public AttributeInputPanel findAttributeInputPanel(final LinkIDService linkIDService, final String id, final String userId,
                                                       final AttributeCore attribute) {
        return new DefaultAttributeInputPanel( id, attribute );
    }

        /**
     * Overridden in case someone modifies the emailaddress attribute directly
     * @param linkIDService
     * @param userId
     * @param attribute
     * @return
     * @throws AttributePermissionDeniedException
     */
    @Override
    public AttributeCore setAttribute(final LinkIDService linkIDService, final String userId, final AttributeCore attribute)
            throws AttributePermissionDeniedException {
        AttributeCore compound = attribute;

        if (((Compound)compound.getValue()).findMember( EmailAddressAttribute.NAME ) == null){
            throw new AttributePermissionDeniedException( "Can't create a compound email attribute without setting an email address" );
        }

        //first, construct the entire compound attribute

        AttributeCore address = getAddressMember( compound );
        AttributeCore expireDate = getExpiredMember( compound );
        AttributeCore confirmed = getConfirmationMember( compound );

        AttributeCore previousAddress = null; //for checking against changed addresses

        if (compound.getId() != null){
            //we have an id, so compound already in the database
            AttributeCore previousCompound = linkIDService.getPersistenceService().findAttribute( userId, compound.getName(), compound.getId() );
            previousAddress = getAddressMember( previousCompound );
            //find other members if necessary, they are invisible, so they might not have been passed along by whoever called us
            if (expireDate == null){
                expireDate = getExpiredMember( previousCompound );
                ((List<AttributeCore>)((Compound)compound.getValue()).getMembers()).add( expireDate );
            }
            if (confirmed == null){
                confirmed = getConfirmationMember( previousCompound );
                ((List<AttributeCore>)((Compound)compound.getValue()).getMembers()).add( confirmed );
            }
        }else { //brand new attribute
            if (expireDate == null){
                expireDate = new AttributeCore( new EmailExpireDateAttribute(null).getAttributeType() );
                ((List<AttributeCore>)((Compound)compound.getValue()).getMembers()).add( expireDate );
            }
            if (confirmed == null){
                confirmed = new AttributeCore(  new EmailConfirmedAttribute(null).getAttributeType() );
                ((List<AttributeCore>)((Compound)compound.getValue()).getMembers()).add( confirmed );
            }
        }

        //if this is a new address, or an old one that has been changed, start the confirmation procedure if the email is not registred yet
        if (previousAddress == null || !previousAddress.getValue().equals( address.getValue() )){
            if (isEmailInUse( linkIDService, (String) address.getValue() )){
                throw new AttributePermissionDeniedException("Email address " + (String)address.getValue() + " is already registered by a different user");
            }else {
                initiateConfirmationProcedure( linkIDService, userId, compound );
            }
            compound = linkIDService.getPersistenceService().setAttribute( userId, compound ); // also updates the members
        }

        return compound;
    }


    private void initiateConfirmationProcedure(LinkIDService linkIDService, String userId, final AttributeCore attribute){
        String email = (String)getAddressMember( attribute ).getValue();

        String confirmationId = UUID.randomUUID().toString();
        getExpiredMember( attribute ).setValue( getEmailConfirmationTimeout( new Date(  ) ) );
        getConfirmationMember( attribute ).setValue( confirmationId );

        // do verification
        String previousUser = linkIDService.getIdentifierService().findSubject( EmailAddressAttribute.NAME, email );
        if (previousUser != null && !previousUser.equals( userId )){
            //somebody used this email but didn't confirm it (in time), too bad, he can't use it anymore
            linkIDService.getIdentifierService().updateSubjectIdentifier( EmailAddressAttribute.NAME, email, userId );
            for (AttributeCore userAttribute : linkIDService.getPersistenceService().listAttributes( previousUser, EmailAttribute.NAME, false )){
                AttributeCore emailAttribute = getAddressMember( userAttribute );
                if ( emailAttribute != null && email.equals( emailAttribute.getValue() )){
                    try {
                        linkIDService.getPersistenceService().removeAttribute( previousUser, emailAttribute.getName(),
                                emailAttribute.getId() ); 
                        break;
                    }
                    catch (AttributeNotFoundException e) {
                        throw new InternalInconsistencyException( e );
                    }
                }
            }
        } else {
            //add new identifier mapping
            linkIDService.getIdentifierService().addSubjectIdentifier( EmailAddressAttribute.NAME, email, userId );
        }

        //send an email for email confirmation
        VelocityContext velocityContext = new VelocityContext();

        velocityContext.put( "link", getConfirmationUrl( userId, confirmationId ) );

        //send the mail
        Properties velocityProperties = new Properties();
        velocityProperties.put( "resource.loader", "class" );
        velocityProperties.put( RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, Log4JLogChute.class.getName() );
        velocityProperties.put( Log4JLogChute.RUNTIME_LOG_LOG4J_LOGGER, EmailAttributeInputPanel.class.getName() );
        velocityProperties.put( "class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader" );

        VelocityEngine velocityEngine;
        try {
            velocityEngine = new VelocityEngine( velocityProperties );
            velocityEngine.init();
        }
        catch (Exception e) {
            throw new RuntimeException( "could not initialize velocity engine", e );
        }

        StringWriter stringWriter = new StringWriter(  );
        try {
            velocityEngine.mergeTemplate( RESOURCE_EMAIL_TEMPLATE, velocityContext, stringWriter );
        }
        catch (Exception e) {
            throw new RuntimeException( "Velocity template error: " + e.getMessage(), e );
        }

        linkIDService.getEmailService().sendEmail( email, linkIDService.getLocalizationService().findText( "webapp.common.profile.email.confirmEmailSubject", getLocale( userId ) ),
                stringWriter.toString() );

    }

    private Locale getLocale(String userId){
        return new Locale( "en" );
    }


    public void confirmAttribute(final LinkIDService linkIDService, final String userId, String confirmationId, final AttributeCore attribute) throws AttributePermissionDeniedException{
        //find user and attribute
        //String userId = linkIDService.getIdentifierService().findSubject( EmailAddressAttribute.NAME, email );
        //if (userId != null){

        String email = (String)getAddressMember( attribute ).getValue();

        String currentOwnderId = linkIDService.getIdentifierService().findSubject( EmailAddressAttribute.NAME, email );
        if ( !userId.equals( currentOwnderId ) ){ //we waited too long, email confirmation has expired and someone else took the email address
            //delete first
             try {
                linkIDService.getPersistenceService().removeAttribute( userId, attribute.getName(),
                        attribute.getId() ); //method also removes the members
            }
            catch (AttributeNotFoundException e) {
                throw new InternalInconsistencyException( e );
            }
            //then notify our caller
            throw new AttributePermissionDeniedException( linkIDService.getLocalizationService().findText( "webapp.common.profile.email.emailExpired", getLocale( userId ) ) );
        }
        //email is still ours, confirm it!
        AttributeCore confirmation = getConfirmationMember( attribute );
        if (confirmation == null){ //see if it is in the database
            AttributeCore previousCompound = linkIDService.getPersistenceService().findAttribute( userId, attribute.getName(), attribute.getId() );
            confirmation = getConfirmationMember( previousCompound );
            ((List<AttributeCore>)((Compound)attribute.getValue()).getMembers()).add( confirmation );
        }
        //need it to build entire compound attribute
        AttributeCore expire = getExpiredMember( attribute );
        if (expire == null){ //see if it is in the database
            AttributeCore previousCompound = linkIDService.getPersistenceService().findAttribute( userId, attribute.getName(), attribute.getId() );
            expire = getExpiredMember( previousCompound );
            ((List<AttributeCore>)((Compound)attribute.getValue()).getMembers()).add( expire );
        }
        if (confirmation != null && confirmation.getValue() != null && confirmation.getValue().equals( confirmationId )){
            confirmation.setValue( "" );
        }

        linkIDService.getPersistenceService().setAttribute( userId, attribute );

    }

    public static boolean isEmailInUse(LinkIDService linkIDService, String email){
        //see if there is a corresponding user for the email, and if so, check if he has confirmed his email address within time
        String userId = linkIDService.getIdentifierService().findSubject( EmailAddressAttribute.NAME, email );
        if (userId != null){
            List<AttributeCore> emails = linkIDService.getPersistenceService().listAttributes( userId, EmailAttribute.NAME, false );
            for (AttributeCore attribute : emails){
                String emailValue = (String)getAddressMember( attribute ).getValue();
                if (emailValue != null && emailValue.equals( email )){
                    boolean confirmed = getConfirmationMember( attribute ).getValue() == null || "".equals( getConfirmationMember( attribute ).getValue() );
                    boolean pending = false;
                    try {
                        pending = new SimpleDateFormat().parse( (String) getExpiredMember( attribute ).getValue() ).after(
                                new Date() );
                    }
                    catch (ParseException e) {
                        throw new RuntimeException( e );
                    }
                    return confirmed || pending;
                }
            }
        }
        return false;
    }

    public static boolean isEmailOwner(LinkIDService linkIDService, String userId, String email){
        return linkIDService.getIdentifierService().findSubject( EmailAddressAttribute.NAME, email ) != null
                 && linkIDService.getIdentifierService().findSubject( EmailAddressAttribute.NAME, email ).equals( userId );
    }

    public AttributeConfirmationPanel getAttributeConfirmationPanel(final LinkIDService linkIDService, final String id, final String userId,
                                               final AttributeCore attribute, final String confirmationId) {

        AttributeCore confirmation = getConfirmationMember( attribute );
        if (confirmation == null){ //see if it is in the database
            AttributeCore previousCompound = linkIDService.getPersistenceService().findCompoundAttributeWhere( userId, EmailAttribute.NAME, EmailConfirmedAttribute.NAME, confirmationId );
            if (previousCompound != null)
                confirmation = getConfirmationMember( previousCompound );
        }
        if (confirmation != null && confirmation.getValue() != null && confirmation.getValue().equals( confirmationId )){
            return new EmailAttributeConfirmationPanel( id, confirmationId, attribute );
        }
        return null;

    }

    private String getEmailConfirmationTimeout(Date startDate) {
        Date date =  new Date( startDate.getTime() + emailTimeout * 1000 * 60);
        return new SimpleDateFormat(  ).format( date );
    }

    private static AttributeCore getAddressMember(AttributeCore attribute){
        if (attribute != null)
            return ((AttributeCore) ((Compound)attribute.getValue()).findMember( EmailAddressAttribute.NAME ));
        else
            return null;
    }

    private static AttributeCore getConfirmationMember(AttributeCore attribute){
        if (attribute != null)
            return ((AttributeCore) ((Compound)attribute.getValue()).findMember( EmailConfirmedAttribute.NAME ));
        else
            return null;
    }

    private static AttributeCore getExpiredMember(AttributeCore attribute){
        if (attribute != null)
            return ((AttributeCore) ((Compound)attribute.getValue()).findMember( EmailExpireDateAttribute.NAME ));
        else
            return null;
    }

    private String getConfirmationUrl(String userId, String confirmationId){
        String url = config().web().userBase() + "/" + ProfileAttributeConstants.ATTRIBUTE_CONFIRMATION_LANDING_PATH + "?" + ProfileAttributeConstants.ATTRIBUTE_CONFIRMATION_PARAMETER_KEY + "=" + confirmationId;
        return url;
    }

}
