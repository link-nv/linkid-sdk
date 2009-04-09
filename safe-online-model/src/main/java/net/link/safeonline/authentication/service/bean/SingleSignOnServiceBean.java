/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.authentication.service.bean;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.interceptor.Interceptors;
import javax.servlet.http.Cookie;

import net.link.safeonline.audit.AccessAuditLogger;
import net.link.safeonline.audit.AuditContextManager;
import net.link.safeonline.audit.SecurityAuditLogger;
import net.link.safeonline.authentication.exception.InternalInconsistencyException;
import net.link.safeonline.authentication.exception.InvalidCookieException;
import net.link.safeonline.authentication.service.AuthenticationAssertion;
import net.link.safeonline.authentication.service.SingleSignOnService;
import net.link.safeonline.authentication.service.SingleSignOnState;
import net.link.safeonline.common.SafeOnlineCookies;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.ApplicationPoolDAO;
import net.link.safeonline.dao.DeviceDAO;
import net.link.safeonline.dao.SessionTrackingDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationPoolEntity;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.audit.SecurityThreatType;
import net.link.safeonline.entity.sessiontracking.SessionAssertionEntity;
import net.link.safeonline.entity.sessiontracking.SessionAuthnStatementEntity;
import net.link.safeonline.entity.sessiontracking.SessionTrackingEntity;
import net.link.safeonline.keystore.SafeOnlineNodeKeyStore;
import net.link.safeonline.service.SubjectService;
import net.link.safeonline.validation.InputValidation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.exceptions.Base64DecodingException;
import org.apache.xml.security.utils.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jboss.annotation.ejb.LocalBinding;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;


/**
 * <h2>{@link SingleSignOnServiceBean}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * Implementation of the {@link SingleSignOnService}.
 * </p>
 * 
 * <p>
 * <i>Mar 25, 2009</i>
 * </p>
 * 
 * @author wvdhaute
 */
@Stateful
@LocalBinding(jndiBinding = SingleSignOnService.JNDI_BINDING)
@Interceptors( { AuditContextManager.class, AccessAuditLogger.class, InputValidation.class })
public class SingleSignOnServiceBean implements SingleSignOnService {

    static final Log                      LOG                                       = LogFactory.getLog(SingleSignOnServiceBean.class);

    public static final String            SECURITY_MESSAGE_INVALID_STATE            = "Attempted to perform an operation with an illegal state";

    public static final String            SECURITY_MESSAGE_INVALID_COOKIE           = "Attempt to use an invalid SSO Cookie";
    public static final String            SECURITY_MESSAGE_INVALID_APPLICATION_POOL = SECURITY_MESSAGE_INVALID_COOKIE
                                                                                            + ": Invalid application pool: ";
    public static final String            SECURITY_MESSAGE_INVALID_DEVICE           = SECURITY_MESSAGE_INVALID_COOKIE
                                                                                            + ": Invalid device: ";
    public static final String            SECURITY_MESSAGE_INVALID_USER             = SECURITY_MESSAGE_INVALID_COOKIE + ": Invalid user: ";

    public static final String            SECURITY_MESSAGE_INVALID_APPLICATION      = SECURITY_MESSAGE_INVALID_COOKIE
                                                                                            + ": Invalid application: ";

    public static final String            SSO_ID_COOKIE_NAME                        = SafeOnlineCookies.SINGLE_SIGN_ON_COOKIE_PREFIX
                                                                                            + ".SSO-ID";

    @EJB(mappedName = SecurityAuditLogger.JNDI_BINDING)
    SecurityAuditLogger                   securityAuditLogger;

    @EJB(mappedName = SubjectService.JNDI_BINDING)
    SubjectService                        subjectService;

    @EJB(mappedName = ApplicationPoolDAO.JNDI_BINDING)
    ApplicationPoolDAO                    applicationPoolDAO;

    @EJB(mappedName = ApplicationDAO.JNDI_BINDING)
    ApplicationDAO                        applicationDAO;

