package net.link.safeonline.encap;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.MobileAuthenticationException;
import net.link.safeonline.authentication.exception.MobileException;
import net.link.safeonline.custom.converter.PhoneNumber;


@Local
public interface Authentication {

    public static final String JNDI_BINDING = EncapConstants.JNDI_PREFIX + "AuthenticationBean/local";


    /*
     * Accessors.
     */
    String getMobileOTP();

    void setMobileOTP(String mobileOTP);

    PhoneNumber getMobile();

    void setMobile(PhoneNumber mobile);

    String getChallengeId();

    void setChallengeId(String challengeId);

    /*
     * Actions.
     */
    String login()
            throws MobileAuthenticationException, IOException;

    String requestOTP()
            throws MalformedURLException, MobileException, AttributeTypeNotFoundException, AttributeNotFoundException;

    String requestNewOTP()
            throws MalformedURLException, MobileException;

    String cancel()
            throws IOException;

    String tryAnotherDevice()
            throws IOException;

    /*
     * Lifecycle.
     */
    void init();

    void destroyCallback();

}
