/*
 * Gemaakt met SharpDevelop.
 * Gebruiker: Tester
 * Datum: 20-10-2009
 * Tijd: 13:23
 * 
 * Dit sjabloon wijzigen: Extra | Opties |Coderen | Standaard kop bewerken.
 */
using System;

namespace safe_online_sdk_dotnet
{
	/// <summary>
	/// Contains some request parameter names
	/// </summary>
	public sealed class RequestConstants
	{
		public static readonly String SAML2_POST_BINDING_REQUEST_PARAM = "SAMLRequest";
		public static readonly String SAML2_POST_BINDING_RESPONSE_PARAM = "SAMLResponse";
		
		public static readonly String LANGUAGE_REQUEST_PARAM = "Language";
		public static readonly String THEME_REQUEST_PARAM = "ThemeName";
		
		private RequestConstants()
		{
		}
	}
}