    @EJB(mappedName = DeviceDAO.JNDI_BINDING)
    DeviceDAO                             deviceDAO;

    @EJB(mappedName = SessionTrackingDAO.JNDI_BINDING)
    SessionTrackingDAO                    sessionTrackingDAO;

    private SingleSignOnState             singleSignOnState;

    private boolean                       forceAuthentication;

    private String                        session;

    private List<ApplicationPoolEntity>   applicationPools;

    private ApplicationEntity             authenticatingApplication;

    private Set<DeviceEntity>             deviceRestriction;

    private List<SingleSignOn>            ssoItems;

    private Cookie                        ssoIdCookie;

    private List<Cookie>                  invalidCookies;

    private List<AuthenticationAssertion> authenticationAssertions;


    @PostConstruct
    public void postConstructCallback() {

        /*
         * Set the initial state of this single sign on service bean.
         */
        singleSignOnState = SingleSignOnState.INIT;
    }

    @PreDestroy
    public void preDestroyCallback() {

    }

    /**
     * {@inheritDoc}
     */
    @Remove
    public void abort() {

        LOG.debug("abort");

    }

    private void checkState(SingleSignOnState state) {

        if (singleSignOnState != state || singleSignOnState == SingleSignOnState.FAILED) {
            securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, SECURITY_MESSAGE_INVALID_STATE);
            throw new IllegalStateException(SECURITY_MESSAGE_INVALID_STATE);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void initialize(boolean forceAuthn, String sessionInfo, List<String> audiences, ApplicationEntity application,
                           Set<DeviceEntity> devices) {

        checkState(SingleSignOnState.INIT);

        /*
         * Safe the state in this stateful session bean.
         */
        forceAuthentication = forceAuthn;
        session = sessionInfo;
        authenticatingApplication = application;
        deviceRestriction = devices;
        applicationPools = new LinkedList<ApplicationPoolEntity>();
        ssoItems = new LinkedList<SingleSignOn>();
        invalidCookies = new LinkedList<Cookie>();
        singleSignOnState = SingleSignOnState.INITIALIZED;

        /*
         * Lookup application pools from audience, if no audience, lookup all pools of the application. If empty, return.
         */
        if (null != audiences && !audiences.isEmpty()) {
            for (String applicationPoolName : audiences) {
                ApplicationPoolEntity applicationPool = applicationPoolDAO.findApplicationPool(applicationPoolName);
                if (null == applicationPool) {
                    LOG.debug(SECURITY_MESSAGE_INVALID_APPLICATION_POOL + applicationPoolName);
                    securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, SECURITY_MESSAGE_INVALID_APPLICATION_POOL
                            + applicationPoolName);
                    applicationPools.clear();
                    return;
                }
                applicationPools.add(applicationPool);
            }
        } else {
            applicationPools = applicationPoolDAO.listApplicationPools(authenticatingApplication);
        }
    }

