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
    public partial class LinkIDLogout : System.Web.UI.Page
    {
        /*
         * All code below should not be modified.
         */
        private static string LINKID_LOGOUT_ENTRY = "https://" + LinkIDLogin.LINKID_HOST + "/linkid-auth/logoutentry";

        /*
         * linkID logout utility class session attribute
         * 
         * Same reason as the authentication session attribute.
         */
        private static string SESSION_SAML2_LOGOUT_UTIL = "linkID.saml2LogoutUtil";

        protected void Page_Load(object sender, EventArgs e)
        {
            // Load applications's keypair and linkID's certificate
            RSACryptoServiceProvider applicationKey = KeyStoreUtil.GetPrivateKeyFromPem(LinkIDLogin.KEY_APP, true);
            X509Certificate2 applicationCert = new X509Certificate2(LinkIDLogin.CERT_APP);
            applicationCert.PrivateKey = applicationKey;
            X509Certificate2 linkidCert = new X509Certificate2(LinkIDLogin.CERT_LINKID);

            string[] responses = Page.Request.Form.GetValues(RequestConstants.SAML2_POST_BINDING_RESPONSE_PARAM);

            AuthenticationProtocolContext authContext =
                (AuthenticationProtocolContext)Session[LinkIDLogin.SESSION_AUTH_CONTEXT];

            // Single logout response received from linkID
            if (null != responses && null != authContext)
            {
                Saml2LogoutUtil saml2LogoutUtil = (Saml2LogoutUtil)Session[SESSION_SAML2_LOGOUT_UTIL];
                string encodedLogoutResponse = responses[0];
                bool result = saml2LogoutUtil.validateEncodedLogoutResponse(encodedLogoutResponse, LinkIDLogin.LINKID_HOST,
                                                                            applicationCert, linkidCert);
                if (false == result)
                {
                    this.ErrorLabel.Text = "Failed to logout";
                }
                else
                {
                    Session[LinkIDLogin.SESSION_AUTH_CONTEXT] = null;
                    this.LogoutButton.Visible = false;
                    this.ErrorLabel.Text = "Successfully logged out...";
                }
            }
            else
            {
                if (null != authContext)
                {
                    Saml2LogoutUtil saml2LogoutUtil = new Saml2LogoutUtil(applicationKey);
                    Session[SESSION_SAML2_LOGOUT_UTIL] = saml2LogoutUtil;
                    this.form1.Action = LINKID_LOGOUT_ENTRY;
                    this.HiddenField1.ID = RequestConstants.SAML2_POST_BINDING_REQUEST_PARAM;
                    this.HiddenField1.Value = saml2LogoutUtil.generateEncodedLogoutRequest(authContext.getUserId(),
                        LinkIDLogin.APP_NAME, LINKID_LOGOUT_ENTRY);
                }
                else
                {
                    this.ErrorLabel.Text = "Single logout but not logged in ?!";
                    this.LogoutButton.Enabled = false;
                }
            }
        }

        protected void Button1_Click(object sender, EventArgs e)
        {

        }

    }
}
