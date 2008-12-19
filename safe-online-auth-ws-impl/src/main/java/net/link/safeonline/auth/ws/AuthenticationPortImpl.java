/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.ws;

import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.ejb.EJBException;
import javax.jws.WebService;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.soap.Addressing;

import net.lin_k.safe_online.auth.AuthenticationPort;
import net.lin_k.safe_online.auth.DeviceCredentialsType;
import net.lin_k.safe_online.auth.WSAuthenticationRequestType;
import net.lin_k.safe_online.auth.WSAuthenticationResponseType;
import net.lin_k.safe_online.auth.WSAuthenticationStepType;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.saml.common.Saml2SubjectConfirmationMethod;
import net.link.safeonline.util.ee.EjbUtils;
import net.link.safeonline.ws.util.ri.Injection;
import oasis.names.tc.saml._2_0.assertion.AssertionType;
import oasis.names.tc.saml._2_0.assertion.AudienceRestrictionType;
import oasis.names.tc.saml._2_0.assertion.AuthnContextType;
import oasis.names.tc.saml._2_0.assertion.AuthnStatementType;
import oasis.names.tc.saml._2_0.assertion.ConditionsType;
import oasis.names.tc.saml._2_0.assertion.NameIDType;
import oasis.names.tc.saml._2_0.assertion.ObjectFactory;
import oasis.names.tc.saml._2_0.assertion.SubjectConfirmationDataType;
import oasis.names.tc.saml._2_0.assertion.SubjectConfirmationType;
import oasis.names.tc.saml._2_0.assertion.SubjectType;
import oasis.names.tc.saml._2_0.protocol.StatusCodeType;
import oasis.names.tc.saml._2_0.protocol.StatusType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.impl.SecureRandomIdentifierGenerator;
import org.opensaml.saml2.core.StatusCode;
import org.w3._2000._09.xmldsig_.KeyInfoType;

import com.sun.xml.ws.api.server.InstanceResolver;
import com.sun.xml.ws.developer.Stateful;
import com.sun.xml.ws.developer.StatefulWebServiceManager;


/**
 * Implementation of OLAS Stateful Authentication web service using JAX-WS.
 * 
 * 
 * Do NOT use {@link Injection} as this is a {@link Stateful} web service and the statefulness is achieved by JAX-WS using the same
 * {@link InstanceResolver} as is used by the {@link Injection}.
 * 
 * @author wvdhaute
 * 
 */

@Stateful
@Addressing
@WebService(endpointInterface = "net.lin_k.safe_online.auth.AuthenticationPort")
// @HandlerChain(file = "auth-ws-handlers.xml")
public class AuthenticationPortImpl implements AuthenticationPort {

    private static final Log                                    LOG           = LogFactory.getLog(AuthenticationPortImpl.class);

    public static StatefulWebServiceManager<AuthenticationPort> manager;

    private DatatypeFactory                                     datatypeFactory;

    private String                                              userId;

    private boolean                                             authenticated = false;


    @PostConstruct
    public void postConstructCallback() {

        try {
            this.datatypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new EJBException("datatype config error");
        }

        LOG.debug("ready");
    }

    public AuthenticationPortImpl() {

        // XXX: make this configurable ..., time is in ms
        manager.setTimeout(1000 * 60 * 30, new TimeoutCallback());
    }


    class TimeoutCallback implements StatefulWebServiceManager.Callback<AuthenticationPort> {

        /**
         * {@inheritDoc}
         */
        public void onTimeout(AuthenticationPort timedOutObject, StatefulWebServiceManager<AuthenticationPort> manager) {

            // XXX: notify stateful device ws to timeout ?
        }

    }


    /**
     * {@inheritDoc}
     */
    public WSAuthenticationResponseType authenticate(WSAuthenticationRequestType request) {

        LOG.debug("authenticate");

        String id = request.getID();
        String applicationId = request.getApplicationId();
        String deviceName = request.getDeviceName();
        KeyInfoType keyInfo = request.getKeyInfo();
        DeviceCredentialsType deviceCredentials = request.getDeviceCredentials();

        // XXX: lookup userId from username in deviceCredentials
        if (null != deviceCredentials) {
            deviceCredentials.getAny();
        }
        if (null == this.userId) {
            this.userId = UUID.randomUUID().toString();
        }
        LOG.debug("userId: " + this.userId);

        SamlAuthorityService samlAuthorityService = EjbUtils.getEJB(SamlAuthorityService.JNDI_BINDING, SamlAuthorityService.class);
        LOG.info("samlAuthorityService=" + samlAuthorityService);

        // generate response
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        Date date = new Date();
        gregorianCalendar.setTime(date);
        XMLGregorianCalendar now = this.datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);