    /**
     * {@inheritDoc}
     */
    public List<AuthenticationAssertion> signOn(List<Cookie> ssoCookies) {

        checkState(SingleSignOnState.INITIALIZED);

        /*
         * If SSO not enabled, return.
         */
        if (!authenticatingApplication.isSsoEnabled()) {
            /*
             * Set state
             */
            singleSignOnState = SingleSignOnState.FAILED;
            return null;
        }

        if (applicationPools.isEmpty()) {
            /*
             * Set state
             */
            singleSignOnState = SingleSignOnState.FAILED;

            LOG.debug("no application pools found");
            return null;
        }

        /*
         * Parse SSO cookies
         */
        for (Cookie ssoCookie : ssoCookies) {
            if (ssoCookie.getName().equals(SSO_ID_COOKIE_NAME)) {
                LOG.debug("found existing SSO-ID cookie");
                ssoIdCookie = ssoCookie;
            } else {
                try {
                    ssoItems.add(new SingleSignOn(ssoCookie));
                } catch (InvalidCookieException e) {
                    LOG.debug("Invalid cookie " + ssoCookie.getName() + " marked for removal");
                    ssoCookie.setMaxAge(0);
                    ssoCookie.setValue("");
                    invalidCookies.add(ssoCookie);
                }
            }
        }
        if (ssoItems.isEmpty()) {
            /*
             * Set state
             */
            singleSignOnState = SingleSignOnState.FAILED;

            LOG.debug("no valid sso cookies found");
            return null;
        }

        /*
         * Filter SSO cookies on application pools
         */
        filterOnPools();

        /*
         * Filter SSO cookies on device restriction
         */
        filterOnDeviceRestriction();

        /*
         * Filter SSO cookies on SSO timeout
         */
        filterOnTime();

        /*
         * If Force Authentication or result list is empty, returned filtered list of cookies.
         */
        if (forceAuthentication || ssoItems.isEmpty()) {
            /*
             * Set state
             */
            singleSignOnState = SingleSignOnState.FORCE_AUTHENTICATION;
            ssoItems.clear();

            LOG.debug("force authentication");
            return null;
        }

        /*
         * Add application to remaining valid SSO cookies
         */
        for (SingleSignOn sso : ssoItems) {
            sso.addSsoApplication(authenticatingApplication);
        }

        /*
         * Set SSO assertions. These can be queried by the webapp. The assertion are per subject a list of authenticated devices. So if
         * multiple assertions are returned, webapp will have to make a selection.
         */
        return getAssertions();
    }

    /**
     * {@inheritDoc}
     */
    public void selectUser(AuthenticationAssertion authenticationAssertion) {

        List<SingleSignOn> newSsoItems = new LinkedList<SingleSignOn>();
        for (SingleSignOn sso : ssoItems) {
            if (sso.subject.equals(authenticationAssertion.getSubject())) {
                newSsoItems.add(sso);
            }
        }
        ssoItems = newSsoItems;

        updateSessionTracker();
    }

    /**
     * {@inheritDoc}
     */
    public List<ApplicationEntity> getApplicationsToLogout(String sessionInfo, ApplicationEntity application, List<Cookie> ssoCookies) {

        invalidCookies = new LinkedList<Cookie>();

        if (!application.isSsoEnabled())
            return null;

        session = sessionInfo;

        /*
         * Find SSO ID cookie
         */
        for (Cookie ssoCookie : ssoCookies) {
            if (ssoCookie.getName().equals(SSO_ID_COOKIE_NAME)) {
                ssoIdCookie = ssoCookie;
            }
        }

        /*
         * Parse SSO cookies
         */
        List<ApplicationEntity> applicationsToLogout = new LinkedList<ApplicationEntity>();
        for (Cookie ssoCookie : ssoCookies) {
            if (ssoCookie.getName().equals(SSO_ID_COOKIE_NAME)) {
                continue;
            }
            try {
                SingleSignOn sso = new SingleSignOn(ssoCookie);
                for (ApplicationEntity applicationToLogout : sso.getApplicationsToLogout(application)) {

                    if (null != session && null != ssoIdCookie) {
                        SessionTrackingEntity tracker = sessionTrackingDAO.findTracker(applicationToLogout, session,
                                ssoIdCookie.getValue(), sso.applicationPool);
                        if (null == tracker) {
                            continue;
                        }
                    }
                    applicationsToLogout.add(applicationToLogout);

                }
            } catch (InvalidCookieException e) {
                LOG.debug("Invalid cookie " + ssoCookie.getName() + " marked for removal");
                ssoCookie.setMaxAge(0);
                ssoCookie.setValue("");
                invalidCookies.add(ssoCookie);
            }
        }
        LOG.debug("found " + applicationsToLogout.size() + " to logout");
        return applicationsToLogout;
    }

    /**
     * {@inheritDoc}
     */
    public List<Cookie> getInvalidCookies() {

        return invalidCookies;
    }

