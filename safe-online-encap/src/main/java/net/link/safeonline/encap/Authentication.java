package net.link.safeonline.encap;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.MobileAuthenticationException;
import net.link.safeonline.authentication.exception.MobileException;


@Local
public interface Authentication {

    /*
     * Accessors.
     */
    String getMobileOTP();

    void setMobileOTP(String mobileOTP);

    String getMobile();

    void setMobile(String mobile);

    String getChallengeId();

    void setChallengeId(String challengeId);

    /*
     * Actions.
     */
    String login() throws MobileAuthenticationException, IOException;

    String requestOTP() throws MalformedURLException, MobileException, AttributeTypeNotFoundException, AttributeNotFoundException;

    String requestNewOTP() throws MalformedURLException, MobileException;

    String cancel() throws IOException;

    String tryAnotherDevice() throws IOException;

    /*
     * Lifecycle.
     */
    void init();

    void destroyCallback();

}
