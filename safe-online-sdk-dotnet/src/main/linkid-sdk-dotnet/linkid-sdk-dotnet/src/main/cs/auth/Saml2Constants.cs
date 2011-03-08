/*
 * Created by SharpDevelop.
 * User: devel
 * Date: 22/12/2008
 * Time: 23:38
 * 
 * To change this template use Tools | Options | Coding | Edit Standard Headers.
 */
using System;

namespace safe_online_sdk_dotnet
{
	/// <summary>
	/// Contains various SAML v2.0 constants
	/// </summary>
	public class Saml2Constants
	{
		public static readonly string SAML2_ASSERTION_NAMESPACE = "urn:oasis:names:tc:SAML:2.0:assertion";
		public static readonly string SAML2_PROTOCOL_NAMESPACE = "urn:oasis:names:tc:SAML:2.0:protocol";
		
		public static readonly string SAML2_STATUS_SUCCESS = "urn:oasis:names:tc:SAML:2.0:status:Success";
		public static readonly string SAML2_STATUS_AUTHN_FAILED = "urn:oasis:names:tc:SAML:2.0:status:AuthnFailed";
		public static readonly string SAML2_STATUS_UNKNOWN_PRINCIPAL = "urn:oasis:names:tc:SAML:2.0:status:UnknownPrincipal";
		public static readonly string SAML2_STATUS_REQUESTER = "urn:oasis:names:tc:SAML:2.0:status:Requester";
		public static readonly string SAML2_STATUS_RESPONDER = "urn:oasis:names:tc:SAML:2.0:status:Responder";
		public static readonly string SAML2_STATUS_VERSION_MISMTACH = "urn:oasis:names:tc:SAML:2.0:status:VersionMismatch";
		public static readonly string SAML2_STATUS_INVALID_ATTRIBUTE_NAME_OR_VALUE = "urn:oasis:names:tc:SAML:2.0:status:InvalidAttrNameOrValue";
		public static readonly string SAML2_STATUS_REQUEST_DENIED = "urn:oasis:names:tc:SAML:2.0:status:RequestDenied";
		public static readonly string SAML2_STATUS_INVALID_NAMEID_POLICY = "urn:oasis:names:tc:SAML:2.0:status:InvalidNameIDPolicy";
		public static readonly string SAML2_STATUS_ATTRIBUTE_UNAVAILABLE = "urn:net:lin-k:safe-online:SAML:2.0:status:AttributeUnavailable";
		
		public static readonly string SAML2_STATUS_PARTIAL_LOGOUT = "urn:oasis:names:tc:SAML:2.0:status;PartialLogout";
		
		public static readonly string SAML2_NAMEID_FORMAT_PERSISTENT = "urn:oasis:names:tc:SAML:2.0:nameid-format:persistent";
		public static readonly string SAML2_NAMEID_FORMAT_ENTITY = "urn:oasis:names:tc:SAML:2.0:nameid-format:entity";
		public static readonly string SAML2_BINDING_HTTP_POST = "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST";
		
		private Saml2Constants()
		{
		}
	}
}