        gregorianCalendar.add(Calendar.SECOND, samlAuthorityService.getAuthnAssertionValidity());
        XMLGregorianCalendar notAfter = this.datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);

        SecureRandomIdentifierGenerator idGenerator;
        try {
            idGenerator = new SecureRandomIdentifierGenerator();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("secure random init error: " + e.getMessage(), e);
        }

        // Assertion
        AssertionType assertion = new AssertionType();
        assertion.setVersion(SAMLVersion.VERSION_20.toString());
        assertion.setID(idGenerator.generateIdentifier());
        assertion.setIssueInstant(now);

        // Issuer
        NameIDType issuerName = new NameIDType();
        String samlAuthorityIssuerName = samlAuthorityService.getIssuerName();
        issuerName.setValue(samlAuthorityIssuerName);
        assertion.setIssuer(issuerName);

        // Subject
        SubjectType subject = new SubjectType();
        NameIDType subjectName = new NameIDType();
        subjectName.setFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:persistent");
        subjectName.setValue(this.userId);
        ObjectFactory samlObjectFactory = new ObjectFactory();
        subject.getContent().add(samlObjectFactory.createNameID(subjectName));
        assertion.setSubject(subject);

        // Conditions
        ConditionsType conditions = new ConditionsType();
        conditions.setNotBefore(now);
        conditions.setNotOnOrAfter(notAfter);
        AudienceRestrictionType audienceRestriction = new AudienceRestrictionType();
        audienceRestriction.getAudience().add(applicationId);
        conditions.getConditionOrAudienceRestrictionOrOneTimeUse().add(audienceRestriction);
        assertion.setConditions(conditions);

        // SubjectConfirmation
        SubjectConfirmationType subjectConfirmation = new SubjectConfirmationType();
        SubjectConfirmationDataType subjectConfirmationData = new SubjectConfirmationDataType();
        if (null != keyInfo) {
            subjectConfirmation.setMethod(Saml2SubjectConfirmationMethod.HOLDER_OF_KEY.getMethodURI());
            org.w3._2000._09.xmldsig_.ObjectFactory dsigObjectFactory = new org.w3._2000._09.xmldsig_.ObjectFactory();
            subjectConfirmationData.getContent().add(dsigObjectFactory.createKeyInfo(keyInfo));
        } else {
            subjectConfirmation.setMethod(Saml2SubjectConfirmationMethod.SENDER_VOUCHES.getMethodURI());
        }
        subjectConfirmationData.setInResponseTo(id);
        subjectConfirmationData.setNotBefore(now);
        subjectConfirmationData.setNotOnOrAfter(notAfter);
        subjectConfirmation.setSubjectConfirmationData(subjectConfirmationData);
        subject.getContent().add(samlObjectFactory.createSubjectConfirmation(subjectConfirmation));

        // Authentication Statement
        AuthnStatementType authnStatement = new AuthnStatementType();
        authnStatement.setAuthnInstant(now);
        AuthnContextType authnContext = new AuthnContextType();
        authnContext.getContent().add(samlObjectFactory.createAuthnContextClassRef(deviceName));
        authnStatement.setAuthnContext(authnContext);
        assertion.getStatementOrAuthnStatementOrAuthzDecisionStatement().add(authnStatement);

        WSAuthenticationResponseType response = new WSAuthenticationResponseType();
        response.setID(idGenerator.generateIdentifier());
        response.setVersion(SAMLVersion.VERSION_20.toString());
        response.setIssueInstant(now);
        response.setInResponseTo(id);
        response.setIssuer(issuerName);
        response.setDeviceName(deviceName);

        WSAuthenticationStepType usageAgreementStep = new WSAuthenticationStepType();
        usageAgreementStep.setAuthenticationStep(AuthenticationStep.USAGE_AGREEMENT.getValue());
        response.getWSAuthenticationStep().add(usageAgreementStep);
        WSAuthenticationStepType identityConfirmationStep = new WSAuthenticationStepType();
        identityConfirmationStep.setAuthenticationStep(AuthenticationStep.IDENTITY_CONFIRMATION.getValue());
        response.getWSAuthenticationStep().add(identityConfirmationStep);
        WSAuthenticationStepType missingAttributesStep = new WSAuthenticationStepType();
        missingAttributesStep.setAuthenticationStep(AuthenticationStep.MISSING_ATTRIBUTES.getValue());
        response.getWSAuthenticationStep().add(missingAttributesStep);
        WSAuthenticationStepType globalUsageAgreementStep = new WSAuthenticationStepType();
        globalUsageAgreementStep.setAuthenticationStep(AuthenticationStep.GLOBAL_USAGE_AGREEMENT.getValue());
        response.getWSAuthenticationStep().add(globalUsageAgreementStep);

        response.setDeviceAuthenticationInformation(null);

        response.getAssertion().add(assertion);

        StatusType status = new StatusType();
        StatusCodeType statusCode = new StatusCodeType();
        statusCode.setValue(StatusCode.SUCCESS_URI);
        status.setStatusCode(statusCode);
        response.setStatus(status);

        // XXX: unexport when done
        if (false == this.authenticated) {
            this.authenticated = true;
        } else {
            manager.unexport(this);
        }

        return response;
    }
}
