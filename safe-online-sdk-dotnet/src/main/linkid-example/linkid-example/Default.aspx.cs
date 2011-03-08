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
using System.Text;
using System.Collections.Specialized;

using safe_online_sdk_dotnet;
using System.Security.Cryptography.X509Certificates;
using System.Xml.Serialization;
using AttributeWSNamespace;
using System.Xml;

namespace linkid_example
{
    public partial class _Default : System.Web.UI.Page
    {
        // linkID host to be used
        private static string LINKID_HOST = "sebeco-dev-11:8443";
        private static string LINKID_AUTH_ENTRY = "https://" + LINKID_HOST + "/linkid-auth/entry";
        private static string LINKID_LOGOUT_ENTRY = "https://" + LINKID_HOST + "/linkid-auth/logoutentry";

        // certificates and key locations
        private static string KEY_DIR = "C:/Users/Administrator/Documents/PKI/";
        private static string CERT_LINKID = KEY_DIR + "linkid.crt";
        private static string CERT_APP = KEY_DIR + "test.crt";
        private static string KEY_APP = KEY_DIR + "test.key";

        // application details
        private static string APP_NAME = "test";
        private static string APP_ATTRIBUTE = "test.test";
        private static string APP_LOCATION = "http://localhost/linkid-example/";
        // private static string APP_LOCATION = "http://localhost:49162/Default.aspx";

        /*
         * linkID authentication context session attribute
         * 
         * After a successfull authentication with linkID this will hold the returned   
         * AuthenticationProtocolContext object which contains the linkID user ID,
         * used authentication device(s) and optionally the returned linkID attributes
         * for the application.
         */
        private static string SESSION_AUTH_CONTEXT = "auth_context";

        /*
         * linkID authentication utility class session attribute
         * 
         * This object has to be stored in the session as sending out a request and validating
         * the response from linkID are both handled by this object and the response validation
         * includes making sure it belongs to the originating request.
         */
        private static string SESSION_SAML2_AUTH_UTIL = "saml2AuthUtil";

        /*
         * linkID logout utility class session attribute
         * 
         * Same reason as the authentication session attribute.
         */
        private static string SESSION_SAML2_LOGOUT_UTIL = "saml2LogoutUtil";

