using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.UI;
using System.Web.UI.WebControls;

using safe_online_sdk_dotnet;

namespace linkid_example
{
    public partial class LoggedIn : System.Web.UI.Page
    {
        protected void Page_Load(object sender, EventArgs e)
        {

            AuthenticationProtocolContext authContext =
                (AuthenticationProtocolContext)Session[LinkIDLogin.SESSION_AUTH_CONTEXT];
            if (null != authContext)
            {
                this.OutputLabel.Text = "<h1>Successfully authenticated</h1>";
                this.OutputLabel.Text += "<p>UserID=" + authContext.getUserId() + 
                    " authenticated using device " + authContext.getAuthenticatedDevices()[0] + "</p>";
                if (null != authContext.getAttributes())
                {
                    // log attributes
                    foreach (String key in authContext.getAttributes().Keys)
                    {
                        this.OutputLabel.Text += "<p/>";
                        this.OutputLabel.Text += "<h2>Attribute=" + key + " via SAML Response</h2>";
                        logAttributes(authContext.getAttributes()[key]);
                    }
                }
            }
            else 
            {
                this.OutputLabel.Text = "Not logged in yet...";
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

    }
}
