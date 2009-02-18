package net.link.safeonline.authentication.service;

import java.util.Map;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeUnavailableException;
import net.link.safeonline.authentication.exception.DatatypeMismatchException;
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.SubjectEntity;


/**
 * Service that fetches that attribute values or locally or remotely or externally.
 * 
 * @author wvdhaute
 * 
 */
@Local
public interface ProxyAttributeService extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "ProxyAttributeServiceBean/local";


    /**
     * Fetches an attribute from the specified subject ID.
     * 
     * @param userId
     * @param attributeName
     * @throws PermissionDeniedException
     * @throws AttributeTypeNotFoundException
     * @throws AttributeUnavailableException
     * @throws SubjectNotFoundException
     */
    Object findAttributeValue(String userId, String attributeName)
            throws PermissionDeniedException, AttributeTypeNotFoundException, AttributeUnavailableException, SubjectNotFoundException;

    /**
     * Fetches an attribute from the specified subject.
     * 
     * @param userId
     * @param attributeName
     * @throws PermissionDeniedException
     * @throws AttributeTypeNotFoundException
     * @throws AttributeUnavailableException
     * @throws SubjectNotFoundException
     */
    Object findAttributeValue(SubjectEntity subject, AttributeTypeEntity attributeType)
            throws PermissionDeniedException, AttributeTypeNotFoundException, AttributeUnavailableException, SubjectNotFoundException;

    /**
     * Creates an attribute for the specified subject with specified attribute value.
     * 
     * @param subject
     * @param attributeType
     * @param attributeValue
     * @throws DatatypeMismatchException
     * @throws PermissionDeniedException
     * @throws NodeNotFoundException
     * @throws SubjectNotFoundException
     */
    void createAttribute(SubjectEntity subject, AttributeTypeEntity attributeType, Object attributeValue)
            throws DatatypeMismatchException, PermissionDeniedException, SubjectNotFoundException, NodeNotFoundException;

    /**
     * Sets an attribute for the specified subject with the specified attribute value.
     * 
     * @param subject
     * @param attributeType
     * @param attributeValue
     * @throws AttributeNotFoundException
     * @throws DatatypeMismatchException
     * @throws PermissionDeniedException
     * @throws NodeNotFoundException
     * @throws SubjectNotFoundException
     */
    void setAttribute(SubjectEntity subject, AttributeTypeEntity attributeType, Object attributeValue)
            throws DatatypeMismatchException, AttributeNotFoundException, PermissionDeniedException, SubjectNotFoundException,
            NodeNotFoundException;

    /**
     * Sets a compound attribute for the specified subject with the specified attribute member values.
     * 
     * @param subject
     * @param attributeType
     * @param attributeId
     * @param memberValues
     * @throws AttributeNotFoundException
     * @throws DatatypeMismatchException
     * @throws PermissionDeniedException
     * @throws NodeNotFoundException
     * @throws SubjectNotFoundException
     */
    void setCompoundAttribute(SubjectEntity subject, AttributeTypeEntity attributeType, String attributeId, Map<String, Object> memberValues)
            throws net.link.safeonline.authentication.exception.AttributeNotFoundException, AttributeTypeNotFoundException,
            DatatypeMismatchException, PermissionDeniedException, SubjectNotFoundException, NodeNotFoundException;

    /**
     * Removes an attribute for the specified subject.
     * 
     * @param subject
     * @param attributeType
     * @throws AttributeNotFoundException
     * @throws PermissionDeniedException
     * @throws NodeNotFoundException
     * @throws SubjectNotFoundException
     */
    void removeAttribute(SubjectEntity subject, AttributeTypeEntity attributeType)
            throws AttributeNotFoundException, PermissionDeniedException, SubjectNotFoundException, NodeNotFoundException;

    /**
     * Removes a compound attribute for the specified subject and given compound attribute id.
     * 
     * @param subject
     * @param attributeType
     * @param attributeId
     * @throws AttributeNotFoundException
     * @throws PermissionDeniedException
     * @throws NodeNotFoundException
     * @throws SubjectNotFoundException
     */
    void removeCompoundAttribute(SubjectEntity subject, AttributeTypeEntity attributeType, String attributeId)
            throws PermissionDeniedException, net.link.safeonline.authentication.exception.AttributeNotFoundException,
            SubjectNotFoundException, NodeNotFoundException;
}
