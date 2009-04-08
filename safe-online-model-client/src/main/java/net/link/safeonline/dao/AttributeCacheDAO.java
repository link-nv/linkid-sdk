/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.entity.AttributeCacheEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.SubjectEntity;


/**
 * Data Access Object interface for Attribute Cache entities.
 * 
 * @author wvdhaute
 * 
 */
@Local
public interface AttributeCacheDAO extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "AttributeCacheDAOBean/local";


    /**
     * @param subject
     * @param attributeType
     * @return List all cached attributes for specified subject and attribute type
     */
    List<AttributeCacheEntity> listAttributes(SubjectEntity subject, AttributeTypeEntity attributeType);

    /**
     * TODO
     */
    void removeAttributes(SubjectEntity subject, AttributeTypeEntity attributeType);

    /**
     * TODO
     */
    AttributeCacheEntity findAttribute(SubjectEntity subject, AttributeTypeEntity attributeType, long index);

    /**
     * TODO
     */
    AttributeCacheEntity addAttribute(AttributeTypeEntity attributeType, SubjectEntity subject, long index);

    /**
     * TODO
     */
    void removeAttribute(AttributeCacheEntity attribute);

    /**
     * TODO
     */
    List<AttributeCacheEntity> listAttributes();

    /**
     * Removes all the attributes of the given attribue type.
     * 
     * @param attributeType
     */
    void removeAttributes(AttributeTypeEntity attributeType);

}
