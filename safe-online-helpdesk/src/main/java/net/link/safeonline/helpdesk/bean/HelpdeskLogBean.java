/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.helpdesk.bean;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.interceptor.Interceptors;

import net.link.safeonline.ctrl.error.ErrorMessageInterceptor;
import net.link.safeonline.entity.helpdesk.HelpdeskContextEntity;
import net.link.safeonline.entity.helpdesk.HelpdeskEventEntity;
import net.link.safeonline.helpdesk.HelpdeskConstants;
import net.link.safeonline.helpdesk.HelpdeskLog;
import net.link.safeonline.helpdesk.service.HelpdeskService;

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
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.datamodel.DataModelSelection;
import org.jboss.seam.faces.FacesMessages;


@Stateful
@Name("helpdeskLog")
@LocalBinding(jndiBinding = HelpdeskConstants.JNDI_PREFIX + "HelpdeskLogBean/local")
@SecurityDomain(HelpdeskConstants.SAFE_ONLINE_HELPDESK_SECURITY_DOMAIN)
@Interceptors(ErrorMessageInterceptor.class)
public class HelpdeskLogBean implements HelpdeskLog {

    private static final Log            LOG                             = LogFactory.getLog(HelpdeskLogBean.class);

    private static final String         HELPDESK_CONTEXT_LIST_NAME      = "helpdeskContextList";

    private static final String         HELPDESK_LOG_LIST_NAME          = "helpdeskLogList";

    private static final String         HELPDESK_USER_LIST_NAME         = "helpdeskUserList";

    private static final String         HELPDESK_USER_CONTEXT_LIST_NAME = "helpdeskUserContextList";

    @EJB
    private HelpdeskService             helpdeskService;

    @In(create = true)
    FacesMessages                       facesMessages;

    private Long                        searchId;

    private String                      searchUserName;

    // set by different datamodels and used by shared log-view.xhtml
    @SuppressWarnings("unused")
    @Out(required = false)
    private HelpdeskContextEntity       context;

    /*
     * Seam Data models
     */
    @DataModel(HELPDESK_CONTEXT_LIST_NAME)
    private List<HelpdeskContextEntity> helpdeskContextList;

    @DataModelSelection(HELPDESK_CONTEXT_LIST_NAME)
    @Out(value = "selectedContext", required = false, scope = ScopeType.SESSION)
    @In(required = false)
    private HelpdeskContextEntity       selectedContext;

    @SuppressWarnings("unused")
    @DataModel(HELPDESK_LOG_LIST_NAME)
    private List<HelpdeskEventEntity>   helpdeskLogList;

    @DataModel(HELPDESK_USER_LIST_NAME)
    private List<String>                helpdeskUserList;

    @DataModelSelection(HELPDESK_USER_LIST_NAME)
    @Out(value = "selectedUser", required = false, scope = ScopeType.SESSION)
    @In(required = false)
    private String                      selectedUser;

    @SuppressWarnings("unused")
    @DataModel(HELPDESK_USER_CONTEXT_LIST_NAME)
    private List<HelpdeskContextEntity> helpdeskUserContextList;

    @DataModelSelection(HELPDESK_USER_CONTEXT_LIST_NAME)
    @Out(value = "selectedUserContext", required = false, scope = ScopeType.SESSION)
    @In(required = false)
    private HelpdeskContextEntity       selectedUserContext;


    /*
     * Seam Factory methods
     */
    @Factory(HELPDESK_CONTEXT_LIST_NAME)
    @RolesAllowed(HelpdeskConstants.HELPDESK_ROLE)
    public void helpdeskContextListFactory() {

        LOG.debug("helpdesk context list factory");
        this.selectedUserContext = null;
        this.helpdeskContextList = this.helpdeskService.listContexts();
    }

    @Factory(HELPDESK_LOG_LIST_NAME)
    @RolesAllowed(HelpdeskConstants.HELPDESK_ROLE)
    public void helpdeskLogListFactory() {

        Long id;
        if (null != this.searchId) {
            id = this.searchId;
            this.searchId = null;
        } else if (null == this.selectedContext) {
            id = this.selectedUserContext.getId();
            this.context = this.selectedUserContext;
        } else {
            id = this.selectedContext.getId();
            this.context = this.selectedContext;
        }
        LOG.debug("helpdesk log list factory ( id=" + id + " )");
        this.helpdeskLogList = this.helpdeskService.listEvents(id);
    }

    @Factory(HELPDESK_USER_LIST_NAME)
    @RolesAllowed(HelpdeskConstants.HELPDESK_ROLE)
    public void helpdeskUserListFactory() {

        LOG.debug("helpdesk user list factory");
        this.helpdeskUserList = this.helpdeskService.listUsers();
        this.selectedContext = null;
    }

