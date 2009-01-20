/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper.audit.bean;

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.interceptor.Interceptors;

import net.link.safeonline.audit.exception.AuditContextNotFoundException;
import net.link.safeonline.audit.service.AuditService;
import net.link.safeonline.ctrl.error.ErrorMessageInterceptor;
import net.link.safeonline.entity.audit.AccessAuditEntity;
import net.link.safeonline.entity.audit.AuditAuditEntity;
import net.link.safeonline.entity.audit.AuditContextEntity;
import net.link.safeonline.entity.audit.ResourceAuditEntity;
import net.link.safeonline.entity.audit.SecurityAuditEntity;
import net.link.safeonline.oper.OperatorConstants;
import net.link.safeonline.oper.audit.AuditSearch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.datamodel.DataModelSelection;
import org.jboss.seam.core.ResourceBundle;
import org.jboss.seam.faces.FacesMessages;


@Stateful
@Name("audit")
@Scope(ScopeType.CONVERSATION)
@LocalBinding(jndiBinding = AuditSearch.JNDI_BINDING)
@SecurityDomain(OperatorConstants.SAFE_ONLINE_OPER_SECURITY_DOMAIN)
@Interceptors(ErrorMessageInterceptor.class)
public class AuditSearchBean implements AuditSearch {

    private static final Log    LOG                      = LogFactory.getLog(AuditSearchBean.class);

    private static final String AUDIT_CONTEXT_LIST_NAME  = "auditContextList";

    private static final String ACCESS_AUDIT_LIST_NAME   = "accessAuditRecordList";
    private static final String SECURITY_AUDIT_LIST_NAME = "securityAuditRecordList";
    private static final String RESOURCE_AUDIT_LIST_NAME = "resourceAuditRecordList";
    private static final String AUDIT_AUDIT_LIST_NAME    = "auditAuditRecordList";


    private enum SearchMode {
        ID,
        USER,
        TIME,
        ALL
    }


    @In(create = true)
    FacesMessages        facesMessages;

    @EJB(mappedName = AuditService.JNDI_BINDING)
    private AuditService auditService;

    private Long         searchContextId;

    private Integer      searchLastTimeDays    = 0;

    private Integer      searchLastTimeHours   = 0;

    private Integer      searchLastTimeMinutes = 0;

    @Out(required = false, scope = ScopeType.SESSION)
    @In(required = false)
    private SearchMode   searchMode;

    @Out(required = false, scope = ScopeType.SESSION)
    @In(required = false)
    private String       searchAuditUser;

    @Out(required = false, scope = ScopeType.SESSION)
    @In(required = false)
    private Date         ageLimit;

    @Out(value = "principal", required = false, scope = ScopeType.SESSION)
    private String       auditPrincipal;


    private void setMode(SearchMode searchMode) {

        this.searchMode = searchMode;
    }


    /*
     * 
     * Datamodels
     */
    @SuppressWarnings("unused")
    @DataModel(AUDIT_CONTEXT_LIST_NAME)
    private List<AuditContextEntity>  auditContextList;

    @DataModelSelection(AUDIT_CONTEXT_LIST_NAME)
    @Out(value = "auditContext", required = false, scope = ScopeType.SESSION)
    @In(required = false)
    private AuditContextEntity        auditContext;

    @SuppressWarnings("unused")
    @DataModel(ACCESS_AUDIT_LIST_NAME)
    private List<AccessAuditEntity>   accessAuditRecordList;

    @DataModelSelection(ACCESS_AUDIT_LIST_NAME)
    @In(required = false)
    private AccessAuditEntity         accessAuditEntity;

    @SuppressWarnings("unused")
    @DataModel(SECURITY_AUDIT_LIST_NAME)
    private List<SecurityAuditEntity> securityAuditRecordList;

    @DataModelSelection(SECURITY_AUDIT_LIST_NAME)
    @In(required = false)
    private SecurityAuditEntity       securityAuditEntity;

    @SuppressWarnings("unused")
    @DataModel(RESOURCE_AUDIT_LIST_NAME)
    private List<ResourceAuditEntity> resourceAuditRecordList;

