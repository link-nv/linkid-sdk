using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.UI;
using System.Web.UI.WebControls;

using System.Security.Cryptography;
using System.Net;
using System.Net.Security;
using System.IO;
using System.Collections.Specialized;

using safe_online_sdk_dotnet;
using System.Security.Cryptography.X509Certificates;
using System.Xml.Serialization;
using System.Xml;


namespace linkid_example
{
    public partial class LinkIDLogin : System.Web.UI.Page
    {
        /*
         * Application specific configuration...
         */
        // linkID host to be used
        public static string LINKID_HOST = "demo.linkid.be";

        // location of this page, linkID will post its authentication response back to this location.
        private static string LOGINPAGE_LOCATION = "http://localhost:49162/LinkIDLogin.aspx";

        // application details
        public static string APP_NAME = "demo-test";

        // certificates and key locations
        public static string KEY_DIR = "C:\\cygwin\\home\\devel\\keystores\\";
        public static string CERT_LINKID = KEY_DIR + "linkid.crt";
        public static string CERT_APP = KEY_DIR + "demotest.crt";
        public static string KEY_APP = KEY_DIR + "demotest.key";

        /*
         * linkID authentication context session attribute
         * 
         * After a successfull authentication with linkID this will hold the returned   
         * AuthenticationProtocolContext object which contains the linkID user ID,
         * used authentication device(s) and optionally the returned linkID attributes
         * for the application.
         */
        public static string SESSION_AUTH_CONTEXT = "linkID.authContext";

        /*
         * All code below should not be modified.
         */
        private static string LINKID_AUTH_ENTRY = "https://" + LINKID_HOST + "/linkid-auth/entry";
        private static string LINKID_MOBILE_MINIMAL_ENTRY = "https://" + LINKID_HOST + "/linkid-qr/auth-min";
        private static string LINKID_MOBILE_MODAL_ENTRY = "https://" + LINKID_HOST + "/linkid-qr/auth";

        // Session attributes
        private static string SESSION_SAML2_AUTH_UTIL = "linkID.saml2AuthUtil";
        private static string SESSION_TARGET_URI = "linkID.targetURI";
        private static string SESSION_LOGIN_MODE = "linkID.loginMode";
        private static string SESSION_MOBILE_AUTH = "linkID.mobileAuthn";
        private static string SESSION_MOBILE_AUTH_MINIMAL = "linkID.mobileAuthnMinimal";