    /**
     * {@inheritDoc}
     */
    public void setCookies(SubjectEntity subject, DeviceEntity authenticationDevice, DateTime authenticationTime) {

        /*
         * If SSO not enabled, return.
         */
        if (!authenticatingApplication.isSsoEnabled())
            return;

        if (ssoItems.isEmpty()) {
            /*
             * Create SSO cookie for each application pool
             */
            createCookies(subject, authenticationDevice, authenticationTime);
        } else {
            /*
             * Go over each sso cookie, see if user/device matches, if so update time, not found => create for each pool
             */
            boolean found = false;
            for (SingleSignOn sso : ssoItems) {
                if (sso.subject.equals(subject) && sso.device.equals(authenticationDevice)) {
                    found = true;
                    sso.time = authenticationTime;
                    sso.setCookie();
                }
            }
            if (!found) {
                createCookies(subject, authenticationDevice, authenticationTime);
            }
        }

        updateSessionTracker();
    }

    private void createCookies(SubjectEntity subject, DeviceEntity authenticationDevice, DateTime authenticationTime) {

        ssoItems = new LinkedList<SingleSignOn>();

        for (ApplicationPoolEntity applicationPool : applicationPools) {
            SingleSignOn sso = new SingleSignOn(subject, applicationPool, authenticatingApplication, authenticationDevice,
                    authenticationTime);
            sso.setCookie();
            ssoItems.add(sso);
        }
    }

    /**
     * {@inheritDoc}
     */
    public List<Cookie> getCookies() {

        /*
         * If SSO not enabled, return.
         */
        if (!authenticatingApplication.isSsoEnabled())
            return null;

        List<Cookie> cookies = new LinkedList<Cookie>();
        for (SingleSignOn sso : ssoItems) {
            cookies.add(sso.ssoCookie);
        }
        if (null != ssoIdCookie) {
            cookies.add(ssoIdCookie);
        }
        return cookies;
    }

    /**
     * {@inheritDoc}
     */
    public SingleSignOnState getState() {

        return singleSignOnState;
    }

    private void filterOnPools() {

        List<SingleSignOn> newSsoItems = new LinkedList<SingleSignOn>();
        for (SingleSignOn sso : ssoItems) {
            if (applicationPools.contains(sso.applicationPool)) {
                newSsoItems.add(sso);
            }
        }
        ssoItems = newSsoItems;
    }

    private void filterOnDeviceRestriction() {

        if (null == deviceRestriction)
            return;

        List<SingleSignOn> newSsoItems = new LinkedList<SingleSignOn>();
        for (SingleSignOn sso : ssoItems) {
            if (deviceRestriction.contains(sso.device)) {
                newSsoItems.add(sso);
            }
        }
        ssoItems = newSsoItems;

    }

    private void filterOnTime() {

        List<SingleSignOn> newSsoItems = new LinkedList<SingleSignOn>();
        for (SingleSignOn sso : ssoItems) {
            if (sso.isValidTime()) {
                newSsoItems.add(sso);
            } else {
                invalidCookies.add(sso.ssoCookie);
            }
        }
        ssoItems = newSsoItems;
    }

    private List<AuthenticationAssertion> getAssertions() {

        authenticationAssertions = new LinkedList<AuthenticationAssertion>();
        for (SingleSignOn sso : ssoItems) {
            AuthenticationAssertion ssoAssertion = null;
            if ((ssoAssertion = findAssertion(sso.subject)) == null) {
                ssoAssertion = new AuthenticationAssertion(sso.subject);
                ssoAssertion.addAuthentication(sso.time, sso.device);
                authenticationAssertions.add(ssoAssertion);
            } else {
                ssoAssertion.addAuthentication(sso.time, sso.device);
            }
        }
        if (authenticationAssertions.size() > 1) {
            /*
             * If multiple assertions, set state to enforce selection
             */
            singleSignOnState = SingleSignOnState.SELECT_USER;
        } else {
            /*
             * Only 1 user
             */
            updateSessionTracker();

            singleSignOnState = SingleSignOnState.SUCCESS;
        }
        return authenticationAssertions;
    }

