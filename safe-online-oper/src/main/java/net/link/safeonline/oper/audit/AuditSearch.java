/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper.audit;

import javax.ejb.Local;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import net.link.safeonline.oper.OperatorConstants;
import net.link.safeonline.audit.exception.AuditContextNotFoundException;


@Local
public interface AuditSearch {

    public static final String JNDI_BINDING = OperatorConstants.JNDI_PREFIX + "AuditSearchBean/local";


    /*
     * Accessors
     */
    Long getSearchContextId();

    void setSearchContextId(Long searchContextId);

    String getSearchAuditUser();

    void setSearchAuditUser(String searchAuditUser);

    Integer getSearchLastTimeDays();

    void setSearchLastTimeDays(Integer searchLastTimeDays);

    Integer getSearchLastTimeHours();

    void setSearchLastTimeHours(Integer searchLastTimeHours);

    Integer getSearchLastTimeMinutes();

    void setSearchLastTimeMinutes(Integer searchLastTimeMinutes);

    /*
     * Factories
     */
    void auditContextListFactory();

    void accessAuditRecordListFactory();

    void securityAuditRecordListFactory();

    void resourceAuditRecordListFactory();

    void auditAuditRecordListFactory();

    /*
     * Actions
     */
    String view();

    String viewResourceRecords();

    String viewSecurityRecords();

    String viewAuditRecords();

    String removeContext()
            throws AuditContextNotFoundException;

    String search();

    String viewPrincipal();

    /*
     * Validators
     */
    void validateId(FacesContext context, UIComponent toValidate, Object value);

    void validateUser(FacesContext context, UIComponent toValidate, Object value);

    /*
     * Lifecycle
     */
    void destroyCallback();

}