    @SuppressWarnings("unused")
    @DataModel(AUDIT_AUDIT_LIST_NAME)
    private List<AuditAuditEntity>    auditAuditRecordList;


    /*
     * 
     * Accessors
     */
    public Long getSearchContextId() {

        return searchContextId;
    }

    public void setSearchContextId(Long searchContextId) {

        this.searchContextId = searchContextId;
    }

    public String getSearchAuditUser() {

        return searchAuditUser;
    }

    public void setSearchAuditUser(String searchAuditUser) {

        this.searchAuditUser = searchAuditUser;
    }

    public Integer getSearchLastTimeDays() {

        return searchLastTimeDays;
    }

    public void setSearchLastTimeDays(Integer searchLastTimeDays) {

        this.searchLastTimeDays = searchLastTimeDays;
    }

    public Integer getSearchLastTimeHours() {

        return searchLastTimeHours;
    }

    public void setSearchLastTimeHours(Integer searchLastTimeHours) {

        this.searchLastTimeHours = searchLastTimeHours;
    }

    public Integer getSearchLastTimeMinutes() {

        return searchLastTimeMinutes;
    }

    public void setSearchLastTimeMinutes(Integer searchLastTimeMinutes) {

        this.searchLastTimeMinutes = searchLastTimeMinutes;
    }

    /*
     * 
     * Factories
     */
    @Factory(AUDIT_CONTEXT_LIST_NAME)
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void auditContextListFactory() {

        LOG.debug("Retrieve audit contexts");
        auditContextList = auditService.listLastContexts();
    }

    @Factory(ACCESS_AUDIT_LIST_NAME)
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void accessAuditRecordListFactory() {

        if (SearchMode.ID == searchMode) {
            LOG.debug("Retrieve access audit records for context " + auditContext.getId());
            accessAuditRecordList = auditService.listAccessAuditRecords(auditContext.getId());
        } else if (SearchMode.USER == searchMode) {
            LOG.debug("Retrieve access audit records for user " + searchAuditUser);
            accessAuditRecordList = auditService.listAccessAuditRecords(searchAuditUser);
        } else if (SearchMode.TIME == searchMode) {
            LOG.debug("Retrieve access audit records since " + ageLimit);
            accessAuditRecordList = auditService.listAccessAuditRecordsSince(ageLimit);

        }
    }

    @Factory(SECURITY_AUDIT_LIST_NAME)
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void securityAuditRecordListFactory() {

        if (SearchMode.ID == searchMode) {
            LOG.debug("Retrieve security audit records for context " + auditContext.getId());
            securityAuditRecordList = auditService.listSecurityAuditRecords(auditContext.getId());
        } else if (SearchMode.USER == searchMode) {
            LOG.debug("Retrieve security audit records for user " + searchAuditUser);
            securityAuditRecordList = auditService.listSecurityAuditRecords(searchAuditUser);
        } else if (SearchMode.TIME == searchMode) {
            LOG.debug("Retrieve security audit records since " + ageLimit);
            securityAuditRecordList = auditService.listSecurityAuditRecordsSince(ageLimit);
        } else if (SearchMode.ALL == searchMode) {
            LOG.debug("Show all security audit records");
            securityAuditRecordList = auditService.listSecurityAuditRecords();
        }
    }

    @Factory(RESOURCE_AUDIT_LIST_NAME)
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void resourceAuditRecordListFactory() {

        if (SearchMode.ID == searchMode) {
            LOG.debug("Retrieve resource audit records for context " + auditContext.getId());
            resourceAuditRecordList = auditService.listResourceAuditRecords(auditContext.getId());
        } else if (SearchMode.TIME == searchMode) {
            LOG.debug("Retrieve resource audit records since " + ageLimit);
            resourceAuditRecordList = auditService.listResourceAuditRecordsSince(ageLimit);
        } else if (SearchMode.ALL == searchMode) {
            LOG.debug("Show all resource audit records");
            resourceAuditRecordList = auditService.listResourceAuditRecords();
        }
    }