    private AuthenticationAssertion findAssertion(SubjectEntity subject) {

        for (AuthenticationAssertion ssoAssertion : authenticationAssertions) {
            if (ssoAssertion.getSubject().equals(subject))
                return ssoAssertion;
        }
        return null;
    }

    private void updateSessionTracker() {

        for (SingleSignOn sso : ssoItems) {
            // session not null: application session needs to be tracked
            if (null != session) {
                // create SSO-ID cookie if not yet created
                if (null == ssoIdCookie) {
                    ssoIdCookie = new Cookie(SSO_ID_COOKIE_NAME, UUID.randomUUID().toString());
                    ssoIdCookie.setMaxAge(-1);
                }
                String ssoId = ssoIdCookie.getValue();

                SessionTrackingEntity tracker = sessionTrackingDAO.findTracker(authenticatingApplication, session, ssoId,
                        sso.applicationPool);
                if (null == tracker) {
                    tracker = sessionTrackingDAO.addTracker(authenticatingApplication, session, ssoId, sso.applicationPool);
                } else {
                    tracker.setTimestamp(new Date());
                }

                /*
                 * Update / add authentication statements
                 */
                SessionAssertionEntity assertion = sessionTrackingDAO.findAssertion(ssoId, sso.applicationPool);
                if (null == assertion) {
                    assertion = sessionTrackingDAO.addAssertion(ssoId, sso.applicationPool);
                }
                if (!sso.subject.equals(assertion.getSubject())) {
                    sessionTrackingDAO.removeStatements(assertion);
                }
                assertion.setSubject(sso.subject);
                SessionAuthnStatementEntity authnStatement = sessionTrackingDAO.addAuthnStatement(assertion, sso.time, sso.device);
                assertion.getStatements().add(authnStatement);
            }
        }
    }


    public class SingleSignOn {

        public static final String     SUBJECT_FIELD          = "subject";
        public static final String     APPLICATION_POOL_FIELD = "applicationPool";
        public static final String     APPLICATION_FIELD      = "application";
        public static final String     DEVICE_FIELD           = "device";
        public static final String     TIME_FIELD             = "time";
        public static final String     SSO_APPLICATIONS_FIELD = "ssoApplications";

        public Cookie                  ssoCookie;
        public SubjectEntity           subject;
        public ApplicationPoolEntity   applicationPool;
        public ApplicationEntity       application;
        public DeviceEntity            device;
        public DateTime                time;
        public List<ApplicationEntity> ssoApplications;


        public SingleSignOn(Cookie ssoCookie) throws InvalidCookieException {

            ssoApplications = new LinkedList<ApplicationEntity>();
            this.ssoCookie = ssoCookie;
            parseCookie(ssoCookie);
        }

        public SingleSignOn(SubjectEntity subject, ApplicationPoolEntity applicationPool, ApplicationEntity application,
                            DeviceEntity device, DateTime time) {

            ssoApplications = new LinkedList<ApplicationEntity>();
            this.subject = subject;
            this.applicationPool = applicationPool;
            this.application = application;
            this.device = device;
            this.time = time;
        }

