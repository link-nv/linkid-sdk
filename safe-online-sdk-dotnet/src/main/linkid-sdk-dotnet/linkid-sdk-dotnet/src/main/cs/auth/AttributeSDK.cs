/*
 * Created by SharpDevelop.
 * User: devel
 * Date: 22/12/2008
 * Time: 12:18
 * 
 * To change this template use Tools | Options | Coding | Edit Standard Headers.
 */
using System;
using System.Collections;
using System.Collections.Generic;

namespace safe_online_sdk_dotnet
{
    /// <summary>
    /// Description of AttributeAbstract.
    /// </summary>
    public class AttributeSDK
    {
        private String attributeId;
        private String attributeName;

        private Boolean unavailable;

        protected Object value;

        public AttributeSDK(String attributeId, String attributeName, Object value)
        {
            this.attributeId = attributeId;
            this.attributeName = attributeName;
            this.value = value;
        }

        public AttributeSDK(String attributeId, String attributeName)
        {
            this.attributeId = attributeId;
            this.attributeName = attributeName;
        }

        public String getAttributeId()
        {
            return attributeId;
        }

        public void setAttributeId(String attributeId)
        {
            this.attributeId = attributeId;
        }

        public String getAttributeName()
        {
            return this.attributeName;
        }

        public void setAttributeName(String attributeName)
        {
            this.attributeName = attributeName;
        }

        public Object getValue()
        {
            return value;
        }

        public void setValue(Object value)
        {
            this.value = value;
        }

        public Boolean isUnavailable()
        {
            return this.unavailable;
        }

        public void setUnavailable(Boolean unavailable)
        {
            this.unavailable = unavailable;
        }

    }
}