    @Factory(HELPDESK_USER_CONTEXT_LIST_NAME)
    @RolesAllowed(HelpdeskConstants.HELPDESK_ROLE)
    public void helpdeskUserContextListFactory() {

        String user;
        if (null != this.searchUserName) {
            user = this.searchUserName;
            this.searchUserName = null;
        } else {
            user = this.selectedUser;
        }
        LOG.debug("helpdesk user context list factory (" + user + ")");
        this.helpdeskUserContextList = this.helpdeskService.listUserContexts(user);
    }

    /*
     * Actions
     */
    @RolesAllowed(HelpdeskConstants.HELPDESK_ROLE)
    public String view() {

        if (null == this.selectedContext) {
            LOG.debug("view log: " + this.selectedUserContext.getId());
        } else {
            LOG.debug("view log: " + this.selectedContext.getId());
        }
        return "view";
    }

    @RolesAllowed(HelpdeskConstants.HELPDESK_ROLE)
    public String removeLog() {

        Long id;
        if (null == this.selectedContext) {
            id = this.selectedUserContext.getId();
        } else {
            id = this.selectedContext.getId();
        }
        LOG.debug("remove log: " + id);

        this.helpdeskService.removeLog(id);
        if (null == this.selectedContext) {
            helpdeskContextListFactory();
        } else {
            helpdeskUserContextListFactory();
        }
        return "success";
    }

    @RolesAllowed(HelpdeskConstants.HELPDESK_ROLE)
    public String viewUser() {

        LOG.debug("view user \"" + this.selectedUser + "\"'s logs");
        return "viewUser";
    }

    @RolesAllowed(HelpdeskConstants.HELPDESK_ROLE)
    public String search() {

        LOG.debug("search id " + this.searchId);
        this.helpdeskContextList = this.helpdeskService.listContexts();
        for (HelpdeskContextEntity currentContext : this.helpdeskContextList) {
            if (currentContext.getId().equals(this.searchId))
                return "view";
        }
        return "search-failed";
    }

    @RolesAllowed(HelpdeskConstants.HELPDESK_ROLE)
    public String searchUser() {

        LOG.debug("search user " + this.searchUserName);
        this.helpdeskUserList = this.helpdeskService.listUsers();
        for (String user : this.helpdeskUserList) {
            if (user.equals(this.searchUserName))
                return "viewUser";
        }
        return "search-failed";
    }

    @Remove
    @Destroy
    public void destroyCallback() {

        LOG.debug("destroy");
    }

    public Long getSearchId() {

        return this.searchId;
    }

    public void setSearchId(Long searchId) {

        this.searchId = searchId;
    }

    public String getSearchUserName() {

        return this.searchUserName;
    }

    public void setSearchUserName(String searchUserName) {

        this.searchUserName = searchUserName;
    }

    @RolesAllowed(HelpdeskConstants.HELPDESK_ROLE)
    public List<String> autocomplete(Object event) {

        String idString = event.toString();
        List<String> idList = new LinkedList<String>();
        this.helpdeskContextList = this.helpdeskService.listContexts();
        for (HelpdeskContextEntity currentContext : this.helpdeskContextList) {
            String contextIdString = currentContext.getId().toString();
            if (contextIdString.startsWith(idString)) {
                idList.add(contextIdString);
            }
        }
        LOG.debug("return suggestion list size = " + idList.size());
        return idList;
    }

    @RolesAllowed(HelpdeskConstants.HELPDESK_ROLE)
    public List<String> autocompleteUser(Object event) {

        String userString = event.toString();
        List<String> userList = new LinkedList<String>();
        this.helpdeskUserList = this.helpdeskService.listUsers();
        for (String user : this.helpdeskUserList) {
            if (user.startsWith(userString)) {
                userList.add(user);
            }
        }
        LOG.debug("return suggestion list size = " + userList.size());
        return userList;
    }

    /*
     *
     * Validators
     */
    @RolesAllowed(HelpdeskConstants.HELPDESK_ROLE)
    public void validateId(FacesContext contextIn, UIComponent toValidate, Object value) {

        Long id = (Long) value;
        LOG.debug("validateId: " + id);
        this.helpdeskContextList = this.helpdeskService.listContexts();
        for (HelpdeskContextEntity helpdeskContext : this.helpdeskContextList) {
            Long contextId = helpdeskContext.getId();
            if (contextId.equals(id))
                return;
        }
        LOG.debug("id " + id + " not found");
        ((UIInput) toValidate).setValid(false);
        this.facesMessages.addFromResourceBundle("errorHelpdeskIdNotFound");
    }

    @RolesAllowed(HelpdeskConstants.HELPDESK_ROLE)
    public void validateUser(FacesContext contextIn, UIComponent toValidate, Object value) {

        String user = (String) value;
        LOG.debug("validateUser: " + user);
        this.helpdeskUserList = this.helpdeskService.listUsers();
        if (!this.helpdeskUserList.contains(user)) {
            ((UIInput) toValidate).setValid(false);
            this.facesMessages.addFromResourceBundle("errorHelpdeskUserNotFound");
        }
    }
}