        public void setCookie() {

            String value = getValue();
            LOG.debug("cookie value: " + value);

            BouncyCastleProvider bcp = (BouncyCastleProvider) Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);
            String encryptedValue;
            try {
                Cipher encryptCipher = Cipher.getInstance("AES", bcp);
                encryptCipher.init(Cipher.ENCRYPT_MODE, SafeOnlineNodeKeyStore.getSSOKey());
                byte[] encryptedBytes = encryptCipher.doFinal(value.getBytes("UTF-8"));
                encryptedValue = new String(Base64.encode(encryptedBytes));

            } catch (InvalidKeyException e) {
                LOG.debug("invalid key: " + e.getMessage());
                throw new InternalInconsistencyException("invalid key: " + e.getMessage());
            } catch (IllegalBlockSizeException e) {
                LOG.debug("illegal block size: " + e.getMessage());
                throw new InternalInconsistencyException("invalid key: " + e.getMessage());
            } catch (BadPaddingException e) {
                LOG.debug("bad padding: " + e.getMessage());
                throw new InternalInconsistencyException("invalid key: " + e.getMessage());
            } catch (NoSuchAlgorithmException e) {
                LOG.debug("no such algorithm: " + e.getMessage());
                throw new InternalInconsistencyException("invalid key: " + e.getMessage());
            } catch (NoSuchPaddingException e) {
                LOG.debug("no such padding: " + e.getMessage());
                throw new InternalInconsistencyException("invalid key: " + e.getMessage());
            } catch (UnsupportedEncodingException e) {
                LOG.debug("unsupported encoding: " + e.getMessage());
                throw new InternalInconsistencyException("invalid key: " + e.getMessage());
            }

            LOG.debug("1: encrypted cookie value: " + encryptedValue);

            if (null != ssoCookie) {
                ssoCookie.setValue(encryptedValue);
            } else {
                ssoCookie = new Cookie(SafeOnlineCookies.SINGLE_SIGN_ON_COOKIE_PREFIX + "." + UUID.randomUUID().toString(), encryptedValue);
            }
            ssoCookie.setMaxAge(-1);
        }

        public String getValue() {

            String ssoApplicationsValue = "";
            for (ApplicationEntity ssoApplication : ssoApplications) {
                ssoApplicationsValue += ssoApplication.getId() + ",";
            }
            if (ssoApplicationsValue.endsWith(",")) {
                ssoApplicationsValue = ssoApplicationsValue.substring(0, ssoApplicationsValue.length() - 1);
            }

            return SUBJECT_FIELD + "=" + subject.getUserId() + ";" + APPLICATION_POOL_FIELD + "=" + applicationPool.getName() + ";"
                    + APPLICATION_FIELD + "=" + application.getId() + ";" + DEVICE_FIELD + "=" + device.getName() + ";" + TIME_FIELD + "="
                    + time.toString() + ";" + SSO_APPLICATIONS_FIELD + "=" + ssoApplicationsValue;
        }

        public void addSsoApplication(ApplicationEntity ssoApplication) {

            if (!ssoApplications.contains(ssoApplication)) {
                ssoApplications.add(ssoApplication);
                setCookie();
            }
        }

        public boolean isValidTime() {

            DateTime notAfter = time.plus(applicationPool.getSsoTimeout());
            DateTime now = new DateTime();
            if (now.isAfter(notAfter)) {
                LOG.debug("SSO Cookie has expired: " + ssoCookie.getName());
                return false;
            }
            return true;
        }

        public List<ApplicationEntity> getApplicationsToLogout(ApplicationEntity logoutApplication) {

            List<ApplicationEntity> applicationsToLogout = new LinkedList<ApplicationEntity>();
            if (logoutApplication.equals(application) || ssoApplications.contains(logoutApplication)) {
                if (!applicationsToLogout.contains(application) && !application.equals(logoutApplication)) {
                    if (null != application.getSsoLogoutUrl()) {
                        LOG.debug("add application " + application.getName() + " for logout");
                        applicationsToLogout.add(application);
                    }
                }
                for (ApplicationEntity ssoApplication : ssoApplications) {
                    if (!applicationsToLogout.contains(ssoApplication) && !ssoApplication.equals(logoutApplication)) {
                        if (null != ssoApplication.getSsoLogoutUrl()) {
                            LOG.debug("add application " + ssoApplication.getName() + " for logout");
                            applicationsToLogout.add(ssoApplication);
                        }
                    }
                }
            }
            return applicationsToLogout;

        }

