/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.attribute.provider.service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import net.link.safeonline.attribute.provider.AttributeCore;
import net.link.safeonline.attribute.provider.AttributeProvider;
import net.link.safeonline.attribute.provider.exception.AttributeNotFoundException;
import org.jetbrains.annotations.Nullable;


/**
 * LinkID Persistence Service. <p/>
 * <p/>
 * Offers use of the LinkID persistence layer towards {@link AttributeProvider}s.
 */
public interface PersistenceService {

    /**
     * @param userId          userId to return attributes from
     * @param attributeName   attribute to return values for
     * @param filterInvisible filter userInvisble member attributes of compounds.
     *
     * @return all {@link AttributeCore}'s for specified user and attribute name.
     */
    List<AttributeCore> listAttributes(String userId, String attributeName, boolean filterInvisible);

    /**
     * Fetch attribute for specified user and attribute ID.
     *
     * @param userId        userId to find attribute for
     * @param attributeName attribute type of attribute to find
     * @param attributeId   attribute ID of attribute to find.
     *
     * @return {@link AttributeCore} or {@code null} if not found.
     */
    @Nullable
    AttributeCore findAttribute(String userId, String attributeName, String attributeId);

    /**
     * Fetch compound attribute of specified type which has a member of specified type with specified value
     *
     * @param userId              userId to find attribute for
     * @param parentAttributeName attribute type of the parent
     * @param memberAttributeName attribute type of the member
     * @param memberValue         value of the member attribute
     *
     * @return {@link AttributeCore} or {@code null} if not found.
     */
    @Nullable
    AttributeCore findCompoundAttributeWhere(String userId, String parentAttributeName, String memberAttributeName,
                                             Serializable memberValue);

    /**
     * Removes an attribute for the specified subject.
     *
     * @param userId        userId to remove the attributes from
     * @param attributeName attribute type of values to be removed
     */
    void removeAttributes(String userId, String attributeName);

    /**
     * Removes an attribute for the specified subject.
     *
     * @param userId        userId to remove the attribute from
     * @param attributeName attribute type of value to be removed
     * @param attributeId   attributeId of value to be removed
     *
     * @throws AttributeNotFoundException no value found.
     */
    void removeAttribute(String userId, String attributeName, String attributeId)
            throws AttributeNotFoundException;

    /**
     * Remove all attributes with specified attribute name.
     *
     * @param attributeName attribute type of attributes to remove.
     */
    void removeAttributes(String attributeName);

    /**
     * Create/modify the specified {@link AttributeCore} for specified user.
     *
     * @param userId    userId to set attribute for.
     * @param attribute attribute to set for subject.
     *
     * @return the updated/created attribute.
     */
    AttributeCore setAttribute(String userId, AttributeCore attribute);

    /**
     * @param subjects      list of userIds to query for
     * @param attributeName name of the attribute
     *
     * @return map containing a list of unique values of an attribute with a count of how many times these values occur
     */
    Map<Serializable, Long> categorize(List<String> subjects, String attributeName);
}
