/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.attribute.provider.service;

import java.io.Serializable;
import java.util.List;
import net.link.safeonline.attribute.provider.AttributeCore;
import net.link.safeonline.attribute.provider.exception.*;


/**
 * LinkID Attribute Service. <p/>
 * <p/>
 * Offers fetching of LinkID attributes.
 */
public interface AttributeService {

    /**
     * @param userId          userId to return attributes from
     * @param attributeName   attribute to return values for
     * @param filterInvisible filter user invisible attributes
     *
     * @return all {@link AttributeCore}'s for specified user and attribute name, filtered on user inivisiblity if specified.
     *
     * @throws AttributeTypeNotFoundException attribute type for specified attribute name does not exist.
     * @throws SubjectNotFoundException       subject does not exist
     */
    List<AttributeCore> listAttributes(String userId, String attributeName, boolean filterInvisible)
            throws SubjectNotFoundException, AttributeTypeNotFoundException;

    /**
     * Fetch attribute for specified user and attribute ID.
     *
     * @param userId        userId to find attribute for
     * @param attributeName attribute type of attribute to find
     * @param attributeId   attribute ID of attribute to find.
     *
     * @return {@link AttributeCore} or {@code null} if not found.
     *
     * @throws AttributeTypeNotFoundException attribute type for specified attribute name does not exist.
     * @throws SubjectNotFoundException       subject does not exist
     */
    AttributeCore findAttribute(String userId, String attributeName, String attributeId)
            throws SubjectNotFoundException, AttributeTypeNotFoundException;

    /**
     * Fetch compound attribute of specified type which has a member of specified type with specified value
     *
     * @param userId              userId to find attribute for
     * @param parentAttributeName attribute type of the parent
     * @param memberAttributeName attribute type of the member
     * @param memberValue         value of the member attribute
     *
     * @return {@link AttributeCore} or {@code null} if not found.
     *
     * @throws AttributeTypeNotFoundException attribute type for specified attribute name does not exist.
     * @throws SubjectNotFoundException       subject does not exist
     */
    AttributeCore findCompoundAttributeWhere(String userId, String parentAttributeName, String memberAttributeName,
                                             Serializable memberValue)
            throws SubjectNotFoundException, AttributeTypeNotFoundException;

    /**
     * Removes an attribute for the specified subject.
     *
     * @param userId        userId to remove the attributes from
     * @param attributeName attribute type of values to be removed
     *
     * @throws AttributeTypeNotFoundException attribute type for specified attribute name does not exist.
     * @throws SubjectNotFoundException       subject does not exist
     */
    void removeAttributes(String userId, String attributeName)
            throws SubjectNotFoundException, AttributeTypeNotFoundException;

    /**
     * Removes an attribute for the specified subject.
     *
     * @param userId        userId to remove the attribute from
     * @param attributeName attribute type of value to be removed
     * @param attributeId   attributeId of value to be removed
     *
     * @throws AttributeNotFoundException     no value found.
     * @throws AttributeTypeNotFoundException attribute type for specified attribute name does not exist.
     * @throws SubjectNotFoundException       subject does not exist
     */
    void removeAttribute(String userId, String attributeName, String attributeId)
            throws SubjectNotFoundException, AttributeTypeNotFoundException, AttributeNotFoundException;

    /**
     * Remove all attributes with specified attribute name.
     *
     * @param attributeName attribute type of attributes to remove.
     *
     * @throws AttributeTypeNotFoundException attribute type for specified attribute name does not exist.
     */
    void removeAttributes(String attributeName)
            throws AttributeTypeNotFoundException;

    /**
     * Create/modify the specified {@link AttributeCore} for specified user.
     *
     * @param userId    userId to set attribute for.
     * @param attribute attribute to set for subject.
     *
     * @return the updated/created attribute.
     *
     * @throws AttributeNotFoundException case attributeId was specified for update but no value was found.
     * @throws SubjectNotFoundException   subject does not exist
     */
    AttributeCore setAttribute(String userId, AttributeCore attribute)
            throws AttributeNotFoundException, SubjectNotFoundException;
}