        private void parseCookie(Cookie cookie)
                throws InvalidCookieException {

            if (!cookie.getName().startsWith(SafeOnlineCookies.SINGLE_SIGN_ON_COOKIE_PREFIX)) {
                LOG.debug("not a single sign on cookie: " + cookie.getName());
                throw new InvalidCookieException("not a single sign on cookie: " + cookie.getName());
            }

            /*
             * Decrypt SSO Cookie value
             */
            LOG.debug("2: encrypted cookie value: " + cookie.getValue());

            BouncyCastleProvider bcp = (BouncyCastleProvider) Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);
            String decryptedValue;
            try {
                Cipher decryptCipher = Cipher.getInstance("AES", bcp);
                decryptCipher.init(Cipher.DECRYPT_MODE, SafeOnlineNodeKeyStore.getSSOKey());
                byte[] decryptedBytes = decryptCipher.doFinal(Base64.decode(cookie.getValue().getBytes("UTF-8")));
                decryptedValue = new String(decryptedBytes);
            } catch (InvalidKeyException e) {
                LOG.debug("invalid key: " + e.getMessage());
                throw new InvalidCookieException(e.getMessage());
            } catch (IllegalBlockSizeException e) {
                LOG.debug("illegal block size: " + e.getMessage());
                throw new InvalidCookieException(e.getMessage());
            } catch (BadPaddingException e) {
                LOG.debug("bad padding: " + e.getMessage());
                throw new InvalidCookieException(e.getMessage());
            } catch (NoSuchAlgorithmException e) {
                LOG.debug("no such algorithm: " + e.getMessage());
                throw new InvalidCookieException(e.getMessage());
            } catch (NoSuchPaddingException e) {
                LOG.debug("no such padding: " + e.getMessage());
                throw new InvalidCookieException(e.getMessage());
            } catch (Base64DecodingException e) {
                LOG.debug("base 64 decoding error: " + e.getMessage());
                throw new InvalidCookieException(e.getMessage());
            } catch (UnsupportedEncodingException e) {
                LOG.debug("unsupported encoding error: " + e.getMessage());
                throw new InvalidCookieException(e.getMessage());
            }

            LOG.debug("Decrypted cookie: " + decryptedValue);

