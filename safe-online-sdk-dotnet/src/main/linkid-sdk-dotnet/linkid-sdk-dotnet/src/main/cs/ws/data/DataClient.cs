/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 	Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

using System;
using System.Collections.Generic;

namespace safe_online_sdk_dotnet
{
    public interface DataClient
    {
        /// <summary>
        /// Sets an attribute for specified user. If not yet created will do so.
        /// </summary>
        /// <param name="userId"></param>
        /// <param name="attribute"></param>
        void setAttributeValue(string userId, AttributeSDK attribute);

        /// <summary>
        /// Sets multiple attribute for specified user. If not yet created will do so.
        /// </summary>
        /// <param name="userId"></param>
        /// <param name="attributes"></param>
        void setAttributeValue(String userId, List<AttributeSDK> attributes);

        /// <summary>
        /// Gives back all attribute values for specified attribute
        /// </summary>
        /// <param name="userId"></param>
        /// <param name="attributeName"></param>
        /// <returns></returns>
        List<AttributeSDK> getAttributes(String userId, String attributeName);

        /// <summary>
        /// Creates a new attribute for the given subject.
        /// </summary>
        /// <param name="userId"></param>
        /// <param name="attribute"></param>
        void createAttribute(String userId, AttributeSDK attribute);

        /// <summary>
        /// Create a new list of attributes for the given subject.
        /// </summary>
        /// <param name="userId"></param>
        /// <param name="attributes"></param>
        void createAttribute(String userId, List<AttributeSDK> attributes);

        void removeAttributes(String userId, String attributeName);

        void removeAttribute(String userId, String attributeName, String attributeId);

        void removeAttribute(String userId, AttributeSDK attribute);

        void removeAttributes(String userId, List<AttributeSDK> attributes);
    }
}