    @Factory(AUDIT_AUDIT_LIST_NAME)
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void auditAuditRecordListFactory() {

        if (SearchMode.ID == searchMode) {
            LOG.debug("Retrieve audit audit records for context " + auditContext.getId());
            auditAuditRecordList = auditService.listAuditAuditRecords(auditContext.getId());
        } else if (SearchMode.TIME == searchMode) {
            LOG.debug("Retrieve audit audit records since " + ageLimit);
            auditAuditRecordList = auditService.listAuditAuditRecordsSince(ageLimit);
        } else if (SearchMode.ALL == searchMode) {
            LOG.debug("retrieving all audit audit");
            auditAuditRecordList = auditService.listAuditAuditRecords();
        }
    }

    /*
     * 
     * Actions
     */
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String view() {

        LOG.debug("View context " + auditContext.getId());

        setMode(SearchMode.ID);
        return "view";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String viewSecurityRecords() {

        LOG.debug("View all security records");
        setMode(SearchMode.ALL);
        return "view-security";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String viewResourceRecords() {

        LOG.debug("View all resource records");
        setMode(SearchMode.ALL);
        return "view-resource";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String viewAuditRecords() {

        LOG.debug("view all audit records");
        setMode(SearchMode.ALL);
        return "view-audit";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String removeContext()
            throws AuditContextNotFoundException {

        LOG.debug("Remove context " + auditContext.getId());
        auditService.removeAuditContext(auditContext.getId());
        auditContextListFactory();
        return "success";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String search() {

        LOG.debug("audit search: id=" + searchContextId + " user=" + searchAuditUser);
        if (null != searchContextId) {
            LOG.debug("Search context id " + searchContextId);
            setMode(SearchMode.ID);
            return "search-id";
        } else if (null != searchAuditUser && searchAuditUser.length() > 0) {
            LOG.debug("Search user " + searchAuditUser);
            setMode(SearchMode.USER);
            return "search-user";
        } else {
            LOG.debug("No search input specified");
            return null;
        }
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String searchLastTime() {

        Long timeLimitInMillis = System.currentTimeMillis()
                - (searchLastTimeMinutes + searchLastTimeHours * 60 + searchLastTimeDays * 60 * 24) * 60 * 1000;
        ageLimit = new Date(timeLimitInMillis);
        LOG.debug("Search audit records since " + ageLimit);
        setMode(SearchMode.TIME);
        return "search-time";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String viewPrincipal() {

        if (null != accessAuditEntity) {
            auditPrincipal = accessAuditEntity.getPrincipal();
        } else if (null != securityAuditEntity) {
            auditPrincipal = securityAuditEntity.getTargetPrincipal();
        }
        if (null == auditPrincipal) {
            auditPrincipal = OperatorConstants.UNKNOWN_PRINCIPAL;
        }
        LOG.debug("view principal: " + auditPrincipal);
        return "view-principal";
    }

    /*
     * 
     * Validators
     */
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void validateId(FacesContext context, UIComponent toValidate, Object value) {

        Long id = (Long) value;
        LOG.debug("validateId = " + id);
        try {
            auditContext = auditService.getAuditContext(id);
        } catch (AuditContextNotFoundException e) {
            ((UIInput) toValidate).setValid(false);
            String errorMsg = ResourceBundle.instance().getString("errorNoAuditRecordsFound");
            FacesMessage message = new FacesMessage(errorMsg);
            context.addMessage(toValidate.getClientId(context), message);
        }
        if (null == auditContext) {
            ((UIInput) toValidate).setValid(false);
            String errorMsg = ResourceBundle.instance().getString("errorNoAuditRecordsFound");
            FacesMessage message = new FacesMessage(errorMsg);
            context.addMessage(toValidate.getClientId(context), message);
        }
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void validateUser(FacesContext context, UIComponent toValidate, Object value) {

        String userName = (String) value;
        Set<String> users = auditService.listUsers();
        if (!users.contains(userName)) {
            ((UIInput) toValidate).setValid(false);
            String errorMsg = ResourceBundle.instance().getString("errorNoAuditRecordsFound");
            FacesMessage message = new FacesMessage(errorMsg);
            context.addMessage(toValidate.getClientId(context), message);
        }
    }

    @Remove
    @Destroy
    public void destroyCallback() {

        LOG.debug("destroy");
    }

}