            /*
             * Check SSO Cookie properties
             */
            String[] properties = decryptedValue.split(";");
            if (null == properties || properties.length != 6) {
                LOG.debug(SECURITY_MESSAGE_INVALID_COOKIE);
                securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, SECURITY_MESSAGE_INVALID_COOKIE);
                throw new InvalidCookieException("Invalid SSO Cookie");
            }

            /*
             * Check subject property
             */
            String[] subjectProperty = properties[0].split("=");
            if (null == subjectProperty || subjectProperty.length != 2 || !subjectProperty[0].equals(SingleSignOn.SUBJECT_FIELD)) {
                LOG.debug(SECURITY_MESSAGE_INVALID_COOKIE);
                securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, SECURITY_MESSAGE_INVALID_COOKIE);
                throw new InvalidCookieException("Invalid SSO Cookie");
            }
            subject = subjectService.findSubject(subjectProperty[1]);
            if (null == subject) {
                LOG.debug(SECURITY_MESSAGE_INVALID_USER + subjectProperty[1]);
                securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, SECURITY_MESSAGE_INVALID_USER + subjectProperty[1]);
                throw new InvalidCookieException("Invalid SSO Cookie");
            }

            /*
             * Check application pool property
             */
            String[] applicationPoolProperty = properties[1].split("=");
            if (null == applicationPoolProperty || applicationPoolProperty.length != 2
                    || !applicationPoolProperty[0].equals(SingleSignOn.APPLICATION_POOL_FIELD)) {
                LOG.debug(SECURITY_MESSAGE_INVALID_COOKIE);
                securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, SECURITY_MESSAGE_INVALID_COOKIE);
                throw new InvalidCookieException("Invalid SSO Cookie");
            }
            applicationPool = applicationPoolDAO.findApplicationPool(applicationPoolProperty[1]);
            if (null == applicationPool) {
                LOG.debug(SECURITY_MESSAGE_INVALID_APPLICATION_POOL + applicationPoolProperty[1]);
                securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, SECURITY_MESSAGE_INVALID_APPLICATION_POOL
                        + applicationPoolProperty[1]);
                throw new InvalidCookieException("Invalid SSO Cookie");
            }

            /*
             * Check application property
             */
            String[] applicationProperty = properties[2].split("=");
            if (null == applicationProperty || applicationProperty.length != 2
                    || !applicationProperty[0].equals(SingleSignOn.APPLICATION_FIELD)) {
                LOG.debug(SECURITY_MESSAGE_INVALID_COOKIE);
                securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, SECURITY_MESSAGE_INVALID_COOKIE);
                throw new InvalidCookieException("Invalid SSO Cookie");
            }
            application = applicationDAO.findApplication(Long.parseLong(applicationProperty[1]));
            if (null == application) {
                LOG.debug(SECURITY_MESSAGE_INVALID_APPLICATION + applicationProperty[1]);
                securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, SECURITY_MESSAGE_INVALID_APPLICATION
                        + applicationProperty[1]);
                throw new InvalidCookieException("Invalid SSO Cookie");
            }

            /*
             * Check device property
             */
            String[] deviceProperty = properties[3].split("=");
            if (null == deviceProperty || deviceProperty.length != 2 || !deviceProperty[0].equals(SingleSignOn.DEVICE_FIELD)) {
                LOG.debug(SECURITY_MESSAGE_INVALID_COOKIE);
                securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, SECURITY_MESSAGE_INVALID_COOKIE);
                throw new InvalidCookieException("Invalid SSO Cookie");
            }
            device = deviceDAO.findDevice(deviceProperty[1]);
            if (null == device) {
                LOG.debug(SECURITY_MESSAGE_INVALID_DEVICE + deviceProperty[1]);
                securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, SECURITY_MESSAGE_INVALID_DEVICE + deviceProperty[1]);
                throw new InvalidCookieException("Invalid SSO Cookie");
            }

            /*
             * Check time property
             */
            String[] timeProperty = properties[4].split("=");
            if (null == timeProperty || timeProperty.length != 2 || !timeProperty[0].equals(SingleSignOn.TIME_FIELD)) {
                LOG.debug(SECURITY_MESSAGE_INVALID_COOKIE);
                securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, SECURITY_MESSAGE_INVALID_COOKIE);
                throw new InvalidCookieException("Invalid SSO Cookie");
            }
            DateTimeFormatter dateFormatter = ISODateTimeFormat.dateTime();
            time = dateFormatter.parseDateTime(timeProperty[1]);

            // check sso applications property
            String[] ssoAppProperty = properties[5].split("=");
            if (null == ssoAppProperty || !ssoAppProperty[0].equals(SingleSignOn.SSO_APPLICATIONS_FIELD)) {
                LOG.debug(SECURITY_MESSAGE_INVALID_COOKIE);
                securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, SECURITY_MESSAGE_INVALID_COOKIE);
                throw new InvalidCookieException("Invalid SSO Cookie");
            }
            if (2 == ssoAppProperty.length) {
                String[] ssoApplicationsString = ssoAppProperty[1].split(",");
                for (String ssoApplicationId : ssoApplicationsString) {
                    ApplicationEntity ssoApplication = applicationDAO.findApplication(Long.parseLong(ssoApplicationId));
                    if (null == ssoApplication) {
                        LOG.debug(SECURITY_MESSAGE_INVALID_APPLICATION + ssoApplicationId);
                        securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, SECURITY_MESSAGE_INVALID_APPLICATION
                                + ssoApplicationId);
                        throw new InvalidCookieException("Invalid SSO Cookie");
                    }
                    ssoApplications.add(ssoApplication);
                }
            }
        }
    }

}
