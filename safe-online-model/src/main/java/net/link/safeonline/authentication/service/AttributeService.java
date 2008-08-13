/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service;

import java.util.Map;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;


/**
 * Attribute Service. To be used by applications to retrieve attributes of their users.
 * 
 * <p>
 * Applications can only retrieve attributes for which the user did confirm the corresponding application identity.
 * Application also cannot retrieve attributes that are marked as data-mining attribute types.
 * </p>
 * 
 * @author fcorneli
 * 
 */
@Local
public interface AttributeService {

    /**
     * Gives back the value of an attribute of a certain subject. The subject must have confirmed attribute usage before
     * the application is allowed to access the attribute value. The attribute should not be marked as data-mining
     * attribute type.
     * 
     * <p>
     * The returned object can be a {@link String} or {@link Boolean}, ... depending on the actual datatype used by
     * corresponding attribute type of the requested attribute. In case of a multivalued attribute the returned object
     * will be an array. In case of a compounded a Map will be returned. In case of a multivalued compounded an array of
     * Maps will be returned.
     * </p>
     * 
     * @param subjectLogin
     * @param attributeName
     * @throws AttributeNotFoundException
     * @throws PermissionDeniedException
     * @throws AttributeTypeNotFoundException
     * @throws SubjectNotFoundException
     */
    Object getConfirmedAttributeValue(String subjectLogin, String attributeName) throws AttributeNotFoundException,
            PermissionDeniedException, AttributeTypeNotFoundException, SubjectNotFoundException;

    /**
     * Returns a map of attributes with values of the given subject. Of course the subject needs to be subscribed onto
     * the current caller application and the attributes returned are those that have been confirmed by the user.
     * 
     * <p>
     * It is possible that a user already confirmed an attribute usage over an attribute that he still needs to define.
     * In this case the resulting map will not contain an entry for the missing attribute.
     * </p>
     * 
     * <p>
     * The type of map values depends on the actual datatype used by the corresponding attribute type of the attribute.
     * In case of a multivalued attribute the map value will be an array.
     * </p>
     * 
     * @param subjectLogin
     * @throws PermissionDeniedException
     * @throws AttributeTypeNotFoundException
     * @throws SubjectNotFoundException
     */
    Map<String, Object> getConfirmedAttributeValues(String subjectLogin) throws PermissionDeniedException,
            AttributeTypeNotFoundException, SubjectNotFoundException;
}