        protected void Page_Load(object sender, EventArgs e)
        {
            RSACryptoServiceProvider applicationKey = 
                KeyStoreUtil.GetPrivateKeyFromPem(KEY_APP, true);
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
            }
            /*
             * SAML 2 response was received and there was yet an authentication context on the session meaning
             * the user was logged in so we are dealing here with a logout response.
             */
            else if (null != responses)
            {
                Saml2LogoutUtil saml2LogoutUtil = (Saml2LogoutUtil)Session[SESSION_SAML2_LOGOUT_UTIL];
                string encodedLogoutResponse = responses[0];
                bool result = saml2LogoutUtil.validateEncodedLogoutResponse(encodedLogoutResponse, LINKID_HOST,
                                                                            applicationCert, linkidCert);
                if (false == result)
                {
                    this.ErrorLabel.Text = "Failed to logout";
                }
                else
                {
                    Session[SESSION_AUTH_CONTEXT] = null;
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
                Saml2AuthUtil saml2AuthUtil = new Saml2AuthUtil(applicationKey);
                Session[SESSION_SAML2_AUTH_UTIL] = saml2AuthUtil;
                this.form1.Action = LINKID_AUTH_ENTRY;
                this.HiddenField1.ID = RequestConstants.SAML2_POST_BINDING_REQUEST_PARAM;
                this.HiddenField1.Value = saml2AuthUtil.generateEncodedAuthnRequest(APP_NAME, null, null,
                    APP_LOCATION, LINKID_AUTH_ENTRY, null, false);
                this.Button1.Text = "Login";
            }
            /*
             * Authentication context found so user is logged in.
             * 
             * Show user ID, used device and test attribute.
             * Retrieve the test attribute using the web service client.
             * Generate a SAML2 logout request and put in the hidden field.
             */
            else
            {
                AuthenticationProtocolContext authContext = (AuthenticationProtocolContext)Session[SESSION_AUTH_CONTEXT];
                Saml2LogoutUtil saml2LogoutUtil = new Saml2LogoutUtil(applicationKey);
                Session[SESSION_SAML2_LOGOUT_UTIL] = saml2LogoutUtil;
                this.form1.Action = LINKID_LOGOUT_ENTRY;
                this.HiddenField1.ID = RequestConstants.SAML2_POST_BINDING_REQUEST_PARAM;
                this.HiddenField1.Value = saml2LogoutUtil.generateEncodedLogoutRequest(authContext.getUserId(),
                    APP_NAME, LINKID_LOGOUT_ENTRY);
                this.OutputLabel.Text = "UserID=" + authContext.getUserId() + " Device=" +
                    authContext.getAuthenticatedDevices()[0];
                if (null != authContext.getAttributes())
                {
                    AttributeSDK appAttribute = null;

                    // log attributes + refetch via Attribute Web Service
                    AttributeClient attributeClient = new AttributeClientImpl(LINKID_HOST, applicationCert, linkidCert);
                    foreach (String key in authContext.getAttributes().Keys)
                    {
                        if (key.Equals(APP_ATTRIBUTE))
                        {
                            appAttribute = authContext.getAttributes()[key].First();
                        }

                        this.OutputLabel.Text += "<p/>";
                        this.OutputLabel.Text += "<h2>Attribute=" + key + " via SAML Response</h2>";
                        logAttributes(authContext.getAttributes()[key]);

                        this.OutputLabel.Text += "<p/>";
                        this.OutputLabel.Text += " <h2>Attribute=" + key + " via Attribute Web Service</h2>";
                        List<AttributeSDK> wsAttributes = attributeClient.getAttributes(authContext.getUserId(), key);
                        logAttributes(wsAttributes);
                    }

                    if (null != appAttribute)
                    {
                        // update application attribute via Data Web Service
                        DataClient dataClient = new DataClientImpl(LINKID_HOST, applicationCert, linkidCert);

                        // set
                        appAttribute.setValue(appAttribute.getValue() + "-modified");
                        dataClient.setAttributeValue(authContext.getUserId(), appAttribute); 

                        // get
                        List<AttributeSDK> dataAttributes = 
                            dataClient.getAttributes(authContext.getUserId(), appAttribute.getAttributeName());
                        this.OutputLabel.Text += "<p/>";
                        this.OutputLabel.Text += " <h2>Attribute=" + appAttribute.getAttributeName() + " via Data Web Service (after modify)</h2>";
                        logAttributes(dataAttributes);

                        // remove
                        dataClient.removeAttribute(authContext.getUserId(), appAttribute);

                        // create
                        appAttribute.setValue(appAttribute.getValue() + "-created");
                        dataClient.createAttribute(authContext.getUserId(), appAttribute);

                        // get
                        dataAttributes = dataClient.getAttributes(authContext.getUserId(), appAttribute.getAttributeName());
                        this.OutputLabel.Text += "<p/>";
                        this.OutputLabel.Text += " <h2>Attribute=" + appAttribute.getAttributeName() + " via Data Web Service (after remove/create)</h2>";
                        logAttributes(dataAttributes);
                    }
                }
                this.Button1.Text = "Logout \"" + APP_NAME + "\"";
            }
        }

        private void logAttributes(List<AttributeSDK> attributes)
        {
            foreach (AttributeSDK attribute in attributes)
            {
                this.OutputLabel.Text += "AttributeID: " + attribute.getAttributeId() + "<br/>";
                if (attribute.getValue() is Compound)
                {
                    Compound compound = (Compound)attribute.getValue();
                    foreach (AttributeSDK member in compound.getMembers())
                    {
                        this.OutputLabel.Text +=
                            "  * Member: " + member.getAttributeName() + " value=" + member.getValue() + "<br/>";
                    }
                }
                else
                {
                    this.OutputLabel.Text += "Value: " + attribute.getValue() + "<br/>";
                }
            }
        }

        protected void Button1_Click(object sender, EventArgs e)
        {

        }
    }
}