        protected void Page_Load(object sender, EventArgs e)
        {
            // Load applications's keypair and linkID's certificate
            RSACryptoServiceProvider applicationKey = KeyStoreUtil.GetPrivateKeyFromPem(KEY_APP, true);
            X509Certificate2 applicationCert = new X509Certificate2(CERT_APP);
            applicationCert.PrivateKey = applicationKey;
            X509Certificate2 linkidCert = new X509Certificate2(CERT_LINKID);

            string[] responses = Page.Request.Form.GetValues(RequestConstants.SAML2_POST_BINDING_RESPONSE_PARAM);
            /*
             * If a SAML2 response was found but no authentication context was on the session we received a
             * SAML2 authentication response.
             */
            if (null != responses && null == Session[SESSION_AUTH_CONTEXT])
            {
                Saml2AuthUtil saml2AuthUtil = (Saml2AuthUtil)Session[SESSION_SAML2_AUTH_UTIL];
                string encodedSaml2Response = responses[0];

                AuthenticationProtocolContext
                     context = saml2AuthUtil.validateEncodedAuthnResponse(encodedSaml2Response, LINKID_HOST,
                                                                          applicationCert, linkidCert);
                Session[SESSION_AUTH_CONTEXT] = context;

                String targetURI = (String) Session[SESSION_TARGET_URI];
                
                String loginModeString = (String) Request[RequestConstants.LOGIN_MODE_REQUEST_PARAM];
                if (null == loginModeString) loginModeString = (String)Session[SESSION_LOGIN_MODE];
                if (null == loginModeString) loginModeString = "POPUP";

                bool mobileAuthn = null != Session[SESSION_MOBILE_AUTH];
                bool mobileAuthnMinimal = null != Session[SESSION_MOBILE_AUTH_MINIMAL];

                if (loginModeString == "POPUP" || mobileAuthn || mobileAuthnMinimal)
                {
                    Response.ContentType = "text/html";
                    Response.Write("<html>");
                    Response.Write("<head>");
                    Response.Write("<script type=\"text/javascript\">");
                    if (mobileAuthn || mobileAuthnMinimal)
                    {
                        Response.Write("window.top.location.replace(\"" + targetURI + "\");");
                    }
                    else
                    {
                        Response.Write("window.opener.location.href = \"" + targetURI + "\";");
                        Response.Write("window.close();");
                    }
                    Response.Write("</script>");
                    Response.Write("</head>");
                    Response.Write("<body>");
                    Response.Write("<noscript><p>You are successfully logged in. Since your browser does not support JavaScript, you must close this popup window and refresh the original window manually.</p></noscript>");
                    Response.Write("</body>");
                    Response.Write("</html>");
                }
                else
                {
                    Response.Redirect(targetURI, true);
                    return;
                }

            }

            /*
             * No authentication context found so not yet logged in.
             * 
             * Generate a SAML2 authentication request and store in the hiddenfield.
             * Put the used authentication utility class on the session.
             */
            if (null == Session[SESSION_AUTH_CONTEXT])
            {
                /*
                 * Check page's request parameters.
                 * They will contain e.g. 
                 *   what linkID authentication mode (mobile, direct, ....), 
                 *   optional target URL to redirect to after handling a linkID authentication response, ...
                 */
                bool mobile = null != Request[RequestConstants.MOBILE_AUTHN_REQUEST_PARAM];
                bool mobileMinimal = null != Request[RequestConstants.MOBILE_AUTHN_MINIMAL_REQUEST_PARAM];
                String targetURI = Request[RequestConstants.TARGET_URI_REQUEST_PARAM];
                String loginMode = Request[RequestConstants.LOGIN_MODE_REQUEST_PARAM];
                String startPage = Request[RequestConstants.START_PAGE_REQUEST_PARAM];

                String linkIDLandingPage = LINKID_AUTH_ENTRY;
                if (mobile)
                {
                    linkIDLandingPage = LINKID_MOBILE_MODAL_ENTRY;
                    Session[SESSION_MOBILE_AUTH] = "true";
                }
                if (mobileMinimal)
                {
                    linkIDLandingPage = LINKID_MOBILE_MINIMAL_ENTRY;
                    Session[SESSION_MOBILE_AUTH_MINIMAL] = "true";
                }
                // Construct the SAML v2.0 Authentication request and fill in the form parameters
                Saml2AuthUtil saml2AuthUtil = new Saml2AuthUtil(applicationKey);
                Session[SESSION_SAML2_AUTH_UTIL] = saml2AuthUtil;
                Session[SESSION_LOGIN_MODE] = loginMode;
                if (null != targetURI) Session[SESSION_TARGET_URI] = targetURI;

                this.form1.Action = linkIDLandingPage;

                this.SAMLRequest.ID = RequestConstants.SAML2_POST_BINDING_REQUEST_PARAM;
                this.SAMLRequest.Value = saml2AuthUtil.generateEncodedAuthnRequest(APP_NAME, null, null,
                    LOGINPAGE_LOCATION, linkIDLandingPage, null, false);

                if (null != loginMode)
                {
                    this.LoginMode.ID = RequestConstants.LOGIN_MODE_REQUEST_PARAM;
                    this.LoginMode.Value = loginMode;
                }
                if (null != startPage)
                {
                    this.StartPage.ID = RequestConstants.START_PAGE_REQUEST_PARAM;
                    this.StartPage.Value = startPage;
                }
            }
        }
    }
}
