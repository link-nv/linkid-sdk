package net.link.safeonline.auth.ws.json;

import java.io.Serializable;
import java.util.*;
import net.link.safeonline.auth.ws.soap.AuthenticationStep;


/**
 * <h2>{@link WSAuthentication}<br> <sub>[in short] (TODO).</sub></h2>
 * <p/>
 * <p> <i>12 01, 2010</i> </p>
 *
 * @author lhunath
 */
public interface WSAuthentication extends Serializable {

    String authenticate(String applicationName, String deviceName, Map<String, String> deviceCredentials, Locale language)
            throws AuthenticationOperationFailedException;

    String requestGlobalUsageAgreement(final Locale language)
            throws AuthenticationOperationFailedException;

    void confirmGlobalUsageAgreement()
            throws AuthenticationOperationFailedException;

    String requestApplicationUsageAgreement(Locale language)
            throws AuthenticationOperationFailedException;

    void confirmApplicationUsageAgreement()
            throws AuthenticationOperationFailedException;

    Map<String, List<AttributeType>> requestIdentity(Locale language)
            throws AuthenticationOperationFailedException;

    public void confirmAllIdentity(Map<String, List<String>> attributeValues)
            throws AuthenticationOperationFailedException;

    void confirmIdentity(Set<String> confirmedAttributeNames, Set<String> rejectedAttributeNames, Map<String, List<String>> attributeValues)
            throws AuthenticationOperationFailedException;

    List<AuthenticationStep> getNextSteps();

    byte[] commit()
            throws AuthenticationOperationFailedException;
}
