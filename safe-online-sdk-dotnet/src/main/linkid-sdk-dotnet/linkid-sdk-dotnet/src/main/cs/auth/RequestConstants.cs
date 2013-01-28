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

        // Mobile authentication flows
        public static readonly String MOBILE_AUTHN_REQUEST_PARAM = "mobileAuthn";
        public static readonly String MOBILE_AUTHN_MINIMAL_REQUEST_PARAM = "mobileAuthnMinimal";

        // Case SP wants to force linkID to go straight to registration or authentication page
        public static readonly String START_PAGE_REQUEST_PARAM = "start_page";

        public static readonly String LANGUAGE_REQUEST_PARAM = "Language";
		public static readonly String THEME_REQUEST_PARAM = "ThemeName";
        public static readonly String LOGIN_MODE_REQUEST_PARAM = "login_mode";
        public static readonly String TARGET_URI_REQUEST_PARAM = "return_uri";

		
		private RequestConstants()
		{
		}
	}
}
