package net.link.safeonline.auth.ws.json;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * <h2>{@link WSAuthentication}<br> <sub>[in short] (TODO).</sub></h2>
 *
 * <p> <i>12 01, 2010</i> </p>
 *
 * @author lhunath
 */
public interface WSAuthentication extends Serializable {

    AuthenticationResponse authenticate(String applicationName, String deviceName, Map<String, String> deviceCredentials, Locale language);

    AuthenticationResponse requestGlobalUsageAgreement();

    AuthenticationResponse confirmGlobalUsageAgreement(boolean agreed);

    AuthenticationResponse requestApplicationUsageAgreement();

    AuthenticationResponse confirmApplicationUsageAgreement(boolean agreed);

    AuthenticationResponse requestIdentity();

    AuthenticationResponse confirmIdentity(List<String> confirmedAttributeNames, List<String> rejectedAttributeNames,
                                           Map<String, List<String>> attributeValues);
}
