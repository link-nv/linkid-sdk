/*
 * Created by SharpDevelop.
 * User: devel
 * Date: 23/12/2008
 * Time: 10:23
 * 
 * To change this template use Tools | Options | Coding | Edit Standard Headers.
 */
using System;

namespace safe_online_sdk_dotnet
{
	/// <summary>
	/// Description of WebServiceConstants.
	/// </summary>
	public class WebServiceConstants
	{
		public static readonly string SAFE_ONLINE_SAML_NAMESPACE = "urn:net:lin-k:safe-online:saml";
		public static readonly string SAFE_ONLINE_SAML_PREFIX = "sosaml";
		
		public static readonly string MUTLIVALUED_ATTRIBUTE = "multivalued";
        public static readonly string ATTRIBUTE_ID_ATTRIBUTE = "attributeId";
        public static readonly string ATTRIBUTE_NAME_ATTRIBUTE = "Name";
		
		private WebServiceConstants()
		{
		}
	}
}
